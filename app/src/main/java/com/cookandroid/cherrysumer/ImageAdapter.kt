package com.cookandroid.cherrysumer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ImageAdapter(
    private val imageUrls: List<String>,
    private val defaultImageResId: Int
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    companion object {
        private const val BASE_URL = "http://3.39.110.119" // 서버 베이스 URL 설정
    }

    inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.post_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val relativePath = imageUrls.getOrNull(position)
        val imageUrl = if (!relativePath.isNullOrEmpty()) "$BASE_URL$relativePath" else null

        Glide.with(holder.imageView.context)
            .load(imageUrl ?: defaultImageResId) // 상대 경로가 있으면 URL 생성, 없으면 기본 이미지
            .placeholder(defaultImageResId)
            .error(defaultImageResId)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return if (imageUrls.isEmpty()) 1 else imageUrls.size // 이미지가 없으면 기본 이미지 1개 표시
    }
}