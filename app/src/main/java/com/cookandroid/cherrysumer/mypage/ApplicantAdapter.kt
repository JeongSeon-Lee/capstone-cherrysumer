package com.cookandroid.cherrysumer.mypage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cookandroid.cherrysumer.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ApplicantAdapter(private val posts: MutableList<ApplicantList>, private val applicantListService: ApplicantListService) : RecyclerView.Adapter<ApplicantAdapter.ApplicantViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.applicant_user_item, parent, false)
        return ApplicantViewHolder(view, applicantListService)
    }

    override fun onBindViewHolder(holder: ApplicantViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post)
    }

    override fun getItemCount(): Int = posts.size

    inner class ApplicantViewHolder(itemView: View, private val applicantListService: ApplicantListService) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: ImageView = itemView.findViewById(R.id.post_image)
        private val nickname: TextView = itemView.findViewById(R.id.user_nickname)
        private val userId: TextView = itemView.findViewById(R.id.user_id)
        private val region: TextView = itemView.findViewById(R.id.user_region)
        private val statusApprove: TextView = itemView.findViewById(R.id.user_status_approve)
        private val statusRefuse: TextView = itemView.findViewById(R.id.user_status_refuse)
        private val fComplete: TextView = itemView.findViewById(R.id.select_complete)
        private val approveButton: TextView = itemView.findViewById(R.id.option_approve)
        private val refuseButton: TextView = itemView.findViewById(R.id.option_refuse)


        fun bind(post: ApplicantList) {
            nickname.text = post.nickname
            userId.text = post.userId.toString()
            region.text = post.region

            val baseUrl = "https://3.39.110.119"
            val profileImageUrl = baseUrl + post.profileImageUrl

            // Glide로 이미지 로드
            Glide.with(itemView.context)
                .load(profileImageUrl) // 이미지 URL
                .placeholder(R.drawable.default_profile_image) // 로드 중일 때 보여줄 기본 이미지
                .error(R.drawable.default_profile_image) // 에러 시 보여줄 이미지
                .into(profileImage) // ImageView에 로드


            // 승인 상태에 따른 UI 설정
            when (post.isConfirmed) {
                "승인" -> {
                    statusApprove.visibility = View.VISIBLE
                    fComplete.visibility = View.VISIBLE
                    statusRefuse.visibility = View.GONE
                    approveButton.visibility = View.GONE
                    refuseButton.visibility = View.GONE
                }
                "거절" -> {
                    statusRefuse.visibility = View.VISIBLE
                    fComplete.visibility = View.VISIBLE
                    statusApprove.visibility = View.GONE
                    approveButton.visibility = View.GONE
                    refuseButton.visibility = View.GONE
                }
                "미확인" -> {
                    approveButton.visibility = View.VISIBLE
                    refuseButton.visibility = View.VISIBLE
                    statusApprove.visibility = View.GONE
                    statusRefuse.visibility = View.GONE
                    fComplete.visibility = View.GONE
                }
            }

            approveButton.setOnClickListener {
                sendDecision(post, "승인")
            }

            refuseButton.setOnClickListener {
                sendDecision(post, "거절")
            }
        }

        private fun sendDecision(post: ApplicantList, decision: String) {
            val request = ApplicantDecisionRequest(userId = post.userId, postId = post.postId, isConfirmed = decision)

            applicantListService.decideApplicant(request).enqueue(object : Callback<ApplicantDecisionResponse> {
                override fun onResponse(
                    call: Call<ApplicantDecisionResponse>,
                    response: Response<ApplicantDecisionResponse>
                ) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody != null) {
                            Toast.makeText(itemView.context, responseBody.message, Toast.LENGTH_SHORT).show()
                            if (responseBody.isSuccess) {
                                post.isConfirmed = decision
                                notifyItemChanged(bindingAdapterPosition) // Update UI based on decision
                            }
                        }
                    } else {
                        Toast.makeText(itemView.context, "요청이 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApplicantDecisionResponse>, t: Throwable) {
                    Toast.makeText(itemView.context, "서버와의 연결에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    // 게시글 목록 전체 갱신
    fun updatePosts(newPosts: List<ApplicantList>) {
        posts.clear()
        posts.addAll(newPosts)
        notifyDataSetChanged()  // 전체 항목을 갱신
    }
}
