package com.uniandes.marketandes.view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.SignalWifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import com.uniandes.marketandes.model.Product
import com.uniandes.marketandes.viewModel.ProductViewModel
import com.uniandes.marketandes.util.NetworkConnectivityObserver
import com.uniandes.marketandes.viewModel.ConnectivityViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter

@Composable
fun PagComprar(navController: NavHostController, viewModel: ProductViewModel) {
    val context = LocalContext.current

    val connectivityViewModel: ConnectivityViewModel = viewModel()
    val connectivityObserver = remember { NetworkConnectivityObserver(context) }
    LaunchedEffect(Unit) {
        connectivityViewModel.startNetworkCallback(context)
    }
    val isConnected by connectivityViewModel.isConnected

    val productos by viewModel.products.collectAsState()
    var selectedCategory by remember { mutableStateOf("Todos") }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    val categories = listOf(
        "Ciencias" to Icons.Filled.Science,
        "Tecnología" to Icons.Filled.Memory,
        "Lenguas" to Icons.Filled.Language,
        "Arquitectura" to Icons.Filled.Foundation,
        "Libros" to Icons.Filled.Book,
    )

    var categoryInteractions by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }

    fun loadCategoryInteractions() {
        val db = FirebaseFirestore.getInstance()
        db.collection("categoryInteractions").get()
            .addOnSuccessListener { snapshot ->
                val interactionsMap = snapshot.documents.associate { doc ->
                    val category = doc.id
                    val count = doc.getLong("interactions") ?: 0L
                    category to count
                }
                categoryInteractions = interactionsMap
            }
    }

    LaunchedEffect(Unit) {
        loadCategoryInteractions()
    }

    val sortedCategories = categories.sortedByDescending { (name, _) ->
        categoryInteractions[name] ?: 0L
    }

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
        }.addOnFailureListener {
            toastMessage = "Error actualizando interacciones"
        }.addOnSuccessListener {
            loadCategoryInteractions()
        }
    }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            toastMessage = null
        }
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
                        onClick = {
                            selectedCategory = "Todos"
                            incrementCategoryInteraction("Todos")
                        }
                    )
                }

                sortedCategories.forEach { (categoryName, icon) ->
                    item {
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
                        ProductCard(product = producto, navController = navController)
                    }
                }
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
        androidx.compose.foundation.Image(
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
