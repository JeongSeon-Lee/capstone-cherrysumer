package com.cookandroid.cherrysumer.mypage

import android.content.Intent
import android.util.Log
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

class RecruitmentAdapter(private val posts: MutableList<RecruitmentPost>, private val recruitmentService: RecruitmentService) :
    RecyclerView.Adapter<RecruitmentAdapter.RecruitmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecruitmentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recruiment_item, parent, false)
        return RecruitmentViewHolder(view, recruitmentService)
    }

    override fun onBindViewHolder(holder: RecruitmentViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post)
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    class RecruitmentViewHolder(itemView: View, private val recruitmentService: RecruitmentService) : RecyclerView.ViewHolder(itemView) {
        private val postImage: ImageView = itemView.findViewById(R.id.post_image)
        private val postId: TextView = itemView.findViewById(R.id.post_id)
        private val title: TextView = itemView.findViewById(R.id.title)
        private val status: TextView = itemView.findViewById(R.id.status)
        private val moreButton: ImageView = itemView.findViewById(R.id.more_button)

        fun bind(post: RecruitmentPost) {
            postId.text = post.postId.toString()
            title.text = post.title
            status.text = "${post.applicantCount}명"

            val baseUrl = "http://3.39.110.119"
            val postImageUrl = baseUrl + post.imageUrl

            // Glide로 이미지 로드
            Glide.with(itemView.context)
                .load(postImageUrl) // 이미지 URL
                .placeholder(R.drawable.default_post_image1) // 로드 중일 때 보여줄 기본 이미지
                .error(R.drawable.default_post_image1) // 에러 시 보여줄 이미지
                .into(postImage) // ImageView에 로드

            // 게시글 클릭 시 상세 정보로 이동
            itemView.setOnClickListener {
                fetchPostDetail(post.postId)
            }

            // 'more_button' 클릭 시 메뉴 표시
            moreButton.setOnClickListener { view ->
                showPopupMenu(view, post)
            }
        }

        private fun showPopupMenu(view: View, post: RecruitmentPost) {
            // PopupMenu 생성 및 표시
            val popupMenu = android.widget.PopupMenu(itemView.context, view)
            val inflater = popupMenu.menuInflater
            inflater.inflate(R.menu.post_recruitment_menu, popupMenu.menu)

            // menu_register 표시 여부 결정
            val menuRegister = popupMenu.menu.findItem(R.id.menu_register)
            menuRegister.isVisible = post.purchaseCompleted && !post.inventoryRegistered

            // 메뉴 아이템 클릭 시 처리
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_status -> {
                        // 참여 신청자 목록 액티비티로 이동
                        val intent = Intent(itemView.context, ApplicantListActivity::class.java)
                        intent.putExtra("postId", post.postId)
                        itemView.context.startActivity(intent)
                        true
                    }
                    R.id.menu_register -> {
                        // 재고 등록 프래그먼트로 이동
//                        val intent = Intent(itemView.context, InventoryRegisterActivity::class.java)
                        // post 객체를 JSON으로 직렬화하여 전달
//                        val postJson = Gson().toJson(post)
//                        intent.putExtra("recruitmentPost", postJson)
//                        itemView.context.startActivity(intent)
                        true
                    }
                    else -> false
                }
            }

            // 메뉴 표시
            popupMenu.show()
        }


        private fun fetchPostDetail(postId: Long) {
            recruitmentService.getPostStatusDetail(postId).enqueue(object : Callback<RecruitmentStatusDetailResponse> {
                override fun onResponse(call: Call<RecruitmentStatusDetailResponse>, response: Response<RecruitmentStatusDetailResponse>) {
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

                override fun onFailure(call: Call<RecruitmentStatusDetailResponse>, t: Throwable) {
                    Toast.makeText(itemView.context, "서버와의 연결에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            })
        }

        private fun navigateToPostDetail(postDetailData: RecruitmentStatusDetailData) {
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
    fun updatePosts(newPosts: List<RecruitmentPost>) {
        posts.clear()  // 기존 항목 모두 삭제
        posts.addAll(newPosts)  // 새 항목 모두 추가
        notifyDataSetChanged()  // 전체 항목을 갱신
    }
}