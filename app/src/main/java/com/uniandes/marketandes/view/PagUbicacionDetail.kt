package com.uniandes.marketandes.view
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

import coil.compose.rememberAsyncImagePainter
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.uniandes.marketandes.viewModel.ConnectivityViewModel
import java.net.URLDecoder
import java.net.URLEncoder



@Composable

fun UbicacionDetail (navController: NavHostController) {
    val backStackEntry = navController.currentBackStackEntry
    val nombreUbicacion =
        backStackEntry?.arguments?.getString("nombreUbicacion") ?: "Ubicación no disponible"
    val imagenUrl = backStackEntry?.arguments?.getString("imagenUrl") ?: ""
    val nombreUbicacionDecoded = URLDecoder.decode(nombreUbicacion, "UTF-8")
    val imagenUrlDecoded = URLDecoder.decode(imagenUrl, "UTF-8")
    val direccion = backStackEntry?.arguments?.getString("direccion") ?: "Direccion no disponible"
    val direccionDecoded = URLDecoder.decode(direccion, "UTF-8")

    val context = LocalContext.current
    val connectivityViewModel: ConnectivityViewModel = viewModel()
    var showError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        connectivityViewModel.checkConnectivity(context)
    }



    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Tienda: $nombreUbicacionDecoded",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (imagenUrlDecoded.isNotEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(imagenUrlDecoded),
                contentDescription = "Imagen de la ubicación",
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
        } else {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                Text(text = "Sin Imagen", color = Color.White)
            }
        }

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00296B))
        ) {
            Text("Volver", color = Color.White)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                connectivityViewModel.checkConnectivity(context)

                if (connectivityViewModel.isConnected.value) {
                    val firestore = FirebaseFirestore.getInstance()
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonimo"

                    val clickData = hashMapOf(
                        "tienda_nombre" to nombreUbicacionDecoded,
                        "imagen_url" to imagenUrlDecoded,
                        "direccion" to direccionDecoded,
                        "timestamp" to FieldValue.serverTimestamp(),
                        "user_id" to userId
                    )

                    firestore.collection("clics_ir_tienda")
                        .add(clickData)
                        .addOnSuccessListener {
                            Log.d("Firestore", "Evento guardado")
                        }

                    navController.navigate(
                        "storemaps?destinoNombre=${
                            URLEncoder.encode(
                                nombreUbicacionDecoded,
                                "UTF-8"
                            )
                        }&destinoImagen=${URLEncoder.encode(imagenUrlDecoded, "UTF-8")}"+
                        "&destinoDireccion=${URLEncoder.encode(direccionDecoded, "UTF-8")}"
                    )
                } else {
                    showError = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A9396))
        )

        {
            Text("Ir", color = Color.White)
        }

        if (showError) {
            ErrorDialog(
                message = "No tienes conexión estable. Dirección de la tienda: $direccionDecoded",
                onDismiss = { showError = false }
            )
        }

    }
}

@Composable
fun ErrorDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Conexión inestable") },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Aceptar")
            }
        }
    )
}