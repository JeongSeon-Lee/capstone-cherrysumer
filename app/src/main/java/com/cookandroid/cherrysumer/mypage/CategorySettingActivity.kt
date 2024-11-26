package com.cookandroid.cherrysumer.mypage

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
import androidx.core.content.ContextCompat
import com.cookandroid.cherrysumer.AuthInterceptor
import com.cookandroid.cherrysumer.R
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.POST
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Body

class CategorySettingActivity : AppCompatActivity() {
    private lateinit var items: List<LinearLayout>
    private val selectedBackgroundColor = Color.parseColor("#FF5959")
    private val defaultBackgroundColor = Color.parseColor("#F0F0F0")
    private lateinit var submitButton: Button
    private lateinit var previousButton: ImageButton
    private lateinit var mycategoryService: MyCategoryService

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
        // 액티비티의 전체 배경색을 하얀색으로 설정
        window.decorView.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        setContentView(R.layout.activity_mypage_category)

        submitButton = findViewById(R.id.submit_button)
        previousButton = findViewById(R.id.previous_button)

        previousButton.setOnClickListener {
            finish() // 현재 액티비티 종료
        }

        // Retrofit 초기화
        val token = getToken()
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(this, token))
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://3.39.110.119/")
            .client(okHttpClient)  // OkHttpClient를 Retrofit에 추가
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder()
                        .setLenient() // JSON 파서가 관대하게 설정
                        .create()
                )
            )
            .build()

        mycategoryService = retrofit.create(MyCategoryService::class.java)

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

        // 저장 버튼 클릭 리스너
        submitButton.setOnClickListener {
            submitSelectedCategories()
        }
    }

    // SharedPreferences에서 토큰을 가져옴
    private fun getToken(): String? {
        val sharedPreferences = getSharedPreferences("CherrySumerprefs", MODE_PRIVATE)
        return sharedPreferences.getString("token", null)
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

    // 선택된 카테고리 서버로 전송
    private fun submitSelectedCategories() {
        val selectedCategories = items
            .filter { (it.background as? ColorDrawable)?.color == selectedBackgroundColor }
            .mapNotNull { itemCategoryMap[it.id] }  // 선택된 카테고리 리스트

        if (selectedCategories.isEmpty()) {
            Toast.makeText(this, "카테고리를 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val requestBody = CategoryRequestBody(selectedCategories)

        // 서버 요청 보내기
        mycategoryService.submitCategories(requestBody).enqueue(object : Callback<MyCategoryResponse> {
            override fun onResponse(call: Call<MyCategoryResponse>, response: Response<MyCategoryResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!
                    if (responseBody.isSuccess) {
                        Toast.makeText(this@CategorySettingActivity, "카테고리가 성공적으로 저장되었습니다.", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@CategorySettingActivity, "오류가 발생했습니다: ${responseBody.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@CategorySettingActivity, "서버 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MyCategoryResponse>, t: Throwable) {
                Toast.makeText(this@CategorySettingActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

// 서버로 보낼 카테고리 리스트를 위한 데이터 클래스
data class CategoryRequestBody(
    val category: List<String>
)

// 서버 응답 데이터 클래스
data class MyCategoryResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val data: List<String>?
)

// Retrofit 인터페이스
interface MyCategoryService {
    @POST("mypage/change/category")
    fun submitCategories(
        @Body requestBody: CategoryRequestBody
    ): Call<MyCategoryResponse>
}
