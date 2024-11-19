package com.cookandroid.cherrysumer.join

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cookandroid.cherrysumer.R

class ProgressBarFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // progress_bar.xml 레이아웃을 inflate하여 반환
        return inflater.inflate(R.layout.progress_bar, container, false)
    }
}