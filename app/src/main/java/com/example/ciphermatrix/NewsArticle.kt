package com.example.ciphermatrix

import com.google.gson.annotations.SerializedName

/**
 * Data Model for Cyber Security News.
 * Using @SerializedName ensures mapping even if the API JSON structure changes slightly.
 */
data class NewsArticle(
    @SerializedName("title")
    val title: String = "No Title",

    @SerializedName("description")
    val description: String = "No description available.",

    @SerializedName("urlToImage")
    val urlToImage: String? = null,

    @SerializedName("url")
    val url: String? = null //
)