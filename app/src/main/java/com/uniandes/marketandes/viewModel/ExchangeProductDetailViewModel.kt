package com.uniandes.marketandes.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.uniandes.marketandes.local.ExchangeProductDao
import com.uniandes.marketandes.model.ExchangeProduct
import com.uniandes.marketandes.model.ExchangeProductEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ExchangeProductDetailViewModel(
    private val dao: ExchangeProductDao,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _product = MutableStateFlow<ExchangeProduct?>(null)
    val product: StateFlow<ExchangeProduct?> = _product

    private val _sellerName = MutableStateFlow("Cargando…")
    val sellerName: StateFlow<String> = _sellerName

    fun loadExchangeProduct(productId: String) {
        viewModelScope.launch {
            try {
                // 1) Caché local
                dao.getExchangeProductById(productId)?.let { e ->
                    _product.value = mapEntityToModel(e)
                    loadSellerName(e.sellerID)  // cargamos nombre también
                    Log.d("EPDetailVM", "Cargado de caché: $productId")
                }

                // 2) Refrescar desde Firestore
                val doc = db.collection("exchange_products")
                    .document(productId)
                    .get()
                    .await()

                if (doc.exists()) {
                    val remote = ExchangeProduct(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        productToExchangeFor = doc.getString("productToExchangeFor") ?: "",
                        imageURL = doc.getString("imageURL") ?: "",
                        category = doc.getString("category") ?: "",
                        description = doc.getString("description") ?: "",
                        sellerID = doc.getString("sellerID") ?: "",
                        sellerRating = doc.getLong("sellerRating")?.toInt() ?: 0
                    )
                    // Actualizar caché
                    dao.insertExchangeProducts(listOf(mapModelToEntity(remote)))
                    _product.value = remote
                    loadSellerName(remote.sellerID)
                    Log.d("EPDetailVM", "Refrescado de Firestore y cache actualizado")
                }

            } catch (e: Exception) {
                Log.e("EPDetailVM", "Error cargando producto", e)
            }
        }
    }

    private fun loadSellerName(uid: String) {
        viewModelScope.launch {
            try {
                val doc = db.collection("users").document(uid).get().await()
                val name = doc.getString("nombre")
                    ?: doc.getString("fullName")
                    ?: "Vendedor desconocido"
                _sellerName.value = name
            } catch (e: Exception) {
                Log.e("EPDetailVM", "Error cargando nombre vendedor", e)
                _sellerName.value = "Error al cargar nombre"
            }
        }
    }

    private fun mapEntityToModel(e: ExchangeProductEntity) = ExchangeProduct(
        id = e.id,
        name = e.name,
        productToExchangeFor = e.productToExchangeFor,
        imageURL = e.imageURL,
        category = e.category,
        description = e.description,
        sellerID = e.sellerID,
        sellerRating = e.sellerRating
    )

    private fun mapModelToEntity(m: ExchangeProduct) = ExchangeProductEntity(
        id = m.id,
        name = m.name,
        productToExchangeFor = m.productToExchangeFor,
        imageURL = m.imageURL,
        category = m.category,
        description = m.description,
        sellerID = m.sellerID,
        sellerRating = m.sellerRating
    )
}