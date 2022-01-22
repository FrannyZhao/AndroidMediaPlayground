package com.franny.androidmediaplayground.media

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import timber.log.Timber

class AudioTrackWrapper {
    private var mAudioTrack: AudioTrack? = null

    interface Callback {
        fun onStart()
        fun onStop()
    }

    private var mCallback: Callback? = null

    fun setCallback(callback: Callback) {
        mCallback = callback
    }

    fun initAudioTrack(header: WAVDecoder.Header): AudioTrack {
        val channel = when (header.channelNumber) {
            1 -> AudioFormat.CHANNEL_OUT_MONO
            2 -> AudioFormat.CHANNEL_OUT_STEREO
            else -> AudioFormat.CHANNEL_OUT_DEFAULT
        }
        val minBufferSize = AudioTrack.getMinBufferSize(
            header.sampleRate,
            channel,
            AudioFormat.ENCODING_PCM_16BIT
        )
        mAudioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            header.sampleRate,
            channel,
            AudioFormat.ENCODING_PCM_16BIT,
            minBufferSize,
            AudioTrack.MODE_STREAM)
        mAudioTrack!!.positionNotificationPeriod = header.durationInSeconds * header.sampleRate
        mAudioTrack!!.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
            override fun onMarkerReached(audioTrack: AudioTrack?) {
                Timber.d("onMarkerReached ")
            }

            override fun onPeriodicNotification(audioTrack: AudioTrack?) {
                Timber.d("onPeriodicNotification")
                stopPlay()
            }
        })
        return mAudioTrack!!
    }

    fun startPlay() {
        mCallback?.onStart()
        mAudioTrack?.play()
    }

    fun stopPlay() {
        mCallback?.onStop()
        mAudioTrack?.stop()
    }

    fun release() {
        mCallback = null
        mAudioTrack?.release()
    }
}