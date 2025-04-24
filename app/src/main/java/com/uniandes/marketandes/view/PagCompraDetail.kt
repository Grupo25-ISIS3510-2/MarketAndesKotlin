package com.uniandes.marketandes.view

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.uniandes.marketandes.R
import com.uniandes.marketandes.local.AppDatabase
import com.uniandes.marketandes.model.Product
import com.uniandes.marketandes.viewmodel.FavoritosViewModel
import com.uniandes.marketandes.viewModel.FavoritosViewModelFactory
import com.uniandes.marketandes.viewModel.ProductDetailViewModel
import com.uniandes.marketandes.util.NetworkConnectivityObserver
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Composable
fun PagCompraDetail(
    navController: NavHostController,
    productId: String
) {
    val context = LocalContext.current
    val daoP = AppDatabase.getDatabase(context).productDao()
    val daoF = AppDatabase.getDatabase(context).favoriteDao()
    val connectivityObserver = remember { NetworkConnectivityObserver(context) }
    val favoritosFactory = remember { FavoritosViewModelFactory(daoF, connectivityObserver) }
    val favoritosViewModel: FavoritosViewModel = viewModel(factory = favoritosFactory)

    val detailViewModel: ProductDetailViewModel = viewModel()

    val product by detailViewModel.product.observeAsState()

    LaunchedEffect(productId) {
        detailViewModel.loadProduct(productId)
    }

    val connectivityState = connectivityObserver.isConnected.collectAsState(initial = false)
    val isOffline = !connectivityState.value

    product?.let {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .background(Color(0xFF002D6A), shape = CircleShape)
                        .padding(12.dp)
                        .size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { /* Imagen anterior */ }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Anterior")
                }
                Image(
                    painter = rememberAsyncImagePainter(it.imageURL),
                    contentDescription = "Imagen de ${it.name}",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .height(250.dp)
                        .fillMaxWidth(0.7F)
                        .clip(RoundedCornerShape(8.dp))
                )
                IconButton(onClick = { /* Imagen siguiente */ }) {
                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Siguiente")
                }
            }

            Text(
                text = it.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = it.category,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 9.dp)
            )

            Box(
                modifier = Modifier
                    .background(Color(0xFF002D6A), shape = RoundedCornerShape(8.dp))
                    .padding(vertical = 9.dp, horizontal = 24.dp)
            ) {
                Text(
                    text = "$ ${it.price}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Text(
                text = it.description,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val favoritos by favoritosViewModel.productosFavoritos.collectAsState()
                val yaEsFavorito = favoritos.any { fav: Product -> fav.id == it.id }

                Button(
                    onClick = {
                        favoritosViewModel.toggleFavorito(it)

                        if (!yaEsFavorito) {
                            val mediaPlayer = MediaPlayer.create(context, R.raw.add_favorite)
                            mediaPlayer.start()
                            mediaPlayer.setOnCompletionListener { player ->
                                player.release()
                            }
                        }



                        Toast.makeText(
                            context,
                            when {
                                isOffline && !yaEsFavorito -> "Guardado en favoritos, se sincronizará cuando vuelva la conexión"
                                isOffline && yaEsFavorito -> "Eliminado de favoritos, se sincronizará cuando vuelva la conexión"
                                !isOffline && yaEsFavorito -> "Eliminado de favoritos"
                                !isOffline && !yaEsFavorito -> "Guardado en favoritos"
                                else -> "Guardado en favoritos, se sincronizará cuando vuelva la conexión"
                            },
                            Toast.LENGTH_SHORT
                        ).show()


                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (yaEsFavorito) Color(0xFFD32F2F) else Color(0xFFFFC107)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        if (yaEsFavorito) "Eliminar de favoritos" else "Guardar en favoritos",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { /* Acción contactar */ },
                    colors = ButtonDefaults.buttonColors(Color(0xFF002D6A)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Contactar Vendedor", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Vendedor",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = it.sellerID,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                repeat(it.sellerRating) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Estrella",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    } ?: CircularProgressIndicator(modifier = Modifier.padding(16.dp))
}
