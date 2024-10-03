package com.example.cherrysumer.retrofit

import com.example.cherrysumer.InventoryItemModel
import retrofit2.Call
import retrofit2.http.GET

interface INetworkService {
    @GET("inventory")
    fun getInventoryItems(): Call<ApiResponse<List<InventoryItemModel>>>
}
