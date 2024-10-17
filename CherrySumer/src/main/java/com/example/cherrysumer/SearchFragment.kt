package com.example.cherrysumer

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.cherrysumer.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private var useFirstLayout: Boolean = true // 초기 레이아웃 상태
    private var isFirstOpen: Boolean = true // 프래그먼트가 처음 열렸는지 여부
    private var currentQuery: String = "" // 현재 쿼리 문을 저장하는 변수

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true) // 메뉴 생성을 허용합니다.
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        // 레이아웃 설정
        updateLayout(inflater)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // 메뉴 리소스를 Inflate합니다.
        if (!useFirstLayout) { // 두 번째 레이아웃일 때만 SearchView 설정
            inflater.inflate(R.menu.action_search_menu, menu)

            // SearchView 설정
            val searchItem = menu.findItem(R.id.menu_action_search)
            val searchView = searchItem.actionView as? androidx.appcompat.widget.SearchView

            searchView?.setOnQueryTextFocusChangeListener { _, hasFocus ->
                if (hasFocus && isFirstOpen) {
                    searchView.clearFocus() // 포커스 제거
                    isFirstOpen = false
                }
            }

            searchView?.isIconified = false // 서치 뷰를 항상 보이게 설정

            searchView?.setOnCloseListener {
                // 쿼리 초기화
                searchView.setQuery("", false) // 쿼리 초기화
                searchView?.isIconified = false
                true // false를 반환하여 SearchView가 닫히지 않도록 함
            }

            searchView?.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let {
                        // 쿼리 제출 시 InventoryListFragment로 이동
                        val inventoryListFragment = InventoryListFragment.newInstance("", query) // 쿼리를 인수로 전달하여 생성
                        // FragmentTransaction을 사용하여 Fragment 교체
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.inventory_list_content, inventoryListFragment) // nav_content는 프래그먼트를 교체할 컨테이너 ID입니다.
                            .addToBackStack(null) // 백스택에 추가하여 이전 프래그먼트로 돌아갈 수 있게 설정
                            .commit()
                    }

                    // 키보드 숨기기
                    val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.hideSoftInputFromWindow(searchView.windowToken, 0)

                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    // 쿼리 텍스트가 변경될 때의 동작을 정의 (필요시 사용)
                    return true
                }
            })

            searchView?.setQuery(currentQuery, true) // 현재 쿼리 설정
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { // 업 버튼 클릭 시
                Log.d("SearchFragment", "Up button clicked") // 업 버튼 클릭 로그

                // 키보드 숨기기
                val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(binding.root.windowToken, 0)

                // SearchView의 포커스 제거
                val searchView = (activity as? AppCompatActivity)?.findViewById<androidx.appcompat.widget.SearchView>(R.id.menu_action_search)
                searchView?.clearFocus()

                isFirstOpen = true // 업 버튼 클릭 시 isFirstOpen을 false로 설정
                activity?.onBackPressed() // 이전 프래그먼트로 돌아가기
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun updateLayout(inflater: LayoutInflater) {
        val searchView: View = if (useFirstLayout) {
            // 첫 번째 레이아웃을 인플레이트
            inflater.inflate(R.layout.fragment_search_first, binding.root, false).apply {
                val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
                (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
                (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
                (activity as? AppCompatActivity)?.supportActionBar?.title = ""

                val searchQueryEditText = findViewById<EditText>(R.id.search_query_first)
                findViewById<ImageButton>(R.id.search_button_first).setOnClickListener {
                    // 입력된 값을 가져와서 null 또는 빈 문자열인지 확인
                    val query = searchQueryEditText.text.toString()
                    if (query.isNotEmpty()) { // 쿼리가 비어있지 않을 경우
                        currentQuery = query
                        useFirstLayout = false
                        updateLayoutWithQuery(inflater, query) // 쿼리와 함께 레이아웃 업데이트
                    } else {
                        // 쿼리가 비어있을 때 사용자에게 알림
                        Toast.makeText(activity, "검색어를 입력하세요.", Toast.LENGTH_SHORT).show()
                    }
                }

                searchQueryEditText.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) { // "확인" 버튼이 눌린 경우
                        // 입력된 값을 가져와서 null 또는 빈 문자열인지 확인
                        val query = searchQueryEditText.text.toString()
                        if (query.isNotEmpty()) { // 쿼리가 비어있지 않을 경우
                            findViewById<ImageButton>(R.id.search_button_first).performClick() // 버튼 클릭
                        } else {
                            // 쿼리가 비어있을 때 사용자에게 알림
                            Toast.makeText(activity, "검색어를 입력하세요.", Toast.LENGTH_SHORT).show()
                        }
                        true
                    } else {
                        false
                    }
                }
            }
        } else {
            // 두 번째 레이아웃을 인플레이트
            inflater.inflate(R.layout.fragment_search_second, binding.root, false).apply {
                val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
                (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
                (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
                (activity as? AppCompatActivity)?.supportActionBar?.title = ""
            }
        }

        // 기존 뷰를 제거하고 새로운 뷰 추가
        binding.root.removeAllViews() // 기존 뷰 제거
        binding.root.addView(searchView) // 새로운 뷰 추가
    }

    private fun updateLayoutWithQuery(inflater: LayoutInflater, query: String) {
        // 두 번째 레이아웃을 인플레이트
        val searchView = inflater.inflate(R.layout.fragment_search_second, binding.root, false).apply {
            val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
            (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
            (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            (activity as? AppCompatActivity)?.supportActionBar?.title = ""
        }

        // 기존 뷰를 제거하고 새로운 뷰 추가
        binding.root.removeAllViews() // 기존 뷰 제거
        binding.root.addView(searchView) // 새로운 뷰 추가
    }
}

