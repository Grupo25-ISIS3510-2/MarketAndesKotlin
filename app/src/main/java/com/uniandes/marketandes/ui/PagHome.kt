package com.uniandes.marketandes.ui

import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

@Composable
fun PagHome(navController: NavHostController) {
    var productos by remember { mutableStateOf<List<HomeProduct>>(emptyList()) }
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        productos = getHomeProductsFromFirestore(db).filter { it.category == "Ciencias" }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Productos recomendados",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn {
            items(productos) { producto ->
                HomeProductCard(product = producto, navController)
            }
        }
    }
}

@Composable
fun HomeProductCard(product: HomeProduct, navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
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
            modifier = Modifier
                .padding(top = 4.dp)
                .clickable { navController.navigate("detalle_compra/${product.name}") }
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

suspend fun getHomeProductsFromFirestore(db: FirebaseFirestore): List<HomeProduct> {
    return try {
        val snapshot = db.collection("products").get().await()
        snapshot.documents.mapNotNull { doc ->
            val name = doc.getString("name") ?: "Sin nombre"
            val price = doc.getLong("price")?.toInt() ?: 0
            val imageURL = doc.getString("imageURL") ?: "Sin imagen"
            val category = doc.getString("category") ?: "Sin categoria"
            val description = doc.getString("description") ?: "Sin descripción"
            val sellerID = doc.getString("sellerID") ?: "Sin vendedor"
            val sellerRating = doc.getLong("sellerRating")?.toInt() ?: 0
            val comments = getHomeCommentsForProduct(db, name)
            HomeProduct(name, price, imageURL, category, description, sellerID, sellerRating, comments )
        }
    } catch (e: Exception) {
        emptyList()
    }
}

private suspend fun getHomeCommentsForProduct(db: FirebaseFirestore, name: String): List<ProductComment> {
    return try {
        val commentsSnapshot = db.collection("products")
            .document(name)
            .collection("comments")
            .get()
            .await()

        commentsSnapshot.documents.mapNotNull { doc ->
            val text = doc.getString("text") ?: ""
            val author = doc.getString("author") ?: "Anonimo"
            ProductComment(text, author)
        }
    } catch (e: Exception) {
        emptyList()
    }
}

data class ProductComment(
    val text: String,
    val author: String
)

data class HomeProduct(
    val name: String,
    val price: Int,
    val imageURL: String,
    val category: String,
    val description: String,
    val sellerID: String,
    val sellerRating: Int,
    val comments: List<ProductComment>
)