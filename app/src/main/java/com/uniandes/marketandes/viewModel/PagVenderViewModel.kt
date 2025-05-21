package com.uniandes.marketandes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.uniandes.marketandes.model.Product
import com.uniandes.marketandes.model.ProductForm
import com.uniandes.marketandes.repository.FirebaseService
import com.uniandes.marketandes.repository.ProductRepository
import com.uniandes.marketandes.util.NetworkConnectivityObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PagVenderViewModel(private val productRepository: ProductRepository,
                         private val connectivityObserver: NetworkConnectivityObserver
) : ViewModel() {
    private val firebaseService = FirebaseService()

    private val _formState = MutableStateFlow(ProductForm())
    val formState = _formState.asStateFlow()

    fun updateForm(newForm: ProductForm) {
        val priceAsInt = try {
            newForm.price.toInt()
        } catch (e: NumberFormatException) {
            0
        }
        _formState.value = newForm.copy(price = priceAsInt)
    }

    fun resetForm() {
        _formState.value = ProductForm()
    }

    private fun mapFormToProduct(form: ProductForm): Product {
        return Product(
            id = java.util.UUID.randomUUID().toString(),
            name = form.name,
            price = form.price,
            imageURL = form.imageURL,
            category = form.category,
            description = form.description,
            sellerID = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown",
            sellerRating = 5
        )
    }




    fun submitProduct(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val form = _formState.value
            val isConnected = connectivityObserver.isConnected.value

            if (isConnected) {
                firebaseService.uploadProductFromForm(
                    form,
                    onSuccess = { onResult("online") },
                    onFailure = { onResult("error") }
                )
            } else {
                val product = mapFormToProduct(form)
                productRepository.saveProductLocallyWhenOffline(product)
                onResult("offline")
            }
        }
    }






}
