package com.uniandes.marketandes.viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.uniandes.marketandes.model.PuntoDeInteres
import kotlinx.coroutines.launch

class ChatMapViewModel : ViewModel() {

    val userLocations = mutableStateOf<Map<String, GeoPoint>>(emptyMap())
    val puntoSugerido = mutableStateOf<PuntoDeInteres?>(null)
    val distanciaPromedio = mutableStateOf<Double?>(null)

    private val firestore = FirebaseFirestore.getInstance()

    val puntos = listOf(
        PuntoDeInteres(LatLng(4.603100641251616, -74.06514973706602), "Entrada del ML", "..."),
        PuntoDeInteres(LatLng(4.601203630411922, -74.06556084750304), "El bobo", "..."),
        PuntoDeInteres(LatLng(4.602659914804456, -74.06631365426061), "Primer piso del Aulas", "...")
    )

    fun fetchUserLocations(chatId: String) {
        viewModelScope.launch {
            firestore.collection("chats").document(chatId).get()
                .addOnSuccessListener { doc ->
                    @Suppress("UNCHECKED_CAST")
                    val locs = doc.get("userLocations") as? Map<String, GeoPoint>
                    if (locs != null) {
                        userLocations.value = locs
                        calcularPuntoSugerido(locs)
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("PagChatMapViewModel", "Error al obtener ubicaciones: $e")
                }
        }
    }

    private fun calcularPuntoSugerido(locs: Map<String, GeoPoint>) {
        if (locs.size != 2) {
            puntoSugerido.value = null
            distanciaPromedio.value = null
            return
        }

        val coords = locs.values.map { LatLng(it.latitude, it.longitude) }

        val sugerido = puntos.minByOrNull { punto ->
            val distancias = coords.map { user -> distanciaEntre(user, punto.latLng) }
            distancias.sum()
        }

        puntoSugerido.value = sugerido

        sugerido?.let {
            val distancias = coords.map { user -> distanciaEntre(user, it.latLng) }
            val promedio = distancias.average()
            distanciaPromedio.value = promedio
        }
    }

    private fun distanciaEntre(p1: LatLng, p2: LatLng): Double {
        val res = FloatArray(1)
        android.location.Location.distanceBetween(
            p1.latitude, p1.longitude,
            p2.latitude, p2.longitude,
            res
        )
        return res[0].toDouble()
    }
}