package com.uniandes.marketandes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.uniandes.marketandes.model.ExchangeProduct
import com.uniandes.marketandes.model.ExchangeProductForm
import com.uniandes.marketandes.model.Product
import com.uniandes.marketandes.model.ProductForm
import com.uniandes.marketandes.repository.ExchangeProductRepository
import com.uniandes.marketandes.repository.FirebaseService
import com.uniandes.marketandes.repository.ProductRepository
import com.uniandes.marketandes.util.NetworkConnectivityObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class PagExchangeProductViewModel(
    private val ExchangeProductRepository: ExchangeProductRepository,
    private val connectivityObserver: NetworkConnectivityObserver
) : ViewModel() {
    private val firebaseService = FirebaseService()

    private val _formState = MutableStateFlow(ExchangeProductForm())
    val formState = _formState.asStateFlow()

    fun updateForm(newForm: ExchangeProductForm) {
        _formState.value = newForm
    }

    fun resetForm() {
        _formState.value = ExchangeProductForm()
    }

    private fun mapFormToExchangeProduct(form: ExchangeProductForm): ExchangeProduct {
        return ExchangeProduct(
            id = java.util.UUID.randomUUID().toString(),
            name = form.name,
            imageURL = form.imageURL,
            category = form.category,
            description = form.description,
            sellerID = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown",
            sellerRating = 5,
            productToExchangeFor = form.productToExchangeFor
        )
    }




    fun submitExchangeProduct(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val form = _formState.value
            val isConnected = connectivityObserver.isConnected.value

            if (isConnected) {
                val uploadResult = withContext(Dispatchers.IO) {
                    var result = "error"
                    val latch = kotlinx.coroutines.CompletableDeferred<Unit>()

                    firebaseService.uploadExchangeProductFromForm(
                        form,
                        onSuccess = {
                            result = "online"
                            latch.complete(Unit)
                        },
                        onFailure = {
                            result = "error"
                            latch.complete(Unit)
                        }
                    )

                    latch.await()
                    result
                }

                onResult(uploadResult)

            } else {
                withContext(Dispatchers.IO) {
                    val exchangeProduct = mapFormToExchangeProduct(form)
                    ExchangeProductRepository.saveExchangeProductLocallyWhenOffline(exchangeProduct)
                }
                onResult("offline")
            }
        }
    }








}
