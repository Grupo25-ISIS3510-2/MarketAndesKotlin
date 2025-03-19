package com.uniandes.marketandes.ui

import android.widget.Toast
import android.os.Bundle
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.CameraUpdate

@Composable
fun MapaScreen(navController: NavHostController) {
    val context = LocalContext.current

    // Inicializar MapView
    val mapView = remember { MapView(context) }

    // Lista de puntos de interés
    val puntosDeInteres = listOf(
        LatLng(4.60971, -74.08175), // Ejemplo de punto de interés
        LatLng(4.61072, -74.07561),
        LatLng(4.61754, -74.08412)
    )

    // Configuración del ciclo de vida del MapView
    LaunchedEffect(mapView) {
        mapView.onCreate(Bundle())
        mapView.onResume()
    }

    DisposableEffect(mapView) {
        onDispose {
            mapView.onPause()
            mapView.onStop()
        }
    }

    // Configuración del mapa
    var googleMap: GoogleMap? by remember { mutableStateOf(null) }

    LaunchedEffect(mapView) {
        mapView.getMapAsync { map ->
            googleMap = map

            // Configurar los controles del mapa
            map.uiSettings.isZoomControlsEnabled = true
            map.uiSettings.isMyLocationButtonEnabled = true

            // Agregar los marcadores al mapa
            puntosDeInteres.forEach { punto ->
                map.addMarker(MarkerOptions().position(punto).title("Punto de interés"))
            }

            // Centrar la cámara en el primer punto
            val cameraPosition = CameraUpdateFactory.newLatLngZoom(puntosDeInteres[0], 12f)
            map.moveCamera(cameraPosition)

            // Acción al hacer click en el marcador
            map.setOnMarkerClickListener { marker ->
                Toast.makeText(context, "Punto seleccionado: ${marker.title}", Toast.LENGTH_SHORT).show()
                true
            }
        }
    }

    // UI para mostrar el mapa y los botones
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Selecciona un punto de encuentro",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // Acción para confirmar el punto de encuentro
                navController.navigate("confirmarUbicacion")
            }
        ) {
            Text("Seleccionar punto")
        }
    }
}
