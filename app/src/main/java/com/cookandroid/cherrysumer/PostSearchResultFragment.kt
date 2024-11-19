package com.cookandroid.cherrysumer

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

class PostSearchResultFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SearchAdapter
    private lateinit var searchPostService: SearchPostService
    private lateinit var posts: MutableList<SearchPost>
    private lateinit var arrButtonText: TextView
    private lateinit var arrButton: LinearLayout
    private lateinit var categoryButton: LinearLayout
    private lateinit var categoryButtonText: TextView
    private lateinit var previousButton: ImageButton
    private lateinit var searchInput: EditText

    private var selectedArrFilter: String? = null // 정렬 필터 상태 저장
    private var selectedCategoryFilter: String? = null // 카테고리 필터 상태 저장

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        posts = mutableListOf()

        val searchQuery = arguments?.getString("searchQuery") ?: ""

        searchInput = view.findViewById(R.id.search_input)
        searchInput.setText(searchQuery)

        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val searchQuerys = searchInput.text.toString().trim()

                if (searchQuerys.isNotEmpty()) {
                    fetchSearchPostData()
                    hideKeyboard(searchInput) // 키보드 숨기기
                } else {
                    Toast.makeText(requireContext(), "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }

        searchInput.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableRight = searchInput.compoundDrawables[2] // 오른쪽 drawable
                if (drawableRight != null) {
                    val drawableWidth = drawableRight.bounds.width()
                    val touchX = event.rawX
                    val editTextRight = searchInput.right

                    // 클릭 위치가 drawable의 영역 안인지 확인
                    if (touchX >= (editTextRight - drawableWidth - searchInput.paddingEnd)) {
                        // 검색 버튼 클릭 처리
                        val searchQuery = searchInput.text.toString().trim()
                        if (searchQuery.isNotEmpty()) {
                            fetchSearchPostData()
                            hideKeyboard(searchInput) // 키보드 숨기기
                        } else {
                            Toast.makeText(requireContext(), "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show()
                        }

                        // performClick 호출 추가
                        searchInput.performClick()
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }

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
        searchPostService = retrofit.create(SearchPostService::class.java)
        adapter = SearchAdapter(posts, searchPostService)

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // arrButton 초기화 및 클릭 리스너 설정
        arrButton = view.findViewById(R.id.arr_button)
        arrButtonText = view.findViewById(R.id.arr_button_text)

        arrButton.setOnClickListener {
            val arrFilterDialog = ArrayFilter({ selectedFilter ->
                updateArrText(selectedFilter)
                selectedArrFilter = selectedFilter // 선택한 필터 상태 저장
            }, selectedArrFilter ?: "최신순") // 기본 필터 설정
            arrFilterDialog.show(childFragmentManager, "arrFilterDialog")
        }


        // categoryButton 초기화 및 클릭 리스너 설정
        categoryButton = view.findViewById(R.id.category_button)
        categoryButtonText = view.findViewById(R.id.category_button_text) // 초기화 위치 변경

        categoryButton.setOnClickListener {
            val categoryDialog = CategoryFilter({ selectedFilter ->
                updateCategoryText(selectedFilter)
                selectedCategoryFilter = selectedFilter // 선택한 필터 상태 저장
            }, selectedCategoryFilter ?: "전체") // 기본 필터 설정
            categoryDialog.show(childFragmentManager, "categoryDialog")
        }

        // 버튼 클릭 시 이전 프래그먼트로 이동
        previousButton = view.findViewById(R.id.previous_button)
        previousButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 프래그먼트가 보여질 때 서버에 요청을 보내서 데이터를 가져옴
        fetchSearchPostData()
    }

    // SharedPreferences에서 토큰을 가져옴
    private fun getToken(): String? {
        val sharedPreferences = requireActivity().getSharedPreferences("CherrySumerprefs",
            Context.MODE_PRIVATE
        )
        return sharedPreferences.getString("token", null)
    }

    // arr 필터 선택 시 텍스트 업데이트
    private fun updateArrText(selectedFilter: String) {
        arrButtonText.text = selectedFilter
        selectedArrFilter = selectedFilter // 선택한 필터 상태 저장
        // 필터 변경 시 게시물 다시 조회
        fetchSearchPostData()
    }

    // category 필터 선택 시 텍스트 업데이트
    private fun updateCategoryText(selectedFilter: String) {
        categoryButtonText.text = selectedFilter
        selectedCategoryFilter = selectedFilter // 선택한 필터 상태 저장
        // 필터 변경 시 게시물 다시 조회
        fetchSearchPostData()
    }

    private fun hideKeyboard(view: View) {
        val imm = ContextCompat.getSystemService(view.context, InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    // 서버에서 데이터를 가져오는 메서드
    private fun fetchSearchPostData() {

        val query = searchInput.text.toString() // 검색어
        val category = categoryButtonText.text.toString() // 카테고리 텍스트
        val filter = arrButtonText.text.toString() // 정렬 필터 텍스트

        searchPostService.searchPosts(query = query, category = category, filter = filter)
            .enqueue(object : Callback<SearchPostResponse> {
                override fun onResponse(call: Call<SearchPostResponse>, response: Response<SearchPostResponse>) {
                    if (response.isSuccessful && response.body()?.isSuccess == true) {
                        val posts = response.body()?.data
                        Log.d("LikeStatus", "결과입니다: ${posts}") // 로그 출력

                        if (posts.isNullOrEmpty()) {
                            view?.let { showNoPostsMessage(it) }  // 데이터가 없을 때 empty_view 표시
                            adapter.updatePosts(emptyList()) // 어댑터에 빈 목록 전달
                        } else {
                            view?.let { hideNoPostsMessage(it) }  // 데이터가 있을 때 empty_view 숨기기
                            adapter.updatePosts(posts) // 검색 결과 업데이트
                        }
                    } else {
                        Toast.makeText(requireContext(), "오류: ${response.body()?.message ?: "응답 실패"}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<SearchPostResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "서버와의 연결에 실패했습니다.", Toast.LENGTH_SHORT).show()
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

data class SearchPostResponse(
    val isSuccess: Boolean, // 요청 성공 여부
    val code: String, // 상태 코드
    val message: String, // 응답 메시지
    val data: List<SearchPost> // 검색 결과 목록
)

data class SearchPost(
    val postId: Long, // 게시물 ID
    val imageUrl: String?, // 이미지 URL (nullable)
    val title: String, // 게시물 제목
    val productname: String, // 상품명
    val region: String, // 동네
    val upload: String, // 업로드 시간
    val price: Int, // 1인당 가격
    var likes: Int, // 좋아요 수
    var like_status: Boolean, // 사용자 좋아요 여부
    val closed: Boolean // 모집 마감 여부
)

data class SearchPostDetailResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val data: SearchPostDetailData
)

data class SearchPostDetailData(
    val postId: Long,
    val writer: String,
    val imagefiles: List<String>?,
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

data class SearchLikeResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val data: SearchLikeData
)

data class SearchLikeData(
    val postId: Long,
    val like_status: Boolean,
    val likes: Int
)

interface SearchPostService {
    @GET("/posts/search")
    fun searchPosts(
        @Query("q") query: String, // 검색 내용
        @Query("category") category: String, // 카테고리
        @Query("filter") filter: String // 필터
    ): Call<SearchPostResponse>

    @GET("posts/{postId}")
    fun getSearchPostDetail(@Path("postId") postId: Long): Call<SearchPostDetailResponse>

    // 게시글 좋아요 상태 토글
    @PUT("posts/{postId}/likes")
    fun getToggleLike(@Path("postId") postId: Long): Call<SearchLikeResponse>
}