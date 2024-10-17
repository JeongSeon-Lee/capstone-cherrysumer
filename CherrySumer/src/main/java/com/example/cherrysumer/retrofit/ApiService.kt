package com.example.cherrysumer.retrofit

import com.example.cherrysumer.retrofit.models.InventoryItem
import com.example.cherrysumer.retrofit.models.ApiResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("inventory/location/{stockLocation}")
    fun listInventoryItems(@Path("stockLocation") stockLocation: String): Call<ApiResponse<List<InventoryItem>>>

    @GET("inventory/search")
    fun searchInventoryItems(@Query("query") query: String): Call<ApiResponse<List<InventoryItem>>>

    @DELETE("inventory/{id}")
    fun deleteInventoryItem(@Path("id") itemId: Int): Call<ApiResponse<Unit>>

    @POST("inventory/insert")
    fun insertInventoryItem(@Body item: InventoryItem): Call<ApiResponse<Unit>>
}
