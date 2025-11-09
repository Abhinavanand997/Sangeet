package com.example.myapplication

//updated ones
import android.text.Editable
import android.text.TextWatcher

import com.example.myapplication.R
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.text.capitalize
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.myapplication.adapter.CategoryAdapter
import com.example.myapplication.adapter.SectionSongListAdapter
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.model.CategoryModel
import com.example.myapplication.model.SongModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
lateinit var exoplayer: ExoPlayer


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var categoryAdapter: CategoryAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //updated
        setupSearchFeature()

        getCategories()
        setupSections("section_1",binding.section1MainLayout,binding.section1Title,binding.section1RecyclerView)
        setupMostlyPlayed("section_4",binding.section4MainLayout,binding.section4Title,binding.section4RecyclerView)
        setupSections("section_3",binding.section3MainLayout,binding.section3Title,binding.section3RecyclerView)
        setupSections("section_2",binding.section2MainLayout,binding.section2Title,binding.section2RecyclerView)

        binding.optionBtn.setOnClickListener {
            showPopupMenu()
        }
        // 🔹 FAB for UploadSongActivity
        binding.addSongFab.setOnClickListener {
            startActivity(Intent(this, UploadSongActivity::class.java))
        }

    }
    fun showPopupMenu(){
        val popupMenu =PopupMenu(this,binding.optionBtn)
        val inflator =popupMenu.menuInflater
        inflator.inflate(R.menu.option_menu,popupMenu.menu)
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.logout->{
                    logout()
                    true
                }
            }
            false
        }
    }
    fun logout(){
        MyExoplayer.getInstance()?.release()
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onResume() {
        super.onResume()
        showPlayerView()

    }
    fun getCategories(){
        FirebaseFirestore.getInstance().collection("categories")
            .get().addOnSuccessListener {
                val categoryList=it.toObjects(CategoryModel::class.java)
                setupCategoryRecyclerView(categoryList)
            }

    }
    fun setupCategoryRecyclerView(categoryList: List<CategoryModel>){
        categoryAdapter= CategoryAdapter(categoryList)
        binding.categoriesRecylerView.layoutManager= LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
        binding.categoriesRecylerView.adapter= categoryAdapter
    }
    fun setupSections(id: String,mainlayout: RelativeLayout,titleview: TextView,recyclerView: RecyclerView){
        FirebaseFirestore.getInstance().collection("sections").document(id)
            .get().addOnSuccessListener {
                val section =it.toObject(CategoryModel::class.java)
                section?.apply {
                    mainlayout.visibility= View.VISIBLE
                    titleview.text=name
                    recyclerView.layoutManager= LinearLayoutManager(this@MainActivity,
                        LinearLayoutManager.HORIZONTAL,false)
                    recyclerView.adapter= SectionSongListAdapter(songs)
                   mainlayout.setOnClickListener {
                        SongsListActivity.category=section
                        startActivity(Intent(this@MainActivity, SongsListActivity::class.java))
                    }
                }
            }
    }
    fun setupMostlyPlayed(id: String,mainlayout: RelativeLayout,titleview: TextView,recyclerView: RecyclerView){
        FirebaseFirestore.getInstance().collection("sections").document(id)
            .get().addOnSuccessListener {
                FirebaseFirestore.getInstance().collection("songs").orderBy("count",Query.Direction.DESCENDING)
                    .limit(6).get().addOnSuccessListener {snapshot->
                        val songsModelList=snapshot.toObjects(SongModel::class.java)
                        val songsIdList=songsModelList.map{
                            it.id
                        }.toList()
                        val section =it.toObject(CategoryModel::class.java)
                        section?.apply {
                            section.songs=songsIdList
                            mainlayout.visibility= View.VISIBLE
                            titleview.text=name
                            recyclerView.layoutManager= LinearLayoutManager(this@MainActivity,
                                LinearLayoutManager.HORIZONTAL,false)
                            recyclerView.adapter= SectionSongListAdapter(songs)
                            mainlayout.setOnClickListener {
                                SongsListActivity.category=section
                                startActivity(Intent(this@MainActivity, SongsListActivity::class.java))
                            }
                        }
                    }

            }
    }

    @OptIn(UnstableApi::class)
    fun showPlayerView(){
        binding.playerrView.setOnClickListener {
            startActivity(Intent(this, PlayerActivity::class.java))
        }
        MyExoplayer.getCurrentSong()?.let {
            binding.playerView.visibility= View.VISIBLE
            binding.songTitleTextView.text=it.title
            Glide.with(binding.songCoverImageView).load(it.coverUrl).apply( RequestOptions().transform(RoundedCorners(32)))
                .into(binding.songCoverImageView)
            exoplayer= MyExoplayer.getInstance()!!
            binding.playerrView.player=exoplayer
            binding.playerrView.showController()
            binding.playerrView.setArtworkDisplayMode(PlayerView.ARTWORK_DISPLAY_MODE_OFF)
        } ?: run {
            binding.playerView.visibility= View.GONE
        }
    }
    //updated
    private fun setupSearchFeature() {
        binding.searchEditText.setOnEditorActionListener { textView, actionId, event ->
            val query = textView.text.toString().trim().capitalize()
            if (query.isEmpty()) {
                Toast.makeText(this, "Enter a song name", Toast.LENGTH_SHORT).show()
                return@setOnEditorActionListener true
            }

            val firstWord = query.split("\\s+".toRegex())[0] // take only first word
            val firestore = FirebaseFirestore.getInstance()

            firestore.collection("songs")
                .orderBy("title")
                .startAt(firstWord)
                .endAt(firstWord + "\uf8ff")
                .limit(1) // only need one match
                .get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.isEmpty) {
                        val songsList = snapshot.toObjects(SongModel::class.java)
                        val foundSong = songsList.firstOrNull()

                        if (foundSong != null) {
                            // ✅ same logic used in your "Mostly Played"
                            MyExoplayer.startPlaying(this, foundSong)
                            startActivity(Intent(this, PlayerActivity::class.java))
                            binding.searchEditText.setText("")
                        } else {
                            Toast.makeText(this, "Song data missing", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Song not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }

            true
        }
    }




}