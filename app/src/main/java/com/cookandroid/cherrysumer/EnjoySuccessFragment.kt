package com.cookandroid.cherrysumer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.cookandroid.cherrysumer.databinding.FragmentPostEnjoyCompleteBinding
import com.cookandroid.cherrysumer.databinding.FragmentPostWriteUploadBinding

class EnjoySuccessFragment : Fragment() {

    private var postId: Long = 0L
    private var title: String = ""
    private var productname: String = ""
    private var price: Int = 0
    private var place: String = ""
    private var date: String = ""
    private var imageUrl: String = ""
    private val baseUrl = "http://3.39.110.119"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 레이아웃을 인플레이트
        val binding = FragmentPostEnjoyCompleteBinding.inflate(inflater, container, false)

        // 전달받은 데이터 읽기
        arguments?.let {
            postId = it.getLong("postId", 0L)
            title = it.getString("title", "")
            productname = it.getString("productname", "")
            price = it.getInt("price", 0)
            place = it.getString("place", "")
            date = it.getString("date", "")
            imageUrl = it.getString("imageUrl", "")
        }

        // 가져온 데이터를 UI에 설정
        binding.postTitle.text = title
        binding.price.text = "1인당 $price 원"
        binding.date.text = date
        binding.location.text = place

        // 서버 베이스 URL과 이미지 경로 결합
        val fullImageUrl = baseUrl + imageUrl
        Glide.with(requireContext()).load(fullImageUrl).into(binding.postImage)


        binding.completeButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // GoHome 버튼 클릭 시 HomeFragment로 이동
        binding.goHomeButton.setOnClickListener {
            // HomeFragment로 이동 (스택에 추가하지 않음)
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, HomeFragment())
            transaction.commitNow()  // Fragment 스택에 추가하지 않음
        }


        return binding.root
    }
}