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
fun InterestSelectionScreen(navController: NavHostController, viewModel: UserPreferencesViewModel)
{
    val interests = listOf("Arte", "Física", "Software", "Libros", "Música")
    val selectedInterests = remember { mutableStateListOf<String>() }

    Column {
        Text("Selecciona tus intereses")
        interests.forEach { interest ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (selectedInterests.contains(interest)) {
                            selectedInterests.remove(interest)
                        } else {
                            selectedInterests.add(interest)
                        }
                    }
            ) {
                Checkbox(checked = selectedInterests.contains(interest), onCheckedChange = null)
                Text(interest)
            }
        }



        Button(onClick = {
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            if (userId != null) {
                viewModel.selectedInterests = selectedInterests
                viewModel.saveInterests(userId) {  // Usamos el UID real del usuario
                    navController.navigate("pag_home")
                }
            } else {
                Log.e("InterestSelectionScreen", "Error: Usuario no autenticado")
            }
        }) {
            Text("Continuar")
        }
    }
}
