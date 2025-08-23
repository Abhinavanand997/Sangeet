package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.myapplication.adapter.SongsListAdapter
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.databinding.ActivitySongsListBinding
import com.example.myapplication.model.CategoryModel

class SongsListActivity : AppCompatActivity() {
    companion object{
        lateinit var category : CategoryModel
    }
    lateinit var binding: ActivitySongsListBinding
    lateinit var songsListAdapter : SongsListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivitySongsListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.nameTextView.text= category.name
        Glide.with(binding.coverImageView).load(category.coverUrl).apply(
            RequestOptions().transform(RoundedCorners(32))).into(binding.coverImageView)
        setupSongsListRecyclerView()
    }
    override fun onResume() {
        super.onResume()
        showPlayerView()

    }
    fun setupSongsListRecyclerView(){
        songsListAdapter= SongsListAdapter(category.songs)
        binding.songsListRecyclerView.layoutManager= LinearLayoutManager(this)
        binding.songsListRecyclerView.adapter=songsListAdapter
    }
    fun showPlayerView(){
        binding.playerView.setOnClickListener {
            startActivity(Intent(this, PlayerActivity::class.java))
        }
        MyExoplayer.getCurrentSong()?.let {
            binding.playerView.visibility= View.VISIBLE
            binding.songTitleTextView.text=it.title
            Glide.with(binding.songCoverImageView).load(it.coverUrl)
                .into(binding.songCoverImageView)
        } ?: run {
            binding.playerView.visibility= View.GONE
        }
    }
}