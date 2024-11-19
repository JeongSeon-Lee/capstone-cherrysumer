package com.cookandroid.cherrysumer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchAdapter(private val posts: MutableList<SearchPost>, private val searchPostService: SearchPostService) :
    RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_item, parent, false)
        return SearchViewHolder(view, searchPostService)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post)
    }

    override fun getItemCount(): Int = posts.size

    class SearchViewHolder(itemView: View, private val searchPostService: SearchPostService) : RecyclerView.ViewHolder(itemView) {
        private val postImage: ImageView = itemView.findViewById(R.id.post_image)
        private val postId: TextView = itemView.findViewById(R.id.post_id)
        private val title: TextView = itemView.findViewById(R.id.title)
        private val region: TextView = itemView.findViewById(R.id.region)
        private val uploadTime: TextView = itemView.findViewById(R.id.upload_time)
        private val price: TextView = itemView.findViewById(R.id.price)
        private val closed: TextView = itemView.findViewById(R.id.closed)
        private val likeStatus: ImageView = itemView.findViewById(R.id.like_status)
        private val likesCount: TextView = itemView.findViewById(R.id.likes_count)

        fun bind(post: SearchPost) {
            // 포스트 ID 숨기기
            postId.text = post.postId.toString()
            title.text = post.title
            region.text = post.region
            uploadTime.text = post.upload
            price.text = "${post.price}원"
            closed.visibility = if (post.closed) View.VISIBLE else View.GONE

            val baseUrl = "http://3.39.110.119"
            val postImageUrl = baseUrl + post.imageUrl

            // Glide로 이미지 로드
            Glide.with(itemView.context)
                .load(postImageUrl) // 이미지 URL
                .placeholder(R.drawable.default_post_image1) // 로드 중일 때 보여줄 기본 이미지
                .error(R.drawable.default_post_image1) // 에러 시 보여줄 이미지
                .into(postImage) // ImageView에 로드

            // 로그로 likeStatus 값 확인
            Log.d("LikeStatus", "Like status: ${post.like_status}") // 로그 출력

            // 좋아요 상태 설정
            likesCount.text = post.likes.toString()
            likeStatus.setImageResource(if (post.like_status) R.drawable.like else R.drawable.no_like)

            // 좋아요 클릭 리스너
            likeStatus.setOnClickListener {
                val newLikeStatus = !post.like_status
                val newLikesCount = if (newLikeStatus) post.likes + 1 else post.likes - 1

                post.like_status = newLikeStatus
                likesCount.text = newLikesCount.toString()
                likeStatus.setImageResource(if (newLikeStatus) R.drawable.like else R.drawable.no_like)

                toggleLike(post, newLikeStatus, newLikesCount)
            }

            // 게시글 클릭 시 상세 정보로 이동
            itemView.setOnClickListener {
                fetchPostDetail(post.postId)
            }
        }

        private fun toggleLike(post: SearchPost, newLikeStatus: Boolean, newLikesCount: Int) {
            searchPostService.getToggleLike(post.postId.toLong()).enqueue(object : Callback<SearchLikeResponse> {
                override fun onResponse(call: Call<SearchLikeResponse>, response: Response<SearchLikeResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        post.like_status = newLikeStatus
                        post.likes = newLikesCount
                    } else {
                        // 서버 오류시 UI 복구
                        post.like_status = !newLikeStatus
                        post.likes = if (newLikeStatus) post.likes - 1 else post.likes + 1
                        likesCount.text = post.likes.toString()
                        likeStatus.setImageResource(if (!newLikeStatus) R.drawable.like else R.drawable.no_like)
                    }
                }

                override fun onFailure(call: Call<SearchLikeResponse>, t: Throwable) {
                    // 네트워크 오류시 UI 복구
                    post.like_status = !newLikeStatus
                    post.likes = if (newLikeStatus) post.likes - 1 else post.likes + 1
                    likesCount.text = post.likes.toString()
                    likeStatus.setImageResource(if (!newLikeStatus) R.drawable.like else R.drawable.no_like)
                }
            })
        }

        private fun fetchPostDetail(postId: Long) {
            searchPostService.getSearchPostDetail(postId).enqueue(object : Callback<SearchPostDetailResponse> {
                override fun onResponse(call: Call<SearchPostDetailResponse>, response: Response<SearchPostDetailResponse>) {
                    if (response.isSuccessful) {
                        val postDetail = response.body()?.data
                        if (postDetail != null) {
                            navigateToPostDetail(postDetail)
                        } else {
                            Toast.makeText(itemView.context, "게시글이 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(itemView.context, "게시글이 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<SearchPostDetailResponse>, t: Throwable) {
                    Toast.makeText(itemView.context, "서버와의 연결에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            })
        }

        private fun navigateToPostDetail(postDetailData: SearchPostDetailData) {
            val gson = Gson()
            val jsonPostDetailData = gson.toJson(postDetailData)

            val postDetailFragment = PostDetailFragment()
            val bundle = Bundle().apply {
                putString("postDetailsJson", jsonPostDetailData)
            }
            postDetailFragment.arguments = bundle

            // itemView.context를 기반으로 액티비티의 FragmentManager 가져오기
            val activity = itemView.context as? AppCompatActivity
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.fragment_container, postDetailFragment) // 프래그먼트를 표시할 컨테이너 ID
                ?.addToBackStack(null) // 뒤로 가기 지원
                ?.commit()
        }
    }

    // 게시글 목록 전체 갱신
    fun updatePosts(newPosts: List<SearchPost>) {
        posts.clear()  // 기존 항목 모두 삭제
        posts.addAll(newPosts)  // 새 항목 모두 추가
        notifyDataSetChanged()  // 전체 항목을 갱신
    }
}