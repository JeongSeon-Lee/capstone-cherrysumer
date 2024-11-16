package com.example.cherrysumer

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cherrysumer.databinding.FragmentPostListBinding
import com.example.cherrysumer.retrofit.ApiCallback
import com.example.cherrysumer.retrofit.ApiManager
import com.example.cherrysumer.retrofit.models.ApiResponse
import com.example.cherrysumer.retrofit.models.PostItem
import retrofit2.Response

class PostListFragment : Fragment() {
    private lateinit var binding: FragmentPostListBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPostListBinding.inflate(inflater, container, false)

        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.adapter = PostAdapter(emptyList()) { }

        fetchPostItems()

        return binding.root
    }

    private fun fetchPostItems() {
        ApiManager().listPostItems(filter = "모집", object : ApiCallback<List<PostItem>> {
            override fun onSuccess(apiResponse: ApiResponse<List<PostItem>>?) {
                val postItems = if (apiResponse?.data != null && apiResponse.data.isNotEmpty()) {
                    // apiResponse와 data가 null이 아니고 비어있지 않은 경우 사용
                    apiResponse.data
                } else {
                    // apiResponse가 null이거나 data가 비어있는 경우 임시 데이터 사용
                    listOf(
                        PostItem(
                            postId = 1,
                            imageUrl = "/image/view/d217ed73-7774-4596-9d7e-d9354ee85125.jpg",
                            title = "Fresh Apples for Sale",
                            productName = "Apple",
                            purchaseDate = "2024-10-29",
                            category = "과일",
                            applicantCount = 5,
                            purchaseCompleted = false,
                            inventoryRegistered = false
                        ),
                        PostItem(
                            postId = 2,
                            imageUrl = "/image/view/a735d82b-512c-4b57-8e92-e54f3a4cdd7d.jpg",
                            title = "Organic Milk for Sale",
                            productName = "Milk",
                            purchaseDate = "2024-10-30",
                            category = "유제품",
                            applicantCount = 10,
                            purchaseCompleted = true,
                            inventoryRegistered = true
                        )
                    )
                }

                binding.recyclerView.adapter = PostAdapter(postItems) { postItem ->
                    val inventoryInsertFragment = InventoryInsertFragment().apply {
                        arguments = Bundle().apply {
                            putString("source", "registerPostItem")
                            putLong("postId", postItem.postId)
                            putString("productName", postItem.productName)
                            putString("purchaseDate", postItem.purchaseDate)
                            putString("category", postItem.category)
                        }
                    }
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_content, inventoryInsertFragment)
                        .addToBackStack(null)
                        .commit()
                }
                super.onSuccess(apiResponse)
            }

            override fun onError(response: Response<ApiResponse<List<PostItem>>>) {
                Toast.makeText(activity, "불러오기에 실패했습니다 (HTTP ${response.code()})", Toast.LENGTH_SHORT).show()
                super.onError(response)
            }

            override fun onFailure(throwable: Throwable) {
                Toast.makeText(activity, "네트워크 오류: ${throwable.message}", Toast.LENGTH_SHORT).show()
                super.onFailure(throwable)
            }
        })
    }

}
