package com.cookandroid.cherrysumer

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.cookandroid.cherrysumer.onboarding.OnboardingActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header

class SplashActivity : AppCompatActivity() {

    // 스플래시 화면 표시 시간을 3초로 설정
    private val splashTimeOut: Long = 5000 // 3초

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("SplashActivity", "onCreate() called")
        super.onCreate(savedInstanceState)
        // 스플래시 화면 레이아웃 설정
        setContentView(R.layout.activity_splash)

        // 핸들러를 사용하여 지연 후 다음 화면으로 전환
        Handler(Looper.getMainLooper()).postDelayed({
            // 로그인 상태를 확인하는 함수 호출 (토큰 기반)
            checkLoginStatus()
        }, splashTimeOut)
    }

    override fun onStart() {
        super.onStart()
        Log.d("SplashActivity", "onStart() called")
    }

    override fun onResume() {
        super.onResume()
        Log.d("SplashActivity", "onResume() called")
    }

    private fun checkLoginStatus() {
        // SharedPreferences에서 토큰 확인
        val prefs = getSharedPreferences("CherrySumerprefs", MODE_PRIVATE)
        val token = prefs.getString("token", null)

        // 토큰이 있을 경우 서버에 유효성 확인 요청
        if (!token.isNullOrEmpty()) {
            validateToken(token)
        } else {
            // 토큰이 없으면 온보딩 화면으로 이동
            goToOnboarding()
        }
    }

    // 토큰 유효성 검사 함수
    private fun validateToken(token: String) {
        val apiService = RetrofitClient.getInstance().create(ApiService::class.java)
        val call = apiService.validateToken("Bearer $token")

        Log.d("SplashActivity", "Validating token...") // 토큰 유효성 검사 로그 추가

        call.enqueue(object : Callback<TokenValidationResponse> {
            override fun onResponse(
                call: Call<TokenValidationResponse>,
                response: Response<TokenValidationResponse>
            ) {
                Log.d("SplashActivity", "Token validation response: ${response.code()}") // 응답 코드 로그 추가

                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("SplashActivity", "Token validation success: $body") // 응답 데이터 로그 추가
                    when {
                        body?.isSuccess == true -> {
                            // 유효한 토큰일 경우 데이터 저장 후 메인 화면으로 이동
                            body.data?.let { tokenData ->
                                goToMain(tokenData) // 데이터를 넘기면서 메인 화면으로 이동
                            }
                        }
                        body?.code == "T404" -> {
                            Log.d("SplashActivity", "Token not found (T404), going to onboarding") // 로그 추가
                            // 토큰이 없는 경우 온보딩 화면으로 이동
                            goToOnboarding()
                        }
                        body?.code == "T401" -> {
                            Log.d("SplashActivity", "Invalid or expired token (T401), going to login") // 로그 추가
                            // 잘못된 토큰이나 만료된 토큰일 경우 로그인 화면으로 이동
                            goToLogin()
                        }
                        else -> {
                            Log.d("SplashActivity", "Unknown error, going to onboarding") // 로그 추가
                            // 기타 실패 시 온보딩 화면으로 이동
                            goToOnboarding()
                        }
                    }
                } else {
                    Log.e("SplashActivity", "Token validation failed with code: ${response.code()}") // 오류 로그 추가
                    // 응답 실패 시 온보딩 화면으로 이동
                    goToOnboarding()
                }
            }

            override fun onFailure(call: Call<TokenValidationResponse>, t: Throwable) {
                Log.e("SplashActivity", "Token validation failed: ${t.message}") // 실패 로그 추가
                t.printStackTrace()
                // 실패한 경우 온보딩 화면으로 이동
                goToOnboarding()
            }
        })
    }

    private fun goToMain(data: TokenData) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("name", data.name)
        intent.putExtra("region", data.region)
        startActivity(intent)
        finish() // 현재 스플래시 액티비티 종료
    }

    private fun goToOnboarding() {
        val intent = Intent(this, OnboardingActivity::class.java)
        startActivity(intent)
        finish() // 현재 스플래시 액티비티 종료
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // 현재 스플래시 액티비티 종료
    }
}

// TokenValidationResponse 데이터 클래스
data class TokenValidationResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val data: TokenData? // data는 null일 수도 있으므로 nullable로 설정
)

data class TokenData(
    val region: String,
    val name: String
)

// API 인터페이스 정의
interface ApiService {
    @GET("/user/auth") // 유효성 검사 엔드포인트로 변경
    fun validateToken(@Header("Authorization") token: String): Call<TokenValidationResponse>
}

// Retrofit 클라이언트 설정
object RetrofitClient {
    private const val BASE_URL = "http://3.39.110.119/" // 실제 API 기본 URL로 변경

    private var retrofit: Retrofit? = null

    fun getInstance(): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }
}