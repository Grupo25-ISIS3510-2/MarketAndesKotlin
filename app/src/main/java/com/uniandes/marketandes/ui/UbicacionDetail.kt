
package com.uniandes.marketandes.ui
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.net.URLDecoder

@Composable
fun UbicacionDetail (navController: NavHostController) {
    val backStackEntry = navController.currentBackStackEntry
    val nombreUbicacion = backStackEntry?.arguments?.getString("nombreUbicacion") ?: "Ubicación no disponible"
    val imagenUrl = backStackEntry?.arguments?.getString("imagenUrl") ?: ""

    val nombreUbicacionDecoded = URLDecoder.decode(nombreUbicacion, "UTF-8")
    val imagenUrlDecoded = URLDecoder.decode(imagenUrl, "UTF-8")

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
    }
}