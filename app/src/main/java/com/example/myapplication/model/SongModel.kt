package com.example.myapplication.model

data class SongModel(
    val id : String,
    val title: String,
    val artist: String,
    val Url : String,
    val coverUrl : String,
){
    constructor(): this("","","","","")
}
