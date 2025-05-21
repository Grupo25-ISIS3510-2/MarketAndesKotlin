package com.uniandes.marketandes.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.uniandes.marketandes.local.AppDatabase
import com.uniandes.marketandes.model.ExchangeProduct
import com.uniandes.marketandes.repository.ExchangeProductRepository
import com.uniandes.marketandes.util.ConnectivityObserver
import com.uniandes.marketandes.util.NetworkStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExchangeProductViewModel(
    connectivityObserver: ConnectivityObserver,
    context: Context
) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val exchangeProductDao = AppDatabase.getDatabase(context).exchangeProductDao()
    private val repository = ExchangeProductRepository(exchangeProductDao, db)

    private val _exchangeProducts = MutableStateFlow<List<ExchangeProduct>>(emptyList())
    val exchangeProducts: StateFlow<List<ExchangeProduct>> = _exchangeProducts

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    init {
        observeNetwork(connectivityObserver)
        // Escuchar cambios remotos para mantener caché y UI sincronizados
        repository.listenForChanges { products ->
            _exchangeProducts.value = products
        }
    }

    private fun observeNetwork(connectivityObserver: ConnectivityObserver) {
        viewModelScope.launch {
            connectivityObserver.observe().collect { status ->
                _isConnected.value = (status == NetworkStatus.Available)
                if (_isConnected.value) {
                    // Cargar remotos y actualizar cache/UI
                    val products = repository.getAllExchangeProducts(online = true)
                    _exchangeProducts.value = products
                } else {
                    // Cargar solo cache local
                    val cached = repository.getAllExchangeProducts(online = false)
                    _exchangeProducts.value = cached
                }
            }
        }
    }

    fun insertOrUpdateProduct(product: ExchangeProduct) {
        viewModelScope.launch {
            repository.insertOrUpdateProduct(product, _isConnected.value)
        }
    }
}