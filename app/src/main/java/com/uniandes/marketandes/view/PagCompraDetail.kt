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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.auth.FirebaseAuth
import com.uniandes.marketandes.R
import com.uniandes.marketandes.viewmodel.FavoritosViewModel
import kotlinx.coroutines.tasks.await
import java.util.Date

@Composable
fun PagCompraDetail(
    navController: NavHostController,
    productId: String,
    favoritosViewModel: FavoritosViewModel = viewModel()
) {
    var product by remember { mutableStateOf<Product?>(null) }
    var favoritoGuardado by remember { mutableStateOf(false) }
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    LaunchedEffect(productId) {
        product = getProductDetails(db, productId)

        product?.let {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                val favRef = db.collection("users")
                    .document(currentUser.uid)
                    .collection("favoritos")
                    .document(productId)

                try {
                    val favDoc = favRef.get().await()
                    Log.d("Firebase", "Favorito existe: ${favDoc.exists()}")

                    if (favDoc.exists()) {
                        val updateMap = mapOf("fechaUltimaVisita" to System.currentTimeMillis())
                        val fecha = Date(updateMap["fechaUltimaVisita"] as Long)
                        Log.d("Firebase", "Fecha de última visita: $fecha")
                        favRef.set(updateMap, SetOptions.merge()).await()
                        Log.d("Firebase", "Fecha de última visita actualizada con: $updateMap")
                    } else {
                        Log.d("Firebase", "No se encontró el favorito, no se actualiza fecha.")
                    }
                } catch (e: Exception) {
                    Log.e("Firebase", "Error al actualizar fechaUltimaVisita", e)
                }
            } else {
                Log.e("Firebase", "Usuario no autenticado")
            }
        }
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

            // Imagen del producto con navegación
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { /* Acción para imagen anterior */ }) {
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
                IconButton(onClick = { /* Acción para imagen siguiente */ }) {
                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Siguiente")
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

            // Descripción del producto
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
                val favoritos by favoritosViewModel.productosFavoritos.collectAsState()
                val yaEsFavorito = favoritos.any { fav -> fav.id == it.id }

                Button(
                    onClick = {
                        favoritosViewModel.toggleFavorito(it)
                        favoritoGuardado = !yaEsFavorito

                        // Sonido si se guarda en favoritos
                        if (!yaEsFavorito) {
                            val mediaPlayer = MediaPlayer.create(context, R.raw.add_favorite)
                            mediaPlayer.start()
                            mediaPlayer.setOnCompletionListener {
                                it.release()
                            }
                        }

                        Toast.makeText(
                            context,
                            if (yaEsFavorito) "Eliminado de favoritos" else "Guardado en favoritos",
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

suspend fun getProductDetails(db: FirebaseFirestore, productId: String): Product? {
    return try {
        Log.d("Firebase", "Buscando producto con ID: $productId")

        val doc = db.collection("products")
            .document(productId)
            .get()
            .await()

        if (doc.exists()) {
            val product = Product(
                id = doc.id,
                name = doc.getString("name") ?: "",
                price = doc.getLong("price")?.toInt() ?: 0,
                imageURL = doc.getString("imageURL") ?: "",
                category = doc.getString("category") ?: "",
                description = doc.getString("description") ?: "Sin descripción",
                sellerID = doc.getString("sellerID") ?: "",
                sellerRating = doc.getLong("sellerRating")?.toInt() ?: 0,
                comments = getCommentsForProduct(db, doc.id)
            )
            Log.d("Firebase", "Producto encontrado: $product")
            product
        } else {
            Log.d("Firebase", "No se encontró el documento")
            null
        }
    } catch (e: Exception) {
        Log.e("Firebase", "Error al obtener detalles del producto", e)
        null
    }
}
