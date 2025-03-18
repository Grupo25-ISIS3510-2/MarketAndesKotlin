package com.uniandes.marketandes.ui.authentication.ui

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class AuthenticationViewModel : ViewModel()
{

    private val auth: FirebaseAuth = Firebase.auth
    private val _loading = MutableLiveData(false)

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

    fun onLoginChange(email: String, password: String)
    {
        _email.value = email
        _password.value = password
        _loginEnable.value = isValidEmail(email) && isValidPassword(password)
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

    fun clearError()
    {
        _loginError.value = null
    }

    private fun isValidEmail(email: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.endsWith("@uniandes.edu.co")

    private fun isValidPassword(password: String): Boolean = password.length >= 6

}
