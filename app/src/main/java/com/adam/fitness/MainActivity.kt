package com.adam.fitness

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.adam.fitness.data.ThemeMode
import com.adam.fitness.ui.navigation.AdamNavHost
import com.adam.fitness.ui.theme.AdamTheme
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        Configuration.getInstance().userAgentValue = packageName

        val app = application as ADAMApplication
        setContent {
            val themeMode by app.settingsRepository.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val darkTheme = when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            AdamTheme(darkTheme = darkTheme) {
                AdamNavHost(app = app)
            }
        }
    }
}
