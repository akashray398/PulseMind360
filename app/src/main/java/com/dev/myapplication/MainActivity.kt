package com.dev.myapplication

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.myapplication.ui.MainScreen
import com.dev.myapplication.ui.components.BiometricVaultLockScreen
import com.dev.myapplication.ui.splash.SplashScreen
import com.dev.myapplication.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val prefs by viewModel.userPreferencesState.collectAsStateWithLifecycle()
            var showSplash by remember { mutableStateOf(true) }
            var isVaultUnlocked by remember { mutableStateOf(false) }

            MyApplicationTheme(themeMode = prefs.themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (showSplash) {
                        SplashScreen(onSplashFinished = { showSplash = false })
                    } else if (prefs.biometricVaultEnabled && !isVaultUnlocked) {
                        BiometricVaultLockScreen(onUnlockSuccess = { isVaultUnlocked = true })
                    } else {
                        MainScreen()
                    }
                }
            }
        }
    }
}
