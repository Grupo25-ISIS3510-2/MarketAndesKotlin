package com.uniandes.marketandes.repository

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.uniandes.marketandes.local.AppDatabase
import com.uniandes.marketandes.local.ExchangeProductDao
import com.uniandes.marketandes.local.toEntity
import com.uniandes.marketandes.model.ExchangeProduct
import com.uniandes.marketandes.model.ExchangeProductEntity
import com.uniandes.marketandes.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.uniandes.marketandes.local.toDomain
import kotlinx.coroutines.processNextEventInCurrentThread


class ExchangeProductRepository(
    context: Context,
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val exchangeProductDao = AppDatabase.getDatabase(context).exchangeProductDao()

    private val collectionName = "exchangeProducts"

    suspend fun getAllExchangeProducts(online: Boolean): List<ExchangeProduct> {
        return withContext(Dispatchers.IO)
        {
            if (online) {
                try {
                    val snapshot = db.collection("exchangeProducts").get().await()
                    val remoteProducts = snapshot.documents.mapNotNull { doc ->
                        val id = doc.id
                        val name = doc.getString("name") ?: return@mapNotNull null
                        val productToExchangeFor = doc.getString("productToExchangeFor") ?: ""
                        val imageURL = doc.getString("imageURL") ?: ""
                        val category = doc.getString("category") ?: ""
                        val description = doc.getString("description") ?: ""
                        val sellerID = doc.getString("sellerID") ?: ""
                        val sellerRating = doc.getLong("sellerRating")?.toInt() ?: 0

                        ExchangeProduct(
                            id,
                            name,
                            productToExchangeFor,
                            imageURL,
                            category,
                            description,
                            sellerID,
                            sellerRating
                        )

                    }
                    // Guardamos productos en cache
                    exchangeProductDao.clearAllExchangeProducts()
                    exchangeProductDao.insertExchangeProducts(remoteProducts.map { it.toEntity() })

                    remoteProducts
                } catch (e: Exception) {
                    Log.e("ExchangeProdRepo", "Error cargando remotos, cargando caché", e)

                    val cached = exchangeProductDao.getAllCachedExchangeProducts().map { it.toDomain() }
                    Log.d("CACHE", "📦 Productos intercambio en caché tras error: ${cached.size}")
                    cached
                }

            } else {
                Log.d(
                    "ExchangeProductRepository",
                    "🔴 Sin conexión: devolviendo productos desde caché"
                )

                val cached = exchangeProductDao.getAllCachedExchangeProducts().map { it.toDomain() }
                Log.d("CACHE", "📦 Productos intercambio en caché tras error: ${cached.size}")
                cached
                // Sin conexión: mostramos productos cacheados
            }
        }
    }


    suspend fun addExchangeProduct(exchangeProduct: ExchangeProduct, online: Boolean) {
        if (online) {
            try {
                db.collection("exchangeProducts")
                    .document(exchangeProduct.id)
                    .set(exchangeProduct)
                    .await()

                exchangeProductDao.insertExchangeProduct(exchangeProduct.toEntity(pendingUpload = false))
                Log.d("ExchangeProductRepository", "✅ Producto subido en línea y guardado localmente.")
            } catch (e: Exception) {
                Log.e("ExchangeProductRepository", "❌ Error subiendo producto online, guardando localmente como pendiente.")
                saveExchangeProductLocallyWhenOffline(exchangeProduct)
            }
        } else {
            Log.d("ProductRepository", "📴 Sin conexión. Guardando producto localmente como pendiente.")
            saveExchangeProductLocallyWhenOffline(exchangeProduct)
        }
    }

    // Escuchar cambios remotos para actualizar caché (como en Chat)
    fun listenForChanges(onChange: (List<ExchangeProduct>) -> Unit) {
        db.collection(collectionName)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("ExchangeProdRepo", "Error escuchando cambios", error)
                    return@addSnapshotListener
                }
                val products = snapshot?.documents.orEmpty().map {
                    ExchangeProduct(
                        id = it.id,
                        name = it.getString("name") ?: "",
                        productToExchangeFor = it.getString("productToExchangeFor") ?: "",
                        imageURL = it.getString("imageURL") ?: "",
                        category = it.getString("category") ?: "",
                        description = it.getString("description") ?: "",
                        sellerID = it.getString("sellerID") ?: "",
                        sellerRating = it.getLong("sellerRating")?.toInt() ?: 0
                    )
                }
                // Actualizar caché local en background
                GlobalScope.launch(Dispatchers.IO) {
                    exchangeProductDao.clearAllExchangeProducts()
                    exchangeProductDao.insertExchangeProducts(products.map { modelToEntity(it) })
                }
                onChange(products)
            }
    }

    private fun modelToEntity(p: ExchangeProduct) = ExchangeProductEntity(
        id = p.id,
        name = p.name,
        productToExchangeFor = p.productToExchangeFor,
        imageURL = p.imageURL,
        category = p.category,
        description = p.description,
        sellerID = p.sellerID,
        sellerRating = p.sellerRating
    )

    private fun entityToModel(e: ExchangeProductEntity) = ExchangeProduct(
        id = e.id,
        name = e.name,
        productToExchangeFor = e.productToExchangeFor,
        imageURL = e.imageURL,
        category = e.category,
        description = e.description,
        sellerID = e.sellerID,
        sellerRating = e.sellerRating
    )

    suspend fun deleteExchangeProductById(productId: String) {
        withContext(Dispatchers.IO) {
            try {
                // Eliminar en Firestore
                db.collection("exchangeProducts").document(productId).delete().await()

                // Eliminar también en Room
                exchangeProductDao.deleteById(productId)

                Log.d("ExchangeProductRepository", "✅ Producto $productId eliminado correctamente.")
            } catch (e: Exception) {
                Log.e("ExchangeProductRepository", "❌ Error eliminando producto: ${e.message}")
                throw e // para que el ViewModel pueda capturar el error si es necesario
            }
        }
    }

    suspend fun saveExchangeProductLocallyWhenOffline(exchangeProduct: ExchangeProduct) {
        withContext(Dispatchers.IO) {
            try {
                val entity = exchangeProduct.toEntity(pendingUpload = true)
                exchangeProductDao.insertExchangeProduct(entity)
                Log.d("ProductRepository", "📦 Producto guardado localmente con pendingUpload=true")
            } catch (e: Exception) {
                Log.e("ProductRepository", "❌ Error guardando producto local: ${e.message}")
            }
        }
    }



    suspend fun uploadPendingExchangeProducts() {
        withContext(Dispatchers.IO) {
            val pendingExchangeProducts = exchangeProductDao.getPendingUploadExchangeProducts().map { it.toDomain() }
            for (exchangeProduct in pendingExchangeProducts) {
                try {
                    // Intentar subir a Firebase
                    val success = uploadProductToServer(exchangeProduct)
                    if (success) {
                        // Marcar como subido (pendingUpload = false) en la base local
                        val updatedEntity = exchangeProduct.toEntity(pendingUpload = false)
                        exchangeProductDao.insertExchangeProduct(updatedEntity)
                    }
                } catch (e: Exception) {
                    // Si falla, continuar con el siguiente producto
                }
            }
        }
    }

    private suspend fun uploadProductToServer(exchangeProduct: ExchangeProduct): Boolean {
        return try {

            db.collection("exchangeProducts")
                .document(exchangeProduct.id)
                .set(exchangeProduct)
                .await()
            Log.d("ExchangeProductRepository", "✅ Producto subido exitosamente a Firebase")
            true
        } catch (e: Exception) {
            Log.e("ExchangeProductRepository", "❌ Error subiendo producto a Firebase: ${e.message}")
            false
        }
    }
}