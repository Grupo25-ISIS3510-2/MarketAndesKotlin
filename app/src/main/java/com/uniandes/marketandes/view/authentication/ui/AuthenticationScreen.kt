package com.uniandes.marketandes.view.authentication.ui

import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.uniandes.marketandes.R
import kotlinx.coroutines.launch

@Composable
fun AuthenticationScreen(viewModel: AuthenticationViewModel, navController: NavHostController)
{
    val isLoading by viewModel.isLoading.observeAsState(false)
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser

    LaunchedEffect(Unit) {
        if (currentUser == null)
        {
            val credenciales = viewModel.getCredentialSafety(context)
            if (credenciales != null)
            {
                (context as? FragmentActivity)?.let { activity ->
                    showBiometricPrompt(activity, viewModel)
                    {
                        navController.navigate("pag_home")
                        {
                            popUpTo("authentication") { inclusive = true }
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures(onTap = { keyboardController?.hide() }) }
    ) {
        if (isLoading)
        {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x80001F5B)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(64.dp))
            }
        }
        else
        {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .imePadding(),
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

                if (currentUser == null)
                {
                    Button(
                        onClick = {
                            (context as? FragmentActivity)?.let { activity ->
                                showBiometricPrompt(activity, viewModel){
                                    navController.navigate("pag_home") {
                                        popUpTo("authentication") { inclusive = true }
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF001F5B))
                    )
                    {
                        Text("Iniciar con Huella", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(25.dp))
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
                RegistrationButton(navController)
            }
        }
    }
}

@Composable
fun Login(modifier: Modifier, viewModel: AuthenticationViewModel, navController: NavHostController)
{
    val email by viewModel.email.observeAsState("")
    val password by viewModel.password.observeAsState("")
    val loginEnable by viewModel.loginEnable.observeAsState(false)
    val loginError by viewModel.loginError.observeAsState(null)
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        EmailField(email, { viewModel.onLoginChange(it, password) }, focusManager)
        Spacer(modifier = Modifier.height(16.dp))
        PasswordField(password, { viewModel.onLoginChange(email, it) }, focusManager)

        loginError?.let {
            Text(
                text = it,
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
            coroutineScope.launch {
                viewModel.onLoginSelected(context) {
                    navController.navigate("pag_home")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailField(email: String, onTextFieldChanged: (String) -> Unit, focusManager: FocusManager)
{
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordField(password: String, onTextFieldChanged: (String) -> Unit, focusManager: FocusManager)
{
    var passwordVisible by remember { mutableStateOf(false) }

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
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    painter = painterResource(id = if (passwordVisible) R.drawable.vista else R.drawable.visible),
                    contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        colors = TextFieldDefaults.textFieldColors()
    )
}

@Composable
fun LoginButton(loginEnable: Boolean, onLoginSelected: () -> Unit)
{
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
}

@Composable
fun RegistrationButton(navController: NavHostController)
{
    TextButton(onClick = { navController.navigate("register") })
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
fun ForgotPassword(viewModel: AuthenticationViewModel)
{
    Text(
        text = "¿Olvidaste tu contraseña?",
        modifier = Modifier.clickable
        {
            viewModel.forgotPassword(viewModel.email.value ?: "")
        },
        fontSize = 12.sp,
        color = Color(0xFF001F5B),
        fontWeight = FontWeight.Bold
    )
}

fun showBiometricPrompt(activity: FragmentActivity, viewModel: AuthenticationViewModel, onSuccess: () -> Unit) {
    val biometricManager = BiometricManager.from(activity)
    when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
        BiometricManager.BIOMETRIC_SUCCESS -> {
            val executor = ContextCompat.getMainExecutor(activity)
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Autenticación con huella")
                .setSubtitle("Usa tu huella para acceder")
                .setNegativeButtonText("Cancelar")
                .setConfirmationRequired(true)
                .build()

            val biometricPrompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)

                        val credenciales = viewModel.getCredentialSafety(activity)
                        if (credenciales != null) {
                            val (email, password) = credenciales
                            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(activity, "Autenticación exitosa", Toast.LENGTH_SHORT).show()
                                        viewModel.loginWithStoredCredentials(activity) {
                                            Toast.makeText(activity, "Autenticación exitosa", Toast.LENGTH_SHORT).show()
                                            onSuccess()
                                        }
                                    } else {
                                        Toast.makeText(activity, "No tienes conexión, inténtalo nuevamente más tarde", Toast.LENGTH_LONG).show()
                                    }
                                }
                        } else {
                            Toast.makeText(activity, "No se encontraron credenciales guardadas", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        Toast.makeText(activity, "Error: $errString", Toast.LENGTH_SHORT).show()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Toast.makeText(activity, "Huella no reconocida", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            biometricPrompt.authenticate(promptInfo)
        }
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
            Toast.makeText(activity, "Este dispositivo no tiene soporte para huella", Toast.LENGTH_SHORT).show()
        }
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
            Toast.makeText(activity, "El sensor de huella no está disponible", Toast.LENGTH_SHORT).show()
        }
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
            Toast.makeText(activity, "No hay huellas registradas", Toast.LENGTH_SHORT).show()
        }
        else -> {
            Toast.makeText(activity, "No se pudo autenticar", Toast.LENGTH_SHORT).show()
        }
    }
}
