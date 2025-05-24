package com.uniandes.marketandes.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

class PerfilCheckViewModel : ViewModel() {

    private val _mostrarRecordatorio = MutableStateFlow(false)
    val mostrarRecordatorio = _mostrarRecordatorio.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    init {
        verificarUltimaActualizacion()
    }

    private fun verificarUltimaActualizacion() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _mostrarRecordatorio.value = true // No hay usuario → mostramos por defecto
            return
        }

        viewModelScope.launch {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    val lastUpdateTimestamp = document.getTimestamp("lastProfileUpdate")

                    if (lastUpdateTimestamp != null) {
                        val lastUpdateDate = lastUpdateTimestamp.toDate()
                        val diasTranscurridos = calcularDiasDesde(lastUpdateDate)

                        _mostrarRecordatorio.value = diasTranscurridos > 7
                        Log.d("PerfilCheck", "Días desde la última actualización: $diasTranscurridos")
                    } else {
                        _mostrarRecordatorio.value = true // No existe → mostrar recordatorio
                        Log.d("PerfilCheck", "No se encontró 'lastProfileUpdate'. Mostrando recordatorio.")
                    }
                }
                .addOnFailureListener { e ->
                    _mostrarRecordatorio.value = true // Error al consultar → mostrar por defecto
                    Log.e("PerfilCheck", "Error al consultar Firestore: ${e.message}")
                }
        }
    }

    private fun calcularDiasDesde(date: Date): Long {
        val now = Date()
        val diffInMillis = now.time - date.time
        return TimeUnit.MILLISECONDS.toDays(diffInMillis)
    }
}
