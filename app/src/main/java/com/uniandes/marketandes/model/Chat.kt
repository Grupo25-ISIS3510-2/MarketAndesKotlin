package com.uniandes.marketandes.model

data class Chat(
    val chatId: String,
    val otherUserName: String,
    val lastMessage: String,
    val otherUserImage: String,
    val roleLabel: String
)