package com.example.miniprojectmap

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// Model Data Quote
data class QuoteResponse(
    @SerializedName("quote") val quote: String,
    @SerializedName("author") val author: String
)

// Interface API
interface ApiService {
    @GET("quotes/random")
    suspend fun getRandomQuote(): QuoteResponse
}

// Config Retrofit
object ApiConfig {
    private const val BASE_URL = "https://dummyjson.com/"

    fun getApiService(): ApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}