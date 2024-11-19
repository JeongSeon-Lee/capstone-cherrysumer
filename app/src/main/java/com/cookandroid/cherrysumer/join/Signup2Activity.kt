package com.cookandroid.cherrysumer.join

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.cookandroid.cherrysumer.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query



class Signup2Activity : AppCompatActivity() {
    private lateinit var nameInput: EditText
    private lateinit var nicknameInput: EditText
    private lateinit var nextButton: Button
    private lateinit var previousButton: ImageButton

    // Retrofit 서비스 인스턴스
    private lateinit var nickNameCheckService: NickNameCheckService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join2)

        // View 초기화
        nameInput = findViewById(R.id.name_input)
        nicknameInput = findViewById(R.id.nickname_input)
        nextButton = findViewById(R.id.nextButton)
        previousButton = findViewById(R.id.previous_button)

        // Retrofit 초기화
        val retrofit = Retrofit.Builder()
            .baseUrl("http://3.39.110.119/") // 서버 베이스 URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        nickNameCheckService = retrofit.create(NickNameCheckService::class.java)

        // 버튼 리스너 설정
        nextButton.setOnClickListener { onNextButtonClick() }
        previousButton.setOnClickListener { onPreviousButtonClick() }
    }

    private fun onNextButtonClick() {
        val name = nameInput.text.toString().trim()
        val nickname = nicknameInput.text.toString().trim()

        if (name.isEmpty() || nickname.isEmpty()) {
            showAlert("모든 필드를 입력해주세요.")
            return
        }

        UserData.name = name
        UserData.nickname = nickname

        // 서버로 nickname 전송
        sendNicknameToServer(nickname)
    }

    private fun onPreviousButtonClick() {
        // 첫 번째 회원가입 페이지로 이동
        val intent = Intent(this, Signup1Activity::class.java)
        startActivity(intent)
        finish()
    }

    private fun sendNicknameToServer(nickname: String) {
        nickNameCheckService.checkNicknameExists(nickname).enqueue(object : Callback<NicknameCheckResponse> {
            override fun onResponse(call: Call<NicknameCheckResponse>, response: Response<NicknameCheckResponse>) {
                Log.d("Signup2Activity", "Response Code: ${response.code()}")
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        if (responseBody.isSuccess) {
                            // 닉네임 사용 가능: 다음 회원가입 페이지로 이동
                            val intent = Intent(this@Signup2Activity, Signup3Activity::class.java)
                            startActivity(intent)
                        } else {
                            // 닉네임이 이미 존재하는 경우 경고 메시지
                            showAlert(responseBody.message)
                        }
                    } else {
                        showAlert("응답 본문이 비어 있습니다.")
                    }
                } else {
                    showAlert("알 수 없는 오류가 발생했습니다. 코드: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<NicknameCheckResponse>, t: Throwable) {
                showAlert("서버와의 통신에 실패했습니다: ${t.message}")
            }
        })
    }

    private fun showAlert(message: String) {
        AlertDialog.Builder(this)
            .setTitle("경고")
            .setMessage(message)
            .setPositiveButton("확인", null)
            .show()
    }
}

// 데이터 클래스 (서버 응답)
data class NicknameCheckResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String
)

// Retrofit 인터페이스 정의
interface NickNameCheckService {
    @GET("user/nickname-exists")
    fun checkNicknameExists(@Query("nickname") nickname: String): Call<NicknameCheckResponse>
}