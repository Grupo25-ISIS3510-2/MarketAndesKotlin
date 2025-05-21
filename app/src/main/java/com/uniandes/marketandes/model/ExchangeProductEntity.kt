package com.uniandes.marketandes.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_exchange_products")
data class ExchangeProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val productToExchangeFor: String,
    val imageURL: String,
    val category: String,
    val description: String,
    val sellerID: String,
    val sellerRating: Int
)