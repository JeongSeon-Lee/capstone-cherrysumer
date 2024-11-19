package com.cookandroid.cherrysumer.mypage

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.cookandroid.cherrysumer.MainActivity
import com.cookandroid.cherrysumer.R

class StatusActivity : AppCompatActivity() {
    private lateinit var participationButton: LinearLayout
    private lateinit var recruitmentButton: LinearLayout
    private lateinit var participationText: TextView
    private lateinit var recruitmentText: TextView
    private lateinit var underLine1: View
    private lateinit var underLine2: View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage_participation_recruitment)

        participationButton = findViewById(R.id.participation_button)
        recruitmentButton = findViewById(R.id.recruitment_button)
        participationText = findViewById(R.id.participation_text)
        recruitmentText = findViewById(R.id.recruitment_text)
        underLine1 = findViewById(R.id.under_line1)
        underLine2 = findViewById(R.id.under_line2)

        // 이전 버튼 클릭 시 마이페이지로 돌아감
        val previousButton = findViewById<ImageButton>(R.id.previous_button)
        previousButton.setOnClickListener {
//            val intent = Intent(this, MainActivity::class.java)
//            intent.putExtra("goToMyPage", true) // MyPageFragment로 돌아가라는 신호 전달
//            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
//            startActivity(intent)
            finish() // 현재 액티비티 종료
        }

        participationButton.setOnClickListener {
            showFragment(ParticipationFragment()) // 참여 버튼 클릭 시 ParticipationFragment로 전환
            updateButtonStyles(true) // 참여 버튼 활성화
        }

        recruitmentButton.setOnClickListener {
            showFragment(RecruitmentFragment()) // 모집 버튼 클릭 시 RecruitmentFragment로 전환
            updateButtonStyles(false) // 모집 버튼 활성화
        }

        // 초기 화면은 참여 프래그먼트로 설정 (옵션)
        if (savedInstanceState == null) {
            showFragment(ParticipationFragment())
            updateButtonStyles(true)
        }
    }

    private fun updateButtonStyles(isParticipationSelected: Boolean) {
        if (isParticipationSelected) {
            participationText.setTextColor(ContextCompat.getColor(this, R.color.cherry)) // 선택된 텍스트 색상
            underLine1.setBackgroundColor(ContextCompat.getColor(this, R.color.cherry))
            underLine1.layoutParams.height = resources.getDimensionPixelSize(R.dimen.selected_line_height)
            participationText.typeface = ResourcesCompat.getFont(this, R.font.notosans_kr_bold)

            recruitmentText.setTextColor(ContextCompat.getColor(this, R.color.grey_B4))
            underLine2.setBackgroundColor(ContextCompat.getColor(this, R.color.grey_EB))
            underLine2.layoutParams.height = resources.getDimensionPixelSize(R.dimen.default_line_height)
            recruitmentText.typeface = ResourcesCompat.getFont(this, R.font.notosans_kr_medium)
        } else {
            recruitmentText.setTextColor(ContextCompat.getColor(this, R.color.cherry))
            underLine2.setBackgroundColor(ContextCompat.getColor(this, R.color.cherry))
            underLine2.layoutParams.height = resources.getDimensionPixelSize(R.dimen.selected_line_height)
            recruitmentText.typeface = ResourcesCompat.getFont(this, R.font.notosans_kr_bold)

            participationText.setTextColor(ContextCompat.getColor(this, R.color.grey_B4))
            underLine1.setBackgroundColor(ContextCompat.getColor(this, R.color.grey_EB))
            underLine1.layoutParams.height = resources.getDimensionPixelSize(R.dimen.default_line_height)
            participationText.typeface = ResourcesCompat.getFont(this, R.font.notosans_kr_medium)
        }
        underLine1.requestLayout()
        underLine2.requestLayout()
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}