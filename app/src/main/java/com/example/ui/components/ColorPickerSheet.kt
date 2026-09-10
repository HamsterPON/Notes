package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.parseColorString
import com.example.ui.theme.toHexColorString

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ColorPickerSheet(
    title: String,
    initialColorHex: Long,
    onColorSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val theme = LocalAppTheme.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val initialR = ((initialColorHex shr 16) and 0xFF).toFloat()
    val initialG = ((initialColorHex shr 8) and 0xFF).toFloat()
    val initialB = (initialColorHex and 0xFF).toFloat()

    var red by remember { mutableFloatStateOf(initialR) }
    var green by remember { mutableFloatStateOf(initialG) }
    var blue by remember { mutableFloatStateOf(initialB) }

    fun currentColorLong(): Long {
        val r = red.toInt().coerceIn(0, 255)
        val g = green.toInt().coerceIn(0, 255)
        val b = blue.toInt().coerceIn(0, 255)
        return 0xFF000000L or (r.toLong() shl 16) or (g.toLong() shl 8) or b.toLong()
    }

    var hexText by remember { mutableStateOf(initialColorHex.toHexColorString()) }

    fun updateFromHex(hex: String) {
        hexText = hex
        val parsed = parseColorString(hex, currentColorLong())
        red = ((parsed shr 16) and 0xFF).toFloat()
        green = ((parsed shr 8) and 0xFF).toFloat()
        blue = (parsed and 0xFF).toFloat()
    }

    // Curated color palette presets
    val presetPalettes = listOf(
        0xFFFFFFFFL, 0xFFF5F5F7L, 0xFFE5E5EAL, 0xFFD1D1D6L,
        0xFF8E8E93L, 0xFF636366L, 0xFF2C2C2EL, 0xFF1C1C1EL,
        0xFF111113L, 0xFF07050DL, 0xFF7C5CFFL, 0xFFA855F7L,
        0xFF3B82F6L, 0xFF06B6D4L, 0xFF10B981L, 0xFFEAB308L,
        0xFFF97316L, 0xFFEF4444L, 0xFFEC4899L, 0xFF64748BL
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = theme.getSurface(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = theme.getPrimaryText()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Swatch Preview & Hex input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(theme.getBackground())
                    .border(1.dp, theme.getBorder(), RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(currentColorLong()))
                        .border(1.dp, Color.Black.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hex Color",
                        style = MaterialTheme.typography.labelMedium,
                        color = theme.getSecondaryText()
                    )

                    BasicTextField(
                        value = hexText,
                        onValueChange = { updateFromHex(it) },
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            color = theme.getPrimaryText(),
                            fontWeight = FontWeight.SemiBold
                        ),
                        cursorBrush = SolidColor(theme.getPrimaryText()),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("hex_color_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // RGB Sliders
            ColorSliderRow(
                label = "Red (${red.toInt()})",
                value = red,
                color = Color.Red,
                onValueChange = {
                    red = it
                    hexText = currentColorLong().toHexColorString()
                },
                textColor = theme.getSecondaryText()
            )

            ColorSliderRow(
                label = "Green (${green.toInt()})",
                value = green,
                color = Color(0xFF22C55E),
                onValueChange = {
                    green = it
                    hexText = currentColorLong().toHexColorString()
                },
                textColor = theme.getSecondaryText()
            )

            ColorSliderRow(
                label = "Blue (${blue.toInt()})",
                value = blue,
                color = Color(0xFF3B82F6),
                onValueChange = {
                    blue = it
                    hexText = currentColorLong().toHexColorString()
                },
                textColor = theme.getSecondaryText()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Preset Palettes",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = theme.getSecondaryText(),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                presetPalettes.forEach { colorHex ->
                    val color = Color(colorHex)
                    val isSelected = currentColorLong() == colorHex
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) theme.getPrimaryText() else theme.getBorder(),
                                shape = CircleShape
                            )
                            .clickable {
                                red = ((colorHex shr 16) and 0xFF).toFloat()
                                green = ((colorHex shr 8) and 0xFF).toFloat()
                                blue = (colorHex and 0xFF).toFloat()
                                hexText = colorHex.toHexColorString()
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Cancel", color = theme.getPrimaryText())
                }

                Button(
                    onClick = {
                        onColorSelected(currentColorLong())
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.getButton(),
                        contentColor = theme.getButtonText()
                    )
                ) {
                    Text("Apply")
                }
            }
        }
    }
}

@Composable
private fun ColorSliderRow(
    label: String,
    value: Float,
    color: Color,
    onValueChange: (Float) -> Unit,
    textColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontSize = 11.sp
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..255f,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color.copy(alpha = 0.8f)
            )
        )
    }
}
