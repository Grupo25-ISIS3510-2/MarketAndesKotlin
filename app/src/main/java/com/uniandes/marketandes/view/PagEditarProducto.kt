package com.uniandes.marketandes.view

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SignalWifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.uniandes.marketandes.util.ConnectivityObserver
import com.uniandes.marketandes.util.NetworkConnectivityObserver
import com.uniandes.marketandes.viewModel.ProductDetailViewModel
import com.uniandes.marketandes.viewModel.ProductViewModel
import com.uniandes.marketandes.viewModel.ProductViewModelFactory
import kotlinx.coroutines.launch
import com.uniandes.marketandes.cache.ProductCache


@Composable
fun PagEditarProducto(
    navController: NavController,
    productId: String,
    connectivityObserver: ConnectivityObserver
) {
    val context = LocalContext.current
    val factory = ProductViewModelFactory(connectivityObserver, context)
    val productViewModel: ProductViewModel = viewModel(factory = factory)
    val productDetailViewModel: ProductDetailViewModel = viewModel()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Observamos el estado de red
    val networkObserver = remember { NetworkConnectivityObserver(context) }
    val isConnected by networkObserver.isConnected.collectAsState()

    // Cargar producto una vez
    LaunchedEffect(productId) {
        productDetailViewModel.loadProduct(productId)
    }

    val productState by productDetailViewModel.product.observeAsState()

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    actionColor = Color.White,
                    containerColor = Color(0xFF00205B),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    ) { padding ->
        productState?.let { producto ->
            var nombre by remember { mutableStateOf(producto.name) }
            var descripcion by remember { mutableStateOf(producto.description) }
            var precio by remember { mutableStateOf(producto.price.toString()) }
            var categoria by remember { mutableStateOf(producto.category) }
            var imageUrl by remember { mutableStateOf(producto.imageURL) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!isConnected) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SignalWifiOff,
                            contentDescription = "Sin conexión",
                            tint = Color(0xFF856404),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Estás sin conexión. Cambios se guardarán localmente.",
                            color = Color(0xFF856404),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text("Editar producto", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = precio,
                    onValueChange = { precio = it },
                    label = { Text("Precio") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = categoria,
                    onValueChange = { categoria = it },
                    label = { Text("Categoría") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("URL de la imagen") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val precioDouble = precio.toDoubleOrNull()
                        if (precioDouble != null) {
                            productViewModel.actualizarProducto(
                                id = productId,
                                nombre = nombre,
                                descripcion = descripcion,
                                precio = precioDouble,
                                categoria = categoria,
                                imagen = imageUrl
                            )


                            Log.d("CONEXIOEDIT", isConnected.toString())

                            if (!isConnected) {
                                coroutineScope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Cambios guardados localmente. Se sincronizarán cuando haya conexión.",
                                        actionLabel = "Entendido",
                                        duration = SnackbarDuration.Indefinite
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        navController.popBackStack()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Producto actualizado", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                        } else {
                            Toast.makeText(context, "Precio inválido", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Guardar cambios")
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}
