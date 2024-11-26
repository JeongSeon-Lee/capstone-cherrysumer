package com.cookandroid.cherrysumer.join

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.cookandroid.cherrysumer.MainActivity
import com.cookandroid.cherrysumer.R

class Signup6Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 액티비티의 전체 배경색을 하얀색으로 설정
        window.decorView.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        setContentView(R.layout.activity_join6)

        // "시작하기" 버튼 클릭 시 메인 화면으로 이동
        findViewById<View>(R.id.start_button).setOnClickListener {
            navigateToMainActivity()
        }
    }

    private fun navigateToMainActivity() {
        // 메인 화면으로 이동
        val intent = Intent(this, MainActivity::class.java)

        // UserData에서 name과 region을 Intent에 추가
        intent.putExtra("name", UserData.name)
        intent.putExtra("region", UserData.region)

        startActivity(intent)
        finish() // 현재 Activity 종료
    }
}