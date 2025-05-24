package com.uniandes.marketandes.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.uniandes.marketandes.repository.ExchangeProductRepository
import com.uniandes.marketandes.util.NetworkConnectivityObserver
import com.uniandes.marketandes.viewmodel.PagExchangeProductViewModel

class PagExchangeProductViewModelFactory(
    private val ExchangeProductRepository: ExchangeProductRepository,
    private val connectivityObserver: NetworkConnectivityObserver
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PagExchangeProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PagExchangeProductViewModel(ExchangeProductRepository, connectivityObserver) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
