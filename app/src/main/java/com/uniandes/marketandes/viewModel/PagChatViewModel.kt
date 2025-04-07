package com.uniandes.marketandes.viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.uniandes.marketandes.model.Chat
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.launch

class PagChatViewModel : ViewModel() {
    val chats = mutableStateOf<List<Chat>>(emptyList())

    private val db = FirebaseFirestore.getInstance()

    fun fetchChats(userUID: String) {
        viewModelScope.launch {
            db.collection("chats")
                .whereArrayContains("userIDs", userUID)
                .get()
                .addOnSuccessListener { result ->
                    val chatList = result.documents.map { document ->
                        val chatID = document.id
                        val userName = document.getString("userName") ?: "Usuario"
                        val lastMessage = document.getString("lastMessage") ?: "No hay mensajes"
                        val userImage = document.getString("userImage") ?: ""
                        Chat(chatID, userName, lastMessage, userImage)
                    }
                    chats.value = chatList
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error al cargar los chats", e)
                }
        }
    }

    fun updateUserLocationInChat(chatId: String, userUID: String, lat: Double, lng: Double) {
        val locationUpdate = mapOf(
            "userLocations.$userUID" to GeoPoint(lat, lng)
        )

        db.collection("chats")
            .document(chatId)
            .update(locationUpdate)
            .addOnSuccessListener {
                Log.d("Firestore", "Ubicación de $userUID actualizada (GeoPoint) en $chatId")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error al actualizar ubicación en $chatId", e)
            }
    }
}