package com.uniandes.marketandes.viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniandes.marketandes.model.Message
import com.uniandes.marketandes.repository.ChatRepository
import kotlinx.coroutines.launch

class ChatDetailViewModel(private val repository: ChatRepository) : ViewModel() {
    val messages = mutableStateOf<List<Message>>(emptyList())
    val message = mutableStateOf("")

    fun fetchMessages(chatId: String) {
        viewModelScope.launch {
            val loadedMessages = repository.getMessages(chatId)
            messages.value = loadedMessages
        }
    }

    fun sendMessage(chatId: String, userUID: String) {
        if (message.value.isNotEmpty()) {
            val newMessage = Message(
                text = message.value,
                senderId = userUID,
                timestamp = System.currentTimeMillis()
            )

            viewModelScope.launch {
                val success = repository.sendMessage(chatId, newMessage)
                if (success) {
                    messages.value = messages.value + newMessage

                    message.value = ""
                }
            }
        }
    }
}