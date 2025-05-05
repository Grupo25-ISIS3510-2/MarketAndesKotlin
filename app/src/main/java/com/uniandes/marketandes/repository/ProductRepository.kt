package com.uniandes.marketandes.repository

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.uniandes.marketandes.local.AppDatabase
import com.uniandes.marketandes.local.toDomain
import com.uniandes.marketandes.local.toEntity
import com.uniandes.marketandes.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProductRepository(
    context: Context,
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val productDao = AppDatabase.getDatabase(context).productDao()

    // Nueva función con soporte de red y caching
    suspend fun getAllProducts(online: Boolean): List<Product>
    {
        return withContext(Dispatchers.IO)
        {
            if (online)
            {
                try
                {
                    Log.d("ProductRepository", "🔵 Conectado: obteniendo productos desde Firestore")


                    val snapshot = db.collection("products").get().await()
                    val products = snapshot.documents.mapNotNull { doc ->
                        val id = doc.id
                        val name = doc.getString("name") ?: return@mapNotNull null
                        val price = doc.getLong("price")?.toInt() ?: return@mapNotNull null
                        val imageURL = doc.getString("imageURL") ?: ""
                        val category = doc.getString("category") ?: ""
                        val description = doc.getString("description") ?: ""
                        val sellerID = doc.getString("sellerID") ?: ""
                        val sellerRating = doc.getLong("sellerRating")?.toInt() ?: 0

                        Product(id, name, price, imageURL, category, description, sellerID, sellerRating)
                    }

                    Log.d("ProductRepository", "🟢 ${products.size} productos obtenidos. Guardando en caché.")

                    // Guardamos productos en cache
                    productDao.clearAllProducts()
                    productDao.insertProducts(products.map { it.toEntity() })

                    products
                }
                catch (e: Exception)
                {
                    Log.e("ProductRepository", "🟠 Error al obtener productos: ${e.message}")

                    val cached = productDao.getAllCachedProducts().map { it.toDomain() }
                    Log.d("CACHE", "📦 Productos en caché tras error: ${cached.size}")
                    cached

                    // Si falla la red, devolvemos los productos cacheados
                }
            }
            else
            {
                Log.d("ProductRepository", "🔴 Sin conexión: devolviendo productos desde caché")

                val cached = productDao.getAllCachedProducts().map { it.toDomain() }
                Log.d("CACHE", "📦 Productos en caché tras error: ${cached.size}")
                cached
                // Sin conexión: mostramos productos cacheados
            }
        }
    }

    suspend fun getProductById(productId: String): Product? {
        return try {
            val doc = db.collection("products").document(productId).get().await()
            if (doc.exists()) {
                val id = doc.id
                val name = doc.getString("name") ?: "Sin nombre"
                val price = doc.getLong("price")?.toInt() ?: 0
                val imageURL = doc.getString("imageURL") ?: "Sin imagen"
                val category = doc.getString("category") ?: "Sin categoría"
                val description = doc.getString("description") ?: "Sin descripción"
                val sellerID = doc.getString("sellerID") ?: "Sin vendedor"
                val sellerRating = doc.getLong("sellerRating")?.toInt() ?: 0
                Product(id, name, price, imageURL, category, description, sellerID, sellerRating)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}