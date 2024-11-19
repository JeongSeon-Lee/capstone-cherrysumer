package com.cookandroid.cherrysumer

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.cookandroid.cherrysumer.databinding.FragmentSearchBinding
import com.cookandroid.cherrysumer.retrofit.ApiManager
import com.cookandroid.cherrysumer.retrofit.models.ApiResponse
import com.cookandroid.cherrysumer.retrofit.ApiCallback
import com.cookandroid.cherrysumer.retrofit.models.RecentSearchItem
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.JsonObject

class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private var useFirstLayout = true
    private var isFirstOpen = true
    private var currentQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true) // 프래그먼트에서 메뉴를 생성할 수 있도록 설정
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        updateLayout() // 현재 레이아웃 업데이트
        return binding.root
    }

    // 메뉴 생성
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (!useFirstLayout) { // 두 번째 레이아웃에서만 메뉴 생성
            inflater.inflate(R.menu.action_search_menu, menu)
            val searchItem = menu.findItem(R.id.menu_action_search)
            setupSearchView(searchItem) // SearchView 설정
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    // 메뉴 아이템 선택 시 호출
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { // 업 버튼 클릭 시
                handleUpButton() // 뒤로 가기 동작 처리
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupSearchView(searchItem: MenuItem) {
        val searchView = searchItem.actionView as? androidx.appcompat.widget.SearchView

        searchView?.apply {
            // SearchView 포커스 변화 이벤트 처리
            setOnQueryTextFocusChangeListener { _, hasFocus ->
                if (hasFocus && isFirstOpen) {
                    clearFocus() // 초기 포커스 제거
                    isFirstOpen = false
                }
            }

            isIconified = false // 아이콘 상태 해제 (항상 보이게)
            setOnCloseListener {
                setQuery("", false) // 검색어 초기화
                isIconified = false
                true
            }

            // 검색어 입력 및 제출 이벤트 처리
            setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let { navigateToInventoryListFragment(it) }
                    hideKeyboard(this@apply) // 키보드 숨기기
                    return true
                }

                override fun onQueryTextChange(newText: String?) = true
            })

            setQuery(currentQuery, true) // 현재 쿼리 설정
        }
    }

    private fun handleUpButton() {
        if (!useFirstLayout) {
            useFirstLayout = true
            updateLayout()
        } else {
            activity?.onBackPressed()
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun navigateToInventoryListFragment(query: String) {
        try {
            val fragment = InventoryListFragment.newInstance("", query)
            parentFragmentManager.beginTransaction()
                .replace(R.id.inventory_list_content, fragment)
                .addToBackStack(null)
                .commitAllowingStateLoss()
        } catch (e: Exception) {
            Log.e("SearchFragment", "Error navigating to InventoryListFragment: ${e.message}", e)
        }
    }

    private fun updateLayout() {
        // 사용할 레이아웃을 선택
        val layoutResId = if (useFirstLayout) R.layout.fragment_search_first else R.layout.fragment_search_second
        val newView = layoutInflater.inflate(layoutResId, binding.root, false)

        // 레이아웃에 따라 초기 설정
        if (useFirstLayout) {
            setupFirstLayout(newView)
        } else {
            setupToolbar(newView)
        }

        // 기존 레이아웃을 제거하고 새 레이아웃 추가
        binding.root.removeAllViews()
        binding.root.addView(newView)
    }

    private fun setupFirstLayout(view: View) {
        setupToolbar(view) // 툴바 설정
        val searchQueryEditText = view.findViewById<EditText>(R.id.search_query_first)
        val chipGroup = view.findViewById<ChipGroup>(R.id.recent_search_chip_group)

        // 최근 검색어 가져오기 및 표시
        getRecentSearches(chipGroup)

        // 검색 버튼 클릭 이벤트 처리
        view.findViewById<ImageButton>(R.id.search_button_first).setOnClickListener {
            val query = searchQueryEditText.text.toString()
            if (query.isNotEmpty()) { // 입력값이 비어있지 않으면
                currentQuery = query
                useFirstLayout = false // 두 번째 레이아웃으로 전환
                updateLayout()
            } else {
                showToast("검색어를 입력하세요.")
            }
        }

        // 키보드 IME 액션 처리
        searchQueryEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                view.findViewById<ImageButton>(R.id.search_button_first).performClick()
                true
            } else false
        }
    }

    private fun setupToolbar(view: View) {
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        (activity as? AppCompatActivity)?.apply {
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true) // 업 버튼 활성화
            supportActionBar?.title = "" // 제목 설정 없음
        }
    }

    private fun setupRecentSearchChips(chipGroup: ChipGroup, recentSearches: List<RecentSearchItem>) {
        chipGroup.removeAllViews() // 기존 Chip 제거
        recentSearches.forEach { item ->
            val chip = Chip(context).apply {
                text = item.name
                isCloseIconVisible = true
                setTextColor(ContextCompat.getColor(context, R.color.black))
                chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white))
                chipStrokeColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.light_gray))
                chipStrokeWidth = 1f
                chipCornerRadius = 24f

                // 클릭 이벤트
                setOnClickListener {
                    currentQuery = item.name
                    useFirstLayout = false
                    updateLayout() // 두 번째 레이아웃으로 전환
                }

                // 닫기 아이콘 클릭 이벤트
                setOnCloseIconClickListener {
                    deleteRecentSearch(item) { success ->
                        if (success) chipGroup.removeView(this)
                        else showToast("삭제에 실패했습니다.")
                    }
                }
            }
            // 디버그 메시지 추가
            Log.d("ChipDebug", "Text: ${chip.text}, Background: ${chip.chipBackgroundColor}, Stroke: ${chip.chipStrokeColor}")
            chipGroup.addView(chip) // ChipGroup에 추가
        }
    }

    private fun getRecentSearches(chipGroup: ChipGroup) {
        ApiManager().getRecentSearchLogs(object : ApiCallback<List<RecentSearchItem>> {
            override fun onSuccess(apiResponse: ApiResponse<List<RecentSearchItem>>?) {
                val searchItems = apiResponse?.data.orEmpty()
                    .filter { it.state == true }
                setupRecentSearchChips(chipGroup, searchItems) // 검색어를 Chip으로 표시
                super.onSuccess(apiResponse)
            }
        })
    }

    private fun deleteRecentSearch(item: RecentSearchItem, onResult: (Boolean) -> Unit) {
        val jsonObject = JsonObject().apply {
            addProperty("name", item.name)
            addProperty("createdAt", item.createdAt)
            addProperty("state", item.state)
        }
        ApiManager().deleteRecentSearchLog(jsonObject, object : ApiCallback<Unit> {
            override fun onSuccess(apiResponse: ApiResponse<Unit>?) {
                onResult(true)
                super.onSuccess(apiResponse)
            }

            override fun onError(response: retrofit2.Response<ApiResponse<Unit>>) {
                onResult(false)
                super.onError(response)
            }

            override fun onFailure(throwable: Throwable) {
                onResult(false)
                super.onFailure(throwable)
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
