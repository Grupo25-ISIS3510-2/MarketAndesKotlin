package com.uniandes.marketandes.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
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

    // Cargar los chats del usuario actual
    LaunchedEffect(currentUserUID) {
        currentUserUID?.let {
            db.collection("chats")
                .whereArrayContains("userIDs", it)  // Verificar que el usuario esté en el chat
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

        // Crear chat entre dos usuarios de ejemplo (Esto es solo para pruebas)
        Button(onClick = {
            // Aquí llamamos a la función para crear un chat entre dos usuarios
            val user1UID = "ibAqVn9o8rRoEr73X6QCCxIubJA2"  // UID de Juan
            val user2UID = "oZ32dSMi4fZwwyNk7nXyVU6qZsm2"  // UID de Pepe
            createChatBetweenUsers(user1UID, user2UID)  // Crea el chat entre ellos

            // Después de crear el chat, volvemos a cargar los chats
            loadChats(currentUserUID, chats)
        }) {
            Text("Crear chat entre Juan y Pepe")
        }

        // Mostrar los chats
        LazyColumn {
            items(chats.value) { chat ->
                ChatItem(chat = chat, onClick = {
                    navController.navigate("chatDetail/${chat.chatId}")
                })
            }
        }
    }
}

// Función para recargar los chats después de crear uno nuevo
fun loadChats(currentUserUID: String?, chats: MutableState<List<Chat>>) {
    currentUserUID?.let {
        FirebaseFirestore.getInstance().collection("chats")
            .whereArrayContains("userIDs", it)  // Verificar que el usuario esté en el chat
            .get()
            .addOnSuccessListener { result ->
                val chatList = result.documents.map { document ->
                    val chatID = document.id
                    val userName = document.getString("userName") ?: "Usuario"
                    val lastMessage = document.getString("lastMessage") ?: "No hay mensajes"
                    val userImage = document.getString("userImage") ?: ""
                    Chat(chatID, userName, lastMessage, userImage)
                }
                chats.value = chatList  // Actualiza la lista de chats
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error al cargar los chats", e)
            }
    }
}

@Composable
fun ChatItem(chat: Chat, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
    ) {
        // Imagen del usuario (puedes usar una imagen circular)
        Image(
            painter = rememberAsyncImagePainter(chat.userImage),
            contentDescription = "Imagen de usuario",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape) // Imagen circular
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = chat.userName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = chat.lastMessage,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

// Esta es la función que creará un chat entre dos usuarios.
fun createChatBetweenUsers(user1UID: String, user2UID: String) {
    // Asegúrate de que la variable esté correctamente referenciada en la cadena
    val chatID = if (user1UID < user2UID) "$user1UID$user2UID" else "$user2UID$user1UID"

    val db = FirebaseFirestore.getInstance()

    val chatData = hashMapOf(
        "userName" to "Pepe",  // Nombre del otro usuario, por ejemplo, el de 'pepe@uniandes.edu.co'
        "lastMessage" to "¡Hola! ¿Cómo estás?",  // Mensaje inicial
        "userImage" to "URL_imagen_usuario",  // URL de la imagen del usuario
        "userIDs" to listOf(user1UID, user2UID)  // Los dos usuarios involucrados
    )

    db.collection("chats")
        .document(chatID)
        .set(chatData)
        .addOnSuccessListener {
            Log.d("Firestore", "Chat creado exitosamente")
            // Agregar el primer mensaje en la subcolección messages
            val firstMessage = hashMapOf(
                "text" to "¡Hola! ¿Cómo estás?",  // El primer mensaje
                "senderId" to user1UID,  // El usuario que envía el mensaje (el primero)
                "timestamp" to System.currentTimeMillis()  // La marca de tiempo
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
