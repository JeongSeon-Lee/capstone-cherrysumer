package com.cookandroid.cherrysumer.FindPW

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import com.cookandroid.cherrysumer.R
import com.google.gson.GsonBuilder

class FindPasswordActivity2 : AppCompatActivity() {

    private lateinit var previousButton: ImageButton
    private lateinit var nextButton: Button
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var findPwService2: FindPwService2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 액티비티의 전체 배경색을 하얀색으로 설정
        window.decorView.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        setContentView(R.layout.activity_pw_find2)

        previousButton = findViewById(R.id.previous_button)
        nextButton = findViewById(R.id.pw_find_button)
        nameEditText = findViewById(R.id.name_input)
        emailEditText = findViewById(R.id.email_input)

        // 첫 번째 페이지에서 전달받은 loginId
        val loginId = intent.getStringExtra("loginId")

        // Retrofit 클라이언트 초기화
        val retrofit = Retrofit.Builder()
            .baseUrl("http://3.39.110.119/")  // base URL
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder().setLenient().create() // JSON 파서 설정
                )
            )
            .build()

        findPwService2 = retrofit.create(FindPwService2::class.java)

        // 이전 버튼 클릭 시 이전 페이지로 이동
        previousButton.setOnClickListener {
            finish()  // 현재 액티비티 종료 -> 이전 페이지로 돌아감
        }

        // 다음 버튼 클릭 시 동작
        nextButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()

            // 이름 또는 이메일이 비어있을 경우 경고 메시지 표시
            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "이름과 이메일을 모두 입력하세요.", Toast.LENGTH_SHORT).show()
            } else {
                // 서버로 POST 요청 보내기
                if (loginId != null) {
                    sendPasswordResetRequest(loginId, name, email)
                } else {
                    Toast.makeText(this, "알 수 없는 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 비밀번호 재설정 요청을 서버로 보내는 함수
    private fun sendPasswordResetRequest(loginId: String, name: String, email: String) {
        val requestData = PasswordResetRequest(loginId, name, email)

        findPwService2.resetPassword(requestData).enqueue(object : Callback<PasswordResetResponse> {
            override fun onResponse(
                call: Call<PasswordResetResponse>,
                response: Response<PasswordResetResponse>
            ) {
                if (response.isSuccessful) {
                    val result = response.body()
                    if (result != null && result.isSuccess && result.code == "200") {
                        // 인증 성공 -> 세 번째 비밀번호 찾기 페이지로 이동
                        val intent = Intent(this@FindPasswordActivity2, FindPasswordActivity3::class.java)
                        intent.putExtra("loginId", loginId)  // loginId를 다음 페이지로 전달
                        startActivity(intent)
                    } else {
                        // 인증 실패 -> 사용자 없음 경고창 표시
                        Toast.makeText(this@FindPasswordActivity2, "사용자를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // 서버 오류 처리
                    Toast.makeText(this@FindPasswordActivity2, "서버 오류가 발생했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PasswordResetResponse>, t: Throwable) {
                // 통신 오류 처리
                Toast.makeText(this@FindPasswordActivity2, "통신 오류가 발생했습니다. 인터넷 연결을 확인해주세요.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

// 요청 데이터 모델
data class PasswordResetRequest(
    val loginId: String,
    val name: String,
    val email: String
)

// 응답 데이터 모델
data class PasswordResetResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String
)

// Retrofit API 서비스 인터페이스
interface FindPwService2 {
    @POST("user/findPwd")
    fun resetPassword(@Body request: PasswordResetRequest): Call<PasswordResetResponse>
}