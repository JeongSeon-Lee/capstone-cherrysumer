package com.cookandroid.cherrysumer

import ChatMessageAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.cookandroid.cherrysumer.databinding.FragmentChatMessageBinding
import com.cookandroid.cherrysumer.retrofit.MyApplication
import com.cookandroid.cherrysumer.retrofit.models.ChatMessage
import com.cookandroid.cherrysumer.retrofit.models.ChatResponse
import com.cookandroid.cherrysumer.retrofit.models.PostInfo
import com.google.android.material.internal.ViewUtils.hideKeyboard
import com.google.gson.Gson
import io.reactivex.disposables.Disposable
import org.json.JSONObject
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader
import java.util.*

class ChatMessageFragment : Fragment() {

    private val url = "ws://3.39.110.119/chat/inbox"
    private lateinit var stompClient: ua.naiksoftware.stomp.StompClient
    private var subscription: Disposable? = null
    private var roomId: String? = null
    private var userNickname: String? = null
    private var token: String? = null
    private var myId: Long? = null

    private val messageList = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatMessageAdapter

    private var _binding: FragmentChatMessageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatMessageBinding.inflate(inflater, container, false)

        roomId = arguments?.getString("ROOM_ID")
        userNickname = arguments?.getString("USER_NICKNAME")
        token = MyApplication.getSavedToken()

        setupToolbar()
        setupUI()
        setupStompClient()

        return binding.root
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        val actionBar = (activity as? AppCompatActivity)?.supportActionBar

        actionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back) // 업 버튼 이미지 설정
        }

        binding.toolbar.setNavigationOnClickListener {
            if (parentFragmentManager.backStackEntryCount > 0) {
                parentFragmentManager.popBackStack()
            } else {
                activity?.onBackPressedDispatcher?.onBackPressed()
            }
        }
    }

    private fun setupUI() {
        binding.toolbarTitle.text = userNickname

        val bottomNav = activity?.findViewById<View>(R.id.bottom_navigation)
        bottomNav?.visibility = View.GONE

        binding.buttonSend.setOnClickListener {
            val message = binding.editTextMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
                binding.editTextMessage.text.clear()
            } else {
                Toast.makeText(requireContext(), "메시지를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun setupStompClient() {
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, url)
        val headers = listOf(StompHeader("Authorization", "Bearer $token"))
        stompClient.connect(headers)

        stompClient.lifecycle().subscribe { event ->
            when (event.type) {
                LifecycleEvent.Type.OPENED -> {
                    Log.i("STOMP", "Connected")
                    subscribeToMessages()
                }
                LifecycleEvent.Type.CLOSED -> {
                    Log.i("STOMP", "Disconnected")
                }
                LifecycleEvent.Type.ERROR -> {
                    Log.e("STOMP", "Error", event.exception)
                }
                LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> {
                    Log.e("STOMP", "Failed server heartbeat")
                }
                else -> {
                    Log.i("STOMP", "Unhandled lifecycle event: ${event.type}")
                }
            }
        }
    }

    private fun subscribeToMessages() {
        subscription = stompClient.topic("/sub/channel/$roomId").subscribe { topicMessage ->
            requireActivity().runOnUiThread {
                try {
                    Log.d("ChatMessageFragment", "Raw payload: ${topicMessage.payload}")

                    // JSON 파싱
                    val jsonObject = JSONObject(topicMessage.payload)

                    // myId 설정
                    if (jsonObject.has("myId")) {
                        myId = jsonObject.getLong("myId")
                    }

                    // 어댑터 초기화
                    if (!::adapter.isInitialized) {
                        adapter = ChatMessageAdapter(messageList, myId ?: -1)
                        binding.recyclerViewChat.layoutManager = LinearLayoutManager(requireContext())
                        binding.recyclerViewChat.adapter = adapter
                        Log.d("ChatMessageFragment", "Adapter initialized successfully")
                    } else {
                        Log.d("ChatMessageFragment", "Adapter already initialized")
                    }

                    // PostInfo 업데이트
                    if (jsonObject.has("post")) {
                        val postObject = jsonObject.getJSONObject("post")
                        val postInfo = PostInfo(
                            postId = postObject.getLong("postId"),
                            title = postObject.getString("title"),
                            productname = postObject.getString("productname"),
                            price = postObject.getInt("price"),
                            place = postObject.getString("place"),
                            date = postObject.getString("date"),
                            imageUrl = postObject.optString("imageUrl", null)
                        )
                        updatePostInfo(postInfo)
                    }

                    // chatList 또는 단일 메시지 파싱
                    if (jsonObject.has("chatList")) {
                        val chatListJsonArray = jsonObject.getJSONArray("chatList")
                        val chatList = mutableListOf<ChatMessage>()
                        for (i in 0 until chatListJsonArray.length()) {
                            val chatMessageObject = chatListJsonArray.getJSONObject(i)
                            val chatMessage = ChatMessage(
                                id = if (chatMessageObject.isNull("id")) null else chatMessageObject.getLong("id"),
                                senderId = chatMessageObject.getLong("senderId"),
                                date = chatMessageObject.getString("date"),
                                time = chatMessageObject.getString("time"),
                                message = chatMessageObject.getString("message")
                            )
                            chatList.add(chatMessage)
                        }
                        Log.d("ChatMessageFragment", "Parsed chatList: $chatList")
                        updateChatList(chatList)
                    } else {
                        // 단일 메시지 처리
                        val chatMessage = ChatMessage(
                            id = if (jsonObject.isNull("id")) null else jsonObject.getLong("id"),
                            senderId = jsonObject.getLong("senderId"),
                            date = jsonObject.getString("date"),
                            time = jsonObject.getString("time"),
                            message = jsonObject.getString("message")
                        )
                        Log.d("ChatMessageFragment", "Parsed single message: $chatMessage")
                        updateChatList(listOf(chatMessage))
                    }

                } catch (e: Exception) {
                    Log.e("ChatMessageFragment", "Error processing message: ${e.message}", e)
                }
            }
        }
    }


    private fun sendMessage(content: String) {
        if (myId == null) {
            Toast.makeText(requireContext(), "사용자 ID가 초기화되지 않았습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val data = JSONObject()
        try {
            data.put("roomId", roomId)
            data.put("message", content)

            stompClient.send("/pub/message", data.toString()).subscribe({
                Log.d("ChatMessageFragment", "Message sent successfully: $content")
            }, { error ->
                Log.e("ChatMessageFragment", "Error sending message: ${error.message}")
            })
        } catch (e: Exception) {
            Log.e("ChatMessageFragment", "Error creating JSON: ${e.message}")
        }
    }


    private fun updatePostInfo(post: PostInfo) {
        binding.textTitle.text = post.title
        binding.textPrice.text = "${post.price}원"
        binding.textDate.text = "${post.date}"
        binding.textPlace.text = "${post.place}"

        if (!post.imageUrl.isNullOrEmpty()) {
            val fullImageUrl = "http://3.39.110.119${post.imageUrl}"
            Glide.with(this)
                .load(fullImageUrl)
                .placeholder(R.drawable.ic_cherry)
                .error(R.drawable.ic_cherry)
                .into(binding.imagePost)
        } else {
            binding.imagePost.setImageResource(R.drawable.ic_cherry)
        }
    }

    private fun updateChatList(chatList: List<ChatMessage>) {
        Log.d("ChatMessageFragment", "updateChatList called with ${chatList.size} messages.")

        // 기존 메시지와 비교하여 새 메시지만 필터링
        val newMessages = chatList.filter { newMessage ->
            messageList.none { existingMessage ->
                existingMessage.senderId == newMessage.senderId &&
                        existingMessage.date == newMessage.date &&
                        existingMessage.time == newMessage.time &&
                        existingMessage.message == newMessage.message
            }
        }

        if (newMessages.isEmpty()) {
            Log.d("ChatMessageFragment", "No new messages to add.")
            return
        }

        // 새 메시지 추가
        messageList.addAll(newMessages)
        Log.d("ChatMessageFragment", "Added ${newMessages.size} new messages. Total messages: ${messageList.size}")

        // RecyclerView에 변경 알림
        adapter.notifyDataSetChanged()

        // 리스트의 마지막 항목으로 스크롤
        if (messageList.isNotEmpty()) {
            binding.recyclerViewChat.post {
                val layoutManager = binding.recyclerViewChat.layoutManager as LinearLayoutManager
                layoutManager.scrollToPositionWithOffset(messageList.size - 1, 0)
                Log.d("ChatMessageFragment", "RecyclerView scrolled to position ${messageList.size - 1}.")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stompClient.disconnect()
        _binding = null
    }
}
