package com.mobilecodex.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 品牌色
val Primary = Color(0xFF6C5CE7)
val PrimaryVariant = Color(0xFF5A4BD1)
val Secondary = Color(0xFF00CEC9)
val SecondaryVariant = Color(0xFF00B5B0)
val Background = Color(0xFF1A1A2E)
val Surface = Color(0xFF16213E)
val SurfaceVariant = Color(0xFF0F3460)
val OnPrimary = Color.White
val OnBackground = Color(0xFFEAEAEA)
val OnSurface = Color(0xFFCCCCCC)
val Error = Color(0xFFE74C3C)

// 语法高亮色
val SyntaxKeyword = Color(0xFFFF79C6)
val SyntaxString = Color(0xFFF1FA8C)
val SyntaxNumber = Color(0xFFBD93F9)
val SyntaxComment = Color(0xFF6272A4)
val SyntaxFunction = Color(0xFF50FA7B)
val SyntaxType = Color(0xFF8BE9FD)
val SyntaxOperator = Color(0xFFFF79C6)

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = SecondaryVariant,
    background = Background,
    surface = Surface,
    surfaceVariant = SurfaceVariant,
    onPrimary = OnPrimary,
    onBackground = OnBackground,
    onSurface = OnSurface,
    error = Error
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = SecondaryVariant,
    background = Color(0xFFF8F9FA),
    surface = Color.White,
    surfaceVariant = Color(0xFFF1F3F5),
    onPrimary = Color.White,
    onBackground = Color(0xFF212529),
    onSurface = Color(0xFF343A40),
    error = Error
)

@Composable
fun MobileCodexTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
