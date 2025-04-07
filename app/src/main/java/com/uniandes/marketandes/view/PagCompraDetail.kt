package com.uniandes.marketandes.view

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun PagCompraDetail(navController: NavHostController, productName: String) {
    var product by remember { mutableStateOf<Product?>(null) }
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(productName) {
        product = getProductDetails(db, productName)
    }

    product?.let {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Botón de volver
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .background(Color (0xFF002D6A), shape = CircleShape)
                        .padding(12.dp) //
                        .size(24.dp)

                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White,
                        modifier = Modifier
                                .size(42.dp)
                    )
                }
            }
            // Imagen del producto con navegación
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { /* Acción para imagen anterior */ }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Anterior"
                    )
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
                IconButton(onClick = { /* Acción para imagen siguiente */ }) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Siguiente"
                    )
                }
            }

            // Nombre del producto
            Text(
                text = it.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Categoria del producto
            Text(
                text = it.category,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 9.dp)
            )

            // Precio
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


            // Descripcion del producto
            Text(
                text = it.description,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )


            // Botones de acción
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { /* Acción de compra */ },
                    colors = ButtonDefaults.buttonColors(Color(0xFFFFC107)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Comprar", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { /* Acción para contactar */ },
                    colors = ButtonDefaults.buttonColors(Color(0xFF002D6A)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Contactar Vendedor", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            // Vendedor con estrellas
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
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

            // Sección de comentarios
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    text = "Comentarios",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                if (it.comments.isNotEmpty()) {
                    it.comments.forEach { comment ->
                        CommentItem(comment)
                    }
                } else {
                    Text(
                        text = "No hay comentarios aún.",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

        }
    } ?: CircularProgressIndicator(modifier = Modifier.padding(16.dp))
}

@Composable
fun CommentItem(comment: Comment) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = comment.author,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = comment.text,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

suspend fun getProductDetails(db: FirebaseFirestore, name: String): Product? {
    return try {
        val snapshot = db.collection("products")
            .whereEqualTo("name", name)
            .get()
            .await()

        snapshot.documents.firstOrNull()?.let { doc ->
            Product(
                name = doc.getString("name") ?: "",
                price = doc.getLong("price")?.toInt() ?: 0,
                imageURL = doc.getString("imageURL") ?: "",
                category = doc.getString("category") ?: "",
                description = doc.getString("description")?: "Sin descripción",
                sellerID = doc.getString("sellerID") ?: "",
                sellerRating = doc.getLong("sellerRating")?.toInt() ?: 0,
                comments = getCommentsForProduct(db, doc.id)

            )
        }
    } catch (e: Exception) {
        null
    }
}