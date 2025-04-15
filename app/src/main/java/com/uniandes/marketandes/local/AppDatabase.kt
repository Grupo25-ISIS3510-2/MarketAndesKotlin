package com.uniandes.marketandes.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.uniandes.marketandes.model.ProductEntity
import com.uniandes.marketandes.model.FavoriteEntity

@Database(entities = [ProductEntity::class, FavoriteEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase()
{
    abstract fun productDao(): ProductDao
    abstract fun favoriteDao(): FavoriteDao


    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "marketandes_db"
                )
                    .fallbackToDestructiveMigration() // 🔥 añade esta línea
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}