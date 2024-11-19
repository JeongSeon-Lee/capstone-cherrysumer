package com.cookandroid.cherrysumer.mypage

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cookandroid.cherrysumer.AuthInterceptor
import com.cookandroid.cherrysumer.R
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

class RecruitmentFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecruitmentAdapter
    private lateinit var recruitmentService: RecruitmentService
    private lateinit var posts: MutableList<RecruitmentPost>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_status_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        posts = mutableListOf()

        // Retrofit 초기화
        val token = getToken()
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(requireContext(), token))
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

        // RecyclerView 설정
        recruitmentService = retrofit.create(RecruitmentService::class.java)
        adapter = RecruitmentAdapter(posts, recruitmentService)

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // 프래그먼트가 보여질 때 서버에 요청을 보내서 데이터를 가져옴
        fetchRecruitmentData(view)
    }

    // SharedPreferences에서 토큰을 가져옴
    private fun getToken(): String? {
        val sharedPreferences = requireActivity().getSharedPreferences("CherrySumerprefs", MODE_PRIVATE)
        return sharedPreferences.getString("token", null)
    }

    // 서버에서 참여 데이터를 가져오는 메서드
    private fun fetchRecruitmentData(view: View) {
        recruitmentService.getRecruitmentPost("모집").enqueue(object : Callback<RecruitmentResponse> {
            override fun onResponse(
                call: Call<RecruitmentResponse>,
                response: Response<RecruitmentResponse>
            ) {
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val posts = response.body()?.data
                    Log.d("RecruitmentFragment", "서버로부터 받은 데이터: ${response.body()}")

                    if (posts.isNullOrEmpty()) {
                        showNoPostsMessage(view) // 데이터가 없을 때 empty_view 표시
                        adapter.updatePosts(emptyList())  // 어댑터에 빈 목록 전달
                    } else {
                        hideNoPostsMessage(view) // 데이터가 있을 때 empty_view 숨기기
                        adapter.updatePosts(posts)
                    }
                } else {
                    Toast.makeText(requireContext(), "요청이 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RecruitmentResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "서버와의 연결에 실패했습니다.", Toast.LENGTH_SHORT).show()
                Log.e("RecruitmentFragment", "서버 연결 실패", t)
            }
        })
    }

    // 내 게시글이 없을 때 empty_view 표시
    private fun showNoPostsMessage(view: View) {
        view.findViewById<View>(R.id.empty_view).visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    // 내 게시글이 있을 때 empty_view 숨기기
    private fun hideNoPostsMessage(view: View) {
        view.findViewById<View>(R.id.empty_view).visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }
}

data class RecruitmentResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val data: List<RecruitmentPost>
)

data class RecruitmentPost(
    val postId: Long,
    val imageUrl: String,
    val title: String,
    val productname: String?,
    val date: String,
    val category: String,
    val applicantCount: Int,
    val purchaseCompleted: Boolean,
    val inventoryRegistered: Boolean
)

data class RecruitmentStatusDetailResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val data: RecruitmentStatusDetailData
)

data class RecruitmentStatusDetailData(
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

interface RecruitmentService {
    @GET("mypage/applications/{filter}")
    fun getRecruitmentPost(@Path("filter") filter: String): Call<RecruitmentResponse>

    @GET("posts/{postId}")
    fun getPostStatusDetail(@Path("postId") postId: Long): Call<RecruitmentStatusDetailResponse>
}