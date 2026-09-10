package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat

@Composable
fun NotesTheme(
    themeConfig: ThemeConfig = ThemeConfig.WhitePreset,
    content: @Composable () -> Unit
) {
    val isDarkBackground = ColorUtils.calculateLuminance(themeConfig.backgroundColor.toInt()) < 0.45

    val dynamicColorScheme = if (isDarkBackground) {
        darkColorScheme(
            primary = themeConfig.getAccent(),
            onPrimary = themeConfig.getButtonText(),
            primaryContainer = themeConfig.getSurface(),
            onPrimaryContainer = themeConfig.getPrimaryText(),
            secondary = themeConfig.getSecondaryText(),
            onSecondary = themeConfig.getButtonText(),
            background = themeConfig.getBackground(),
            onBackground = themeConfig.getPrimaryText(),
            surface = themeConfig.getSurface(),
            onSurface = themeConfig.getPrimaryText(),
            surfaceVariant = themeConfig.getSurface(),
            onSurfaceVariant = themeConfig.getSecondaryText(),
            outline = themeConfig.getBorder(),
            outlineVariant = themeConfig.getBorder()
        )
    } else {
        lightColorScheme(
            primary = themeConfig.getAccent(),
            onPrimary = themeConfig.getButtonText(),
            primaryContainer = themeConfig.getSurface(),
            onPrimaryContainer = themeConfig.getPrimaryText(),
            secondary = themeConfig.getSecondaryText(),
            onSecondary = themeConfig.getButtonText(),
            background = themeConfig.getBackground(),
            onBackground = themeConfig.getPrimaryText(),
            surface = themeConfig.getSurface(),
            onSurface = themeConfig.getPrimaryText(),
            surfaceVariant = themeConfig.getSurface(),
            onSurfaceVariant = themeConfig.getSecondaryText(),
            outline = themeConfig.getBorder(),
            outlineVariant = themeConfig.getBorder()
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            try {
                val window = (view.context as? Activity)?.window
                if (window != null) {
                    window.statusBarColor = themeConfig.getBackground().toArgb()
                    window.navigationBarColor = themeConfig.getBackground().toArgb()
                    val insetsController = WindowCompat.getInsetsController(window, view)
                    insetsController.isAppearanceLightStatusBars = !isDarkBackground
                    insetsController.isAppearanceLightNavigationBars = !isDarkBackground
                }
            } catch (_: Exception) {
                // Ignore in headless or testing environments
            }
        }
    }

    CompositionLocalProvider(LocalAppTheme provides themeConfig) {
        MaterialTheme(
            colorScheme = dynamicColorScheme,
            typography = Typography,
            content = content
        )
    }
}
