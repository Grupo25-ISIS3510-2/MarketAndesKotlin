package com.uniandes.marketandes.view.preferences

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.uniandes.marketandes.util.NetworkConnectivityObserver

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
    val connectivityObserver = remember { NetworkConnectivityObserver(navController.context) }
    val connectivityState = connectivityObserver.isConnected.collectAsState(initial = false)
    val isOffline = !connectivityState.value
    val context = LocalContext.current

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

    // Guardar facultades localmente si estamos offline
    fun saveFacultiesLocally(context: Context, faculties: List<String>) {
        val sharedPreferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putStringSet("saved_faculties", faculties.toSet())
        editor.apply()
        Log.d("FacultyScreen", "Facultades guardadas localmente: ${faculties.toSet()}")
    }




    // Sincronizar las facultades cuando se restablezca la conexión
    fun syncFacultiesWhenOnline() {
        if (!isOffline) {
            if (userId != null) {
                Log.d("FacultyScreen", "Sincronizando facultades con Firebase")
                viewModel.saveFaculties(userId) {
                    Log.d("FacultyScreen", "Sincronización exitosa con Firebase")
                    // Limpiar las facultades guardadas localmente después de la sincronización
                    val sharedPreferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
                    sharedPreferences.edit().remove("saved_faculties").apply()
                    Toast.makeText(context, "Facultades actualizadas", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            }
        }
    }

    // Observar el estado de conectividad y sincronizar cuando vuelva a estar en línea
    LaunchedEffect(connectivityState.value) {
        Log.d("FacultyScreen", "Conectividad cambiada: ${connectivityState.value}")
        if (!isOffline) {
            Log.d("FacultyScreen", "Conexión restaurada. Sincronizando facultades guardadas localmente.")
            val sharedPreferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
            val savedFaculties = sharedPreferences.getStringSet("saved_faculties", emptySet())?.toList() ?: emptyList()
            if (savedFaculties.isNotEmpty() && userId != null) {
                Log.d("FacultyScreen", "Sincronizando facultades guardadas localmente: $savedFaculties")
                viewModel.saveFaculties(userId) {
                    sharedPreferences.edit().remove("saved_faculties").apply()
                    Log.d("FacultyScreen", "Facultades sincronizadas con éxito")
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
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

        Spacer(modifier = Modifier.height(16.dp))  // Espacio antes del botón

        Button(
            onClick = {
                if (userId != null) {
                    Log.d("FacultyScreen", "Facultades seleccionadas: $selectedFaculties")

                    // Si estamos offline, guardamos localmente las facultades seleccionadas
                    if (isOffline) {
                        saveFacultiesLocally(context = navController.context, faculties = selectedFaculties)
                        Log.d("FacultyScreenOFFLINE", "Guardando facultades localmente: $selectedFaculties")
                    } else {
                        // Si estamos online, sincronizamos con Firebase
                        viewModel.saveFaculties(userId) {
                            Log.d("FacultyScreen", "Facultades sincronizadas con Firebase")
                            // Limpiar facultades guardadas localmente después de la sincronización
                            val sharedPreferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
                            sharedPreferences.edit().remove("saved_faculties").apply()
                            Toast.makeText(context, "Facultades actualizadas", Toast.LENGTH_SHORT).show()

                            // Navegar dependiendo de si estamos editando o no
                            if (isEdit) {
                                navController.popBackStack()  // Volver atrás si estamos en modo de edición
                            } else {
                                navController.navigate("interest_selection")  // Ir a la siguiente pantalla si no
                            }
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
