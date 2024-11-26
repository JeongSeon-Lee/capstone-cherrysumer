package com.cookandroid.cherrysumer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import com.google.gson.Gson
import android.os.Parcelable
import android.widget.Button
import android.widget.ImageButton
import com.bumptech.glide.Glide
import com.cookandroid.cherrysumer.mypage.MyRegionSettingActivity
import com.cookandroid.cherrysumer.mypage.StatusActivity
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

class HomeFragment : Fragment() {

    private lateinit var spinner: Spinner
    private lateinit var adapter: CustomSpinnerAdapter
    private lateinit var userNameTextView: TextView
    private lateinit var regionPostTitle: TextView
    private lateinit var arrButtonText: TextView
    private lateinit var arrButton: LinearLayout
    private lateinit var categoryButton: LinearLayout
    private lateinit var categoryButtonText: TextView
    private lateinit var postContainer: LinearLayout
    private lateinit var postService: PostService
    private lateinit var searchButton: ImageButton
    private lateinit var option1Container: LinearLayout
    private lateinit var option2Container: LinearLayout

    private var selectedArrFilter: String? = null // 정렬 필터 상태 저장
    private var selectedCategoryFilter: String? = null // 카테고리 필터 상태 저장


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.showBottomNavigationView()

        // 사용자 이름 표시
        userNameTextView = view.findViewById(R.id.user_name)
        val name = arguments?.getString("name") ?: "사용자"
        userNameTextView.text = "$name\u200B님!"

        // Spinner 초기화
        spinner = view.findViewById(R.id.user_spot_select)


        // 이전 페이지에서 전달받은 지역명(region)을 가져와서 사용
        val region = arguments?.getString("region") ?: "지역 없음"
        val items = listOf(region, "내 동네 설정")

        adapter = CustomSpinnerAdapter(requireContext(), items)
        spinner.adapter = adapter

        // 지역명 게시글로 설정
        regionPostTitle = view.findViewById(R.id.region_post_title)
        regionPostTitle.text = "$region 게시글"


