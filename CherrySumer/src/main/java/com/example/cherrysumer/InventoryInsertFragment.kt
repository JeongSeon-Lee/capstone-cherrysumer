package com.example.cherrysumer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.cherrysumer.databinding.FragmentInventoryInsertBinding

class InventoryInsertFragment : Fragment() {

    private lateinit var binding: FragmentInventoryInsertBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInventoryInsertBinding.inflate(inflater, container, false)

        // 검색 기능 구현

        return binding.root
    }
}
