package com.cookandroid.cherrysumer.mypage

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
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cookandroid.cherrysumer.MainActivity
import com.cookandroid.cherrysumer.PostDetailFragment
import com.cookandroid.cherrysumer.R
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LikePostsAdapter(private val posts: MutableList<LikePosts>, private val likePostService: LikePostService) : RecyclerView.Adapter<LikePostsAdapter.LikePostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikePostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_item, parent, false)
        return LikePostViewHolder(view, likePostService)
    }

    override fun onBindViewHolder(holder: LikePostViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post)
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    // ViewHolder 클래스
    class LikePostViewHolder(itemView: View, private val likePostService: LikePostService) : RecyclerView.ViewHolder(itemView) {
        private val postId: TextView = itemView.findViewById(R.id.post_id)
        private val title: TextView = itemView.findViewById(R.id.title)
        private val region: TextView = itemView.findViewById(R.id.region)
        private val dot: TextView = itemView.findViewById(R.id.dot)
        private val price: TextView = itemView.findViewById(R.id.price)
        private val likeStatus: ImageView = itemView.findViewById(R.id.like_status)
        private val likesCount: TextView = itemView.findViewById(R.id.likes_count)
        private val closed: TextView = itemView.findViewById(R.id.closed)
        private val postImage: ImageView = itemView.findViewById(R.id.post_image)

        fun bind(post: LikePosts) {
            postId.text = post.postId.toString()
            title.text = post.title
            region.text = post.region
            price.text = "${post.price}원"
            likeStatus.setImageResource(R.drawable.like)
            likesCount.visibility = View.GONE
            dot.visibility = View.GONE
            closed.visibility = if (post.closed) View.VISIBLE else View.GONE

            val baseUrl = "http://3.39.110.119"
            val postImageUrl = baseUrl + post.imageUrl

            Log.d("hi", postImageUrl)

            // Glide로 이미지 로드
            Glide.with(itemView.context)
                .load(postImageUrl) // 이미지 URL
                .placeholder(R.drawable.default_post_image1) // 로드 중일 때 보여줄 기본 이미지
                .error(R.drawable.default_post_image1) // 에러 시 보여줄 이미지
                .into(postImage) // ImageView에 로드

            // 좋아요 상태 토글
            likeStatus.setOnClickListener {
                val newLikeStatus = post.like_status
                post.like_status = newLikeStatus
                likeStatus.setImageResource(if (newLikeStatus) R.drawable.like else R.drawable.no_like)
                toggleLike(post, likeStatus, newLikeStatus)
            }

            // 게시글 클릭 시 상세 정보로 이동
            itemView.setOnClickListener {
                fetchPostDetail(post.postId)
            }
        }

        private fun toggleLike(post: LikePosts, heartStatus: ImageView, newLikeStatus: Boolean) {
            val postIdLong = post.postId.toLong()

            likePostService.toggleLike(postIdLong).enqueue(object : Callback<UserLikeResponse> {
                override fun onResponse(call: Call<UserLikeResponse>, response: Response<UserLikeResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        Log.d("HomeActivity", "좋아요 상태 변경 성공: ${response.body()}")
                        post.like_status = newLikeStatus
                    } else {
                        Log.e("HomeActivity", "좋아요 상태 변경 오류 - 코드: ${response.code()}, 메시지: ${response.message()}")
                        heartStatus.setImageResource(if (newLikeStatus) R.drawable.like else R.drawable.no_like)
                        post.like_status = !newLikeStatus
                    }
                }

                override fun onFailure(call: Call<UserLikeResponse>, t: Throwable) {
                    Log.e("HomeActivity", "네트워크 오류: ${t.message}")
                    heartStatus.setImageResource(if (newLikeStatus) R.drawable.like else R.drawable.no_like)
                    post.like_status = !newLikeStatus
                }
            })
        }

        private fun fetchPostDetail(postId: Long) {
            likePostService.getPostDetail(postId).enqueue(object : Callback<LikePostDetailResponse> {
                override fun onResponse(call: Call<LikePostDetailResponse>, response: Response<LikePostDetailResponse>) {
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

                override fun onFailure(call: Call<LikePostDetailResponse>, t: Throwable) {
                    Toast.makeText(itemView.context, "서버와의 연결에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            })
        }

        private fun navigateToPostDetail(postDetailData: LikePostDetailData) {
            // 이전 액티비티 정보를 Intent에 저장 (명시적으로 액티비티 이름을 입력)
            val intent = Intent(itemView.context, MainActivity::class.java).apply {
                putExtra("previousActivity", "com.cookandroid.cherrysumer.mypage.LikeListActivity") // 명시적으로 액티비티 이름 입력
                // MainActivity를 스택에 넣지 않도록 플래그를 설정하지 않음
            }

            // LikePostDetailData를 JSON으로 변환
            val gson = Gson()
            val jsonPostDetailData = gson.toJson(postDetailData)
            intent.putExtra("postDetailsJson", jsonPostDetailData)

            // MainActivity로 이동
            itemView.context.startActivity(intent)
        }
    }

    // 게시글 목록 전체 갱신
    fun updatePosts(newPosts: List<LikePosts>) {
        posts.clear()
        posts.addAll(newPosts)
        notifyDataSetChanged()  // 전체 항목을 갱신
    }
}