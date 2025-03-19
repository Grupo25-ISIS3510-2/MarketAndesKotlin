package com.uniandes.marketandes.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.uniandes.marketandes.ui.model.Message
import androidx.compose.foundation.lazy.rememberLazyListState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(chatId: String, navController: NavHostController) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()

    var message by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }

    // Control para LazyColumn (para desplazarse automáticamente al último mensaje)
    val scrollState = rememberLazyListState()

    // Usamos un CoroutineScope explícito para manejar la corrutina
    val coroutineScope = rememberCoroutineScope()

    // Usamos LaunchedEffect para escuchar los cambios de Firestore y hacer scroll automático
    LaunchedEffect(chatId) {
        // Cargar los mensajes y escucharlos en tiempo real
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    messages = snapshot.documents.map {
                        Message(
                            text = it.getString("text") ?: "",
                            senderId = it.getString("senderId") ?: "",
                            timestamp = it.getLong("timestamp") ?: 0L
                        )
                    }
                    // Desplazamos al último mensaje cuando los mensajes se actualizan
                    coroutineScope.launch {
                        scrollState.animateScrollToItem(messages.size - 1)
                    }
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Mostrar los mensajes
        LazyColumn(state = scrollState, modifier = Modifier.weight(1f)) {
            items(messages) { message ->
                MessageBubble(message = message, currentUserUID = currentUser?.uid ?: "")
            }
        }

        // Campo para escribir nuevos mensajes
        TextField(
            value = message,
            onValueChange = { newMessage -> message = newMessage },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF1F1F1)),  // Fondo gris claro
            placeholder = { Text(text = "Escribe tu mensaje...") },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color(0xFFF1F1F1),  // Fondo gris claro
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Fila de botones
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 3.dp),  // Espaciado inferior
            horizontalArrangement = Arrangement.SpaceBetween  // Para separar los botones
        ) {
            // Botón para volver a la pantalla de chats
            Button(
                onClick = {
                    navController.popBackStack() // Vuelve a la pantalla de chats
                },
                modifier = Modifier.weight(1f),  // Ocupa la mitad del espacio
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00296B))  // Azul
            ) {
                Text("Volver", color = Color.White)
            }

            Spacer(modifier = Modifier.width(8.dp)) // Espacio entre los botones

            Button(
                onClick = {
                    if (message.isNotEmpty()) {
                        val newMessage = hashMapOf(
                            "text" to message,
                            "senderId" to currentUser?.uid,
                            "timestamp" to System.currentTimeMillis()
                        )

                        // Primero, agregamos el mensaje a la subcolección "messages"
                        db.collection("chats")
                            .document(chatId)
                            .collection("messages")
                            .add(newMessage)
                            .addOnSuccessListener { documentReference ->
                                Log.d("Firestore", "Mensaje enviado con éxito")

                                // Actualizamos el campo 'lastMessage' en el documento principal del chat
                                val lastMessageUpdate = mutableMapOf<String, Any>(
                                    "lastMessage" to message,  // Actualiza el último mensaje con el contenido
                                )

                                // Aquí actualizamos el documento principal
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
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Error al enviar el mensaje", e)
                            }

                        // Limpiar el campo de texto después de enviar el mensaje
                        message = ""
                    }
                },
                modifier = Modifier.weight(1f),  // Ocupa la mitad del espacio
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00296B))  // Azul
            ) {
                Text("Enviar", color = Color.White)
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message, currentUserUID: String) {
    val isCurrentUser = message.senderId == currentUserUID
    val backgroundColor = if (isCurrentUser) Color(0xFFFDC500) else Color(0xFF00509D)
    val textColor = if (isCurrentUser) Color.Black else Color.White
    val alignment = if (isCurrentUser) Alignment.End else Alignment.Start

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        // Burbuja del mensaje
        Column(
            modifier = Modifier
                .background(backgroundColor, RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Text(
                text = message.text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = textColor
            )
        }
    }
}

