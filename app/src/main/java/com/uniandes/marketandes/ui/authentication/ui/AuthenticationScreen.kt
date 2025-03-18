package com.uniandes.marketandes.ui.authentication.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.uniandes.marketandes.R
import kotlinx.coroutines.launch

@Composable
fun AuthenticationScreen(viewModel: AuthenticationViewModel, navController: NavHostController)
{
    val isLoading by viewModel.isLoading.observeAsState(false)

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x80001F5B)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(64.dp))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.market_andes_logo1),
                    contentDescription = "MarketAndes Logo",
                    modifier = Modifier.size(280.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Login(Modifier.align(Alignment.CenterHorizontally), viewModel)
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "¡Al hacer clic en 'Iniciar sesión', aceptas el tratamiento de tus datos para ofrecerte una mejor experiencia!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF001F5B),
                    modifier = Modifier.padding(horizontal = 32.dp),
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(25.dp))
                RegisterButton(navController)
            }
        }
    }
}

@Composable
fun RegisterButton(navController: NavHostController)
{
    TextButton(onClick = { navController.navigate("pag_home") })
    {
        Text(
            "¿Primera vez? Regístrate aquí",
            color = Color(0xFF001F5B),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textDecoration = TextDecoration.Underline
        )
    }
}

@Composable
fun Login(modifier: Modifier, viewModel: AuthenticationViewModel) {
    val email by viewModel.email.observeAsState("")
    val password by viewModel.password.observeAsState("")
    val loginEnable by viewModel.loginEnable.observeAsState(false)
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        EmailField(email) { viewModel.onLoginChange(it, password) }
        Spacer(modifier = Modifier.height(16.dp))
        PasswordField(password) { viewModel.onLoginChange(email, it) }
        Spacer(modifier = Modifier.height(8.dp))
        ForgotPassword()
        Spacer(modifier = Modifier.height(40.dp))
        LoginButton(loginEnable) {
            coroutineScope.launch { viewModel.onLoginSelected {} }
        }
    }
}

@Composable
fun LoginButton(loginEnable: Boolean, onLoginSelected: () -> Unit) {
    Button(
        onClick = { onLoginSelected() },
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF001F5B)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 31.dp),
        enabled = loginEnable
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.uniandes_logo),
                contentDescription = "Uniandes Logo",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "INICIAR SESIÓN CON CUENTA UNIANDES",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PasswordField(password: String, onTextFieldChanged: (String) -> Unit) {
    TextField(
        value = password,
        onValueChange = { onTextFieldChanged(it) },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 31.dp),
        placeholder = { Text("Contraseña") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        colors = TextFieldDefaults.textFieldColors()
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun EmailField(email: String, onTextFieldChanged: (String) -> Unit) {
    TextField(
        value = email,
        onValueChange = { onTextFieldChanged(it) },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 31.dp),
        placeholder = { Text("Correo electrónico Uniandes") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        singleLine = true,
        colors = TextFieldDefaults.textFieldColors()
    )
}

@Composable
fun ForgotPassword() {
    Text(
        text = "¿Olvidaste tu contraseña?",
        modifier = Modifier.clickable { },
        fontSize = 12.sp,
        color = Color(0xFF001F5B),
        fontWeight = FontWeight.Bold
    )
}
