package com.cookandroid.cherrysumer

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.cookandroid.cherrysumer.databinding.FragmentInventoryBinding
import com.cookandroid.cherrysumer.mypage.StatusActivity
import com.google.android.material.tabs.TabLayout

class InventoryFragment : Fragment() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentInventoryBinding.inflate(inflater, container, false)

        setHasOptionsMenu(true)

        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayShowTitleEnabled(false)

        val stockLocation = arguments?.getString("stockLocation") ?: getString(R.string.fridge)
        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.tab_content, InventoryListFragment.newInstance(stockLocation, ""))
                .commit()
        }
        binding.tabs.apply {
            addTab(newTab().setText(getString(R.string.fridge)))
            addTab(newTab().setText(getString(R.string.freezer)))
            addTab(newTab().setText(getString(R.string.outdoor_storage)))
            when (stockLocation) {
                getString(R.string.fridge) -> selectTab(getTabAt(0))
                getString(R.string.freezer) -> selectTab(getTabAt(1))
                getString(R.string.outdoor_storage) -> selectTab(getTabAt(2))
            }
        }
        binding.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val transaction = childFragmentManager.beginTransaction()
                when (tab?.text) {
                    getString(R.string.fridge) -> transaction.replace(R.id.tab_content, InventoryListFragment.newInstance(getString(R.string.fridge), ""))
                    getString(R.string.freezer) -> transaction.replace(R.id.tab_content, InventoryListFragment.newInstance(getString(R.string.freezer), ""))
                    getString(R.string.outdoor_storage) -> transaction.replace(R.id.tab_content, InventoryListFragment.newInstance(getString(R.string.outdoor_storage), ""))
                    else -> return
                }
                transaction.commit()
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
        })

        binding.inventoryAdd.setOnClickListener {
            val inflater = LayoutInflater.from(requireContext())
            val popupView = inflater.inflate(R.layout.custom_popup_menu, null)

            // PopupWindow 생성
            val popupWindow = PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            )
            popupWindow.elevation = 8f

            // 메뉴 클릭 이벤트
            popupView.findViewById<TextView>(R.id.menu_direct_input).setOnClickListener {
                popupWindow.dismiss()
                // 직접 입력 페이지 이동
                val transaction = activity?.supportFragmentManager?.beginTransaction()
                transaction?.replace(R.id.fragment_container, InventoryInsertFragment())
                transaction?.addToBackStack(null)
                transaction?.commit()
            }

            popupView.findViewById<TextView>(R.id.menu_import_purchase).setOnClickListener {
                popupWindow.dismiss()
                // 게시글 가져오기 페이지 이동
                val intent = Intent(requireContext(), StatusActivity::class.java)
                startActivity(intent)
            }

            // 가로 크기 제한 (예: 200dp로 제한)
            val density = resources.displayMetrics.density
            popupWindow.width = (170 * density).toInt()

            // 팝업 위치: 플로팅 버튼 바로 위
            val location = IntArray(2)
            binding.inventoryAdd.getLocationOnScreen(location)

            val xOffset = binding.inventoryAdd.width / 2 - popupWindow.width / 2 - (64 * density).toInt()
            val yOffset = -(binding.inventoryAdd.height + popupView.measuredHeight + (46 * density).toInt())

            popupWindow.showAtLocation(binding.inventoryAdd, Gravity.NO_GRAVITY, location[0] + xOffset, location[1] + yOffset)
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_search -> {
                val transaction = activity?.supportFragmentManager?.beginTransaction()
                val bundle = Bundle().apply {
                    putBoolean("USE_FIRST_LAYOUT", true)
                }
                val searchFragment = SearchFragment()
                searchFragment.arguments = bundle
                transaction?.replace(R.id.fragment_container, searchFragment)
                transaction?.addToBackStack(null)
                transaction?.commit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
