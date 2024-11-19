package com.cookandroid.cherrysumer

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.cookandroid.cherrysumer.FindID.FindIdActivity1
import com.cookandroid.cherrysumer.FindPW.FindPasswordActivity1
import com.cookandroid.cherrysumer.databinding.ActivityLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import com.cookandroid.cherrysumer.R

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginService: LoginService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        // Retrofit 초기화
        val retrofit = Retrofit.Builder()
            .baseUrl("http://3.39.110.119/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        loginService = retrofit.create(LoginService::class.java)

        // 로그인 버튼 클릭 리스너 설정
        binding.loginButton.setOnClickListener {
            val id = binding.idInput.text.toString()
            val password = binding.pwInput.text.toString()

            if (id.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                login(id, password)
            }
        }

        // 회원가입 클릭 리스너 설정
        binding.joinOption.setOnClickListener {
            val intent = Intent(this, com.cookandroid.cherrysumer.join.Signup1Activity::class.java)
            startActivity(intent)
        }

        // 아이디 찾기 클릭 리스너 설정
        binding.forgotId.setOnClickListener {
            val intent = Intent(this, FindIdActivity1::class.java)
            startActivity(intent)
        }

        // 비밀번호 찾기 클릭 리스너 설정
        binding.forgotPassword.setOnClickListener {
            val intent = Intent(this, FindPasswordActivity1::class.java)
            startActivity(intent)
        }
    }

    // 로그인 함수
    private fun login(id: String, password: String) {
        val loginRequest = LoginRequest(id, password)
        loginService.login(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { responseBody ->
                        if (responseBody.isSuccess) {
                            // 로그인 성공, 메인 화면으로 이동
                            Toast.makeText(this@LoginActivity, "로그인 성공", Toast.LENGTH_SHORT).show()
                            val token = responseBody.data?.token // JWT 토큰
                            saveToken(token ?: "") // 토큰 저장
                            // 추가적으로 지역 및 이름도 필요하다면 처리
                            val region = responseBody.data?.region
                            val name = responseBody.data?.name

                            val intent = Intent(this@LoginActivity, MainActivity::class.java).apply {
                                putExtra("token", token)
                                putExtra("region", region)
                                putExtra("name", name)
                            }
                            startActivity(intent)
                            finish()
                        } else {
                            // 로그인 실패, 경고창 표시
                            Toast.makeText(this@LoginActivity, responseBody.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    // 다른 상태 코드 처리
                    Toast.makeText(this@LoginActivity, "알 수 없는 오류 발생: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                // 서버와의 통신 실패
                Toast.makeText(this@LoginActivity, "서버 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 토큰 저장 함수
    private fun saveToken(token: String) {
        val sharedPreferences = getSharedPreferences("CherrySumerprefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("token", token)
            apply()
        }
    }

}

// 로그인 요청 데이터 클래스
data class LoginRequest(val loginId: String, val password: String)

// 로그인 응답 데이터 클래스
data class LoginResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val data: LoginData?
)

// 로그인 데이터 클래스 (토큰, 지역, 이름)
data class LoginData(
    val token: String?,
    val region: String?,
    val name: String?
)

// Retrofit 서비스 인터페이스
interface LoginService {
    @POST("user/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
}