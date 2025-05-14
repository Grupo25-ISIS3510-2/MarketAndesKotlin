package com.uniandes.marketandes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniandes.marketandes.model.ProductForm
import com.uniandes.marketandes.repository.FirebaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PagVenderViewModel : ViewModel() {
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


    fun submitProduct(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            firebaseService.uploadProductFromForm(
                _formState.value,
                onSuccess = { onResult(true) },
                onFailure = { onResult(false) }
            )
        }
    }

}
