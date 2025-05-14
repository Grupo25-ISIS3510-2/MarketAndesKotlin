package com.uniandes.marketandes.view.preferences

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserPreferencesViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    var selectedFaculties by mutableStateOf<List<String>>(emptyList())
    var selectedInterests by mutableStateOf<List<String>>(emptyList())

    // Guardar las facultades seleccionadas
    fun saveFaculties(userId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                // Realizamos la operación en segundo plano
                withContext(Dispatchers.IO) {
                    val userRef = db.collection("users").document(userId)
                    userRef.set(mapOf("faculties" to selectedFaculties), SetOptions.merge()).await()
                }
                Log.d("Firestore", "Facultades guardadas exitosamente.")
                onSuccess()
            } catch (e: Exception) {
                Log.e("Firestore", "Error guardando facultades", e)
            }
        }
    }

    // Guardar los intereses seleccionados
    fun saveInterests(userId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                // Realizamos la operación en segundo plano
                withContext(Dispatchers.IO) {
                    val userRef = db.collection("users").document(userId)
                    userRef.set(mapOf("interests" to selectedInterests), SetOptions.merge()).await()
                }
                Log.d("Firestore", "Intereses guardados exitosamente.")
                onSuccess()
            } catch (e: Exception) {
                Log.e("Firestore", "Error guardando intereses", e)
            }
        }
    }

    // Cargar las facultades del usuario
    fun loadFaculties(uid: String, onResult: (List<String>) -> Unit) {
        viewModelScope.launch {
            try {
                // Realizamos la operación en segundo plano
                val faculties = withContext(Dispatchers.IO) {
                    val document = db.collection("users").document(uid).get().await()
                    document.get("faculties") as? List<String> ?: emptyList()
                }
                onResult(faculties)
            } catch (e: Exception) {
                Log.e("Firestore", "Error cargando facultades", e)
                onResult(emptyList())
            }
        }
    }

    // Cargar los intereses del usuario
    fun loadInterests(uid: String, onResult: (List<String>) -> Unit) {
        viewModelScope.launch {
            try {
                // Realizamos la operación en segundo plano
                val interests = withContext(Dispatchers.IO) {
                    val document = db.collection("users").document(uid).get().await()
                    document.get("interests") as? List<String> ?: emptyList()
                }
                onResult(interests)
            } catch (e: Exception) {
                Log.e("Firestore", "Error cargando intereses", e)
                onResult(emptyList())
            }
        }
    }
}
