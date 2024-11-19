package com.example.cherrysumer

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.example.cherrysumer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // 어두운 텍스트
        }

        setupActiveNavIcons(R.id.nav_home)

        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    setupActiveNavIcons(item.itemId)
                    true
                }
                R.id.nav_posts -> {
                    setupActiveNavIcons(item.itemId)
                    true
                }
                R.id.nav_chats -> {
                    setupActiveNavIcons(item.itemId)
                    true
                }
                R.id.nav_inventory -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_content, InventoryFragment())
                        .commit()
                    setupActiveNavIcons(item.itemId)
                    true
                }
                R.id.nav_mypage -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_content, PostListFragment())
                        .commit()
                    setupActiveNavIcons(item.itemId)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupActiveNavIcons(activeNavId: Int) {
        val navItems = mapOf(
            R.id.nav_home to Pair(R.drawable.ic_home_active, R.drawable.ic_home_inactive),
            R.id.nav_posts to Pair(R.drawable.ic_posts_active, R.drawable.ic_posts_inactive),
            R.id.nav_chats to Pair(R.drawable.ic_chats_active, R.drawable.ic_chats_inactive),
            R.id.nav_inventory to Pair(R.drawable.ic_inventory_active, R.drawable.ic_inventory_inactive),
            R.id.nav_mypage to Pair(R.drawable.ic_mypage_active, R.drawable.ic_mypage_inactive)
        )

        navItems.forEach { (id, resources) ->
            binding.bottomNavigation.menu.findItem(id).setIcon(resources.second)
        }

        navItems[activeNavId]?.let { resources ->
            binding.bottomNavigation.menu.findItem(activeNavId).setIcon(resources.first)
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
}
