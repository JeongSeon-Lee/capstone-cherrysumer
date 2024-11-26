package com.cookandroid.cherrysumer

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.flexbox.FlexboxLayout
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Query
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.http.HTTP

class PostSearchFragment : Fragment() {
    private lateinit var searchService: SearchService
    private lateinit var wordContainer: FlexboxLayout
    private lateinit var searchButton: ImageButton
    private lateinit var searchInput: EditText
    private lateinit var previousButton: ImageButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_post_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        wordContainer = view.findViewById(R.id.word_horizontal_container)
        searchButton = view.findViewById(R.id.search_button)
        searchInput = view.findViewById(R.id.category_input)
        previousButton = view.findViewById(R.id.previous_button)

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

        searchService = retrofit.create(SearchService::class.java)

        // 최근 검색어 가져오기
        loadRecentSearches()

        // 버튼 클릭 시 이전 프래그먼트로 이동
        previousButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 검색 버튼 클릭 처리
        searchButton.setOnClickListener {
            val searchText = searchInput.text.toString().trim()
            if (searchText.isEmpty()) {
                Toast.makeText(context, "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                // 검색어를 Bundle에 담아 PostSearchResultFragment로 전달
                val bundle = Bundle().apply {
                    putString("searchQuery", searchText) // 검색어 전달
                }

                val postSearchResultFragment = PostSearchResultFragment().apply {
                    arguments = bundle // 전달된 Bundle 설정
                }

                // 프래그먼트 전환
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, postSearchResultFragment) // `fragment_container`는 메인 액티비티의 프래그먼트 컨테이너 ID
                    .addToBackStack(null) // 뒤로 가기 버튼 지원
                    .commit()
            }
        }
    }

    private fun loadRecentSearches() {
        searchService.getRecentSearch().enqueue(object : Callback<RecentSearchResponse> {
            override fun onResponse(call: Call<RecentSearchResponse>, response: Response<RecentSearchResponse>) {
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val recentItems = response.body()?.data ?: return
                    displayRecentSearches(recentItems)
                } else {
                    Toast.makeText(context, "최근 검색어를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RecentSearchResponse>, t: Throwable) {
                Toast.makeText(context, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayRecentSearches(items: List<RecentSearchItem>) {
        wordContainer.removeAllViews() // 기존 항목 초기화

        // state가 false인 항목만 필터링
        val filteredItems = items.filter { !it.state }

        filteredItems.forEach { item ->
            // 최근 검색어 항목 레이아웃 inflate
            val view = LayoutInflater.from(context).inflate(R.layout.recent_search_items, wordContainer, false)

            // TextView와 ImageButton 참조
            val textView = view.findViewById<TextView>(R.id.add_recent_word)
            val deleteButton = view.findViewById<ImageButton>(R.id.delete_button)

            // TextView에 검색어 설정
            textView.text = item.name

            // TextView 클릭 시 검색 기능 수행 및 PostSearchResultFragment로 이동
            textView.setOnClickListener {
                val searchQuery = item.name // 클릭된 텍스트 값 가져오기
                val bundle = Bundle().apply {
                    putString("searchQuery", searchQuery) // 검색어 전달
                }

                val postSearchResultFragment = PostSearchResultFragment().apply {
                    arguments = bundle // 전달된 Bundle 설정
                }

                // 프래그먼트 전환
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, postSearchResultFragment) // `fragment_container`는 메인 액티비티의 프래그먼트 컨테이너 ID
                    .addToBackStack(null) // 뒤로 가기 지원
                    .commit()
            }

            // ImageButton 클릭 시 삭제 기능 수행
            deleteButton.setOnClickListener {
                deleteSearch(item) // 서버에서 삭제
                wordContainer.removeView(view) // UI에서 제거
            }

            // 동적으로 View 추가
            wordContainer.addView(view)
        }
    }

    private fun deleteSearch(item: RecentSearchItem) {

        // 요청 본문을 구성하는 JsonObject 생성
        val jsonObject = JsonObject().apply {
            addProperty("name", item.name)
            addProperty("createdAt", item.createdAt)
            addProperty("state", false)
        }



        // Retrofit을 사용하여 DELETE 요청 보내기
        searchService.deleteSearch(jsonObject).enqueue(object : Callback<DeleteResponse<Unit>> {
            override fun onResponse(call: Call<DeleteResponse<Unit>>, response: Response<DeleteResponse<Unit>>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "삭제되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d("deleteSearch", "Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(context, "삭제 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DeleteResponse<Unit>>, t: Throwable) {
                Log.e("deleteSearch", "Request failed: ${t.message}", t)
                Toast.makeText(context, "네트워크 오류로 삭제할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

}

data class RecentSearchResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val data: List<RecentSearchItem>?
)

data class RecentSearchItem(
    val name: String,
    val createdAt: String,
    val state: Boolean
)

data class DeleteResponse<T>(
    val isSuccess: Boolean,
    val code: String,
    val message: String
)

interface SearchService {

    @GET("api/search-log/recent")
    fun getRecentSearch(): Call<RecentSearchResponse>


    @HTTP(method = "DELETE", path = "api/search-log/delete", hasBody = true)
    fun deleteSearch(@Body request: JsonObject): Call<DeleteResponse<Unit>>
}