package com.example.cherrysumer

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.example.cherrysumer.databinding.FragmentInventoryInsertBinding
import com.example.cherrysumer.retrofit.ApiCallback
import com.example.cherrysumer.retrofit.ApiManager
import com.example.cherrysumer.retrofit.models.ApiResponse
import com.example.cherrysumer.retrofit.models.InventoryItem
import com.google.android.material.chip.Chip
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class InventoryInsertFragment : Fragment() {
    private lateinit var binding: FragmentInventoryInsertBinding
    private var selectedStockLocation: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInventoryInsertBinding.inflate(inflater, container, false)

        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.title = "재고 추가"
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 구매 날짜 입력 설정
        val purchaseDateInput = binding.purchaseDateInput

        purchaseDateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                    purchaseDateInput.setText(formattedDate)
                },
                year, month, day
            )
            datePicker.show()
        }

        // 유통기한 입력 설정
        val expirationDateInput = binding.expirationDateInput

        expirationDateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                    expirationDateInput.setText(formattedDate)
                },
                year, month, day
            )
            datePicker.show()
        }

        // 수량 설정
        val numberPicker = binding.quantityPicker
        numberPicker.minValue = 0
        numberPicker.maxValue = 100
        numberPicker.wrapSelectorWheel = true

        // 재고 위치 설정
        binding.stockLocationChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val selectedChipId = checkedIds.firstOrNull()
            selectedStockLocation = if (selectedChipId != null) {
                val selectedChip = group.findViewById<Chip>(selectedChipId)
                selectedChip?.text?.toString()
            } else {
                null
            }
        }

        // 카테고리 추가
        val categories = resources.getStringArray(R.array.category_array)
        for (category in categories.drop(1)) {
            val chip = Chip(context)
            chip.text = category
            chip.isCheckable = true
            binding.categoryChipGroup.addView(chip)
        }

        binding.addButton.setOnClickListener {
            val selectedCategory = binding.categoryChipGroup
                .children
                .filterIsInstance<Chip>()
                .firstOrNull { it.isChecked }?.text.toString()

            val productName = binding.productNameInput.text.toString()
            val expirationDateInputString = binding.expirationDateInput.text.toString()
            val purchaseDateInputString = binding.purchaseDateInput.text.toString()

            // String을 LocalDate로 변환 후 자정 시간으로 LocalDateTime으로 변환
            val expirationDate = try {
                LocalDate.parse(expirationDateInputString, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    .atTime(23, 59, 59)
            } catch (e: Exception) {
                Log.e("InventoryInsert", "Failed to parse expiration date", e)
                null
            }
            val purchaseDate = try {
                LocalDate.parse(purchaseDateInputString, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    .atTime(23, 59, 59)
            } catch (e: Exception) {
                Log.e("InventoryInsert", "Failed to parse purchase date", e)
                null
            }

            val quantity = binding.quantityPicker.value
            val stockLocation = selectedStockLocation ?: ""

            if (productName.isNotEmpty() && expirationDate != null && stockLocation.isNotEmpty() && selectedCategory.isNotEmpty()) {
                val inventoryItem = InventoryItem(
                    createdAt = "",
                    updatedAt = "",
                    id = 0,
                    productName = productName,
                    purchaseDate = purchaseDate.toString(),
                    expirationDate = expirationDate.toString(), // 서버와의 통신을 위해 String으로 변환
                    quantity = quantity,
                    stockLocation = stockLocation,
                    category = selectedCategory // 카테고리를 단일 String으로 설정
                )

                // 입력된 전체 데이터를 로그로 출력
                Log.d("InventoryInsert", "Inventory Item: $inventoryItem")

                // 서버에 데이터 전송
                ApiManager().insertInventoryItem(inventoryItem, object : ApiCallback<Unit> {
                    override fun onSuccess(apiResponse: ApiResponse<Unit>?) {
                        requireActivity().supportFragmentManager.popBackStack()
                        Toast.makeText(activity, "재고를 추가하였습니다.", Toast.LENGTH_SHORT).show()
                        super.onSuccess(apiResponse)
                    }
                    override fun onError(response: Response<ApiResponse<Unit>>) {
                        Toast.makeText(activity, "재고 추가에 실패했습니다 (HTTP ${response.code()})", Toast.LENGTH_SHORT).show()
                        super.onError(response)
                    }
                    override fun onFailure(throwable: Throwable) {
                        Toast.makeText(activity, "네트워크 오류: ${throwable.message}", Toast.LENGTH_SHORT).show()
                        super.onFailure(throwable)
                    }
                })
            } else {
                Toast.makeText(context, "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }
}

