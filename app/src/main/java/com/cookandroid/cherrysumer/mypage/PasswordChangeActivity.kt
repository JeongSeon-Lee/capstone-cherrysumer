package com.cookandroid.cherrysumer.mypage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.cookandroid.cherrysumer.AuthInterceptor
import com.cookandroid.cherrysumer.FindPW.ChangePasswordRequest
import com.cookandroid.cherrysumer.FindPW.ChangePasswordResponse
import com.cookandroid.cherrysumer.FindPW.FindPasswordActivity4
import com.cookandroid.cherrysumer.R
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

class PasswordChangeActivity : AppCompatActivity() {
    private lateinit var previousButton: ImageButton
    private lateinit var nowPassword: EditText
    private lateinit var changePassword: EditText
    private lateinit var checkInput: EditText
    private lateinit var passwordChangeButton: Button
    private lateinit var passwordChangeService: PasswordChangeService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 액티비티의 전체 배경색을 하얀색으로 설정
        window.decorView.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        setContentView(R.layout.activity_mypage_password_change)

        // Retrofit 초기화
        val token = getToken()
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(this, token))
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://3.39.110.119/")
            .client(okHttpClient)  // OkHttpClient를 Retrofit에 추가
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder()
                        .setLenient() // JSON 파서가 관대하게 설정
                        .create()
                )
            )
            .build()

        passwordChangeService = retrofit.create(PasswordChangeService::class.java)

        previousButton = findViewById(R.id.previous_button)
        nowPassword = findViewById(R.id.now_password)
        changePassword = findViewById(R.id.change_password)
        checkInput = findViewById(R.id.check_input)
        passwordChangeButton = findViewById(R.id.pw_change_button)

        val loginId = intent.getStringExtra("loginId")

        previousButton.setOnClickListener {
            finish() // 현재 액티비티 종료
        }


        passwordChangeButton.setOnClickListener {
            val nowPW = nowPassword.text.toString().trim()
            val changePw = changePassword.text.toString().trim()
            val checkPW = checkInput.text.toString().trim()

            // 입력칸 비어있을 경우 경고 메시지
            if (nowPW.isEmpty() || changePw.isEmpty() || checkPW.isEmpty()) {
                Toast.makeText(this, "모든 입력칸을 채워주세요.", Toast.LENGTH_SHORT).show()
            }

            // 두 비밀번호가 일치하지 않는 경우 경고 메시지
            else if (changePw != checkPW) {
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
        val requestData = PasswordChangeRequest(loginId, pasaword)
        Log.d("PasswordChangeActivity", "Changing password for user: $loginId, new password: $pasaword")

        passwordChangeService.getChangePassword(requestData).enqueue(object :
            Callback<PasswordChangeResponse> {
            override fun onResponse(
                call: Call<PasswordChangeResponse>,
                response: Response<PasswordChangeResponse>
            ) {
                if (response.isSuccessful) {
                    val result = response.body()
                    if (result != null && result.isSuccess && result.code == "200") {
                        Toast.makeText(this@PasswordChangeActivity, "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show()
                        finish() // 현재 액티비티 종료
                    } else {
                        // 실패 응답에 따라 메시지 처리
                        val errorMessage = result?.message ?: "비밀번호 변경에 실패하였습니다."
                        val errorData = result?.data ?: ""
                        Toast.makeText(this@PasswordChangeActivity, "$errorMessage $errorData", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // 서버 오류 처리
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(this@PasswordChangeActivity, "서버 오류가 발생했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                    Log.e("FindIdActivity1", "Error: ${response.code()} - $errorBody")
                }
            }

            override fun onFailure(call: Call<PasswordChangeResponse>, t: Throwable) {
                // 통신 오류 처리
                Toast.makeText(this@PasswordChangeActivity, "통신 오류가 발생했습니다. 인터넷 연결을 확인해주세요.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // SharedPreferences에서 토큰을 가져옴
    private fun getToken(): String? {
        val sharedPreferences = getSharedPreferences("CherrySumerprefs", MODE_PRIVATE)
        return sharedPreferences.getString("token", null)
    }

}

data class PasswordChangeRequest(
    val loginId: String,
    val password: String
)

data class PasswordChangeResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val data: String? = null // 데이터는 null일 수 있음
)

interface PasswordChangeService {
    @POST("user/changePwd")
    fun getChangePassword(@Body request: PasswordChangeRequest): Call<PasswordChangeResponse>
}