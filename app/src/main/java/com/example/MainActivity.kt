package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NoteEditScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.NotesTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.NotesViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: NotesViewModel = viewModel()
            val currentTheme by viewModel.currentTheme.collectAsState()
            NotesTheme(themeConfig = currentTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = currentTheme.getBackground()
                ) {
                    NotesApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun NotesApp(
    viewModel: NotesViewModel = viewModel()
) {
    val screen by viewModel.currentScreen.collectAsState()
    val currentTheme by viewModel.currentTheme.collectAsState()

    val animDuration = (280 / currentTheme.animationScale).toInt().coerceIn(100, 600)
    val fadeDuration = (220 / currentTheme.animationScale).toInt().coerceIn(80, 500)

    AnimatedContent(
        targetState = screen,
        transitionSpec = {
            if (targetState is AppScreen.Edit || targetState is AppScreen.Settings) {
                // Opening note or settings: scale up + slide slightly from right with smooth fade
                (slideInHorizontally(
                    animationSpec = tween(animDuration),
                    initialOffsetX = { fullWidth -> fullWidth / 8 }
                ) + fadeIn(animationSpec = tween(fadeDuration)) + scaleIn(
                    initialScale = 0.94f,
                    animationSpec = tween(animDuration)
                )).togetherWith(
                    fadeOut(animationSpec = tween(fadeDuration)) + scaleOut(
                        targetScale = 0.96f,
                        animationSpec = tween(fadeDuration)
                    )
                )
            } else {
                // Returning home: slide out to right + smooth fade back in
                (fadeIn(animationSpec = tween(fadeDuration)) + scaleIn(
                    initialScale = 0.96f,
                    animationSpec = tween(fadeDuration)
                )).togetherWith(
                    slideOutHorizontally(
                        animationSpec = tween(animDuration),
                        targetOffsetX = { fullWidth -> fullWidth / 8 }
                    ) + fadeOut(animationSpec = tween(fadeDuration)) + scaleOut(
                        targetScale = 0.94f,
                        animationSpec = tween(fadeDuration)
                    )
                )
            }
        },
        label = "screen_transition"
    ) { currentTargetScreen ->
        when (currentTargetScreen) {
            is AppScreen.Home -> {
                HomeScreen(
                    viewModel = viewModel,
                    onOpenNote = { note -> viewModel.openNote(note) }
                )
            }
            is AppScreen.Edit -> {
                NoteEditScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateToHome() }
                )
            }
            is AppScreen.Settings -> {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateToHome() }
                )
            }
        }
    }
}
