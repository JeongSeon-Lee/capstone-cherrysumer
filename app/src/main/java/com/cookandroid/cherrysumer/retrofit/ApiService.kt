package com.cookandroid.cherrysumer.retrofit

import com.cookandroid.cherrysumer.retrofit.models.ApiResponse
import com.cookandroid.cherrysumer.retrofit.models.InventoryItem
import com.cookandroid.cherrysumer.retrofit.models.PostItem
import com.cookandroid.cherrysumer.retrofit.models.RecentSearchItem
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
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

    @POST("inventory/edit/{id}")
    fun editInventoryItem(@Path("id") itemId: Int, @Body item: InventoryItem): Call<ApiResponse<Unit>>

    @GET("inventory/register/{postId}")
    fun registerPostItem(@Path("postId") postId: Long): Call<ApiResponse<Unit>>

    @GET("mypage/applications/{filter}")
    fun listPostItems(@Path("filter") filter: String): Call<ApiResponse<List<PostItem>>>

    @GET("api/search-log/recent")
    fun getRecentSearchLogs(): Call<ApiResponse<List<RecentSearchItem>>>

    @HTTP(method = "DELETE", path = "api/search-log/delete", hasBody = true)
    fun deleteRecentSearchLog(@Body request: JsonObject): Call<ApiResponse<Unit>>

}
