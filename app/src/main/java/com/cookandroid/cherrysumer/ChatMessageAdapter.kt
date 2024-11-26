import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cookandroid.cherrysumer.R
import com.cookandroid.cherrysumer.retrofit.models.ChatMessage
import java.text.SimpleDateFormat
import java.util.*

class ChatMessageAdapter(private val messages: List<ChatMessage>, private val myId: Long) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_OUTGOING = 1
        private const val VIEW_TYPE_INCOMING = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == myId) VIEW_TYPE_OUTGOING else VIEW_TYPE_INCOMING
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_OUTGOING) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_outgoing, parent, false)
            OutgoingMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_incoming, parent, false)
            IncomingMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.d("ChatMessageAdapter", "onBindViewHolder called for position: $position.")

        val message = messages[position]

        // 첫 번째 메시지거나 이전 메시지와 날짜가 다르면 날짜를 표시
        val showDate = position == 0 || messages[position].date != messages[position - 1].date

        if (holder is OutgoingMessageViewHolder) {
            holder.bind(message, showDate)
        } else if (holder is IncomingMessageViewHolder) {
            holder.bind(message, showDate)
        }
    }

    override fun getItemCount(): Int = messages.size

    private fun formatDate(date: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())

            val parsedDate = inputFormat.parse(date)
            outputFormat.format(parsedDate ?: Date())
        } catch (e: Exception) {
            e.printStackTrace()
            date // 실패 시 원래 날짜 반환
        }
    }

    private fun formatTime(time: String): String {
        return try {
            val inputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("a hh:mm", Locale.getDefault())

            val parsedTime = inputFormat.parse(time)
            val formattedTime = outputFormat.format(parsedTime ?: Date())

            // "AM"과 "PM"을 한글로 변환
            formattedTime.replace("AM", "오전").replace("PM", "오후")
        } catch (e: Exception) {
            e.printStackTrace()
            time // 실패 시 원래 시간 반환
        }
    }

    class OutgoingMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val messageTimeTextView: TextView = itemView.findViewById(R.id.messageTimeTextView)
        private val messageDateTextView: TextView = itemView.findViewById(R.id.messageDateTextView)

        fun bind(message: ChatMessage, showDate: Boolean) {
            messageTextView.text = message.message
            messageTimeTextView.text = ChatMessageAdapter(emptyList(), 0).formatTime(message.time)
            if (showDate) {
                messageDateTextView.text = ChatMessageAdapter(emptyList(), 0).formatDate(message.date)
                messageDateTextView.visibility = View.VISIBLE
            } else {
                messageDateTextView.visibility = View.GONE
            }
        }
    }

    class IncomingMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val messageTimeTextView: TextView = itemView.findViewById(R.id.messageTimeTextView)
        private val messageDateTextView: TextView = itemView.findViewById(R.id.messageDateTextView)

        fun bind(message: ChatMessage, showDate: Boolean) {
            messageTextView.text = message.message
            messageTimeTextView.text = ChatMessageAdapter(emptyList(), 0).formatTime(message.time)
            if (showDate) {
                messageDateTextView.text = ChatMessageAdapter(emptyList(), 0).formatDate(message.date)
                messageDateTextView.visibility = View.VISIBLE
            } else {
                messageDateTextView.visibility = View.GONE
            }
        }
    }
}
