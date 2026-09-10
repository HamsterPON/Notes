package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BaseThemeType
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.ThemeConfig
import com.example.ui.theme.themeCardBackground

@Composable
fun ThemeSelectorRow(
    selectedType: BaseThemeType,
    onSelectPreset: (BaseThemeType) -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = listOf(
        ThemeConfig.WhitePreset to "Minimalist & pure light",
        ThemeConfig.DarkPreset to "Deep obsidian & soft contrast",
        ThemeConfig.GlossyPreset to "Reflective highlight gradients",
        ThemeConfig.LiquidGlassPreset to "Translucent glass & layered depth",
        ThemeConfig.GlowPreset to "Vibrant neon glow & aura"
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items(presets, key = { it.first.id }) { (preset, description) ->
            val isSelected = selectedType == preset.baseType
            ThemeVisualCard(
                preset = preset,
                description = description,
                isSelected = isSelected,
                onClick = { onSelectPreset(preset.baseType) }
            )
        }
    }
}

@Composable
fun ThemeVisualCard(
    preset: ThemeConfig,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val activeTheme = LocalAppTheme.current

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 0.98f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "theme_card_scale"
    )

    Column(
        modifier = Modifier
            .width(160.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("theme_preset_${preset.baseType.name.lowercase()}")
    ) {
        // Visual thumbnail mimicking the theme's appearance
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(preset.getBackground())
                .border(
                    BorderStroke(
                        width = if (isSelected) 2.5.dp else 1.dp,
                        color = if (isSelected) activeTheme.getPrimaryText() else preset.getBorder()
                    ),
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(10.dp)
        ) {
            // Miniature card inside
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .themeCardBackground(preset)
                    .padding(8.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(50.dp)
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(preset.getPrimaryText())
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(preset.getAccent())
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .width(70.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(preset.getSecondaryText().copy(alpha = 0.6f))
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Mini check row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(preset.getAccent()),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .background(preset.getButtonText())
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(preset.getSecondaryText().copy(alpha = 0.4f))
                        )
                    }
                }
            }

            // Selection Check Badge
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(activeTheme.getPrimaryText()),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = activeTheme.getButtonText(),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = preset.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = activeTheme.getPrimaryText(),
            fontSize = 14.sp
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = activeTheme.getSecondaryText(),
            fontSize = 11.sp,
            lineHeight = 14.sp
        )
    }
}
