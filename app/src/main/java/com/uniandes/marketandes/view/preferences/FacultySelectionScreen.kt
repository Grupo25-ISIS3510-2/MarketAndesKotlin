package com.uniandes.marketandes.view.preferences

import android.content.Context
import android.util.Log
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
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.uniandes.marketandes.util.NetworkConnectivityObserver
import kotlinx.coroutines.launch

@Composable
fun FacultySelectionScreen(
    navController: NavHostController,
    viewModel: UserPreferencesViewModel,
    isEdit: Boolean,
    preselectedFaculties: List<String>
) {
    val faculties = listOf(
        "Medicina", "Derecho", "Ingeniería", "Educación", "Ciencias",
        "Ciencias Sociales", "Economía", "Artes y humanidades",
        "Administración", "Arquitectura"
    )

    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val connectivityObserver = remember { NetworkConnectivityObserver(context) }
    val connectivityState by connectivityObserver.isConnected.collectAsState(initial = false)
    val isOffline = !connectivityState

    val selectedFaculties = remember { mutableStateListOf<String>() }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (preselectedFaculties.isNotEmpty()) {
            selectedFaculties.clear()
            selectedFaculties.addAll(preselectedFaculties)
            viewModel.selectedFaculties = selectedFaculties.toList()
        } else if (userId != null) {
            viewModel.loadFaculties(userId) { savedFaculties ->
                selectedFaculties.clear()
                selectedFaculties.addAll(savedFaculties)
                viewModel.selectedFaculties = selectedFaculties.toList()
            }
        }
    }

    LaunchedEffect(connectivityState) {
        if (!isOffline && userId != null) {
            val savedFaculties = getSavedFaculties(context)
            if (savedFaculties.isNotEmpty()) {
                Log.d("FacultyScreen", "Sincronizando localmente guardadas: $savedFaculties")
                viewModel.saveFaculties(userId) {
                    clearSavedFaculties(context)
                    Log.d("FacultyScreen", "Facultades sincronizadas")
                }
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    actionColor = Color.White,
                    containerColor = Color(0xFF00205B),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(paddingValues)
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
                                    viewModel.selectedFaculties = selectedFaculties.toList()
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (userId != null) {
                        Log.d("FacultyScreen", "Seleccionadas: $selectedFaculties")

                        if (isOffline) {
                            saveFacultiesLocally(context, selectedFaculties)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Facultades actualizadas, se sincronizará cuando regrese la conexión",
                                    actionLabel = "Entendido",
                                    duration = SnackbarDuration.Indefinite
                                )
                            }
                        } else {
                            viewModel.saveFaculties(userId) {
                                clearSavedFaculties(context)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Facultades actualizadas",
                                        actionLabel = "Entendido",
                                        duration = SnackbarDuration.Indefinite
                                    )
                                }
                                if (isEdit) navController.popBackStack()
                                else navController.navigate("interest_selection")
                            }
                        }
                    } else {
                        Log.e("FacultySelectionScreen", "Usuario no autenticado")
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
}

@Composable
fun FacultyChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                color = if (isSelected) Color(0xFFFFC107) else Color.LightGray.copy(alpha = 0.3f),
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

// Utilidades

private fun saveFacultiesLocally(context: Context, faculties: List<String>) {
    val prefs = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
    prefs.edit().putStringSet("saved_faculties", faculties.toSet()).apply()
    Log.d("FacultyScreen", "Guardadas localmente: ${faculties.toSet()}")
}

private fun getSavedFaculties(context: Context): List<String> {
    val prefs = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
    return prefs.getStringSet("saved_faculties", emptySet())?.toList() ?: emptyList()
}

private fun clearSavedFaculties(context: Context) {
    val prefs = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
    prefs.edit().remove("saved_faculties").apply()
}
