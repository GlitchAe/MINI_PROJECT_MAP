package com.example.miniprojectmap

import com.google.gson.annotations.SerializedName

// Bentuk JSON dari https://dummyjson.com/quotes/random
data class QuoteResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("quote") val quote: String,
    @SerializedName("author") val author: String
)