package com.heallens.android.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Verification : Screen("verification/{email}") {
        fun createRoute(email: String) = "verification/$email"
    }
    object ForgotPassword : Screen("forgot_password")
    object ResetPassword : Screen("reset_password")
    object Dashboard : Screen("dashboard")
    object Scanner : Screen("scanner")
    object ReportAnalyzer : Screen("report_analyzer")
    object History : Screen("history")
    object Chatbot : Screen("chatbot")
    object Emergency : Screen("emergency")
    object Profile : Screen("profile")
}

