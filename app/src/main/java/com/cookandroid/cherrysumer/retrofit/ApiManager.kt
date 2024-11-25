package com.cookandroid.cherrysumer.retrofit

import android.util.Log
import com.cookandroid.cherrysumer.models.ChatRoom
import com.cookandroid.cherrysumer.retrofit.models.ApiResponse
import com.cookandroid.cherrysumer.retrofit.models.InventoryItem
import com.cookandroid.cherrysumer.retrofit.models.PostItem
import com.cookandroid.cherrysumer.retrofit.models.RecentSearchItem
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Path

class ApiManager {
    private val apiService: ApiService = MyApplication.networkService

    fun listInventoryItems(stockLocation: String, callback: ApiCallback<List<InventoryItem>>) {
        apiService.listInventoryItems(stockLocation).enqueue(createCallback(callback))
    }

    fun searchInventoryItems(searchQeury: String, callback: ApiCallback<List<InventoryItem>>) {
        apiService.searchInventoryItems(searchQeury).enqueue(createCallback(callback))
    }

    fun deleteInventoryItem(id: Int, callback: ApiCallback<Unit>) {
        apiService.deleteInventoryItem(id).enqueue(createCallback(callback))
    }

    fun insertInventoryItem(item: InventoryItem, callback: ApiCallback<Unit>) {
        apiService.insertInventoryItem(item).enqueue(createCallback(callback))
    }

    fun editInventoryItem(id: Int, item: InventoryItem, callback: ApiCallback<Unit>) {
        apiService.editInventoryItem(id, item).enqueue(createCallback(callback))
    }

    fun registerPostItem(id: Long, callback: ApiCallback<Unit>) {
        apiService.registerPostItem(id).enqueue(createCallback(callback))
    }

    fun listPostItems(filter: String, callback: ApiCallback<List<PostItem>>) {
        apiService.listPostItems(filter).enqueue(createCallback(callback))
    }

    fun getRecentSearchLogs(callback: ApiCallback<List<RecentSearchItem>>) {
        apiService.getRecentSearchLogs().enqueue(createCallback(callback))
    }

    fun deleteRecentSearchLog(jsonObject: JsonObject, callback: ApiCallback<Unit>) {
        apiService.deleteRecentSearchLog(jsonObject).enqueue(createCallback(callback))
    }

    fun getChatRooms(status: String, callback: ApiCallback<List<ChatRoom>>) {
        apiService.getChatRooms(status).enqueue(createCallback(callback))
    }

    private fun <T> createCallback(callback: ApiCallback<T>): Callback<ApiResponse<T>> {
        return object : Callback<ApiResponse<T>> {
            override fun onResponse(call: Call<ApiResponse<T>>, response: Response<ApiResponse<T>>) {
                if (response.isSuccessful) {
                    callback.onSuccess(response.body())
                } else {
                    callback.onError(response)
                }
            }
            override fun onFailure(call: Call<ApiResponse<T>>, t: Throwable) {
                callback.onFailure(t)
            }
        }
    }
}

interface ApiCallback<T> {
    fun onSuccess(apiResponse: ApiResponse<T>?) {
        Log.d("ApiManager", "Success: ${apiResponse?.message}")
    }
    fun onError(response: Response<ApiResponse<T>>) {
        Log.e("ApiManager", "Error: ${response.errorBody()?.string()}")
    }
    fun onFailure(throwable: Throwable) {
        Log.e("ApiManager", "Network Failure: ${throwable.message}", throwable)
    }
}