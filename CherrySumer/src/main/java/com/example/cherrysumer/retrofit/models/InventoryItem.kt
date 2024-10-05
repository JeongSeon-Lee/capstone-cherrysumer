package com.example.cherrysumer.retrofit.models

import com.google.gson.annotations.SerializedName

data class InventoryItem(
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("id") val id: Int,
    @SerializedName("productName") val productName: String,
    @SerializedName("expiration_date") val expirationDate: String, // 서버에서 문자열로 전달됨
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("stock_location") val stockLocation: String,
    @SerializedName("category") val category: String?
)