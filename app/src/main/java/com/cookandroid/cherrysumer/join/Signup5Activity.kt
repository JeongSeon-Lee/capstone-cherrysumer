package com.cookandroid.cherrysumer.join

import android.Manifest
import android.os.Bundle
import android.content.Intent
import android.widget.Button
import com.cookandroid.cherrysumer.R
import android.annotation.SuppressLint
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cookandroid.cherrysumer.LoginActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.overlay.Marker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query


class Signup5Activity : AppCompatActivity() {
    private val permissionRequest = 99
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var naverMap: NaverMap
    private var currentMarker: Marker? = null // 하나의 마커만 사용
    private lateinit var signupService: SignupService
    private lateinit var kakaoApi: KakaoApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join5)

        // FusedLocationProviderClient 초기화
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // NaverMap 초기화
        setupMap()

        // 위치 권한 요청
        requestLocationPermission()

        // Retrofit 초기화 (서버)
        val retrofit = Retrofit.Builder()
            .baseUrl("http://3.39.110.119/") // 서버 엔드포인트 설정
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        signupService = retrofit.create(SignupService::class.java)

        // Retrofit 초기화 (카카오 API)
        val kakaoRetrofit = Retrofit.Builder()
            .baseUrl("https://dapi.kakao.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        kakaoApi = kakaoRetrofit.create(KakaoApi::class.java)



        // 위치로 이동 버튼 클릭 리스너 설정
        findViewById<Button>(R.id.nextButton).setOnClickListener {
            sendSignupData()
        }

        findViewById<ImageButton>(R.id.previous_button).setOnClickListener {
            moveToPreviousPage()
        }

        // 내 위치로 이동 버튼
//        findViewById<Button>(R.id.btn_move_to_my_location).setOnClickListener {
//            moveToMyLocation()
//        }
    }

    // 주소를 가져오는 비동기 함수
    // 코루틴을 사용해 getAddressFromCoordinates를 호출
    private fun getAddressFromCoordinates(latitude: Double, longitude: Double) {

        // 코루틴을 사용해 비동기 API 호출 처리
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = kakaoApi.getAddress(longitude, latitude)

                if (response.isSuccessful) {
                    // B타입의 지역 정보를 필터링
                    val document = response.body()?.documents?.firstOrNull {
                        it.region_type == "B"
                    }

                    if (document != null) {
                        // 필요한 데이터 추출
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
                            UserData.region = region3depthName
                            UserData.regionCode = code
                        }
                    }
                } else {
                    // 오류 처리
                    withContext(Dispatchers.Main) {
                        Log.e("KakaoAddress", "Error: ${response.errorBody()?.string()}")
                        Toast.makeText(this@Signup5Activity, "주소 정보를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                // 네트워크 오류 처리
                withContext(Dispatchers.Main) {
                    Log.e("KakaoAddress", "API 호출 실패", e)
                    Toast.makeText(this@Signup5Activity, "주소 API 호출 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
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
        }
    }

//    private fun enableMapClickToMarkLocation() {
//        naverMap.setOnMapClickListener { _, latLng ->
//            // 사용자가 지도에서 다른 위치를 클릭할 때마다 하나의 마커만 유지
//            currentMarker?.map = null
//
//            // 클릭한 위치에 새 마커를 표시
//            currentMarker = Marker().apply {
//                position = latLng
//                map = naverMap
//            }
//
//            // 이 위치를 ViewModel에 저장
//            saveLocationToViewModel(latLng)
//
//            // 클릭한 위치의 주소를 카카오 API로 요청
//            getAddressFromCoordinates(latLng.latitude, latLng.longitude)
//        }
//    }

    // LatLng를 받는 메서드
    private fun saveLocationToViewModel(location: LatLng) {
        UserData.latitude = location.latitude.toString()   // 위도를 저장
        UserData.longitude = location.longitude.toString() // 경도를 저장
    }

//    // 내 위치로 이동 버튼 클릭 시 동작
//    private fun moveToMyLocation() {
//        if (isPermitted()) {
//            // 위치 서비스가 활성화되어 있는지 확인
//            checkLocationSettings()
//
//            startLocationUpdates() // 위치 업데이트 시작
//
//            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
//                    if (location != null) {
//                        val myLocation = LatLng(location.latitude, location.longitude)
//
//                        // 사용자의 현재 위치로 카메라 이동
//                        val cameraUpdate = CameraUpdate.scrollTo(myLocation).animate(CameraAnimation.Easing)
//                        naverMap.moveCamera(cameraUpdate)
//
//                        // 사용자의 위치에 마커 업데이트
//                        currentMarker?.map = null
//                        currentMarker = Marker().apply {
//                            position = myLocation
//                            map = naverMap
//                        }
//
//                        // 사용자의 위치를 ViewModel에 저장
//                        saveLocationToViewModel(myLocation)
//
//                        // 현재 위치의 주소를 카카오 API로 요청
//                        getAddressFromCoordinates(location.latitude, location.longitude)
//                    } else {
//                        // 위치를 찾을 수 없는 경우
//                        Toast.makeText(this, "현재 위치를 찾을 수 없습니다. 위치 서비스가 활성화되어 있는지 확인해주세요.", Toast.LENGTH_SHORT).show()
//                    }
//                }.addOnFailureListener {
//                    // 위치 요청 실패 처리
//                    Toast.makeText(this, "위치 요청에 실패했습니다.", Toast.LENGTH_SHORT).show()
//                }
//            } else {
//                Log.e("LocationUpdate", "Location permission not granted.")
//                Toast.makeText(this, "위치 권한을 허용하지 않으면 회원가입을 진행할 수 없습니다.", Toast.LENGTH_LONG).show()
//                moveToLoginScreen()
//            }
//        } else {
//            // 권한이 거부된 경우
//            Toast.makeText(this, "위치 권한을 허용하지 않으면 회원가입을 진행할 수 없습니다.", Toast.LENGTH_LONG).show()
//            moveToLoginScreen()
//        }
//    }

    // Activity 종료 시 LocationCallback 해제
    override fun onStop() {
        super.onStop()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    // 위치 서비스 활성화 확인
//    private fun checkLocationSettings() {
//        val locationRequest = LocationRequest.create().apply {
//            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//        }
//
//        val settingsClient = LocationServices.getSettingsClient(this)
//        val task = settingsClient.checkLocationSettings(LocationSettingsRequest.Builder()
//            .addLocationRequest(locationRequest).build())
//
//        task.addOnSuccessListener {
//            // 모든 요구 사항이 충족된 경우
//            Log.d("LocationSettings", "Location settings are satisfied.")
//        }.addOnFailureListener { exception ->
//            if (exception is ResolvableApiException) {
//                try {
//                    // 사용자가 설정을 변경하도록 요청
//                    exception.startResolutionForResult(this, 0)
//                } catch (sendEx: IntentSender.SendIntentException) {
//                    Log.e("LocationSettings", "Error resolving location settings.", sendEx)
//                }
//            }
//        }
//    }

    private fun sendSignupData() {
        val signupData = SignupData(
            loginId = UserData.loginId ?: "",
            password = UserData.password ?: "",
            name = UserData.name ?: "",
            nickname = UserData.nickname ?: "",
            email = UserData.email ?: "",
            category = UserData.category,
            region = UserData.region ?: "",
            regionCode = UserData.regionCode ?: "",
            longitude = UserData.longitude ?: "",
            latitude = UserData.latitude ?: ""
        )

        Log.d("SignupData", "Sending Signup Data: $signupData") // 로그 추가
        Log.d("SignupData", "Login ID: ${UserData.loginId}") // 로그 추가

        signupService.signup(signupData).enqueue(object : Callback<SignupResponse> {
            override fun onResponse(call: Call<SignupResponse>, response: Response<SignupResponse>) {
                when (response.code()) {
                    200 -> {
                        // 회원가입 성공
                        val intent = Intent(this@Signup5Activity, Signup6Activity::class.java)
                        startActivity(intent)
                    }
                    409 -> {
                        // 서버 오류 처리
                        Toast.makeText(this@Signup5Activity, "서버 오류: 나중에 다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(this@Signup5Activity, "알 수 없는 오류 발생 : ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<SignupResponse>, t: Throwable) {
                // 서버 통신 실패 처리
                Toast.makeText(this@Signup5Activity, "통신 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun moveToPreviousPage() {
        val intent = Intent(this, Signup4Activity::class.java)
        startActivity(intent)
    }

    private fun moveToLoginScreen() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // 현재 액티비티 종료
    }

    override fun onDestroy() {
        super.onDestroy()
        // Location updates 중지
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    // Retrofit API 인터페이스 정의
    interface SignupService {
        @POST("user/join") // 회원가입 엔드포인트
        fun signup(@Body signupData: SignupData): Call<SignupResponse>
    }

    interface KakaoApi {
        @GET("v2/local/geo/coord2regioncode.json")
        suspend fun getAddress(
            @Query("x") longitude: Double,
            @Query("y") latitude: Double,
            @Header("Authorization") authHeader: String = "KakaoAK e55a1db9b16f14b0df9400ef23025307"
        ): Response<KakaoResponse>
    }

    data class KakaoResponse(
        val documents: List<Document>
    )

    data class Document(
        val region_type: String,
        val address_name: String,
        val region_1depth_name: String,
        val region_2depth_name: String,
        val region_3depth_name: String,
        val code: String,
        val x: Double,
        val y: Double
    )

    // 회원가입 데이터 클래스
    data class SignupData(
        val loginId: String,
        val password: String,
        val name: String,
        val nickname: String,
        val email: String,
        val category: List<String>,
        val region: String,
        val regionCode: String,    // regionCode 추가
        val longitude: String,     // longitude 추가
        val latitude: String       // latitude 추가
    )

    // 서버 응답 데이터 클래스
    data class SignupResponse(
        val isSuccess: Boolean,
        val code: String,
        val message: String
    )
}