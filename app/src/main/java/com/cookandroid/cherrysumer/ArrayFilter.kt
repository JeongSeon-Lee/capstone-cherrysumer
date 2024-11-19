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

class ArrayFilter(
    private val selectedFilterCallback: (String) -> Unit,
    private var currentFilter: String // 현재 선택된 필터를 저장하는 변수
) : BottomSheetDialogFragment() {

    private lateinit var latestContainer: LinearLayout
    private lateinit var recommendedContainer: LinearLayout
    private lateinit var popularContainer: LinearLayout
    private lateinit var lowPriceContainer: LinearLayout
    private lateinit var highPriceContainer: LinearLayout

    private lateinit var checkLatest: ImageView
    private lateinit var checkRecommended: ImageView
    private lateinit var checkPopular: ImageView
    private lateinit var checkLowPrice: ImageView
    private lateinit var checkHighPrice: ImageView

    private lateinit var filterLatestText: TextView
    private lateinit var filterRecommendedText: TextView
    private lateinit var filterPopularText: TextView
    private lateinit var filterLowPriceText: TextView
    private lateinit var filterHighPriceText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.arr_filter, container, false)

        // 컨테이너 초기화
        latestContainer = view.findViewById(R.id.filter_latest_container)
        recommendedContainer = view.findViewById(R.id.filter_recommended_container)
        popularContainer = view.findViewById(R.id.filter_popular_container)
        lowPriceContainer = view.findViewById(R.id.filter_low_price_container)
        highPriceContainer = view.findViewById(R.id.filter_high_price_container)

        // 체크 아이콘 및 텍스트뷰 초기화
        checkLatest = view.findViewById(R.id.check_latest)
        checkRecommended = view.findViewById(R.id.check_recommended)
        checkPopular = view.findViewById(R.id.check_popular)
        checkLowPrice = view.findViewById(R.id.check_low_price)
        checkHighPrice = view.findViewById(R.id.check_high_price)

        filterLatestText = view.findViewById(R.id.filter_latest_text)
        filterRecommendedText = view.findViewById(R.id.filter_recommended_text)
        filterPopularText = view.findViewById(R.id.filter_popular_text)
        filterLowPriceText = view.findViewById(R.id.filter_low_price_text)
        filterHighPriceText = view.findViewById(R.id.filter_high_price_text)

        // 현재 선택된 필터에 따라 UI 업데이트
        updateSelectedFilterUI()

        // 클릭 리스너 설정
        latestContainer.setOnClickListener {
            selectFilter("최신순", checkLatest, filterLatestText)
        }

        recommendedContainer.setOnClickListener {
            selectFilter("추천순", checkRecommended, filterRecommendedText)
        }

        popularContainer.setOnClickListener {
            selectFilter("인기순", checkPopular, filterPopularText)
        }

        lowPriceContainer.setOnClickListener {
            selectFilter("저가순", checkLowPrice, filterLowPriceText)
        }

        highPriceContainer.setOnClickListener {
            selectFilter("고가순", checkHighPrice, filterHighPriceText)
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
        checkLatest.visibility = View.GONE
        checkRecommended.visibility = View.GONE
        checkPopular.visibility = View.GONE
        checkLowPrice.visibility = View.GONE
        checkHighPrice.visibility = View.GONE

        // 모든 텍스트 속성 초기화
        resetTextViews()

        // 현재 필터에 맞는 체크 아이콘 및 텍스트 속성 표시
        when (currentFilter) {
            "최신순" -> {
                checkLatest.visibility = View.VISIBLE
                filterLatestText.setTextColor(requireContext().getColor(R.color.cherry)) // 선택된 색상
                filterLatestText.typeface = ResourcesCompat.getFont(
                    requireContext(),
                    R.font.notosans_kr_medium
                ) // 선택된 폰트
            }

            "추천순" -> {
                checkRecommended.visibility = View.VISIBLE
                filterRecommendedText.setTextColor(requireContext().getColor(R.color.cherry))
                filterRecommendedText.typeface =
                    ResourcesCompat.getFont(requireContext(), R.font.notosans_kr_medium)
            }

            "인기순" -> {
                checkPopular.visibility = View.VISIBLE
                filterPopularText.setTextColor(requireContext().getColor(R.color.cherry))
                filterPopularText.typeface =
                    ResourcesCompat.getFont(requireContext(), R.font.notosans_kr_medium)
            }

            "저가순" -> {
                checkLowPrice.visibility = View.VISIBLE
                filterLowPriceText.setTextColor(requireContext().getColor(R.color.cherry))
                filterLowPriceText.typeface =
                    ResourcesCompat.getFont(requireContext(), R.font.notosans_kr_medium)
            }

            "고가순" -> {
                checkHighPrice.visibility = View.VISIBLE
                filterHighPriceText.setTextColor(requireContext().getColor(R.color.cherry))
                filterHighPriceText.typeface =
                    ResourcesCompat.getFont(requireContext(), R.font.notosans_kr_medium)
            }
        }
    }

    // 모든 텍스트 속성 초기화
    private fun resetTextViews() {
        filterLatestText.setTextColor(requireContext().getColor(R.color.black)) // 기본 색상
        filterLatestText.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.notosans_kr_regular) // 기본 폰트

        filterRecommendedText.setTextColor(requireContext().getColor(R.color.black))
        filterRecommendedText.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.notosans_kr_regular)

        filterPopularText.setTextColor(requireContext().getColor(R.color.black))
        filterPopularText.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.notosans_kr_regular)

        filterLowPriceText.setTextColor(requireContext().getColor(R.color.black))
        filterLowPriceText.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.notosans_kr_regular)

        filterHighPriceText.setTextColor(requireContext().getColor(R.color.black))
        filterHighPriceText.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.notosans_kr_regular)
    }
}