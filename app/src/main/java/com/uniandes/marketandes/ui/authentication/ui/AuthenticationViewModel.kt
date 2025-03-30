package com.uniandes.marketandes.ui.authentication.ui

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

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

    fun onLoginChange(email: String, password: String) {
        _email.value = email
        _password.value = password
        _loginEnable.value = isValidEmail(email) && isValidPassword(password)
        _loginError.value = null
    }

    fun onLoginSelected(home: () -> Unit)
    {
        _isLoading.value = true
        viewModelScope.launch {
            try
            {
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




    fun logout() {
        auth.signOut()
        _isAuthenticated.value = false  // 🔴 Asegura que cuando cierre sesión, se actualice el estado
    }

    fun clearError() {
        _loginError.value = null
    }

    private fun isValidEmail(email: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.endsWith("@uniandes.edu.co")

    private fun isValidPassword(password: String): Boolean = password.length >= 6
}