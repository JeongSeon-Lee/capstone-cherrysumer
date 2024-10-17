package com.example.cherrysumer

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
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
        setHasOptionsMenu(true)

        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.tab_content, InventoryListFragment.newInstance("냉장실", ""))
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
                    "냉장실" -> transaction?.replace(R.id.tab_content, InventoryListFragment.newInstance("냉장실", ""))
                    "냉동실" -> transaction?.replace(R.id.tab_content, InventoryListFragment.newInstance("냉동실", ""))
                    "실외 저장소" -> transaction?.replace(R.id.tab_content, InventoryListFragment.newInstance("실외 저장소", ""))
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
            popupMenu.menuInflater.inflate(R.menu.inventory_insert_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_direct_input -> {
                        val transaction = activity?.supportFragmentManager?.beginTransaction()
                        transaction?.replace(R.id.nav_content, InventoryInsertFragment())
                        transaction?.addToBackStack(null)
                        transaction?.commit()
                        true
                    }
                    R.id.menu_import_purchase -> {
                        true
                    }
                    else -> false
                }
            }

            popupMenu.show()
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu) // 메뉴 리소스를 Inflate합니다.
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_search -> {
                // 검색 버튼 클릭 시 SearchFragment로 이동합니다.
                val transaction = activity?.supportFragmentManager?.beginTransaction()
                val bundle = Bundle().apply {
                    putBoolean("USE_FIRST_LAYOUT", true) // 첫 번째 레이아웃 사용 조건 전달
                }
                val searchFragment = SearchFragment()
                searchFragment.arguments = bundle
                transaction?.replace(R.id.nav_content, searchFragment)
                transaction?.addToBackStack(null)
                transaction?.commit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
