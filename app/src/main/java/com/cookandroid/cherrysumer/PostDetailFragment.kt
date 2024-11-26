package com.cookandroid.cherrysumer

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import com.google.gson.Gson
import android.os.Parcelable
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.cookandroid.cherrysumer.mypage.LikeListActivity
import com.cookandroid.cherrysumer.mypage.PostManageActivity
import com.cookandroid.cherrysumer.mypage.StatusActivity
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

class PostDetailFragment : Fragment() {
    private lateinit var previousButton: AppCompatImageButton
    private lateinit var postOwnerTextView: TextView
    private lateinit var closedTextView: TextView
    private lateinit var postTitleTextView: TextView
    private lateinit var uploadTimeTextView: TextView
    private lateinit var priceTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var capacityTextView: TextView
    private lateinit var contentTextView: TextView
    private lateinit var likeStatusImageView: ImageView
    private lateinit var helpButton: AppCompatImageButton
    private lateinit var postDetailService: PostDetailService
    private lateinit var viewPagerImages : ViewPager2
    private lateinit var enjoyButton: Button
    private lateinit var closedButton: Button
    private lateinit var messageButton: Button
    private lateinit var likesCount: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_post_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.hideBottomNavigationView()


        // UI 요소 초기화
        previousButton = view.findViewById(R.id.previous_button)
        postOwnerTextView = view.findViewById(R.id.post_owner)
        closedTextView = view.findViewById(R.id.closed)
        postTitleTextView = view.findViewById(R.id.post_title)
        uploadTimeTextView = view.findViewById(R.id.upload_time)
        priceTextView = view.findViewById(R.id.price)
        dateTextView = view.findViewById(R.id.date)
        locationTextView = view.findViewById(R.id.location)
        capacityTextView = view.findViewById(R.id.capacity)
        contentTextView = view.findViewById(R.id.content)
        likeStatusImageView = view.findViewById(R.id.like_status)
        likesCount = view.findViewById(R.id.likes_count)
        helpButton = view.findViewById(R.id.help_button)
        viewPagerImages = view.findViewById(R.id.viewPagerImages)
        enjoyButton = view.findViewById(R.id.enjoy_button)
        closedButton = view.findViewById(R.id.closed_button)
        messageButton = view.findViewById(R.id.message_button)


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

        postDetailService = retrofit.create(PostDetailService::class.java)



        // JSON 문자열을 PostDetailData 객체로 변환
        val jsonPostData = arguments?.getString("postDetailsJson")
        val postData = Gson().fromJson(jsonPostData, PostData::class.java)

        // PostDetailData 객체를 Post 객체로 변환
        val post = postData.toPostD()

        Log.d("PostDetailFragment", "Received data: $postData")

        val imageUrls = postData.imagefiles ?: emptyList()
        val defaultImageResId = R.drawable.default_post_image1

        val adapter = ImageAdapter(imageUrls, defaultImageResId)
        viewPagerImages.adapter = adapter


