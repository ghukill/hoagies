package com.hoagies.games

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hoagies.games.data.settings.AppSettings
import com.hoagies.games.ui.navigation.HoagiesNavHost
import com.hoagies.games.ui.theme.HoagiesTheme
import com.hoagies.games.ui.theme.isDark

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val container = (application as HoagiesApp).container

        setContent {
            val loaded: AppSettings? by container.settings.settings
                .collectAsStateWithLifecycle(initialValue = null)
            // Settings load in a few milliseconds; waiting avoids a flash of the wrong theme.
            val settings = loaded ?: return@setContent

            val dark = settings.isDark()
            DisposableEffect(dark) {
                // Keep status-bar icons readable when the app theme differs from the phone's.
                val style = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose {}
            }

            HoagiesTheme(settings) {
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    HoagiesNavHost()
                }
            }
        }
    }
}
