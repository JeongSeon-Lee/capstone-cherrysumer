package com.cookandroid.cherrysumer.mypage

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import retrofit2.Callback
import retrofit2.Call
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cookandroid.cherrysumer.AuthInterceptor
import com.cookandroid.cherrysumer.R
import com.cookandroid.cherrysumer.join.Signup5Activity
import com.cookandroid.cherrysumer.join.UserData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.gson.GsonBuilder
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

class MyRegionSettingActivity : AppCompatActivity() {
    private val permissionRequest = 99
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var naverMap: NaverMap
    private var currentMarker: Marker? = null // 하나의 마커만 사용
    private lateinit var myResionService: MyRegionService
    private lateinit var kakaoApi: KakaoApi
    private lateinit var myRegionTextView: TextView
    private lateinit var submitButton: Button
    private lateinit var previousButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 액티비티의 전체 배경색을 하얀색으로 설정
        window.decorView.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        setContentView(R.layout.activity_mypage_region)

        myRegionTextView = findViewById(R.id.my_region)
        submitButton = findViewById(R.id.submit_button)
        previousButton = findViewById(R.id.previous_button)

        // FusedLocationProviderClient 초기화
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // NaverMap 초기화
        setupMap()

        // 위치 권한 요청
        requestLocationPermission()

