package com.uniandes.marketandes.local

import androidx.room.*
import com.uniandes.marketandes.model.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM cached_favorites")
    fun observeAllFavorites(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorites(favorites: List<FavoriteEntity>)

    @Query("DELETE FROM cached_favorites")
    suspend fun clearAllFavorites()

    @Query("SELECT * FROM cached_favorites")
    fun observeAllCachedFavorites(): Flow<List<FavoriteEntity>>

    @Query("DELETE FROM cached_favorites WHERE id = :productId")
    suspend fun deleteFavorite(productId: String)

    @Query("SELECT * FROM cached_favorites WHERE synced = 0") // Asegúrate de tener una columna `synced`
    suspend fun getPendingFavorites(): List<FavoriteEntity>

    @Query("UPDATE cached_favorites SET synced = 1 WHERE id = :productoId")
    suspend fun markAsSynced(productoId: String)

}
