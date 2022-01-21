package com.franny.androidmediaplayground.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.franny.androidmediaplayground.R
import com.franny.androidmediaplayground.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private val homeViewModel by activityViewModels<HomeViewModel>()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.playByMediaplayerButton.setOnClickListener {
            homeViewModel.togglePLayPauseByMediaPlayer(context!!)
        }
        binding.playByAudiotrackButton.setOnClickListener {
            homeViewModel.togglePlayPauseByAudioTrack(context!!)
        }
        homeViewModel.isPlayingByMediaPlayer.observe(viewLifecycleOwner, playByMediaPlayerObserver)
        homeViewModel.isPlayingByAudioTrack.observe(viewLifecycleOwner, playByAudioTrackObserver)
        return binding.root
    }

    private val playByMediaPlayerObserver = Observer<Boolean> {
        if (it) {
            binding.playByMediaplayerButton.text = getString(R.string.pause_by_mediaplayer)
            binding.playByAudiotrackButton.isEnabled = false
        } else {
            binding.playByMediaplayerButton.text = getString(R.string.play_by_mediaplayer)
            binding.playByAudiotrackButton.isEnabled = true
        }
    }

    private val playByAudioTrackObserver = Observer<Boolean> {
        if (it) {
            binding.playByAudiotrackButton.text = getString(R.string.pause_by_audiotrack)
            binding.playByMediaplayerButton.isEnabled = false
        } else {
            binding.playByAudiotrackButton.text = getString(R.string.play_by_audiotrack)
            binding.playByMediaplayerButton.isEnabled = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}