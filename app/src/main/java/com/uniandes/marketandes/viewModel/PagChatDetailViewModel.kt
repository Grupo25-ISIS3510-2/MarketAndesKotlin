package com.uniandes.marketandes.viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.uniandes.marketandes.model.Message

class ChatDetailViewModel : ViewModel() {
    val messages = mutableStateOf<List<Message>>(emptyList())
    val message = mutableStateOf("")

    private val db = FirebaseFirestore.getInstance()

    fun fetchMessages(chatId: String) {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("Firestore", "Error al escuchar cambios en los mensajes", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    messages.value = snapshot.documents.map {
                        Message(
                            text = it.getString("text") ?: "",
                            senderId = it.getString("senderId") ?: "",
                            timestamp = it.getLong("timestamp") ?: 0L
                        )
                    }
                }
            }
    }

    fun sendMessage(chatId: String, userUID: String) {
        if (message.value.isNotEmpty()) {
            val newMessage = hashMapOf(
                "text" to message.value,
                "senderId" to userUID,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(newMessage)
                .addOnSuccessListener {
                    Log.d("Firestore", "Mensaje enviado con éxito")
                    updateLastMessage(chatId, message.value)
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error al enviar el mensaje", e)
                }
            message.value = ""
        }
    }

    private fun updateLastMessage(chatId: String, lastMessageText: String) {
        val lastMessageUpdate = mapOf<String, Any>("lastMessage" to lastMessageText)

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
}