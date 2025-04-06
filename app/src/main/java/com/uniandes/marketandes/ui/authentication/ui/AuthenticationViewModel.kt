package com.uniandes.marketandes.ui.authentication.ui

import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey


class AuthenticationViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> = _email

    private val _password = MutableLiveData<String>()
    val password: LiveData<String> = _password

    private val _loginEnable = MutableLiveData<Boolean>()
    val loginEnable: LiveData<Boolean> = _loginEnable

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isAuthenticated = MutableLiveData(false)
    val isAuthenticated: LiveData<Boolean> = _isAuthenticated

    private val _loginError = MutableLiveData<String?>()
    val loginError: LiveData<String?> = _loginError


    init {
        val currentUser = auth.currentUser
        _isAuthenticated.value = currentUser != null
        Log.d("MarketAndes", "Usuario autenticado al iniciar: ${currentUser?.email}")

    }


    fun onLoginChange(email: String, password: String) {
        _email.value = email
        _password.value = password
        _loginEnable.value = isValidEmail(email) && isValidPassword(password)
        _loginError.value = null
    }

    fun onLoginSelected(context: Context, home: () -> Unit)
    {
        _isLoading.value = true
        viewModelScope.launch {
            try
            {

                val sharedPreferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                sharedPreferences.edit().putBoolean("usuario_autenticado", true).apply()


                auth.signInWithEmailAndPassword(_email.value ?: "", _password.value ?: "")
                    .addOnCompleteListener { task ->
                        _isLoading.value = false
                        if (task.isSuccessful)
                        {

                            Log.d("MarketAndesLogin", "signInWithEmailAndPassword: Logueado!!")

                            val user = FirebaseAuth.getInstance().currentUser
                            Log.d("MarketAndesLogin", "signInWithEmailAndPassword: ${user?.email}")
                            _isAuthenticated.value = true
                            home()
                            saveCredentialSafety(context, _email.value ?: "", _password.value ?: "")

                        } else
                        {
                            Log.d("MarketAndesLogin", "signInWithEmailAndPassword: ${task.exception?.message}")
                            _loginError.value = task.exception?.message
                        }
                    }
            }
            catch (e: Exception)
            {
                _isLoading.value = false
                Log.e("MarketAndesLogin", "Error en login: ${e.message}")
                _loginError.value = e.message
            }
        }
    }

    fun forgotPassword(email: String)
    {
        if (email.isNotEmpty())
        {
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _loginError.value = "Correo de recuperación enviado a $email"
                    } else {
                        _loginError.value = task.exception?.message ?: "Error al enviar correo"
                    }
                }
        }
        else
        {
            _loginError.value = "Por favor, ingresa tu correo"
        }
    }


    fun saveCredentialSafety(context: Context, email: String, password: String)
    {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "credentials",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        sharedPreferences.edit()
            .putString("email", email)
            .putString("password", password)
            .apply()

        Log.d("MarketAndesDebug", "Credenciales guardadas: $email / $password")


    }


    fun getCredentialSafety(context: Context): Pair<String, String>? {
        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "credentials",
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val email = sharedPreferences.getString("email", null)
        val password = sharedPreferences.getString("password", null)

        Log.d("MarketAndesDebug", "Credenciales obtenidas: $email / $password")


        return if (!email.isNullOrBlank() && !password.isNullOrBlank()) {
            Pair(email, password)
        } else {
            null
        }
    }



    fun loginWithStoredCredentials(context: Context, home: () -> Unit)
    {
        val credentials = getCredentialSafety(context)

        if (credentials != null) {
            val (email, password) = credentials

            Log.d("MarketAndesDebug", "Intentando login con credenciales almacenadas: $email / $password")

            _isLoading.value = true
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    _isLoading.value = false
                    if (task.isSuccessful) {
                        Log.d("MarketAndesLogin", "Login con huella exitoso: $email")
                        _isAuthenticated.value = true
                        home()
                    } else {
                        Log.e("MarketAndesLogin", "Error en login con huella: ${task.exception?.message}")
                        _loginError.value = task.exception?.message
                    }
                }
        } else {
            Log.e("MarketAndesLogin", "No se encontraron credenciales almacenadas")
            _loginError.value = "No se encontraron credenciales guardadas"
        }
    }




    private fun isValidEmail(email: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.endsWith("@uniandes.edu.co")

    private fun isValidPassword(password: String): Boolean = password.length >= 6
}