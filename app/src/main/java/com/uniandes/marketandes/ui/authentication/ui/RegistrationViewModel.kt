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

    private val _selectedCategory = MutableLiveData<String?>()
    val selectedCategory: LiveData<String?> = _selectedCategory

    private val _registerEnable = MutableLiveData<Boolean>()
    val registerEnable: LiveData<Boolean> = _registerEnable

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isRegistered = MutableLiveData(false)
    val isRegistered: LiveData<Boolean> = _isRegistered

    private val _registerError = MutableLiveData<String?>()
    val registerError: LiveData<String?> = _registerError

    fun onRegisterChange(email: String, password: String, confirmPassword: String, category: String?) {
        _email.value = email
        _password.value = password
        _confirmPassword.value = confirmPassword
        _selectedCategory.value = category

        _registerEnable.value = isValidEmail(email) && isValidPassword(password) &&
                password == confirmPassword && !category.isNullOrEmpty()
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
        _registerEnable.value = isValidEmail(_email.value ?: "") &&
                isValidPassword(_password.value ?: "") &&
                _password.value == _confirmPassword.value &&
                !category.isNullOrEmpty()
    }

    fun onRegisterSelected(home: () -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(_email.value ?: "", _password.value ?: "")
                    .addOnCompleteListener { task ->
                        _isLoading.value = false
                        if (task.isSuccessful) {
                            Log.d("MarketAndesRegister", "Usuario registrado con éxito!")
                            _isRegistered.value = true
                            home()
                        } else {
                            Log.d("MarketAndesRegister", "Error en registro: ${task.exception?.message}")
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