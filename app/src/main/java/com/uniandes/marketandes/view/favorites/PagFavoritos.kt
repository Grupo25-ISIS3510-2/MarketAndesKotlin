package com.uniandes.marketandes.view.favorites

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SignalWifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.uniandes.marketandes.local.AppDatabase
import com.uniandes.marketandes.model.Product
import com.uniandes.marketandes.util.NetworkConnectivityObserver
import com.uniandes.marketandes.viewModel.ConnectivityViewModel
import com.uniandes.marketandes.viewModel.FavoritosViewModelFactory
import com.uniandes.marketandes.viewmodel.FavoritosViewModel

@Composable
fun PagFavoritos(navController: NavHostController) {
    val context = LocalContext.current

    val dao = AppDatabase.getDatabase(context).favoriteDao()
    val connectivityViewModel: ConnectivityViewModel = viewModel()
    val connectivityObserver = remember { NetworkConnectivityObserver(context) }
    val factory = remember { FavoritosViewModelFactory(dao, connectivityObserver) }
    val favoritosViewModel: FavoritosViewModel = viewModel(factory = factory)

    // Ejecutamos la comprobación de conectividad
    LaunchedEffect(Unit) {
        connectivityViewModel.startNetworkCallback(context)
    }

    val isConnected by connectivityViewModel.isConnected
    val productos by favoritosViewModel.productosFavoritos.collectAsState()
    val toastMessage by favoritosViewModel.mensajeVisible.collectAsState()

    // Mostrar mensaje como toast
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            favoritosViewModel.mensajeMostrado()
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
            // Banner de advertencia persistente cuando está sin conexión
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
                            text = "Estás sin conexión. Mostrando favoritos locales.",
                            color = Color(0xFF856404),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Text(
                text = "Tus productos favoritos",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (productos.isEmpty() && isConnected) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(productos) { producto ->
                        FavoriteProductCard(
                            product = producto,
                            navController = navController,
                            onCardClick = { id -> navController.navigate("detalle_compra/$id") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteProductCard(
    product: Product,
    navController: NavHostController,
    onCardClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .width(160.dp)
            .height(240.dp)
            .clickable { onCardClick(product.id) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            androidx.compose.foundation.Image(
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
