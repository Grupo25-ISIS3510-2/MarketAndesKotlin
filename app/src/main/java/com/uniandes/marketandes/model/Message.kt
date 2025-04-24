package com.uniandes.marketandes.model

data class Message(
    val text: String,
    val senderId: String,
    val timestamp: Long,
)