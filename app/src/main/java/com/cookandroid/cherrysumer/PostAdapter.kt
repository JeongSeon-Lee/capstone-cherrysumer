package com.cookandroid.cherrysumer

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.cookandroid.cherrysumer.databinding.ItemPostBinding
import com.cookandroid.cherrysumer.retrofit.models.PostItem

class PostViewHolder(val binding: ItemPostBinding): RecyclerView.ViewHolder(binding.root)

class PostAdapter(
    private val postItems: List<PostItem>,
    private val onInsertClick: (PostItem) -> Unit
): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    override fun getItemCount(): Int{
        return postItems?.size ?: 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
            = PostViewHolder(ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as PostViewHolder).binding
        val model = postItems[position]

        binding.itemTitle.text = model.title

        binding.insertInventoryButton.setOnClickListener {
            onInsertClick(model)
        }
    }
}