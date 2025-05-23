package com.uniandes.marketandes.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.uniandes.marketandes.model.ProductEntity
import com.uniandes.marketandes.model.FavoriteEntity
import com.uniandes.marketandes.model.MessageEntity
import com.uniandes.marketandes.model.ExchangeProductEntity

@Database(
    entities = [ProductEntity::class, FavoriteEntity::class, MessageEntity::class, ExchangeProductEntity::class],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun messageDao(): MessageDao
    abstract fun exchangeProductDao(): ExchangeProductDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "marketandes_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }


    }
}