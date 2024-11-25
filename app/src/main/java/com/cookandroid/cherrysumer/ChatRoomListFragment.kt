package com.cookandroid.cherrysumer

import android.os.Bundle
import android.text.Html
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cookandroid.cherrysumer.databinding.FragmentChatRoomListBinding
import com.cookandroid.cherrysumer.models.ChatRoom
import com.cookandroid.cherrysumer.retrofit.ApiCallback
import com.cookandroid.cherrysumer.retrofit.ApiManager
import com.cookandroid.cherrysumer.retrofit.models.ApiResponse
import retrofit2.Response

class ChatRoomListFragment : Fragment() {

    private var _binding: FragmentChatRoomListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ChatRoomListAdapter
    private var selectedFilter: String = "전체" // 기본 필터

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatRoomListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView 초기화
        binding.recyclerViewChatRooms.layoutManager = LinearLayoutManager(requireContext())

        // 초기화된 채팅방 목록 데이터 (임시)
        val chatRooms = listOf<ChatRoom>() // 실제 데이터로 교체 필요
        adapter = ChatRoomListAdapter(chatRooms) { roomId ->
            navigateToChatMessage(roomId) // 클릭 이벤트에서 roomId 전달
        }
        binding.recyclerViewChatRooms.adapter = adapter

        // 필터와 도움말 버튼 설정
        setupFilterButtons()

        // "전체" 버튼을 기본 선택 상태로 설정
        toggleFilterState(binding.filterAll, listOf(binding.filterAll, binding.filterParticipating, binding.filterRecruiting))

        setupHelpButton(binding.helpButton)

        // 채팅방 목록 데이터 로드
        loadChatRooms()
    }

    private fun setupFilterButtons() {
        val buttons = listOf(
            binding.filterAll,
            binding.filterParticipating,
            binding.filterRecruiting
        )

        // 기본 선택 상태 설정
        toggleFilterState(binding.filterAll, buttons)

        binding.filterAll.setOnClickListener {
            toggleFilterState(binding.filterAll, buttons)
            selectedFilter = "전체"
            loadChatRooms() // 필터 변경 시 데이터 로드
        }

        binding.filterParticipating.setOnClickListener {
            toggleFilterState(binding.filterParticipating, buttons)
            selectedFilter = "참여"
            loadChatRooms() // 필터 변경 시 데이터 로드
        }

        binding.filterRecruiting.setOnClickListener {
            toggleFilterState(binding.filterRecruiting, buttons)
            selectedFilter = "모집"
            loadChatRooms() // 필터 변경 시 데이터 로드
        }
    }

    private fun toggleFilterState(selectedView: TextView, buttons: List<TextView>) {
        buttons.forEach { button ->
            button.isSelected = false // 모든 버튼 초기화
        }
        selectedView.isSelected = true // 선택된 버튼 활성화
    }

    private fun setupHelpButton(view: View) {
        binding.helpButton.setOnClickListener {
            val inflater = LayoutInflater.from(requireContext())
            val tooltipView = inflater.inflate(R.layout.tooltip_layout, null)

            // 각 TextView에 HTML 적용
            val dDayTextView: TextView = tooltipView.findViewById(R.id.tooltip_text_d_day)
            val quantityTextView: TextView = tooltipView.findViewById(R.id.tooltip_text_quantity)
            val addTextView: TextView = tooltipView.findViewById(R.id.tooltip_text_add)
            val modifyDeleteTextView: TextView = tooltipView.findViewById(R.id.tooltip_text_modify_delete)

            // 각각의 텍스트 설정 (HTML로 강조)
            val dDayText = "<b>전체</b> : 모든 채팅을 한 곳에서 볼 수 있습니다."
            val quantityText = "<b>참여</b> : 내가 참여하고자 하는 게시글의 작성자들과의 채팅을 확인할 수 있습니다."
            val addText = "<b>모집</b> : 내 게시글에 참여하고자 하는 사람들과의 채팅을 확인할 수 있습니다."
            val modifyDeleteText = "<b>채팅방 나가기</b> : 원하는 항목을 왼쪽으로 슬라이드하면 기능이 보입니다."

            // 텍스트 적용
            dDayTextView.text = Html.fromHtml(dDayText, Html.FROM_HTML_MODE_LEGACY)
            quantityTextView.text = Html.fromHtml(quantityText, Html.FROM_HTML_MODE_LEGACY)
            addTextView.text = Html.fromHtml(addText, Html.FROM_HTML_MODE_LEGACY)
            modifyDeleteTextView.text = Html.fromHtml(modifyDeleteText, Html.FROM_HTML_MODE_LEGACY)

            // 팝업 생성
            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val horizontalMargin = (screenWidth * 0.05).toInt() // 좌우 마진 5%
            val popupWidth = screenWidth - (horizontalMargin * 2) // 팝업 너비 = 전체 너비 - 좌우 마진

            val popupWindow = PopupWindow(
                tooltipView,
                popupWidth,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            )

            // 툴팁 표시 (화면 중앙, Y축 오프셋 포함)
            val yOffset = -330 // 화면 중심에서 위로 이동 (픽셀 단위, 필요에 따라 조정)
            popupWindow.showAtLocation(
                view, // 기준 뷰
                Gravity.CENTER, // 화면 중앙
                0, // X축 오프셋
                yOffset  // Y축 오프셋 (위로 이동)
            )

            // 일정 시간 후 닫기
            tooltipView.postDelayed({ popupWindow.dismiss() }, 5000)
        }
    }

    private fun loadChatRooms() {
        val apiManager = ApiManager()
        apiManager.getChatRooms(selectedFilter, object : ApiCallback<List<ChatRoom>> {
            override fun onSuccess(apiResponse: ApiResponse<List<ChatRoom>>?) {
                val chatRooms = apiResponse?.data ?: emptyList()
                adapter.updateChatRooms(chatRooms)
                super.onSuccess(apiResponse)
            }

            override fun onError(response: Response<ApiResponse<List<ChatRoom>>>) {
                Toast.makeText(requireContext(), "Failed to load chat rooms", Toast.LENGTH_SHORT).show()
                super.onError(response)
            }

            override fun onFailure(throwable: Throwable) {
                Toast.makeText(requireContext(), "Network error: ${throwable.message}", Toast.LENGTH_SHORT).show()
                super.onFailure(throwable)
            }
        })
    }

    private fun navigateToChatMessage(roomId: String) {
        val fragment = ChatMessageFragment().apply {
            arguments = Bundle().apply {
                putString("ROOM_ID", roomId)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지
    }
}
