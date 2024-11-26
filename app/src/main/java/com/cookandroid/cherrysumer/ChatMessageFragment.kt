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
                    val gson = Gson()
                    val response = gson.fromJson(topicMessage.payload, ChatResponse::class.java)

                    // partnerId를 툴바 제목으로 설정
                    binding.toolbarTitle.text = response.partnerId.toString()

                    // myId 설정
                    myId = response.myId

                    // 어댑터 초기화
                    if (!::adapter.isInitialized) {
                        adapter = ChatMessageAdapter(messageList, myId!!)
                        binding.recyclerViewChat.layoutManager = LinearLayoutManager(requireContext())
                        binding.recyclerViewChat.adapter = adapter
                        Log.d("ChatMessageFragment", "Adapter initialized successfully")
                    } else {
                        Log.d("ChatMessageFragment", "Adapter already initialized")
                    }

                    // PostInfo 업데이트
                    response.post?.let {
                        Log.d("ChatMessageFragment", "Updating PostInfo: ${it.title}")
                        updatePostInfo(it)
                    }

                    // 채팅 리스트 업데이트
                    updateChatList(response.chatList)
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

            val calendar = Calendar.getInstance()
            val currentTime = String.format(
                "%02d:%02d",
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)
            )

            messageList.add(ChatMessage(0, myId!!, "", currentTime, content))
            adapter.notifyItemInserted(messageList.size - 1)
            binding.recyclerViewChat.scrollToPosition(messageList.size - 1)

            stompClient.send("/pub/message", data.toString()).subscribe()
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
        messageList.addAll(chatList)
        adapter.notifyDataSetChanged()
        binding.recyclerViewChat.scrollToPosition(messageList.size - 1)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stompClient.disconnect()
        _binding = null
    }
}
