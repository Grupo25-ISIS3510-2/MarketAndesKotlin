package com.uniandes.marketandes.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uniandes.marketandes.repository.ExchangeProductRepository
import com.uniandes.marketandes.util.NetworkConnectivityObserver
import com.uniandes.marketandes.viewModel.PagExchangeProductViewModelFactory
import com.uniandes.marketandes.viewmodel.PagExchangeProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagExchangeProduct(
    exchangeProductRepository: ExchangeProductRepository,
    connectivityObserver: NetworkConnectivityObserver,
    viewModel: PagExchangeProductViewModel = viewModel(
        factory = PagExchangeProductViewModelFactory(exchangeProductRepository, connectivityObserver)
    )
){
    val form by viewModel.formState.collectAsState()
    var message by remember { mutableStateOf("") }
    var showOfflineDialog by remember { mutableStateOf(false) }

    val categorias = listOf("Tecnología", "Ciencias", "Arquitectura", "Libros", "Lenguas")
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Publicar nuevo producto de intercambio", style = MaterialTheme.typography.titleLarge)

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = form.name,
                onValueChange = { viewModel.updateForm(form.copy(name = it)) },
                label = { Text("Nombre del producto") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = form.imageURL,
                onValueChange = { viewModel.updateForm(form.copy(imageURL = it)) },
                label = { Text("URL de la imagen") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = form.category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categorias.forEach { categoria ->
                        DropdownMenuItem(
                            text = { Text(categoria) },
                            onClick = {
                                viewModel.updateForm(form.copy(category = categoria))
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = form.description,
                onValueChange = { viewModel.updateForm(form.copy(description = it)) },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00296B),
                    contentColor = Color.White
                ),
                onClick = {
                    viewModel.submitExchangeProduct { result ->
                        when (result) {
                            "online" -> {
                                message = "Producto subido con éxito"
                                viewModel.resetForm()
                            }
                            "offline" -> {
                                showOfflineDialog = true
                                viewModel.resetForm()
                            }
                            else -> {
                                message = "Error al subir producto"
                            }
                        }
                    }
                },
                enabled = form.name.isNotBlank()  && form.imageURL.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Subir producto")
            }

            if (message.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = message,
                    color = if (message.contains("éxito")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showOfflineDialog) {
        AlertDialog(
            onDismissRequest = { showOfflineDialog = false },
            title = { Text("Sin conexión") },
            text = { Text("Tu producto fue guardado localmente y se subirá automáticamente cuando tengas conexión.") },
            confirmButton = {
                TextButton(onClick = { showOfflineDialog = false }) {
                    Text("Aceptar")
                }
            }
        )
    }
}
