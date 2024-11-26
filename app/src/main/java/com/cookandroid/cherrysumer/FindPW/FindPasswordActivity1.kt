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
import retrofit2.http.GET
import retrofit2.http.Query
import com.cookandroid.cherrysumer.R
import com.google.gson.GsonBuilder

class FindPasswordActivity1 : AppCompatActivity() {

    private lateinit var previousButton: ImageButton
    private lateinit var userIdEditText: EditText
    private lateinit var nextButton: Button
    private lateinit var findPwService1: FindPwService1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 액티비티의 전체 배경색을 하얀색으로 설정
        window.decorView.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        setContentView(R.layout.activity_pw_find1)

        previousButton = findViewById(R.id.previous_button)
        userIdEditText = findViewById(R.id.user_id)
        nextButton = findViewById(R.id.pw_find_button)

        // Retrofit 클라이언트 초기화
        val retrofit = Retrofit.Builder()
            .baseUrl("http://3.39.110.119/")  // base URL
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder().setLenient().create() // JSON 파서 관대하게 설정
                )
            )
            .build()

        findPwService1 = retrofit.create(FindPwService1::class.java)

        // 뒤로 가기 버튼 클릭 시 이전 화면으로 이동
        previousButton.setOnClickListener {
            finish()  // 현재 액티비티 종료 -> 이전 페이지로 이동
        }

        // 다음 버튼 클릭 시 동작
        nextButton.setOnClickListener {
            val loginId = userIdEditText.text.toString()

            // 입력 필드가 비어있으면 경고창 표시
            if (loginId.isEmpty()) {
                Toast.makeText(this, "아이디를 입력하세요.", Toast.LENGTH_SHORT).show()
            } else {
                // 서버로 아이디 전송
                checkIdExists(loginId)
            }
        }
    }

    // 서버에 아이디 존재 여부를 확인하는 함수
    private fun checkIdExists(loginId: String) {
        findPwService1.checkIdExists(loginId).enqueue(object : Callback<IdResponse> {
            override fun onResponse(call: Call<IdResponse>, response: Response<IdResponse>) {
                if (response.isSuccessful) {
                    val result = response.body()
                    // 아이디가 존재할 때 (200 코드)
                    if (result != null && result.isSuccess && result.code == "200") {
                        // 아이디가 존재하지 않음 -> 경고창 표시
                        Toast.makeText(this@FindPasswordActivity1, "존재하지 않는 아이디입니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        // 아이디가 존재 -> 비밀번호 찾기 두 번째 페이지로 이동
                        val intent = Intent(this@FindPasswordActivity1, FindPasswordActivity2::class.java)
                        intent.putExtra("loginId", loginId) // loginId를 두 번째 페이지로 전달
                        startActivity(intent)
                    }
                } else {
                    // 상태 코드가 409일 때 처리 (이미 존재하는 아이디)
                    if (response.code() == 409) {
                        // 아이디가 존재 -> 비밀번호 찾기 두 번째 페이지로 이동
                        val intent = Intent(this@FindPasswordActivity1, FindPasswordActivity2::class.java)
                        intent.putExtra("loginId", loginId) // loginId를 두 번째 페이지로 전달
                        startActivity(intent)
                    } else {
                        // 그 외의 서버 오류 처리
                        val errorBody = response.errorBody()?.string()
                        Toast.makeText(this@FindPasswordActivity1, "서버 오류가 발생했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                        Log.e("FindIdActivity1", "Error: ${response.code()} - $errorBody")
                    }
                }
            }

            override fun onFailure(call: Call<IdResponse>, t: Throwable) {
                // 통신 오류 처리
                Toast.makeText(this@FindPasswordActivity1, "통신 오류가 발생했습니다. 인터넷 연결을 확인해주세요.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

// 서버 응답을 위한 데이터 모델
data class IdResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String
)

// Retrofit API 서비스 인터페이스
interface FindPwService1 {
    @GET("user/id-exists")
    fun checkIdExists(@Query("loginId") loginId: String): Call<IdResponse>
}