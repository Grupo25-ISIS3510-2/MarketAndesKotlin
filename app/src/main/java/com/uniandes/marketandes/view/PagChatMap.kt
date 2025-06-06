package com.uniandes.marketandes.view

import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.uniandes.marketandes.R
import com.uniandes.marketandes.viewModel.ChatMapViewModel
import com.uniandes.marketandes.viewModel.ConnectivityViewModel
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.text.font.FontStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagChatMap(
    chatId: String,
    navController: NavHostController,
    chatMapViewModel: ChatMapViewModel = remember { ChatMapViewModel() }
) {
    val context = LocalContext.current

    // Observador de conectividad
    val connectivityViewModel: ConnectivityViewModel = viewModel()
    LaunchedEffect(Unit) {
        connectivityViewModel.checkConnectivity(context)
        connectivityViewModel.startNetworkCallback(context)
    }
    val isConnected by connectivityViewModel.isConnected

    // MapView y estado de GoogleMap
    val mapView = remember { MapView(context) }
    val googleMap = remember { mutableStateOf<GoogleMap?>(null) }
    val scope = rememberCoroutineScope()

    // Estados de ubicación y punto sugerido
    val userLocations by chatMapViewModel.userLocations
    val puntoSugerido by chatMapViewModel.puntoSugerido
    val distanciaPromedio by chatMapViewModel.distanciaPromedio

    // Carga inicial de ubicaciones
    LaunchedEffect(chatId) {
        chatMapViewModel.fetchUserLocations(chatId)
    }

    // Ciclo de vida de MapView
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

    Column(Modifier.fillMaxSize()) {
        // Banner de desconexión
        if (!isConnected) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFB00020))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sin conexión. Conéctate para ver el mapa.",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Encabezado con punto sugerido y mensaje de modo offline
        val textoDistancia = distanciaPromedio?.let { " (${it.toInt()} m promedio)" } ?: ""
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = puntoSugerido?.nombreUbicacion
                    ?.let { "Punto sugerido: $it$textoDistancia" }
                    ?: "Selecciona un punto de encuentro",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            if (!isConnected) {
                Text(
                    text = "Para visualizar el mapa requieres tener conexión",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp).fillMaxWidth(),
                    fontStyle = FontStyle.Italic
                )
            }
        }

        Box(Modifier.fillMaxSize()) {
            // Solo inicializamos el mapa cuando hay conexión
            if (isConnected) {
                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.fillMaxSize()
                ) { mv ->
                    mv.getMapAsync { map ->
                        googleMap.value = map
                        map.clear()

                        // Marcadores de usuarios
                        userLocations.forEach { (_, geo) ->
                            map.addMarker(
                                MarkerOptions()
                                    .position(LatLng(geo.latitude, geo.longitude))
                                    .title("Usuario")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                            )
                        }

                        // Marcadores de puntos de interés
                        chatMapViewModel.puntos.forEach { punto ->
                            val destacado = puntoSugerido?.latLng == punto.latLng
                            map.addMarker(
                                MarkerOptions()
                                    .position(punto.latLng)
                                    .title(
                                        if (destacado) "Sugerido: ${punto.nombreUbicacion}" else punto.nombreUbicacion
                                    )
                                    .icon(
                                        BitmapDescriptorFactory.defaultMarker(
                                            if (destacado) BitmapDescriptorFactory.HUE_GREEN else BitmapDescriptorFactory.HUE_RED
                                        )
                                    )
                            )
                        }

                        // Animar cámara y dibujar ruta si hay punto sugerido
                        puntoSugerido?.let { sugerido ->
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(sugerido.latLng, 16f))
                            val uid = FirebaseAuth.getInstance().currentUser?.uid
                            val origin = uid?.let { userLocations[it] }?.let { LatLng(it.latitude, it.longitude) }
                            origin?.let { start ->
                                scope.launch {
                                    try {
                                        val ruta = fetchRoute(start, sugerido.latLng, context.getString(R.string.api_key))
                                        map.addPolyline(
                                            PolylineOptions().addAll(ruta).width(10f)
                                        )
                                    } catch (e: Exception) {
                                        Log.w("PagChatMap", "Error ruta: $e")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private val httpClient = OkHttpClient()
suspend fun fetchRoute(origin: LatLng, destination: LatLng, apiKey: String): List<LatLng> =
    withContext(Dispatchers.IO) {
        val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${origin.latitude},${origin.longitude}" +
                "&destination=${destination.latitude},${destination.longitude}" +
                "&mode=walking" +
                "&key=$apiKey"
        val resp = httpClient.newCall(Request.Builder().url(url).build()).execute()
        val body = resp.body?.string() ?: throw Exception("Vacío")
        if (!resp.isSuccessful) throw Exception("Error Directions API")
        val json = JSONObject(body)
        val points = json.getJSONArray("routes")
            .takeIf { it.length() > 0 }
            ?.getJSONObject(0)
            ?.getJSONObject("overview_polyline")
            ?.getString("points")
            ?: ""
        return@withContext decodePolyline(points)
    }

fun decodePolyline(encoded: String): List<LatLng> {
    val poly = mutableListOf<LatLng>()
    var index = 0; var lat = 0; var lng = 0
    while (index < encoded.length) {
        var result = 0; var shift = 0; var b: Int
        do {
            b = encoded[index++].code - 63
            result = result or ((b and 0x1f) shl shift)
            shift += 5
        } while (b >= 0x20)
        lat += if (result and 1 != 0) (result shr 1).inv() else (result shr 1)
        result = 0; shift = 0
        do {
            b = encoded[index++].code - 63
            result = result or ((b and 0x1f) shl shift)
            shift += 5
        } while (b >= 0x20)
        lng += if (result and 1 != 0) (result shr 1).inv() else (result shr 1)
        poly.add(LatLng(lat / 1E5, lng / 1E5))
    }
    return poly
}
