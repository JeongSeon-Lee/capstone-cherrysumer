package com.cookandroid.cherrysumer

import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.cookandroid.cherrysumer.databinding.FragmentInventoryInsertBinding
import com.cookandroid.cherrysumer.retrofit.ApiCallback
import com.cookandroid.cherrysumer.retrofit.ApiManager
import com.cookandroid.cherrysumer.retrofit.models.ApiResponse
import com.cookandroid.cherrysumer.retrofit.models.InventoryItem
import com.google.android.material.chip.Chip
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class InventoryInsertFragment : Fragment() {
    private lateinit var binding: FragmentInventoryInsertBinding
    private val source by lazy { arguments?.getString("source") }
    private val postId by lazy { arguments?.getLong("postId") }
    private val itemIdToEdit by lazy { arguments?.getInt("itemIdToEdit") }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInventoryInsertBinding.inflate(inflater, container, false)

        setupUI()
        setupFieldsFromArguments()

        binding.addButton.setOnClickListener { handleAddOrEditInventory() }

        return binding.root
    }

    private fun setupUI() {
        setupToolbar()
        binding.addButton.text = if (source == "editInventoryItem") "수정하기" else "추가하기"
        setupDatePicker(binding.purchaseDateInput)
        setupDatePicker(binding.expirationDateInput)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_number_picker, null)
        val numberPicker = dialogView.findViewById<NumberPicker>(R.id.dialogNumberPicker)
        setupNumberPicker(numberPicker)
    }

    private fun setupToolbar() {
        // Toolbar를 액션바로 설정
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        val actionBar = (activity as? AppCompatActivity)?.supportActionBar

        // 액션바의 기본 타이틀 비활성화
        actionBar?.setDisplayShowTitleEnabled(false)

        // XML에서 정의한 TextView를 사용하여 타이틀 설정
        binding.toolbarTitle.text = if (source == "editInventoryItem") "재고 수정" else "재고 추가"

        // 업 버튼 활성화
        actionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            // 뒤로 가기 동작
            activity?.onBackPressed()
        }
    }

    private fun setupDatePicker(inputField: EditText) {
        inputField.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                    inputField.setText(formattedDate)
                },
                year, month, day
            )
            datePicker.show()
        }
    }

    private fun setupNumberPicker(numberPicker: NumberPicker, min: Int = 0, max: Int = 100) {
        binding.quantityInput.setOnClickListener {
            // 다이얼로그 레이아웃 Inflate
            val dialogView = layoutInflater.inflate(R.layout.dialog_number_picker, null)

            // NumberPicker 초기화
            val numberPicker = dialogView.findViewById<NumberPicker>(R.id.dialogNumberPicker)
            numberPicker.minValue = 0 // 최소값
            numberPicker.maxValue = 100 // 최대값
            numberPicker.wrapSelectorWheel = true // 롤링 활성화

            // AlertDialog 빌더
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("수량 선택")
                .setView(dialogView) // 커스텀 레이아웃 설정
                .setPositiveButton("확인") { _, _ ->
                    // 선택된 값 처리
                    val selectedQuantity = numberPicker.value
                    binding.quantityInput.setText(selectedQuantity.toString()) // EditText에 값 설정
                }
                .setNegativeButton("취소", null)
                .create()

            dialog.show()
        }

        binding.quantityInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    // 힌트일 때는 왼쪽 정렬
                    binding.quantityInput.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                } else {
                    // 값이 입력되면 오른쪽 정렬
                    binding.quantityInput.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                }
            }
        })
    }

