package com.uniandes.marketandes.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.uniandes.marketandes.model.Message
import com.uniandes.marketandes.local.MessageDao
import com.uniandes.marketandes.model.Chat
import com.uniandes.marketandes.model.MessageEntity
import kotlinx.coroutines.tasks.await

class ChatRepository(private val db: FirebaseFirestore, private val messageDao: MessageDao) {

    suspend fun getMessages(chatId: String): List<Message> {
        // Fetch messages from Room and Firestore asynchronously
        val localMessages = messageDao.getMessagesByChatId(chatId).map {
            Message(text = it.text, senderId = it.senderId, timestamp = it.timestamp)
        }

        val snapshot = db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .get()
            .await()

        snapshot.documents.forEach {
            val messageEntity = MessageEntity(
                chatId = chatId,
                text = it.getString("text") ?: "",
                senderId = it.getString("senderId") ?: "",
                timestamp = it.getLong("timestamp") ?: 0L
            )
            insertMessage(messageEntity) // Save to Room
        }

        return snapshot.documents.map {
            Message(
                text = it.getString("text") ?: "",
                senderId = it.getString("senderId") ?: "",
                timestamp = it.getLong("timestamp") ?: 0L
            )
        }
    }

    fun listenForMessages(
        chatId: String,
        onMessagesReceived: (List<Message>) -> Unit
    ) {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    Log.w("ChatRepo", "listen error", e)
                    return@addSnapshotListener
                }
                val msgs = snap?.documents.orEmpty().map {
                    Message(
                        text = it.getString("text") ?: "",
                        senderId = it.getString("senderId") ?: "",
                        timestamp = it.getLong("timestamp") ?: 0L
                    )
                }
                onMessagesReceived(msgs)
            }
    }

    suspend fun sendMessage(chatId: String, newMessage: Message): Boolean {
        return try {
            val map = hashMapOf(
                "text" to newMessage.text,
                "senderId" to newMessage.senderId,
                "timestamp" to newMessage.timestamp
            )
            db.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(map)
                .await()

            messageDao.insert(
                MessageEntity(
                    chatId = chatId,
                    text = newMessage.text,
                    senderId = newMessage.senderId,
                    timestamp = newMessage.timestamp
                )
            )
            // also update lastMessage field...
            db.collection("chats")
                .document(chatId)
                .update("lastMessage", newMessage.text)
                .await()

            true
        } catch (ex: Exception) {
            Log.w("ChatRepo", "send failed", ex)
            false
        }
    }

    private suspend fun insertMessage(messageEntity: MessageEntity) {
        messageDao.insert(messageEntity)
    }

    private suspend fun updateLastMessage(chatId: String, lastMessageText: String) {
        val lastMessageUpdate = mapOf("lastMessage" to lastMessageText)
        db.collection("chats")
            .document(chatId)
            .update(lastMessageUpdate)
            .await()
    }

    suspend fun getChatInfo(chatId: String, currentUserId: String): Chat? {
        return try {
            val doc = db.collection("chats").document(chatId).get().await()

            val userIDs = doc.get("userIDs") as? List<String> ?: return null
            val lastMessage = doc.getString("lastMessage") ?: ""
            val productName = doc.getString("productName") ?: "Producto"
            val otherUserId = userIDs.firstOrNull { it != currentUserId } ?: return null

            // Consultar info del otro usuario
            val userDoc = db.collection("users").document(otherUserId).get().await()
            val otherUserName = userDoc.getString("name") ?: "Desconocido"
            val otherUserImage = userDoc.getString("profileImage") ?: ""

            // Determinar si el usuario actual es comprador o vendedor
            val roleLabel = if (currentUserId == userIDs[0]) {
                "Comprador $productName"
            } else {
                "Vendedor $productName"
            }

            Chat(
                chatId = chatId,
                otherUserName = otherUserName,
                lastMessage = lastMessage,
                otherUserImage = otherUserImage,
                roleLabel = roleLabel
            )

        } catch (e: Exception) {
            Log.w("Firestore", "Error al obtener info del chat", e)
            null
        }
    }
}