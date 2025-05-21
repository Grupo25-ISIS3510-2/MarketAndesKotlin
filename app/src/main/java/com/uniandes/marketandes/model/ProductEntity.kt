package com.uniandes.marketandes.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val price: Int,
    val imageURL: String,
    val category: String,
    val description: String,
    val sellerID: String,
    val sellerRating: Int,
    val pendingUpload: Boolean = true
)
