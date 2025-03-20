package com.uniandes.marketandes

import RegisterScreen
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.uniandes.marketandes.ui.*
import com.uniandes.marketandes.ui.authentication.ui.AuthenticationScreen
import com.uniandes.marketandes.ui.authentication.ui.AuthenticationViewModel
import com.uniandes.marketandes.ui.authentication.ui.RegistrationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem("", R.drawable.shop_icon, "pag_comprar"),
        BottomNavItem("", R.drawable.add_icon, "pag_vender"),
        BottomNavItem("", R.drawable.home_icon, "pag_home"),
        BottomNavItem("", R.drawable.exchange_icon, "pag_intercambio"),
        BottomNavItem("", R.drawable.chat_icon, "pag_chat")
    )

    var selectedIndex by remember { mutableStateOf(items.indexOfFirst { it.route == "pag_home" }) }
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        topBar = { HeaderBar() },
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
        ContentScreen(navController, Modifier.padding(innerPadding))
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderBar() {
    var isSidebarOpen by remember { mutableStateOf(false) }
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
            IconButton(onClick = { isSidebarOpen = !isSidebarOpen }) {
                Icon(
                    painter = painterResource(id = R.drawable.settings_logo),
                    contentDescription = "Menú",
                    tint = if (isSidebarOpen) Color(0xFFFDC500) else Color.White
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
fun ContentScreen(navController: NavHostController, modifier: Modifier) {
    NavHost(
        navController,
        startDestination = "pag_home",  // Aquí empieza tu navegación
        modifier = modifier
    ) {
        composable("authentication") { AuthenticationScreen(AuthenticationViewModel(), navController) }
        composable("register") { RegisterScreen(RegistrationViewModel(), navController) }
        composable("pag_comprar") { PagComprar() }
        composable("pag_vender") { PagVender() }
        composable("pag_home") { PagHome() }
        composable("pag_intercambio") { PagIntercambio() }
        composable("pag_chat") { PagChat(navController) }
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
    }
}

