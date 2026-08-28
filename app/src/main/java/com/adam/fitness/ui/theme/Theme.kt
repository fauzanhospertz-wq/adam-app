package com.adam.fitness.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = AdamOrange,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    background = AdamBgDark,
    onBackground = androidx.compose.ui.graphics.Color.White,
    surface = AdamSurfaceDark,
    onSurface = androidx.compose.ui.graphics.Color.White,
    surfaceVariant = AdamSurfaceDark2,
    secondary = AdamGreen,
    error = AdamRed
)

private val LightColors = lightColorScheme(
    primary = AdamOrange,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    background = AdamBgLight,
    onBackground = androidx.compose.ui.graphics.Color.Black,
    surface = AdamSurfaceLight,
    onSurface = androidx.compose.ui.graphics.Color.Black,
    surfaceVariant = AdamSurfaceLight2,
    secondary = AdamGreen,
    error = AdamRed
)

@Composable
fun AdamTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, typography = AdamTypography, content = content)
}
