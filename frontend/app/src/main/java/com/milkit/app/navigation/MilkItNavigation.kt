package com.milkit.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.milkit.app.data.repository.AuthRepository
import com.milkit.app.presentation.auth.AuthViewModel
import com.milkit.app.presentation.auth.LoginScreen
import com.milkit.app.presentation.auth.SignupScreen
import com.milkit.app.presentation.main.MainScreen
import javax.inject.Inject

@Composable
fun MilkItNavigation(
    navController: NavHostController = rememberNavController()
) {
    val authRepository: AuthRepository = hiltViewModel<AuthViewModel>().let { 
        // Get the repository from the ViewModel (this is a workaround for Hilt injection in Composables)
        // In a real app, you might want to use a different approach
        return@let authRepository
    }
    
    // Check if user is logged in
    val isLoggedIn by authRepository.isLoggedInFlow().collectAsState(initial = authRepository.isLoggedIn())

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) Screen.Main.route else Screen.Login.route
    ) {
        // Authentication screens
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToSignup = {
                    navController.navigate(Screen.Signup.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Signup.route) {
            SignupScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                },
                onSignupSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                }
            )
        }

        // Main app screen (contains bottom navigation)
        composable(Screen.Main.route) {
            MainScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Main : Screen("main")
}
