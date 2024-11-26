package com.cookandroid.cherrysumer.mypage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cookandroid.cherrysumer.AuthInterceptor
import com.cookandroid.cherrysumer.MainActivity
import com.cookandroid.cherrysumer.R
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

class ApplicantListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ApplicantAdapter
    private lateinit var applicantListService: ApplicantListService
    private val posts = mutableListOf<ApplicantList>()
    private var postId: Long = 0L

    private lateinit var allButton: TextView
    private lateinit var approvalButton: TextView
    private lateinit var refuseButton: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 액티비티의 전체 배경색을 하얀색으로 설정
        window.decorView.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        setContentView(R.layout.activity_mypage_applicant_list)

        // 이전 페이지로 돌아감
        val previousButton = findViewById<ImageButton>(R.id.previous_button)
        previousButton.setOnClickListener {
//            val intent = Intent(this, StatusActivity::class.java)
//            startActivity(intent)
            finish() // 현재 액티비티 종료
        }

        postId = intent.getLongExtra("postId", 0L)

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

        applicantListService = retrofit.create(ApplicantListService::class.java)
        adapter = ApplicantAdapter(posts, applicantListService)

        // RecyclerView 설정
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // 필터 버튼 설정
        allButton = findViewById(R.id.all_button)
        approvalButton = findViewById(R.id.approval_button)
        refuseButton = findViewById(R.id.refuse_button)

        // 필터 버튼 클릭 리스너 설정
        allButton.setOnClickListener { onFilterButtonClick(allButton, "전체") }
        approvalButton.setOnClickListener { onFilterButtonClick(approvalButton, "승인") }
        refuseButton.setOnClickListener { onFilterButtonClick(refuseButton, "거절") }

        // 기본적으로 전체 목록 요청
        fetchApplicantList("전체")
    }

    // SharedPreferences에서 토큰을 가져옴
    private fun getToken(): String? {
        val sharedPreferences = getSharedPreferences("CherrySumerprefs", MODE_PRIVATE)
        return sharedPreferences.getString("token", null)
    }

    // 관심목록 데이터 요청
    private fun fetchApplicantList(filter: String) {
        Log.d("ApplicantListActivity", "요청 파라미터 postId: $postId, filter: $filter")

        applicantListService.getApplicantList(postId, filter).enqueue(object : Callback<ApplicantListResponse> {
            override fun onResponse(
                call: Call<ApplicantListResponse>,
                response: Response<ApplicantListResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val posts = response.body()?.data
                    // 서버로부터 받은 데이터 로그 출력
                    Log.d("ApplicantListActivity", "서버로부터 받은 데이터: ${response.body()}")

                    if (posts.isNullOrEmpty()) {
                        showNoPostsMessage() // 데이터가 없을 때 empty_view 표시
                        adapter.updatePosts(emptyList())  // 어댑터에 빈 목록 전달
                    } else {
                        hideNoPostsMessage() // 데이터가 있을 때 empty_view 숨기기
                        adapter.updatePosts(posts)
                    }
                } else {
                    Toast.makeText(this@ApplicantListActivity, "요청이 실패했습니다.", Toast.LENGTH_SHORT).show()
                    Log.e("ApplicantListActivity", "요청이 실패했습니다. 코드: ${response.code()}, 메시지: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ApplicantListResponse>, t: Throwable) {
                Log.e("ApplicantListActivity", "서버와의 연결에 실패했습니다.", t)
                Toast.makeText(this@ApplicantListActivity, "서버와의 연결에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 내 게시글이 없을 때 empty_view 표시
    private fun showNoPostsMessage() {
        findViewById<View>(R.id.empty_view).visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    // 내 게시글이 있을 때 empty_view 숨기기
    private fun hideNoPostsMessage() {
        findViewById<View>(R.id.empty_view).visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }

    // 필터 버튼 클릭 처리
    private fun onFilterButtonClick(selectedButton: TextView, filter: String) {
        // 모든 버튼의 배경과 텍스트 색상 초기화
        resetButtonStyles()

        // 선택된 버튼에 대한 스타일 적용
        selectedButton.setBackgroundResource(R.drawable.option_selected_button)
        selectedButton.setTextColor(getColor(R.color.white))

        // 필터링된 데이터 요청
        fetchApplicantList(filter)
    }

    // 버튼 스타일 초기화
    private fun resetButtonStyles() {
        val paddingLeft = allButton.paddingLeft
        val paddingTop = allButton.paddingTop
        val paddingRight = allButton.paddingRight
        val paddingBottom = allButton.paddingBottom

        allButton.setBackgroundResource(R.drawable.post_category_select_button)
        allButton.setTextColor(getColor(R.color.black))
        allButton.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)  // 기존 패딩 값 유지

        approvalButton.setBackgroundResource(R.drawable.post_category_select_button)
        approvalButton.setTextColor(getColor(R.color.black))
        approvalButton.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)  // 기존 패딩 값 유지

        refuseButton.setBackgroundResource(R.drawable.post_category_select_button)
        refuseButton.setTextColor(getColor(R.color.black))
        refuseButton.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
    }
}

data class ApplicantListResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val data: List<ApplicantList>
)

data class ApplicantList(
    val userId: Long,
    val profileImageUrl: String?,
    val postId: Long,
    val nickname: String,
    val region: String,
    var isConfirmed: String
)

data class ApplicantDecisionRequest(
    val userId: Long,
    val postId: Long,
    val isConfirmed: String
)

data class ApplicantDecisionResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String
)

interface ApplicantListService {
    @GET("mypage/participations/{postId}")
    fun getApplicantList(
        @Path("postId") postId: Long,
        @Query("filter") filter: String
    ): Call<ApplicantListResponse>

    @POST("mypage/participations/decide")
    fun decideApplicant(
        @Body request: ApplicantDecisionRequest
    ): Call<ApplicantDecisionResponse>
}