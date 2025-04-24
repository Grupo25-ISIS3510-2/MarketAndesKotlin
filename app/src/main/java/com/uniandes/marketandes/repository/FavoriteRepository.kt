package com.uniandes.marketandes.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.uniandes.marketandes.local.FavoriteDao
import com.uniandes.marketandes.local.toDomainF
import com.uniandes.marketandes.local.toFavoriteEntity
import com.uniandes.marketandes.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FavoriteRepository(
    private val dao: FavoriteDao,
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    suspend fun getFavoritesOnline(): List<Product> {
        val userId = auth.currentUser?.uid ?: return emptyList()

        val snapshot = db.collection("users")
            .document(userId)
            .collection("favoritos")
            .get()
            .await()

        val favoritos = snapshot.documents.mapNotNull { doc ->
            val id = doc.getString("id") ?: doc.id
            val name = doc.getString("name") ?: return@mapNotNull null
            val price = doc.getLong("price")?.toInt() ?: return@mapNotNull null
            val imageURL = doc.getString("imageURL") ?: ""
            val category = doc.getString("category") ?: ""
            val description = doc.getString("description") ?: ""
            val sellerID = doc.getString("sellerID") ?: ""
            val sellerRating = doc.getLong("sellerRating")?.toInt() ?: 0

            Product(id, name, price, imageURL, category, description, sellerID, sellerRating)
        }

        // Guardamos los favoritos en Room como FavoriteEntity
        dao.clearAllFavorites()
        dao.insertFavorites(favoritos.map { it.toFavoriteEntity() })

        return favoritos
    }

    fun getFavoritesOffline(): Flow<List<Product>> {
        return dao.observeAllFavorites()
            .map { it.map { entity -> entity.toDomainF() } }
    }

}
