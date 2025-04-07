package com.uniandes.marketandes.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.uniandes.marketandes.model.Message
import kotlinx.coroutines.launch

class ChatDetailViewModel : ViewModel() {
    val messages = mutableStateOf<List<Message>>(emptyList())
    val message = mutableStateOf("")

    private val db = FirebaseFirestore.getInstance()

    fun sendMessage(chatId: String, currentUserUID: String) {
        if (message.value.isNotEmpty()) {
            val newMessage = hashMapOf(
                "text" to message.value,
                "senderId" to currentUserUID,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(newMessage)
                .addOnSuccessListener {
                    Log.d("Firestore", "Mensaje enviado con éxito")
                    updateLastMessage(chatId)
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error al enviar el mensaje", e)
                }
            message.value = ""
        }
    }

    private fun updateLastMessage(chatId: String) {
        val lastMessageUpdate = mutableMapOf<String, Any>(
            "lastMessage" to message.value
        )

        db.collection("chats")
            .document(chatId)
            .update(lastMessageUpdate)
            .addOnSuccessListener {
                Log.d("Firestore", "Último mensaje actualizado con éxito")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error al actualizar el último mensaje", e)
            }
    }

    fun fetchMessages(chatId: String) {
        viewModelScope.launch {
            db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener { snapshot ->
                    messages.value = snapshot.documents.map {
                        Message(
                            text = it.getString("text") ?: "",
                            senderId = it.getString("senderId") ?: "",
                            timestamp = it.getLong("timestamp") ?: 0L
                        )
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error al cargar los mensajes", e)
                }
        }
    }
}