package com.milkit.app.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: String,
    val username: String,
    val phone: String,
    val createdAt: String,
    val lastLogin: String? = null
) : Parcelable

@Parcelize
data class AuthResponse(
    val message: String,
    val user: User,
    val token: String
) : Parcelable

@Parcelize
data class LoginRequest(
    val identifier: String, // username or phone
    val password: String
) : Parcelable

@Parcelize
data class SignupRequest(
    val username: String,
    val phone: String,
    val password: String
) : Parcelable

@Parcelize
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
) : Parcelable

@Parcelize
data class UpdateProfileRequest(
    val username: String? = null,
    val phone: String? = null
) : Parcelable