        option1Container = view.findViewById(R.id.option1_container)
        option1Container.setOnClickListener {
            val inventoryCheckFragment = InventoryCheckFragment()

            // 프래그먼트를 교체
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, inventoryCheckFragment) // fragment_container는 호스트 액티비티의 FrameLayout ID
                .addToBackStack(null) // 뒤로가기 시 이전 프래그먼트로 돌아가기
                .commit()
        }

        option2Container = view.findViewById(R.id.option2_container)
        option2Container.setOnClickListener {
            val intent = Intent(activity, StatusActivity::class.java)
            startActivity(intent)
        }

        searchButton = view.findViewById(R.id.search_button)

        // 버튼 클릭 시 PostSearchFragment로 이동
        searchButton.setOnClickListener {
            val postSearchFragment = PostSearchFragment()

            // 프래그먼트를 교체
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, postSearchFragment) // fragment_container는 호스트 액티비티의 FrameLayout ID
                .addToBackStack(null) // 뒤로가기 시 이전 프래그먼트로 돌아가기
                .commit()
        }

        // Spinner 아이템 선택 리스너 설정
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // 선택된 아이템에 따른 처리
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 아무것도 선택되지 않은 경우
                // 별도의 처리 없음
            }
        }

        // arrButton 초기화 및 클릭 리스너 설정
        arrButton = view.findViewById(R.id.arr_button)
        arrButtonText = view.findViewById(R.id.arr_button_text) ?: return

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

        // 게시물 컨테이너 초기화
        postContainer = view.findViewById(R.id.post_container)

        // SharedPreferences에서 저장된 토큰을 가져옴
        val sharedPreferences = requireActivity().getSharedPreferences("CherrySumerprefs", AppCompatActivity.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)

        // Retrofit 초기화
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(requireContext(), token)) // Context와 token을 전달
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

        postService = retrofit.create(PostService::class.java)

        // 게시물 요청
        fetchPosts()
    }



    // arr 필터 선택 시 텍스트 업데이트
    private fun updateArrText(selectedFilter: String) {
        arrButtonText.text = selectedFilter
        selectedArrFilter = selectedFilter // 선택한 필터 상태 저장
        // 필터 변경 시 게시물 다시 조회
        fetchPosts()
    }

    // category 필터 선택 시 텍스트 업데이트
    private fun updateCategoryText(selectedFilter: String) {
        categoryButtonText.text = selectedFilter
        selectedCategoryFilter = selectedFilter // 선택한 필터 상태 저장
        // 필터 변경 시 게시물 다시 조회
        fetchPosts()
    }

    // 게시물 가져오기
    private fun fetchPosts() {
        val category = selectedCategoryFilter ?: "전체"
        val filter = selectedArrFilter ?: "최신순"

        // 로그 출력: 게시물 요청 시작
        Log.d("HomeActivity", "게시물 요청 - 카테고리: $category, 정렬: $filter")

        postService.getPosts(category, filter).enqueue(object : Callback<PostResponse> {
            override fun onResponse(call: Call<PostResponse>, response: Response<PostResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val postResponse = response.body()!!
                    Log.d("HomeActivity", "서버 응답 성공: $postResponse")

                    // 수정된 부분: data.posts를 사용
                    if (postResponse.isSuccess && postResponse.data.posts.isNotEmpty()) {
                        val postLoadingData = postResponse.data
                        val region = postLoadingData.region
                        val name = postLoadingData.name

                        // 사용자 이름과 지역명 설정
                        userNameTextView.text = "$name\u200B님!"
                        regionPostTitle.text = "$region 게시글"

                        // Spinner 데이터 설정 및 초기화
                        val items = listOf(region, "내 동네 설정")
                        val adapter = CustomSpinnerAdapter(requireContext(), items)
                        spinner.adapter = adapter

                        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                when (position) {
                                    1 -> { // "내 동네 설정" 클릭
                                        val intent = Intent(activity, MyRegionSettingActivity::class.java)
                                        intent.putExtra("currentRegion", region)
                                        startActivity(intent)

                                        // Spinner 상태를 원래 region 값으로 유지
                                        spinner.setSelection(0)
                                    }
                                    else -> {
                                        Log.d("HomeActivity", "현재 선택된 지역: $region")
                                    }
                                }
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {
                                // 아무 것도 선택되지 않았을 경우 처리
                            }
                        }

                        displayPosts(postResponse.data.posts) // data.posts로 게시글 목록을 가져옵니다.
                    } else {
                        postContainer.removeAllViews() // 이전 뷰 제거
                        // 게시물이 없을 경우 처리
                        Log.d("HomeActivity", "게시물이 없습니다.")
                    }
                } else {
                    // 서버 응답이 성공적이지 않음 (예: 상태 코드 404, 500 등)
                    Log.e("HomeActivity", "서버 응답 오류 - 코드: ${response.code()}, 메시지: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<PostResponse>, t: Throwable) {
                // 네트워크 오류 등 실패 처리
                Log.e("HomeActivity", "API 요청 실패: ${t.message}")
            }
        })
    }

    private fun displayPosts(posts: List<Post>) {
        // 프래그먼트가 연결되지 않은 경우 중단
        if (!isAdded) return

        postContainer.removeAllViews() // 이전 게시물 뷰 제거

        // 키워드 데이터 요청 후 UI 업데이트
        fetchKeywordData { keywords ->
            // 게시물 목록을 순회하며 뷰 추가
            for ((index, post) in posts.withIndex()) {
                // 다섯 번째 게시물 위치에 키워드 추천 XML 추가 (키워드가 있는 경우만)
                if (index == 4 && keywords.isNotEmpty()) {
                    // user_like_category_question.xml을 인플레이트해서 categoryBoxView 생성
                    val categoryBoxView = layoutInflater.inflate(R.layout.user_like_category_question, null)
                    addKeywordRecommendation(categoryBoxView, keywords) // 키워드 추천을 추가
                    postContainer.addView(categoryBoxView) // categoryBoxView를 postContainer에 추가
                }
                addPostItem(post) // 게시물 추가
            }
        }
    }

    // 키워드 추천 뷰를 추가하는 함수
    private fun addKeywordRecommendation(categoryBoxView: View, keywords: List<String>) {
        // categoryBoxView에서 category_box를 찾습니다
        val categoryBox: LinearLayout = categoryBoxView.findViewById(R.id.category_box)

        // 기존의 아이템들을 초기화 (새로운 데이터를 추가하기 전에 기존 항목을 지움)
        categoryBox.removeAllViews()

        // 서버로부터 받은 각 키워드에 대해 동적으로 TextView를 추가
        for (keyword in keywords) {
            // category_box 안에 들어갈 새로운 TextView 생성
            val keywordView = LayoutInflater.from(requireContext()).inflate(R.layout.inventory_check_itmes, categoryBox, false)

            // TextView를 찾고 키워드를 설정
            val keywordTextView: TextView = keywordView.findViewById(R.id.add_recent_word)
            keywordTextView.text = keyword

            // 키워드 뷰에 클릭 리스너 추가
            keywordTextView.setOnClickListener {
                // 클릭된 키워드를 검색 쿼리로 설정
                val searchQuery = keyword
                val bundle = Bundle().apply {
                    putString("searchQuery", searchQuery) // 검색어 전달
                }

                // 검색 결과 화면으로 이동
                val postSearchResultFragment = PostSearchResultFragment().apply {
                    arguments = bundle // 전달된 Bundle 설정
                }

                // 프래그먼트 전환
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, postSearchResultFragment) // `fragment_container`는 메인 액티비티의 프래그먼트 컨테이너 ID
                    .addToBackStack(null) // 뒤로 가기 지원
                    .commit()
            }

            // 키워드 뷰를 category_box에 추가
            categoryBox.addView(keywordView)
        }
    }

    // 키워드 데이터 가져오기 함수
    private fun fetchKeywordData(callback: (List<String>) -> Unit) {
        // 예시 API 요청 (PostService에 fetchKeywords 메서드 추가 필요)
        postService.fetchKeywords().enqueue(object : Callback<KeywordResponse> {
            override fun onResponse(call: Call<KeywordResponse>, response: Response<KeywordResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val keywords = response.body()!!.data ?: emptyList() // null일 경우 빈 리스트 반환
                    callback(keywords) // 키워드 데이터 반환
                } else {
                    Log.e("HomeFragment", "키워드 요청 실패: ${response.message()}")
                    callback(emptyList()) // 빈 데이터 반환
                }
            }

            override fun onFailure(call: Call<KeywordResponse>, t: Throwable) {
                Log.e("HomeFragment", "키워드 요청 에러: ${t.message}")
                callback(emptyList()) // 빈 데이터 반환
            }
        })
    }


    private fun addPostItem(post: Post) {
        // 프래그먼트가 연결되지 않은 경우 중단
        if (!isAdded) return

        // 리스트 항목 레이아웃 인플레이트
        val postView = LayoutInflater.from(requireContext()).inflate(R.layout.post_item, postContainer, false)

        // 인플레이트한 뷰의 구성 요소에 데이터 설정
        //val postImage: ImageView = postView.findViewById(R.id.post_image)
        val postId: TextView = postView.findViewById(R.id.post_id)
        val title: TextView = postView.findViewById(R.id.title)
        val postImage: ImageView = postView.findViewById(R.id.post_image)
        val region: TextView = postView.findViewById(R.id.region)
        val uploadTime: TextView = postView.findViewById(R.id.upload_time)
        val price: TextView = postView.findViewById(R.id.price)
        val likesCount: TextView = postView.findViewById(R.id.likes_count)
        val likeStatus: ImageView = postView.findViewById(R.id.like_status)
        val closed: TextView = postView.findViewById(R.id.closed)

        // 서버로부터 받아온 데이터를 이용해 UI 업데이트
        postId.text = post.postId.toString()
        title.text = post.title
        region.text = post.region
        uploadTime.text = post.upload
        price.text = "${post.price}원"
        likesCount.text = post.likes.toString()
        likeStatus.setImageResource(if (post.like_status) R.drawable.like else R.drawable.no_like)
        closed.visibility = if (post.closed) View.VISIBLE else View.GONE

        if (post.imageUrl != null) {
            val imageUrl = "http://3.39.110.119${post.imageUrl}"

            Glide.with(requireContext())
                .load(imageUrl)
                .error(R.drawable.default_post_image1)  // 기본 이미지 설정
                .into(postImage)
        } else {
            // 이미지 URL이 없으면 기본 이미지 설정
            postImage.setImageResource(R.drawable.default_post_image1)
        }

        // 클릭 리스너 추가
        likeStatus.setOnClickListener {
            // 좋아요 상태를 반전
            val newLikeStatus = !post.like_status

            // 새로운 좋아요 수 계산
            val newLikesCount = if (newLikeStatus) post.likes + 1 else post.likes - 1

            // UI를 즉시 업데이트 (로컬 데이터 변경)
            post.like_status = newLikeStatus // 데이터 소스도 업데이트
            likeStatus.setImageResource(if (newLikeStatus) R.drawable.like else R.drawable.no_like)

            // 좋아요 수 업데이트 (즉시 반영)
            post.likes = newLikesCount
            likesCount.text = newLikesCount.toString() // UI 업데이트

            // 서버에 좋아요 상태를 변경 요청
            toggleLike(post, likeStatus, newLikeStatus, likesCount, newLikesCount)
        }

        postView.setOnClickListener {
            fetchPostDetail(post.postId) // postId를 사용해 서버 요청
        }


        // 포스트 뷰를 post_container에 추가
        postContainer.addView(postView)
    }

    // 찜 상태 토글 메서드
    private fun toggleLike(post: Post, heartStatus: ImageView, newLikeStatus: Boolean, likesCount: TextView, newLikesCount: Int) {
        // postId를 Long으로 변환
        val postIdLong = post.postId.toLong()

        // 서버 호출
        postService.toggleLike(postIdLong).enqueue(object : Callback<LikeResponse> {
            override fun onResponse(call: Call<LikeResponse>, response: Response<LikeResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    // 성공적으로 좋아요 상태 변경
                    Log.d("HomeActivity", "좋아요 상태 변경 성공: ${response.body()}")
                    post.like_status = newLikeStatus // 상태 업데이트
                } else {
                    // 서버 응답 오류
                    Log.e("HomeActivity", "좋아요 상태 변경 오류 - 코드: ${response.code()}, 메시지: ${response.message()}")
                    // 실패 시 이전 상태로 복구
                    heartStatus.setImageResource(if (newLikeStatus) R.drawable.like else R.drawable.no_like)
                    post.likes = if (newLikeStatus) post.likes - 1 else post.likes + 1 // 원래 상태로 복구
                    likesCount.text = post.likes.toString() // UI 업데이트
                    post.like_status = !newLikeStatus // 원래 상태로 복구
                }
            }

            override fun onFailure(call: Call<LikeResponse>, t: Throwable) {
                // 네트워크 오류 발생
                Log.e("HomeActivity", "네트워크 오류: ${t.message}")
                // 실패 시 이전 상태로 복구
                heartStatus.setImageResource(if (newLikeStatus) R.drawable.like else R.drawable.no_like)
                post.likes = if (newLikeStatus) post.likes - 1 else post.likes + 1 // 원래 상태로 복구
                likesCount.text = post.likes.toString() // UI 업데이트
                post.like_status = !newLikeStatus // 원래 상태로 복구
            }
        })
    }

    private fun fetchPostDetail(postId: Long) {
        postService.getPostDetail(postId).enqueue(object : Callback<PostDetailResponse> {
            override fun onResponse(call: Call<PostDetailResponse>, response: Response<PostDetailResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val postDetail = response.body()!!.data
                    Log.d("HomeFragment", "게시글 상세 정보: $postDetail")
                    // 상세 정보를 UI에 반영하거나 상세 페이지로 이동하는 처리
                    navigateToPostDetail(postDetail)
                } else {
                    Log.e("HomeFragment", "상세 정보 요청 실패 - 코드: ${response.code()}, 메시지: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<PostDetailResponse>, t: Throwable) {
                Log.e("HomeFragment", "네트워크 오류: ${t.message}")
            }
        })
    }


    private fun navigateToPostDetail(postDetailData: PostDetailData) {
        Log.d("HomeFragment", "Navigating to PostFragment with data: $postDetailData")

        val fragment = PostDetailFragment()
        val bundle = Bundle()

        // Gson을 사용하여 PostDetailData를 JSON 문자열로 변환
        val gson = Gson()
        val jsonPostDetailData = gson.toJson(postDetailData)
        bundle.putString("postDetailsJson", jsonPostDetailData) // JSON 문자열을 Bundle에 추가

        fragment.arguments = bundle // Bundle을 Fragment에 설정

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commitAllowingStateLoss()
    }

}

