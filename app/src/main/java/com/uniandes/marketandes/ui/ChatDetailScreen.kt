package com.uniandes.marketandes.ui

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.uniandes.marketandes.ui.model.Message


@Composable
fun ChatDetailScreen(chatId: String, navController: NavHostController) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()

    var message = remember { mutableStateOf("") }
    var messages = remember { mutableStateOf<List<Message>>(emptyList()) }

    // Cargar los mensajes al inicio o cuando cambia el chatId
    LaunchedEffect(chatId) {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp") // Ordenamos por la fecha
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    messages.value = snapshot.documents.map {
                        Message(
                            text = it.getString("text") ?: "",  // Obtiene el texto
                            senderId = it.getString("senderId") ?: "",  // Obtiene el ID del remitente
                            timestamp = it.getLong("timestamp") ?: 0L  // Obtiene la marca de tiempo
                        )
                    }
                }
            }
    }

    // Componente de UI para mostrar los mensajes y enviar nuevos
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(messages.value) { message ->  // Accedemos a `messages.value`
                MessageBubble(message = message)  // Pasamos cada `message` a MessageBubble
            }
        }

        // Campo para escribir nuevos mensajes
        TextField(
            value = message.value,  // Acceder al valor real del estado
            onValueChange = { newMessage -> message.value = newMessage },  // Actualiza el valor del estado
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = "Escribe tu mensaje...") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Botón para enviar el mensaje
        Button(
            onClick = {
                if (message.value.isNotEmpty()) {  // Usamos message.value en lugar de message
                    // Crear el mensaje
                    val newMessage = hashMapOf(
                        "text" to message.value,
                        "senderId" to currentUser?.uid,  // ID del usuario que está enviando el mensaje
                        "timestamp" to System.currentTimeMillis()  // Marca de tiempo del mensaje
                    )

                    // Agregar el mensaje a la subcolección "messages"
                    db.collection("chats")
                        .document(chatId)  // Usamos el chatId para identificar el chat
                        .collection("messages")  // Subcolección donde se guardan los mensajes
                        .add(newMessage)  // Agregar el nuevo mensaje
                        .addOnSuccessListener {
                            Log.d("Firestore", "Mensaje enviado con éxito")
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error al enviar el mensaje", e)
                        }

                    message.value = "" // Limpiar el campo de texto después de enviar el mensaje
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enviar")
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    Column {
        Text(text = "${message.senderId}: ${message.text}")
    }
}
