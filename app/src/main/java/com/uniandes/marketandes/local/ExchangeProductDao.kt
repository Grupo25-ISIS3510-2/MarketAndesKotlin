package com.uniandes.marketandes.local

import androidx.room.*
import com.uniandes.marketandes.model.ExchangeProductEntity

@Dao
interface ExchangeProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExchangeProducts(products: List<ExchangeProductEntity>)

    @Query("DELETE FROM cached_exchange_products")
    suspend fun clearAllExchangeProducts()

    @Query("SELECT * FROM cached_exchange_products")
    suspend fun getAllCachedExchangeProducts(): List<ExchangeProductEntity>

    @Query("SELECT * FROM cached_exchange_products WHERE id = :productId LIMIT 1")
    suspend fun getExchangeProductById(productId: String): ExchangeProductEntity?

    @Query("DELETE FROM cached_exchange_products WHERE id = :productId")
    suspend fun deleteById(productId: String)
}