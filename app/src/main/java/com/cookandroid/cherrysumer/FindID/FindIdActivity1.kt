package com.cookandroid.cherrysumer.FindID

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
import retrofit2.http.GET
import retrofit2.http.Query
import com.cookandroid.cherrysumer.R
import com.google.gson.GsonBuilder

class FindIdActivity1 : AppCompatActivity() {

    private lateinit var previousButton: ImageButton
    private lateinit var emailEditText: EditText
    private lateinit var nextButton: Button
    private lateinit var findIdService: FindIdService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 액티비티의 전체 배경색을 하얀색으로 설정
        window.decorView.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        setContentView(R.layout.activity_id_find1)

        previousButton = findViewById(R.id.previous_button)
        emailEditText = findViewById(R.id.user_email)
        nextButton = findViewById(R.id.id_find_button)

        // Retrofit 클라이언트 초기화
        val retrofit = Retrofit.Builder()
            .baseUrl("http://3.39.110.119/")
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder()
                        .setLenient() // JSON 파서가 관대하게 설정
                        .create()
                )
            )
            .build()

        findIdService = retrofit.create(FindIdService::class.java)

        // 이전 버튼 클릭 시 이전 화면으로 돌아감
        previousButton.setOnClickListener {
            finish() // 현재 액티비티 종료
        }

        // 다음 버튼 클릭 시 이메일 전송
        nextButton.setOnClickListener {
            val email = emailEditText.text.toString()

            if (email.isEmpty()) {
                Toast.makeText(this, "이메일을 입력하세요.", Toast.LENGTH_SHORT).show()
            } else {
                sendEmailToServer(email)
            }
        }
    }

    private fun sendEmailToServer(email: String) {
        findIdService.findIdByEmail(email).enqueue(object : Callback<EmailResponse> {
            override fun onResponse(call: Call<EmailResponse>, response: Response<EmailResponse>) {
                if (response.isSuccessful) {
                    val result = response.body()
                    if (result != null && result.isSuccess && result.code == "200") {
                        // 성공 시 다음 화면으로 이동
                        val intent = Intent(this@FindIdActivity1, FindIdActivity2::class.java)
                        intent.putExtra("loginId", result.data?.loginId)
                        startActivity(intent)
                    } else {
                        // 실패 시 경고창
                        Toast.makeText(this@FindIdActivity1, "가입하지 않은 이메일입니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // 서버 에러 처리
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(this@FindIdActivity1, "존재하지 않는 이메일입니다.", Toast.LENGTH_SHORT).show()
                    Log.e("FindIdActivity1", "Error: ${response.code()} - $errorBody")
                }
            }

            override fun onFailure(call: Call<EmailResponse>, t: Throwable) {
                // 통신 실패 시 처리
                Toast.makeText(this@FindIdActivity1, "통신 오류가 발생했습니다. 인터넷 연결을 확인해주세요.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

// 2. EmailResponse - 서버 응답을 위한 데이터 모델
data class EmailResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val data: EmailData?
)

data class EmailData(
    val loginId: String?
)

// 3. Retrofit API 서비스 인터페이스
interface FindIdService {
    @GET("user/findId")
    fun findIdByEmail(@Query("email") email: String): Call<EmailResponse>
}