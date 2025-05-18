package com.uniandes.marketandes.view

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
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.firebase.auth.FirebaseAuth
import com.uniandes.marketandes.model.Product
import com.uniandes.marketandes.util.NetworkConnectivityObserver
import com.uniandes.marketandes.viewModel.ConnectivityViewModel
import com.uniandes.marketandes.viewModel.ProductViewModel
import com.uniandes.marketandes.viewModel.ProductViewModelFactory

@Composable
fun Pag_misPublicaciones(navController: NavHostController) {
    val context = LocalContext.current
    val connectivityObserver = remember { NetworkConnectivityObserver(context) }
    val factory = remember { ProductViewModelFactory(connectivityObserver, context) }
    val productViewModel: ProductViewModel = viewModel(factory = factory)
    val connectivityViewModel: ConnectivityViewModel = viewModel()

    LaunchedEffect(Unit) {
        connectivityViewModel.startNetworkCallback(context)
    }

    val isConnected by connectivityViewModel.isConnected
    val currentUser = FirebaseAuth.getInstance().currentUser
    val myProducts by productViewModel.products.collectAsState(initial = emptyList())

    val userProducts = remember(myProducts, currentUser?.uid) {
        myProducts.filter { it.sellerID == currentUser?.uid }
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
                        modifier = Modifier.fillMaxWidth(),
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
                            text = "Estás sin conexión. Mostrando tus publicaciones locales.",
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
                text = "Tus Publicaciones",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (myProducts.isEmpty()) {
                if (isConnected) {
                    CircularProgressIndicator()
                } else {
                    Text(
                        text = "No tienes publicaciones para mostrar.",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
            } else {
                if (userProducts.isEmpty()) {
                    Text(
                        text = "No tienes publicaciones creadas.",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        items(userProducts) { product ->
                            PublicationCard(
                                product = product,
                                onClick = { navController.navigate("detalle_compra/${product.id}") },
                                onDelete = {
                                    productViewModel.deleteProduct(product.id)
                                    Toast.makeText(
                                        context,
                                        "Publicación eliminada",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onEdit = {
                                    navController.navigate("editar_publicacion/${product.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PublicationCard(
    product: Product,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .width(170.dp)
            .height(350.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GlideImage(
                model = product.imageURL,
                contentDescription = "Imagen de ${product.name}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(140.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = product.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF002366))
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$ ${product.price}",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF002366))
                ) {
                    Text(
                        "Editar",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text(
                        "Eliminar",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}
