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



}