/*    private fun setupCategoryChips() {
        binding.categoryChipGroup.isSingleSelection = true
        val categories = resources.getStringArray(R.array.category_array)

        for (category in categories) {
            val chip = Chip(requireContext()).apply {
                text = category
                isCheckable = true
            }
            binding.categoryChipGroup.addView(chip)
        }
    }*/

    private fun setupFieldsFromArguments() {
        val productName = arguments?.getString("productName")
        val purchaseDate = arguments?.getString("purchaseDate")
        val expirationDate = arguments?.getString("expirationDate")
        val quantity = arguments?.getInt("quantity")
        val stockLocation = arguments?.getString("stockLocation")
        val category = arguments?.getString("category")

        binding.productNameInput.setText(productName)
        binding.purchaseDateInput.setText(purchaseDate?.substringBefore("T"))
        binding.expirationDateInput.setText(expirationDate?.substringBefore("T"))
        quantity?.let {
            binding.quantityInput.setText(it.toString())
        }
        category?.let {
            binding.categoryChipGroup.children.forEach { chip ->
                if (chip is Chip && chip.text.toString() == it) {
                    chip.isChecked = true
                }
            }
        }
        stockLocation?.let {
            binding.stockLocationChipGroup.children.forEach { chip ->
                if (chip is Chip && chip.text.toString() == it) {
                    chip.isChecked = true
                    Log.d("InventoryInsertFragment", "Stock location chip checked: ${chip.text}")
                }
            }
        }
    }

    private fun parseDate(dateString: String): String {
        return try {
            LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                .atTime(23, 59, 59)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (e: Exception) {
            Log.e("InventoryInsert", "Failed to parse date: $dateString", e)
            ""
        }
    }

    private fun getSelectedCategory(): String {
        val selectedChipId = binding.categoryChipGroup.checkedChipId
        return if (selectedChipId != View.NO_ID) {
            binding.categoryChipGroup.findViewById<Chip>(selectedChipId)?.text?.toString() ?: ""
        } else {
            ""
        }
    }

    private fun getSelectedStockLocation(): String {
        val selectedChipId = binding.stockLocationChipGroup.checkedChipId
        return if (selectedChipId != View.NO_ID) {
            binding.stockLocationChipGroup.findViewById<Chip>(selectedChipId)?.text?.toString() ?: ""
        } else {
            ""
        }
    }

    private fun isValidInput(
        productName: String,
        stockLocation: String,
        category: String
    ): Boolean {
        return productName.isNotEmpty() && stockLocation.isNotEmpty() && category.isNotEmpty()
    }

    private fun navigateToInventory(stockLocation: String) {
        val inventoryFragment = InventoryFragment().apply {
            arguments = Bundle().apply {
                putString("stockLocation", stockLocation)
            }
        }

        activity?.supportFragmentManager?.let { fragmentManager ->
            // 백 스택을 비동기적으로 초기화하고 완료된 후 프래그먼트를 이동
            fragmentManager.popBackStack(
                null,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )

            // 백 스택 초기화 후 바로 프래그먼트 전환
            Handler(Looper.getMainLooper()).post {
                fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, inventoryFragment)
                    .addToBackStack(null)
                    .commitAllowingStateLoss()
            }
        }
    }

    private fun handleAddOrEditInventory() {
        val productName = binding.productNameInput.text.toString()
        val purchaseDate = parseDate(binding.purchaseDateInput.text.toString())
        val expirationDate = parseDate(binding.expirationDateInput.text.toString())
        val quantity = binding.quantityInput.text.toString().toIntOrNull() ?: 0
        val stockLocation = getSelectedStockLocation()
        val category = getSelectedCategory()

        if (isValidInput(productName, stockLocation, category)) {
            val inventoryItem = InventoryItem(
                createdAt = "", updatedAt = "", id = 0,
                productName = productName, purchaseDate = purchaseDate, expirationDate = expirationDate,
                quantity = quantity, stockLocation = stockLocation, category = category
            )
            if (source == "editInventoryItem") {
                editInventoryItem(inventoryItem)
            } else {
                insertInventoryItem(inventoryItem)
            }
        } else {
            Toast.makeText(context, "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun insertInventoryItem(inventoryItem: InventoryItem) {
        ApiManager().insertInventoryItem(inventoryItem, object : ApiCallback<Unit> {
            override fun onSuccess(apiResponse: ApiResponse<Unit>?) {
                handlePostRegistrationIfNeeded(inventoryItem)
                navigateToInventory(inventoryItem.stockLocation)
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
    }

    private fun editInventoryItem(inventoryItem: InventoryItem) {
        itemIdToEdit?.let { itemId ->
            ApiManager().editInventoryItem(itemId, inventoryItem, object : ApiCallback<Unit> {
                override fun onSuccess(apiResponse: ApiResponse<Unit>?) {
                    navigateToInventory(inventoryItem.stockLocation)
                    Toast.makeText(activity, "재고를 수정하였습니다.", Toast.LENGTH_SHORT).show()
                    super.onSuccess(apiResponse)
                }

                override fun onError(response: Response<ApiResponse<Unit>>) {
                    Toast.makeText(activity, "재고 수정에 실패했습니다 (HTTP ${response.code()})", Toast.LENGTH_SHORT).show()
                    super.onError(response)
                }

                override fun onFailure(throwable: Throwable) {
                    Toast.makeText(activity, "네트워크 오류: ${throwable.message}", Toast.LENGTH_SHORT).show()
                    super.onFailure(throwable)
                }
            })
        } ?: Log.e("EditInventoryItem", "itemIdToEdit is null")
    }

    private fun handlePostRegistrationIfNeeded(inventoryItem: InventoryItem) {
        if (source == "registerPostItem") {
            postId?.let { postId ->
                ApiManager().registerPostItem(postId, object : ApiCallback<Unit> {
                    override fun onError(response: Response<ApiResponse<Unit>>) {
                        Log.e("InventoryInsertFragment", "공구에서 가져온 재고는 추가하였으나 재고 등록 완료에는 실패했습니다.")
                        super.onError(response)
                    }

                    override fun onFailure(throwable: Throwable) {
                        Log.e("InventoryInsertFragment", "공구에서 가져온 재고는 추가하였으나 재고 등록 완료에는 실패했습니다.")
                        super.onFailure(throwable)
                    }
                })
            } ?: Log.e("RegisterPostItem", "postId is null")
        }
    }

}