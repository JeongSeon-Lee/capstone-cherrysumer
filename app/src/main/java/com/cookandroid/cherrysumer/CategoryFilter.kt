package com.cookandroid.cherrysumer

import android.app.Dialog
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CategoryFilter(
    private val selectedFilterCallback: (String) -> Unit,
    private var currentFilter: String // 현재 선택된 필터를 저장하는 변수
) : BottomSheetDialogFragment() {

    private lateinit var allContainer: LinearLayout
    private lateinit var fruitContainer: LinearLayout
    private lateinit var VegetableContainer: LinearLayout
    private lateinit var deliveryContainer: LinearLayout
    private lateinit var meatContainer: LinearLayout
    private lateinit var frozenFoodContainer: LinearLayout
    private lateinit var marineProductsContainer: LinearLayout
    private lateinit var drinkContainer: LinearLayout
    private lateinit var dairyProductsContainer: LinearLayout
    private lateinit var convenientFoodContainer: LinearLayout
    private lateinit var dessertContainer: LinearLayout
    private lateinit var dailySuppliesContainer: LinearLayout

    private lateinit var checkAll: ImageView
    private lateinit var checkFruit: ImageView
    private lateinit var checkVegetable: ImageView
    private lateinit var checkDelivery: ImageView
    private lateinit var checkMeat: ImageView
    private lateinit var checkFrozenFood: ImageView
    private lateinit var checkMarineProducts: ImageView
    private lateinit var checkDrink: ImageView
    private lateinit var checkDairyProducts: ImageView
    private lateinit var checkConvenientFood: ImageView
    private lateinit var checkDessert: ImageView
    private lateinit var checkDailySupplies: ImageView

    private lateinit var allText: TextView
    private lateinit var fruitText: TextView
    private lateinit var VegetableText: TextView
    private lateinit var deliveryText: TextView
    private lateinit var meatText: TextView
    private lateinit var frozenFoodText: TextView
    private lateinit var marineProductsText: TextView
    private lateinit var drinkText: TextView
    private lateinit var dairyProductsText: TextView
    private lateinit var convenientFoodText: TextView
    private lateinit var dessertText: TextView
    private lateinit var dailySuppliesText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.category_filter, container, false)

        allContainer = view.findViewById(R.id.all_container)
        fruitContainer = view.findViewById(R.id.fruit_container)
        VegetableContainer = view.findViewById(R.id.vegetable_container)
        deliveryContainer = view.findViewById(R.id.delivery_container)
        meatContainer = view.findViewById(R.id.meat_container)
        frozenFoodContainer = view.findViewById(R.id.frozen_food_container)
        marineProductsContainer = view.findViewById(R.id.marine_products_container)
        drinkContainer = view.findViewById(R.id.drink_container)
        dairyProductsContainer = view.findViewById(R.id.dairy_products_container)
        convenientFoodContainer = view.findViewById(R.id.convenient_food_container)
        dessertContainer = view.findViewById(R.id.dessert_container)
        dailySuppliesContainer = view.findViewById(R.id.daily_supplies_container)

        checkAll = view.findViewById(R.id.check_all)
        checkFruit = view.findViewById(R.id.check_fruit)
        checkVegetable = view.findViewById(R.id.check_vegetable)
        checkDelivery = view.findViewById(R.id.check_delivery)
        checkMeat = view.findViewById(R.id.check_meat)
        checkFrozenFood = view.findViewById(R.id.check_frozen_food)
        checkMarineProducts = view.findViewById(R.id.check_marine_products)
        checkDrink = view.findViewById(R.id.check_drink)
        checkDairyProducts = view.findViewById(R.id.check_dairy_products)
        checkConvenientFood = view.findViewById(R.id.check_convenient_food)
        checkDessert = view.findViewById(R.id.check_dessert)
        checkDailySupplies = view.findViewById(R.id.check_daily_supplies)

        allText = view.findViewById(R.id.all_text)
        fruitText = view.findViewById(R.id.fruit_text)
        VegetableText = view.findViewById(R.id.vegetable_text)
        deliveryText = view.findViewById(R.id.delivery_text)
        meatText = view.findViewById(R.id.meat_text)
        frozenFoodText = view.findViewById(R.id.frozen_food_text)
        marineProductsText = view.findViewById(R.id.marine_products_text)
        drinkText = view.findViewById(R.id.drink_text)
        dairyProductsText = view.findViewById(R.id.dairy_products_text)
        convenientFoodText = view.findViewById(R.id.convenient_food_text)
        dessertText = view.findViewById(R.id.dessert_text)
        dailySuppliesText = view.findViewById(R.id.daily_supplies_text)

        // 현재 선택된 필터에 따라 UI 업데이트
        updateSelectedFilterUI()

        // 클릭 리스너 설정
        allContainer.setOnClickListener {
            selectFilter("전체", checkAll, allText)
        }

        fruitContainer.setOnClickListener {
            selectFilter("과일", checkFruit, fruitText)
        }

        VegetableContainer.setOnClickListener {
            selectFilter("채소", checkVegetable, VegetableText)
        }

        deliveryContainer.setOnClickListener {
            selectFilter("배달", checkDelivery, deliveryText)
        }

        meatContainer.setOnClickListener {
            selectFilter("정육", checkMeat, meatText)
        }

        frozenFoodContainer.setOnClickListener {
            selectFilter("냉동식품", checkFrozenFood, frozenFoodText)
        }

        marineProductsContainer.setOnClickListener {
            selectFilter("수산물", checkMarineProducts, marineProductsText)
        }

        drinkContainer.setOnClickListener {
            selectFilter("음료", checkDrink, drinkText)
        }

        dairyProductsContainer.setOnClickListener {
            selectFilter("유제품", checkDairyProducts, dairyProductsText)
        }

        convenientFoodContainer.setOnClickListener {
            selectFilter("간편식", checkConvenientFood, convenientFoodText)
        }

        dessertContainer.setOnClickListener {
            selectFilter("디저트", checkDessert, dessertText)
        }

        dailySuppliesContainer.setOnClickListener {
            selectFilter("생활용품", checkDailySupplies, dailySuppliesText)
        }

        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // 배경을 투명하게 설정
        return dialog
    }

    override fun onStart() {
        super.onStart()
        // 배경을 어둡게 설정
        dialog?.window?.setDimAmount(0.5f) // 어둡게 할 정도 설정
    }

    // 필터 선택 시 UI 업데이트 및 콜백 호출
    private fun selectFilter(
        selectedFilter: String,
        selectedCheckIcon: ImageView,
        selectedTextView: TextView
    ) {
        currentFilter = selectedFilter
        selectedFilterCallback(selectedFilter)
        updateSelectedFilterUI()
        dismiss()
    }

    // 선택된 필터에 따라 체크 아이콘 및 텍스트 속성 표시
    private fun updateSelectedFilterUI() {
        // 모든 체크 아이콘 숨김
        checkAll.visibility = View.GONE
        checkFruit.visibility = View.GONE
        checkVegetable.visibility = View.GONE
        checkDelivery.visibility = View.GONE
        checkMeat.visibility = View.GONE
        checkFrozenFood.visibility = View.GONE
        checkMarineProducts.visibility = View.GONE
        checkDrink.visibility = View.GONE
        checkDairyProducts.visibility = View.GONE
        checkConvenientFood.visibility = View.GONE
        checkDessert.visibility = View.GONE
        checkDailySupplies.visibility = View.GONE

        // 모든 텍스트 속성 초기화
        resetTextViews()

        // 현재 필터에 맞는 체크 아이콘 및 텍스트 속성 표시
        when (currentFilter) {
            "전체" -> {
                checkAll.visibility = View.VISIBLE
                allText.setTextColor(requireContext().getColor(R.color.cherry))
                allText.typeface = ResourcesCompat.getFont(
                    requireContext(),
                    R.font.notosans_kr_medium
                )
            }
            "과일" -> {
                checkFruit.visibility = View.VISIBLE
                fruitText.setTextColor(requireContext().getColor(R.color.cherry))
                fruitText.typeface = ResourcesCompat.getFont(
                    requireContext(),
                    R.font.notosans_kr_medium
                )
            }
            "채소" -> {
                checkVegetable.visibility = View.VISIBLE
                VegetableText.setTextColor(requireContext().getColor(R.color.cherry))
                VegetableText.typeface = ResourcesCompat.getFont(
                    requireContext(),
                    R.font.notosans_kr_medium
                )
            }
            "배달" -> {
                checkDelivery.visibility = View.VISIBLE
                deliveryText.setTextColor(requireContext().getColor(R.color.cherry))
                deliveryText.typeface = ResourcesCompat.getFont(
                    requireContext(),
                    R.font.notosans_kr_medium
                )
            }
            "정육" -> {
                checkMeat.visibility = View.VISIBLE
                meatText.setTextColor(requireContext().getColor(R.color.cherry))
                meatText.typeface = ResourcesCompat.getFont(
                    requireContext(),
                    R.font.notosans_kr_medium
                )
            }
            "냉동식품" -> {
                checkFrozenFood.visibility = View.VISIBLE
                frozenFoodText.setTextColor(requireContext().getColor(R.color.cherry))
                frozenFoodText.typeface = ResourcesCompat.getFont(
                    requireContext(),
                    R.font.notosans_kr_medium
                )
            }
            "수산물" -> {
                checkMarineProducts.visibility = View.VISIBLE
                marineProductsText.setTextColor(requireContext().getColor(R.color.cherry))
                marineProductsText.typeface = ResourcesCompat.getFont(
                    requireContext(),
                    R.font.notosans_kr_medium
                )
            }
            "음료" -> {
                checkDrink.visibility = View.VISIBLE
                drinkText.setTextColor(requireContext().getColor(R.color.cherry))
                drinkText.typeface = ResourcesCompat.getFont(
                    requireContext(),
                    R.font.notosans_kr_medium
                )
            }
            "유제품" -> {
                checkDairyProducts.visibility = View.VISIBLE
                dairyProductsText.setTextColor(requireContext().getColor(R.color.cherry))
                dairyProductsText.typeface = ResourcesCompat.getFont(
                    requireContext(),
                    R.font.notosans_kr_medium
                )
            }
            "간편식" -> {
                checkConvenientFood.visibility = View.VISIBLE
                convenientFoodText.setTextColor(requireContext().getColor(R.color.cherry))
                convenientFoodText.typeface = ResourcesCompat.getFont(
                    requireContext(),
                    R.font.notosans_kr_medium
                )
            }
            "디저트" -> {
                checkDessert.visibility = View.VISIBLE
                dessertText.setTextColor(requireContext().getColor(R.color.cherry))
                dessertText.typeface = ResourcesCompat.getFont(
                    requireContext(),
                    R.font.notosans_kr_medium
                )
            }
            "생활용품" -> {
                checkDailySupplies.visibility = View.VISIBLE
                dailySuppliesText.setTextColor(requireContext().getColor(R.color.cherry))
                dailySuppliesText.typeface = ResourcesCompat.getFont(
                    requireContext(),
                    R.font.notosans_kr_medium
                )
            }
        }
    }

    // 모든 텍스트 속성 초기화
    private fun resetTextViews() {
        allText.setTextColor(requireContext().getColor(R.color.black)) // 기본 색상
        allText.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.notosans_kr_regular) // 기본 폰트

        fruitText.setTextColor(requireContext().getColor(R.color.black)) // 기본 색상
        fruitText.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.notosans_kr_regular) // 기본 폰트

        VegetableText.setTextColor(requireContext().getColor(R.color.black)) // 기본 색상
        VegetableText.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.notosans_kr_regular) // 기본 폰트

        deliveryText.setTextColor(requireContext().getColor(R.color.black))
        deliveryText.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.notosans_kr_regular)

        meatText.setTextColor(requireContext().getColor(R.color.black))
        meatText.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.notosans_kr_regular)

        frozenFoodText.setTextColor(requireContext().getColor(R.color.black))
        frozenFoodText.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.notosans_kr_regular)

        marineProductsText.setTextColor(requireContext().getColor(R.color.black))
        marineProductsText.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.notosans_kr_regular)

        drinkText.setTextColor(requireContext().getColor(R.color.black)) // 기본 색상
        drinkText.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.notosans_kr_regular) // 기본 폰트

        dairyProductsText.setTextColor(requireContext().getColor(R.color.black)) // 기본 색상
        dairyProductsText.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.notosans_kr_regular) // 기본 폰트

        convenientFoodText.setTextColor(requireContext().getColor(R.color.black)) // 기본 색상
        convenientFoodText.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.notosans_kr_regular) // 기본 폰트

        dessertText.setTextColor(requireContext().getColor(R.color.black)) // 기본 색상
        dessertText.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.notosans_kr_regular) // 기본 폰트

        dailySuppliesText.setTextColor(requireContext().getColor(R.color.black)) // 기본 색상
        dailySuppliesText.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.notosans_kr_regular) // 기본 폰트
    }
}