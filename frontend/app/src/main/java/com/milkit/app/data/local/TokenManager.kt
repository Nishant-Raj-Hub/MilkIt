package com.milkit.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val tokenKey = stringPreferencesKey("auth_token")
    private val userIdKey = stringPreferencesKey("user_id")
    private val usernameKey = stringPreferencesKey("username")
    private val phoneKey = stringPreferencesKey("phone")

    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[tokenKey] = token
        }
    }

    suspend fun saveUserData(userId: String, username: String, phone: String) {
        context.dataStore.edit { preferences ->
            preferences[userIdKey] = userId
            preferences[usernameKey] = username
            preferences[phoneKey] = phone
        }
    }

    fun getToken(): String? {
        return runBlocking {
            context.dataStore.data.first()[tokenKey]
        }
    }

    fun getTokenFlow(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[tokenKey]
        }
    }

    fun getUserId(): String? {
        return runBlocking {
            context.dataStore.data.first()[userIdKey]
        }
    }

    fun getUsername(): String? {
        return runBlocking {
            context.dataStore.data.first()[usernameKey]
        }
    }

    fun getPhone(): String? {
        return runBlocking {
            context.dataStore.data.first()[phoneKey]
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    fun isLoggedInFlow(): Flow<Boolean> {
        return getTokenFlow().map { token ->
            token != null
        }
    }
}
