package com.cookandroid.cherrysumer.retrofit.models

import com.google.gson.annotations.SerializedName

data class PostItem(
    @SerializedName("postId") val postId: Long,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("title") val title: String,
    @SerializedName("productName") val productName: String,
    @SerializedName("date") val purchaseDate: String,
    @SerializedName("category") val category: String,
    @SerializedName("applicantCount") val applicantCount: Int,
    @SerializedName("purchaseCompleted") val purchaseCompleted: Boolean,
    @SerializedName("inventoryRegistered") val inventoryRegistered: Boolean,
)