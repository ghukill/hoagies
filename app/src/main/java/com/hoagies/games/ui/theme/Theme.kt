package com.hoagies.games.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.hoagies.games.data.settings.AppSettings
import com.hoagies.games.data.settings.ThemeMode

/** Current settings, available anywhere in the UI tree. */
val LocalAppSettings = staticCompositionLocalOf { AppSettings() }

/** Whether the app is currently drawing dark, after applying the user's theme choice. */
@Composable
fun AppSettings.isDark(): Boolean = when (themeMode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

private val HoagiesShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
)

@Composable
fun HoagiesTheme(settings: AppSettings = AppSettings(), content: @Composable () -> Unit) {
    val dark = settings.isDark()
    val colors = when {
        settings.highContrast && dark -> HighContrastDarkColors
        settings.highContrast -> HighContrastLightColors
        dark -> DarkColors
        else -> LightColors
    }
    val density = LocalDensity.current
    // Scale text on top of the phone's own font size so both preferences are honoured.
    val scaledDensity = Density(density.density, density.fontScale * settings.textSize.scale)

    CompositionLocalProvider(
        LocalDensity provides scaledDensity,
        LocalAppSettings provides settings,
    ) {
        MaterialTheme(
            colorScheme = colors,
            typography = HoagiesTypography,
            shapes = HoagiesShapes,
            content = content,
        )
    }
}
