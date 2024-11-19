package com.cookandroid.cherrysumer

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.marginTop
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import com.google.gson.GsonBuilder
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar


class PostWriteFragment : Fragment() {

    private lateinit var titleEditText: EditText
    private lateinit var itemNameEditText: EditText
    private lateinit var priceEditText: EditText
    private lateinit var personnelEditText: EditText
    private lateinit var pricePerPersonTextView: TextView
    private lateinit var dateEditText: EditText
    private lateinit var timeEditText: EditText
    private lateinit var placeEditText: EditText
    private val selectedCategories = mutableListOf<String>()
    private lateinit var categoryBox: LinearLayout
    private lateinit var detailCategoryEditText: EditText
    private lateinit var detailCategoryContainer: LinearLayout
    private val addedCategories = mutableListOf<String>()
    private lateinit var regButton: Button
    private lateinit var descriptionEditText: EditText
    private lateinit var writeButton: Button
    private val MAX_IMAGE_COUNT = 3
    private var currentImageCount = 0
    private lateinit var imageContainer: LinearLayout
    private lateinit var imageCountText: TextView
    private val imageUris = mutableListOf<Uri>()

    private lateinit var postWriteService: PostWriteService

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_post_write, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        val sharedPreferences = requireActivity().getSharedPreferences("CherrySumerprefs", AppCompatActivity.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(requireContext(), token))
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://3.39.110.119/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .build()

        postWriteService = retrofit.create(PostWriteService::class.java)

        // Initialize UI components
        titleEditText = view.findViewById(R.id.title_input)
        itemNameEditText = view.findViewById(R.id.item_name_input)
        priceEditText = view.findViewById(R.id.price_input)
        personnelEditText = view.findViewById(R.id.personnel_input)
        pricePerPersonTextView = view.findViewById(R.id.price_per_person)
        dateEditText = view.findViewById(R.id.date_input)
        timeEditText = view.findViewById(R.id.time_input)
        placeEditText = view.findViewById(R.id.place_input)
        detailCategoryEditText = view.findViewById(R.id.category_input)
        writeButton = view.findViewById(R.id.write_button)
        regButton = view.findViewById(R.id.reg_button)
        detailCategoryContainer = view.findViewById(R.id.detail_category_container)
        descriptionEditText = view.findViewById(R.id.description_input)
        imageContainer = view.findViewById(R.id.image_container)
        imageCountText = view.findViewById(R.id.image_count)
        categoryBox = view.findViewById(R.id.category_box)

