package com.heallens.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.heallens.android.data.local.DataStoreManager
import com.heallens.android.data.repository.AuthRepository
import com.heallens.android.data.repository.AuthRepositoryImpl
import com.heallens.android.navigation.NavGraph
import com.heallens.android.navigation.Screen
import com.heallens.android.ui.theme.DarkBackground
import com.heallens.android.ui.theme.HealLensTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.heallens.android.ui.components.MedicalDisclaimerDialog
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var authRepository: AuthRepository
    private val startDestinationState = mutableStateOf(Screen.Splash.route)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataStoreManager = DataStoreManager(applicationContext)
        authRepository = AuthRepositoryImpl(dataStoreManager = dataStoreManager)

        handleDeepLinkIntent(intent)

        setContent {
            val disclaimerAccepted by dataStoreManager.disclaimerAcceptedFlow.collectAsState(initial = true)

            HealLensTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        startDestination = startDestinationState.value
                    )

                    if (!disclaimerAccepted) {
                        MedicalDisclaimerDialog(
                            onAgree = {
                                lifecycleScope.launch {
                                    dataStoreManager.saveDisclaimerAccepted(true)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLinkIntent(intent)
    }

    private fun handleDeepLinkIntent(intent: Intent?) {
        val data: Uri = intent?.data ?: return
        val uriStr = data.toString()

        val isRecovery = uriStr.contains("type=recovery") || uriStr.contains("reset-password") || uriStr.contains("recovery")

        // Extract access_token and refresh_token from query or fragment anchor (#access_token=...&refresh_token=...)
        val fragment = data.fragment ?: ""
        var accessToken: String? = data.getQueryParameter("access_token")
        var refreshToken: String? = data.getQueryParameter("refresh_token")

        if (accessToken.isNullOrEmpty() && fragment.contains("access_token=")) {
            fragment.split("&").forEach { param ->
                val parts = param.split("=")
                if (parts.size == 2) {
                    if (parts[0] == "access_token") accessToken = parts[1]
                    if (parts[0] == "refresh_token") refreshToken = parts[1]
                }
            }
        }

        lifecycleScope.launch {
            if (!accessToken.isNullOrEmpty() && !refreshToken.isNullOrEmpty()) {
                authRepository.setRecoverySession(accessToken!!, refreshToken!!)
            }

            if (isRecovery) {
                startDestinationState.value = Screen.ResetPassword.route
            } else if (uriStr.contains("dashboard") || uriStr.contains("type=signup") || uriStr.contains("code=") || !accessToken.isNullOrEmpty()) {
                startDestinationState.value = Screen.Dashboard.route
            }
        }
    }
}
