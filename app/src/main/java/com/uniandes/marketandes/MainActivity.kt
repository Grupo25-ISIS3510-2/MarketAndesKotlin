package com.uniandes.marketandes
//marketandes
import RegisterScreen
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import com.uniandes.marketandes.ui.*
import com.uniandes.marketandes.ui.authentication.ui.AuthenticationScreen
import com.uniandes.marketandes.ui.authentication.ui.AuthenticationViewModel
import com.uniandes.marketandes.ui.authentication.ui.RegistrationViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            MarketAndesApp(navController)

        }
    }
}

@Composable

fun MarketAndesApp(navControllerAuth: NavHostController) {
    //val navControllerAuth = rememberNavController()
    val viewModelAuth: AuthenticationViewModel = viewModel()
    val isAuthenticated by viewModelAuth.isAuthenticated.observeAsState(false)
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1500)
        isLoading = false
    }

    if (isLoading) {
        SplashScreen()
    }
    else
    {
        if (isAuthenticated)
        {

            MainScreen()
            Log.d("MarketAndesAppOLA", "isAuthenticated: $isAuthenticated")
            Log.d("MarketAndesApp", "isLoading: $isLoading")
            Log.d("MarketAndesApp", "navControllerAuth: $navControllerAuth")
        }
        else
        {
            AuthNavHost(navControllerAuth, isAuthenticated)
            ///MainScreen(navControllerAuth, isAuthenticated)
            Log.d("TRUEEEEE", "isAuthenticated: $isAuthenticated")
        }

    }


    Log.d("MarketAndesAppOLA", "isAuthenticated: $isAuthenticated")
    Log.d("MarketAndesApp", "isLoading: $isLoading")
    Log.d("MarketAndesApp", "navControllerAuth: $navControllerAuth")
    //MainScreen(navControllerMain)
}

@Composable
fun AuthNavHost(navController: NavHostController, isAuthenticated: Boolean)
{
    NavHost(
        navController = navController,
        startDestination = if (isAuthenticated) "mainSCreen" else "authentication"
    ) {
        composable("authentication") { AuthenticationScreen(viewModel = viewModel(), navController) }
        composable("register") { RegisterScreen(viewModel = viewModel(), navController) }
        composable("pag_home") { MainScreen() }

    }
    Log.d("AuthNavHost", "isAuthenticated: $isAuthenticated")
    Log.d("AuthNavHost", "navController: $navController")
}



@Composable
fun SplashScreen() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF00296B))
    ) {
        Image(
            painter = painterResource(id = R.drawable.market_andes_loading),
            contentDescription = "MarketAndes Logo",
            modifier = Modifier.size(300.dp)
        )
    }
}

@Composable
fun MainScreen(navController: NavHostController) {
    Scaffold(
        topBar = { HeaderBar() },
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHostContainer(navController, Modifier.padding(innerPadding))
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
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem("", R.drawable.shop_icon, "pag_comprar"),
        BottomNavItem("", R.drawable.add_icon, "pag_vender"),
        BottomNavItem("", R.drawable.home_icon, "pag_home"),
        BottomNavItem("", R.drawable.exchange_icon, "pag_intercambio"),
        BottomNavItem("", R.drawable.chat_icon, "pag_chat")
    )
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar(containerColor = Color(0xFF00296B)) {
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.title,
                        tint = if (currentRoute == item.route) Color(0xFFFDC500) else Color.White
                    )
                },
                label = {},
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFFDC500),
                    unselectedIconColor = Color.White,
                    indicatorColor = Color(0xFF00296B)
                )
            )
        }
    }
}

@Composable
fun NavHostContainer(navController: NavHostController, modifier: Modifier) {
    NavHost(navController, startDestination = "pag_home", modifier = modifier) {
        composable("authentication") { AuthenticationScreen(AuthenticationViewModel(), navController) }
        composable("register") { RegisterScreen(navController) }
        composable("pag_comprar") { PagComprar() }
        composable("pag_vender") { PagVender() }
        composable("pag_home") { PagHome() }
        composable("pag_intercambio") { PagIntercambio() }
        composable("pag_chat") { PagChat() }
    }
}

fun currentRoute(navController: NavHostController): String? {
    return navController.currentBackStackEntry?.destination?.route
}

data class BottomNavItem(val title: String, val icon: Int, val route: String)
