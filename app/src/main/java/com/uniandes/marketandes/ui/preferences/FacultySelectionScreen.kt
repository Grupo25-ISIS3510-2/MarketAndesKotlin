package com.uniandes.marketandes.ui.preferences

import UserPreferencesViewModel
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth


@Composable
fun FacultySelectionScreen(navController: NavHostController, viewModel: UserPreferencesViewModel)
{
    val faculties = listOf(
        "Medicina", "Derecho", "Ingeniería", "Educación", "Ciencias",
        "Ciencias Sociales", "Economía", "Artes y humanidades",
        "Administración", "Arquitectura"
    )
    val selectedFaculties = remember { mutableStateListOf<String>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Barra amarilla de progreso
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f) // Solo hasta la mitad
                .height(6.dp)
                .background(Color(0xFFFFC107)) // Amarillo
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Título y descripción
        Text(
            text = "Facultades",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF002366) // Azul oscuro
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Déjanos saber tu facultad para recomendarte mejores productos!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de facultades en ovalo
        Column {
            faculties.chunked(2).forEach { rowFaculties ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowFaculties.forEach { faculty ->
                        FacultyChip(
                            text = faculty,
                            isSelected = selectedFaculties.contains(faculty),
                            onClick = {
                                if (selectedFaculties.contains(faculty)) {
                                    selectedFaculties.remove(faculty)
                                } else {
                                    selectedFaculties.add(faculty) // Se permite seleccionar varias
                                }
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Botón de continuar
        Button(
            onClick = {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    viewModel.selectedFaculties = selectedFaculties
                    viewModel.saveFaculties(userId) {
                        navController.navigate("interest_selection")
                    }
                } else {
                    Log.e("FacultySelectionScreen", "Error: Usuario no autenticado")
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF002366)), // Azul oscuro
            shape = RoundedCornerShape(13.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = "CONTINUAR", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun FacultyChip(text: String, isSelected: Boolean, onClick: () -> Unit)
{
    Box(
        modifier = Modifier
            .background(
                if (isSelected) Color(0xFFFFC107) else Color.LightGray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp) // Más redondeado
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
