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

    private val _productosFavoritos = MutableStateFlow<List<Product>>(emptyList())
    val productosFavoritos: StateFlow<List<Product>> = _productosFavoritos.asStateFlow()

    private val _mensajeVisible = MutableStateFlow<String?>(null)  // Estado para el mensaje visible
    val mensajeVisible: StateFlow<String?> = _mensajeVisible.asStateFlow()  // Publicar el mensaje
    private var productosOnlineCache: List<Product> = emptyList()  // Cache para los productos de Firestore

    init {
        observarFavoritos()
        sincronizarFavoritos()
    }

    // Observamos los favoritos en caché (base de datos local)
    private fun observarFavoritos() {
        viewModelScope.launch {
            dao.observeAllFavorites()
                .map { it.map { entity -> entity.toProduct() } }
                .collect { cachedList ->
                    if (_productosFavoritos.value.isEmpty() || productosOnlineCache.isEmpty()) {
                        _productosFavoritos.value = cachedList
                        Log.d("FavoritosViewModel", "🟢 Observando favoritos en caché: ${cachedList.size}")
                    }
                }
        }
    }

    // Sincronización de favoritos
    private fun sincronizarFavoritos() {
        viewModelScope.launch {
            connectivityObserver.observe()
                .collect { status ->
                    when (status) {
                        NetworkStatus.Available -> {
                            Log.d("FavoritosViewModel", "🟢 Conexión disponible, sincronizando favoritos con Firestore")
                            try {
                                val productosOnline = getFavoritesOnline()
                                productosOnlineCache = productosOnline  // Guardamos la última sincronización de Firestore

                                if (productosOnline.isNotEmpty()) {
                                    // Solo actualizamos si hay productos nuevos
                                    if (_productosFavoritos.value != productosOnline) {
                                        _productosFavoritos.value = productosOnline
                                        Log.d("FavoritosViewModel", "Productos sincronizados desde Firestore: ${productosOnline.size}")

                                        // Guardamos los productos sincronizados en la caché (Room)
                                        guardarFavoritosEnCache(productosOnline)
                                    }

                                    // Sincronizar los productos locales con Firestore
                                    val productosOffline = dao.getPendingFavorites()
                                    if (productosOffline.isNotEmpty()) {
                                        syncFavoritesToFirestore(productosOffline)
                                    }
                                } else {
                                    Log.d("FavoritosViewModel", "No se encontraron productos en Firestore.")
                                }
                            } catch (e: Exception) {
                                Log.e("FavoritosViewModel", "Error al sincronizar favoritos con Firestore", e)
                            }
                        }
                        NetworkStatus.Unavailable -> {
                            Log.d("FavoritosViewModel", "🔴 Sin conexión, mostrando favoritos desde caché")
                            _mensajeVisible.value = "Sin conexión. Mostrando favoritos desde caché."

                            // Mostrar los favoritos desde caché
                            if (productosOnlineCache.isNotEmpty()) {
                                _productosFavoritos.value = productosOnlineCache
                                Log.d("FavoritosViewModel", "🟢 Mostrando favoritos en caché: ${productosOnlineCache.size}")
                            } else {
                                // Si no hay caché, mostrar los favoritos desde Room
                                dao.observeAllFavorites()
                                    .map { it.map { entity -> entity.toProduct() } }
                                    .collect { cachedList ->
                                        _productosFavoritos.value = cachedList
                                        Log.d("FavoritosViewModel", "🟢 Observando favoritos en caché: ${cachedList.size}")
                                    }
                            }
                        }
                        else -> { /* No hay acción a tomar en estado desconocido */ }
                    }
                }
        }
    }

    // Método para guardar los productos sincronizados en caché (Room)
    private suspend fun guardarFavoritosEnCache(productos: List<Product>) {
        val productosEntity = productos.map { it.toEntity() }
        dao.insertFavorites(productosEntity)  // Guardar en la base de datos local (Room)
        Log.d("FavoritosViewModel", "🟢 Productos sincronizados con la caché: ${productosEntity.size}")
    }

    // Obtener favoritos de Firestore
    private suspend fun getFavoritesOnline(): List<Product> {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return emptyList()

        val snapshot = FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .collection("favoritos")
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            val id = doc.getString("id") ?: doc.id
            val name = doc.getString("name") ?: return@mapNotNull null
            val price = doc.getLong("price")?.toInt() ?: return@mapNotNull null
            val imageURL = doc.getString("imageURL") ?: ""
            val category = doc.getString("category") ?: ""
            val description = doc.getString("description") ?: ""
            val sellerID = doc.getString("sellerID") ?: ""
            val sellerRating = doc.getLong("sellerRating")?.toInt() ?: 0

            Product(id, name, price, imageURL, category, description, sellerID, sellerRating)
        }
    }

    // Sincronizar productos a Firestore
    private suspend fun syncFavoritesToFirestore(productosOffline: List<FavoriteEntity>) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        productosOffline.forEach { favoriteEntity ->
            val favoritoRef = FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .collection("favoritos")
                .document(favoriteEntity.id)

            val favoritoData = mapOf(
                "id" to favoriteEntity.id,
                "name" to favoriteEntity.name,
                "price" to favoriteEntity.price,
                "imageURL" to favoriteEntity.imageURL,
                "category" to favoriteEntity.category,
                "description" to favoriteEntity.description,
                "sellerID" to favoriteEntity.sellerID,
                "sellerRating" to favoriteEntity.sellerRating,
                "fechaAgregado" to System.currentTimeMillis()
            )

            try {
                favoritoRef.set(favoritoData).await()

                // Marcar el favorito como sincronizado en la base de datos local
                dao.markAsSynced(favoriteEntity.id)
                Log.d("FavoritosViewModel", "Producto sincronizado con Firestore: ${favoriteEntity.name}")
            } catch (e: Exception) {
                Log.e("FavoritosViewModel", "Error al sincronizar con Firestore: ${e.message}")
            }
        }
    }

    // Método para guardar el producto en Firestore o en Room (si no hay conexión)
    fun toggleFavorito(product: Product) {
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch

            val networkStatus = connectivityObserver.observe().first()

            if (networkStatus == NetworkStatus.Unavailable) {
                // No hay conexión a Internet, guardamos el producto en Room
                dao.insertFavorites(listOf(product.toEntity()))
                Log.d("FavoritoOffline", "Producto guardado en Room (sin conexión): ${product.name}")

                // Actualizar el mensaje visible
                _mensajeVisible.value = "Producto guardado en favoritos localmente. Se sincronizará cuando haya conexión."
            } else {
                // Hay conexión, intentamos agregarlo a Firestore
                val favoritoRef = FirebaseFirestore.getInstance().collection("users")
                    .document(userId)
                    .collection("favoritos")
                    .document(product.id)

                try {
                    val snapshot = favoritoRef.get().await()
                    if (snapshot.exists()) {
                        // Si el favorito ya existe, lo eliminamos de Firestore
                        favoritoRef.delete().await()
                        // También lo eliminamos de la base de datos local
                        dao.deleteFavorite(product.id)
                        Log.d("FavoritosViewModel", "Producto eliminado de favoritos: ${product.name}")
                        _mensajeVisible.value = "Producto eliminado de favoritos."
                    } else {
                        // Si no existe, lo agregamos a Firestore
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

                        // Actualizar el mensaje visible
                        _mensajeVisible.value = "Producto agregado a favoritos en línea."
                    }
                } catch (e: Exception) {
                    Log.e("FavoritosViewModel", "Error al hacer toggle de favorito en Firestore: ${e.message}")
                }
            }
        }
    }

    fun mensajeMostrado() {
        _mensajeVisible.value = null
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
