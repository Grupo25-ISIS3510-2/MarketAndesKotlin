package com.uniandes.marketandes.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.uniandes.marketandes.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class FavoritosViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val _productosFavoritos = MutableStateFlow<List<Product>>(emptyList())
    val productosFavoritos: StateFlow<List<Product>> = _productosFavoritos

    init {
        cargarFavoritos()
    }

    fun guardarEnFavoritos(product: Product, onResult: (Boolean, String) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onResult(false, "Usuario no autenticado")
            return
        }

        viewModelScope.launch {
            try {
                val favoritoRef = db.collection("users")
                    .document(userId)
                    .collection("favoritos")
                    .document(product.id)

                val docSnapshot = favoritoRef.get().await()
                if (docSnapshot.exists()) {
                    onResult(true, "Ya está en favoritos")
                } else {
                    val fechaActual = System.currentTimeMillis()
                    val data = hashMapOf(
                        "id" to product.id,
                        "name" to product.name,
                        "price" to product.price,
                        "imageURL" to product.imageURL,
                        "category" to product.category,
                        "description" to product.description,
                        "sellerID" to product.sellerID,
                        "sellerRating" to product.sellerRating,
                        "fechaAgregado" to fechaActual
                    )

                    favoritoRef.set(data).await()
                    Log.d("FavoritosViewModel", "Producto guardado en favoritos: ${product.name}")
                    onResult(true, "Guardado en favoritos")
                    cargarFavoritos()
                }
            } catch (e: Exception) {
                Log.e("FavoritosViewModel", "Error al guardar en favoritos", e)
                onResult(false, "Error: ${e.message}")
            }
        }
    }

    private fun cargarFavoritos() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                Log.d("FavoritosViewModel", "Usuario no autenticado (userId es null)")
                return@launch
            }

            try {
                val favoritosSnapshot = db.collection("users")
                    .document(userId)
                    .collection("favoritos")
                    .get()
                    .await()

                val productos = mutableListOf<Product>()

                for (doc in favoritosSnapshot.documents) {
                    val id = doc.getString("id") ?: doc.id
                    val name = doc.getString("name")
                    val price = doc.getLong("price")?.toInt()
                    val imageURL = doc.getString("imageURL")
                    val category = doc.getString("category")
                    val description = doc.getString("description")
                    val sellerID = doc.getString("sellerID")
                    val sellerRating = doc.getLong("sellerRating")?.toInt()

                    if (name != null && price != null && imageURL != null &&
                        category != null && description != null && sellerID != null && sellerRating != null
                    ) {
                        val producto = Product(
                            id, name, price, imageURL, category, description, sellerID, sellerRating
                        )
                        productos.add(producto)
                    }
                }

                _productosFavoritos.value = productos

            } catch (e: Exception) {
                Log.e("FavoritosViewModel", "Error cargando favoritos: ${e.message}", e)
                _productosFavoritos.value = emptyList()
            }
        }
    }


    fun toggleFavorito(product: Product) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch

            val favoritoRef = db.collection("users")
                .document(userId)
                .collection("favoritos")
                .document(product.id)

            try {
                val snapshot = favoritoRef.get().await()
                if (snapshot.exists()) {
                    favoritoRef.delete().await()
                    Log.d("FavoritosViewModel", "Producto eliminado de favoritos: ${product.name}")
                } else {
                    val favoritoData = mapOf(
                        "id" to product.id,
                        "name" to product.name,
                        "price" to product.price,
                        "imageURL" to product.imageURL,
                        "category" to product.category,
                        "description" to product.description,
                        "sellerID" to product.sellerID,
                        "sellerRating" to product.sellerRating,
                        "fechaAgregado" to System.currentTimeMillis()
                    )
                    favoritoRef.set(favoritoData).await()
                    Log.d("FavoritosViewModel", "Producto agregado a favoritos: ${product.name}")
                }
                cargarFavoritos()
            } catch (e: Exception) {
                Log.e("FavoritosViewModel", "Error al hacer toggle de favorito: ${e.message}")
            }
        }
    }

    val categoriaFavorita: StateFlow<String?> = _productosFavoritos
        .map { favoritos ->
            favoritos
                .groupingBy { it.category }
                .eachCount()
                .maxByOrNull { it.value }
                ?.key
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun updateFavoritos(favoritos: List<Product>) {
        _productosFavoritos.value = favoritos
    }


}
