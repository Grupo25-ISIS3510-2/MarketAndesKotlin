package com.uniandes.marketandes.view.preferences

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.uniandes.marketandes.util.NetworkConnectivityObserver

@Composable
fun InterestSelectionScreen(
    navController: NavHostController,
    viewModel: UserPreferencesViewModel,
    isEdit: Boolean,
    preselectedInterests: List<String>
) {
    Log.d("InterestScreen", "isEdit = $isEdit")
    Log.d("InterestScreen", "Intereses recibidos por parámetro: ${preselectedInterests.joinToString()}")

    val interests = listOf(
        "Arte", "Física", "Utensilios", "Diseño", "Lenguas", "Ingeniería", "Libros", "Medicina",
        "Tecnología", "Administración", "Software", "Música", "Arquitectura", "Psicología",
        "Educación", "Química", "Economía", "Comunicación", "Derecho", "Inglés"
    )

    val selectedInterests = remember { mutableStateListOf<String>() }
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val connectivityObserver = remember { NetworkConnectivityObserver(navController.context) }
    val connectivityState = connectivityObserver.isConnected.collectAsState(initial = false)
    val isOffline = !connectivityState.value
    val context = LocalContext.current

    // Preseleccionar intereses una sola vez al iniciar
    LaunchedEffect(Unit) {
        if (preselectedInterests.isNotEmpty()) {
            selectedInterests.clear()
            selectedInterests.addAll(preselectedInterests)
            viewModel.selectedInterests = selectedInterests
        } else if (userId != null) {
            viewModel.loadInterests(userId) { savedInterests ->
                selectedInterests.clear()
                selectedInterests.addAll(savedInterests)
                viewModel.selectedInterests = selectedInterests
            }
        }
    }

    // Guardar intereses localmente si estamos offline
    fun saveInterestLocally(context: Context, interest: List<String>) {
        val sharedPreferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putStringSet("saved_interests", interest.toSet())
        editor.apply()
        Log.d("InterestScreen", "Intereses guardadas localmente: ${interest.toSet()}")
    }

    // Sincronizar las intereses cuando se restablezca la conexión
    fun syncInterestWhenOnline() {
        if (!isOffline) {
            if (userId != null) {
                Log.d("InterestScreen", "Sincronizando intereses con Firebase")
                viewModel.saveInterests(userId) {
                    Log.d("InterestScreen", "Sincronización exitosa con Firebase")
                    // Limpiar las intereses guardadas localmente después de la sincronización
                    val sharedPreferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
                    sharedPreferences.edit().remove("saved_interests").apply()
                    Toast.makeText(context, "Intereses actualizados", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            }
        }
    }

    // Observar el estado de conectividad y sincronizar cuando vuelva a estar en línea
    LaunchedEffect(connectivityState.value) {
        Log.d("InterestScreen", "Conectividad cambiada: ${connectivityState.value}")
        if (!isOffline) {
            Log.d("InterestScreen", "Conexión restaurada. Sincronizando intereses guardados localmente.")
            val sharedPreferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
            val savedInterests = sharedPreferences.getStringSet("saved_interests", emptySet())?.toList() ?: emptyList()
            if (savedInterests.isNotEmpty() && userId != null) {
                Log.d("InterestScreen", "Sincronizando intereses guardadas localmente: $savedInterests")
                viewModel.saveInterests(userId) {
                    sharedPreferences.edit().remove("saved_interests").apply()
                    Log.d("InterestScreen", "Intereses sincronizadas con éxito")
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(6.dp)
                .background(Color(0xFFFFC107))
        )

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Intereses",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF002366)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Déjanos saber tus intereses para recomendarte mejores productos!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column {
            interests.chunked(2).forEach { rowInterests ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowInterests.forEach { interest ->
                        InterestChip(
                            text = interest,
                            isSelected = selectedInterests.contains(interest),
                            onClick = {
                                if (selectedInterests.contains(interest)) {
                                    selectedInterests.remove(interest)
                                } else {
                                    selectedInterests.add(interest)
                                }
                                viewModel.selectedInterests = selectedInterests
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (userId != null) {
                    if (isOffline)
                    {
                        saveInterestLocally(context, selectedInterests)
                        Toast.makeText(context, "Intereses guardadas localmente", Toast.LENGTH_SHORT).show()
                    } else {
                        syncInterestWhenOnline()
                    }
                    Log.d("InterestScreen", "Intereses seleccionados: $selectedInterests")
                    viewModel.saveInterests(userId) {
                        if (isEdit) {
                            navController.popBackStack()
                        } else {
                            navController.navigate("pag_home")
                        }
                    }
                } else {
                    Log.e("InterestSelectionScreen", "Error: Usuario no autenticado")
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF002366)),
            shape = RoundedCornerShape(13.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = if (isEdit) "GUARDAR CAMBIOS" else "CONTINUAR",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun InterestChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                if (isSelected) Color(0xFFFFC107) else Color.LightGray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) Color(0xFF002366) else Color.Gray
        )
    }
}
