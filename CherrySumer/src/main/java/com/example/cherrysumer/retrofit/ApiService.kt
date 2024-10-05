package com.example.cherrysumer.retrofit

import com.example.cherrysumer.retrofit.models.InventoryItem
import com.example.cherrysumer.retrofit.models.ApiResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("inventory")
    fun getInventoryItems(): Call<ApiResponse<List<InventoryItem>>>

    @POST("inventory/insert")
    fun insertInventory(@Body item: InventoryItem): Call<ApiResponse<Unit>>

    @DELETE("inventory/{id}")
    fun deleteInventoryItem(@Path("id") itemId: Int): Call<ApiResponse<Unit>>
}
