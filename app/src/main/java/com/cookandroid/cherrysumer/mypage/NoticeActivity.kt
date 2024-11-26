package com.cookandroid.cherrysumer.mypage


import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cookandroid.cherrysumer.R

class NoticeActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NoticeAdapter
    private lateinit var previousButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 액티비티의 전체 배경색을 하얀색으로 설정
        window.decorView.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        setContentView(R.layout.activity_mypage_notice)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        previousButton = findViewById(R.id.previous_button)

        previousButton.setOnClickListener {
            // 현재 Activity를 종료하여 이전 화면(마이페이지)으로 돌아감
            finish()
        }


        // 더미 데이터
        val notices = listOf(
            Notice("24/10/04", "게시글 참여자 단체채팅 방법 안내", "저희 앱에서는 따로 단체 채팅 기능을 지원하고 있지 않습니다."),
            Notice("24/10/05", "공지사항 2", "두 번째 공지 내용입니다."),
            Notice("24/10/06", "공지사항 3", "세 번째 공지 내용입니다.")
        )

        adapter = NoticeAdapter(notices)
        recyclerView.adapter = adapter
    }
}