package com.uniandes.marketandes.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.uniandes.marketandes.model.Message
import kotlinx.coroutines.tasks.await

class ChatRepository(private val db: FirebaseFirestore) {

    suspend fun getMessages(chatId: String): List<Message> {
        return try {
            val snapshot = db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp")
                .get()
                .await()
            snapshot.documents.map {
                Message(
                    text = it.getString("text") ?: "",
                    senderId = it.getString("senderId") ?: "",
                    timestamp = it.getLong("timestamp") ?: 0L
                )
            }
        } catch (e: Exception) {
            Log.w("Firestore", "Error al cargar los mensajes", e)
            emptyList()
        }
    }

    suspend fun sendMessage(chatId: String, newMessage: Message): Boolean {
        return try {
            val messageMap = hashMapOf(
                "text" to newMessage.text,
                "senderId" to newMessage.senderId,
                "timestamp" to newMessage.timestamp
            )

            db.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(messageMap)
                .await()

            updateLastMessage(chatId, newMessage.text)
            true
        } catch (e: Exception) {
            Log.w("Firestore", "Error al enviar el mensaje", e)
            false
        }
    }

    private suspend fun updateLastMessage(chatId: String, lastMessageText: String) {
        try {
            val lastMessageUpdate = mapOf("lastMessage" to lastMessageText)

            db.collection("chats")
                .document(chatId)
                .update(lastMessageUpdate)
                .await()
            Log.d("Firestore", "Último mensaje actualizado con éxito")
        } catch (e: Exception) {
            Log.w("Firestore", "Error al actualizar el último mensaje", e)
        }
    }
}
