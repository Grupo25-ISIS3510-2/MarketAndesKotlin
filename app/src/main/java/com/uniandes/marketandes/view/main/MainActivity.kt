package com.uniandes.marketandes.view.main

import RegisterScreen
import com.uniandes.marketandes.view.preferences.UserPreferencesViewModel
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.Manifest
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.uniandes.marketandes.R
import kotlinx.coroutines.delay
import com.uniandes.marketandes.view.authentication.ui.AuthenticationScreen
import com.uniandes.marketandes.view.authentication.ui.AuthenticationViewModel
import com.uniandes.marketandes.view.preferences.FacultySelectionScreen
import com.uniandes.marketandes.view.preferences.InterestSelectionScreen

class MainActivity : FragmentActivity() {
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            } else {
                MarketAndesApp(navController)

            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions as Array<String>, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setContent {
                        val navController = rememberNavController()
                        MarketAndesApp(navController)
                    }
                } else {
                    Toast.makeText(this, "Se requieren permisos de ubicación", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

@Composable
fun MarketAndesApp(navControllerAuth: NavHostController) {
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
        composable("faculty_selection") { FacultySelectionScreen( navController, UserPreferencesViewModel(), false, emptyList()) }
        composable("interest_selection") { InterestSelectionScreen( navController, UserPreferencesViewModel(), false, emptyList()) }

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