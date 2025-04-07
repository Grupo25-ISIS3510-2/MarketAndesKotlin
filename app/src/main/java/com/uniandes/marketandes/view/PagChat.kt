package com.uniandes.marketandes.view

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.uniandes.marketandes.model.Chat
import com.uniandes.marketandes.viewModel.PagChatViewModel
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
fun PagChat(navController: NavHostController, viewModel: PagChatViewModel = remember { PagChatViewModel() }) {
    val currentUserUID = FirebaseAuth.getInstance().currentUser?.uid
    val context = LocalContext.current
    val fusedLocationClient = remember {
        com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
    }

    val chats by viewModel.chats

    LaunchedEffect(currentUserUID) {
        currentUserUID?.let {
            viewModel.fetchChats(it)
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
            items(chats) { chat ->
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
                                        currentUserUID?.let { userUID ->
                                            viewModel.updateUserLocationInChat(chat.chatId, userUID, lat, lng)
                                        }
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
            .background(Color(0xFFF1F1F1), shape = RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            model = chat.userImage,
            contentDescription = "Foto de perfil",
            modifier = Modifier
                .size(48.dp)
                .aspectRatio(1f)
                .background(Color.LightGray, shape = RoundedCornerShape(24.dp)),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = chat.userName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = chat.lastMessage,
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 1
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = onLocationClick) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Ver ubicación",
                    modifier = Modifier.size(28.dp),
                    tint = Color(0xFF00296B)
                )
            }

            IconButton(onClick = onUpdateLocationClick) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Actualizar ubicación",
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFF00296B)
                )
            }
        }
    }
}