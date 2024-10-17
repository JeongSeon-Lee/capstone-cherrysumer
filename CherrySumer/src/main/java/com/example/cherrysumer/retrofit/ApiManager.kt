package com.example.cherrysumer.retrofit

import android.util.Log
import com.example.cherrysumer.retrofit.models.ApiResponse
import com.example.cherrysumer.retrofit.models.InventoryItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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