        // 날짜 선택
        dateEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                // 월은 0부터 시작하므로 +1을 해줌
                val formattedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                dateEditText.setText(formattedDate)
            }, year, month, day)
            datePicker.show()
        }

        // 시간 선택
        timeEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePicker = TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                timeEditText.setText(formattedTime)
            }, hour, minute, true)  // true는 24시간 형식 사용 여부
            timePicker.show()
        }

        // 카테고리 버튼들을 반복문을 통해 설정
        setupCategoryButtons()

        // Set up image picker
        view.findViewById<LinearLayout>(R.id.image_select_icon).setOnClickListener {
            if (currentImageCount < MAX_IMAGE_COUNT) {
                openImagePicker()
            } else {
                Toast.makeText(requireContext(), "이미지 최대 개수는 3개입니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up price calculation on focus change
        priceEditText.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) calculatePricePerPerson() }
        personnelEditText.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) calculatePricePerPerson() }

        // Register category
        regButton.setOnClickListener { addCategory() }

        // Submit post
        writeButton.setOnClickListener { submitPost() }
    }

    // 카테고리 버튼들을 반복문을 통해 설정하는 함수
    private fun setupCategoryButtons() {
        for (i in 0 until categoryBox.childCount) {
            val textView = categoryBox.getChildAt(i) as? TextView
            textView?.let {
                setupCategoryButton(it, it.text.toString()) // 텍스트를 카테고리 이름으로 사용
            }
        }
    }

    // 각 카테고리 태그 버튼을 설정하는 함수
    private fun setupCategoryButton(textView: TextView, category: String) {
        val defaultBackground = ContextCompat.getDrawable(requireContext(), R.drawable.post_category_select_button)
        val selectedBackground = ContextCompat.getDrawable(requireContext(), R.drawable.post_category_selected_button)
        val defaultTextColor = ContextCompat.getColor(requireContext(), R.color.black)
        val selectedTextColor = ContextCompat.getColor(requireContext(), R.color.cherry)

        // 현재 패딩 값 저장
        val paddingLeft = textView.paddingLeft
        val paddingTop = textView.paddingTop
        val paddingRight = textView.paddingRight
        val paddingBottom = textView.paddingBottom

        // 초기 상태 설정
        textView.setBackground(defaultBackground) // 배경만 설정
        textView.setTextColor(defaultTextColor)   // 텍스트 색상만 설정

        // 기존 패딩 값 유지
        textView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)

        textView.setOnClickListener {
            if (selectedCategories.contains(category)) {
                // 선택 해제
                selectedCategories.remove(category)
                textView.setBackground(defaultBackground)  // 배경만 초기화
                textView.setTextColor(defaultTextColor)    // 텍스트 색상만 초기화
            } else {
                // 선택
                selectedCategories.add(category)
                textView.setBackground(selectedBackground) // 배경만 변경
                textView.setTextColor(selectedTextColor)    // 텍스트 색상만 변경
            }

            // 선택 상태에 따라 패딩 값 유지
            textView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
        }
    }

    // 1. ActivityResultLauncher와 ActivityResultContracts 정의
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let {
                if (it.clipData != null) {
                    val count = it.clipData!!.itemCount
                    for (i in 0 until count) {
                        if (currentImageCount < MAX_IMAGE_COUNT) {
                            val imageUri = it.clipData!!.getItemAt(i).uri
                            addImageToContainer(imageUri)
                        } else {
                            Toast.makeText(requireContext(), "이미지는 최대 3개까지만 선택 가능합니다.", Toast.LENGTH_SHORT).show()
                            break
                        }
                    }
                } else if (it.data != null && currentImageCount < MAX_IMAGE_COUNT) {
                    addImageToContainer(it.data!!)
                } else {
                    Toast.makeText(requireContext(), "이미지는 최대 3개까지만 선택 가능합니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 2. 이미지 선택을 위한 Intent 시작
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // 여러 개 선택 가능
        imagePickerLauncher.launch(intent)  // ActivityResultLauncher로 이미지 선택 시작
    }

    // 이미지를 컨테이너에 추가하는 함수
    private fun addImageToContainer(imageUri: Uri) {
        imageUris.add(imageUri)

        // ImageView 생성
        val imageView = ImageView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(68.dp, 68.dp).apply { marginEnd = 8.dp }
            setImageURI(imageUri)
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = ContextCompat.getDrawable(requireContext(), R.drawable.image_radius_20dp)
            tag = imageUri  // tag에 imageUri 저장
        }

        // 'X' 버튼 생성
        val deleteButton = ImageView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(24.dp, 24.dp).apply {
                gravity = Gravity.TOP or Gravity.END
                marginEnd = 4.dp
                topMargin = 4.dp
            }
            setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_image_cancle))  // 'X' 아이콘 사용
            setOnClickListener {
                removeImage(imageUri)  // 삭제 시 해당 이미지 URI 전달
            }
        }

        // 이미지와 'X' 버튼을 포함하는 FrameLayout 생성
        val container = FrameLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(68.dp, 68.dp).apply { marginEnd = 10.dp }
            addView(imageView)
            addView(deleteButton)
            tag = imageUri  // container의 tag에 imageUri 저장 (삭제 시 찾기 용이)
        }

        // 이미지를 container에 추가
        imageContainer.addView(container)

        // 이미지 개수 업데이트
        currentImageCount++
        imageCountText.text = "$currentImageCount/$MAX_IMAGE_COUNT"
    }

    // 이미지 삭제 함수
    private fun removeImage(imageUri: Uri) {
        // imageUris에서 imageUri 삭제
        imageUris.remove(imageUri)

        // imageContainer에서 해당 이미지가 포함된 container를 삭제
        for (i in 0 until imageContainer.childCount) {
            val container = imageContainer.getChildAt(i) as? FrameLayout
            val imageView = container?.getChildAt(0) as? ImageView
            // container의 tag가 imageUri와 일치하는지 확인
            if (imageView?.tag == imageUri) {
                imageContainer.removeViewAt(i)  // container 삭제
                break
            }
        }

        // 이미지 개수 업데이트
        currentImageCount--
        imageCountText.text = "$currentImageCount/$MAX_IMAGE_COUNT"
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()

    private fun calculatePricePerPerson() {
        val priceText = priceEditText.text.toString()
        val personnelText = personnelEditText.text.toString()
        if (priceText.isNotEmpty() && personnelText.isNotEmpty()) {
            val totalPrice = priceText.toIntOrNull()
            val personnelCount = personnelText.toIntOrNull()
            if (totalPrice != null && personnelCount != null && personnelCount > 0) {
                pricePerPersonTextView.text = "1인당 부담 가격 : ${totalPrice / personnelCount}원"
            } else {
                pricePerPersonTextView.text = "1인당 부담 가격 : 계산할 수 없음"
            }
        } else {
            pricePerPersonTextView.text = "1인당 부담 가격 : "
        }
    }

    private fun addCategory() {
        val categoryText = detailCategoryEditText.text.toString().trim()
        if (categoryText.isNotEmpty()) {
            val inflater = LayoutInflater.from(requireContext())
            val categoryView = inflater.inflate(R.layout.detail_category_items, detailCategoryContainer, false)
            val categoryTextView = categoryView.findViewById<TextView>(R.id.add_category)
            categoryTextView.text = categoryText
            categoryTextView.setOnClickListener {
                detailCategoryContainer.removeView(categoryView)
                addedCategories.remove(categoryText)
            }
            detailCategoryContainer.addView(categoryView)
            addedCategories.add(categoryText)
            detailCategoryEditText.text.clear()
        }
    }

    private fun submitPost() {
        val title = titleEditText.text.toString()
        val productName = itemNameEditText.text.toString()
        val price = priceEditText.text.toString().toIntOrNull()
        val capacity = personnelEditText.text.toString().toIntOrNull()
        val dateInput = dateEditText.text.toString()
        val timeInput = timeEditText.text.toString()
        val date = "${dateInput}T${timeInput}:00"
        val place = placeEditText.text.toString()
        val content = descriptionEditText.text.toString()
        val selectedCategoryList = selectedCategories.toList()
        val detailedCategory = detailCategoryEditText.text.toString()
            .takeIf { it.isNotBlank() }
            ?.split(",")
            ?.map { it.trim() }

        // 날짜 형식 확인 (YYYY-DD-MM 형식)
        val datePattern = "^\\d{4}-\\d{2}-\\d{2}$".toRegex()
        if (!dateInput.matches(datePattern)) {
            Toast.makeText(requireContext(), "날짜는 YYYY-MM-DD 형식으로 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // 시간 형식 확인 (HH:MM 형식)
        val timePattern = "^([01]?[0-9]|2[0-3]):([0-5]?[0-9])$".toRegex()
        if (!timeInput.matches(timePattern)) {
            Toast.makeText(requireContext(), "시간은 HH:MM 형식으로 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // 필수 입력 값 확인
        if (title.isBlank() || productName.isBlank() || price == null || capacity == null ||
            date.isBlank() || place.isBlank() || content.isBlank() || selectedCategoryList.isEmpty()) {
            Toast.makeText(requireContext(), "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val json = JSONObject().apply {
            put("title", title)
            put("productname", productName)
            put("price", price)
            put("capacity", capacity)
            put("date", date)
            put("place", place)
            put("content", content)
            put("category", JSONArray(selectedCategoryList))
            put("detailed_category", JSONArray(detailedCategory))
        }


        // Log the JSON request body
        Log.d("PostWrite", "Request JSON: $json")


        val request = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val imageParts = imageUris.mapNotNull { uri ->
            uri?.let {
                val file = uriToFile(it)
                Log.d("PostWrite", "Image File: ${file.name}, Path: ${file.absolutePath}")
                MultipartBody.Part.createFormData("file", file.name, RequestBody.create("image/*".toMediaTypeOrNull(), file))
            }
        }

        // Log the number of images to be uploaded
        Log.d("PostWrite", "Number of images to upload: ${imageParts.size}")
        Log.d("PostWrite", "Request JSON: $json")
        Log.d("PostWrite", "Request Image Parts: $imageParts")

        postWriteService.createPost(request, imageParts).enqueue(object : Callback<PostWriteResponse> {
            override fun onResponse(call: Call<PostWriteResponse>, response: Response<PostWriteResponse>) {
                val statusCode = response.code()
                Log.d("PostWrite", "Response Status Code: $statusCode")
                if (response.isSuccessful) {
                    val responseData = response.body()
                    Log.d("PostWrite", "Response Success: ${response.body()}")

                    val rePostData = responseData?.data
                    val bundle = Bundle().apply {
                        putLong("postId", rePostData?.postId ?: 0L)
                        putString("title", rePostData?.title ?: "")
                        putString("productname", rePostData?.productname ?: "")
                        putInt("price", rePostData?.price ?: 0)
                        putString("place", rePostData?.place ?: "")
                        putString("date", rePostData?.date ?: "")
                        putString("imageUrl", rePostData?.imageUrl ?: "")
                    }
                    // 성공 시 '업로드 성공' 프래그먼트로 이동
                    val fragment = UploadSuccessFragment() // 업로드 성공을 표시하는 프래그먼트
                    fragment.arguments = bundle

                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment) // 프래그먼트 컨테이너를 새 프래그먼트로 교체
                        .commit() // 트랜잭션을 커밋하여 화면에 반영
                } else {
                    Log.e("PostWrite", "Error Response: ${response.errorBody()?.string()}")
                    Toast.makeText(requireContext(), "서버 오류: 다시 시도하세요.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PostWriteResponse>, t: Throwable) {
                Log.e("PostWrite", "Request Failed: ${t.localizedMessage}")
                Toast.makeText(requireContext(), "게시물 등록에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val file = File(requireContext().cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { outputStream ->
            inputStream?.copyTo(outputStream)
        }
        // Log the URI and file path
        Log.d("PostWrite", "URI to File: $uri, File Path: ${file.absolutePath}")
        return file
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 1
    }
}

// 응답 데이터 클래스
data class PostWriteResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val data: RePostData? // 성공 시 데이터, 실패 시에는 null
)

// 성공 시 데이터를 담을 클래스
data class RePostData(
    val postId: Long,
    val title: String,
    val productname: String,
    val price: Int,
    val place: String,
    val date: String,
    val imageUrl: String
)

// Retrofit API interface
interface PostWriteService {
    @Multipart
    @POST("posts")
    fun createPost(
        @Part("request") jsonPart: RequestBody,
        @Part files: List<MultipartBody.Part>
    ): Call<PostWriteResponse>
}