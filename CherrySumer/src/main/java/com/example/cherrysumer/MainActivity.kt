package com.example.cherrysumer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.cherrysumer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_content, InventoryFragment())
            .commit()

        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inventory -> {
                    // Handle Inventory navigation
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_content, InventoryFragment())
                        .commit()
                    true
                }

                else -> false
            }
        }
    }
}