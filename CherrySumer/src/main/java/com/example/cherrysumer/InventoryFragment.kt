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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cherrysumer.databinding.FragmentInventoryBinding
import com.example.cherrysumer.retrofit.ApiResponse
import com.example.cherrysumer.retrofit.ErrorResponse
import com.example.cherrysumer.retrofit.INetworkService
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InventoryFragment : Fragment() {

    private var param: String? = null

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
        val binding = FragmentInventoryBinding.inflate(inflater, container, false)
        val networkService: INetworkService = MyApplication.networkService
        val call: Call<ApiResponse<List<InventoryItemModel>>> = networkService.getInventoryItems()

        binding.helpButton.setOnClickListener {
            val inflater = LayoutInflater.from(requireContext())
            val tooltipView = inflater.inflate(R.layout.tooltip_layout, null)

            // PopupWindow 생성 (가로 사이즈를 MATCH_PARENT로 설정)
            val popupWindow = PopupWindow(
                tooltipView,
                ViewGroup.LayoutParams.MATCH_PARENT, // 전체 화면 너비
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            )

            // 툴팁을 화면에 표시
            popupWindow.showAsDropDown(it, 0, 0)

            // 몇 초 뒤에 팝업을 닫도록 설정
            tooltipView.postDelayed({
                popupWindow.dismiss()
            }, 5000)
        }

        call.enqueue(object : Callback<ApiResponse<List<InventoryItemModel>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<InventoryItemModel>>>,
                response: Response<ApiResponse<List<InventoryItemModel>>>
            ) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.isSuccess) {
                        val inventoryItems = apiResponse.data?.filter { it.stockLocation == param } ?: listOf()
                        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
                        binding.recyclerView.adapter = InventoryAdapter(inventoryItems)

                        Toast.makeText(activity, "데이터 로딩 성공", Toast.LENGTH_SHORT).show()
                        Log.d("InventoryFragment", "Loaded items count: ${inventoryItems.size}")

                        // 카테고리 필터링
                        binding.categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                                val selectedCategory = binding.categorySpinner.selectedItem.toString()
                                val filteredItems = if (selectedCategory == "카테고리") {
                                    inventoryItems
                                } else {
                                    inventoryItems.filter { it.detailedCategory.contains(selectedCategory) }
                                }
                                binding.recyclerView.adapter = InventoryAdapter(filteredItems)
                            }

                            override fun onNothingSelected(parent: AdapterView<*>) {}
                        }

                        // 정렬 기능
                        binding.sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                                val selectedSort = binding.sortSpinner.selectedItem.toString()
                                val sortedItems = when (selectedSort) {
                                    "만료 임박 순" -> inventoryItems.sortedBy { it.expirationDate }
                                    "만료 여유 순" -> inventoryItems.sortedByDescending { it.expirationDate }
                                    "재고 많은 순" -> inventoryItems.sortedByDescending { it.quantity }
                                    "재고 적은 순" -> inventoryItems.sortedBy { it.quantity }
                                    else -> inventoryItems
                                }
                                binding.recyclerView.adapter = InventoryAdapter(sortedItems)
                            }

                            override fun onNothingSelected(parent: AdapterView<*>) {}
                        }

                        // 스와이프 컨트롤러 설정
                        val swipeController = SwipeController()
                        swipeController.setButtonActionListener(object : SwipeControllerActions {
                            override fun onLeftClicked(position: Int) {
                                super.onLeftClicked(position)
                                // 왼쪽 클릭 동작
                            }

                            override fun onRightClicked(position: Int) {
                                // 오른쪽 클릭 동작 (예: 아이템 삭제)
                                // InventoryAdapter.delData(position)
                            }
                        })
                        val itemTouchHelper = ItemTouchHelper(swipeController)
                        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
                        binding.recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
                            override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                                swipeController.onDraw(c)
                            }
                        })
                    } else {
                        // isSuccess가 false일 때 처리
                        val errorMessage = apiResponse?.message ?: "Unknown error"
                        Log.e("InventoryFragment", "Error: ${apiResponse?.code} - $errorMessage")
                        Toast.makeText(activity, "Error: ${apiResponse?.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // 서버가 비정상적인 응답을 반환한 경우 (예: 401, 502 등)
                    val errorBody = response.errorBody()?.string()
                    if (errorBody != null) {
                        try {
                            val gson = Gson()
                            val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                            Log.e("InventoryFragment", "Error: ${errorResponse.code} - ${errorResponse.message}")
                            Toast.makeText(activity, "Error: ${errorResponse.message}", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(activity, "Unknown error", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(activity, "Unknown error", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<InventoryItemModel>>>, t: Throwable) {
                // 네트워크 오류 또는 파싱 오류 처리
                Log.e("InventoryFragment", "Network Failure: ${t.message}", t)
                Toast.makeText(activity, "Network Failure: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

        return binding.root
    }

    companion object {
        private const val ARG_PARAM = "param"

        fun newInstance(param: String): InventoryFragment {
            return InventoryFragment().apply {
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
