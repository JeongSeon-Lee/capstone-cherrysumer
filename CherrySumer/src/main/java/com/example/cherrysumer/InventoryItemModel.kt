package com.example.cherrysumer

import com.google.gson.annotations.SerializedName

data class InventoryItemModel(
    @SerializedName("productName") val productName: String,
    @SerializedName("expiration_date") val expirationDate: String, // 서버에서 문자열로 전달됨
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("stock_location") val stockLocation: String,
    @SerializedName("detailed_category") val detailedCategory: List<String>
    // 기타 필드도 필요에 따라 추가
)
