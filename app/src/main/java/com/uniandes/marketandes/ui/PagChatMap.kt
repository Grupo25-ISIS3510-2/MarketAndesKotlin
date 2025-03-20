package com.uniandes.marketandes.ui

import android.os.Bundle
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.net.URLEncoder

@Composable
fun PagChatMap(chatId: String, navController: NavHostController) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    val puntosDeInteres = listOf(
        PuntoDeInteres(
            latLng = LatLng(4.603100641251616, -74.06514973706602),
            nombreUbicacion = "Entrada del ML",
            imagenUrl = "https://www.uniandes.edu.co/sites/default/files/news2/Tec-Monterrey-ng.jpg"
        ),
        PuntoDeInteres(
            latLng = LatLng(4.601203630411922, -74.06556084750304),
            nombreUbicacion = "El bobo",
            imagenUrl = "https://static.wixstatic.com/media/4f9e1c_6b98fba98691417eb4525fbbcc540e96~mv2.jpg/v1/fill/w_320,h_410,fp_0.50_0.50,q_90/4f9e1c_6b98fba98691417eb4525fbbcc540e96~mv2.jpg"
        ),
        PuntoDeInteres(
            latLng = LatLng(4.602659914804456, -74.06631365426061),
            nombreUbicacion = "Primer piso del Aulas",
            imagenUrl = "https://pinillaarquitectos.com/wp/wp-content/uploads/2024/07/20240521_130856-edited-scaled.jpg"
        )
    )

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

    LaunchedEffect(mapView) {
        mapView.getMapAsync { map ->
            puntosDeInteres.forEach { punto ->
                val markerOptions = MarkerOptions()
                    .position(punto.latLng)
                    .title(punto.nombreUbicacion)
                val marker = map.addMarker(markerOptions)

                map.setOnMarkerClickListener { clickedMarker ->
                    val selectedPunto = puntosDeInteres.find { it.nombreUbicacion == clickedMarker.title }
                    if (selectedPunto != null) {
                        showLocationInfo(selectedPunto, navController, chatId)
                    }
                    true
                }
            }
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(puntosDeInteres[0].latLng, 15f))
        }
    }

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
    }
}

fun showLocationInfo(punto: PuntoDeInteres, navController: NavHostController, chatId: String) {
    val nombreUbicacion = URLEncoder.encode(punto.nombreUbicacion, "UTF-8")
    val imagenUrl = URLEncoder.encode(punto.imagenUrl, "UTF-8")

    navController.navigate("confirmarUbicacion/$chatId/$nombreUbicacion/$imagenUrl")
}