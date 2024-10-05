package com.example.cherrysumer

import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import java.time.format.DateTimeFormatter
import androidx.recyclerview.widget.RecyclerView
import com.example.cherrysumer.databinding.ItemInventoryBinding
import com.example.cherrysumer.retrofit.models.InventoryItem
import java.time.LocalDate
import java.time.LocalDateTime

class InventoryViewHolder(val binding: ItemInventoryBinding): RecyclerView.ViewHolder(binding.root)

class InventoryAdapter(val inventoryItems: List<InventoryItem>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    override fun getItemCount(): Int{
        return inventoryItems?.size ?: 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
            = InventoryViewHolder(ItemInventoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as InventoryViewHolder).binding
        val model = inventoryItems[position]

        val expirationDateString = model.expirationDate // String 형태로 가정
        val formatter = DateTimeFormatter.ISO_DATE_TIME // 서버의 날짜 형식에 맞게 수정
        val expirationDate = try {
            // LocalDateTime으로 파싱 후 LocalDate로 변환
            LocalDateTime.parse(expirationDateString, formatter).toLocalDate()
        } catch (e: Exception) {
            Log.e("InventoryAdapter", "Date parsing error: ${e.message}")
            LocalDate.now() // 예외 발생 시 현재 날짜로 대체
        }
        val currentDate = LocalDate.now()
        val daysLeft = expirationDate.toEpochDay() - currentDate.toEpochDay() // LocalDate로 변환 후 사용

        binding.itemProductName.text = model.productName
        binding.itemExpirationDate.text = "~${expirationDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))}"
        binding.itemCategory.text = model.category ?: ""
        binding.itemQuantity.text = "${model.quantity}"
        binding.itemIcon.setImageResource(when (model.category) {
            "과일" -> R.drawable.ic_fruit
            "채소" -> R.drawable.ic_cherry   // 이미지 바꿔야 함
            "정육" -> R.drawable.ic_meat
            "냉동식품" -> R.drawable.ic_frozen_food
            "수산물" -> R.drawable.ic_fish
            "음료" -> R.drawable.ic_drink
            "간편식" -> R.drawable.ic_convenience_food
            "디저트" -> R.drawable.ic_dessert
            "생활용품" -> R.drawable.ic_daily_necessity
            "유제품" -> R.drawable.ic_cherry  // 이미지 바꿔야 함
            else -> R.drawable.ic_cherry
        })
        binding.itemDday.text = when {
            daysLeft > 0 -> "D-${daysLeft}"
            daysLeft == 0L -> "D-DAY"
            else -> "D+${-daysLeft}"
        }
        if (daysLeft <= 0) {
            binding.itemDday.setBackgroundColor(Color.parseColor("#FF8B8B"))
        }
        if (model.quantity <= 1) {
            binding.itemQuantity.setBackgroundColor(Color.parseColor("#FF8B8B"))
        }
    }
}