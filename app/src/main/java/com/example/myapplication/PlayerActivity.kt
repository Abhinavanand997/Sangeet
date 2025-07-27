package com.example.myapplication

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {
    lateinit var binding: ActivityPlayerBinding
    lateinit var exoplayer: ExoPlayer
    var playerListener= object : Player.Listener{
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            showGif(isPlaying)
        }
    }


    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        MyExoplayer.getCurrentSong()?.apply {
            binding.songTitleTextView.text=title
            binding.songArtistTextView.text=artist
            Glide.with(binding.songCoverImageView).load(coverUrl).
                    circleCrop().into(binding.songCoverImageView)
            Glide.with(binding.songGifImageView).load(R.drawable.media_player).
            circleCrop().into(binding.songGifImageView)
            exoplayer= MyExoplayer.getInstance()!!
            binding.playerView.player=exoplayer
            exoplayer.addListener(playerListener)
            binding.playerView.showController()
            binding.playerView.setArtworkDisplayMode(PlayerView.ARTWORK_DISPLAY_MODE_OFF)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        exoplayer?.removeListener(playerListener)
    }
    fun showGif(show: Boolean){
        if(show)
            binding.songGifImageView.visibility= View.VISIBLE
        else
            binding.songGifImageView.visibility= View.INVISIBLE
    }
}