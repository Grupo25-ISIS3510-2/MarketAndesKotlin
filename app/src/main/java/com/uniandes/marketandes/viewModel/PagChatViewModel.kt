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
                    val chatList = result.documents.mapNotNull { document ->
                        val chatID = document.id
                        val userIDs = document.get("userIDs") as? List<String> ?: return@mapNotNull null
                        val userNames = document.get("userName") as? List<String> ?: return@mapNotNull null
                        val userImages = document.get("userImage") as? List<String> ?: return@mapNotNull null
                        val lastMessage = document.getString("lastMessage") ?: "No hay mensajes"
                        val productName = document.getString("productName") ?: "Producto"

                        // Identificar el otro usuario (que no soy yo)
                        val otherIndex = if (userUID == userIDs[0]) 1 else 0
                        val otherUserName = userNames.getOrNull(otherIndex) ?: "Usuario"
                        val otherUserImage = userImages.getOrNull(otherIndex) ?: ""
                        val roleLabel = if (userUID == userIDs[0]) "Comprador $productName" else "Vendedor $productName"

                        Chat(
                            chatId = chatID,
                            otherUserName = otherUserName,
                            lastMessage = lastMessage,
                            otherUserImage = otherUserImage,
                            roleLabel = roleLabel
                        )
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