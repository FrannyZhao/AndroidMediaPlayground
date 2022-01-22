package com.franny.androidmediaplayground.media

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import timber.log.Timber
import java.lang.Exception
import java.lang.IllegalStateException
import java.nio.ByteBuffer

class AudioTrackManager {
    interface IAudioPlayStateListener {
        fun onStart()
        fun onStop()
    }

    private var mAudioTrack: AudioTrack? = null
    private var bufferCount: Long = 0

    /**
     * 指定缓冲区大小。调用AudioTrack类的getMinBufferSize方法可以获得。
     */
    private var mMinBufferSize = 0
    private var iAudioPlayStateListener: IAudioPlayStateListener? = null
    private fun initAudioTrack() {
        //根据采样率，采样精度，单双声道来得到frame的大小。
        //计算最小缓冲区 *10
        mMinBufferSize = AudioTrack.getMinBufferSize(mSampleRateInHz, mChannelConfig, mAudioFormat)
        Timber.i("initAudioTrack:  mMinBufferSize: " + mMinBufferSize * BUFFER_CAPITAL + " b")
        //注意，按照数字音频的知识，这个算出来的是一秒钟buffer的大小。
        mAudioTrack = AudioTrack(
            mStreamType, mSampleRateInHz, mChannelConfig,
            mAudioFormat, mMinBufferSize * BUFFER_CAPITAL, mMode
        )
    }

    fun addAudioPlayStateListener(iAudioPlayStateListener: IAudioPlayStateListener?) {
        this.iAudioPlayStateListener = iAudioPlayStateListener
    }

    fun prepareAudioTrack() {
        bufferCount = 0
        Timber.i("prepareAudioTrack:------> ")
        if (null == mAudioTrack) {
            return
        }
        if (mAudioTrack!!.state == AudioTrack.STATE_UNINITIALIZED) {
            initAudioTrack()
        }
        mAudioTrack!!.play()
        iAudioPlayStateListener?.onStart()
    }

    @Synchronized
    fun write(bytes: ByteBuffer) {
        if (null != mAudioTrack) {
            val byteSize: Int = bytes.array().size
            bufferCount += byteSize.toLong()
            val write: Int = mAudioTrack!!.write(bytes, 0, byteSize)
            Timber.d("write: 接收到数据 $byteSize b | 已写入 $bufferCount b")
            if (write == 0 && null != iAudioPlayStateListener) {
                //由于缓存的缘故，会先把缓存的bytes填满再播放，当write=0的时候存在没有播完的情况
                try {
                    Thread.sleep(2000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                iAudioPlayStateListener?.onStop()
            }
        }
    }

    fun stopPlay() {
        Timber.i("stopPlay: ")
        if (null == mAudioTrack) {
            return
        }
        iAudioPlayStateListener?.onStop()
        try {
            if (mAudioTrack!!.playState == AudioTrack.PLAYSTATE_PLAYING) {
                mAudioTrack!!.stop()
            }
        } catch (e: IllegalStateException) {
            Timber.e("stop: $e")
            e.printStackTrace()
        }
    }

    fun release() {
        if (null == mAudioTrack) {
            return
        }
        Timber.i("release: ")
        stopPlay()
        iAudioPlayStateListener = null
        try {
            mAudioTrack!!.release()
            mAudioTrack = null
        } catch (e: Exception) {
            Timber.e("release: $e")
            e.printStackTrace()
        }
    }

    fun setBufferParams(pcmFileSize: Int) {
        //设置缓冲的大小 为PCM文件大小的10%
        Timber.d(
            "setFileSize: PCM文件大小为：" + pcmFileSize + " b 最小缓存空间为 " + mMinBufferSize * BUFFER_CAPITAL + " b"
        )
        if (pcmFileSize < mMinBufferSize * BUFFER_CAPITAL) {
            mAudioTrack = AudioTrack(
                mStreamType, mSampleRateInHz, mChannelConfig,
                mAudioFormat, mMinBufferSize, mMode
            )
            Timber.d("setFileSize: pcmFileSize 文件小于最小缓冲数据的10倍，修改为默认的1倍------>")
        } else {
            //缓存大小为PCM文件大小的10%，如果小于mMinBufferSize * BUFFER_CAPITAL，则按默认值设置
            val cacheFileSize = (pcmFileSize * 0.1).toInt()
            var realBufferSize = (cacheFileSize / mMinBufferSize + 1) * mMinBufferSize
            Timber.d(
                "计算得到缓存空间为: " + realBufferSize + " b 最小缓存空间为 " + mMinBufferSize * BUFFER_CAPITAL + " b"
            )
            if (realBufferSize < mMinBufferSize * BUFFER_CAPITAL) {
                realBufferSize = mMinBufferSize * BUFFER_CAPITAL
            }
            mAudioTrack = AudioTrack(
                mStreamType, mSampleRateInHz, mChannelConfig,
                mAudioFormat, realBufferSize, mMode
            )
            Timber.d(
                "setFileSize: 重置缓存空间为： " + realBufferSize + " b | " + realBufferSize / 1024 + " kb"
            )
        }
        bufferCount = 0
    }

    companion object {
        @Volatile
        private var mInstance: AudioTrackManager? = null

        /**
         * 音频流类型
         */
        private const val mStreamType = AudioManager.STREAM_MUSIC

        /**
         * 指定采样率 （MediaRecoder 的采样率通常是8000Hz AAC的通常是44100Hz。
         * 设置采样率为44100，目前为常用的采样率，官方文档表示这个值可以兼容所有的设置）
         */
        private const val mSampleRateInHz = 16000

        /**
         * 指定捕获音频的声道数目。在AudioFormat类中指定用于此的常量
         */
        private const val mChannelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO //单声道

        /**
         * 指定音频量化位数 ,在AudioFormaat类中指定了以下各种可能的常量。
         * 通常我们选择ENCODING_PCM_16BIT和ENCODING_PCM_8BIT PCM代表的是脉冲编码调制，它实际上是原始音频样本。
         * 因此可以设置每个样本的分辨率为16位或者8位，16位将占用更多的空间和处理能力,表示的音频也更加接近真实。
         */
        private const val mAudioFormat = AudioFormat.ENCODING_PCM_16BIT

        /**
         * STREAM的意思是由用户在应用程序通过write方式把数据一次一次得写到audiotrack中。
         * 这个和我们在socket中发送数据一样，
         * 应用层从某个地方获取数据，例如通过编解码得到PCM数据，然后write到audiotrack。
         */
        private const val mMode = AudioTrack.MODE_STREAM
        private const val BUFFER_CAPITAL = 10

        /**
         * 获取单例引用
         *
         * @return
         */
        val instance: AudioTrackManager?
            get() {
                if (mInstance == null) {
                    synchronized(AudioTrackManager::class.java) {
                        if (mInstance == null) {
                            mInstance = AudioTrackManager()
                        }
                    }
                }
                return mInstance
            }
    }

    init {
        initAudioTrack()
    }
}
