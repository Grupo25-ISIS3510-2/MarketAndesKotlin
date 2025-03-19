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



