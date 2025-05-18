package com.uniandes.marketandes.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.uniandes.marketandes.model.Product
import com.uniandes.marketandes.repository.ProductRepository
import com.uniandes.marketandes.util.ConnectivityObserver
import com.uniandes.marketandes.util.NetworkStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductViewModel(
    private val connectivityObserver: ConnectivityObserver,
    context: Context
) : ViewModel()
{

    private val repository = ProductRepository(context)

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _networkStatus = MutableStateFlow(NetworkStatus.Unavailable)
    val networkStatus = _networkStatus.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage

    init
    {
        viewModelScope.launch {
            loadProducts(isOnline=false)
        }
        observeNetwork()
    }

    private fun loadProducts(isOnline: Boolean)
    {
        viewModelScope.launch {
            val productos = repository.getAllProducts(online = isOnline)
            _products.value = productos
            if (!isOnline)
            {
                _toastMessage.value = "Sin conexión: productos cargados desde caché"
            }
        }
    }

    fun resetToast() {
        _toastMessage.value = null
    }

    private fun observeNetwork()
    {
        viewModelScope.launch {
            connectivityObserver.observe().collect {
                _networkStatus.value = it
                Log.d("NetworkStatus", "📶 Estado de red: $it")

                val isOnline = it == NetworkStatus.Available
                loadProducts(isOnline)
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            try {
                repository.deleteProductById(productId)
                _toastMessage.value = "Producto eliminado exitosamente"
                // Actualizar lista luego de borrar
                val isOnline = _networkStatus.value == NetworkStatus.Available
                loadProducts(isOnline)
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error eliminando producto", e)
                _toastMessage.value = "Error al eliminar el producto"
            }
        }
    }

    fun actualizarProducto(id: String, nombre: String, descripcion: String, precio: Double, categoria: String, imagen: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("products").document(id).update(
            mapOf(
                "name" to nombre,
                "description" to descripcion,
                "price" to precio,
                "category" to categoria,
                "imageURL" to imagen
            )
        ).addOnSuccessListener {
            Log.d("Firestore", "Producto actualizado")
        }.addOnFailureListener {
            Log.e("Firestore", "Error al actualizar", it)
        }
    }


}