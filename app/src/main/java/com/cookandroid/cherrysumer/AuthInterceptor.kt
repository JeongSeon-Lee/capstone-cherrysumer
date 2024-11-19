package com.cookandroid.cherrysumer

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import okhttp3.Interceptor
import okhttp3.Response
import java.net.HttpURLConnection

class AuthInterceptor(private val context: Context, private val token: String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        // 토큰이 있을 경우 Authorization 헤더에 추가
        token?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }

        val requestWithToken = requestBuilder.build()

        // 요청 세부정보 로그 출력
        Log.d("AuthInterceptor", "요청 URL: ${requestWithToken.url}")
        Log.d("AuthInterceptor", "요청 메서드: ${requestWithToken.method}")
        Log.d("AuthInterceptor", "요청 헤더: ${requestWithToken.headers}")
        val response = chain.proceed(requestWithToken)

        // 만약 서버 응답이 401(Unauthorized)라면 토큰이 만료된 것으로 처리
        if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
            // 메인 스레드에서 경고창을 띄우기 위해 Handler 사용
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                Toast.makeText(context, "토큰이 만료되었습니다. 다시 로그인 해주세요.", Toast.LENGTH_LONG).show()

                // 로그인 화면으로 이동
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            }
        }

        return response
    }
}