package com.uniandes.marketandes.local

import com.uniandes.marketandes.model.ExchangeProduct
import com.uniandes.marketandes.model.ExchangeProductEntity
import com.uniandes.marketandes.model.Product
import com.uniandes.marketandes.model.ProductEntity
import com.uniandes.marketandes.model.FavoriteEntity

fun Product.toEntity(): ProductEntity = ProductEntity(
    id = id,
    name = name,
    price = price,
    imageURL = imageURL,
    category = category,
    description = description,
    sellerID = sellerID,
    sellerRating = sellerRating,
    pendingUpload = pendingUpload
)

fun ExchangeProduct.toEntity(): ExchangeProductEntity = ExchangeProductEntity(
    id = id,
    name = name,
    productToExchangeFor = productToExchangeFor,
    imageURL = imageURL,
    category = category,
    description = description,
    sellerID = sellerID,
    sellerRating = sellerRating,
    pendingUpload = pendingUpload
)



fun ExchangeProduct.toEntity(pendingUpload: Boolean): ExchangeProductEntity = ExchangeProductEntity(
    id = id,
    name = name,
    productToExchangeFor = productToExchangeFor,
    imageURL = imageURL,
    category = category,
    description = description,
    sellerID = sellerID,
    sellerRating = sellerRating,
    pendingUpload = pendingUpload
)

fun ExchangeProductEntity.toDomain(): ExchangeProduct {
    return ExchangeProduct(
        id = this.id,
        name = this.name,
        productToExchangeFor = this.productToExchangeFor,
        imageURL = this.imageURL,
        category = this.category,
        description = this.description,
        sellerID = this.sellerID,
        sellerRating = this.sellerRating
    )
}



// Conversión de Product a ProductEntity (para productos normales)
fun Product.toEntity(pendingUpload: Boolean): ProductEntity = ProductEntity(
    id = id,
    name = name,
    price = price,
    imageURL = imageURL,
    category = category,
    description = description,
    sellerID = sellerID,
    sellerRating = sellerRating,
    pendingUpload = pendingUpload
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
    sellerRating = sellerRating,
    pendingUpload = pendingUpload
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
