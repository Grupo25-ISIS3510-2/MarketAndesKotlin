package com.uniandes.marketandes.view.favorites

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.uniandes.marketandes.view.HomeProduct
import com.uniandes.marketandes.model.Product
import com.uniandes.marketandes.viewmodel.FavoritosViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun PagFavoritos(navController: NavHostController, viewModel: FavoritosViewModel = remember { FavoritosViewModel() }) {
    val productos by viewModel.productosFavoritos.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tus productos favoritos",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize()
        ) {
            items(productos) { producto ->
                FavoriteProductCard(product = producto, navController)
            }
        }
    }
}

@Composable
fun FavoriteProductCard(product: Product, navController: NavHostController, viewModel: FavoritosViewModel = remember { FavoritosViewModel() }) {
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
