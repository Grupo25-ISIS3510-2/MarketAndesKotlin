package com.uniandes.marketandes.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun Pag_seleccion(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top, // El título ahora está en la parte superior
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { navController.navigate("pag_vender") },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003F88)),
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(vertical = 16.dp)
        ) {
            Text(text = "VENDER", fontSize = 20.sp, color = Color.White)
        }

        Button(
            onClick = { navController.navigate("pag_ExchangeProduct") },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003F88)),
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(vertical = 16.dp)
        ) {
            Text(text = "INTERCAMBIAR", fontSize = 20.sp, color = Color.White)
        }
    }
}
