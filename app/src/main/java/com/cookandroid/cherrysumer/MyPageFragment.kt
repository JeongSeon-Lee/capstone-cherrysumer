package com.cookandroid.cherrysumer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.cookandroid.cherrysumer.mypage.*
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.http.GET
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.Header

class MyPageFragment : Fragment() {
    private lateinit var mypageService: MypageService
    private var profileData: ProfileData? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mypage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        mypageService = retrofit.create(MypageService::class.java)

        val profileImageView: ImageView = view.findViewById(R.id.profile_image)
        val nameTextView: TextView = view.findViewById(R.id.name)

        // 서버에서 프로필 데이터를 가져옴
        fetchProfileData(view, profileImageView, nameTextView)

        // 프로필 보기 버튼 클릭 리스너
        val profileButton: Button = view.findViewById(R.id.profileButton)
        profileButton.setOnClickListener {
            // 서버에서 받은 프로필 데이터를 넘겨줌
            profileData?.let {
                val intent = Intent(activity, ProfileViewActivity::class.java)
                intent.putExtra("name", it.name)
                intent.putExtra("nickname", it.nickname)
                intent.putExtra("email", it.email)
                intent.putExtra("region", it.region)
                intent.putExtra("profileImageUrl", it.profileImageUrl)
                intent.putExtra("loginId", it.loginId)
                startActivity(intent)
            }
        }

        setupClickListener(view, R.id.like_list, LikeListActivity::class.java)
        setupClickListener(view, R.id.post_manage, PostManageActivity::class.java)
        setupClickListener(view, R.id.status, StatusActivity::class.java)
        setupClickListener(view, R.id.category_setting, CategorySettingActivity::class.java)
        setupClickListener(view, R.id.myRegion_setting, MyRegionSettingActivity::class.java)
        setupClickListener(view, R.id.notice, NoticeActivity::class.java)
        setupLogoutClickListener(view, R.id.logout)
        byeClickListener(view, R.id.bye) {
            showDeleteAccountDialog()
        }
    }

    private fun showDeleteAccountDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_bye, null)
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val cancelButton = dialogView.findViewById<Button>(R.id.cancel_button)
        val deleteButton = dialogView.findViewById<Button>(R.id.decision_closed)

        // "더 써볼래요" 클릭 시 다이얼로그 닫기
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        // "떠날래요" 클릭 시 계정 삭제 함수 호출
        deleteButton.setOnClickListener {
            deleteAccount()
            dialog.dismiss()  // 다이얼로그 닫기
        }

        dialog.show()
    }

    private fun deleteAccount() {
        // SharedPreferences에서 저장된 토큰을 가져옴
        val sharedPreferences = requireActivity().getSharedPreferences("CherrySumerprefs", AppCompatActivity.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)

        if (token != null) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    // DELETE 요청 보내기
                    val response = mypageService.deleteAccount(token)
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "회원탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                        logout()  // 로그아웃 처리
                    } else {
                        Toast.makeText(requireContext(), "회원탈퇴 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("NetworkError", "네트워크 오류: ${e.message}", e)
                    Toast.makeText(requireContext(), "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 로그아웃 버튼 클릭 리스너
    private fun setupLogoutClickListener(view: View, id: Int) {
        val button: TextView = view.findViewById(id)
        button.setOnClickListener {
            logout() // 로그아웃 처리
        }
    }

    // 로그아웃 처리
    private fun logout() {
        // SharedPreferences에서 토큰 삭제
        val sharedPreferences = requireActivity().getSharedPreferences("CherrySumerprefs", AppCompatActivity.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("token") // 토큰 삭제
        editor.apply() // 변경사항 저장

        // 로그인 화면으로 이동
        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK // 기존 액티비티 스택을 정리하고 새 로그인 화면으로 이동
        startActivity(intent)
        activity?.finish() // 현재 액티비티 종료
    }

    private fun setupClickListener(view: View, id: Int, activityClass: Class<*>) {
        val button: TextView = view.findViewById(id)
        button.setOnClickListener {
            val intent = Intent(activity, activityClass)
            startActivity(intent)
        }
    }

    private fun byeClickListener(view: View, id: Int, onClickAction: () -> Unit) {
        val button: TextView = view.findViewById(id)
        button.setOnClickListener {
            onClickAction()  // 전달된 함수 실행
        }
    }

    private fun fetchProfileData(view: View, profileImageView: ImageView, nameTextView: TextView) {
        // Coroutine을 사용하여 비동기 요청을 보냄
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = mypageService.getProfile()
                if (response.isSuccessful) {
                    val profileData = response.body()?.data
                    this@MyPageFragment.profileData = profileData

                    profileData?.let {
                        nameTextView.text = it.name
                        // 프로필 이미지 URL이 없거나 비어 있으면 기본 이미지 사용
                        val imageUrl = if (it.profileImageUrl.isNullOrEmpty()) {
                            "drawable/default_profile_image"  // 기본 이미지 리소스 경로
                        } else {
                            "http://3.39.110.119${it.profileImageUrl}"  // 서버에서 받은 프로필 이미지 URL
                        }

                        // Glide로 이미지 로드 (기본 이미지 설정)
                        Glide.with(this@MyPageFragment)
                            .load(imageUrl)  // 이미지 URL 설정
                            .placeholder(R.drawable.default_profile_image)  // 기본 이미지 설정
                            .circleCrop()
                            .into(profileImageView)
                    }
                } else {
                    Toast.makeText(view.context, "서버 오류 발생", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("NetworkError", "네트워크 오류: ${e.message}", e)
                Toast.makeText(view.context, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val profileImageView: ImageView = requireView().findViewById(R.id.profile_image)
        val nameTextView: TextView = requireView().findViewById(R.id.name)
        fetchProfileData(requireView(), profileImageView, nameTextView)
    }
}

data class ProfileResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val data: ProfileData
)

data class ProfileData(
    val name: String,
    val nickname: String,
    val email: String,
    val region: String,
    val profileImageUrl: String,
    val loginId: String
)


interface MypageService {
    @GET("mypage/profile")
    suspend fun getProfile(): Response<ProfileResponse>

    @DELETE("mypage/deleteAccount")
    suspend fun deleteAccount(@Header("Authorization") token: String): Response<Void>
}