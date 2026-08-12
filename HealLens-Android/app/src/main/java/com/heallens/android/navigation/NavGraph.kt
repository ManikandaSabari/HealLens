package com.heallens.android.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.heallens.android.ui.auth.ForgotPasswordScreen
import com.heallens.android.ui.auth.ForgotPasswordViewModel
import com.heallens.android.ui.auth.LoginScreen
import com.heallens.android.ui.auth.LoginViewModel
import com.heallens.android.ui.auth.ResetPasswordScreen
import com.heallens.android.ui.auth.ResetPasswordViewModel
import com.heallens.android.ui.auth.SignupScreen
import com.heallens.android.ui.auth.SignupViewModel
import com.heallens.android.ui.auth.VerificationScreen
import com.heallens.android.ui.auth.VerificationViewModel
import com.heallens.android.ui.main.MainShellScreen
import com.heallens.android.ui.main.MainShellViewModel
import com.heallens.android.ui.profile.ProfileViewModel
import com.heallens.android.ui.splash.SplashScreen
import com.heallens.android.ui.splash.SplashViewModel
import com.heallens.android.ui.theme.DarkBackground
import com.heallens.android.ui.theme.TextPrimary

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            val viewModel: SplashViewModel = viewModel()
            SplashScreen(
                viewModel = viewModel,
                onNavigateNext = { targetRoute ->
                    navController.navigate(targetRoute) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Login Screen
        composable(Screen.Login.route) {
            val viewModel: LoginViewModel = viewModel()
            LoginScreen(
                viewModel = viewModel,
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignup = {
                    navController.navigate(Screen.Signup.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }

        // Signup Screen
        composable(Screen.Signup.route) {
            val viewModel: SignupViewModel = viewModel()
            SignupScreen(
                viewModel = viewModel,
                onNavigateToVerification = { email ->
                    navController.navigate(Screen.Verification.createRoute(email)) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // Verification Screen
        composable(
            route = Screen.Verification.route,
            arguments = listOf(
                navArgument("email") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val viewModel: VerificationViewModel = viewModel()
            VerificationScreen(
                email = email,
                viewModel = viewModel,
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Forgot Password Screen
        composable(Screen.ForgotPassword.route) {
            val viewModel: ForgotPasswordViewModel = viewModel()
            ForgotPasswordScreen(
                viewModel = viewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // Reset Password Screen
        composable(Screen.ResetPassword.route) {
            val viewModel: ResetPasswordViewModel = viewModel()
            ResetPasswordScreen(
                viewModel = viewModel,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Main Application Shell & Dashboard (Phase 3)
        composable(Screen.Dashboard.route) {
            val mainShellViewModel: MainShellViewModel = viewModel()
            val profileViewModel: ProfileViewModel = viewModel()
            MainShellScreen(
                viewModel = mainShellViewModel,
                profileViewModel = profileViewModel,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = TextPrimary
        )
    }
}
