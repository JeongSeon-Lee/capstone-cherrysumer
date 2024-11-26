package com.cookandroid.cherrysumer.join

import android.util.Log
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.cookandroid.cherrysumer.LoginActivity
import com.cookandroid.cherrysumer.R
import com.google.gson.GsonBuilder
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class Signup1Activity : AppCompatActivity() {

    private lateinit var idInput: EditText
    private lateinit var pwInput: EditText
    private lateinit var nextButton: Button
    private lateinit var previousButton: ImageButton
    private lateinit var idCheckService: IdCheckService // Retrofit 서비스

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 액티비티의 전체 배경색을 하얀색으로 설정
        window.decorView.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        setContentView(R.layout.activity_join1)

        idInput = findViewById(R.id.id_input)
        pwInput = findViewById(R.id.pw_input)
        nextButton = findViewById(R.id.next_button)
        previousButton = findViewById(R.id.previous_button)

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

        idCheckService = retrofit.create(IdCheckService::class.java)

        nextButton.setOnClickListener {
            val id = idInput.text.toString().trim()
            val password = pwInput.text.toString().trim()

            // 필드가 비어있는지 확인
            if (id.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "모든 필드를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 싱글턴 객체 UserData에 데이터 저장
            UserData.loginId = id
            UserData.password = password

            Log.d("SignupData", "Stored Login ID: ${UserData.loginId}, Password: ${UserData.password}")

            // 서버로 ID 전송
            sendIdToServer(id)
        }

        // 이전 버튼 클릭 시 로그인 페이지로 이동
        previousButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // 서버로 ID 전송하는 함수
    private fun sendIdToServer(id: String) {
        idCheckService.checkIdExists(id).enqueue(object : Callback<IdCheckResponse> {
            override fun onResponse(call: Call<IdCheckResponse>, response: Response<IdCheckResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        if (responseBody.isSuccess) {
                            // ID 사용 가능: 다음 회원가입 단계로 이동
                            Toast.makeText(this@Signup1Activity, responseBody.message, Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@Signup1Activity, Signup2Activity::class.java)
                            startActivity(intent)
                        } else {
                            // 이미 존재하는 ID 경고 메시지
                            Toast.makeText(this@Signup1Activity, responseBody.message, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@Signup1Activity, "응답 본문이 비어 있습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@Signup1Activity, "알 수 없는 오류 발생: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<IdCheckResponse>, t: Throwable) {
                Log.e("Signup1Activity", "서버 연결 실패: ${t.message}", t)
                Toast.makeText(this@Signup1Activity, "서버 연결 실패", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

data class IdCheckResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String
)

interface IdCheckService {
    @GET("user/id-exists")
    fun checkIdExists(@Query("loginId") id: String): Call<IdCheckResponse>
}