package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppTheme

val PresetNoteColors = listOf(
    null to "Default",
    0xFFFEF2F2 to "Rose",
    0xFFFFFBEB to "Amber",
    0xFFF0FDF4 to "Emerald",
    0xFFECFEFF to "Cyan",
    0xFFEFF6FF to "Sky",
    0xFFF5F3FF to "Violet",
    0xFFFDF2F8 to "Pink",
    0xFF181B22 to "Obsidian"
)

@Composable
fun NoteFormattingToolbar(
    onHeadingClick: () -> Unit,
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
    onBulletClick: () -> Unit,
    onNumberedClick: () -> Unit,
    onQuoteClick: () -> Unit,
    onAddChecklistClick: () -> Unit,
    onColorClick: () -> Unit,
    onFolderClick: () -> Unit,
    onTagClick: () -> Unit,
    onReminderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppTheme.current
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ToolbarIconButton(
            icon = Icons.Default.FormatSize,
            label = "H1",
            contentDescription = "Heading",
            onClick = onHeadingClick
        )
        ToolbarIconButton(
            icon = Icons.Default.FormatBold,
            contentDescription = "Bold",
            onClick = onBoldClick
        )
        ToolbarIconButton(
            icon = Icons.Default.FormatItalic,
            contentDescription = "Italic",
            onClick = onItalicClick
        )
        ToolbarIconButton(
            icon = Icons.Default.FormatListBulleted,
            contentDescription = "Bullet List",
            onClick = onBulletClick
        )
        ToolbarIconButton(
            icon = Icons.Default.FormatListNumbered,
            contentDescription = "Numbered List",
            onClick = onNumberedClick
        )
        ToolbarIconButton(
            icon = Icons.Default.FormatQuote,
            contentDescription = "Quote",
            onClick = onQuoteClick
        )
        ToolbarIconButton(
            icon = Icons.Default.Checklist,
            contentDescription = "Checklist",
            onClick = onAddChecklistClick
        )

        Spacer(
            modifier = Modifier
                .width(1.dp)
                .height(20.dp)
                .background(theme.getBorder().copy(alpha = 0.5f))
        )

        ToolbarIconButton(
            icon = Icons.Default.ColorLens,
            contentDescription = "Note Color",
            onClick = onColorClick
        )
        ToolbarIconButton(
            icon = Icons.Default.Folder,
            contentDescription = "Folder",
            onClick = onFolderClick
        )
        ToolbarIconButton(
            icon = Icons.Default.LocalOffer,
            contentDescription = "Tags",
            onClick = onTagClick
        )
        ToolbarIconButton(
            icon = Icons.Default.Alarm,
            contentDescription = "Reminder",
            onClick = onReminderClick
        )
    }
}

@Composable
private fun ToolbarIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    label: String? = null
) {
    val theme = LocalAppTheme.current

    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (label != null) {
            Text(
                text = label,
                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = theme.getPrimaryText()
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = theme.getPrimaryText(),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
