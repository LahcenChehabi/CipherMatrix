package com.example.ciphermatrix

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    //
    private const val BASE_URL = "https://www.virustotal.com/api/v3/"

    //
    val virusTotalApi: VirusTotalService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VirusTotalService::class.java)
    }
}