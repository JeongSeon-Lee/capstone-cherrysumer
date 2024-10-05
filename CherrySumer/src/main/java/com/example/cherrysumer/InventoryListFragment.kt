package com.example.cherrysumer

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.PopupWindow
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cherrysumer.databinding.FragmentInventoryListBinding
import com.example.cherrysumer.retrofit.models.ApiResponse
import com.example.cherrysumer.retrofit.ApiService
import com.example.cherrysumer.retrofit.MyApplication
import com.example.cherrysumer.retrofit.models.InventoryItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InventoryListFragment : Fragment() {

    private var param: String? = null
    private lateinit var inventoryItems: List<InventoryItem>
    private lateinit var binding: FragmentInventoryListBinding
    private lateinit var filteredItems: List<InventoryItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param = it.getString(ARG_PARAM)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInventoryListBinding.inflate(inflater, container, false)
        val networkService: ApiService = MyApplication.networkService

        // 초기 UI 설정
        when (param) {
            "냉장실", "냉동실", "실외 저장소" -> {
                binding.helpButton.visibility = View.VISIBLE
                binding.stockLocationSpinner.visibility = View.GONE
            }
            else -> {
                binding.helpButton.visibility = View.GONE
                binding.stockLocationSpinner.visibility = View.VISIBLE
            }
        }

        binding.helpButton.setOnClickListener {
            val inflater = LayoutInflater.from(requireContext())
            val tooltipView = inflater.inflate(R.layout.tooltip_layout, null)

            // PopupWindow 생성
            val popupWindow = PopupWindow(
                tooltipView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            )
            popupWindow.showAsDropDown(it, 0, 0)

            // 몇 초 뒤에 팝업을 닫도록 설정
            tooltipView.postDelayed({ popupWindow.dismiss() }, 5000)
        }

        // 서버에서 데이터 요청
        fetchInventoryItems(networkService, binding)

        return binding.root
    }

    private fun fetchInventoryItems(networkService: ApiService, binding: FragmentInventoryListBinding) {
        networkService.getInventoryItems().enqueue(object : Callback<ApiResponse<List<InventoryItem>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<InventoryItem>>>,
                response: Response<ApiResponse<List<InventoryItem>>>
            ) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.isSuccess) {
                        inventoryItems = if (param in listOf("냉장실", "냉동실", "실외 저장소")) {
                            apiResponse.data?.filter { it.stockLocation == param }?.sortedByDescending { it.createdAt } ?: listOf() ?: listOf()
                        } else {
                            apiResponse.data?.filter {
                                it.productName?.contains(param ?: "", ignoreCase = true) == true
                            }?.sortedByDescending { it.createdAt } ?: listOf()
                        }

                        Log.d("InventoryListFragment", "Received ApiResponse: $apiResponse")
                        Log.d("InventoryListFragment", "Received Inventory Items: $inventoryItems")

                        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
                        binding.recyclerView.adapter = InventoryAdapter(inventoryItems)

                        if (activity != null) Toast.makeText(activity, "데이터 로딩 성공", Toast.LENGTH_SHORT).show()

                        setupFilters(binding)
                        setupSwipeController(binding)
                    } else {
                        handleApiError(apiResponse)
                    }
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<InventoryItem>>>, t: Throwable) {
                Log.e("InventoryListFragment", "Network Failure: ${t.message}", t)
                if (activity != null)
                    Toast.makeText(activity, "Network Failure: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupFilters(binding: FragmentInventoryListBinding) {
        // 필터링할 아이템 리스트를 저장하는 변수
        filteredItems = inventoryItems

        // 카테고리 필터링
        binding.categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedCategory = binding.categorySpinner.selectedItem.toString()

                // 카테고리 필터링
                filteredItems = if (selectedCategory == "카테고리") {
                    inventoryItems // 모든 아이템
                } else {
                    inventoryItems.filter { it.category == selectedCategory } // 선택한 카테고리로 필터링
                }

                // 필터링된 아이템을 기반으로 업데이트
                binding.recyclerView.adapter = InventoryAdapter(filteredItems)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // 아무것도 선택되지 않았을 때의 기본 동작
                binding.recyclerView.adapter = InventoryAdapter(filteredItems)
            }
        }

        // 정렬 기능
        binding.sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedSort = binding.sortSpinner.selectedItem.toString()
                filteredItems = when (selectedSort) {
                    "최신 등록 순" -> filteredItems.sortedByDescending { it.createdAt }
                    "만료 임박 순" -> filteredItems.sortedBy { it.expirationDate }
                    "만료 여유 순" -> filteredItems.sortedByDescending { it.expirationDate }
                    "재고 많은 순" -> filteredItems.sortedByDescending { it.quantity }
                    "재고 적은 순" -> filteredItems.sortedBy { it.quantity }
                    else -> filteredItems
                }

                // 필터링된 아이템을 기반으로 업데이트
                binding.recyclerView.adapter = InventoryAdapter(filteredItems)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                binding.recyclerView.adapter = InventoryAdapter(filteredItems)
            }
        }

        // 재고 위치 필터링
        binding.stockLocationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedStockLocation = binding.stockLocationSpinner.selectedItem.toString()
                filteredItems = when (selectedStockLocation) {
                    "냉장실", "냉동실", "실외 저장소" -> filteredItems.filter { it.stockLocation == selectedStockLocation }
                    else -> filteredItems // 다른 재고 위치는 필터링하지 않음
                }

                // 필터링된 아이템을 기반으로 업데이트
                binding.recyclerView.adapter = InventoryAdapter(filteredItems)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                binding.recyclerView.adapter = InventoryAdapter(filteredItems)
            }
        }
    }

    private fun setupSwipeController(binding: FragmentInventoryListBinding) {
        // 스와이프 컨트롤러 설정
        val swipeController = SwipeController()
        swipeController.setButtonActionListener(object : SwipeControllerActions {
            override fun onRightClicked(position: Int) {
                // 오른쪽 클릭 시 아이템 삭제 확인 대화상자 표시
                val itemToDelete = filteredItems[position]
                Log.d("SwipeController", "Deleting item: ${itemToDelete.productName} with ID: ${itemToDelete.id}")

                // 삭제 확인 대화상자
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("삭제 확인")
                builder.setMessage("정말 ${itemToDelete.productName}을(를) 삭제하시겠습니까?")
                builder.setPositiveButton("확인") { _, _ ->
                    deleteInventoryItem(itemToDelete) // 아이템 삭제 메소드 호출
                }
                builder.setNegativeButton("취소", null) // 취소 버튼
                builder.show()
            }
        })
        val itemTouchHelper = ItemTouchHelper(swipeController)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
        binding.recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                swipeController.onDraw(c)
            }
        })
    }

    private fun deleteInventoryItem(item: InventoryItem) {
        val networkService: ApiService = MyApplication.networkService
        Log.d("DeleteInventoryItem", "Deleting item with ID: ${item.id}") // 추가된 로그
        networkService.deleteInventoryItem(item.id).enqueue(object : Callback<ApiResponse<Unit>> {
            override fun onResponse(call: Call<ApiResponse<Unit>>, response: Response<ApiResponse<Unit>>) {
                if (response.isSuccessful) {
                    Toast.makeText(activity, "아이템이 삭제되었습니다.", Toast.LENGTH_SHORT).show()

                    // 삭제된 아이템을 제외한 새로운 filteredItems 생성
                    filteredItems = filteredItems.filter { it.id != item.id }

                    // RecyclerView 어댑터 업데이트
                    binding.recyclerView.adapter = InventoryAdapter(filteredItems)

                    // 아이템 삭제 후 다시 데이터 요청
                    // fetchInventoryItems(networkService, binding)
                } else {
                    // 에러 로그 추가
                    Log.e("DeleteInventoryItem", "Error deleting item: ${response.errorBody()?.string()}")
                    Toast.makeText(activity, "삭제 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<Unit>>, t: Throwable) {
                Toast.makeText(activity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun handleApiError(apiResponse: ApiResponse<List<InventoryItem>>?) {
        val errorMessage = apiResponse?.message ?: "Unknown error"
        Log.e("InventoryListFragment", "Error: ${apiResponse?.code} - $errorMessage")
        if (activity != null) Toast.makeText(activity, "Error: ${apiResponse?.message}", Toast.LENGTH_SHORT).show()
    }

    private fun handleErrorResponse(response: Response<ApiResponse<List<InventoryItem>>>) {
        val errorBody = response.errorBody()?.string()
        if (errorBody != null) {
            try {
                Log.e("InventoryListFragment", "Error Body: $errorBody")
                if (activity != null) Toast.makeText(activity, "Error Body: $errorBody", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                if (activity != null) Toast.makeText(activity, "Unknown error", Toast.LENGTH_SHORT).show()
            }
        } else {
            if (activity != null) Toast.makeText(activity, "Unknown error", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val ARG_PARAM = "param"

        fun newInstance(param: String): InventoryListFragment {
            return InventoryListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM, param)
                }
            }
        }
    }
}


enum class ButtonsState {
    GONE,
    LEFT_VISIBLE,
    RIGHT_VISIBLE
}

class SwipeController : ItemTouchHelper.Callback() {
    private var swipeBack : Boolean = false // 스크롤 시 끝 지정
    // 버튼의 상태를 나타냄
    private var buttonShowedState  : ButtonsState = ButtonsState.GONE
    private val buttonWidth : Float = 300F // 나타낼 버튼의 크기
    // 현재 보여지는 버튼
    private var buttonInstance : RectF? = null
    // 버튼 클릭 리스너
    private var buttonActions:SwipeControllerActions? =null
    fun setButtonActionListener(listener:SwipeControllerActions){
        this.buttonActions = listener
    }
    // 현재 선택 뷰 홀더
    private var currentItemViewHolder : RecyclerView.ViewHolder? = null

    @SuppressLint("RtlHardcoded")
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val swipeFlags = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        return makeMovementFlags(0,swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}


    override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {

        if (swipeBack){
            swipeBack = buttonShowedState != ButtonsState.GONE
            return 0
        }

        return super.convertToAbsoluteDirection(flags, layoutDirection)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        // 스와이프 상태일 때 적용
        if (actionState==ACTION_STATE_SWIPE){
            if (buttonShowedState != ButtonsState.GONE){
                var dX = dX
                // 스와이프 뷰를 정지시킴
                if (buttonShowedState == ButtonsState.LEFT_VISIBLE) dX = Math.max(dX,buttonWidth)
                if (buttonShowedState == ButtonsState.RIGHT_VISIBLE) dX = Math.min(dX,-buttonWidth)
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }else{
                setTouchListener(c,recyclerView,viewHolder,dX,dY
                    ,actionState, isCurrentlyActive)
            }
        }
        if (buttonShowedState == ButtonsState.GONE){
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
        currentItemViewHolder = viewHolder
    }

    // 기본 터치 리스너
    private fun setTouchListener(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ){
        recyclerView.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                p1?.let{ event ->
                    swipeBack = event.action == MotionEvent.ACTION_CANCEL
                            || event.action == MotionEvent.ACTION_UP

                    // 얼마나 많이 드래그 했는지 확인
                    // 좌, 우 스와이프 상태 확인
                    if (swipeBack){
                        if (dX < - buttonWidth)
                            buttonShowedState = ButtonsState.RIGHT_VISIBLE
                        else if ( dX > buttonWidth)
                            buttonShowedState = ButtonsState.LEFT_VISIBLE

                        if (buttonShowedState!= ButtonsState.GONE){
                            setTouchDownListener(c,recyclerView,viewHolder,
                                dX, dY, actionState, isCurrentlyActive)
                            setItemClickable(recyclerView,false)
                        }
                    }
                }
                return false

            }

        })
    }

    // 터치 시작 리스너
    private fun setTouchDownListener(
        c: Canvas, recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
        actionState: Int, isCurrentlyActive: Boolean
    ){
        recyclerView.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                p1?.let{event->
                    if (event.action == MotionEvent.ACTION_DOWN){
                        setTouchUpListener(c,recyclerView, viewHolder,
                            dX, dY, actionState, isCurrentlyActive)
                    }
                }
                return false
            }

        })
    }

    // 터치 중지 리스너 -> x좌표 고정
    private fun setTouchUpListener(
        c: Canvas, recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
        actionState: Int, isCurrentlyActive: Boolean
    ){
        recyclerView.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                p1?.let {event ->
                    if (event.action == MotionEvent.ACTION_UP){
                        this@SwipeController.onChildDraw(c,recyclerView, viewHolder,
                            0F, dY, actionState, isCurrentlyActive)
                        // 터치 리스너 재정의
                        recyclerView.setOnTouchListener(object : View.OnTouchListener {
                            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                                return false
                            }

                        })
                        setItemClickable(recyclerView,true)
                        swipeBack = false

                        if (buttonActions != null && buttonInstance != null
                            && buttonInstance!!.contains(event.x,event.y)){
                            if (buttonShowedState == ButtonsState.LEFT_VISIBLE){
                                buttonActions?.onLeftClicked(viewHolder.absoluteAdapterPosition)
                            }
                            else if (buttonShowedState == ButtonsState.RIGHT_VISIBLE){
                                Log.d("right clikk!! ","delete")
                                buttonActions?.onRightClicked(viewHolder.absoluteAdapterPosition)
                            }
                        }
                        buttonShowedState = ButtonsState.GONE
                        currentItemViewHolder = null
                    }
                }
                return false
            }

        })
    }

    // 버튼 그리기
    private fun drawButtons(c : Canvas, viewHolder: RecyclerView.ViewHolder){
        val buttonWidthWithoutPadding = buttonWidth - 20
        val corners = 16F
        val itemView = viewHolder.itemView
        val p  = Paint()

        // 왼쪽 버튼 그리기
        val leftButton = RectF(itemView.left.toFloat(),itemView.top.toFloat(),
            itemView.left+buttonWidthWithoutPadding,itemView.bottom.toFloat())
        p.color = Color.BLUE
        c.drawRoundRect(leftButton,corners,corners,p)
        drawText("EDIT", c,leftButton,p)

        // 오른쪽 버튼 그리기
        val rightButton = RectF(itemView.right-buttonWidthWithoutPadding,
            itemView.top.toFloat(),itemView.right.toFloat(),itemView.bottom.toFloat())
        p.color = Color.RED
        c.drawRoundRect(rightButton,corners,corners,p)
        drawText("DELETE",c,rightButton,p)

        buttonInstance = null
        if (buttonShowedState == ButtonsState.LEFT_VISIBLE){
            buttonInstance = leftButton
        }else if (buttonShowedState == ButtonsState.RIGHT_VISIBLE){
            buttonInstance = rightButton
        }
    }

    // 버튼의 텍스트 그리기
    private fun drawText(text:String,c:Canvas,button:RectF,p : Paint){
        val textSize : Float = 60F
        p.color = Color.WHITE
        p.isAntiAlias = true
        p.textSize = textSize
        val textWidth : Float = p.measureText(text)
        c.drawText(text, button.centerX()-(textWidth/2),button.centerY()+(textSize/2),p)
    }

    private fun setItemClickable(recyclerView: RecyclerView,boolean: Boolean){
        for (i in 0 until recyclerView.childCount){
            recyclerView.getChildAt(i).isClickable = boolean
        }
    }

    public fun onDraw(c: Canvas){
        currentItemViewHolder?.let { drawButtons(c, it) }
    }
}

interface SwipeControllerActions {

    fun onLeftClicked(position :Int){

    }

    fun onRightClicked(position: Int){

    }
}
