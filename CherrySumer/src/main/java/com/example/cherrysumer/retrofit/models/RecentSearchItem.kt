package com.example.cherrysumer.retrofit.models

import com.google.gson.annotations.SerializedName

data class RecentSearchItem(
    @SerializedName("name") val name: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("state") val state: Boolean
)
