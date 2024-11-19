package com.cookandroid.cherrysumer.join

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cookandroid.cherrysumer.R

class Signup4Activity : AppCompatActivity() {
    private lateinit var items: List<LinearLayout>
    private val selectedBackgroundColor = Color.parseColor("#FF5959")
    private val defaultBackgroundColor = Color.parseColor("#F0F0F0")

    // 아이템 ID와 카테고리 번호 매핑 (ID에 따라 숫자로 변환)
    private val itemCategoryMap = mapOf(
        R.id.item1 to "과일",
        R.id.item2 to "배달",
        R.id.item3 to "정육",
        R.id.item4 to "냉동식품",
        R.id.item5 to "수산물",
        R.id.item6 to "음료",
        R.id.item7 to "간편식",
        R.id.item8 to "디저트",
        R.id.item9 to "생활용품",
        R.id.item10 to "채소",
        R.id.item11 to "유제품"

    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join4)

        // 아이템 리스트 초기화
        items = listOf(
            findViewById(R.id.item1),
            findViewById(R.id.item2),
            findViewById(R.id.item3),
            findViewById(R.id.item4),
            findViewById(R.id.item5),
            findViewById(R.id.item6),
            findViewById(R.id.item7),
            findViewById(R.id.item8),
            findViewById(R.id.item9),
            findViewById(R.id.item10),
            findViewById(R.id.item11)
        )

        // 아이템 클릭 리스너 설정
        for (item in items) {
            item.setOnClickListener {
                toggleSelection(item)
                logSelectedCategories()
            }
        }

        // 다음 버튼 설정
        findViewById<Button>(R.id.nextButton).setOnClickListener {
            navigateToNextPage()
        }

        // 이전 버튼 설정
        findViewById<ImageButton>(R.id.previous_button).setOnClickListener {
            navigateToPreviousPage()
        }
    }

    // 아이템 선택 토글 기능
    private fun toggleSelection(item: LinearLayout) {
        val currentColor = (item.background as? ColorDrawable)?.color ?: defaultBackgroundColor
        if (currentColor == selectedBackgroundColor) {
            item.setBackgroundColor(defaultBackgroundColor)
        } else {
            item.setBackgroundColor(selectedBackgroundColor)
        }
    }

    // 선택된 카테고리 로그로 출력
    private fun logSelectedCategories() {
        val selectedCategories = items
            .filter { (it.background as? ColorDrawable)?.color == selectedBackgroundColor }
            .mapNotNull { itemCategoryMap[it.id] }  // 문자열 값으로 변환

        Log.d("Signup4Activity", "선택된 카테고리: $selectedCategories")
    }

    // 다음 페이지로 이동ㅅ
    private fun navigateToNextPage() {
        // 선택된 카테고리 추출 (아이템 ID를 문자열로 변환)
        val selectedCategories = items
            .filter { (it.background as? ColorDrawable)?.color == selectedBackgroundColor }
            .mapNotNull { itemCategoryMap[it.id] }  // 문자열 값으로 변환

        if (selectedCategories.isNotEmpty()) {
            // UserData에 선택된 카테고리 저장
            UserData.category = selectedCategories

            startActivity(Intent(this, Signup5Activity::class.java))
            finish()
        } else {
            Toast.makeText(this, "하나 이상의 카테고리를 선택해 주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    // 이전 페이지로 이동
    private fun navigateToPreviousPage() {
        startActivity(Intent(this, Signup3Activity::class.java))
        finish()
    }
}