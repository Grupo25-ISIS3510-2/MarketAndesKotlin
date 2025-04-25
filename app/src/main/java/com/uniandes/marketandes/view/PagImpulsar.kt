package com.uniandes.marketandes.view

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.uniandes.marketandes.model.Product

@Composable
fun PagImpulsar(navController: NavHostController) {
    val context = LocalContext.current
    val db = Firebase.firestore
    val productosPorCategoria = remember { mutableStateMapOf<String, MutableList<Product>>() }

    LaunchedEffect(true) {
        db.collection("products")
            .get()
            .addOnSuccessListener { result ->
                val mapa = mutableMapOf<String, MutableList<Product>>()
                for (document in result) {
                    val id = document.id
                    val nombre = document.getString("name") ?: "Sin nombre"
                    val precio = (document.getDouble("price") ?: 0.0).toInt()
                    val imageURL = document.getString("imageURL") ?: ""
                    val categoria = document.getString("category") ?: "Sin categoría"
                    val descripcion = document.getString("description") ?: ""
                    val sellerID = document.getString("sellerID") ?: ""
                    val sellerRating = (document.getDouble("sellerRating") ?: 0.0).toInt()

                    val producto = Product(id, nombre, precio, imageURL, categoria, descripcion, sellerID, sellerRating)
                    mapa.getOrPut(categoria) { mutableListOf() }.add(producto)
                }
                productosPorCategoria.clear()
                productosPorCategoria.putAll(mapa.toSortedMap())
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error cargando datos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Productos para impulsar",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )
        }

        item {
            Text(
                text = "Impulsa tus productos y ayuda a otros estudiantes a encontrar lo que necesitan.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth()
            )
        }

        item {
            Text(
                text = "Estas categorías muestran productos que puedes destacar.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth()
            )
        }

        item {
            Text(
                text = "Cuantos más productos haya por categoría, ¡mayor será la visibilidad para todos!",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth()
            )
        }

        item {
            Text(
                text = "Revisa lo que ya está publicado y, si tienes algo que encaje, ¡súbelo y empieza a vender más rápido!",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth()
            )
        }

        productosPorCategoria.forEach { (categoria, productos) ->
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(Color(0xFFE9E4F0), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = categoria,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5A189A)
                        )
                        Text(
                            text = "${productos.size} ${if (productos.size == 1) "producto" else "productos"}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF7B2CBF)
                        )
                    }
                }
            }

            items(productos) { producto ->
                ProductoCard(product = producto, navController = navController)
            }
        }
    }
}

@Composable
fun ProductoCard(product: Product, navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { navController.navigate("detalle_compra/${product.id}") },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F1FB)) // Morado claro
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = rememberAsyncImagePainter(product.imageURL),
                contentDescription = "Imagen de ${product.name}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(160.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF002366)), // Azul oscuro
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
}
