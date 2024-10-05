package com.example.cherrysumer.retrofit

import android.content.Context
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = getToken()

        Log.d("AuthInterceptor", "Retrieved Token: $token") // 토큰 확인용 로그

        return if (token != null) {
            val newRequest = originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
            Log.d("AuthInterceptor", "Sending request with Authorization header")
            chain.proceed(newRequest)
        } else {
            Log.d("AuthInterceptor", "Sending request without Authorization header")
            chain.proceed(originalRequest)
        }
    }

    private fun getToken(): String? {
        val sharedPreferences = context.getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("jwt_token", null)
    }
}
