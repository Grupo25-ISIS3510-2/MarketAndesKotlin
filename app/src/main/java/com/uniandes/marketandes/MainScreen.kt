package com.uniandes.marketandes
import PerfilScreen
import RegisterScreen
import UserPreferencesViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavController
import com.uniandes.marketandes.ui.*
import com.uniandes.marketandes.ui.authentication.ui.AuthenticationScreen
import com.uniandes.marketandes.ui.authentication.ui.AuthenticationViewModel
import com.uniandes.marketandes.ui.authentication.ui.RegistrationViewModel
import com.google.android.gms.maps.model.LatLng
import com.uniandes.marketandes.ui.preferences.FacultySelectionScreen
import com.uniandes.marketandes.ui.preferences.InterestSelectionScreen
import kotlinx.coroutines.launch
import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.uniandes.marketandes.ui.user_preferences.PerfilViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var userLocation by remember { mutableStateOf<LatLng?>(null) }

    GetUserLocation { location ->
        userLocation = location
    }

    val items = listOf(
        BottomNavItem("", R.drawable.shop_icon, "pag_comprar"),
        BottomNavItem("", R.drawable.add_icon, "pag_vender"),
        BottomNavItem("", R.drawable.home_icon, "pag_home"),
        BottomNavItem("", R.drawable.exchange_icon, "pag_intercambio"),
        BottomNavItem("", R.drawable.chat_icon, "pag_chat")
    )

    var selectedIndex by remember { mutableStateOf(items.indexOfFirst { it.route == "pag_home" }) }
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            Box(
                modifier = Modifier
                    .requiredHeight(400.dp)
                    .fillMaxHeight(0.2f)
                    .requiredSizeIn(maxHeight = 200.dp)
                    .background(Color(0xFFF7F2FC))
            )

            if (drawerState.isOpen) {
                DrawerContent(navController, onClose = { scope.launch { drawerState.close() } })
            }
        }
    ) {
        Scaffold(
            topBar = { HeaderBar (onMenuClick = { scope.launch { drawerState.open() } } ) },
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar(containerColor = Color(0xFF00296B)) {
                    items.forEach { item ->
                        NavigationBarItem(
                            currentRoute == item.route,
                            onClick = {
                                selectedIndex = items.indexOf(item)
                                navController.navigate(item.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(id = item.icon),
                                    contentDescription = item.title,
                                    tint = if (currentRoute == item.route) Color(0xFFFDC500) else Color.White
                                )
                            },
                            label = {},
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFFFDC500),
                                unselectedIconColor = Color.White,
                                indicatorColor = Color(0xFF00296B)
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            ContentScreen(navController,userLocation, Modifier.padding(innerPadding))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderBar(onMenuClick: () -> Unit) {
    TopAppBar(
        title = {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.market_andes_largo),
                    contentDescription = "MarketAndes Logo",
                    modifier = Modifier.height(50.dp).padding(end = 20.dp)
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    painter = painterResource(id = R.drawable.settings_logo),
                    contentDescription = "Menú",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF00296B),
            titleContentColor = Color.White
        )
    )
}

@Composable
fun ContentScreen(navController: NavHostController, userLocation: LatLng?, modifier: Modifier) {
    NavHost(
        navController,
        startDestination = "pag_home",
        modifier = modifier
    ) {
        composable("authentication") { AuthenticationScreen(AuthenticationViewModel(), navController) }
        composable("register") { RegisterScreen(RegistrationViewModel(), navController) }
        composable("pag_comprar") { PagComprar(navController) }
        composable("pag_vender") { PagVender() }
        composable("pag_home") { PagHome(navController) }
        composable("pag_intercambio") { PagIntercambio() }
        composable("pag_chat") { PagChat(navController) }

        composable("pag_perfil_screen") { PerfilScreen(navController) }
        composable(
            route = "edit_faculties?preselected={preselected}",
            arguments = listOf(navArgument("preselected") {
                defaultValue = ""
            })
        ) { backStackEntry ->
            val preselected = backStackEntry.arguments?.getString("preselected") ?: ""
            val preselectedList = if (preselected.isNotEmpty()) preselected.split(",") else emptyList()

            FacultySelectionScreen(
                navController = navController,
                viewModel = viewModel(),
                isEdit = true,
                preselectedFaculties = preselectedList)
        }

        composable(
            route = "storemaps?destinoNombre={destinoNombre}&destinoImagen={destinoImagen}",
            arguments = listOf(
                navArgument("destinoNombre") { defaultValue = ""; nullable = true },
                navArgument("destinoImagen") { defaultValue = ""; nullable = true }
            )
        ) { backStackEntry ->
            val destinoNombre = backStackEntry.arguments?.getString("destinoNombre")
            val destinoImagen = backStackEntry.arguments?.getString("destinoImagen")
            PagStoreMaps(navController, destinoNombre, destinoImagen)
        }



        composable(
            route = "edit_interests?preselected={preselected}",
            arguments = listOf(
                navArgument("preselected") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val preselected = backStackEntry.arguments?.getString("preselected")
                ?.split(",")
                ?.filter { it.isNotBlank() } ?: emptyList()

            InterestSelectionScreen(
                navController = navController,
                viewModel = viewModel(),
                isEdit = true,
                preselectedInterests = preselected,

            )
        }



        composable("pag_compra") { PagComprar(navController) }
        composable("pag_store_maps") {
            userLocation?.let { location ->
                PagStoreMaps(navController)
            } ?: run {
                Text("Obteniendo ubicación...", fontSize = 20.sp, modifier = Modifier.padding(16.dp))
            }
        }
        composable("detalle_compra/{productName}") { backStackEntry ->
            val productName = backStackEntry.arguments?.getString("productName") ?: ""
            PagCompraDetail(navController, productName)
        }
        composable("PagChatMap/{chatId}") { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            PagChatMap(
                navController = navController,
                chatId = chatId
            )
        }
        composable("chatDetail/{chatId}") { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId")
            if (chatId != null) {
                ChatDetailScreen(chatId = chatId, navController = navController)
            }
        }
        composable("confirmarUbicacion/{chatId}/{nombreUbicacion}/{imagenUrl}") {
            ConfirmarUbicacionScreen(navController = navController)
        }
        composable("ubicaciondetail/{nombreUbicacion}/{imagenUrl}") {
            UbicacionDetail(navController)
        }
    }
}

@Composable
fun DrawerContent(navController: NavController, onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .width(280.dp)
            .background(Color(0xFFF7F2FC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .height(500.dp)
                .padding(start = 16.dp, top = 40.dp),
            verticalArrangement = Arrangement.Top
        ) {
            DrawerItem(icon = Icons.Outlined.AccountCircle,text = "Mi perfil",
                onClick = {
                    navController.navigate("pag_perfil_screen")
                    onClose()
                }
            )
            DrawerItem(icon = Icons.Outlined.GridView, text = "Mis publicaciones")
            DrawerItem(icon = Icons.Outlined.History, text = "Historial")
            DrawerItem(icon = Icons.Outlined.ShoppingBag, text = "Mis compras")
            DrawerItem(
                icon = Icons.Outlined.Storefront,
                text = "Tiendas recomendadas",
                onClick = {
                    navController.navigate("pag_store_maps")
                    onClose()
                }
            )
            DrawerItem(
                icon = Icons.Outlined.Logout,
                text = "Cerrar Sesión",
                onClick = {
                    Firebase.auth.signOut()
                    val context = navController.context
                    val intent = Intent(context, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
            )

        }
    }
}

@Composable
fun DrawerItem(icon: ImageVector, text: String, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick?.invoke() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = Color(0xFF1A237E),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            color = Color.Black,
            fontWeight = FontWeight.Medium
        )
    }
}