// Data classes for response
data class PostResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val data: PostLoadingData
)

data class PostLoadingData(
    val region: String,              // 지역 (예: 대연동)
    val name: String,                // 사용자 이름 (예: test user)
    val posts: List<Post>            // 게시글 목록
)

data class Post(
    val postId: Long,
    val title: String,
    val imageUrl: String?,
    val productname: String,
    val region: String,
    val upload: String,
    val price: Int,
    var likes: Int,
    var like_status: Boolean,
    val closed: Boolean
)

data class LikeResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val data: LikeData
)

data class LikeData(
    val postId: Long,
    val like_status: Boolean,
    val likes: Int
)

data class PostDetailResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val data: PostDetailData
)



data class PostDetailData(
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

data class KeywordResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val data: List<String>? // 추천 키워드 리스트
)

interface PostService {
    // 게시글 목록 가져오기
    @GET("posts")
    fun getPosts(@Query("category") category: String, @Query("filter") filter: String): Call<PostResponse>

    // 게시글 좋아요 상태 토글
    @PUT("posts/{postId}/likes")
    fun toggleLike(@Path("postId") postId: Long): Call<LikeResponse>

    // 게시글 상세 정보 가져오기
    @GET("posts/{postId}")
    fun getPostDetail(@Path("postId") postId: Long): Call<PostDetailResponse>

    @GET("posts/recommend")
    fun fetchKeywords(): Call<KeywordResponse>
}