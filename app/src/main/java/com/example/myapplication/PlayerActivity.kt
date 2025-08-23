package com.example.myapplication

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.databinding.ActivityPlayerBinding
import com.bumptech.glide.request.RequestOptions
import jp.wasabeef.glide.transformations.BlurTransformation



class PlayerActivity : AppCompatActivity() {
    lateinit var binding: ActivityPlayerBinding
    lateinit var exoplayer: ExoPlayer
    private var backgroundPlayer: ExoPlayer? = null

    var playerListener= object : Player.Listener{
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)

        }
    }


    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        MyExoplayer.getCurrentSong()?.apply {
            binding.songTitleeTextView.text=title
            binding.albumName.text= title
            binding.songArtisttTextView.text=artist
            binding.playerView.setShutterBackgroundColor(Color.TRANSPARENT)
            Glide.with(binding.songCoverrImageView).load(coverUrl).apply( RequestOptions().transform(RoundedCorners(32)))
                .into(binding.songCoverrImageView)

            exoplayer= MyExoplayer.getInstance()!!
            binding.playerView.player=exoplayer
            exoplayer.addListener(playerListener)
            binding.playerView.showController()
            binding.playerView.setArtworkDisplayMode(PlayerView.ARTWORK_DISPLAY_MODE_OFF)
            if (!videoUrl.isNullOrEmpty()) {
                setupBackgroundVideo(videoUrl)
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if (::exoplayer.isInitialized) {
            exoplayer.removeListener(playerListener)
        }
        backgroundPlayer?.release()
        backgroundPlayer = null
    }

    override fun onPause() {
        super.onPause()
        if (isFinishing) {
            // Release players if activity is finishing

            backgroundPlayer?.release()
        } else {
            // Just pause playback if screen turns off
            backgroundPlayer?.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!exoplayer.isPlaying) {
            exoplayer.play()
        }
        backgroundPlayer?.play()
    }

    private fun setupBackgroundVideo(url: String) {
        backgroundPlayer = ExoPlayer.Builder(this).build().apply {
            val mediaItem = MediaItem.fromUri(url)
            setMediaItem(mediaItem)
            volume = 0f
            repeatMode = ExoPlayer.REPEAT_MODE_ALL
            prepare()
            play()
        }
        binding.backgroundVideoView.player = backgroundPlayer
    }


}