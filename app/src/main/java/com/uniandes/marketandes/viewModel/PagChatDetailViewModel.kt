package com.uniandes.marketandes.viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniandes.marketandes.model.Message
import com.uniandes.marketandes.repository.ChatRepository
import com.uniandes.marketandes.utils.WorkManagerHelper // Importar el helper de WorkManager
import kotlinx.coroutines.launch

class ChatDetailViewModel(private val repository: ChatRepository) : ViewModel() {
    val messages = mutableStateOf<List<Message>>(emptyList())
    val message = mutableStateOf("")

    // Fetch messages from the local database (Room) and Firestore
    fun fetchMessages(chatId: String) {
        viewModelScope.launch {
            // Cargar los mensajes desde la base de datos local primero
            val loadedMessages = repository.getMessages(chatId)
            messages.value = loadedMessages

            // Después, escuchar los cambios en Firestore
            repository.listenForMessages(chatId) { remoteMessages ->
                // Actualizar los mensajes con lo que viene de Firestore
                messages.value = remoteMessages
            }
        }
    }

    // Send message to Firestore and local database
    fun sendMessage(chatId: String, userUID: String) {
        if (message.value.isNotEmpty()) {
            val newMessage = Message(
                text = message.value,
                senderId = userUID,
                timestamp = System.currentTimeMillis()
            )

            viewModelScope.launch {
                // Enviar el mensaje a Firestore y a la base de datos local
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