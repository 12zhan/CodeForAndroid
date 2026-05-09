package com.mobilecodex.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Light Colors
private val LightPrimary = Color(0xFF4F6700)
private val LightOnPrimary = Color(0xFFFFFFFF)
private val LightPrimaryContainer = Color(0xFFD0ED4B)
private val LightOnPrimaryContainer = Color(0xFF141F00)
private val LightSecondary = Color(0xFF5D6146)
private val LightOnSecondary = Color(0xFFFFFFFF)
private val LightSecondaryContainer = Color(0xFFE2E6C4)
private val LightOnSecondaryContainer = Color(0xFF1B1F09)
private val LightTertiary = Color(0xFF3B665B)
private val LightOnTertiary = Color(0xFFFFFFFF)
private val LightTertiaryContainer = Color(0xFFBFECE0)
private val LightOnTertiaryContainer = Color(0xFF00201A)
private val LightBackground = Color(0xFFFEFCF4)
private val LightOnBackground = Color(0xFF1C1C16)
private val LightSurface = Color(0xFFFEFCF4)
private val LightOnSurface = Color(0xFF1C1C16)
private val LightSurfaceVariant = Color(0xFFE3E4D3)
private val LightOnSurfaceVariant = Color(0xFF46483B)
private val LightError = Color(0xFFBA1A1A)

// Dark Colors
private val DarkPrimary = Color(0xFFB4D230)
private val DarkOnPrimary = Color(0xFF283500)
private val DarkPrimaryContainer = Color(0xFF3B4E00)
private val DarkOnPrimaryContainer = Color(0xFFD0ED4B)
private val DarkSecondary = Color(0xFFC5C9AB)
private val DarkOnSecondary = Color(0xFF30331B)
private val DarkSecondaryContainer = Color(0xFF464A30)
private val DarkOnSecondaryContainer = Color(0xFFE2E6C4)
private val DarkTertiary = Color(0xFFA2D0C4)
private val DarkOnTertiary = Color(0xFF07372E)
private val DarkTertiaryContainer = Color(0xFF234E44)
private val DarkOnTertiaryContainer = Color(0xFFBFECE0)
private val DarkBackground = Color(0xFF1C1C16)
private val DarkOnBackground = Color(0xFFE5E2DA)
private val DarkSurface = Color(0xFF1C1C16)
private val DarkOnSurface = Color(0xFFE5E2DA)
private val DarkSurfaceVariant = Color(0xFF46483B)
private val DarkOnSurfaceVariant = Color(0xFFC7C8B7)
private val DarkError = Color(0xFFFFB4AB)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    error = LightError,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = DarkError,
)

@Composable
fun MobileCodexTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
