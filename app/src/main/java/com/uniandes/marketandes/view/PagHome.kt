package com.uniandes.marketandes.view

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uniandes.marketandes.viewmodel.FavoritosViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uniandes.marketandes.model.Product


@Composable
fun PagHome(navController: NavHostController) {
    var productos by remember { mutableStateOf<List<Product>>(emptyList()) }
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    val favoritosViewModel: FavoritosViewModel = viewModel()
    val categoriaFavorita by favoritosViewModel.categoriaFavorita.collectAsState()



    LaunchedEffect(userId) {
        if (userId != null) {
            val preferences = getUserPreferences(db, userId)
            val allProducts = getHomeProductsFromFirestore(db)
            Log.d("Productos", "Cantidad de productos obtenidos: ${allProducts.size}")

            productos = allProducts.filter { product ->
                preferences.faculties.contains(product.category) ||
                        preferences.interests.contains(product.category) ||
                        product.category.trim().equals(categoriaFavorita?.trim(), ignoreCase = true)

            }

            Log.d("Facultades", "Facultades del usuario: ${preferences.faculties}")
            Log.d("Intereses", "Intereses del usuario: ${preferences.interests}")
            Log.d("ProductosFiltrados", "Cantidad de productos filtrados: ${productos.size}")
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (categoriaFavorita != null) {
            Text(
                text = "Recomendaciones de tu categoría favorita: $categoriaFavorita",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            val productosFavoritos = productos.filter { it.category == categoriaFavorita }
            LazyRow(
                modifier = Modifier.fillMaxSize()
            ) {
                items(productosFavoritos) { producto ->
                    HomeProductCard(product = producto, navController = navController)
                }
            }
        } else {
            Text(
                text = "No tienes productos favoritos en una categoría",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        if (productos.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(productos) { producto ->
                    HomeProductCard(product = producto, navController = navController)
                }
            }
        } else {
            Text(
                text = "",
                fontSize = 16.sp
            )
        }

            Text(
            text = "Productos recomendados",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize()
        ) {
            items(productos) { producto ->
                HomeProductCard(product = producto, navController = navController)
            }
        }
    }
}

@Composable
fun HomeProductCard(product: Product, navController: NavHostController) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .width(160.dp)
            .height(240.dp)
            .clickable {
                if (product.id.isNotEmpty()) {
                    navController.navigate("detalle_compra/${product.id}")
                } else {
                    Log.e("Navigation", "El ID del producto está vacío")
                }
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {

        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .height(140.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(product.imageURL),
                    contentDescription = "Imagen de ${product.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            }


            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.name,
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            )


            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF002366))
                    .height(36.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$ ${product.price}",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

    suspend fun getUserPreferences(db: FirebaseFirestore, userId: String): UserPreferences {
    return try {
        val snapshot = db.collection("users").document(userId).get().await()
        val faculties = snapshot.get("faculties") as? List<String> ?: emptyList()
        val interests = snapshot.get("interests") as? List<String> ?: emptyList()
        UserPreferences(faculties, interests)
    } catch (e: Exception) {
        UserPreferences(emptyList(), emptyList())
    }
}

data class UserPreferences(
    val faculties: List<String>,
    val interests: List<String>
)

suspend fun getHomeProductsFromFirestore(db: FirebaseFirestore): List<Product> {
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



