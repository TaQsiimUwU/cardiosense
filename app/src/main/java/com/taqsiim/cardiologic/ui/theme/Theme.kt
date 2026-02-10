package com.taqsiim.cardiologic.ui.theme

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = CardioBlue80,
    onPrimary = Color(0xFF003258),
    primaryContainer = CardioBlueVariant80,
    onPrimaryContainer = Color(0xFF001D36),

    secondary = CardioTeal80,
    onSecondary = Color(0xFF003730),
    secondaryContainer = Color(0xFF00574E),
    onSecondaryContainer = Color(0xFF70F4E5),

    tertiary = InfoBlueDark,
    onTertiary = Color(0xFF003258),

    error = ErrorRedDark,
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    background = Color(0xFF1A1C1E),
    onBackground = Color(0xFFE2E2E5),

    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE2E2E5),
    surfaceVariant = Color(0xFF42474E),
    onSurfaceVariant = Color(0xFFC2C7CF),

    outline = Color(0xFF8C9199),
    outlineVariant = Color(0xFF42474E)
)

private val LightColorScheme = lightColorScheme(
    primary = CardioBlue40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4FF),
    onPrimaryContainer = Color(0xFF001A41),

    secondary = CardioTeal40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFA7F3E8),
    onSecondaryContainer = Color(0xFF002019),

    tertiary = InfoBlue,
    onTertiary = Color.White,

    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    background = Color(0xFFFDFCFF),
    onBackground = Color(0xFF1A1C1E),

    surface = Color(0xFFFDFCFF),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE0E2EC),
    onSurfaceVariant = Color(0xFF44474E),

    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC4C6D0)
)

@Composable
fun CardiosenseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled for consistent medical UI
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
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
