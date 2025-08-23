package com.example.myapplication.model

import android.R

data class CategoryModel(
    val name : String,
    val coverUrl : String,
    var songs : List<String>,
){
    constructor():this("","",listOf())
}
