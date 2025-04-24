package com.uniandes.marketandes.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String,
    val name: String,
    val price: Int,
    val imageURL: String,
    val category: String,
    val description: String,
    val sellerID: String,
    val sellerRating: Int,
    val synced: Boolean = false // Esta columna indica si el producto se ha sincronizado

)