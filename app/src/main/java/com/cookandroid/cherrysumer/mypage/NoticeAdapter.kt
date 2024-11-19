package com.cookandroid.cherrysumer.mypage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cookandroid.cherrysumer.R

class NoticeAdapter(private val notices: List<Notice>) : RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder>() {

    inner class NoticeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.notice_date)
        val titleTextView: TextView = itemView.findViewById(R.id.notice_title)
        val descriptionTextView: TextView = itemView.findViewById(R.id.notice_description)
        val moreButton: ImageButton = itemView.findViewById(R.id.notice_more)
        val itemLayout: LinearLayout = itemView.findViewById(R.id.notice_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notice, parent, false)
        return NoticeViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoticeViewHolder, position: Int) {
        val notice = notices[position]

        holder.dateTextView.text = notice.date
        holder.titleTextView.text = notice.title
        holder.descriptionTextView.text = notice.description
        holder.descriptionTextView.visibility = if(notice.isExpanded) View.VISIBLE else View.GONE
        holder.moreButton.setImageResource(if (notice.isExpanded) R.drawable.up_arrow else R.drawable.down_arrow)

        holder.itemLayout.setOnClickListener {
            // 클릭된 항목의 현재 상태를 확인하고 반전
            val wasExpanded = notice.isExpanded
            // 모든 notice의 isExpanded를 false로 설정하여 다른 항목들은 닫기
            notices.forEach { it.isExpanded = false }
            // 클릭된 항목의 isExpanded를 이전 상태와 반대로 설정
            notice.isExpanded = !wasExpanded
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return notices.size
    }
}

data class Notice(
    val date: String,
    val title: String,
    val description: String,
    var isExpanded: Boolean = false
)