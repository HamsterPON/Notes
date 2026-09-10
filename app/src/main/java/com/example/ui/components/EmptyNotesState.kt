package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppTheme
import com.example.ui.viewmodel.NotesCategoryTab

@Composable
fun EmptyNotesView(
    isSearching: Boolean,
    categoryTab: NotesCategoryTab = NotesCategoryTab.ALL,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppTheme.current

    val icon = when {
        isSearching -> Icons.Outlined.SearchOff
        categoryTab == NotesCategoryTab.FAVORITES -> Icons.Outlined.FavoriteBorder
        categoryTab == NotesCategoryTab.ARCHIVED -> Icons.Outlined.Archive
        categoryTab == NotesCategoryTab.TRASH -> Icons.Outlined.DeleteOutline
        else -> Icons.Outlined.Checklist
    }

    val title = when {
        isSearching -> "No results found"
        categoryTab == NotesCategoryTab.FAVORITES -> "No favorites yet"
        categoryTab == NotesCategoryTab.ARCHIVED -> "Archive is empty"
        categoryTab == NotesCategoryTab.TRASH -> "Trash is empty"
        else -> "All clear"
    }

    val subtitle = when {
        isSearching -> "No notes, checklists, or tags match your search"
        categoryTab == NotesCategoryTab.FAVORITES -> "Tap the heart icon on any note to mark it as a favorite"
        categoryTab == NotesCategoryTab.ARCHIVED -> "Archived notes stay safe here without cluttering your home screen"
        categoryTab == NotesCategoryTab.TRASH -> "Deleted notes will appear here before being permanently removed"
        else -> "Capture ideas, documents, photos, and interactive checklists"
    }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + scaleIn(initialScale = 0.96f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp, vertical = 72.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(theme.getSurface())
                    .border(1.dp, theme.getBorder(), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = theme.getSecondaryText(),
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = theme.getPrimaryText()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = theme.getSecondaryText(),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
    }
}
