package com.example.ciphermatrix

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("v2/everything")
    fun getCyberNews(
        @Query("q") query: String = "cybersecurity OR \"cyber attack\"",
        @Query("language") language: String = "en",
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("apiKey") apiKey: String
    ): Call<NewsResponse>
}