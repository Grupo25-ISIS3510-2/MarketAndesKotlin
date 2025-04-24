package com.uniandes.marketandes.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.uniandes.marketandes.local.FavoriteDao
import com.uniandes.marketandes.model.FavoriteEntity
import com.uniandes.marketandes.model.Product
import com.uniandes.marketandes.util.ConnectivityObserver
import com.uniandes.marketandes.util.NetworkStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FavoritosViewModel(
    private val dao: FavoriteDao,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _productosFavoritos = MutableStateFlow<List<Product>>(emptyList())
    val productosFavoritos: StateFlow<List<Product>> = _productosFavoritos.asStateFlow()

    private val _mensajeToast = MutableStateFlow<String?>(null)
    val mensajeToast: StateFlow<String?> = _mensajeToast.asStateFlow()

    init {
        observarFavoritos()
    }

    private fun observarFavoritos() {
        viewModelScope.launch {
            dao.observeAllFavorites()
                .map { list -> list.map { it.toProduct() } }
                .collect { cachedList ->
                    _productosFavoritos.value = cachedList
                    Log.d("FavoritosViewModel", "🟢 Observando favoritos en caché: ${cachedList.size}")
                }
        }

        viewModelScope.launch {
            val isConnected = connectivityObserver.observe().first() == NetworkStatus.Available
            val userId = auth.currentUser?.uid ?: return@launch

            if (isConnected) {
                try {
                    val snapshot = db.collection("users")
                        .document(userId)
                        .collection("favoritos")
                        .get()
                        .await()

                    val productos = snapshot.documents.mapNotNull { doc ->
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
                            Product(id, name, price, imageURL, category, description, sellerID, sellerRating)
                        } else null
                    }

                    dao.clearAllFavorites()
                    dao.insertFavorites(productos.map { it.toEntity() })
                    _mensajeToast.value = "Favoritos actualizados desde Firestore."
                    Log.d("FavoritosViewModel", "🟢 Favoritos obtenidos de Firestore: ${productos.size}")

                } catch (e: Exception) {
                    Log.e("FavoritosViewModel", "Error al cargar favoritos online", e)
                }
            } else {
                _mensajeToast.value = "Sin conexión. Mostrando favoritos desde caché."
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
            } catch (e: Exception) {
                Log.e("FavoritosViewModel", "Error al hacer toggle de favorito: ${e.message}")
            }
        }
    }

    fun mensajeMostrado() {
        _mensajeToast.value = null
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

    private fun Product.toEntity() = FavoriteEntity(
        id, name, price, imageURL, category, description, sellerID, sellerRating
    )

    private fun FavoriteEntity.toProduct() = Product(
        id, name, price, imageURL, category, description, sellerID, sellerRating
    )
}