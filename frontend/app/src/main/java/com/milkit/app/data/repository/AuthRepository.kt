package com.milkit.app.data.repository

import com.milkit.app.data.local.TokenManager
import com.milkit.app.data.model.*
import com.milkit.app.data.network.ApiService
import com.milkit.app.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {

    suspend fun login(identifier: String, password: String): Flow<Resource<AuthResponse>> = flow {
        try {
            emit(Resource.Loading())
            
            val request = LoginRequest(identifier, password)
            val response = apiService.login(request)
            
            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse != null) {
                    // Save token and user data
                    tokenManager.saveToken(authResponse.token)
                    tokenManager.saveUserData(
                        authResponse.user.id,
                        authResponse.user.username,
                        authResponse.user.phone
                    )
                    emit(Resource.Success(authResponse))
                } else {
                    emit(Resource.Error("Login failed: Empty response"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                emit(Resource.Error("Login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        }
    }

    suspend fun signup(username: String, phone: String, password: String): Flow<Resource<AuthResponse>> = flow {
        try {
            emit(Resource.Loading())
            
            val request = SignupRequest(username, phone, password)
            val response = apiService.signup(request)
            
            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse != null) {
                    // Save token and user data
                    tokenManager.saveToken(authResponse.token)
                    tokenManager.saveUserData(
                        authResponse.user.id,
                        authResponse.user.username,
                        authResponse.user.phone
                    )
                    emit(Resource.Success(authResponse))
                } else {
                    emit(Resource.Error("Signup failed: Empty response"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                emit(Resource.Error("Signup failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        }
    }

    suspend fun logout(): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            
            // Call logout endpoint (optional, since JWT is stateless)
            apiService.logout()
            
            // Clear local data
            tokenManager.clearAll()
            
            emit(Resource.Success("Logged out successfully"))
        } catch (e: Exception) {
            // Even if API call fails, clear local data
            tokenManager.clearAll()
            emit(Resource.Success("Logged out successfully"))
        }
    }

    suspend fun getProfile(): Flow<Resource<User>> = flow {
        try {
            emit(Resource.Loading())
            
            val response = apiService.getProfile()
            
            if (response.isSuccessful) {
                val profileResponse = response.body()
                if (profileResponse?.user != null) {
                    // Update local user data
                    tokenManager.saveUserData(
                        profileResponse.user.id,
                        profileResponse.user.username,
                        profileResponse.user.phone
                    )
                    emit(Resource.Success(profileResponse.user))
                } else {
                    emit(Resource.Error("Failed to get profile: Empty response"))
                }
            } else {
                emit(Resource.Error("Failed to get profile: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        }
    }

    suspend fun updateProfile(username: String?, phone: String?): Flow<Resource<User>> = flow {
        try {
            emit(Resource.Loading())
            
            val request = UpdateProfileRequest(username, phone)
            val response = apiService.updateProfile(request)
            
            if (response.isSuccessful) {
                val profileResponse = response.body()
                if (profileResponse?.user != null) {
                    // Update local user data
                    tokenManager.saveUserData(
                        profileResponse.user.id,
                        profileResponse.user.username,
                        profileResponse.user.phone
                    )
                    emit(Resource.Success(profileResponse.user))
                } else {
                    emit(Resource.Error("Failed to update profile: Empty response"))
                }
            } else {
                emit(Resource.Error("Failed to update profile: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            
            val request = ChangePasswordRequest(currentPassword, newPassword)
            val response = apiService.changePassword(request)
            
            if (response.isSuccessful) {
                emit(Resource.Success("Password changed successfully"))
            } else {
                emit(Resource.Error("Failed to change password: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        }
    }

    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()
    
    fun isLoggedInFlow(): Flow<Boolean> = tokenManager.isLoggedInFlow()
    
    fun getCurrentUserId(): String? = tokenManager.getUserId()
    
    fun getCurrentUsername(): String? = tokenManager.getUsername()
    
    fun getCurrentPhone(): String? = tokenManager.getPhone()
}
