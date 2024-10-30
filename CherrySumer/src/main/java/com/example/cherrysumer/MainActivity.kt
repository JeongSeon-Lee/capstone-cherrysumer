package com.example.cherrysumer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.cherrysumer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
