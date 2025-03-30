package com.uniandes.marketandes.ui.preferences

import UserPreferencesViewModel
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun FacultySelectionScreen(navController: NavHostController, viewModel: UserPreferencesViewModel) {
    val faculties = listOf("Medicina", "Derecho", "Ingeniería", "Educación", "Administración")
    val selectedFaculties = remember { mutableStateListOf<String>() }

    Column {
        Text("Selecciona tus facultades")
        faculties.forEach { faculty ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (selectedFaculties.contains(faculty)) {
                            selectedFaculties.remove(faculty)
                        } else {
                            selectedFaculties.add(faculty)
                        }
                    }
            ) {
                Checkbox(checked = selectedFaculties.contains(faculty), onCheckedChange = null)
                Text(faculty)
            }
        }

        Button(onClick = {
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            if (userId != null) {
                viewModel.selectedFaculties = selectedFaculties
                viewModel.saveFaculties(userId) {
                    navController.navigate("interest_selection")
                }
            } else {
                Log.e("FacultySelectionScreen", "Error: Usuario no autenticado")
            }
        }) {
            Text("Continuar")
        }
    }
}
