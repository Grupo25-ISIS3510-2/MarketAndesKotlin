package com.uniandes.marketandes.local

import androidx.room.*
import com.uniandes.marketandes.model.ExchangeProductEntity
import com.uniandes.marketandes.model.ProductEntity

@Dao
interface ExchangeProductDao
{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExchangeProduct(exchangeProduct: ExchangeProductEntity)

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

    @Query("SELECT * FROM cached_exchange_products WHERE pendingUpload = 1")
    suspend fun getPendingUploadExchangeProducts(): List<ExchangeProductEntity>

    @Update
    suspend fun updateExchangeProduct(exchangeProduct: ExchangeProductEntity)

    @Query("UPDATE cached_products SET pendingUpload = :isUploaded WHERE id = :productId")
    suspend fun updatePendingUploadStatus(productId: String, isUploaded: Boolean)


}