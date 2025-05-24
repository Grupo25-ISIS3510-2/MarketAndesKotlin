import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.outlined.SignalWifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.uniandes.marketandes.util.NetworkConnectivityObserver
import com.uniandes.marketandes.view.user_preferences.PerfilViewModel
import kotlinx.coroutines.launch

@Composable
fun PerfilScreen(
    navController: NavHostController,
    viewModel: PerfilViewModel = viewModel()
) {
    val context = LocalContext.current
    val perfilState by viewModel.perfilState.collectAsState()
    val user = FirebaseAuth.getInstance().currentUser
    val uid = user?.uid
    val connectivityObserver = remember { NetworkConnectivityObserver(context) }

    val connectivityState = connectivityObserver.isConnected.collectAsState(initial = false)
    val isOffline = !connectivityState.value

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uid) {
        if (uid != null) {
            viewModel.cargarPerfil()
            Log.d("PerfilScreen", "Cargando perfil de usuario $uid")
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF00205B))
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Mensaje de sin conexión
            if (isOffline) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SignalWifiOff,
                        contentDescription = "Sin conexión",
                        tint = Color(0xFF856404),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Estás sin conexión. Algunos datos podrían no estar actualizados.",
                        color = Color(0xFF856404),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        tint = Color(0xFF00205B)
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Perfil de Usuario",
                        fontSize = 22.sp,
                        color = Color(0xFF00205B),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(Modifier.height(24.dp))

                    OutlinedTextField(
                        value = perfilState.nombre,
                        onValueChange = { viewModel.onNombreChange(it) },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = perfilState.fechaNacimiento,
                        onValueChange = { viewModel.onFechaNacimientoChange(it) },
                        label = { Text("Fecha nacimiento") },
                        trailingIcon = {
                            Icon(Icons.Default.DateRange, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = user?.email ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Correo electrónico") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = perfilState.telefono,
                        onValueChange = { viewModel.onTelefonoChange(it) },
                        label = { Text("Número de teléfono") },
                        prefix = { Text("+57 ") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            uid?.let {
                                viewModel.guardarPerfil()

                                // Actualizar lastProfileUpdate en Firestore
                                val firestore = FirebaseFirestore.getInstance()
                                firestore.collection("users").document(uid)
                                    .update("lastProfileUpdate", com.google.firebase.Timestamp.now())

                                val mensaje = if (isOffline) {
                                    "Datos de perfil guardados. Se sincronizará cuando regrese la conexión."
                                } else {
                                    "Perfil actualizado correctamente."
                                }

                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = mensaje,
                                        actionLabel = "Entendido",
                                        duration = SnackbarDuration.Indefinite
                                    )
                                }

                                Log.d("PerfilScreen", "Datos guardados para usuario $uid")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00205B))
                    ) {
                        Text("GUARDAR", color = Color.White)
                    }


                    Spacer(Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            navController.navigate("edit_faculties?preselected=${perfilState.facultades.joinToString(",")}")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00205B))
                    ) {
                        Text("Editar facultades")
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            navController.navigate("edit_interests?preselected=${perfilState.intereses.joinToString(",")}")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00205B))
                    ) {
                        Text("Editar intereses")
                    }

                    if (perfilState.mensaje.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Text(text = perfilState.mensaje, color = Color(0xFF00205B))
                    }
                }
            }
        }
    }
}
