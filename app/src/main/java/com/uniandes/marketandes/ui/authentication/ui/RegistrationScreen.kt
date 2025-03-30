import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.uniandes.marketandes.R
import com.uniandes.marketandes.ui.authentication.ui.RegistrationViewModel

@Composable
fun RegisterScreen(viewModel: RegistrationViewModel, navController: NavHostController) {
    val email by viewModel.email.observeAsState("")
    val password by viewModel.password.observeAsState("")
    val confirmPassword by viewModel.confirmPassword.observeAsState("")
    val registerEnable by viewModel.registerEnable.observeAsState(false)
    val isLoading by viewModel.isLoading.observeAsState(false)
    val registerError by viewModel.registerError.observeAsState(null)

    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Selecciona una categoría") }
    val categories = listOf("Ciencias", "Tecnología", "Lenguas", "Arquitectura")

    val focusManager = LocalFocusManager.current // Manejo del foco
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF00205B)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.market_andes_logo1),
                    contentDescription = "Logo",
                    modifier = Modifier.size(220.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))

                InputField(
                    label = "Email",
                    value = email,
                    imeAction = ImeAction.Next,
                    focusRequester = emailFocusRequester,
                    onNext = { passwordFocusRequester.requestFocus() }
                ) {
                    viewModel.onRegisterChange(it, password, confirmPassword, selectedCategory)
                }

                Spacer(modifier = Modifier.height(8.dp))

                InputField(
                    label = "Contraseña",
                    value = password,
                    isPassword = true,
                    imeAction = ImeAction.Next,
                    focusRequester = passwordFocusRequester,
                    onNext = { confirmPasswordFocusRequester.requestFocus() }
                ) {
                    viewModel.onRegisterChange(email, it, confirmPassword, selectedCategory)
                }

                Spacer(modifier = Modifier.height(8.dp))

                InputField(
                    label = "Confirmar contraseña",
                    value = confirmPassword,
                    isPassword = true,
                    imeAction = ImeAction.Done,
                    focusRequester = confirmPasswordFocusRequester,
                    onDone = { focusManager.clearFocus() }
                ) {
                    viewModel.onRegisterChange(email, password, it, selectedCategory)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dropdown para seleccionar la categoría
                Box(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedCategory)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (registerError != null) {
                    Text(text = registerError!!, color = Color.Red, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                navController.addOnDestinationChangedListener { controller, destination, arguments ->
                    Log.d("NavController", "Navegando a: ${destination.route}")
                }

                Button(
                    onClick = { viewModel.onRegisterSelected { navController.navigate("pag_home") } },
                    enabled = registerEnable && selectedCategory != "Selecciona una categoría de preferencia",
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00205B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Crear cuenta", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun InputField(
    label: String,
    value: String,
    isPassword: Boolean = false,
    imeAction: ImeAction = ImeAction.Done,
    focusRequester: FocusRequester = remember { FocusRequester() },
    onNext: (() -> Unit)? = null,
    onDone: (() -> Unit)? = null,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontWeight = FontWeight.Bold) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onNext = { onNext?.invoke() },
            onDone = { onDone?.invoke() }
        )
    )
}