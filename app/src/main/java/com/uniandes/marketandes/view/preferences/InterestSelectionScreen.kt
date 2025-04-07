package com.uniandes.marketandes.view.preferences

import UserPreferencesViewModel
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth

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
