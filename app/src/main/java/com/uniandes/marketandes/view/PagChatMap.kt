package com.uniandes.marketandes.view

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.uniandes.marketandes.R
import com.uniandes.marketandes.viewModel.PagChatMapViewModel
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PagChatMap(
    chatId: String,
    navController: NavHostController,
    viewModel: PagChatMapViewModel = remember { PagChatMapViewModel() }
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val googleMap = remember { mutableStateOf<GoogleMap?>(null) }
    val scope = rememberCoroutineScope()

    val userLocations by viewModel.userLocations
    val puntoSugerido by viewModel.puntoSugerido
    val distanciaPromedio by viewModel.distanciaPromedio

    LaunchedEffect(chatId) {
        viewModel.fetchUserLocations(chatId)
    }

    LaunchedEffect(mapView) {
        mapView.onCreate(Bundle())
        mapView.onResume()
    }

    DisposableEffect(Unit) {
        onDispose {
            mapView.onPause()
            mapView.onStop()
        }
    }

    LaunchedEffect(userLocations) {
        mapView.getMapAsync { map ->
            googleMap.value = map
            map.clear()

            userLocations.forEach { (_, geoPoint) ->
                val pos = LatLng(geoPoint.latitude, geoPoint.longitude)
                map.addMarker(
                    MarkerOptions()
                        .position(pos)
                        .title("Usuario")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                )
            }

            viewModel.puntos.forEach { punto ->
                val isSugerido = puntoSugerido?.latLng == punto.latLng
                map.addMarker(
                    MarkerOptions()
                        .position(punto.latLng)
                        .title(
                            if (isSugerido)
                                "Punto de encuentro sugerido: ${punto.nombreUbicacion}"
                            else
                                punto.nombreUbicacion
                        )
                        .icon(
                            if (isSugerido)
                                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                            else
                                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        )
                )
            }

            puntoSugerido?.let {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(it.latLng, 16f))

                val currentUser = FirebaseAuth.getInstance().currentUser?.uid
                val userPoint = currentUser?.let { uid ->
                    userLocations[uid]?.let { LatLng(it.latitude, it.longitude) }
                }

                userPoint?.let {
                    scope.launch {
                        try {
                            val ruta = fetchRoute(userPoint, puntoSugerido!!.latLng, context.getString(R.string.api_key))
                            map.addPolyline(
                                PolylineOptions()
                                    .addAll(ruta)
                                    .color(Color.BLUE)
                                    .width(10f)
                            )
                        } catch (e: Exception) {
                            Log.w("PagChatMap", "Error obteniendo ruta: $e")
                        }
                    }
                }
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        val textoDistancia = distanciaPromedio?.let {
            val metros = it.toInt()
            " ($metros m promedio)"
        } ?: ""

        Text(
            text = puntoSugerido?.nombreUbicacion?.let { "Punto sugerido: $it$textoDistancia" }
                ?: "Selecciona un punto de encuentro",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(Modifier.fillMaxSize()) {
            AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
        }
    }
}

private val httpClient = OkHttpClient()

suspend fun fetchRoute(origin: LatLng, destination: LatLng, apiKey: String): List<LatLng> {
    return withContext(Dispatchers.IO) {
        val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${origin.latitude},${origin.longitude}" +
                "&destination=${destination.latitude},${destination.longitude}" +
                "&mode=walking" +
                "&key=$apiKey"

        val request = Request.Builder().url(url).build()
        val response = httpClient.newCall(request).execute()

        val bodyString = response.body?.string()
        if (bodyString == null) throw Exception("Cuerpo vacío")
        if (!response.isSuccessful) throw Exception("Error en solicitud Directions API")

        parseRoute(bodyString)
    }
}

fun parseRoute(jsonString: String): List<LatLng> {
    val json = JSONObject(jsonString)
    val routes = json.getJSONArray("routes")
    if (routes.length() == 0) return emptyList()

    val overviewPolyline = routes.getJSONObject(0).getJSONObject("overview_polyline")
    val encodedPoints = overviewPolyline.getString("points")

    return decodePolyline(encodedPoints)
}

fun decodePolyline(encoded: String): List<LatLng> {
    val poly = ArrayList<LatLng>()
    var index = 0
    var lat = 0
    var lng = 0

    while (index < encoded.length) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = encoded[index++].code - 63
            result = result or ((b and 0x1f) shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlat = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
        lat += dlat

        shift = 0
        result = 0
        do {
            b = encoded[index++].code - 63
            result = result or ((b and 0x1f) shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlng = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
        lng += dlng

        poly.add(LatLng(lat / 1E5, lng / 1E5))
    }

    return poly
}