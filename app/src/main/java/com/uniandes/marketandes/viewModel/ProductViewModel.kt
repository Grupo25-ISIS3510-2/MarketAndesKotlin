package com.uniandes.marketandes.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniandes.marketandes.model.Product
import com.uniandes.marketandes.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class ProductViewModel : ViewModel() {

    private val repository = ProductRepository()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _products.value = repository.getAllProducts()
        }
    }
}