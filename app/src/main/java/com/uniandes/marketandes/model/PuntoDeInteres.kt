package com.uniandes.marketandes.model

data class PuntoDeInteres(
    val latLng: com.google.android.gms.maps.model.LatLng,
    val nombreUbicacion: String,
    val imagenUrl: String
)