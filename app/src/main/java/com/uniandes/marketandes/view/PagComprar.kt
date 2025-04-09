package com.uniandes.marketandes.view

import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items

@Composable
fun PagComprar(navController: NavHostController) {
    var productos by remember { mutableStateOf<List<Product>>(emptyList()) }
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        productos = getProductsFromFirestore(db)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Todos los Productos",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize()
        ) {
            items(productos) { producto ->
                ProductCard(product = producto, navController = navController)
            }
        }
    }
}

@Composable
fun ProductCard(product: Product, navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { navController.navigate("detalle_compra/${product.id}") }
    ) {
        Image(
            painter = rememberAsyncImagePainter(product.imageURL),
            contentDescription = "Imagen de ${product.name}",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .height(150.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
        )

        Text(
            text = product.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF002366))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$ ${product.price}",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

suspend fun getProductsFromFirestore(db: FirebaseFirestore): List<Product> {
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
            val comments = getCommentsForProduct(db, id)
            Product(
                id = doc.id,
                name = name,
                price = price,
                imageURL = imageURL,
                category = category,
                description = description,
                sellerID = sellerID,
                sellerRating = sellerRating,
                comments = comments
            )
        }
    } catch (e: Exception) {
        emptyList()
    }
}

suspend fun getCommentsForProduct(db: FirebaseFirestore, id: String): List<Comment> {
    return try {
        val commentsSnapshot = db.collection("products")
            .document(id)
            .collection("comments")
            .get()
            .await()

        commentsSnapshot.documents.mapNotNull { doc ->
            val text = doc.getString("text") ?: ""
            val author = doc.getString("author") ?: "Anonimo"
            Comment(text, author)
        }
    } catch (e: Exception) {
        emptyList()
    }
}

data class Comment(
    val text: String,
    val author: String
)

data class Product(
    val id: String,
    val name: String,
    val price: Int,
    val imageURL: String,
    val category: String,
    val description: String,
    val sellerID: String,
    val sellerRating: Int,
    val comments: List<Comment> = emptyList()
)
