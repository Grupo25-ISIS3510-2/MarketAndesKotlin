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



    suspend fun deleteProductById(productId: String) {
        withContext(Dispatchers.IO) {
            try {
                // Eliminar en Firestore
                db.collection("products").document(productId).delete().await()

                // Eliminar también en Room
                productDao.deleteById(productId)

                Log.d("ProductRepository", "✅ Producto $productId eliminado correctamente.")
            } catch (e: Exception) {
                Log.e("ProductRepository", "❌ Error eliminando producto: ${e.message}")
                throw e // para que el ViewModel pueda capturar el error si es necesario
            }
        }
    }

    suspend fun addProduct(product: Product, online: Boolean) {
        if (online) {
            try {
                db.collection("products")
                    .document(product.id)
                    .set(product)
                    .await()

                productDao.insertProduct(product.toEntity(pendingUpload = false))
                Log.d("ProductRepository", "✅ Producto subido en línea y guardado localmente.")
            } catch (e: Exception) {
                Log.e("ProductRepository", "❌ Error subiendo producto online, guardando localmente como pendiente.")
                saveProductLocallyWhenOffline(product)
            }
        } else {
            Log.d("ProductRepository", "📴 Sin conexión. Guardando producto localmente como pendiente.")
            saveProductLocallyWhenOffline(product)
        }
    }



    suspend fun saveProductLocallyWhenOffline(product: Product) {
        withContext(Dispatchers.IO) {
            try {
                val entity = product.toEntity(pendingUpload = true)
                productDao.insertProduct(entity)
                Log.d("ProductRepository", "📦 Producto guardado localmente con pendingUpload=true")
            } catch (e: Exception) {
                Log.e("ProductRepository", "❌ Error guardando producto local: ${e.message}")
            }
        }
    }

    suspend fun uploadPendingProducts() {
        withContext(Dispatchers.IO) {
            val pendingProducts = productDao.getPendingUploadProducts().map { it.toDomain() }
            for (product in pendingProducts) {
                try {
                    // Intentar subir a Firebase
                    val success = uploadProductToServer(product)
                    if (success) {
                        // Marcar como subido (pendingUpload = false) en la base local
                        val updatedEntity = product.toEntity().copy(pendingUpload = false)
                        productDao.insertProduct(updatedEntity)
                    }
                } catch (e: Exception) {
                    // Si falla, continuar con el siguiente producto
                }
            }
        }
    }

    private suspend fun uploadProductToServer(product: Product): Boolean {
        return try {
            val db = FirebaseFirestore.getInstance()
            db.collection("products")
                .document(product.id)
                .set(product)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }







}