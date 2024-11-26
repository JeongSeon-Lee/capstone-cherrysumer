package com.cookandroid.cherrysumer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.flexbox.FlexboxLayout
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP

class InventoryCheckFragment : Fragment() {
    private lateinit var previousButton: ImageButton
    private lateinit var wordContainer: FlexboxLayout
    private lateinit var inventoryCheckService: InventoryCheckService

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inventory_check, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        wordContainer = view.findViewById(R.id.word_horizontal_container)
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

        inventoryCheckService = retrofit.create(InventoryCheckService::class.java)

        // 최근 검색어 가져오기
        loadInventoryItems()

        // 버튼 클릭 시 이전 프래그먼트로 이동
        previousButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

    }

    private fun loadInventoryItems() {
        inventoryCheckService.getInventoryItems().enqueue(object : Callback<InventoryCheckResponse> {
            override fun onResponse(call: Call<InventoryCheckResponse>, response: Response<InventoryCheckResponse>) {
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val inventoryItems = response.body()?.data ?: return
                    displayRecentSearches(inventoryItems)
                } else {
                    Toast.makeText(context, "최근 검색어를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<InventoryCheckResponse>, t: Throwable) {
                Toast.makeText(context, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayRecentSearches(items: List<String>) {
        wordContainer.removeAllViews() // 기존 항목 초기화

        items.forEach { item ->
            // 최근 검색어 항목 레이아웃 inflate
            val view = LayoutInflater.from(context).inflate(R.layout.inventory_check_itmes, wordContainer, false)

            // TextView 참조
            val textView = view.findViewById<TextView>(R.id.add_recent_word)

            // TextView에 검색어 설정
            textView.text = item // 리스트의 문자열을 그대로 표시

            // TextView 클릭 시 검색 기능 수행 및 PostSearchResultFragment로 이동
            textView.setOnClickListener {
                val searchQuery = item // 클릭된 텍스트 값 가져오기
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

            // 동적으로 View 추가
            wordContainer.addView(view)
        }
    }
}

data class InventoryCheckResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val data: List<String>? // 제품 목록
)

interface InventoryCheckService {
    @GET("inventory/filtered-items")
    fun getInventoryItems(): Call<InventoryCheckResponse>
}