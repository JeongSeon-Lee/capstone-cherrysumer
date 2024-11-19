package com.cookandroid.cherrysumer.mypage

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cookandroid.cherrysumer.MainActivity
import com.cookandroid.cherrysumer.R
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ParticipationAdapter(private val posts: MutableList<ParticipationPost>, private val participationService: ParticipationService) :
    RecyclerView.Adapter<ParticipationAdapter.ParticipationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.participation_item, parent, false)
        return ParticipationViewHolder(view, participationService)
    }

    override fun onBindViewHolder(holder: ParticipationViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post)
    }

    override fun getItemCount(): Int = posts.size

    class ParticipationViewHolder(itemView: View, private val participationService: ParticipationService) : RecyclerView.ViewHolder(itemView) {
        private val postImage: ImageView = itemView.findViewById(R.id.post_image)
        private val postId: TextView = itemView.findViewById(R.id.post_id)
        private val title: TextView = itemView.findViewById(R.id.title)
        private val statusMent: TextView = itemView.findViewById(R.id.status_ment)
        private val status: TextView = itemView.findViewById(R.id.status)
        private val closed: TextView = itemView.findViewById(R.id.closed)
        private val registerButton: TextView = itemView.findViewById(R.id.register_button)

        fun bind(post: ParticipationPost) {
            // 포스트 ID 숨기기
            postId.text = post.postId.toString()

            // 제목 설정
            title.text = post.title

            val baseUrl = "http://3.39.110.119"
            val postImageUrl = baseUrl + post.imageUrl

            // Glide로 이미지 로드
            Glide.with(itemView.context)
                .load(postImageUrl) // 이미지 URL
                .placeholder(R.drawable.default_post_image1) // 로드 중일 때 보여줄 기본 이미지
                .error(R.drawable.default_post_image1) // 에러 시 보여줄 이미지
                .into(postImage) // ImageView에 로드

            // 승인 여부에 따른 텍스트 색상 설정
            when (post.participationStatus) {
                "승인" -> status.setTextColor(itemView.context.getColor(R.color.cherry))
                else -> status.setTextColor(itemView.context.getColor(R.color.grey_6F))
            }
            status.text = post.participationStatus

            // purchaseCompleted에 따라 뷰 가시성 설정
            if (post.purchaseCompleted) {
                statusMent.visibility = View.GONE
                status.visibility = View.GONE
                closed.visibility = View.VISIBLE
            } else {
                statusMent.visibility = View.VISIBLE
                status.visibility = View.VISIBLE
                closed.visibility = View.GONE
            }

            // inventoryRegistered가 true일 경우 버튼 숨기기
            if (post.inventoryRegistered) {
                registerButton.visibility = View.GONE
            } else {
                // inventoryRegistered가 false이고 purchaseCompleted가 true일 경우에만 버튼 표시
                if (post.purchaseCompleted) {
                    registerButton.visibility = View.VISIBLE
                } else {
                    registerButton.visibility = View.GONE
                }
            }

            // 게시글 클릭 시 상세 정보로 이동
            itemView.setOnClickListener {
                fetchPostDetail(post.postId)
            }
        }

        private fun fetchPostDetail(postId: Long) {
            participationService.getParticipationDetail(postId).enqueue(object :
                Callback<ParticipationDetailResponse> {
                override fun onResponse(call: Call<ParticipationDetailResponse>, response: Response<ParticipationDetailResponse>) {
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

                override fun onFailure(call: Call<ParticipationDetailResponse>, t: Throwable) {
                    Toast.makeText(itemView.context, "서버와의 연결에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            })
        }

        private fun navigateToPostDetail(postDetailData: ParticipationStatusDetailData) {
            // 이전 액티비티 정보를 Intent에 저장 (명시적으로 액티비티 이름을 입력)
            val intent = Intent(itemView.context, MainActivity::class.java).apply {
                putExtra("previousActivity", "com.cookandroid.cherrysumer.mypage.StatusActivity") // 명시적으로 액티비티 이름 입력
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
    fun updatePosts(newPosts: List<ParticipationPost>) {
        posts.clear()  // 기존 항목 모두 삭제
        posts.addAll(newPosts)  // 새 항목 모두 추가
        notifyDataSetChanged()  // 전체 항목을 갱신
    }
}