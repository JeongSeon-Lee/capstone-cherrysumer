package com.example.cherrysumer

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.time.format.DateTimeFormatter
import androidx.recyclerview.widget.RecyclerView
import com.example.cherrysumer.databinding.ItemInventoryBinding
import com.example.cherrysumer.retrofit.models.InventoryItem
import java.time.LocalDate
import java.time.LocalDateTime

class InventoryViewHolder(val binding: ItemInventoryBinding): RecyclerView.ViewHolder(binding.root)

class InventoryAdapter(
    private var inventoryItems: List<InventoryItem>,
    private var onUpdateItemQuantity: ((Int, InventoryItem, Int) -> Unit)? = null
): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

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
        val firstCategory = model.category?.split(",")?.firstOrNull()?.trim()

        binding.itemProductName.text = model.productName
        binding.itemExpirationDate.text = "~${expirationDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))}"
        binding.itemCategory.text = model.category ?: ""
        binding.itemQuantity.text = "${model.quantity}"
        binding.itemIcon.setImageResource(when (firstCategory) {
            "과일" -> R.drawable.ic_fruit
            "채소" -> R.drawable.ic_cherry   // 이미지 바꿔야 함
            "유제품" -> R.drawable.ic_cherry  // 이미지 바꿔야 함
            "정육" -> R.drawable.ic_meat
            "냉동식품" -> R.drawable.ic_frozen_food
            "수산물" -> R.drawable.ic_fish
            "음료" -> R.drawable.ic_drink
            "간편식" -> R.drawable.ic_convenience_food
            "디저트" -> R.drawable.ic_dessert
            "생활용품" -> R.drawable.ic_daily_necessity
            else -> R.drawable.ic_cherry
        })
        binding.itemDday.text = when {
            daysLeft > 0 -> "D-${daysLeft}"
            daysLeft == 0L -> "D-DAY"
            else -> "D+${-daysLeft}"
        }
        if (daysLeft <= 3) {
            binding.itemDday.setBackgroundColor(Color.parseColor("#FF8B8B"))
        }
        if (model.quantity <= 1) {
            binding.itemQuantity.setBackgroundColor(Color.parseColor("#FF8B8B"))
        }
        binding.itemPlus.setOnClickListener {
            Log.d("InventoryAdapter", "Plus button clicked for item ID: ${model.id}, Position: $position")
            val updatedItem = model.copy(quantity = model.quantity + 1)
            Log.d("InventoryAdapter", "Updated item: $updatedItem")
            Log.d("InventoryAdapter", "Calling onUpdateItemQuantity callback for Plus button")
            onUpdateItemQuantity?.invoke(model.id, updatedItem, position)
        }

        binding.itemMinus.setOnClickListener {
            Log.d("InventoryAdapter", "Minus button clicked for item ID: ${model.id}, Position: $position")
            if (model.quantity > 0) {
                val updatedItem = model.copy(quantity = model.quantity - 1)
                Log.d("InventoryAdapter", "Updated item: $updatedItem")
                Log.d("InventoryAdapter", "Calling onUpdateItemQuantity callback for Minus button")
                onUpdateItemQuantity?.invoke(model.id, updatedItem, position)
            }
        }

    }

    fun updateItemQuantity(newItems: List<InventoryItem>, position: Int? = null) {
        Log.d("InventoryAdapter", "Updating items. Current list size: ${inventoryItems.size}")
        inventoryItems = newItems
        if (position != null) {
            Log.d("InventoryAdapter", "Notifying item changed at position: $position")
            notifyItemChanged(position)
        } else {
            Log.d("InventoryAdapter", "Notifying dataset changed")
            notifyDataSetChanged()
        }
        Log.d("InventoryAdapter", "Updated list size: ${inventoryItems.size}")
    }

    fun getCurrentItems(): List<InventoryItem> {
        return inventoryItems
    }
}