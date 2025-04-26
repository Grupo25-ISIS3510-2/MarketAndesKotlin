package com.uniandes.marketandes.view

import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import com.uniandes.marketandes.model.Product
import com.uniandes.marketandes.viewModel.ProductViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Foundation
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun PagComprar(navController: NavHostController, viewModel: ProductViewModel) {
    val productos by viewModel.products.collectAsState()
    var selectedCategory by remember { mutableStateOf("Todos") }

    val categories = listOf(
        "Ciencias" to Icons.Filled.Science,
        "Tecnología" to Icons.Filled.Memory,
        "Lenguas" to Icons.Filled.Language,
        "Arquitectura" to Icons.Filled.Foundation,
        "Libros" to Icons.Filled.Book,
    )

    val title = when (selectedCategory) {
        "Todos" -> "Todos los productos"
        "Ciencias" -> "Productos de Ciencias"
        "Tecnología" -> "Productos de Tecnología"
        "Lenguas" -> "Productos de Lenguas"
        "Arquitectura" -> "Productos de Arquitectura"
        "Libros" -> "Productos de Libros"
        else -> "Todos los productos"
    }

    fun incrementCategoryInteraction(category: String) {
        val db = FirebaseFirestore.getInstance()
        val categoryRef = db.collection("categoryInteractions").document(category)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(categoryRef)
            val currentCount = snapshot.getLong("interactions") ?: 0
            transaction.update(categoryRef, "interactions", currentCount + 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterButton(
                categoryName = "Todos",
                icon = Icons.Filled.Apps,
                isSelected = selectedCategory == "Todos",
                onClick = {
                    selectedCategory = "Todos"
                    incrementCategoryInteraction("Todos")
                }
            )
            categories.forEach { (categoryName, icon) ->
                FilterButton(
                    categoryName = categoryName,
                    icon = icon,
                    isSelected = selectedCategory == categoryName,
                    onClick = {
                        selectedCategory = categoryName
                        incrementCategoryInteraction(categoryName)
                    }
                )
            }
        }

        val filteredProducts = if (selectedCategory == "Todos") {
            productos
        } else {
            productos.filter { it.category == selectedCategory }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredProducts) { producto ->
                ProductCard(product = producto, navController = navController)
            }
        }
    }
}

@Composable
fun FilterButton(
    categoryName: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFF002366) else Color.Gray)
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = categoryName,
                modifier = Modifier.size(24.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = categoryName.take(5),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
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
        GlideImage(
            imageUrl = product.imageURL,
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

