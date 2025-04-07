package com.uniandes.marketandes.view

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.uniandes.marketandes.model.Chat

@Composable
fun PagChat(navController: NavHostController) {
    val currentUserUID = FirebaseAuth.getInstance().currentUser?.uid
    val db = FirebaseFirestore.getInstance()
    val chats = remember { mutableStateOf<List<Chat>>(emptyList()) }

    val context = LocalContext.current
    val fusedLocationClient = remember {
        com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
    }

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
                    onLocationClick = {
                        navController.navigate("PagChatMap/${chat.chatId}")
                    },
                    onUpdateLocationClick = {
                        if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            fusedLocationClient.lastLocation
                                .addOnSuccessListener { location ->
                                    if (location != null) {
                                        val lat = location.latitude
                                        val lng = location.longitude
                                        updateUserLocationInChat(chat.chatId, currentUserUID ?: "", lat, lng)
                                    } else {
                                        Log.w("Location", "No se pudo obtener la ubicación. location == null")
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.w("Location", "Error al obtener última ubicación", e)
                                }
                        } else {
                            Log.w("Location", "Permisos de ubicación no concedidos")
                        }
                    }
                )
            }
        }
    }
}

fun updateUserLocationInChat(chatId: String, userUID: String, lat: Double, lng: Double) {
    val db = FirebaseFirestore.getInstance()

    val locationUpdate = mapOf(
        "userLocations.$userUID" to com.google.firebase.firestore.GeoPoint(lat, lng)
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

@Composable
fun ChatItem(
    chat: Chat,
    onClick: () -> Unit,
    onLocationClick: () -> Unit,
    onUpdateLocationClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
    ) {
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

        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Ubicación",
            modifier = Modifier
                .clickable { onLocationClick() }
                .size(24.dp),
            tint = Color(0xFF00296B)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Button(onClick = onUpdateLocationClick) {
            Text("Upd Loc")
        }
    }
}

fun createChatBetweenUsers(user1UID: String, user2UID: String) {
    val chatID = if (user1UID < user2UID) "$user1UID$user2UID" else "$user2UID$user1UID"
    val db = FirebaseFirestore.getInstance()

    val userLocations = mapOf(
        user1UID to listOf(4.6001, -74.0659),
        user2UID to listOf(4.6015, -74.0643)
    )

    val chatData = hashMapOf(
        "userIDs" to listOf(user1UID, user2UID),
        "userName" to "Pepe",
        "lastMessage" to "¡Hola! ¿Cómo estás?",
        "userImage" to "URL_imagen_usuario",
        "userLocations" to userLocations
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