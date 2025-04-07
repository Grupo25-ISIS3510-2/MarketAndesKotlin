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
fun FacultySelectionScreen(
    navController: NavHostController,
    viewModel: UserPreferencesViewModel,
    isEdit: Boolean,
    preselectedFaculties: List<String>
) {
    Log.d("FacultyScreen", "isEdit = $isEdit")
    Log.d("FacultyScreen", "Facultades recibidas por parámetro: ${preselectedFaculties.joinToString()}")

    val faculties = listOf(
        "Medicina", "Derecho", "Ingeniería", "Educación", "Ciencias",
        "Ciencias Sociales", "Economía", "Artes y humanidades",
        "Administración", "Arquitectura"
    )

    val selectedFaculties = remember { mutableStateListOf<String>() }
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    // Preseleccionar facultades una sola vez al iniciar
    LaunchedEffect(Unit) {
        if (preselectedFaculties.isNotEmpty()) {
            selectedFaculties.clear()
            selectedFaculties.addAll(preselectedFaculties)
            viewModel.selectedFaculties = selectedFaculties
        } else if (userId != null) {
            viewModel.loadFaculties(userId) { savedFaculties ->
                selectedFaculties.clear()
                selectedFaculties.addAll(savedFaculties)
                viewModel.selectedFaculties = selectedFaculties
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
            text = "Facultades",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF002366)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Déjanos saber tu facultad para recomendarte mejores productos!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

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
                                    selectedFaculties.add(faculty)
                                }
                                viewModel.selectedFaculties = selectedFaculties
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
                    Log.d("FacultyScreen", "Facultades seleccionadas: $selectedFaculties")
                    viewModel.saveFaculties(userId) {
                        if (isEdit) {
                            navController.popBackStack()
                        } else {
                            navController.navigate("interest_selection")
                        }
                    }
                } else {
                    Log.e("FacultySelectionScreen", "Error: Usuario no autenticado")
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
fun FacultyChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
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
