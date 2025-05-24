package com.uniandes.marketandes.view

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Date

@Composable
fun Pag_seleccion(navController: NavController) {
    val context = LocalContext.current
    val db = Firebase.firestore

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    )


    {

        Text(
            text = "Vender o intercambiar productos",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        Spacer(modifier = Modifier.height(50.dp))
        ActionButton(
            text = "VENDER",
            icon = "🛒",
            onClick = {
                val data = hashMapOf(
                    "action" to "vender",
                    "timestamp" to Date()
                )
                db.collection("button_clicks")
                    .add(data)
                    .addOnSuccessListener {
                        navController.navigate("pag_vender")
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error registrando clic", Toast.LENGTH_SHORT).show()
                    }
            }
        )

        Spacer(modifier = Modifier.height(50.dp))

        ActionButton(
            text = "INTERCAMBIAR",
            icon = "♻️",
            onClick = {
                val data = hashMapOf(
                    "action" to "intercambiar",
                    "timestamp" to Date()
                )
                db.collection("button_clicks")
                    .add(data)
                    .addOnSuccessListener {
                        navController.navigate("pag_ExchangeProduct")
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error registrando clic", Toast.LENGTH_SHORT).show()
                    }
            }
        )
    }
}

@Composable
fun ActionButton(text: String, icon: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003F88)),
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .shadow(elevation = 10.dp, shape = MaterialTheme.shapes.large),
        shape = MaterialTheme.shapes.large
    ) {
        Text(
            text = "$icon  $text",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}
