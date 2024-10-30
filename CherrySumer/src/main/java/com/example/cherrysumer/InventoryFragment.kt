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

        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.title = "나의 재고"

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
                        val transaction = activity?.supportFragmentManager?.beginTransaction()
                        transaction?.replace(R.id.nav_content, PostListFragment())
                        transaction?.addToBackStack(null)
                        transaction?.commit()
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
                transaction?.replace(R.id.nav_content, searchFragment)
                transaction?.addToBackStack(null)
                transaction?.commit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
