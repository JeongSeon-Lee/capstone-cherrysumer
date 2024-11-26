package com.cookandroid.cherrysumer.mypage

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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

    private var loginId: String? = null // loginId 저장
    private var profileImageUrlIntent: String? = null
    private var nameIntent: String? = null
    private var nicknameIntent: String? = null
    private var emailIntent: String? = null
    private var regionIntent: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 액티비티의 전체 배경색을 하얀색으로 설정
        window.decorView.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        setContentView(R.layout.activity_mypage_profile)

        // View 초기화
        initializeViews()

        // Intent 데이터 처리
        handleIntentData(intent)

        // 버튼 동작 설정
        setupButtonActions()
    }

    // View 초기화
    private fun initializeViews() {
        name = findViewById(R.id.user_name)
        nickname = findViewById(R.id.user_nickname)
        userInfoName = findViewById(R.id.userInfo_name)
        userInfoNickname = findViewById(R.id.userInfo_nickname)
        userInfoEmail = findViewById(R.id.userInfo_email)
        userInfoRegion = findViewById(R.id.userInfo_region)
        profileImage = findViewById(R.id.profile_image)
        passwordChangeButton = findViewById(R.id.password_change)
        profileEditButton = findViewById(R.id.profileButton)
    }

    // Intent 데이터 처리
    private fun handleIntentData(intent: Intent) {
        loginId = intent.getStringExtra("loginId") // loginId 저장
        profileImageUrlIntent = intent.getStringExtra("profileImageUrl") // 이미지 URL 저장
        nameIntent = intent.getStringExtra("name")
        nicknameIntent = intent.getStringExtra("nickname")
        emailIntent = intent.getStringExtra("email")
        regionIntent = intent.getStringExtra("region")

        // 로그로 전달된 데이터를 출력
        Log.d("ProfileViewActivity", "loginId: $loginId")
        Log.d("ProfileViewActivity", "profileImageUrlIntent: $profileImageUrlIntent")
        Log.d("ProfileViewActivity", "nameIntent: $nameIntent")
        Log.d("ProfileViewActivity", "nicknameIntent: $nicknameIntent")
        Log.d("ProfileViewActivity", "emailIntent: $emailIntent")
        Log.d("ProfileViewActivity", "regionIntent: $regionIntent")

        // Intent 데이터를 UI에 반영
        updateProfileUI(profileImageUrlIntent, nameIntent, nicknameIntent, regionIntent, emailIntent)
    }

    // 버튼 동작 설정
    private fun setupButtonActions() {
        // 비밀번호 변경 버튼 클릭 시
        passwordChangeButton.setOnClickListener {
            // 비밀번호 변경 화면으로 이동
            val intent = Intent(this, PasswordChangeActivity::class.java).apply {
                putExtra("loginId", loginId) // loginId 전달
            }
            startActivity(intent)
        }

        // 프로필 수정 버튼 클릭 시
        profileEditButton.setOnClickListener {
            val intent = Intent(this, ProfileEditActivity::class.java).apply {
                putExtra("name", name.text.toString())
                putExtra("nickname", nickname.text.toString())
                putExtra("email", userInfoEmail.text.toString())
                putExtra("profileImageUrl", "http://3.39.110.119$profileImageUrlIntent") // 이미지 URL 전달
            }
            startActivity(intent)
        }

        // 이전 버튼 클릭 시 마이페이지로 돌아감
        findViewById<ImageButton>(R.id.previous_button).setOnClickListener {
            finish()
        }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent?.let {
            // 전달된 데이터를 받아 UI 업데이트
            val updatedProfileImage = it.getStringExtra("updated_profile_image")
            val updatedName = it.getStringExtra("updated_name")
            val updatedNickname = it.getStringExtra("updated_nickname")
            val updatedAddress = it.getStringExtra("updated_address")
            val updatedEmail = it.getStringExtra("updated_email")

            // 로그로 전달된 데이터를 출력
            Log.d("ProfileViewActivity", "Updated Data (New Intent): ")
            Log.d("ProfileViewActivity", "updatedProfileImage: $updatedProfileImage")
            Log.d("ProfileViewActivity", "updatedName: $updatedName")
            Log.d("ProfileViewActivity", "updatedNickname: $updatedNickname")
            Log.d("ProfileViewActivity", "updatedEmail: $updatedEmail")
            Log.d("ProfileViewActivity", "updatedAddress: $updatedAddress")


            // UI를 업데이트하는 함수 호출
            updateProfileUI(updatedProfileImage, updatedName, updatedNickname, updatedAddress, updatedEmail)
        }
    }

    // UI 업데이트 로직 분리
    private fun updateProfileUI(
        profileImage: String?,
        name: String?,
        nickname: String?,
        address: String?,
        email: String?
    ) {
        // 프로필 이미지 업데이트
        profileImage?.let {
            val imageUrl = "http://3.39.110.119$it"
            Glide.with(this)
                .load(imageUrl)
                .error(R.drawable.default_profile_image)
                .circleCrop()
                .into(this.profileImage)
        } ?: run {
            this.profileImage.setImageResource(R.drawable.default_profile_image)
        }

        // 텍스트 업데이트
        name?.let { this.name.text = it; userInfoName.text = it }
        nickname?.let { this.nickname.text = it; userInfoNickname.text = it }
        email?.let { userInfoEmail.text = it }
        address?.let { userInfoRegion.text = it }
    }
}

