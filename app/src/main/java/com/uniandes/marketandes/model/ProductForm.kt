package com.uniandes.marketandes.model

data class ProductForm(
    val name: String = "",
    val price: Int = 0 ,
    val imageURL: String = "",
    val category: String = "",
    val description: String = ""
)