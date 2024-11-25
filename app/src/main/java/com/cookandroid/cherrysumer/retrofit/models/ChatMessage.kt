package com.cookandroid.cherrysumer.retrofit.models

data class ChatResponse(
    val myId: Long,
    val partnerId: Long,
    val roomId: String,
    val post: PostInfo,
    val chatList: List<ChatMessage>
)

data class PostInfo(
    val postId: Long,
    val title: String,
    val productname: String,
    val price: Int,
    val place: String,
    val date: String,
    val imageUrl: String?
)

data class ChatMessage(
    val id: Long,
    val senderId: Long,
    val date: String,
    val time: String,
    val message: String
)
