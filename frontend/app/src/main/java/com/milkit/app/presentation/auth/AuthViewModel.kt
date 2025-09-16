package com.milkit.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milkit.app.data.model.AuthResponse
import com.milkit.app.data.repository.AuthRepository
import com.milkit.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<AuthResponse>?>(null)
    val loginState: StateFlow<Resource<AuthResponse>?> = _loginState.asStateFlow()

    private val _signupState = MutableStateFlow<Resource<AuthResponse>?>(null)
    val signupState: StateFlow<Resource<AuthResponse>?> = _signupState.asStateFlow()

    private val _logoutState = MutableStateFlow<Resource<String>?>(null)
    val logoutState: StateFlow<Resource<String>?> = _logoutState.asStateFlow()

    fun login(identifier: String, password: String) {
        viewModelScope.launch {
            authRepository.login(identifier, password).collect { result ->
                _loginState.value = result
            }
        }
    }

    fun signup(username: String, phone: String, password: String) {
        viewModelScope.launch {
            authRepository.signup(username, phone, password).collect { result ->
                _signupState.value = result
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout().collect { result ->
                _logoutState.value = result
            }
        }
    }

    fun clearLoginState() {
        _loginState.value = null
    }

    fun clearSignupState() {
        _signupState.value = null
    }

    fun clearLogoutState() {
        _logoutState.value = null
    }

    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()
}
