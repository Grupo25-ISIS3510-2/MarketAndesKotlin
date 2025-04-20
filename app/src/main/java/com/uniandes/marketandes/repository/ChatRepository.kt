package com.uniandes.marketandes.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.uniandes.marketandes.model.Message
import com.uniandes.marketandes.local.MessageDao
import com.uniandes.marketandes.model.MessageEntity
import kotlinx.coroutines.tasks.await

class ChatRepository(private val db: FirebaseFirestore, private val messageDao: MessageDao) {

    // Obtener mensajes de la base de datos local primero y luego de Firestore
    suspend fun getMessages(chatId: String): List<Message> {
        return try {
            // Primero obtenemos los mensajes desde la base de datos local (Room)
            val localMessages = messageDao.getMessagesByChatId(chatId).map {
                Message(text = it.text, senderId = it.senderId, timestamp = it.timestamp)
            }

            // Luego obtenemos los mensajes desde Firestore
            val snapshot = db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp")
                .get()
                .await()

            // Convertir los mensajes de Firestore a Message y agregar a la base de datos local
            snapshot.documents.forEach {
                val messageEntity = MessageEntity(
                    chatId = chatId,
                    text = it.getString("text") ?: "",
                    senderId = it.getString("senderId") ?: "",
                    timestamp = it.getLong("timestamp") ?: 0L
                )
                insertMessage(messageEntity) // Insertar mensaje en Room
            }

            // Retornar los mensajes de Firestore
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

    // Escuchar mensajes de Firestore en tiempo real
    fun listenForMessages(chatId: String, onMessagesReceived: (List<Message>) -> Unit) {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("Firestore", "Error al escuchar los mensajes", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val messages = snapshot.documents.map {
                        Message(
                            text = it.getString("text") ?: "",
                            senderId = it.getString("senderId") ?: "",
                            timestamp = it.getLong("timestamp") ?: 0L
                        )
                    }
                    onMessagesReceived(messages) // Devolver los mensajes actualizados
                }
            }
    }

    // Enviar mensaje a Firestore y a la base de datos local
    suspend fun sendMessage(chatId: String, newMessage: Message): Boolean {
        return try {
            val messageMap = hashMapOf(
                "text" to newMessage.text,
                "senderId" to newMessage.senderId,
                "timestamp" to newMessage.timestamp
            )

            // Enviar mensaje a Firestore
            db.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(messageMap)
                .await()

            // Guardar el mensaje en la base de datos local (Room)
            val messageEntity = MessageEntity(
                chatId = chatId,
                text = newMessage.text,
                senderId = newMessage.senderId,
                timestamp = newMessage.timestamp
            )
            insertMessage(messageEntity)

            // Actualizar el último mensaje en Firestore
            updateLastMessage(chatId, newMessage.text)
            true
        } catch (e: Exception) {
            Log.w("Firestore", "Error al enviar el mensaje", e)
            false
        }
    }

    private suspend fun insertMessage(messageEntity: MessageEntity) {
        // Insertar mensaje en la base de datos local
        messageDao.insert(messageEntity)
    }

    private suspend fun updateLastMessage(chatId: String, lastMessageText: String) {
        val lastMessageUpdate = mapOf("lastMessage" to lastMessageText)

        // Actualizar el campo "lastMessage" en Firestore
        db.collection("chats")
            .document(chatId)
            .update(lastMessageUpdate)
            .await()
        Log.d("Firestore", "Último mensaje actualizado con éxito")
    }
}