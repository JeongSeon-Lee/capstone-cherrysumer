package com.cookandroid.cherrysumer.onboarding

import android.os.Bundle
import android.widget.Button
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.viewpager2.widget.ViewPager2
import com.cookandroid.cherrysumer.LoginActivity
import com.cookandroid.cherrysumer.R
import com.cookandroid.cherrysumer.databinding.ActivityOnboardingBinding
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator

class OnboardingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_onboarding)

        // 어댑터 설정
        val adapter = OnboardingAdapter(this)
        binding.viewPager.adapter = adapter

        // ViewPager2의 스크롤 방향 설정
        binding.viewPager.layoutDirection = ViewPager2.LAYOUT_DIRECTION_LTR

        // ViewPager2 페이지 변경 콜백 등록
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // 기능 구현
            }
        })

        // DotsIndicator 설정
        binding.dotsIndicator.setViewPager2(binding.viewPager)

        // 시작 버튼 클릭 리스너 설정
        binding.startButton.setOnClickListener {
            try {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}