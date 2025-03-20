package com.uniandes.marketandes.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.uniandes.marketandes.ui.model.Chat

@Composable
fun PagChat(navController: NavHostController) {
    val currentUserUID = FirebaseAuth.getInstance().currentUser?.uid
    val db = FirebaseFirestore.getInstance()
    val chats = remember { mutableStateOf<List<Chat>>(emptyList()) }

    LaunchedEffect(currentUserUID) {
        currentUserUID?.let {
            db.collection("chats")
                .whereArrayContains("userIDs", it)
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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Mis chats",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn {
            items(chats.value) { chat ->
                ChatItem(
                    chat = chat,
                    onClick = {
                        navController.navigate("chatDetail/${chat.chatId}")
                    },
                    // Cambia la ruta de navegación a la nueva pantalla de ubicación
                    onLocationClick = {
                        navController.navigate("PagChatMap/${chat.chatId}")
                    }
                )
            }
        }
    }
}

@Composable
fun ChatItem(chat: Chat, onClick: () -> Unit, onLocationClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
    ) {
        if (chat.userImage.isNotEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(chat.userImage),
                contentDescription = "Imagen de usuario",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00296B))
            ) {
                Text(
                    text = chat.userName.take(1),
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = chat.userName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Ubicación",
            modifier = Modifier
                .clickable { onLocationClick() }
                .size(24.dp),
            tint = Color(0xFF00296B)
        )
    }
}

fun createChatBetweenUsers(user1UID: String, user2UID: String) {
    val chatID = if (user1UID < user2UID) "$user1UID$user2UID" else "$user2UID$user1UID"

    val db = FirebaseFirestore.getInstance()

    val chatData = hashMapOf(
        "userName" to "Pepe",
        "lastMessage" to "¡Hola! ¿Cómo estás?",
        "userImage" to "URL_imagen_usuario",
        "userIDs" to listOf(user1UID, user2UID)
    )

    db.collection("chats")
        .document(chatID)
        .set(chatData)
        .addOnSuccessListener {
            Log.d("Firestore", "Chat creado exitosamente")
            val firstMessage = hashMapOf(
                "text" to "¡Hola! ¿Cómo estás?",
                "senderId" to user1UID,
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("chats")
                .document(chatID)
                .collection("messages")
                .add(firstMessage)
                .addOnSuccessListener {
                    Log.d("Firestore", "Primer mensaje enviado exitosamente")
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error al enviar el primer mensaje", e)
                }
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error al crear el chat", e)
        }
}