package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.text.capitalize
import com.example.myapplication.databinding.ActivityUploadSongBinding
import com.example.myapplication.model.SongModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.FirebaseStorage

class UploadSongActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadSongBinding
    private var audioUri: Uri? = null
    private var coverUri: Uri? = null
    private var isUploading = false

    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadSongBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Select audio file
        binding.selectAudioBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "audio/*"
            startActivityForResult(intent, 100)
        }

        // Select cover image
        binding.selectCoverBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 200)
        }

        // Upload button click
        binding.uploadBtn.setOnClickListener {
            if (!isUploading) {
                uploadSong()
            } else {
                Toast.makeText(this, "Upload in progress...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                100 -> audioUri = data.data
                200 -> coverUri = data.data
            }
        }
    }

    private fun uploadSong() {
        val title = binding.titleEditText.text.toString().trim().capitalize()
        val artist = binding.artistEditText.text.toString().trim()

        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        if (audioUri == null || title.isEmpty() || artist.isEmpty()) {
            Toast.makeText(this, "Please fill all fields and select audio", Toast.LENGTH_SHORT).show()
            return
        }

        isUploading = true
        binding.uploadBtn.isEnabled = false
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.uploadStatusText.visibility = android.view.View.VISIBLE
        binding.uploadStatusText.text = "Uploading... 0%"

        val newSongDocRef = firestore.collection("songs").document()
        val songId = newSongDocRef.id

        val audioRef = storage.reference.child("songs/$songId.mp3")

        audioRef.putFile(audioUri!!)
            .addOnProgressListener { snapshot ->
                val progress = (100.0 * snapshot.bytesTransferred / snapshot.totalByteCount).toInt()
                binding.progressBar.progress = progress
                binding.uploadStatusText.text = "Uploading... $progress%"
            }
            .addOnSuccessListener {
                audioRef.downloadUrl.addOnSuccessListener { audioUrl ->
                    if (coverUri != null) {
                        val coverRef = storage.reference.child("covers/$songId.jpg")
                        coverRef.putFile(coverUri!!)
                            .addOnProgressListener { snapshot ->
                                val progress = (100.0 * snapshot.bytesTransferred / snapshot.totalByteCount).toInt()
                                binding.progressBar.progress = progress
                                binding.uploadStatusText.text = "Processing cover... $progress%"
                            }
                            .addOnSuccessListener {
                                coverRef.downloadUrl.addOnSuccessListener { coverUrl ->
                                    saveSongToFirestore(newSongDocRef, songId, title, artist, audioUrl.toString(), coverUrl.toString(), uid)
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to upload cover: ${it.message}", Toast.LENGTH_SHORT).show()
                                resetUploadUI()
                            }
                    } else {
                        saveSongToFirestore(newSongDocRef, songId, title, artist, audioUrl.toString(), "", uid)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to upload audio: ${it.message}", Toast.LENGTH_SHORT).show()
                resetUploadUI()
            }
    }

    private fun saveSongToFirestore(
        docRef: DocumentReference,
        id: String,
        title: String,
        artist: String,
        audioUrl: String,
        coverUrl: String,
        uid: String
    ) {
        val song = SongModel(
            id = id,
            title = title,
            artist = artist,
            url = audioUrl,
            coverUrl = coverUrl,
            videoUrl = ""
        )

        docRef.set(song)
            .addOnSuccessListener {
                addSongToExplore(id)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save song: ${it.message}", Toast.LENGTH_SHORT).show()
                resetUploadUI()
            }
    }

    private fun addSongToExplore(songId: String) {
        val exploreRef = firestore.collection("categories").document("Explore")

        exploreRef.update("songs", com.google.firebase.firestore.FieldValue.arrayUnion(songId))
            .addOnSuccessListener {
                Toast.makeText(this, "Song uploaded successfully!", Toast.LENGTH_SHORT).show()
                resetUploadUI()
                finish()
            }
            .addOnFailureListener { e ->
                if (e.message?.contains("No document to update") == true) {
                    val exploreData = mapOf(
                        "name" to "Explore",
                        "songs" to listOf(songId)
                    )
                    exploreRef.set(exploreData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Song uploaded successfully!", Toast.LENGTH_SHORT).show()
                            resetUploadUI()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to add to Explore: ${it.message}", Toast.LENGTH_SHORT).show()
                            resetUploadUI()
                        }
                } else {
                    Toast.makeText(this, "Failed to add song to Explore: ${e.message}", Toast.LENGTH_SHORT).show()
                    resetUploadUI()
                }
            }
    }

    private fun resetUploadUI() {
        isUploading = false
        binding.uploadBtn.isEnabled = true
        binding.progressBar.visibility = android.view.View.GONE
        binding.uploadStatusText.visibility = android.view.View.GONE
        binding.progressBar.progress = 0
    }
}
