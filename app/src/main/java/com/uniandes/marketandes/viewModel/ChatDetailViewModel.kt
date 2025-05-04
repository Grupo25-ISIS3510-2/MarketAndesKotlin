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

    // Function to fetch messages asynchronously using coroutines
    fun fetchMessages(chatId: String) {
        viewModelScope.launch {
            // Fetch messages from local storage first
            val loadedMessages = repository.getMessages(chatId)
            messages.value = loadedMessages

            // Then, listen for new messages from Firestore asynchronously
            repository.listenForMessages(chatId) { remoteMessages ->
                messages.value = remoteMessages
            }
        }
    }

    // Function to send a message asynchronously
    fun sendMessage(chatId: String, userUID: String) {
        if (message.value.isNotEmpty()) {
            val newMessage = Message(
                text = message.value,
                senderId = userUID,
                timestamp = System.currentTimeMillis()
            )

            viewModelScope.launch {
                // Send message in background
                val success = repository.sendMessage(chatId, newMessage)
                if (success) {
                    message.value = ""
                } else {
                    Log.w("ChatDetailViewModel", "Error al enviar el mensaje")
                }
            }
        }
    }
}