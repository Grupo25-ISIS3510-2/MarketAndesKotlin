
package com.uniandes.marketandes.model

data class ExchangeProductForm(
    val id: String = "",
    val name: String = "",
    val productToExchangeFor: String = "",
    val imageURL: String = "",
    val category: String = "",
    val description: String = ""
)
