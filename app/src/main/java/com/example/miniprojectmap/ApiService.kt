package com.example.miniprojectmap

import retrofit2.http.GET

interface ApiService {
    @GET("quotes/random")
    suspend fun getRandomQuote(): QuoteResponse
}