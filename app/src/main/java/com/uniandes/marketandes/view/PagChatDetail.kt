package com.uniandes.marketandes.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.uniandes.marketandes.model.Message
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uniandes.marketandes.viewModel.ChatDetailViewModel
import com.uniandes.marketandes.viewModel.ChatDetailViewModelFactory
import com.uniandes.marketandes.repository.ChatRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.uniandes.marketandes.local.AppDatabase
import coil.compose.AsyncImage
import com.uniandes.marketandes.viewModel.ConnectivityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagChatDetail(chatId: String) {
    val context = LocalContext.current
    val messageDao = AppDatabase.getDatabase(context).messageDao()
    val repository = ChatRepository(FirebaseFirestore.getInstance(), messageDao)
    val viewModel: ChatDetailViewModel = viewModel(factory = ChatDetailViewModelFactory(repository))

    val connectivityViewModel: ConnectivityViewModel = viewModel()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUserUID = currentUser?.uid ?: ""

    val messages by viewModel.messages
    val message by viewModel.message

    val productName = remember { mutableStateOf("Producto") }
    val roleLabel = remember { mutableStateOf("Rol") }
    val otherUserName = remember { mutableStateOf("Usuario") }
    val otherUserImage = remember { mutableStateOf("") }

    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        connectivityViewModel.checkConnectivity(context)
    }

    LaunchedEffect(chatId) {
        viewModel.fetchMessages(chatId)

        FirebaseFirestore.getInstance().collection("chats")
            .document(chatId)
            .get()
            .addOnSuccessListener { document ->
                productName.value = document.getString("productName") ?: "Producto"

                val userIDs = document.get("userIDs") as? List<String>
                val userNames = document.get("userName") as? List<String>
                val userImages = document.get("userImage") as? List<String>

                if (userIDs != null && userNames != null && userImages != null) {
                    val otherIndex = if (currentUserUID == userIDs[0]) 1 else 0
                    otherUserName.value = userNames.getOrNull(otherIndex) ?: "Usuario"
                    otherUserImage.value = userImages.getOrNull(otherIndex) ?: ""
                    roleLabel.value = if (currentUserUID == userIDs[0]) {
                        "Comprador ${productName.value}"
                    } else {
                        "Vendedor ${productName.value}"
                    }
                }
            }
    }

    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                scrollState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            GlideImage(
                imageUrl = otherUserImage.value,
                contentDescription = "Foto del otro usuario",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )

            Column {
                Text(
                    text = otherUserName.value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = roleLabel.value,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Icon(
                imageVector = if (connectivityViewModel.isConnected.value) Icons.Default.Wifi else Icons.Default.WifiOff,
                contentDescription = "Conexión",
                tint = if (connectivityViewModel.isConnected.value) Color(0xFFFFFFFF) else Color(0xFF00509D),
                modifier = Modifier.size(24.dp)
            )
        }

        LazyColumn(state = scrollState, modifier = Modifier.weight(1f)) {
            items(messages) { message ->
                MessageBubble(message = message, currentUserUID = currentUserUID)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = message,
                onValueChange = { viewModel.message.value = it },
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFFF1F1F1), shape = RoundedCornerShape(24.dp)),
                placeholder = { Text(text = "Escribe tu mensaje...") },
                trailingIcon = {
                    IconButton(onClick = {
                        viewModel.sendMessage(chatId, currentUserUID)
                    }) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Enviar")
                    }
                },
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color(0xFFF1F1F1),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun MessageBubble(message: Message, currentUserUID: String) {
    val isCurrentUser = message.senderId == currentUserUID
    val backgroundColor = if (isCurrentUser) Color(0xFFFDC500) else Color(0xFF00509D)
    val textColor = if (isCurrentUser) Color.Black else Color.White

    val formattedTime = formatTimestamp(message.timestamp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
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
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formattedTime,
                fontSize = 10.sp,
                color = textColor.copy(alpha = 0.7f)
            )
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(date)
}