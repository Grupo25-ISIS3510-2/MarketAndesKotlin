package com.uniandes.marketandes.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uniandes.marketandes.model.ProductForm
import com.uniandes.marketandes.viewmodel.PagVenderViewModel

@Composable
fun PagVender(viewModel: PagVenderViewModel = viewModel()) {
    val form by viewModel.formState.collectAsState()
    var message by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Publicar nuevo producto", style = MaterialTheme.typography.titleLarge)

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = form.name,
                onValueChange = { viewModel.updateForm(form.copy(name = it)) },
                label = { Text("Nombre del producto") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = if (form.price == 0) "" else form.price.toString(),
                onValueChange = {
                    val parsed = it.toIntOrNull() ?: 0
                    viewModel.updateForm(form.copy(price = parsed))
                },
                label = { Text("Precio") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = form.imageURL,
                onValueChange = { viewModel.updateForm(form.copy(imageURL = it)) },
                label = { Text("URL de la imagen") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = form.category,
                onValueChange = { viewModel.updateForm(form.copy(category = it)) },
                label = { Text("Categoría") },
                modifier = Modifier.fillMaxWidth()
            )
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
                    viewModel.submitProduct { success ->
                        message = if (success) {
                            viewModel.resetForm()
                            "Producto subido con éxito"
                        } else {
                            "Error al subir producto"
                        }
                    }
                },
                enabled = form.name.isNotBlank() && form.price > 0 && form.imageURL.isNotBlank(),
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
}
