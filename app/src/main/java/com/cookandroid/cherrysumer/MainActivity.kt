package com.cookandroid.cherrysumer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.cookandroid.cherrysumer.databinding.ActivityMainBinding
import com.cookandroid.cherrysumer.mypage.LikePostDetailData
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomNavigationView = findViewById(R.id.bottom_navigation)

        // Intent로부터 사용자 데이터 받기
        val region = intent.getStringExtra("region")
        val name = intent.getStringExtra("name")

        val postDetailsJson = intent.getStringExtra("postDetailsJson")
        val previousActivity = intent.getStringExtra("previousActivity")

        // postDetailsJson이 null이 아니면 PostDetailFragment 띄우기
        if (postDetailsJson != null) {
            // PostDetailFragment로 데이터 전달
            val postDetailFragment = PostDetailFragment().apply {
                arguments = Bundle().apply {
                    putString("postDetailsJson", postDetailsJson)  // 그대로 JSON 전달
                    putString("previousActivity", previousActivity)
                }
            }

            // fragment_container에 PostDetailFragment 띄우기
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, postDetailFragment)
                .addToBackStack(null)
                .commit()
        } else {
            // postDetailsJson이 null이면 홈 화면 띄우기
            val homeFragment = HomeFragment().apply {
                arguments = Bundle().apply {
                    putString("region", region)
                    putString("name", name)
                }
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, homeFragment)
                .commit()
        }



        // 네비게이션 바 클릭 리스너 설정
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // 홈 프래그먼트로 전환
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment().apply {
                            arguments = Bundle().apply {
                                putString("region", region)
                                putString("name", name)
                            }
                        })
                        .commit()
                    showBottomNavigationView() // 네비게이션 바 보이기
                    true
                }
                R.id.nav_posts -> {
                    // 게시글 프래그먼트로 전환
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, PostWriteFragment())
                        .commit()
                    showBottomNavigationView() // 네비게이션 바 보이기
                    true
                }
//                R.id.nav_chats -> {
//                    // 채팅 프래그먼트로 전환
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.nav_content, ChatsFragment())
//                        .commit()
//                    true
//                }
//                R.id.nav_inventory -> {
//                    // 재고 프래그먼트로 전환
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.nav_content, InventoryFragment())
//                        .commit()
//                    true
//                }
                R.id.nav_mypage -> {
                    // 마이페이지 프래그먼트로 전환
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, MyPageFragment())
                        .commit()
                    showBottomNavigationView()
                    true
                }
                else -> {
                    hideBottomNavigationView() // 그 외의 프래그먼트에서는 네비게이션 바 숨기기
                    false
                }
            }
        }


        val goToMyPage = intent.getBooleanExtra("goToMyPage", false)

        if (goToMyPage) {
            // MyPageFragment로 이동
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MyPageFragment())
                .commit()
            binding.bottomNavigation.selectedItemId = R.id.nav_mypage // 네비게이션 바에 표시
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            true
        } else {
            super.onSupportNavigateUp()
        }
    }

    // 네비게이션 바 숨기기
    fun hideBottomNavigationView() {
        binding.bottomNavigation.visibility = View.GONE
    }

    // 네비게이션 바 보이기
    fun showBottomNavigationView() {
        binding.bottomNavigation.visibility = View.VISIBLE
    }
}