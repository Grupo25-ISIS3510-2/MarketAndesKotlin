package com.uniandes.marketandes.ui.authentication.ui

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.uniandes.marketandes.R
import kotlinx.coroutines.launch

@Composable
fun AuthenticationScreen(viewModel: AuthenticationViewModel, navController: NavHostController) {
    val isLoading by viewModel.isLoading.observeAsState(false)
    var viewModelReg = RegistrationViewModel()
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(modifier = Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures(onTap = {keyboardController?.hide()})}) {
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
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .imePadding()
                    .windowInsetsPadding(WindowInsets.ime),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.market_andes_logo1),
                    contentDescription = "MarketAndes Logo",
                    modifier = Modifier.size(280.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Login(Modifier.align(Alignment.CenterHorizontally), viewModel, navController)
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
                Register(viewModelReg, navController)
            }
        }
    }
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route

    Log.d("NavController", "Destino actual: $currentDestination")
}

@Composable
fun Register(viewModelReg: RegistrationViewModel, navController: NavHostController) {
    val registerEnable by viewModelReg.registerEnable.observeAsState(false)
    val coroutineScope = rememberCoroutineScope()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        RegistrationButton(registerEnable, navController)
    }
}

@Composable
fun Login(modifier: Modifier, viewModel: AuthenticationViewModel, navController: NavHostController) {
    val email by viewModel.email.observeAsState("")
    val password by viewModel.password.observeAsState("")
    val loginEnable by viewModel.loginEnable.observeAsState(false)
    val loginError by viewModel.loginError.observeAsState(null)
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current // Manejo de foco


    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        EmailField(email, { viewModel.onLoginChange(it, password) }, focusManager)
        Spacer(modifier = Modifier.height(16.dp))
        PasswordField(password, { viewModel.onLoginChange(email, it) }, focusManager)

        if (loginError != null)
        {
            Text(
                text = loginError!!,
                color = Color.Red,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }




        Spacer(modifier = Modifier.height(8.dp))
        ForgotPassword(viewModel)
        Spacer(modifier = Modifier.height(40.dp))
        LoginButton(loginEnable) {
            coroutineScope.launch { viewModel.onLoginSelected {navController.navigate("pag_home")} }
        }
    }
}

@Composable
fun RegistrationButton(registerEnable: Boolean, navController: NavHostController) {

    navController.addOnDestinationChangedListener { controller, destination, arguments ->
        Log.d("NavController", "Navegando a: ${destination.route}")
    }

    TextButton(
        onClick = {  // Elimina la pantalla actual del stack
            navController.navigate("register") }, // Aquí usamos la función pasada como parámetro
    ) {
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
fun PasswordField(password: String, onTextFieldChanged: (String) -> Unit, focusManager: FocusManager) {
    var passwordVisible = remember { mutableStateOf(false) }

    TextField(
        value = password,
        onValueChange = { onTextFieldChanged(it) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 31.dp),
        placeholder = { Text("Contraseña") },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() }
        ),
        singleLine = true,
        visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                Icon(
                    painter = painterResource(id = if (passwordVisible.value) R.drawable.vista else R.drawable.visible),
                    contentDescription = if (passwordVisible.value) "Ocultar contraseña" else "Mostrar contraseña",
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        colors = TextFieldDefaults.textFieldColors()
    )
}


@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
fun EmailField(email: String, onTextFieldChanged: (String) -> Unit, focusManager: FocusManager) {
    TextField(
        value = email,
        onValueChange = { onTextFieldChanged(it) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 31.dp),
        placeholder = { Text("Correo electrónico Uniandes") },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        ),
        singleLine = true,
        colors = TextFieldDefaults.textFieldColors()
    )
}

@Composable
fun ForgotPassword(viewModel: AuthenticationViewModel)
{
    Text(
        text = "¿Olvidaste tu contraseña?",
        modifier = Modifier.clickable {
            viewModel.forgotPassword(viewModel.email.value ?: "")
        },
        fontSize = 12.sp,
        color = Color(0xFF001F5B),
        fontWeight = FontWeight.Bold
    )
}