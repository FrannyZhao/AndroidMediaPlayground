package com.franny.androidmediaplayground.media

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.delay
import timber.log.Timber
import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.Charset
import java.util.*

/**
 * Reference: https://docs.fileformat.com/audio/wav/
 * http://www-mmsp.ece.mcgill.ca/Documents/AudioFormats/WAVE/Samples.html
 */
class WAVDecoder {
    data class Header(
        val riff: String, // 1-4
        val fileSize: Int, // 5-8, size in byte
        val fileType: String, // 9-12
        val format: String, // 13-16
        val formatDataLength: Int, // 17-20
        val formatType: Int, // 21-22, 1 is PCM
        val channelNumber: Int, // 23-24
        val sampleRate: Int, // 25-28
        val unknown1: Int, // 29-32, (Sample Rate * BitsPerSample * Channels) / 8.
        val unknown2: Int, // 33-34, (BitsPerSample * Channels) / 8.1 - 8 bit mono2 - 8 bit stereo/16 bit mono4 - 16 bit stereo
        val bitsPerSample: Int, // 35-36
        val dataTag: String, // 37-40
        val dataSize: Int, // 41-44
    ) {
        override fun toString(): String {
            return "riff: $riff" + "\n" +
                    "fileSize : $fileSize" + "\n" +
                    "fileType : $fileType" + "\n" +
                    "format : $format" + "\n" +
                    "formatDataLength : $formatDataLength" + "\n" +
                    "formatType : $formatType" + "\n" +
                    "channelNumber : $channelNumber" + "\n" +
                    "sampleRate : $sampleRate" + "\n" +
                    "unknown1 : $unknown1" + "\n" +
                    "unknown2 : $unknown2" + "\n" +
                    "bitsPerSample : $bitsPerSample" + "\n" +
                    "dataTag : $dataTag" + "\n" +
                    "dataSize : $dataSize"

        }
    }

    fun decodeHeader(filePath: String): Header? {
        var header: Header? = null
        var randomAccessFile: RandomAccessFile? = null
        var fileChannel: FileChannel? = null
        try {
            randomAccessFile = RandomAccessFile(filePath, "r")
            fileChannel = randomAccessFile.channel
            val buffer = ByteBuffer.allocate(44)
            if (fileChannel.read(buffer) != -1) {
                header = analyzeHeader(buffer)
            }
        } catch (e: FileNotFoundException) {
            Timber.w(e.stackTraceToString())
        } catch (e: IOException) {
            Timber.w(e.stackTraceToString())
        } catch (e: RuntimeException) {
            Timber.w(e.stackTraceToString())
        } finally {
            fileChannel?.close()
            randomAccessFile?.close()
        }
        return header
    }

    private var mAudioTrack: AudioTrack? = null

    fun decode(filePath: String) {
        var randomAccessFile: RandomAccessFile? = null
        var fileChannel: FileChannel? = null
        try {
            randomAccessFile = RandomAccessFile(filePath, "r")
            fileChannel = randomAccessFile.channel
            var buffer = ByteBuffer.allocate(44)
            if (fileChannel.read(buffer) != -1) {
                val header = analyzeHeader(buffer)
                createAudioTrack(header)
                mAudioTrack!!.play()
                buffer = ByteBuffer.allocate(1024)
                while (mAudioTrack!!.playState == AudioTrack.PLAYSTATE_PLAYING &&
                    fileChannel.read(buffer) != -1) {
                    mAudioTrack!!.write(buffer.array(), 0, buffer.array().size)
                    buffer = ByteBuffer.allocate(1024)
                }
            }
        } catch (e: FileNotFoundException) {
            Timber.w(e.stackTraceToString())
        } catch (e: IOException) {
            Timber.w(e.stackTraceToString())
        } catch (e: RuntimeException) {
            Timber.w(e.stackTraceToString())
        } finally {
            fileChannel?.close()
            randomAccessFile?.close()
        }
    }

    fun stop() {
        mAudioTrack?.stop()
        mAudioTrack?.release()
    }

    private fun createAudioTrack(header: Header) {
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
        mAudioTrack = AudioTrack(AudioManager.STREAM_MUSIC,
            header.sampleRate,
            channel,
            AudioFormat.ENCODING_PCM_16BIT,
            minBufferSize,
            AudioTrack.MODE_STREAM)
    }

    /**
     * Reference: https://en.wikipedia.org/wiki/Resource_Interchange_File_Format
     * Multi-byte integers are in little-endian format.
     */
    private fun analyzeHeader(buffer: ByteBuffer): Header {
        val charset = Charset.defaultCharset()
        val riffBytes = ByteArray(4) { index ->
            buffer[index]
        }
        val riff = String(riffBytes, charset)

        val fileSizeBytes = byteArrayOf(buffer[7], buffer[6], buffer[5], buffer[4])
        val fileSize = BigInteger(fileSizeBytes).toInt()

        val fileTypeBytes = byteArrayOf(buffer[8], buffer[9], buffer[10], buffer[11])
        val fileType = String(fileTypeBytes, charset)

        val formatBytes = byteArrayOf(buffer[12], buffer[13], buffer[14], buffer[15])
        val format = String(formatBytes, charset)

        val formatDataLengthBytes = byteArrayOf(buffer[19], buffer[18], buffer[17], buffer[16])
        val formatDataLength = BigInteger(formatDataLengthBytes).toInt()

        val formatTypeBytes = byteArrayOf(buffer[21], buffer[20])
        val formatType = BigInteger(formatTypeBytes).toInt()

        val channelNumberBytes = byteArrayOf(buffer[23], buffer[22])
        val channelNumber = BigInteger(channelNumberBytes).toInt()

        val sampleRateBytes = byteArrayOf(buffer[27], buffer[26], buffer[25], buffer[24])
        val sampleRate = BigInteger(sampleRateBytes).toInt()

        val unknown1Bytes = byteArrayOf(buffer[31], buffer[30], buffer[29], buffer[28])
        val unknown1 = BigInteger(unknown1Bytes).toInt()

        val unknown2Bytes = byteArrayOf(buffer[33], buffer[32])
        val unknown2 = BigInteger(unknown2Bytes).toInt()

        val bitsPerSampleBytes = byteArrayOf(buffer[35], buffer[34])
        val bitsPerSample = BigInteger(bitsPerSampleBytes).toInt()

        val dataTagBytes = byteArrayOf(buffer[36], buffer[37], buffer[38], buffer[39])
        val dataTag = String(dataTagBytes, charset)

        val dataSizeBytes = byteArrayOf(buffer[43], buffer[42], buffer[41], buffer[40])
        val dataSize = BigInteger(dataSizeBytes).toInt()

        return Header(
            riff,
            fileSize,
            fileType,
            format,
            formatDataLength,
            formatType,
            channelNumber,
            sampleRate,
            unknown1,
            unknown2,
            bitsPerSample,
            dataTag,
            dataSize
        ).also {
            Timber.d("$it")
        }
    }

    companion object {
        val instance = WAVDecoder()
    }
}