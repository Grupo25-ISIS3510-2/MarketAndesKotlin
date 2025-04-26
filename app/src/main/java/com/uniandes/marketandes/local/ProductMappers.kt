package com.uniandes.marketandes.local

import com.uniandes.marketandes.model.Product
import com.uniandes.marketandes.model.ProductEntity
import com.uniandes.marketandes.model.FavoriteEntity

// Conversión de Product a ProductEntity (para productos normales)
fun Product.toEntity(): ProductEntity = ProductEntity(
    id = id,
    name = name,
    price = price,
    imageURL = imageURL,
    category = category,
    description = description,
    sellerID = sellerID,
    sellerRating = sellerRating
)

// Conversión de ProductEntity a dominio
fun ProductEntity.toDomain(): Product = Product(
    id = id,
    name = name,
    price = price,
    imageURL = imageURL,
    category = category,
    description = description,
    sellerID = sellerID,
    sellerRating = sellerRating
)

// Conversión de Product a FavoriteEntity
fun Product.toFavoriteEntity(): FavoriteEntity = FavoriteEntity(
    id = id,
    name = name,
    price = price,
    imageURL = imageURL,
    category = category,
    description = description,
    sellerID = sellerID,
    sellerRating = sellerRating
)

// Conversión de FavoriteEntity a dominio
fun FavoriteEntity.toDomainF(): Product = Product(
    id = id,
    name = name,
    price = price,
    imageURL = imageURL,
    category = category,
    description = description,
    sellerID = sellerID,
    sellerRating = sellerRating
)
