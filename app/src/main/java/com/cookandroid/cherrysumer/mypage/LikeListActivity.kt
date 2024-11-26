package com.cookandroid.cherrysumer.mypage

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cookandroid.cherrysumer.AuthInterceptor
import com.cookandroid.cherrysumer.LikeResponse
import com.cookandroid.cherrysumer.Post
import com.cookandroid.cherrysumer.PostDetailData
import com.cookandroid.cherrysumer.PostDetailFragment
import com.cookandroid.cherrysumer.PostDetailResponse
import com.cookandroid.cherrysumer.PostResponse
import com.cookandroid.cherrysumer.R
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

class LikeListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LikePostsAdapter
    private lateinit var likePostService: LikePostService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 액티비티의 전체 배경색을 하얀색으로 설정
        window.decorView.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        setContentView(R.layout.activity_mypage_likelist)

        // 이전 버튼 클릭 시 마이페이지로 돌아감
        val previousButton = findViewById<ImageButton>(R.id.previous_button)
        previousButton.setOnClickListener {
            finish() // 현재 액티비티를 종료하여 이전 페이지로 돌아감
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

        likePostService = retrofit.create(LikePostService::class.java)
        adapter = LikePostsAdapter(mutableListOf(), likePostService)

        // RecyclerView 설정
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        recyclerView.adapter = adapter

        // 관심목록 요청
        fetchLikePosts()
    }

    // SharedPreferences에서 토큰을 가져옴
    private fun getToken(): String? {
        val sharedPreferences = getSharedPreferences("CherrySumerprefs", MODE_PRIVATE)
        return sharedPreferences.getString("token", null)
    }

    // 관심목록 데이터 요청
    private fun fetchLikePosts() {
        likePostService.getLikePosts().enqueue(object : Callback<LikePostResponse> {
            override fun onResponse(
                call: Call<LikePostResponse>,
                response: Response<LikePostResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val posts = response.body()?.data
                    // 서버로부터 받은 데이터 로그 출력
                    Log.d("LikeListActivity", "서버로부터 받은 데이터: ${response.body()}")

                    if (posts.isNullOrEmpty()) {
                        showNoPostsMessage() // 데이터가 없을 때 empty_view 표시
                        adapter.updatePosts(emptyList())  // 어댑터에 빈 목록 전달
                    } else {
                        hideNoPostsMessage() // 데이터가 있을 때 empty_view 숨기기
                        adapter.updatePosts(posts)
                    }
                } else {
                    Toast.makeText(this@LikeListActivity, "요청이 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LikePostResponse>, t: Throwable) {
                Toast.makeText(this@LikeListActivity, "서버와의 연결에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 찜한 게시글이 없을 때 empty_view 표시
    private fun showNoPostsMessage() {
        findViewById<View>(R.id.empty_view).visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    // 찜한 게시글이 있을 때 empty_view 숨기기
    private fun hideNoPostsMessage() {
        findViewById<View>(R.id.empty_view).visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }


}



data class LikePostResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val data: List<LikePosts>
)

data class LikePosts(
    val postId: Long,
    val title: String,
    val imageUrl: String,
    val productname: String,
    val region: String,
    val upload: String,
    val price: Int,
    var likes: Int,
    var like_status: Boolean,
    val closed: Boolean
)

data class UserLikeResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val data: UserLikeData
)

data class UserLikeData(
    val postId: Long,
    val like_status: Boolean,
    val likes: Int
)

data class LikePostDetailResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val data: LikePostDetailData
)

data class LikePostDetailData(
    val postId: Long,
    val writer: String,
    val imagefiles: List<String>,
    val detail_category: List<String>,
    val category: List<String>,
    val title: String,
    val productname: String,
    val upload: String,
    val price: Int,
    val date: String,
    val capacity: Int,
    val place: String,
    val likes: Int,
    val like_status: Boolean,
    val closed: Boolean,
    val author: Boolean,
    val join: Boolean,
    val content: String
)

interface LikePostService {
    @GET("mypage/posts/likes")
    fun getLikePosts(): Call<LikePostResponse>

    // 게시글 좋아요 상태 토글
    @PUT("posts/{postId}/likes")
    fun toggleLike(@Path("postId") postId: Long): Call<UserLikeResponse>

    // 게시글 상세 정보 가져오기
    @GET("posts/{postId}")
    fun getPostDetail(@Path("postId") postId: Long): Call<LikePostDetailResponse>
}