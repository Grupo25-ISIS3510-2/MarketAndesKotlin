package com.uniandes.marketandes.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.uniandes.marketandes.util.ConnectivityObserver

class ExchangeProductViewModelFactory(
    private val connectivityObserver: ConnectivityObserver,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExchangeProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExchangeProductViewModel(connectivityObserver, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}