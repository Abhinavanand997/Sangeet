package com.example.myapplication.model


data class SongModel(
    val id: String,
    val title: String,
    val artist: String,
    val url: String,
    val coverUrl: String,
    val videoUrl: String
) {
    constructor() : this("", "", "", "", "", "")
}
