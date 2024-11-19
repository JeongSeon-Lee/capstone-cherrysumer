package com.cookandroid.cherrysumer.mypage

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cookandroid.cherrysumer.MainActivity
import com.cookandroid.cherrysumer.R

class ProfileViewActivity : AppCompatActivity() {
    private lateinit var profileImage: ImageView
    private lateinit var name: TextView
    private lateinit var nickname: TextView
    private lateinit var userInfoName: TextView
    private lateinit var userInfoNickname: TextView
    private lateinit var userInfoEmail: TextView
    private lateinit var userInfoRegion: TextView
    private lateinit var passwordChangeButton: LinearLayout
    private lateinit var profileEditButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage_profile)

        name = findViewById(R.id.user_name)
        nickname = findViewById(R.id.user_nickname)
        userInfoName = findViewById(R.id.userInfo_name)
        userInfoNickname = findViewById(R.id.userInfo_nickname)
        userInfoEmail = findViewById(R.id.userInfo_email)
        userInfoRegion = findViewById(R.id.userInfo_region)
        profileImage = findViewById(R.id.profile_image)
        passwordChangeButton = findViewById(R.id.password_change)
        profileEditButton = findViewById(R.id.profileButton)

        // Intent로 전달된 데이터 받기
        val nameIntent = intent.getStringExtra("name")
        val nicknameIntent = intent.getStringExtra("nickname")
        val emailIntent = intent.getStringExtra("email")
        val regionIntent = intent.getStringExtra("region")
        val profileImageUrlIntent = intent.getStringExtra("profileImageUrl")

        nameIntent?.let {
            name.text = it
            userInfoName.text = it
        }

        nicknameIntent?.let {
            nickname.text = it
            userInfoNickname.text = it
        }

        emailIntent?.let {
            userInfoEmail.text = it
        }

        regionIntent?.let {
            userInfoRegion.text = it
        }

        // 이미지 URL이 있을 경우 Glide로 이미지 로드, 없으면 기본 이미지 로드
        profileImageUrlIntent?.let {
            val imageUrl = "http://3.39.110.119$it"  // 베이스 주소 추가
            Glide.with(this)
                .load(imageUrl)  // URL로 이미지를 로드
                .error(R.drawable.default_profile_image)  // 에러 발생 시 기본 이미지 설정
                .circleCrop()  // 원형으로 이미지 자르기
                .into(profileImage)
        } ?: run {
            // 이미지 URL이 없으면 기본 이미지 설정
            profileImage.setImageResource(R.drawable.default_profile_image)
        }

        // 비밀번호 변경 버튼 클릭 시
        passwordChangeButton.setOnClickListener {
            // 비밀번호 변경 액티비티로 이동
//            val intent = Intent(this, PasswordChangeActivity::class.java)
//            startActivity(intent)
        }

        // 프로필 수정 버튼 클릭 시
        profileEditButton.setOnClickListener {
            // 프로필 수정 액티비티로 이동
//            val intent = Intent(this, ProfileEditActivity::class.java)
//            startActivity(intent)
        }

        // 이전 버튼 클릭 시 마이페이지로 돌아감
        val previousButton = findViewById<ImageButton>(R.id.previous_button)
        previousButton.setOnClickListener {
//            val intent = Intent(this, MainActivity::class.java)
//            intent.putExtra("goToMyPage", true) // MyPageFragment로 돌아가라는 신호 전달
//            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
//            startActivity(intent)
            finish() // 현재 액티비티 종료
        }
    }
}