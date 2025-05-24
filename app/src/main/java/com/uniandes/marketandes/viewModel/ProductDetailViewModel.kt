package com.uniandes.marketandes.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.uniandes.marketandes.cache.ProductCache
import com.uniandes.marketandes.model.Product
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProductDetailViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _product = MutableLiveData<Product?>()
    val product: LiveData<Product?> = _product

    private val _sellerName = MutableLiveData<String>()
    val sellerName: LiveData<String> = _sellerName

    fun loadProduct(productId: String) {
        viewModelScope.launch {
            // Intentar cargar producto desde cache en memoria primero
            val cachedProduct = ProductCache.getProduct(productId)
            if (cachedProduct != null) {
                _product.value = cachedProduct
                loadSellerName(cachedProduct.sellerID)
                Log.d("ViewModel", "Producto cargado desde cache: $productId")
                return@launch
            }

            // Si no está en cache, cargar desde Firestore
            try {
                Log.d("ViewModel", "Cargando producto de Firestore $productId")

                val doc = db.collection("products").document(productId).get().await()

                if (doc.exists()) {
                    val producto = Product(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        price = doc.getLong("price")?.toInt() ?: 0,
                        imageURL = doc.getString("imageURL") ?: "",
                        category = doc.getString("category") ?: "",
                        description = doc.getString("description") ?: "Sin descripción",
                        sellerID = doc.getString("sellerID") ?: "",
                        sellerRating = doc.getLong("sellerRating")?.toInt() ?: 0,
                    )

                    // Guardar en cache para próximas cargas
                    ProductCache.putProduct(producto)

                    _product.value = producto
                    updateLastVisitIfFavorited(productId)
                    loadSellerName(producto.sellerID)
                    Log.d("ViewModel", "UID del vendedor: ${producto.sellerID}")

                } else {
                    Log.d("ViewModel", "Producto no encontrado en Firestore")
                    _product.value = null
                }

            } catch (e: Exception) {
                Log.e("ViewModel", "Error al cargar producto", e)
                _product.value = null
            }
        }
    }

    private suspend fun updateLastVisitIfFavorited(productId: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return

        val favRef = db.collection("users")
            .document(user.uid)
            .collection("favoritos")
            .document(productId)

        try {
            val favDoc = favRef.get().await()
            if (favDoc.exists()) {
                val updateMap = mapOf("fechaUltimaVisita" to System.currentTimeMillis())
                favRef.set(updateMap, SetOptions.merge()).await()
                Log.d("ViewModel", "Fecha de última visita actualizada para favorito")
            } else {
                Log.d("ViewModel", "El producto no está en favoritos, no se actualiza fecha")
            }
        } catch (e: Exception) {
            Log.e("ViewModel", "Error al actualizar fechaUltimaVisita", e)
        }
    }

    private fun loadSellerName(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                Log.d("ViewModel", "Documento del vendedor: ${document.data}") // Depuración

                val name = when {
                    document.contains("nombre") -> document.getString("nombre")
                    document.contains("fullName") -> document.getString("fullName")
                    else -> null
                } ?: "Vendedor desconocido"

                Log.d("ViewModel", "Nombre del vendedor: $name")
                _sellerName.value = name
            }
            .addOnFailureListener { e ->
                Log.e("ViewModel", "Error al cargar el nombre del vendedor", e)
                _sellerName.value = "Error al cargar nombre"
            }
    }
}
