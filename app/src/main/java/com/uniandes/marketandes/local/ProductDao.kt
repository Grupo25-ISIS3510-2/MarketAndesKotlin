package com.uniandes.marketandes.local

import androidx.room.*
import com.uniandes.marketandes.model.ProductEntity

@Dao
interface ProductDao
{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Query("DELETE FROM cached_products")
    suspend fun clearAllProducts()

    @Query("SELECT * FROM cached_products")
    suspend fun getAllCachedProducts(): List<ProductEntity>
}
