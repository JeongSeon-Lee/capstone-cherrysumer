package com.example.cherrysumer

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.cherrysumer.databinding.FragmentInventoryBinding
import com.google.android.material.tabs.TabLayout

class InventoryFragment : Fragment() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentInventoryBinding.inflate(inflater, container, false)

        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.tab_content, InventoryListFragment.newInstance("냉장실"))
                .commit()
        }

        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.title = "나의 재고"

        binding.tabs.apply {
            addTab(newTab().setText("냉장실"))
            addTab(newTab().setText("냉동실"))
            addTab(newTab().setText("실외 저장소"))
        }

        binding.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val transaction = childFragmentManager.beginTransaction()
                when (tab?.text) {
                    "냉장실" -> transaction?.replace(R.id.tab_content, InventoryListFragment.newInstance("냉장실"))
                    "냉동실" -> transaction?.replace(R.id.tab_content, InventoryListFragment.newInstance("냉동실"))
                    "실외 저장소" -> transaction?.replace(R.id.tab_content, InventoryListFragment.newInstance("실외 저장소"))
                    else -> return
                }
                transaction?.commit()
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
        })

        binding.inventoryAdd.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), it)
            popupMenu.menuInflater.inflate(R.menu.inventory_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_direct_input -> {
                        // 직접 입력 선택 시 동작
                        val transaction = activity?.supportFragmentManager?.beginTransaction()
                        transaction?.replace(R.id.nav_content, InventoryInsertFragment())
                        transaction?.addToBackStack(null)
                        transaction?.commit()
                        true
                    }
                    R.id.menu_receipt_scan -> {
                        // 영수증 촬영 선택 시 동작
                        Toast.makeText(requireContext(), "영수증 촬영 선택됨", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }

            popupMenu.show()
        }

        return binding.root
    }
}
