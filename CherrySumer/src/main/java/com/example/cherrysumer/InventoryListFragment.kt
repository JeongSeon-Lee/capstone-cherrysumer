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
import com.example.cherrysumer.retrofit.ApiCallback
import com.example.cherrysumer.retrofit.ApiManager
import com.example.cherrysumer.retrofit.models.ApiResponse
import com.example.cherrysumer.retrofit.models.InventoryItem
import retrofit2.Response

class InventoryListFragment : Fragment() {
    private var stockLocation: String? = null
    private var searchQuery: String? = null
    private lateinit var binding: FragmentInventoryListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            stockLocation = it.getString(ARG_PARAM1)
            searchQuery = it.getString(ARG_PARAM2)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInventoryListBinding.inflate(inflater, container, false)

        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.adapter = InventoryAdapter(emptyList())

        setupFiltersVisibility()
        setupHelpButton(binding.helpButton)

        fetchInventoryItems { isSuccess ->
            setupFilters()
            setupSwipeController()
        }

        return binding.root
    }

    private fun setupFiltersVisibility() {
        when (stockLocation) {
            "냉장실", "냉동실", "실외 저장소" -> {
                binding.helpButton.visibility = View.VISIBLE
                binding.stockLocationSpinner.visibility = View.GONE
            }
            else -> {
                binding.helpButton.visibility = View.GONE
                binding.stockLocationSpinner.visibility = View.VISIBLE
            }
        }
    }

    private fun setupHelpButton(view: View) {
        binding.helpButton.setOnClickListener {
            val inflater = LayoutInflater.from(requireContext())
            val tooltipView = inflater.inflate(R.layout.tooltip_layout, null)
            val popupWindow = PopupWindow(
                tooltipView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            )
            popupWindow.showAsDropDown(view, 0, 0)
            tooltipView.postDelayed({ popupWindow.dismiss() }, 5000)
        }
    }

    private fun fetchInventoryItems(callback: (Boolean) -> Unit = { false }) {
        if (stockLocation in listOf("냉장실", "냉동실", "실외 저장소")) {
            ApiManager().listInventoryItems(stockLocation ?: "냉장실", object : ApiCallback<List<InventoryItem>> {
                override fun onSuccess(apiResponse: ApiResponse<List<InventoryItem>>?) {
                    val inventoryItems = apiResponse?.data?.sortedByDescending { it.createdAt } ?: emptyList()
                    binding.recyclerView.adapter = InventoryAdapter(inventoryItems)
                    super.onSuccess(apiResponse)
                    callback(true)
                }
                override fun onError(response: Response<ApiResponse<List<InventoryItem>>>) {
                    Toast.makeText(activity, "불러오기에 실패했습니다 (HTTP ${response.code()})", Toast.LENGTH_SHORT).show()
                    super.onError(response)
                }
                override fun onFailure(throwable: Throwable) {
                    Toast.makeText(activity, "네트워크 오류: ${throwable.message}", Toast.LENGTH_SHORT).show()
                    super.onFailure(throwable)
                }
            })
        } else {
            Log.d("InventoryListFragment", "else")
            ApiManager().searchInventoryItems(searchQuery ?: "", object : ApiCallback<List<InventoryItem>> {
                override fun onSuccess(apiResponse: ApiResponse<List<InventoryItem>>?) {
                    val inventoryItems = apiResponse?.data?.filter {it.productName?.contains(searchQuery ?: "", ignoreCase = true) == true }?.sortedByDescending { it.createdAt } ?: emptyList()
                    binding.recyclerView.adapter = InventoryAdapter(inventoryItems)
                    super.onSuccess(apiResponse)
                    callback(true)
                }
                override fun onError(response: Response<ApiResponse<List<InventoryItem>>>) {
                    Toast.makeText(activity, "검색에 실패했습니다 (HTTP ${response.code()})", Toast.LENGTH_SHORT).show()
                    super.onError(response)
                }
                override fun onFailure(throwable: Throwable) {
                    Toast.makeText(activity, "네트워크 오류: ${throwable.message}", Toast.LENGTH_SHORT).show()
                    super.onFailure(throwable)
                }
            })
        }
    }

    private fun setupFilters() {
        val currentItems = (binding.recyclerView.adapter as InventoryAdapter).getCurrentItems()

        binding.categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedCategory = binding.categorySpinner.selectedItem.toString()
                val filteredItems =
                    if (selectedCategory == "카테고리") currentItems
                    else currentItems.filter { it.category == selectedCategory }
                binding.recyclerView.adapter = InventoryAdapter(filteredItems)
            }
            override fun onNothingSelected(parent: AdapterView<*>) { }
        }

        binding.sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedSort = binding.sortSpinner.selectedItem.toString()
                val filteredItems = when (selectedSort) {
                    "최신 등록 순" -> currentItems.sortedByDescending { it.createdAt }
                    "만료 임박 순" -> currentItems.sortedBy { it.expirationDate }
                    "만료 여유 순" -> currentItems.sortedByDescending { it.expirationDate }
                    "재고 많은 순" -> currentItems.sortedByDescending { it.quantity }
                    "재고 적은 순" -> currentItems.sortedBy { it.quantity }
                    else -> currentItems
                }
                binding.recyclerView.adapter = InventoryAdapter(filteredItems)
            }
            override fun onNothingSelected(parent: AdapterView<*>) { }
        }

        binding.stockLocationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedStockLocation = binding.stockLocationSpinner.selectedItem.toString()
                val filteredItems =
                    if (selectedStockLocation in listOf("냉장실", "냉동실", "실외 저장소"))
                        currentItems.filter { it.stockLocation == selectedStockLocation }
                    else currentItems
                binding.recyclerView.adapter = InventoryAdapter(filteredItems)
            }
            override fun onNothingSelected(parent: AdapterView<*>) { }
        }
    }

    private fun setupSwipeController() {
        val swipeController = SwipeController()
        swipeController.setButtonActionListener(object : SwipeControllerActions {
            override fun onRightClicked(position: Int) {
                val currentItems = (binding.recyclerView.adapter as InventoryAdapter).getCurrentItems()
                val itemToDelete = currentItems[position]
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("삭제 확인")
                builder.setMessage("정말 ${itemToDelete.productName}을(를) 삭제하시겠습니까?")
                builder.setPositiveButton("확인") { _, _ ->
                    deleteInventoryItem(itemToDelete)
                }
                builder.setNegativeButton("취소", null)
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
        ApiManager().deleteInventoryItem(item.id, object : ApiCallback<Unit> {
            override fun onSuccess(apiResponse: ApiResponse<Unit>?) {
                val currentItems = (binding.recyclerView.adapter as InventoryAdapter).getCurrentItems()
                val filteredItems = currentItems.filter { it.id != item.id }
                binding.recyclerView.adapter = InventoryAdapter(filteredItems)
                Toast.makeText(activity, "삭제가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                super.onSuccess(apiResponse)
            }
            override fun onError(response: Response<ApiResponse<Unit>>) {
                Toast.makeText(activity, "삭제에 실패했습니다 (HTTP ${response.code()})", Toast.LENGTH_SHORT).show()
                super.onError(response)
            }
            override fun onFailure(throwable: Throwable) {
                Toast.makeText(activity, "네트워크 오류: ${throwable.message}", Toast.LENGTH_SHORT).show()
                super.onFailure(throwable)
            }
        })
    }

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        fun newInstance(param1: String, param2: String): InventoryListFragment {
            return InventoryListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
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
