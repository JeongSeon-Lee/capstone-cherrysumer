package com.cookandroid.cherrysumer.join

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.core.view.isVisible
import com.cookandroid.cherrysumer.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Body
import retrofit2.http.POST

class Signup3Activity : AppCompatActivity() {
    private lateinit var emailInput: EditText
    private lateinit var checkButton: Button
    private lateinit var checkContainer: LinearLayout
    private lateinit var checkNumber: EditText
    private lateinit var timerText: TextView
    private lateinit var nextButton: Button
    private lateinit var previousButton: ImageButton
    private var timerStarted = false
    private var timerHandler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    private var secondsRemaining = 600 // 10 minutes
    private lateinit var apiService: ApiService

    private var isEmailVerified = false // 이메일 인증 성공 여부 플래그

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join3)

        emailInput = findViewById(R.id.email_input)
        checkButton = findViewById(R.id.check_button)
        checkContainer = findViewById(R.id.check_container)
        checkNumber = findViewById(R.id.check_number)
        timerText = findViewById(R.id.timer)
        nextButton = findViewById(R.id.nextButton)
        previousButton = findViewById(R.id.previous_button)

        // Retrofit 초기화
        val retrofit = Retrofit.Builder()
            .baseUrl("http://3.39.110.119/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        checkButton.setOnClickListener { sendVerificationEmail() }
        nextButton.setOnClickListener {
            Log.d("Signup3Activity", "Next button clicked")
            verifyCode()
        }
        previousButton.setOnClickListener { finish() }
    }

    private fun sendVerificationEmail() {
        val email = emailInput.text.toString()
        if (!isValidEmail(email)) {
            showToast("유효한 이메일 주소를 입력하세요.")
            return
        }

        // ViewModel에 이메일 저장
        UserData.email = email

        // 이메일 GET 요청 전송
        apiService.sendVerificationEmail(email).enqueue(object : Callback<EmailResponse> {
            override fun onFailure(call: Call<EmailResponse>, t: Throwable) {
                showToast("서버 오류. 나중에 다시 시도해 주세요.")
            }

            override fun onResponse(call: Call<EmailResponse>, response: Response<EmailResponse>) {
                val body = response.body()
                if (response.isSuccessful && body?.isSuccess == true) {
                    runOnUiThread {
                        checkContainer.visibility = View.VISIBLE
                        startTimer()
                    }
                } else {
                    showToast(body?.message ?: "서버 오류. 나중에 다시 시도해 주세요.")
                }
            }
        })
    }

    private fun verifyCode() {
        val code = checkNumber.text.toString()
        val email = UserData.email

        // 이메일이 null이 아닐 경우에만 진행
        if (email.isNullOrEmpty()) {
            showToast("이메일을 입력하세요.")
            return
        }

        if (code.isEmpty()) {
            showToast("인증번호를 입력하세요.")
            return
        }

        Log.d("Signup3Activity", "Verifying code: $code") // 인증 코드 로그

        // 인증번호 및 이메일 POST 요청 전송
        apiService.verifyCode(CodeRequest(email, code)).enqueue(object : Callback<EmailResponse> {
            override fun onFailure(call: Call<EmailResponse>, t: Throwable) {
                showToast("서버 오류. 나중에 다시 시도해 주세요.")
                Log.e("Signup3Activity", "API 호출 실패", t)
            }

            override fun onResponse(call: Call<EmailResponse>, response: Response<EmailResponse>) {
                Log.d("Signup3Activity", "Response Code: ${response.code()}")
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    if (body.isSuccess) {
                        isEmailVerified = true // 이메일 인증 성공 플래그 설정
                        startActivity(Intent(this@Signup3Activity, Signup4Activity::class.java))
                    } else {
                        showToast(body.message ?: "서버 오류. 나중에 다시 시도해 주세요.")
                    }
                } else {
                    showToast("서버 오류. 나중에 다시 시도해 주세요.")
                }
            }
        })
    }

    private fun startTimer() {
        secondsRemaining = 600 // 10 minutes
        updateTimerText()

        timerRunnable = object : Runnable {
            override fun run() {
                if (secondsRemaining > 0) {
                    secondsRemaining--
                    updateTimerText()
                    timerHandler.postDelayed(this, 1000)
                } else {
                    showToast("타이머가 만료되었습니다.") // 타이머 만료 알림
                    checkContainer.visibility = View.INVISIBLE // UI 업데이트
                }
            }
        }
        timerHandler.postDelayed(timerRunnable!!, 1000)
    }

    private fun updateTimerText() {
        val minutes = secondsRemaining / 60
        val seconds = secondsRemaining % 60
        val time = String.format("%02d:%02d", minutes, seconds)
        timerText.text = time
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this@Signup3Activity, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timerHandler.removeCallbacks(timerRunnable ?: return)
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

// Retrofit 인터페이스 정의
interface ApiService {
    @GET("user/email-verification")
    fun sendVerificationEmail(@Query("email") email: String): Call<EmailResponse>

    @POST("user/email-verification")
    fun verifyCode(@Body request: CodeRequest): Call<EmailResponse>
}

// 이메일 요청 데이터 클래스
data class EmailResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String
)

// 인증 코드 요청 데이터 클래스
data class CodeRequest(val email: String, val code: String)