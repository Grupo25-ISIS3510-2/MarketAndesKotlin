package com.uniandes.marketandes.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.uniandes.marketandes.util.ConnectivityObserver

class ProductViewModelFactory(
    private val connectivityObserver: ConnectivityObserver,
    private val context: Context
) : ViewModelProvider.Factory
{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java))
        {
            return ProductViewModel(connectivityObserver, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}