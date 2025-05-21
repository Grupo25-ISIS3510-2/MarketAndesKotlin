package com.uniandes.marketandes.view

import androidx.compose.runtime.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.shape.CircleShape
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
import com.uniandes.marketandes.viewModel.ExchangeProductDetailViewModel
import com.uniandes.marketandes.viewModel.ExchangeProductDetailViewModelFactory

@Composable
fun PagIntercambiarDetail(
    navController: NavHostController,
    productId: String
) {
    val context = LocalContext.current
    val dao = AppDatabase.getDatabase(context).exchangeProductDao()
    val viewModel: ExchangeProductDetailViewModel = viewModel(
        factory = ExchangeProductDetailViewModelFactory(dao)
    )

    val product by viewModel.product.collectAsState(initial = null)
    val sellerName by viewModel.sellerName.collectAsState()

    LaunchedEffect(productId) {
        viewModel.loadExchangeProduct(productId)
    }

    product?.let { itProduct ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Botón para volver atrás
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

            GlideImage(
                imageUrl = itProduct.imageURL,
                contentDescription = "Imagen de ${itProduct.name}",
                modifier = Modifier
                    .height(250.dp)
                    .fillMaxWidth(0.7f)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit
            )

            Text(
                text = itProduct.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = itProduct.category,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 9.dp, bottom = 9.dp)
            )

            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF002366))
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                val exchangeText = if (itProduct.productToExchangeFor.isNullOrBlank()) {
                    "Sugiere tu oferta para intercambiar"
                } else {
                    "Intercambio por ${itProduct.productToExchangeFor}"
                }
                Text(
                    text = exchangeText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            Text(
                text = itProduct.description,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { /* Acción guardar en favoritos */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "Guardar en favoritos",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { /* Acción contactar vendedor */ },
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
                    text = sellerName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                repeat(itProduct.sellerRating) {
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