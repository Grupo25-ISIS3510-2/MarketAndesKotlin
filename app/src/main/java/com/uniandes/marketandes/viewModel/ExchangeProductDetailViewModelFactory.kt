package com.uniandes.marketandes.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.uniandes.marketandes.local.ExchangeProductDao

class ExchangeProductDetailViewModelFactory(
    private val dao: ExchangeProductDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExchangeProductDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExchangeProductDetailViewModel(dao, firestore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}