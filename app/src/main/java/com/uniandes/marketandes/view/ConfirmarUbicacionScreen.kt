package com.uniandes.marketandes.view

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.net.URLDecoder

@Composable
fun ConfirmarUbicacionScreen(navController: NavHostController) {
    val backStackEntry = navController.currentBackStackEntry
    val chatId = backStackEntry?.arguments?.getString("chatId") ?: "chatIdNoDisponible"
    val nombreUbicacion = backStackEntry?.arguments?.getString("nombreUbicacion") ?: "Ubicación no disponible"
    val imagenUrl = backStackEntry?.arguments?.getString("imagenUrl") ?: ""

    val nombreUbicacionDecoded = URLDecoder.decode(nombreUbicacion, "UTF-8")
    val imagenUrlDecoded = URLDecoder.decode(imagenUrl, "UTF-8")

    val context = LocalContext.current
    val currentUserUID = FirebaseAuth.getInstance().currentUser?.uid
    val db = FirebaseFirestore.getInstance()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Ubicación Seleccionada: $nombreUbicacionDecoded",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        if (imagenUrlDecoded.isNotEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(imagenUrlDecoded),
                contentDescription = "Imagen de la ubicación",
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
        } else {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                Text(text = "Sin Imagen", color = Color.White)
            }
        }

        Button(
            onClick = {
                Log.d("ConfirmarUbicacion", "Ubicación confirmada: $nombreUbicacionDecoded")

                if (chatId != "chatIdNoDisponible") {
                    val mensaje = hashMapOf(
                        "senderId" to currentUserUID,
                        "text" to "El punto de reunión va a ser: $nombreUbicacionDecoded",
                        "imagenUrl" to imagenUrlDecoded,
                        "timestamp" to System.currentTimeMillis()
                    )

                    db.collection("chats")
                        .document(chatId)
                        .collection("messages")
                        .add(mensaje)
                        .addOnSuccessListener {
                            Log.d("Firestore", "Mensaje enviado exitosamente")
                            Toast.makeText(context, "Ubicación compartida con éxito", Toast.LENGTH_SHORT).show()

                            val lastMessageUpdate = hashMapOf(
                                "lastMessage" to "Se ha seleccionado el punto: $nombreUbicacionDecoded"
                            )

                            db.collection("chats")
                                .document(chatId)
                                .update(lastMessageUpdate as Map<String, Any>)
                                .addOnSuccessListener {
                                    Log.d("Firestore", "Último mensaje actualizado con éxito")
                                    navController.navigate("chatDetail/$chatId") {
                                        popUpTo("chatDetail/$chatId") { inclusive = true }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.w("Firestore", "Error al actualizar el último mensaje", e)
                                    Toast.makeText(context, "Error al actualizar el último mensaje", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error al enviar el mensaje", e)
                            Toast.makeText(context, "Error al compartir ubicación", Toast.LENGTH_SHORT).show()
                        }
                }
                else {
                    Log.d("Firestore", "CHAT NO TIENE ID")
                }
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00296B))
        ) {
            Text("Confirmar Ubicación", color = Color.White)
        }
    }
}