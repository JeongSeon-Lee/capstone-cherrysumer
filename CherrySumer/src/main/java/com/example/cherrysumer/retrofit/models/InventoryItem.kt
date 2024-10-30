package com.example.cherrysumer.retrofit.models

import com.google.gson.annotations.SerializedName

data class InventoryItem(
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("id") val id: Int,
    @SerializedName("productName") val productName: String,
    @SerializedName("purchase_date") val purchaseDate: String,
    @SerializedName("expiration_date") val expirationDate: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("stockLocation") val stockLocation: String,
    @SerializedName("category") val category: String?
)