package com.uniandes.marketandes.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.uniandes.marketandes.model.Product
import com.uniandes.marketandes.model.ProductForm

class FirebaseService {
    private val firestore = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    fun uploadProductFromForm(
        form: ProductForm,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    )
    {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val sellerID = FirebaseAuth.getInstance().currentUser?.uid


        if (currentUser != null) {
            val product = hashMapOf(
                "name" to form.name,
                "price" to form.price,
                "imageURL" to form.imageURL,
                "category" to form.category,
                "description" to form.description,
                "sellerID" to sellerID,
                "sellerRating" to 5
            )

            Log.d("Firestore", "Subiendo producto: $product")
            firestore.collection("products")
                .add(product)
                .addOnSuccessListener {
                    Log.d("Firestore", "Producto subido correctamente")
                    onSuccess()
                }
                .addOnFailureListener {
                    Log.e("Firestore", "Error al subir producto: ${it.message}")
                    onFailure(it)
                }
        } else {
            Log.e("Auth", "Usuario no autenticado")
            onFailure(Exception("Usuario no autenticado"))
        }
    }


    fun Product.toMap(): Map<String, Any> = mapOf(
    "name" to name,
    "price" to price,
    "imageURL" to imageURL,
    "category" to category,
    "description" to description,
    "sellerID" to sellerID,
    "sellerRating" to sellerRating
                )
    }
    fun eliminarDeFirebase(productId: String, onComplete: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val productosRef = db.collection("productos")

        productosRef.document(productId)
            .delete()
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Error eliminando producto", e)
                onComplete(false)
            }
    }
