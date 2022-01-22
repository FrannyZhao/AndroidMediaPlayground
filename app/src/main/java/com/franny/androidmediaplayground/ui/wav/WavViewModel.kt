package com.franny.androidmediaplayground.ui.wav

import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.franny.androidmediaplayground.R
import com.franny.androidmediaplayground.media.AudioTrackWrapper
import com.franny.androidmediaplayground.media.WAVDecoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WavViewModel : ViewModel() {
    private val _isPlayingByMediaPlayer = MutableLiveData<Boolean>(false)
    val isPlayingByMediaPlayer: LiveData<Boolean>
        get() = _isPlayingByMediaPlayer

    private val _isPlayingByAudioTrack = MutableLiveData<Boolean>(false)
    val isPlayingByAudioTrack: LiveData<Boolean>
        get() = _isPlayingByAudioTrack

    private val _wavHeader = MutableLiveData<String>("")
    val wavHeader: LiveData<String>
        get() = _wavHeader

    private var mediaPlayer: MediaPlayer? = null

    private var audioTrackWrapper: AudioTrackWrapper = AudioTrackWrapper()
    private var wavDecoder: WAVDecoder = WAVDecoder()

    private val mediaPlayerOnCompletionListener = MediaPlayer.OnCompletionListener {
        _isPlayingByMediaPlayer.postValue(false)
        mediaPlayer?.release()
    }

    private val audioTrackCallback = object : AudioTrackWrapper.Callback {
        override fun onStart() {
            _isPlayingByAudioTrack.postValue(true)
        }

        override fun onStop() {
            _isPlayingByAudioTrack.postValue(false)
        }
    }

    fun getWavHeader() {
        _wavHeader.postValue(wavDecoder.decodeHeader(WAV_FILE_PATH).toString())
    }

    fun togglePLayPauseByMediaPlayer(context: Context) {
        if (_isPlayingByMediaPlayer.value == true) {
            pauseByMediaPlayer()
        } else {
            playByMediaPlayer(context)
        }
    }

    suspend fun togglePlayPauseByAudioTrack() {
        if (_isPlayingByAudioTrack.value == true) {
            pauseByAudioTrack()
        } else {
            playByAudioTrack()
        }
    }

    private fun playByMediaPlayer(context: Context) {
        _isPlayingByMediaPlayer.postValue(true)
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.starwars60)
        }
        mediaPlayer?.start()
        mediaPlayer?.isLooping = false
        mediaPlayer?.setOnCompletionListener(mediaPlayerOnCompletionListener)
    }

    private fun pauseByMediaPlayer() {
        _isPlayingByMediaPlayer.postValue(false)
        mediaPlayer?.pause()
    }

    private suspend fun playByAudioTrack() {
        audioTrackWrapper.setCallback(audioTrackCallback)
        wavDecoder.decodeHeader(WAV_FILE_PATH)?.let { header ->
            withContext(Dispatchers.IO) {
                audioTrackWrapper.startPlay()
                wavDecoder.decode(WAV_FILE_PATH, audioTrackWrapper.initAudioTrack(header))
            }
        }
    }

    private fun pauseByAudioTrack() {
        audioTrackWrapper.stopPlay()
    }

    override fun onCleared() {
        super.onCleared()
        audioTrackWrapper.release()
    }

    companion object {
        private const val WAV_BIG_FILE_PATH = "/sdcard/Music/StarWars60.wav"
        private const val WAV_SMALL_FILE_PATH = "/sdcard/Music/starwars3.wav"
        private const val WAV_SMALL_FILE_PATH2 = "/sdcard/Music/M1F1-Alaw-AFsp.wav"
        private const val WAV_FILE_PATH = WAV_BIG_FILE_PATH
    }
}