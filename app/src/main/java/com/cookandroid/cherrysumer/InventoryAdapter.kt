package com.cookandroid.cherrysumer

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import java.time.format.DateTimeFormatter
import androidx.recyclerview.widget.RecyclerView
import com.cookandroid.cherrysumer.retrofit.models.InventoryItem
import com.cookandroid.cherrysumer.databinding.ItemInventoryBinding
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

        val purchaseDateString = model.purchaseDate
        val expirationDateString = model.expirationDate
        val formatter = DateTimeFormatter.ISO_DATE_TIME

        val purchaseDate = try {
            LocalDateTime.parse(purchaseDateString, formatter).toLocalDate()
        } catch (e: Exception) {
            Log.e("InventoryAdapter", "Date parsing error: ${e.message}")
            LocalDate.now()
        }
        val expirationDate = try {
            LocalDateTime.parse(expirationDateString, formatter).toLocalDate()
        } catch (e: Exception) {
            Log.e("InventoryAdapter", "Date parsing error: ${e.message}")
            LocalDate.now()
        }
        val currentDate = LocalDate.now()
        val daysLeft = expirationDate.toEpochDay() - currentDate.toEpochDay()
        val firstCategory = model.category?.split(",")?.firstOrNull()?.trim()

        binding.itemProductName.text = model.productName
        binding.itemExpirationDate.text =
            "${purchaseDate.format(DateTimeFormatter.ofPattern("yy.MM.dd"))}~${expirationDate.format(DateTimeFormatter.ofPattern("yy.MM.dd"))}"
        binding.itemCategory.text = model.category ?: ""
        binding.itemQuantity.text = "${model.quantity}"

        binding.itemIcon.setImageResource(
            when (firstCategory) {
                "과일" -> R.drawable.ic_fruit
                "채소" -> R.drawable.ic_cherry
                "유제품" -> R.drawable.ic_dairy
                "정육" -> R.drawable.ic_meat
                "냉동식품" -> R.drawable.ic_frozen_food
                "수산물" -> R.drawable.ic_seafood
                "음료" -> R.drawable.ic_beverage
                "간편식" -> R.drawable.ic_convenience_food
                "디저트" -> R.drawable.ic_dessert
                "생활용품" -> R.drawable.ic_household_goods
                else -> R.drawable.ic_cherry
            }
        )

        binding.itemDday.text = when {
            daysLeft > 0 -> "D-${daysLeft}"
            daysLeft == 0L -> "D-DAY"
            else -> "D+${-daysLeft}"
        }
        binding.itemDday.setBackgroundColor(
            if (daysLeft <= 3) Color.parseColor("#FF8B8B") else Color.parseColor("#B4B4B4")
        )

        // density 사용하여 배경 크기와 마진 조정
        val density = binding.itemQuantity.resources.displayMetrics.density
        val quantityColor = if (model.quantity <= 1) "#FF8B8B" else "#FFFFFF"
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 8 * density
            setColor(Color.parseColor(quantityColor))
        }
        binding.itemQuantity.background = drawable

        val layoutParams = (binding.itemQuantity.layoutParams as ViewGroup.MarginLayoutParams)
        layoutParams.width = (30 * density).toInt()
        layoutParams.setMargins(
            (10 * density).toInt(), // 좌측 마진
            0, // 상단 마진
            (10 * density).toInt(), // 우측 마진
            0  // 하단 마진
        )
        binding.itemQuantity.layoutParams = layoutParams

        binding.btnPlus.setOnClickListener {
            val updatedItem = model.copy(quantity = model.quantity + 1)
            onUpdateItemQuantity?.invoke(model.id, updatedItem, position)
        }

        binding.btnMinus.setOnClickListener {
            if (model.quantity > 0) {
                val updatedItem = model.copy(quantity = model.quantity - 1)
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