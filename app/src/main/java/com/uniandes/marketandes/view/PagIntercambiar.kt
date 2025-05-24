package com.uniandes.marketandes.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Foundation
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.outlined.SignalWifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.uniandes.marketandes.viewModel.ExchangeProductViewModel
import coil.compose.rememberAsyncImagePainter
import com.uniandes.marketandes.model.ExchangeProduct


@Composable
fun PagIntercambiar(navController: NavHostController, viewModel: ExchangeProductViewModel) {
    val productos by viewModel.exchangeProducts.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()

    var selectedCategory by remember { mutableStateOf("Todos") }

    val categories = listOf(
        "Ciencias" to Icons.Filled.Science,
        "Tecnología" to Icons.Filled.Memory,
        "Lenguas" to Icons.Filled.Language,
        "Arquitectura" to Icons.Filled.Foundation,
        "Libros" to Icons.Filled.Book,
    )

    val title = when (selectedCategory) {
        "Todos" -> "Todos los productos para intercambiar"
        else -> "Productos para intercambio de $selectedCategory"
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isConnected) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SignalWifiOff,
                            contentDescription = "Sin conexión",
                            tint = Color(0xFF856404),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Estás sin conexión. Algunos datos podrían no estar actualizados.",
                            color = Color(0xFF856404),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterButton(
                        categoryName = "Todos",
                        icon = Icons.Filled.Apps,
                        isSelected = selectedCategory == "Todos",
                        onClick = { selectedCategory = "Todos" }
                    )
                }
                categories.forEach { (categoryName, icon) ->
                    item {
                        FilterButton(
                            categoryName = categoryName,
                            icon = icon,
                            isSelected = selectedCategory == categoryName,
                            onClick = { selectedCategory = categoryName }
                        )
                    }
                }
            }

            val filteredProducts = if (selectedCategory == "Todos") {
                productos
            } else {
                productos.filter { it.category == selectedCategory }
            }

            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay productos para esta categoría",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredProducts) { producto ->
                        ExchangeProductCard(product = producto, navController = navController)
                    }
                }
            }
        }
    }
}

@Composable
fun ExchangeProductCard(product: ExchangeProduct, navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { navController.navigate("detalle_intercambiar/${product.id}") }
    ) {
        GlideImage(
            imageUrl = product.imageURL,
            contentDescription = "Imagen de ${product.name}",
            modifier = Modifier
                .height(150.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Text(
            text = product.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp)
        )

        Box(
            modifier = Modifier
                .wrapContentWidth()
                .align(Alignment.CenterHorizontally)
                .padding(top = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF002366))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            val exchangeText = if (product.productToExchangeFor.isNullOrBlank()) {
                "Sugiere tu oferta para intercambiar"
            } else {
                "Intercambio por ${product.productToExchangeFor}"
            }
            Text(
                text = exchangeText,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}