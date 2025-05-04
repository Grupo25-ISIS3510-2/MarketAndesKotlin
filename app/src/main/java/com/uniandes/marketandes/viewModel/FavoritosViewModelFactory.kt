package com.uniandes.marketandes.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.uniandes.marketandes.local.FavoriteDao
import com.uniandes.marketandes.util.ConnectivityObserver
import com.uniandes.marketandes.viewmodel.FavoritosViewModel

class FavoritosViewModelFactory(
    private val dao: FavoriteDao,
    private val connectivityObserver: ConnectivityObserver,

) : ViewModelProvider.Factory
{
    override fun <T : ViewModel> create(modelClass: Class<T>): T
    {
        if (modelClass.isAssignableFrom(FavoritosViewModel::class.java))
        {
            return FavoritosViewModel(dao, connectivityObserver) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}