package com.franny.androidmediaplayground.media

import android.net.Uri
import android.os.Environment
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.util.*

/**
 * Reference: https://docs.fileformat.com/audio/wav/
 * https://stackoverflow.com/questions/41541956/play-pcm-stream-in-android
 * https://www.jianshu.com/p/10a02df10289
 */
class WAVDecoder {
    /*
    private void readFileByFileChannel(String path) {
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(path, "r");
            FileChannel fileChannel = randomAccessFile.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(10);
            while (fileChannel.read(buffer) != -1) {
                buffer = ByteBuffer.allocate(10);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
     */

    fun test() {
        try {
            // val uri = Uri.parse("android.resource://com.franny.androidmediaplayground/raw/text")
            // val file = File("android.resource://com.franny.androidmediaplayground/raw/text")

            val randomAccessFile = RandomAccessFile("${Environment.getExternalStorageDirectory().absoluteFile}/Music/text", "r")
            val fileChannel = randomAccessFile.channel
            var buffer = ByteBuffer.allocate(44)
            while (fileChannel.read(buffer) != -1) {
                // Timber.i(Arrays.toString(buffer.array()))
                Timber.i("${buffer.char}")
                buffer = ByteBuffer.allocate(44)
            }
        } catch (e: FileNotFoundException) {
            Timber.w(e.stackTraceToString())
        } catch (e: IOException) {
            Timber.w(e.stackTraceToString())
        } catch (e: RuntimeException) {

        }
    }

}