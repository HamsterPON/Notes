package com.example.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

fun Modifier.themeCardBackground(
    theme: ThemeConfig,
    shape: Shape? = null,
    customColorTint: Long? = null
): Modifier = composed {
    val cardShape = shape ?: RoundedCornerShape(theme.borderRadiusDp.dp)

    // Optional animated pulse for subtle aura breathing in Glow theme
    val infiniteTransition = rememberInfiniteTransition(label = "glow_pulse")
    val pulseAlpha by if (theme.isGlow && theme.glowIntensity > 0.05f) {
        infiniteTransition.animateFloat(
            initialValue = 0.82f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(2800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_alpha"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(1.0f) }
    }

    var modifier: Modifier = this

    // 1. Radiating Ambient Glow Aura for Cards & Windows
    if (theme.isGlow && theme.glowIntensity > 0.05f) {
        val glowBase = theme.getGlow()
        val intensity = theme.glowIntensity * pulseAlpha
        val glowRadius = theme.glowRadiusDp

        modifier = modifier.drawBehind {
            val rPx = glowRadius.dp.toPx()
            val cornerRadiusPx = theme.borderRadiusDp.dp.toPx()

            // Outer soft ambient radiation
            drawRoundRect(
                color = glowBase.copy(alpha = (0.12f * intensity).coerceIn(0.02f, 0.35f)),
                topLeft = Offset(-rPx, -rPx),
                size = Size(size.width + rPx * 2, size.height + rPx * 2),
                cornerRadius = CornerRadius(cornerRadiusPx + rPx * 0.7f)
            )

            // Middle diffusion halo
            val midRPx = rPx * 0.5f
            drawRoundRect(
                color = glowBase.copy(alpha = (0.22f * intensity).coerceIn(0.05f, 0.55f)),
                topLeft = Offset(-midRPx, -midRPx),
                size = Size(size.width + midRPx * 2, size.height + midRPx * 2),
                cornerRadius = CornerRadius(cornerRadiusPx + midRPx * 0.4f)
            )

            // Inner vibrant rim aura
            val innerRPx = rPx * 0.2f
            drawRoundRect(
                color = glowBase.copy(alpha = (0.35f * intensity).coerceIn(0.1f, 0.75f)),
                topLeft = Offset(-innerRPx, -innerRPx),
                size = Size(size.width + innerRPx * 2, size.height + innerRPx * 2),
                cornerRadius = CornerRadius(cornerRadiusPx + innerRPx * 0.2f)
            )
        }
    } else if (theme.shadowIntensity > 0.05f) {
        modifier = modifier.shadow(
            elevation = (7 * theme.shadowIntensity).dp,
            shape = cardShape,
            spotColor = Color.Black.copy(alpha = (theme.shadowIntensity * 0.30f).coerceIn(0.04f, 0.5f))
        )
    }

    // Clip to shape
    modifier = modifier.clip(cardShape)

    // Base background color / custom note tint
    val baseSurface = if (customColorTint != null && customColorTint > 0) {
        Color(customColorTint)
    } else {
        theme.getSurface()
    }

    if (theme.isGlossy) {
        val highlightColor = baseSurface.copy(alpha = (baseSurface.alpha + 0.12f).coerceAtMost(1f))
        modifier = modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    highlightColor,
                    baseSurface,
                    baseSurface.copy(alpha = (baseSurface.alpha - 0.06f).coerceAtLeast(0.1f))
                )
            )
        )
    } else if (theme.isLiquidGlass) {
        modifier = modifier.background(baseSurface)
    } else {
        modifier = modifier.background(baseSurface)
    }

    // Border: Glow illuminated border, Liquid Glass translucent border, or crisp hairline
    val borderColor = if (theme.isGlow && theme.glowIntensity > 0.05f) {
        theme.getGlow().copy(alpha = (0.35f + theme.glowIntensity * 0.55f * pulseAlpha).coerceIn(0.25f, 0.95f))
    } else if (theme.isLiquidGlass) {
        theme.getBorder().copy(alpha = 0.50f)
    } else {
        theme.getBorder()
    }

    val borderWidth = if (theme.isGlow && theme.glowIntensity > 0.3f) 1.5.dp else 1.dp
    modifier = modifier.border(BorderStroke(borderWidth, borderColor), cardShape)

    // Glossy specular highlight gradient on upper region
    if (theme.isGlossy) {
        modifier = modifier.drawWithContent {
            drawContent()
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.16f),
                        Color.White.copy(alpha = 0.03f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = size.height * 0.45f
                ),
                topLeft = Offset.Zero,
                size = Size(size.width, size.height * 0.45f)
            )
        }
    }

    modifier
}

fun Modifier.themeButtonBackground(
    theme: ThemeConfig,
    shape: Shape? = null
): Modifier = composed {
    val buttonShape = shape ?: RoundedCornerShape((theme.borderRadiusDp * 0.75f).dp)
    var modifier: Modifier = this

    if (theme.isGlow && theme.glowIntensity > 0.1f) {
        val glowColor = theme.getGlow().copy(alpha = (theme.glowIntensity * 0.65f).coerceIn(0.2f, 0.9f))
        modifier = modifier.shadow(
            elevation = (10 * theme.glowIntensity).dp,
            shape = buttonShape,
            ambientColor = glowColor,
            spotColor = glowColor
        )
    } else if (theme.shadowIntensity > 0.1f) {
        modifier = modifier.shadow(
            elevation = (4 * theme.shadowIntensity).dp,
            shape = buttonShape
        )
    }

    modifier = modifier.clip(buttonShape)

    if (theme.isGlossy) {
        val btnColor = theme.getButton()
        modifier = modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    btnColor.copy(alpha = (btnColor.alpha + 0.15f).coerceAtMost(1f)),
                    btnColor
                )
            )
        )
    } else {
        modifier = modifier.background(theme.getButton())
    }

    if (theme.isGlow && theme.glowIntensity > 0.1f) {
        modifier = modifier.border(
            BorderStroke(1.2.dp, theme.getGlow().copy(alpha = 0.75f)),
            buttonShape
        )
    }

    modifier
}

fun Long.toHexColorString(): String {
    val r = ((this shr 16) and 0xFF).toInt()
    val g = ((this shr 8) and 0xFF).toInt()
    val b = (this and 0xFF).toInt()
    return String.format("#%02X%02X%02X", r, g, b)
}

fun parseColorString(hex: String, defaultColor: Long): Long {
    return try {
        val clean = hex.trim().removePrefix("#")
        when (clean.length) {
            6 -> 0xFF000000L or clean.toLong(16)
            8 -> clean.toLong(16)
            3 -> {
                val r = clean[0].toString().repeat(2)
                val g = clean[1].toString().repeat(2)
                val b = clean[2].toString().repeat(2)
                0xFF000000L or "$r$g$b".toLong(16)
            }
            else -> defaultColor
        }
    } catch (e: Exception) {
        defaultColor
    }
}
