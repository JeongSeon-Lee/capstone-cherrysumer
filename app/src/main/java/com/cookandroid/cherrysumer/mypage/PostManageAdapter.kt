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

class PostManageAdapter(private val posts: MutableList<MyPosts>, private val managePostService: ManagePostService) : RecyclerView.Adapter<PostManageAdapter.ManagePostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManagePostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_manage_item, parent, false)
        return ManagePostViewHolder(view, managePostService)
    }

    override fun onBindViewHolder(holder: ManagePostViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post)
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    // ViewHolder 클래스
    class ManagePostViewHolder(itemView: View, private val managePostService: ManagePostService) : RecyclerView.ViewHolder(itemView) {
        private val postId: TextView = itemView.findViewById(R.id.post_id)
        private val title: TextView = itemView.findViewById(R.id.title)
        private val region: TextView = itemView.findViewById(R.id.region)
        private val price: TextView = itemView.findViewById(R.id.price)
        private val closed: TextView = itemView.findViewById(R.id.closed)
        private val moreButton: ImageView = itemView.findViewById(R.id.more_button)
        private val postImage: ImageView = itemView.findViewById(R.id.post_image)

        fun bind(post: MyPosts) {
            postId.text = post.postId.toString()
            title.text = post.title
            region.text = post.region
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

            // 게시글 클릭 시 상세 정보로 이동
            itemView.setOnClickListener {
                fetchPostDetail(post.postId)
            }

            // 'more_button' 클릭 시 메뉴 표시
            moreButton.setOnClickListener { view ->
                showPopupMenu(view, post.postId)
            }
        }

        private fun showPopupMenu(view: View, postId: Long) {
            // PopupMenu 생성 및 표시
            val popupMenu = android.widget.PopupMenu(itemView.context, view)
            val inflater = popupMenu.menuInflater
            inflater.inflate(R.menu.post_manage_menu, popupMenu.menu)

            // 메뉴 아이템 클릭 시 처리
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_edit -> {
                        // 수정 처리 (예시)
                        Log.d("PostManage", "수정 버튼 클릭")
                        true
                    }
                    R.id.menu_delete -> {
                        // 삭제 처리 (예시)
                        deletePost(postId)
                        true
                    }
                    else -> false
                }
            }

            // 메뉴 표시
            popupMenu.show()
        }

        private fun deletePost(postId: Long) {
            // 서버로 DELETE 요청 보내기
            managePostService.deletePost(postId).enqueue(object : Callback<DeletePostResponse> {
                override fun onResponse(call: Call<DeletePostResponse>, response: Response<DeletePostResponse>) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody != null && responseBody.isSuccess) {
                            // 게시글 삭제 성공
                            Toast.makeText(itemView.context, "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                            // 페이지를 다시 로드하거나 필요한 동작 수행
                            val position = bindingAdapterPosition
                            if (position != RecyclerView.NO_POSITION) {
                                (itemView.context as PostManageActivity).removePost(position)
                            }
                        } else {
                            // 삭제 실패
                            Toast.makeText(itemView.context, responseBody?.message ?: "서버 오류", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // 서버 응답이 실패일 경우
                        Toast.makeText(itemView.context, "서버 응답 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<DeletePostResponse>, t: Throwable) {
                    // 네트워크 오류 발생 시
                    Toast.makeText(itemView.context, "서버와의 연결에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            })
        }

        private fun reloadPage() {
            // 페이지를 다시 로드하거나 적절한 액티비티/프래그먼트를 다시 불러오는 방법을 구현
            // 예시로 MainActivity로 돌아가도록 설정
            val intent = Intent(itemView.context, PostManageActivity::class.java)
            itemView.context.startActivity(intent)
        }


        private fun fetchPostDetail(postId: Long) {
            managePostService.getMyPostDetail(postId).enqueue(object : Callback<MyPostDetailResponse> {
                override fun onResponse(call: Call<MyPostDetailResponse>, response: Response<MyPostDetailResponse>) {
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

                override fun onFailure(call: Call<MyPostDetailResponse>, t: Throwable) {
                    Toast.makeText(itemView.context, "서버와의 연결에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            })
        }

        private fun navigateToPostDetail(postDetailData: MyPostDetailData) {
            // 이전 액티비티 정보를 Intent에 저장 (명시적으로 액티비티 이름을 입력)
            val intent = Intent(itemView.context, MainActivity::class.java).apply {
                putExtra("previousActivity", "com.cookandroid.cherrysumer.mypage.PostManageActivity") // 명시적으로 액티비티 이름 입력
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
    fun updatePosts(newPosts: List<MyPosts>) {
        posts.clear()
        posts.addAll(newPosts)
        notifyDataSetChanged()  // 전체 항목을 갱신
    }
}