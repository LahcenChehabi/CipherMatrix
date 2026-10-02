package com.example.ciphermatrix

import retrofit2.Call
import retrofit2.http.*

interface VirusTotalService {

    // First step: Submit the URL
    @FormUrlEncoded
    @POST("urls")
    fun scanUrl(
        @Header("x-apikey") apiKey: String,
        @Field("url") url: String
    ): Call<ScanResponse>

    // Second step: Get the report using the ID
    @GET("analyses/{id}")
    fun getReport(
        @Header("x-apikey") apiKey: String,
        @Path("id") id: String
    ): Call<ReportResponse>
}

// Data models for the report
data class ScanResponse(val data: Data)
data class Data(val id: String)

data class ReportResponse(val data: ReportData)
data class ReportData(val attributes: Attributes)
data class Attributes(val stats: Map<String, Int>) // Shows harmless, malicious, etc.