package com.cookandroid.cherrysumer.FindID

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.cookandroid.cherrysumer.LoginActivity
import com.cookandroid.cherrysumer.R

class FindIdActivity2 : AppCompatActivity() {

    private lateinit var previousButton: ImageButton
    private lateinit var userIdTextView: TextView
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 액티비티의 전체 배경색을 하얀색으로 설정
        window.decorView.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        setContentView(R.layout.activity_id_find2)

        previousButton = findViewById(R.id.previous_button)
        userIdTextView = findViewById(R.id.user_id)
        loginButton = findViewById(R.id.login_button)

        // 1. 이전 페이지에서 전달받은 loginId를 가져와서 user_id에 설정
        val loginId = intent.getStringExtra("loginId")
        if (loginId != null) {
            userIdTextView.text = loginId
        } else {
            // loginId가 없을 경우 처리 (optional)
            Toast.makeText(this, "아이디를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
        }

        // 2. 이전 버튼 클릭 시 이전 화면으로 돌아감
        previousButton.setOnClickListener {
            finish() // 현재 액티비티 종료
        }

        // 3. 로그인하러 가기 버튼 클릭 시 로그인 페이지로 이동
        loginButton.setOnClickListener {
            val intent = Intent(this@FindIdActivity2, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}