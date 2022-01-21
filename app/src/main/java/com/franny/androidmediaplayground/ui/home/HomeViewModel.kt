package com.franny.androidmediaplayground.ui.home

import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.franny.androidmediaplayground.R
import com.franny.androidmediaplayground.media.WAVDecoder

class HomeViewModel : ViewModel() {
    private val _isPlayingByMediaPlayer = MutableLiveData<Boolean>(false)
    val isPlayingByMediaPlayer: LiveData<Boolean>
        get() = _isPlayingByMediaPlayer

    private val _isPlayingByAudioTrack = MutableLiveData<Boolean>(false)
    val isPlayingByAudioTrack: LiveData<Boolean>
        get() = _isPlayingByAudioTrack

    private var mediaPlayer: MediaPlayer? = null

    private val mediaPlayerOnCompletionListener = MediaPlayer.OnCompletionListener {
        _isPlayingByMediaPlayer.postValue(false)
        mediaPlayer?.release()
    }

    fun togglePLayPauseByMediaPlayer(context: Context) {
        if (_isPlayingByMediaPlayer.value == true) {
            pauseByMediaPlayer(context)
        } else {
            playByMediaPlayer(context)
        }
    }

    fun togglePlayPauseByAudioTrack(context: Context) {
        if (_isPlayingByAudioTrack.value == true) {
            pauseByAudioTrack(context)
        } else {
            playByAudioTrack(context)
        }
    }

    private fun playByMediaPlayer(context: Context) {
        _isPlayingByMediaPlayer.postValue(true)
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.starwars60)
        }
        mediaPlayer?.prepare()
        mediaPlayer?.start()
        mediaPlayer?.isLooping = false
        mediaPlayer?.setOnCompletionListener(mediaPlayerOnCompletionListener)
    }

    private fun pauseByMediaPlayer(context: Context) {
        _isPlayingByMediaPlayer.postValue(false)
        mediaPlayer?.pause()
    }

    private fun playByAudioTrack(context: Context) {
        _isPlayingByAudioTrack.postValue(true)
        val wavDecoder = WAVDecoder()
        wavDecoder.test()


    }

    private fun pauseByAudioTrack(context: Context) {
        _isPlayingByAudioTrack.postValue(false)



    }
}