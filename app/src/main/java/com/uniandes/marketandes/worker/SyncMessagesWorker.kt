package com.uniandes.marketandes.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.uniandes.marketandes.local.AppDatabase
import com.uniandes.marketandes.model.MessageEntity
import com.uniandes.marketandes.repository.ChatRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking

class SyncMessagesWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    private val db = FirebaseFirestore.getInstance()
    private val messageDao = AppDatabase.getDatabase(appContext).messageDao()
    private val chatRepository = ChatRepository(db, messageDao)

    override fun doWork(): Result {
        return try {
            val unsyncedMessages = getPendingMessages()
            syncMessagesWithFirestore(unsyncedMessages)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun getPendingMessages(): List<MessageEntity> {
        return runBlocking {
            messageDao.getMessagesByChatId("someChatId")
        }
    }

    private fun syncMessagesWithFirestore(messages: List<MessageEntity>) {
        messages.forEach { messageEntity ->
            val newMessage = hashMapOf(
                "text" to messageEntity.text,
                "senderId" to messageEntity.senderId,
                "timestamp" to messageEntity.timestamp
            )

            runBlocking {
                db.collection("chats")
                    .document("someChatId")
                    .collection("messages")
                    .add(newMessage)
                    .addOnSuccessListener {
                        deleteSentMessageFromRoom(messageEntity)
                    }
            }
        }
    }

    private fun deleteSentMessageFromRoom(messageEntity: MessageEntity) {
        runBlocking {
            messageDao.deleteMessagesByChatId("someChatId")
        }
    }
}
