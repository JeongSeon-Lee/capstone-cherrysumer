package com.cookandroid.cherrysumer.models

data class ChatRoom(
    val chatRoomId: String,
    val userId: Int,
    val userNickname: String,
    val postId: Int,
    val userProfileImageUrl: String?,
    val lastMessage: String,
    val updatedAt: String,
    val status: String
)
