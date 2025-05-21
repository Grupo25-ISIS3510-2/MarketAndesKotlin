package com.uniandes.marketandes.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.uniandes.marketandes.local.ExchangeProductDao
import com.uniandes.marketandes.model.ExchangeProduct
import com.uniandes.marketandes.model.ExchangeProductEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ExchangeProductRepository(
    private val exchangeProductDao: ExchangeProductDao,
    private val db: FirebaseFirestore
) {

    private val collectionName = "exchangeProducts"

    suspend fun getAllExchangeProducts(online: Boolean): List<ExchangeProduct> {
        return if (online) {
            try {
                val snapshot = db.collection(collectionName).get().await()
                val remoteProducts = snapshot.documents.map { doc ->
                    ExchangeProduct(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        productToExchangeFor = doc.getString("productToExchangeFor") ?: "",
                        imageURL = doc.getString("imageURL") ?: "",
                        category = doc.getString("category") ?: "",
                        description = doc.getString("description") ?: "",
                        sellerID = doc.getString("sellerID") ?: "",
                        sellerRating = doc.getLong("sellerRating")?.toInt() ?: 0
                    )
                }

                // Actualizar caché local
                withContext(Dispatchers.IO) {
                    exchangeProductDao.clearAllExchangeProducts()
                    exchangeProductDao.insertExchangeProducts(
                        remoteProducts.map { modelToEntity(it) }
                    )
                }
                remoteProducts
            } catch (e: Exception) {
                Log.e("ExchangeProdRepo", "Error cargando remotos, cargando caché", e)
                getFromCache()
            }
        } else {
            getFromCache()
        }
    }

    private suspend fun getFromCache(): List<ExchangeProduct> =
        withContext(Dispatchers.IO) {
            exchangeProductDao.getAllCachedExchangeProducts().map { entityToModel(it) }
        }

    suspend fun insertOrUpdateProduct(product: ExchangeProduct, online: Boolean): Boolean {
        // Guardar localmente siempre
        withContext(Dispatchers.IO) {
            exchangeProductDao.insertExchangeProducts(listOf(modelToEntity(product)))
        }

        if (online) {
            // Intentar subir a Firebase
            return try {
                val map = mapOf(
                    "name" to product.name,
                    "productToExchangeFor" to product.productToExchangeFor,
                    "imageURL" to product.imageURL,
                    "category" to product.category,
                    "description" to product.description,
                    "sellerID" to product.sellerID,
                    "sellerRating" to product.sellerRating
                )
                db.collection(collectionName).document(product.id).set(map).await()
                true
            } catch (e: Exception) {
                Log.w("ExchangeProdRepo", "Error subiendo producto a Firebase", e)
                false
            }
        } else {
            // Sin conexión: solo cache, retorno false para saber que no sincronizó
            return false
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
}