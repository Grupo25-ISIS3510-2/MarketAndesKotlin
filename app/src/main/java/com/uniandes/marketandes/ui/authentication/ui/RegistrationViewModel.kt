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

class RegistrationViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth

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
        _registerEnable.value = isValidEmail(email) && isValidPassword(password) && password == confirmPassword
    }

    fun onRegisterSelected(home: () -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(_email.value ?: "", _password.value ?: "")
                    .addOnCompleteListener { task ->
                        _isLoading.value = false
                        if (task.isSuccessful) {
                            Log.d("MarketAndesRegister", "createUserWithEmailAndPassword: Registrado exitosamente!")
                            _isRegistered.value = true
                            val user = FirebaseAuth.getInstance().currentUser
                            Log.d("MarketAndesRegister", "signInWithEmailAndPassword: ${user?.email}")
                            home()
                        } else {
                            Log.d("MarketAndesRegister", "createUserWithEmailAndPassword: ${task.exception?.message}")
                            _registerError.value = task.exception?.message
                        }
                    }
            } catch (e: Exception) {
                _isLoading.value = false
                Log.e("MarketAndesRegister", "Error en registro: ${e.message}")
                _registerError.value = e.message
            }
        }
    }

    fun clearError() {
        _registerError.value = null
    }

    private fun isValidEmail(email: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.endsWith("@uniandes.edu.co")

    private fun isValidPassword(password: String): Boolean = password.length >= 6
}
