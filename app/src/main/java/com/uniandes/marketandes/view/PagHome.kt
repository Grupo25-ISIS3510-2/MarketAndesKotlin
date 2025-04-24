package com.uniandes.marketandes.view

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.uniandes.marketandes.local.AppDatabase
import com.uniandes.marketandes.model.Product
import com.uniandes.marketandes.util.NetworkConnectivityObserver
import com.uniandes.marketandes.util.NetworkStatus
import com.uniandes.marketandes.viewModel.FavoritosViewModelFactory
import com.uniandes.marketandes.viewModel.ProductViewModel
import com.uniandes.marketandes.viewModel.ProductViewModelFactory
import com.uniandes.marketandes.viewmodel.FavoritosViewModel
import kotlinx.coroutines.delay

@Composable
fun PagHome(navController: NavHostController) {
    val context = LocalContext.current
    val connectivityObserver = remember { NetworkConnectivityObserver(context) }

    val dao = AppDatabase.getDatabase(context).favoriteDao()
    val favoritosFactory = remember { FavoritosViewModelFactory(dao, connectivityObserver) }
    val favoritosViewModel: FavoritosViewModel = viewModel(factory = favoritosFactory)

    val productViewModel: ProductViewModel = viewModel(factory = ProductViewModelFactory(connectivityObserver, context))

    val productos by productViewModel.products.collectAsStateWithLifecycle()
    val networkStatus by productViewModel.networkStatus.collectAsStateWithLifecycle()
    val categoriaFavorita by favoritosViewModel.categoriaFavorita.collectAsState()
    val toastMessage by favoritosViewModel.mensajeVisible.collectAsState()

    Log.d("Conexion", "$networkStatus")
    // Mostrar Toast por falta de conexión

    LaunchedEffect(networkStatus) {
        if (networkStatus != NetworkStatus.Available) {
            delay(5000) // ⏳ Espera 5 segundos
            Toast.makeText(context, "Estás sin conexión. Mostrando productos en caché.", Toast.LENGTH_LONG).show()
        }
    }

    // Mostrar Toast de favoritosViewModel
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            favoritosViewModel.mensajeMostrado()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!categoriaFavorita.isNullOrBlank()) {
            Text(
                text = "Recomendaciones de tu categoría favorita: $categoriaFavorita",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            val productosFavoritos = productos.filter { it.category.equals(categoriaFavorita, ignoreCase = true) }
            LazyRow(modifier = Modifier.fillMaxWidth()) {
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

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Productos recomendados",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (productos.isNotEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(4.dp)
            ) {
                items(productos) { producto ->
                    HomeProductCard(product = producto, navController = navController)
                }
            }
        } else {
            Text(
                text = "No hay productos disponibles",
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
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
            Image(
                painter = rememberAsyncImagePainter(product.imageURL),
                contentDescription = "Imagen de ${product.name}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
            )

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
