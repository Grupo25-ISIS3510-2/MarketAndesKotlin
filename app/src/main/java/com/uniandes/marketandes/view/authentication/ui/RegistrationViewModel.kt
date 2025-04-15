package com.uniandes.marketandes.view.authentication.ui

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RegistrationViewModel : ViewModel()
{

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> = _email

    private val _password = MutableLiveData<String>()
    val password: LiveData<String> = _password

    private val _confirmPassword = MutableLiveData<String>()
    val confirmPassword: LiveData<String> = _confirmPassword

    private val _registerEnable = MutableLiveData<Boolean>()
    val registerEnable: LiveData<Boolean> = _registerEnable

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isRegistered = MutableLiveData(false)
    val isRegistered: LiveData<Boolean> = _isRegistered

    private val _registerError = MutableLiveData<String?>()
    val registerError: LiveData<String?> = _registerError

    fun onRegisterChange(email: String, password: String, confirmPassword: String) {
        _email.value = email
        _password.value = password
        _confirmPassword.value = confirmPassword

        _registerEnable.value = isValidEmail(email) && isValidPassword(password) &&
                password == confirmPassword
    }

    fun onRegisterSelected(home: () -> Unit)
    {
        _isLoading.value = true
        viewModelScope.launch {
            try
            {
                val emailValue = _email.value ?: ""
                val passwordValue = _password.value ?: ""

                auth.createUserWithEmailAndPassword(emailValue, passwordValue).await()

                Log.d("MarketAndesRegister", "Usuario registrado con éxito!")
                _isRegistered.value = true
                home()
            }
            catch (e: Exception)
            {
                Log.e("MarketAndesRegister", "Error en registro: ${e.message}")
                _registerError.value = e.message
            }
            finally
            {
                _isLoading.value = false
            }
        }
    }

    fun clearError()
    {
        _registerError.value = null
    }

    private fun isValidEmail(email: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.endsWith("@uniandes.edu.co")

    private fun isValidPassword(password: String): Boolean = password.length >= 6
}
