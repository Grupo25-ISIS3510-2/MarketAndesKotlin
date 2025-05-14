package com.uniandes.marketandes.view.user_preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PerfilViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _perfilState = MutableStateFlow(PerfilUiState())
    val perfilState: StateFlow<PerfilUiState> = _perfilState

    fun onNombreChange(value: String) {
        _perfilState.value = _perfilState.value.copy(nombre = value)
    }

    fun onFechaNacimientoChange(value: String) {
        _perfilState.value = _perfilState.value.copy(fechaNacimiento = value)
    }

    fun onTelefonoChange(value: String) {
        _perfilState.value = _perfilState.value.copy(telefono = value)
    }

    fun onFacultadesChange(nuevasFacultades: List<String>) {
        _perfilState.value = _perfilState.value.copy(facultades = nuevasFacultades)
    }

    fun onInteresesChange(nuevosIntereses: List<String>) {
        _perfilState.value = _perfilState.value.copy(intereses = nuevosIntereses)
    }

    fun resetMensaje() {
        _perfilState.value = _perfilState.value.copy(mensaje = "")
    }

    // Cargar perfil del usuario
    fun cargarPerfil() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                // Usamos withContext(Dispatchers.IO) para que la carga de datos se realice en segundo plano
                val snapshot = withContext(Dispatchers.IO) {
                    db.collection("users").document(uid).get().await()
                }

                val nombre = snapshot.getString("nombre") ?: ""
                val fechaNacimiento = snapshot.getString("fechaNacimiento") ?: ""
                val telefono = snapshot.getString("telefono") ?: ""
                val facultades = snapshot.get("faculties") as? List<String> ?: emptyList()
                val intereses = snapshot.get("interests") as? List<String> ?: emptyList()

                _perfilState.value = PerfilUiState(
                    nombre = nombre,
                    fechaNacimiento = fechaNacimiento,
                    telefono = telefono,
                    facultades = facultades,
                    intereses = intereses
                )
            } catch (e: Exception) {
                _perfilState.value = _perfilState.value.copy(mensaje = "Error al cargar perfil: ${e.message}")
            }
        }
    }

    // Guardar perfil actualizado
    fun guardarPerfil() {
        val uid = auth.currentUser?.uid ?: return
        val nombre = _perfilState.value.nombre

        val perfil = mapOf(
            "nombre" to nombre,
            "fechaNacimiento" to _perfilState.value.fechaNacimiento,
            "telefono" to _perfilState.value.telefono,
            "faculties" to _perfilState.value.facultades,
            "interests" to _perfilState.value.intereses
        )

        viewModelScope.launch {
            try {
                // Realizamos la actualización de los datos en Firestore en segundo plano
                withContext(Dispatchers.IO) {
                    db.collection("users").document(uid).update(perfil).await()
                }

                // Actualizamos el perfil del usuario en FirebaseAuth
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(nombre)
                    .build()

                auth.currentUser?.updateProfile(profileUpdates)?.await()

                _perfilState.value = _perfilState.value.copy(mensaje = "Perfil actualizado correctamente")
            } catch (e: Exception) {
                _perfilState.value = _perfilState.value.copy(mensaje = "Error al guardar perfil: ${e.message}")
            }
        }
    }
}

data class PerfilUiState(
    val nombre: String = "",
    val fechaNacimiento: String = "",
    val telefono: String = "",
    val mensaje: String = "",
    val facultades: List<String> = emptyList(),
    val intereses: List<String> = emptyList()
)