        val sharedPreferences = getSharedPreferences("CherrySumerprefs", AppCompatActivity.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)

// Retrofit 초기화
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(this, token)) // Activity의 context와 token 전달
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

        myResionService = retrofit.create(MyRegionService::class.java)

        // Retrofit 초기화 (카카오 API)
        val kakaoRetrofit = Retrofit.Builder()
            .baseUrl("https://dapi.kakao.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        kakaoApi = kakaoRetrofit.create(KakaoApi::class.java)

        previousButton.setOnClickListener {
            finish()
        }

        submitButton.setOnClickListener {
            val region = UserData.region
            val regionCode = UserData.regionCode
            val latitude = currentMarker?.position?.latitude.toString()
            val longitude = currentMarker?.position?.longitude.toString()

            // Send the data to the server
            if (region != null && regionCode != null && latitude != null && longitude != null) {
                submitRegionData(region, regionCode, latitude, longitude)
            } else {
                Toast.makeText(this, "위치 정보가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 주소를 가져오는 비동기 함수
    // 코루틴을 사용해 getAddressFromCoordinates를 호출
    private fun getAddressFromCoordinates(latitude: Double, longitude: Double) {

        // 코루틴을 사용해 비동기 API 호출 처리
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = kakaoApi.getRegion(longitude, latitude)

                if (response.isSuccessful) {
                    // B타입의 지역 정보를 필터링
                    val document = response.body()?.documents?.firstOrNull {
                        it.region_type == "B"
                    }

                    if (document != null) {
                        // 필요한 데이터 추출
                        val region1depthName = document.region_1depth_name
                        val region2depthName = document.region_2depth_name
                        val region3depthName = document.region_3depth_name
                        val code = document.code
                        val x = document.x
                        val y = document.y

                        // 메인 스레드에서 UI 작업을 처리하기 위해 Dispatchers.Main 사용
                        withContext(Dispatchers.Main) {
                            // 로그 출력 (UI 변경도 여기서 가능)
                            Log.d("KakaoAddress", "Region 3 Depth Name: $region3depthName")
                            Log.d("KakaoAddress", "Code: $code")
                            Log.d("KakaoAddress", "Coordinates: ($x, $y)")

                            // 받아온 주소 정보를 ViewModel에 저장
                            myRegionTextView.text = "$region1depthName $region2depthName $region3depthName"
                            UserData.region = region3depthName
                            UserData.regionCode = code
                            UserData.latitude = x.toString()
                            UserData.longitude = y.toString()
                        }
                    }
                } else {
                    // 오류 처리
                    withContext(Dispatchers.Main) {
                        Log.e("KakaoAddress", "Error: ${response.errorBody()?.string()}")
                        Toast.makeText(this@MyRegionSettingActivity, "주소 정보를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                // 네트워크 오류 처리
                withContext(Dispatchers.Main) {
                    Log.e("KakaoAddress", "API 호출 실패", e)
                    Toast.makeText(this@MyRegionSettingActivity, "주소 API 호출 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                supportFragmentManager.beginTransaction().add(R.id.map, it).commit()
            }

        mapFragment.getMapAsync { naverMap ->
            this.naverMap = naverMap
            setupLocationServices()

            // Enable map tap to mark a location
//            enableMapClickToMarkLocation()

            // 지도 클릭 이벤트 제거
            disableMapClick()
        }
    }

    private fun setupLocationServices() {
        if (isPermitted()) {
            startLocationUpdates()
        } else {
            requestLocationPermission()
        }
    }

    private fun disableMapClick() {
        // 지도 클릭 이벤트 비활성화
        naverMap.setOnMapClickListener(null)
    }

    private fun isPermitted(): Boolean {
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        for (perm in permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    // 위치 권한 요청 메서드
    private fun requestLocationPermission() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없으면 요청
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), permissionRequest)
        } else {
            // 권한이 이미 허용된 경우 위치 업데이트 시작
            startLocationUpdates()
        }
    }

    @SuppressLint("MissingPermission")
    // 위치 업데이트를 시작하는 메서드
    private fun startLocationUpdates() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).apply {
                setMaxUpdateDelayMillis(5000) // 5초마다 업데이트
            }.build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    if (locationResult.locations.isNotEmpty()) {
                        val location = locationResult.lastLocation
                        // 위치 결과가 있을 경우 업데이트
                        updateMapLocation(location)
                    } else {
                        Log.e("LocationUpdate", "위치 업데이트 결과가 없습니다.")
                    }
                }
            }

            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            Log.e("LocationUpdates", "Permission not granted.")
        }
    }

    private fun updateMapLocation(location: Location?) {
        location?.let {
            val myLocation = LatLng(it.latitude, it.longitude)

            // 사용자의 현재 위치에 마커 표시, 기존 마커 제거 후 새 마커 표시
            currentMarker?.map = null
            currentMarker = Marker().apply {
                position = myLocation
                map = naverMap
            }

            // 카메라를 사용자의 위치로 이동
            val cameraUpdate = CameraUpdate.scrollTo(myLocation).animate(CameraAnimation.Easing)
            naverMap.moveCamera(cameraUpdate)

            // Fetch address information based on the location
            getAddressFromCoordinates(it.latitude, it.longitude)
        }
    }

    private fun submitRegionData(region: String, regionCode: String, latitude: String, longitude: String) {
        // RegionRequest 객체 생성
        val regionRequest = RegionRequest(
            region = region,
            regionCode = regionCode,
            latitude = latitude,
            longitude = longitude
        )

        myResionService.submitRegionData(regionRequest).enqueue(object : Callback<MyRegionResponse> {
            override fun onResponse(call: Call<MyRegionResponse>, response: Response<MyRegionResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!
                    if (responseBody.isSuccess) {
                        Toast.makeText(this@MyRegionSettingActivity, responseBody.message, Toast.LENGTH_SHORT).show()
                        finish() // Activity 종료
                    } else {
                        Toast.makeText(this@MyRegionSettingActivity, "서버 오류: ${responseBody.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MyRegionSettingActivity, "서버 오류", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<MyRegionResponse>, t: Throwable) {
                Toast.makeText(this@MyRegionSettingActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onStop() {
        super.onStop()
        if (::locationCallback.isInitialized) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Location updates 중지
        if (::locationCallback.isInitialized) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }
}


data class KakaoMapResponse(
    val documents: List<RegionDocument>
)

data class RegionDocument(
    val region_type: String,
    val address_name: String,
    val region_1depth_name: String,
    val region_2depth_name: String,
    val region_3depth_name: String,
    val code: String,
    val x: Double,
    val y: Double
)

data class RegionRequest(
    val region: String,
    val regionCode: String,
    val longitude: String,
    val latitude: String
)

data class MyRegionResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String
)


interface KakaoApi {
    @GET("v2/local/geo/coord2regioncode.json")
    suspend fun getRegion(
        @Query("x") longitude: Double,
        @Query("y") latitude: Double,
        @Header("Authorization") authHeader: String = "KakaoAK e55a1db9b16f14b0df9400ef23025307"
    ): Response<KakaoMapResponse>
}

interface MyRegionService {
    @POST("mypage/region")
    fun submitRegionData(
        @Body requestBody: RegionRequest
    ): Call<MyRegionResponse>
}

