package com.cookandroid.cherrysumer.mypage

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cookandroid.cherrysumer.R
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.cookandroid.cherrysumer.AuthInterceptor
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.MultipartBody
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody.Part
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PartMap
import java.io.File

class ProfileEditActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var nicknameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var profileImageView: ImageView
    private lateinit var profileImageEditButton: ImageButton
    private lateinit var submitButton: Button
    private lateinit var profileEditService: ProfileEditService
    private lateinit var previousButton: ImageButton

    // 선택된 이미지의 URI를 저장할 변수
    private var selectedImageUri: Uri? = null

    private val IMAGE_PICK_REQUEST = 1 // 갤러리에서 이미지 선택을 위한 요청 코드


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 액티비티의 전체 배경색을 하얀색으로 설정
        window.decorView.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        setContentView(R.layout.activity_mypage_profile_edit)


        // Retrofit 초기화
        val token = getToken()
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(this, token))
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

        profileEditService = retrofit.create(ProfileEditService::class.java)

        nameEditText = findViewById(R.id.name_input)
        nicknameEditText = findViewById(R.id.nickname_input)
        emailEditText = findViewById(R.id.email_input)
        profileImageView = findViewById(R.id.user_image)
        profileImageEditButton = findViewById(R.id.edit_button)
        submitButton = findViewById(R.id.submit_button)
        previousButton = findViewById(R.id.previous_button)

        // Intent로 전달된 데이터 받기
        val name = intent.getStringExtra("name")
        val nickname = intent.getStringExtra("nickname")
        val email = intent.getStringExtra("email")
        val profileImageUrl = intent.getStringExtra("profileImageUrl")

        name?.let { nameEditText.setText(it) }
        nickname?.let { nicknameEditText.setText(it) }
        email?.let { emailEditText.setText(it) }

        // 프로필 이미지 URL이 "null" 문자열을 포함하거나 비어있으면 기본 이미지로 설정
        Log.d("ProfileEdit", "profileImageUrl: $profileImageUrl") // 로그로 URL 확인
        if (!profileImageUrl.isNullOrEmpty() && !profileImageUrl.contains("null")) {
            Glide.with(this)
                .load(profileImageUrl) // URL로 이미지 로드
                .circleCrop() // 원형으로 이미지 자르기
                .error(R.drawable.default_profile_image) // 오류 시 기본 이미지 로드
                .into(profileImageView)
        } else {
            // 잘못된 URL 또는 null인 경우 기본 이미지 설정
            profileImageView.setImageResource(R.drawable.default_profile_image)
        }

        Log.d("ProfileEdit", "profileImageUrl: $profileImageUrl")

        // profile_image 클릭 시 갤러리 열기
        profileImageView.setOnClickListener {
            openGallery()
        }

        profileImageEditButton.setOnClickListener{
            openGallery()
        }

        previousButton.setOnClickListener {
            finish() // 현재 액티비티 종료
        }

        submitButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val nickname = nicknameEditText.text.toString()
            val email = emailEditText.text.toString()

            // 입력 값 검사
            if (name.isEmpty() || nickname.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 서버에 요청할 데이터 생성
            val dto = mapOf(
                "name" to name,
                "nickname" to nickname,
                "email" to email
            )
            val jsonString = GsonBuilder().create().toJson(dto)
            val jsonPart = RequestBody.create("application/json".toMediaTypeOrNull(), jsonString)

            // 1. ImageView에서 Bitmap 가져오기
            val bitmap = profileImageView.drawable?.toBitmap() // Drawable에서 Bitmap을 가져오되, null이 가능하도록 처리

// 2. Bitmap이 null이거나 잘못된 URL일 경우, null로 처리하여 서버에 전달
            val filePart: MultipartBody.Part? = if (bitmap != null) {
                // Bitmap이 null이 아니면 Bitmap을 File로 변환
                val imageFile = bitmapToFile(bitmap, "profile_image.jpg")

                // 파일을 Multipart로 변환
                imageFile?.let { file ->
                    val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                    MultipartBody.Part.createFormData("file", file.name, requestFile)
                }
            } else {
                // Bitmap이 없거나 잘못된 URL이 있을 경우, null로 처리
                null
            }

// 3. profileImageUrl이 "null" 문자열을 포함하는지 확인하고, 그런 경우도 null로 처리
            val profileImageUrl = intent.getStringExtra("profileImageUrl")
            val isInvalidImageUrl = profileImageUrl?.contains("null") == true || profileImageUrl?.isEmpty() == true

            if (isInvalidImageUrl) {
                // 잘못된 URL인 경우 null로 처리
                null
            }

            // 4. 서버로 요청 보내기
            val call = profileEditService.modifyProfile(jsonPart, filePart)
            call.enqueue(object : Callback<ModifyResponse> {
                override fun onResponse(call: Call<ModifyResponse>, response: Response<ModifyResponse>) {
                    if (response.isSuccessful && response.body()?.isSuccess == true) {
                        // 서버에서 반환된 데이터 가져오기
                        val data = response.body()?.data
                        val updatedProfileImage = data?.profileImageUrl
                        val updatedName = data?.name
                        val updatedNickname = data?.nickname
                        val updateEmail = data?.email
                        val updatedAddress = data?.region

                        Toast.makeText(this@ProfileEditActivity, "수정사항이 적용되었습니다.", Toast.LENGTH_SHORT).show()

                        // Intent에 데이터를 담아 이전 화면으로 전달
                        val intent = Intent(this@ProfileEditActivity, ProfileViewActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra("profileImageUrl", updatedProfileImage)
                            putExtra("name", updatedName)
                            putExtra("nickname", updatedNickname)
                            putExtra("region", updatedAddress)
                            putExtra("email", updateEmail )
                        }
                        startActivity(intent)
                        finish() // 현재 액티비티 종료
                    } else {
                        // 내부 상태 디버깅용 로그 추가
                        val currentName = nameEditText.text.toString()
                        val currentNickname = nicknameEditText.text.toString()
                        val currentEmail = emailEditText.text.toString()
                        val currentImageUri = selectedImageUri?.toString() ?: profileImageUrl

                        // 서버 응답을 로그로 출력
                        val errorBody = response.errorBody()?.string() // 에러 응답의 내용을 String으로 변환
                        Log.e("ProfileEditError", "프로필 수정 실패: 응답 코드: ${response.code()}, 오류 메시지: $errorBody")
                        Log.e("ProfileEditError", "프로필 수정 실패: 응답 코드: ${response.code()}")
                        Log.e("ProfileEditError", "현재 입력 상태 - 이름: $currentName, 닉네임: $currentNickname, 이메일: $currentEmail, 이미지: $currentImageUri")
                        Toast.makeText(this@ProfileEditActivity, "프로필 수정에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ModifyResponse>, t: Throwable) {
                    Toast.makeText(this@ProfileEditActivity, "네트워크 오류. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                }
            })
        }

    }

    // Bitmap을 File로 저장하는 함수
    private fun bitmapToFile(bitmap: Bitmap, fileName: String): File? {
        return try {
            val file = File(cacheDir, fileName)
            file.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // SharedPreferences에서 토큰을 가져옴
    private fun getToken(): String? {
        val sharedPreferences = getSharedPreferences("CherrySumerprefs", MODE_PRIVATE)
        return sharedPreferences.getString("token", null)
    }

    // 갤러리 열기
    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    // ActivityResultLauncher 초기화
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
                try {
                    Glide.with(this)
                        .load(uri)
                        .circleCrop()
                        .into(profileImageView)

                } catch (e: Exception) {
                    Toast.makeText(this, "이미지를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                    profileImageView.setImageResource(R.drawable.default_profile_image)
                }
            } else {
            }
        }

//    // 갤러리에서 이미지 선택 후 처리
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_REQUEST) {
//            val selectedImageUri = data?.data
//
//            selectedImageUri?.let {
//                try {
//                    // Convert Uri to File
//                    val selectedFile = getSelectedImageFile(it)
//
//                    selectedFile?.let { file ->
//                        // You now have the File object for the selected image
//                        Glide.with(this)
//                            .load(file)
//                            .circleCrop()
//                            .into(profileImageView)
//                    } ?: run {
//                        // Handle the case where the file could not be obtained
//                        Toast.makeText(this, "이미지 파일을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
//                        profileImageView.setImageResource(R.drawable.default_profile_image)
//                    }
//
//                } catch (e: Exception) {
//                    // Handle error
//                    Toast.makeText(this, "이미지를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
//                    profileImageView.setImageResource(R.drawable.default_profile_image)
//                }
//            }
//        }
//    }

    fun getFileFromUri(uri: Uri): File? {
        val filePath = getRealPathFromUri(uri) // URI를 파일 경로로 변환
        return if (filePath != null) File(filePath) else null
    }

    fun getRealPathFromUri(uri: Uri): String? {
        val cursor = contentResolver.query(uri, arrayOf(MediaStore.Images.Media.DATA), null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                return it.getString(columnIndex)
            }
        }
        return null
    }

    // 선택된 이미지 파일을 가져오는 함수
    private fun getSelectedImageFile(uri: Uri): File? {
        val cursor = contentResolver.query(uri, arrayOf(MediaStore.Images.Media.DATA), null, null, null)

        cursor?.let {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(MediaStore.Images.Media.DATA)
                val filePath = it.getString(columnIndex)
                return File(filePath)
            }
            it.close()
        }

        return null
    }
}


data class ModifyResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val data: ModifyData?
)

data class ModifyData(
    val name: String,
    val nickname: String,
    val email: String,
    val region: String,
    val profileImageUrl: String
)


interface ProfileEditService {
    @Multipart
    @POST("mypage/profile/modify")
    fun modifyProfile(
        @retrofit2.http.Part("dto") jsonPart: RequestBody, // String과 Any 타입을 사용할 수 있도록 수정
        @retrofit2.http.Part file: MultipartBody.Part? // MultipartBody.Part를 사용하여 파일을 전달 (파일 하나만)
    ): Call<ModifyResponse>
}