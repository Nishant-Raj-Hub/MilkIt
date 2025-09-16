package com.milkit.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = MilkBlue,
    onPrimary = White,
    primaryContainer = MilkBlueDark,
    onPrimaryContainer = MilkBlueLight,
    secondary = MilkGreen,
    onSecondary = White,
    secondaryContainer = MilkGreenDark,
    onSecondaryContainer = MilkGreenLight,
    tertiary = MilkYellow,
    onTertiary = DarkGray,
    tertiaryContainer = MilkYellowDark,
    onTertiaryContainer = MilkYellowLight,
    error = ErrorRed,
    onError = White,
    errorContainer = ErrorRedDark,
    onErrorContainer = ErrorRedLight,
    background = DarkBackground,
    onBackground = White,
    surface = DarkSurface,
    onSurface = White,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = LightGray,
    outline = MediumGray,
    outlineVariant = DarkGray,
    scrim = Black,
    inverseSurface = LightGray,
    inverseOnSurface = DarkGray,
    inversePrimary = MilkBlueDark,
    surfaceDim = DarkSurfaceDim,
    surfaceBright = DarkSurfaceBright,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
)

private val LightColorScheme = lightColorScheme(
    primary = MilkBlue,
    onPrimary = White,
    primaryContainer = MilkBlueLight,
    onPrimaryContainer = MilkBlueDark,
    secondary = MilkGreen,
    onSecondary = White,
    secondaryContainer = MilkGreenLight,
    onSecondaryContainer = MilkGreenDark,
    tertiary = MilkYellow,
    onTertiary = DarkGray,
    tertiaryContainer = MilkYellowLight,
    onTertiaryContainer = MilkYellowDark,
    error = ErrorRed,
    onError = White,
    errorContainer = ErrorRedLight,
    onErrorContainer = ErrorRedDark,
    background = White,
    onBackground = DarkGray,
    surface = White,
    onSurface = DarkGray,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = MediumGray,
    outline = MediumGray,
    outlineVariant = LightGray,
    scrim = Black,
    inverseSurface = DarkGray,
    inverseOnSurface = LightGray,
    inversePrimary = MilkBlueLight,
    surfaceDim = LightSurfaceDim,
    surfaceBright = White,
    surfaceContainerLowest = White,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest,
)

@Composable
fun MilkItTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
