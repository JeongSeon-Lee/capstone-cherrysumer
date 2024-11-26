package com.cookandroid.cherrysumer.FindPW

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import androidx.core.content.ContextCompat
import com.cookandroid.cherrysumer.LoginActivity
import com.cookandroid.cherrysumer.R

class FindPasswordActivity4 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 액티비티의 전체 배경색을 하얀색으로 설정
        window.decorView.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        setContentView(R.layout.activity_pw_find4)

        // '로그인 하러 가기' 버튼
        val loginButton: Button = findViewById(R.id.login_button)
        loginButton.setOnClickListener {
            // 로그인 페이지로 이동
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish() // 현재 화면 종료
        }
    }
}