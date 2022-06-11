package com.example.moviesearch.data

import com.google.gson.annotations.SerializedName

data class Movie (
    var title: String,
    @SerializedName("link") val infoLink: String,
    @SerializedName("image") val imageLink: String,
    val userRating: String,
    val pubDate: String
)

data class Request(
    val total: Int,
    val start: Int,
    val display: Int,
    val items: List<Movie>)


