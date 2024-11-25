package com.cookandroid.cherrysumer.retrofit

import android.app.Application
import android.content.Context
import com.cookandroid.cherrysumer.AuthInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MyApplication : Application() {

    companion object {
        const val BASE_URL = "http://3.39.110.119/"
        const val PREFS_NAME = "CherrySumerprefs" // MyPageFragment와 동일한 이름
        const val TOKEN_KEY = "token" // MyPageFragment와 동일한 키
        lateinit var networkService: ApiService
        private lateinit var appContext: Context

        /**
         * SharedPreferences에서 저장된 JWT 토큰 가져오기
         */
        fun getSavedToken(): String? {
            if (!::appContext.isInitialized) {
                throw IllegalStateException("Application context is not initialized")
            }
            val sharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return sharedPreferences.getString(TOKEN_KEY, null)
        }
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        // 저장된 토큰 가져오기
        val token = getSavedToken()

        // AuthInterceptor에 토큰 전달
        val authInterceptor = AuthInterceptor(this, token)

        // 로깅 인터셉터 설정 (디버깅용)
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        // OkHttpClient에 Interceptor 추가
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor) // AuthInterceptor 추가
            .addInterceptor(logging) // 로깅 인터셉터 추가
            .build()

        // Retrofit 인스턴스 생성
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // OkHttpClient 설정
            .addConverterFactory(GsonConverterFactory.create()) // JSON 데이터 변환
            .build()

        // ApiService 초기화
        networkService = retrofit.create(ApiService::class.java)
    }
}
