package com.uniandes.marketandes.cache

import android.util.Log
import android.util.LruCache
import com.uniandes.marketandes.model.Product

object ProductCache {
    // Tamaño máximo de la cache (ajustable)
    private val cache = LruCache<String, Product>(10)

    fun getProduct(id: String): Product? {
        val product = cache.get(id)
        if (product != null) {
            Log.d("LRUCache", "✅ Producto obtenido de cache: ${product.name} (ID: $id)")
        } else {
            Log.d("LRUCache", "❌ Producto no encontrado en cache (ID: $id)")
        }
        return product
    }

    fun putProduct(product: Product) {
        cache.put(product.id, product)
        Log.d("LRUCache", "📦 Producto cacheado: ${product.name} (ID: ${product.id})")
    }

    fun removeProduct(productId: String) {
        cache.remove(productId)
        Log.d("LRUCache", "Producto eliminado de la cache (ID: $productId)")
    }

}
