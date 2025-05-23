package com.uniandes.marketandes.model

data class ExchangeProduct(
    val id: String,
    val name: String,
    val productToExchangeFor: String,
    val imageURL: String,
    val category: String,
    val description: String,
    val sellerID: String,
    val sellerRating: Int,
    val pendingUpload: Boolean = false
)