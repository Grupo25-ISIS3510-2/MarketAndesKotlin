package com.uniandes.marketandes.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.uniandes.marketandes.local.AppDatabase
import com.uniandes.marketandes.model.ExchangeProduct
import com.uniandes.marketandes.model.Product
import com.uniandes.marketandes.repository.ExchangeProductRepository
import com.uniandes.marketandes.util.ConnectivityObserver
import com.uniandes.marketandes.util.NetworkStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExchangeProductViewModel(
    private val connectivityObserver: ConnectivityObserver,
    context: Context
) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val repository = ExchangeProductRepository(context, db)

    private val _exchangeProducts = MutableStateFlow<List<ExchangeProduct>>(emptyList())
    val exchangeProducts: StateFlow<List<ExchangeProduct>> = _exchangeProducts

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _networkStatus = MutableStateFlow(NetworkStatus.Unavailable)
    val networkStatus = _networkStatus.asStateFlow()


    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage


    init
    {
        viewModelScope.launch {
            loadExchangeProducts(isOnline=false)
        }
        observeNetwork()
    }


    private fun loadExchangeProducts(isOnline: Boolean)
    {
        viewModelScope.launch {
            val productos = repository.getAllExchangeProducts(online = isOnline)
            _exchangeProducts.value = productos
            if (!isOnline)
            {
                _toastMessage.value = "Sin conexión: productos cargados desde caché"
            }
        }
    }

    fun addExchangeProduct(exchangeProduct: ExchangeProduct) {
        viewModelScope.launch {
            try {
                val isOnline = _networkStatus.value == NetworkStatus.Available
                repository.addExchangeProduct(exchangeProduct, online = isOnline)
                _toastMessage.value = "Producto agregado"
                loadExchangeProducts(isOnline)
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error agregando producto", e)
                _toastMessage.value = "Error al agregar producto"
            }
        }
    }

    private fun uploadPendingExchangeProducts() {
        viewModelScope.launch {
            try {
                repository.uploadPendingExchangeProducts()
                // Puedes actualizar la lista después de subir productos
                loadExchangeProducts(true)
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error subiendo productos pendientes", e)
            }
        }


    }

    private fun observeNetwork() {
        viewModelScope.launch {
            connectivityObserver.observe().collect {
                _networkStatus.value = it
                Log.d("NetworkStatus", "📶 Estado de red: $it")

                val isOnline = it == NetworkStatus.Available
                loadExchangeProducts(isOnline)

                if (isOnline) {
                    // Subir productos pendientes cuando haya conexión
                    uploadPendingExchangeProducts()
                }
            }
        }

    }

    fun deleteExchangeProduct(productId: String) {
        viewModelScope.launch {
            try {
                repository.deleteExchangeProductById(productId)
                _toastMessage.value = "Producto eliminado exitosamente"
                // Actualizar lista luego de borrar
                val isOnline = _networkStatus.value == NetworkStatus.Available
                loadExchangeProducts(isOnline)
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error eliminando producto", e)
                _toastMessage.value = "Error al eliminar el producto"
            }
        }
    }



}