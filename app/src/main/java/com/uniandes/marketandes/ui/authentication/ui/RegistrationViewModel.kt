package com.uniandes.marketandes.ui.authentication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class RegistrationViewModel : ViewModel()
{
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun registerUser(email: String, password: String, onResult: (Boolean, String?) -> Unit)
    {
        viewModelScope.launch {
            try
            {
                auth.createUserWithEmailAndPassword(email, password).await()
                onResult(true, null) // Éxito
            }
            catch (e: Exception)
            {
                onResult(false, e.message) // Error
            }
        }
    }
}
