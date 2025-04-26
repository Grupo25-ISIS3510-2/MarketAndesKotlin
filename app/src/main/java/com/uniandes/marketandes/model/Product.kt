package com.uniandes.marketandes.model

data class Product(
    val id:String,
    val name: String,
    val price: Int,
    val imageURL: String,
    val category: String,
    val description: String,
    val sellerID: String,
    val sellerRating: Int
)