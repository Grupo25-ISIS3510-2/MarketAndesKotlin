package com.uniandes.marketandes.view
import android.os.Bundle
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.LocationServices
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.uniandes.marketandes.model.PuntoDeInteres

@SuppressLint("MissingPermission")
@Composable
fun GetUserLocation(onLocationRetrieved: (LatLng) -> Unit) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    LaunchedEffect(Unit) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val latLng = LatLng(it.latitude, it.longitude)
                onLocationRetrieved(latLng)
            }
        }
    }
}

@Composable
fun PagStoreMaps(navController: NavHostController, destinoNombre: String? = null, destinoImagen: String? = null, destinoDireccion: String? = null) {


    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasPermission by remember { mutableStateOf(false) }
    var map by remember { mutableStateOf<com.google.android.gms.maps.GoogleMap?>(null) }


    RequestLocationPermission {
        hasPermission = true
    }

    if (hasPermission) {
        GetUserLocation { location ->
            userLocation = location
        }
    }

    val recommendedStore = listOf(
        PuntoDeInteres(
            latLng = LatLng(4.603728473666652, -74.06595420828403),
            nombreUbicacion = "Papeleria el Toro",
            imagenUrl = "https://lh5.googleusercontent.com/p/AF1QipOkuk68xYsDm9oKAiqJw7-asTuOEdCoJMddY104=w426-h240-k-no",
            direccion = "Cra. 1 #20a-20, Santa Fé, Bogotá"
        ),
        PuntoDeInteres(
            latLng = LatLng(4.603924729028438, -74.06583195459699),
            nombreUbicacion = "Cintel Tecnologia",
            imagenUrl = "https://lh5.googleusercontent.com/p/AF1QipOr32q12dZOj0S5IxEmzG3MYS6ZZETCbpj4jdYk=w408-h725-k-no",
            direccion = "#20A Carrera 1 #40, Bogotá"
        ),
        PuntoDeInteres(
            latLng = LatLng(4.603649834194452, -74.06696533067164),
            nombreUbicacion = "Plotter Art",
            imagenUrl = "https://lh5.googleusercontent.com/p/AF1QipOgTHXiL_zKcMkfUUO1Wy1i-xDdPm0l6ffVF3_R=w408-h544-k-no",
            direccion = "Cl. 20 #2A-58"
        ),
        PuntoDeInteres(
            latLng = LatLng(4.602428018244872, -74.06646040482757),
            nombreUbicacion = "QUALITY PLOTTER",
            imagenUrl = "https://streetviewpixels-pa.googleapis.com/v1/thumbnail?panoid=2Qvh2CPDjL0bMWooepEqbw&cb_client=search.gws-prod.gps&w=408&h=240&yaw=208.70874&pitch=0&thumbfov=100",
            direccion = "Cl. 19 Bis #1-29 LOCAL1"
        ),
        PuntoDeInteres(
            latLng = LatLng(4.602392713212415, -74.06641116603761),
            nombreUbicacion = "Centro de Copiado La Primera",
            imagenUrl = "https://lh3.googleusercontent.com/proxy/mmxSx3o1iRJNcNk0n-pFz817M2wb0UThLK92FQnjZnIF6vZzypGplhvmn-WJu8CCG8e9uDlzOi5HHLN4ZaRMAuPJ67gHtIRJvx-z119Ags1sCa-7oxw2jMCdyglcwfXNj2HgsSpUNm_NoOV0EjCdTEh1bbywH8VokVLtPA=s1360-w1360-h1020-rw",
            direccion = "Cl. 19 #1 - 15, Bogotá"
        ),
        PuntoDeInteres(
            latLng = LatLng(4.602325492832178, -74.0672307220746),
            nombreUbicacion = "Copy Andes E.U.",
            imagenUrl = "https://direccion.com.co/wp-content/uploads/2017/10/COPYANDES.jpg.webp",
            direccion = "Cra. 2a #18a-52, Bogotá"
        ),
        PuntoDeInteres(
            latLng = LatLng(4.602522568346729, -74.0666901367991),
            nombreUbicacion = "Print Copy",
            imagenUrl = "https://lh3.googleusercontent.com/proxy/MNQhrtYskUfTJUe4yF-zJvH821JxHxOxFWZYBxy5CzLmFI-bwzowyR964Z-6bhSIEAgAvZ7iUgt3gfMtoKGaTTolNLO2jNumDx201Dr_1wgw02Iqv-IogS9WXeiGtebSjd_il4puH3y9szAqt6Yiljkm7JS0ezSF8ICTqw=s1360-w1360-h1020-rw",
            direccion = "Cl. 19 #1-49, Bogotá"
        )
    )

    val decodedDestinoNombre = destinoNombre?.let { java.net.URLDecoder.decode(it, "UTF-8") }
    val puntoDestino = recommendedStore.find { it.nombreUbicacion == decodedDestinoNombre }


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

    AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize()) { mapView ->
        mapView.getMapAsync { googleMap ->
            map = googleMap

            recommendedStore.forEach { punto ->
                googleMap.addMarker(MarkerOptions().position(punto.latLng).title(punto.nombreUbicacion))
            }
            googleMap.setOnMarkerClickListener { clickedMarker ->
                val selectedPunto = recommendedStore.find { it.nombreUbicacion == clickedMarker.title }
                if (selectedPunto != null) {
                    showLocationInfo1(selectedPunto, navController)
                }
                true
            }

            userLocation?.let { location ->
                if (userLocation != null && puntoDestino != null) {
                    // Marcar destino
                    map?.addMarker(
                        MarkerOptions()
                            .position(puntoDestino.latLng)
                            .title("Destino: ${puntoDestino.nombreUbicacion}")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    )

                    // Mover la cámara para que quepan ambos puntos (ubicación + destino)
                    val bounds = com.google.android.gms.maps.model.LatLngBounds.builder()
                        .include(userLocation!!)
                        .include(puntoDestino.latLng)
                        .build()
                    map?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150))

                    // Línea entre ubicación y destino
                    map?.addPolyline(
                        com.google.android.gms.maps.model.PolylineOptions()
                            .add(userLocation, puntoDestino.latLng)
                            .color(android.graphics.Color.BLUE)
                            .width(10f)
                    )
                }

                googleMap.addMarker(MarkerOptions().position(location).title("Mi ubicación").icon(
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
            } ?: run {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(recommendedStore[0].latLng, 15f))
            }
        }
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = androidx.compose.ui.Alignment.End
    ) {
        FloatingActionButton(
            onClick = {
                map?.let { googleMap ->
                    userLocation?.let { location ->
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                    } ?: Toast.makeText(context, "Ubicación no disponible", Toast.LENGTH_SHORT).show()
                }
            },
            containerColor = Color(0xFFFFC107)
        ) {
            Text("📍", fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        FloatingActionButton(
            onClick = {
                map?.let { googleMap ->
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(recommendedStore[0].latLng, 15f))
                }
            },
            containerColor = MaterialTheme.colorScheme.secondary
        ) {
            Text("🏪", fontSize = 12.sp)
        }
    }

}

fun showLocationInfo1(punto: PuntoDeInteres, navController: NavHostController) {
    val nombreUbicacion = URLEncoder.encode(punto.nombreUbicacion, "UTF-8")
    val imagenUrl = URLEncoder.encode(punto.imagenUrl, "UTF-8")
    val direccion = URLEncoder.encode(punto.direccion, "UTF-8")

    navController.navigate("ubicaciondetail/$nombreUbicacion/$imagenUrl/$direccion")
}

@Composable
fun RequestLocationPermission(onPermissionGranted:  () -> Unit) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted()
        } else {
            Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
}