package com.uniandes.marketandes.repository
import com.google.firebase.firestore.FirebaseFirestore
import com.uniandes.marketandes.model.Product
import kotlinx.coroutines.tasks.await

class ProductRepository (private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    suspend fun getAllProducts(): List<Product> {
        return try {
            val snapshot = db.collection("products").get().await()
            snapshot.documents.mapNotNull { doc ->
                val id = doc.id
                val name = doc.getString("name") ?: "Sin nombre"
                val price = doc.getLong("price")?.toInt() ?: 0
                val imageURL = doc.getString("imageURL") ?: "Sin imagen"
                val category = doc.getString("category") ?: "Sin categoria"
                val description = doc.getString("description") ?: "Sin descripción"
                val sellerID = doc.getString("sellerID") ?: "Sin vendedor"
                val sellerRating = doc.getLong("sellerRating")?.toInt() ?: 0
                Product(id, name, price, imageURL, category, description, sellerID, sellerRating)
            }
        } catch (e: Exception) {
            emptyList()
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