        // Null 체크 후 UI 설정
        postData?.let { data ->
            previousButton.setOnClickListener {
                val previousActivity = activity?.intent?.getStringExtra("previousActivity")

                if (previousActivity != null) {
                    val intent = when (previousActivity) {
                        "com.cookandroid.cherrysumer.mypage.PostManageActivity" -> {
                            Intent(requireContext(), PostManageActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                            }
                        }
                        "com.cookandroid.cherrysumer.mypage.LikeListActivity" -> {
                            Intent(requireContext(), LikeListActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                            }
                        }
                        "com.cookandroid.cherrysumer.mypage.StatusActivity" -> {
                            Intent(requireContext(), StatusActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                            }
                        }
                        else -> null
                    }

                    if (intent != null) {
                        try {
                            startActivity(intent)
                            activity?.finish()  // 현재 프래그먼트가 속한 액티비티 종료
                        } catch (e: ActivityNotFoundException) {
                            Log.e("IntentError", "해당 액티비티를 찾을 수 없습니다: $previousActivity", e)
                            // 오류 시 백 스택으로 돌아가기
                            (context as MainActivity).supportFragmentManager.popBackStack()
                        }
                    }
                } else {
                    // 백 스택에서 프래그먼트를 pop하여 이전 화면으로 돌아가기
                    val fragmentManager = (context as MainActivity).supportFragmentManager
                    if (fragmentManager.backStackEntryCount > 0) {
                        fragmentManager.popBackStack()
                    }
                }
            }

            // 다이얼로그 설정
            helpButton.setOnClickListener {
                showHelpDialog()
            }

            // 작성자 설정
            postOwnerTextView.text = "${data.writer}님의 게시글"

            // closed 상태에 따라 visibility 설정
            closedTextView.visibility = if (data.closed) View.VISIBLE else View.GONE

            // closed 상태에 따라 enjoyButton 활성/비활성 설정
            enjoyButton.isEnabled = !data.closed

            // join 상태에 따라 enjoyButton 비활성화 및 텍스트 변경
            if (data.join) {
                enjoyButton.isEnabled = false
                enjoyButton.text = "신청완료"
            } else {
                enjoyButton.isEnabled = true
                enjoyButton.text = "참여하기"
            }

            if (data.closed) {
                enjoyButton.isEnabled = false
                enjoyButton.text = "    마감      "
                closedButton.isEnabled = false
                closedButton.text = "마감"
            } else {
                enjoyButton.isEnabled = true
                enjoyButton.text = "참여하기"
                closedButton.isEnabled = true
                closedButton.text = "마감하기"
            }

            if (data.author) {
                enjoyButton.visibility = View.GONE
                messageButton.visibility = View.GONE
                closedButton.visibility = View.VISIBLE
                likesCount.visibility = View.VISIBLE
                likesCount.text = "${data.likes}"
            } else {
                enjoyButton.visibility = View.VISIBLE
                messageButton.visibility = View.VISIBLE
                closedButton.visibility = View.GONE
                likesCount.visibility = View.GONE
            }

            // 카테고리 태그 동적 생성
            val categories = (data.category ?: emptyList()) + (data.detail_category ?: emptyList()) // 카테고리 결합
            // 태그 컨테이너 레이아웃
            val tagBoxLayout = view.findViewById<LinearLayout>(R.id.categories)

            for (cat in categories) {
                // tag_item.xml 레이아웃을 inflate하여 새로운 뷰 생성
                val categoryTagView = LayoutInflater.from(requireContext()).inflate(R.layout.tag_item, tagBoxLayout, false)

                // inflate한 뷰에서 TextView를 찾아 텍스트 설정
                val tagTextView = categoryTagView.findViewById<TextView>(R.id.category_tag_text)
                tagTextView.text = cat

                // 생성한 categoryTagView를 tagBoxLayout에 추가
                tagBoxLayout.addView(categoryTagView)
            }

            // 게시글 제목, 업로드 시간, 가격, 날짜, 위치, 수용 인원, 내용 설정
            postTitleTextView.text = data.title
            uploadTimeTextView.text = data.upload
            priceTextView.text = "${data.price}원" // 가격 표시
            dateTextView.text = data.date
            locationTextView.text = data.place
            capacityTextView.text = "${data.capacity}명" // 수용 인원 표시
            contentTextView.text = data.content

            // 좋아요 상태 이미지 설정
            likeStatusImageView.setImageResource(
                if (data.like_status) R.drawable.like else R.drawable.no_like
            )

            // 하트 클릭 리스너 설정
            likeStatusImageView.setOnClickListener {
                toggleLike(post)
            }

            enjoyButton.setOnClickListener {
                postApplicant(data.postId)
            }

            closedButton.setOnClickListener {
                showModalDialog(data.postId)
            }
        }
    }

    private fun showModalDialog(postId: Long) {
        // Dialog 생성
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_post_closed)
        dialog.setCancelable(true) // true로 설정하면 밖을 눌러도 닫힘

        // Dialog 배경 어둡게 설정
        dialog.window?.setDimAmount(0.7f) // 0.0 ~ 1.0으로 어둡게 조정 가능

        // Dialog 내부 요소 가져오기
        val close: Button = dialog.findViewById(R.id.decision_closed)
        val cancel: Button = dialog.findViewById(R.id.cancel_button)


        // "취소" 버튼 클릭 시 다이얼로그 닫기
        cancel.setOnClickListener {
            dialog.dismiss()
        }

        // "마감" 버튼 클릭 시 API 호출
        close.setOnClickListener {
            postClosed(postId) { isSuccess, message ->
                dialog.dismiss() // 다이얼로그 닫기
                if (isSuccess) {
                    /// 성공 시 Toast로 메시지 띄우고 이전 화면으로 돌아가기
                    Toast.makeText(requireContext(), "게시글이 성공적으로 마감되었습니다.", Toast.LENGTH_SHORT).show()
                    // 경고창이 뜨고 나서 자동으로 이전 화면으로 돌아가도록 처리
                    // 이전 화면으로 돌아가기
                    val previousActivity = activity?.intent?.getStringExtra("previousActivity")

                    if (previousActivity != null) {
                        val intent = when (previousActivity) {
                            "com.cookandroid.cherrysumer.mypage.PostManageActivity" -> {
                                Intent(requireContext(), PostManageActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                                }
                            }
                            "com.cookandroid.cherrysumer.mypage.LikeListActivity" -> {
                                Intent(requireContext(), LikeListActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                                }
                            }
                            "com.cookandroid.cherrysumer.mypage.StatusActivity" -> {
                                Intent(requireContext(), StatusActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                                }
                            }
                            else -> null
                        }

                        if (intent != null) {
                            try {
                                startActivity(intent)
                                activity?.finish()  // 현재 프래그먼트가 속한 액티비티 종료
                            } catch (e: ActivityNotFoundException) {
                                Log.e("IntentError", "해당 액티비티를 찾을 수 없습니다: $previousActivity", e)
                                // 오류 시 백 스택으로 돌아가기
                                (context as MainActivity).supportFragmentManager.popBackStack()
                            }
                        }
                    } else {
                        // 백 스택에서 프래그먼트를 pop하여 이전 화면으로 돌아가기
                        val fragmentManager = (context as MainActivity).supportFragmentManager
                        if (fragmentManager.backStackEntryCount > 0) {
                            fragmentManager.popBackStack()
                        }
                    }
                } else {
                    // 실패 시 서버 메시지를 Toast로 띄우기
                    Toast.makeText(requireContext(), "실패: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Dialog 표시
        dialog.show()
    }

    // 게시글 마감 API 호출
    private fun postClosed(postId: Long, callback: (Boolean, String) -> Unit) {
        val call = postDetailService.postclosed(postId)
        call.enqueue(object : Callback<PostClosedResponse> {
            override fun onResponse(
                call: Call<PostClosedResponse>,
                response: Response<PostClosedResponse>
            ) {
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    // 성공적으로 마감 완료
                    callback(true, response.body()?.message ?: "성공")
                } else {
                    // 실패 시 서버 메시지 처리
                    val errorMessage = response.body()?.message ?: "요청 실패"
                    callback(false, errorMessage)
                }
            }

            override fun onFailure(call: Call<PostClosedResponse>, t: Throwable) {
                // 네트워크 오류 처리
                callback(false, "네트워크 오류: ${t.message}")
            }
        })
    }

    private fun toggleLike(post: PostD) {
        // postId를 Long으로 변환
        val postIdLong = post.postId.toLong()
        val newLikeStatus = !post.like_status // 새로운 좋아요 상태

        // 서버 호출
        postDetailService.toggleLike(postIdLong).enqueue(object : Callback<LikeToggleResponse> {
            override fun onResponse(call: Call<LikeToggleResponse>, response: Response<LikeToggleResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    // 성공적으로 좋아요 상태 변경
                    Log.d("PostDetailFragment", "좋아요 상태 변경 성공: ${response.body()}")
                    post.like_status = newLikeStatus // 상태 업데이트
                    updateHeartIcon(newLikeStatus) // UI 업데이트
                } else {
                    // 서버 응답 오류
                    Log.e("PostDetailFragment", "좋아요 상태 변경 오류 - 코드: ${response.code()}, 메시지: ${response.message()}")
                    updateHeartIcon(!newLikeStatus) // 실패 시 원래 상태로 복구
                }
            }

            override fun onFailure(call: Call<LikeToggleResponse>, t: Throwable) {
                // 네트워크 오류 발생
                Log.e("PostDetailFragment", "네트워크 오류: ${t.message}")
                updateHeartIcon(!newLikeStatus) // 실패 시 원래 상태로 복구
            }
        })
    }

    private fun postApplicant(postId: Long) {
        postDetailService.applyForPost(postId).enqueue(object : Callback<PostApplicationResponse> {
            override fun onResponse(
                call: Call<PostApplicationResponse>,
                response: Response<PostApplicationResponse>
            ) {
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val responseData = response.body()?.data
                    if (responseData != null) {
                        // 성공적으로 공구 신청 완료, 데이터 전달하며 프래그먼트 이동
                        navigateToEnjoySuccessFragment(responseData)
                    }
                } else {
                    // 서버 응답 실패 처리
                    val errorMessage = response.body()?.message ?: "요청에 실패했습니다."
                    Log.e("PostApplicant", "공구 신청 실패 - 메시지: $errorMessage")
                }
            }

            override fun onFailure(call: Call<PostApplicationResponse>, t: Throwable) {
                // 네트워크 오류 처리
                Log.e("PostApplicant", "네트워크 오류 발생: ${t.message}")
                Toast.makeText(
                    requireContext(),
                    "네트워크 오류가 발생했습니다. 다시 시도해주세요.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    // 공구 요청 성공 시 EnjoySuccessFragment로 이동하는 함수
    private fun navigateToEnjoySuccessFragment(data: PostApplicationData) {
        val fragment = EnjoySuccessFragment()
        val bundle = Bundle().apply {
            putLong("postId", data.postId)
            putString("title", data.title)
            putInt("price", data.price)
            putString("place", data.place)
            putString("date", data.date)
            putString("imageUrl", data.imageUrl)
        }
        fragment.arguments = bundle

        // 프래그먼트 이동
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment) // fragment_container는 액티비티의 프래그먼트 컨테이너 ID
        transaction.addToBackStack(null) // 뒤로가기 시 이전 화면으로 돌아가기 위해 스택에 추가
        transaction.commit()
    }

    private fun updateHeartIcon(isLiked: Boolean) {
        likeStatusImageView.setImageResource(if (isLiked) R.drawable.like else R.drawable.no_like)
    }


    private fun showHelpDialog() {
        // 다이얼로그 레이아웃 인플레이트
        val dialogView = layoutInflater.inflate(R.layout.dialog_post_guide, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // 각 TextView에 HTML을 사용하여 볼드체 설정
        dialogView.findViewById<TextView>(R.id.text1).text =
            Html.fromHtml("<b>참여하기</b> : 게시글 일정에 참여 신청을 할 수 있습니다. 버튼을 누르실 경우 작성자에게 회원님의 정보가 보여지며, 신청은 취소할 수 없습니다.", Html.FROM_HTML_MODE_COMPACT)

        dialogView.findViewById<TextView>(R.id.text2).text =
            Html.fromHtml("<b>메시지</b> : 게시글 작성자와 채팅을 할 수 있습니다. 신청 취소, 일정 조정 등이 필요할 때 사용해 보세요.", Html.FROM_HTML_MODE_COMPACT)

        dialogView.findViewById<TextView>(R.id.text3).text =
            Html.fromHtml("<b>찜</b> : 하트를 누르면 관심 게시글에 추가됩니다. 찜한 게시글은 마이페이지에서 확인 가능합니다.", Html.FROM_HTML_MODE_COMPACT)

        dialogView.findViewById<TextView>(R.id.text4).text =
            Html.fromHtml("<b>참여신청 승인 여부는 마이페이지에서 확인 가능합니다.</b>", Html.FROM_HTML_MODE_COMPACT)

        dialogView.findViewById<TextView>(R.id.text5).text =
            Html.fromHtml("작성자에게 제공되는 신청자 정보 : 닉네임, 거주지", Html.FROM_HTML_MODE_COMPACT)


        // 다이얼로그 위치 조정
        dialog.window?.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL) // 상단 중앙에 위치

        // 다이얼로그 보여주기
        dialog.show()
    }
}

data class PostD(
    val postId: Long,
    val writer: String,
    val imageUrls: List<String>?,
    val detail_category: List<String>?,
    val category: List<String>,
    val title: String,
    val productname: String,
    val upload: String,
    val price: Int,
    val date: String,
    val capacity: Int,
    val place: String,
    val likes: Int,
    var like_status: Boolean,
    val closed: Boolean,
    val author: Boolean,
    val join: Boolean,
    val content: String
)

data class PostData(
    val postId: Long,
    val writer: String,
    val writerId: Long,
    val imagefiles: List<String>?,
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
) {
    // Post 객체로 변환하는 메소드
    fun toPostD(): PostD {
        return PostD(
            postId = postId,
            writer = writer,
            imageUrls = imagefiles,
            detail_category = detail_category,
            category = category,
            title = title,
            productname = productname,
            upload = upload,
            price = price,
            date = date,
            capacity = capacity,
            place = place,
            likes = likes,
            like_status = like_status,
            closed = closed,
            author = author,
            join = join,
            content = content
        )
    }
}

data class LikeToggleResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val data: LikeToggleData
)

data class LikeToggleData(
    val postId: Long,
    val like_status: Boolean,
    val likes: Int
)

data class PostApplicationResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val data: PostApplicationData?
)

data class PostApplicationData(
    val postId: Long,
    val title: String,
    val price: Int,
    val place: String,
    val date: String,
    val imageUrl: String
)

data class PostClosedResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val data: PostClosedData
)

data class PostClosedData(
    val postId: Long,
    val closed: Boolean
)

interface PostDetailService {
    // 게시글 좋아요 상태 토글
    @PUT("posts/{postId}/likes")
    fun toggleLike(@Path("postId") postId: Long): Call<LikeToggleResponse>

    // 공구 참여 신청
    @POST("posts/{postId}/application")
    fun applyForPost(@Path("postId") postId: Long): Call<PostApplicationResponse>

    // 게시글 마감 신청
    @PUT("posts/{postId}/closed")
    fun postclosed(@Path("postId") postId: Long): Call<PostClosedResponse>
}