package com.example.cherrysumer

import android.app.Application
import android.content.Context
import com.example.cherrysumer.retrofit.INetworkService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MyApplication : Application() {

    companion object {
        const val BASE_URL = "http://3.39.110.119/"
        lateinit var networkService: INetworkService
    }

    override fun onCreate() {
        super.onCreate()

        // 임시 토큰 설정 (실제 유효한 테스트 토큰으로 교체)
        saveToken("eyJyZWdEYXRlIjoxNzI3ODU0NzQwMzQzLCJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMiIsImlzcyI6ImNoZXJyeXN1bWVyIiwiZXhwIjoxNzMwNDQ2NzQwfQ.eOVN_mOOc8B-ch91X43nCzfijBHGBUrvW5OM_ThBPAg")

        // AuthInterceptor에 this (Application context) 전달
        val authInterceptor = AuthInterceptor(this)

        // 로깅 인터셉터 설정 (디버깅용)
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        // OkHttpClient에 Interceptor 추가
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging) // 로깅 인터셉터 추가
            .build()

        // Retrofit 인스턴스 생성
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // OkHttpClient 설정
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // INetworkService 초기화
        networkService = retrofit.create(INetworkService::class.java)
    }

    private fun saveToken(token: String) {
        val sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("jwt_token", token)
        editor.apply()
    }
}
