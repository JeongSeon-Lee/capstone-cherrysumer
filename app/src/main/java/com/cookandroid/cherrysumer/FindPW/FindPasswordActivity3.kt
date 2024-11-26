package com.cookandroid.cherrysumer.FindPW

import android.content.Intent
import android.os.Bundle
import android.util.Log
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

class FindPasswordActivity3 : AppCompatActivity() {

    private lateinit var previousButton: ImageButton
    private lateinit var changePwInput: EditText
    private lateinit var checkPwInput: EditText
    private lateinit var nextButton: Button
    private lateinit var changePwService: ChangePwService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 액티비티의 전체 배경색을 하얀색으로 설정
        window.decorView.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        setContentView(R.layout.activity_pw_find3)

        previousButton = findViewById(R.id.previous_button)
        changePwInput = findViewById(R.id.change_pw_input)
        checkPwInput = findViewById(R.id.check_input)
        nextButton = findViewById(R.id.pw_change_button)

        // 이전 페이지에서 전달받은 loginId
        val loginId = intent.getStringExtra("loginId")

        // Retrofit 초기화
        val retrofit = Retrofit.Builder()
            .baseUrl("http://3.39.110.119/")  // base URL
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder().setLenient().create() // JSON 파서 설정
                )
            )
            .build()

        changePwService = retrofit.create(ChangePwService::class.java)

        // 이전 버튼 클릭 시 이전 페이지로 이동
        previousButton.setOnClickListener {
            finish()  // 현재 액티비티 종료 -> 이전 페이지로 돌아감
        }

        // 다음 버튼 클릭 시 동작
        nextButton.setOnClickListener {
            val changePw = changePwInput.text.toString().trim()
            val checkPw = checkPwInput.text.toString().trim()

            // 입력칸 비어있을 경우 경고 메시지
            if (changePw.isEmpty() || checkPw.isEmpty()) {
                Toast.makeText(this, "모든 입력칸을 채워주세요.", Toast.LENGTH_SHORT).show()
            }
            // 두 비밀번호가 일치하지 않는 경우 경고 메시지
            else if (changePw != checkPw) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
            }
            // 조건 만족 시 서버로 비밀번호 변경 요청
            else {
                if (loginId != null) {
                    sendChangePasswordRequest(loginId, changePw)
                } else {
                    Toast.makeText(this, "알 수 없는 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 비밀번호 변경 요청을 서버로 보내는 함수
    private fun sendChangePasswordRequest(loginId: String, pasaword: String) {
        val requestData = ChangePasswordRequest(loginId, pasaword)
        Log.d("FindPasswordActivity3", "Changing password for user: $loginId, new password: $pasaword")

        changePwService.changePassword(requestData).enqueue(object : Callback<ChangePasswordResponse> {
            override fun onResponse(
                call: Call<ChangePasswordResponse>,
                response: Response<ChangePasswordResponse>
            ) {
                if (response.isSuccessful) {
                    val result = response.body()
                    if (result != null && result.isSuccess && result.code == "200") {
                        // 비밀번호 변경 성공 -> 네 번째 페이지로 이동
                        val intent = Intent(this@FindPasswordActivity3, FindPasswordActivity4::class.java)
                        startActivity(intent)
                    } else {
                        // 실패 응답에 따라 메시지 처리
                        val errorMessage = result?.message ?: "비밀번호 변경에 실패하였습니다."
                        val errorData = result?.data ?: ""
                        Toast.makeText(this@FindPasswordActivity3, "$errorMessage $errorData", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // 서버 오류 처리
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(this@FindPasswordActivity3, "서버 오류가 발생했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                    Log.e("FindIdActivity1", "Error: ${response.code()} - $errorBody")
                }
            }

            override fun onFailure(call: Call<ChangePasswordResponse>, t: Throwable) {
                // 통신 오류 처리
                Toast.makeText(this@FindPasswordActivity3, "통신 오류가 발생했습니다. 인터넷 연결을 확인해주세요.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

// 요청 데이터 모델
data class ChangePasswordRequest(
    val loginId: String,
    val password: String
)

// 응답 데이터 모델
data class ChangePasswordResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val data: String? = null // 데이터는 null일 수 있음
)

// Retrofit API 서비스 인터페이스
interface ChangePwService {
    @POST("user/changePwd")  // 요청 경로
    fun changePassword(@Body request: ChangePasswordRequest): Call<ChangePasswordResponse>
}