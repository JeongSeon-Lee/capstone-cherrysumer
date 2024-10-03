package com.example.cherrysumer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.cherrysumer.databinding.ActivityInventoryListBinding
import com.google.android.material.tabs.TabLayout

class InventoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityInventoryListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "나의 재고"

        binding.tabs.apply {
            addTab(newTab().setText("냉장실"))
            addTab(newTab().setText("냉동실"))
            addTab(newTab().setText("실외 저장소"))
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.tab_content, InventoryFragment.newInstance("냉장실"))
            .commit()

        binding.tabs.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val transaction = supportFragmentManager.beginTransaction()
                when (tab?.text) {
                    "냉장실"-> transaction.replace(R.id.tab_content, InventoryFragment.newInstance("냉장실"))
                    "냉동실"-> transaction.replace(R.id.tab_content, InventoryFragment.newInstance("냉동실"))
                    "실외 저장소"-> transaction.replace(R.id.tab_content, InventoryFragment.newInstance("실외 저장소"))
                }
                transaction.commit()
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }
        })

        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inventory -> {
                    // Handle Inventory navigation
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.tab_content, InventoryFragment())
                        .commit()
                    true
                }

                else -> false
            }
        }
    }
}