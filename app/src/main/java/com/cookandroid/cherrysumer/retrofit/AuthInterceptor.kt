package com.cookandroid.cherrysumer.retrofit

import okhttp3.Interceptor
import okhttp3.Response
import android.util.Log

class AuthInterceptor(private val token: String?) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        return if (!token.isNullOrEmpty()) {
            val newRequest = originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token") // 토큰 추가
                .build()
            Log.d("AuthInterceptor", "Request with token: $token")
            chain.proceed(newRequest)
        } else {
            Log.d("AuthInterceptor", "Request without token")
            chain.proceed(originalRequest)
        }
    }
}
