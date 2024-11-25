package com.cookandroid.cherrysumer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cookandroid.cherrysumer.models.ChatRoom

class ChatRoomListAdapter(
    private var chatRooms: List<ChatRoom>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<ChatRoomListAdapter.ChatRoomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_room, parent, false)
        return ChatRoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        val chatRoom = chatRooms[position]
        holder.bind(chatRoom, onClick)
    }

    override fun getItemCount(): Int = chatRooms.size

    fun updateChatRooms(newChatRooms: List<ChatRoom>) {
        chatRooms = newChatRooms
        notifyDataSetChanged()
    }

    class ChatRoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImageView: ImageView = itemView.findViewById(R.id.imageViewProfile)
        private val roomNameTextView: TextView = itemView.findViewById(R.id.textViewRoomName)
        private val lastMessageTextView: TextView = itemView.findViewById(R.id.textViewLastMessage)
        private val timeTextView: TextView = itemView.findViewById(R.id.textViewTime)
        private val badgeTextView: TextView = itemView.findViewById(R.id.overlayBadge)

        fun bind(chatRoom: ChatRoom, onClick: (String) -> Unit) {
            // 프로필 이미지 설정 (Glide 사용)
            Glide.with(itemView.context)
                .load("http://3.39.110.119/${chatRoom.userProfileImageUrl}")
                .placeholder(R.drawable.ic_profile_placeholder)
                .circleCrop()
                .into(profileImageView)

            // 방 이름 설정
            roomNameTextView.text = chatRoom.userNickname

            // 마지막 메시지 설정
            lastMessageTextView.text = chatRoom.lastMessage

            // 시간 형식 변환
            val formattedTime = formatTime(chatRoom.updatedAt)
            timeTextView.text = formattedTime

            // 배지 설정
            badgeTextView.apply {
                text = when (chatRoom.status) {
                    "승인", "거절", "미확인" -> {
                        "참여"
                    }
                    "게시자" -> {
                        "모집"
                    }
                    else -> {
                        visibility = View.INVISIBLE
                        return@apply // 텍스트 설정 생략
                    }
                }
                visibility = View.VISIBLE // "참여" 또는 "모집"일 때는 visible로 설정
            }

            // 아이템 클릭 이벤트 설정
            itemView.setOnClickListener {
                onClick(chatRoom.chatRoomId)
            }
        }

        private fun formatTime(updatedAt: String): String {
            try {
                val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                val outputFormat = java.text.SimpleDateFormat("a hh:mm", java.util.Locale.getDefault())

                val date = inputFormat.parse(updatedAt)
                val formattedTime = outputFormat.format(date)

                // "AM"과 "PM"을 한글로 변환
                return formattedTime.replace("AM", "오전").replace("PM", "오후")
            } catch (e: Exception) {
                e.printStackTrace()
                return "" // 날짜 변환 실패 시 빈 문자열 반환
            }
        }

    }
}
