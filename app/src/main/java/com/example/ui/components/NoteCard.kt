package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.ChecklistItem
import com.example.data.NoteEntity
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.themeCardBackground
import com.example.ui.util.DateFormatter
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteCard(
    note: NoteEntity,
    onClick: () -> Unit,
    onPinClick: () -> Unit,
    onFavoriteClick: () -> Unit = {},
    onDeleteClick: () -> Unit,
    onRestoreClick: () -> Unit = {},
    onDuplicateClick: () -> Unit = {},
    onChecklistToggle: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val theme = LocalAppTheme.current

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.972f else 1.0f,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = (450f / theme.animationScale).coerceAtLeast(200f)),
        label = "card_press_scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("note_card_${note.id}")
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            }
            .themeCardBackground(theme, customColorTint = note.colorHex)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Photos preview if any
            if (note.imagePaths.isNotEmpty()) {
                NoteCardPhotosPreview(
                    imagePaths = note.imagePaths,
                    borderRadius = theme.borderRadiusDp * 0.65f,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // Header row with Title and subtle Pin / Fav / Delete buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                    if (note.title.isNotBlank()) {
                        Text(
                            text = note.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = theme.getPrimaryText(),
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Folder badge
                    if (note.folder.isNotBlank() && note.folder != "Notes" && !note.isDeleted) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(theme.getBorder().copy(alpha = 0.35f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = theme.getSecondaryText(),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = note.folder,
                                style = MaterialTheme.typography.labelSmall,
                                color = theme.getSecondaryText(),
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (note.isDeleted) {
                        IconButton(
                            onClick = onRestoreClick,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restore,
                                contentDescription = "Restore note",
                                tint = theme.getAccent(),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = "Delete permanently",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        // Favorite button
                        IconButton(
                            onClick = onFavoriteClick,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (note.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite note",
                                tint = if (note.isFavorite) Color(0xFFF43F5E) else theme.getSecondaryText().copy(alpha = 0.45f),
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        // Pin button
                        IconButton(
                            onClick = onPinClick,
                            modifier = Modifier.size(28.dp).testTag("pin_button_${note.id}")
                        ) {
                            Icon(
                                imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                contentDescription = "Pin note",
                                tint = if (note.isPinned) theme.getAccent() else theme.getSecondaryText().copy(alpha = 0.45f),
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        // Delete button
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(28.dp).testTag("delete_button_${note.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = "Delete note",
                                tint = theme.getSecondaryText().copy(alpha = 0.45f),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            // Content excerpt
            if (note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = theme.getSecondaryText(),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 19.sp
                )
            }

            // Checklist preview items (if any)
            if (note.checklistItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    note.checklistItems.take(3).forEach { item ->
                        ChecklistCardItem(
                            item = item,
                            onToggle = { onChecklistToggle(item.id) }
                        )
                    }

                    if (note.checklistItems.size > 3) {
                        Text(
                            text = "+${note.checklistItems.size - 3} more items",
                            style = MaterialTheme.typography.labelMedium,
                            color = theme.getSecondaryText().copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // Tags Chips
            if (note.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    note.tags.take(4).forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(theme.getBorder().copy(alpha = 0.4f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelSmall,
                                color = theme.getPrimaryText().copy(alpha = 0.8f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer with last modified date & indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = DateFormatter.formatModifiedDate(note.modifiedAt),
                        style = MaterialTheme.typography.labelMedium,
                        color = theme.getSecondaryText().copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )

                    // Reminder indicator
                    if (note.reminderTime != null && note.reminderTime > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = "Has reminder",
                            tint = theme.getAccent(),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (note.checklistItems.isNotEmpty()) {
                        val done = note.checklistItems.count { it.isCompleted }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(theme.getBorder().copy(alpha = 0.35f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$done/${note.checklistItems.size}",
                                style = MaterialTheme.typography.labelMedium,
                                fontSize = 10.sp,
                                color = theme.getSecondaryText(),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (note.imagePaths.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(theme.getBorder().copy(alpha = 0.35f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${note.imagePaths.size} photo${if (note.imagePaths.size > 1) "s" else ""}",
                                style = MaterialTheme.typography.labelMedium,
                                fontSize = 10.sp,
                                color = theme.getSecondaryText(),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChecklistCardItem(
    item: ChecklistItem,
    onToggle: () -> Unit
) {
    val theme = LocalAppTheme.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable { onToggle() }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (item.isCompleted) theme.getAccent() else Color.Transparent)
                .border(
                    BorderStroke(
                        width = if (item.isCompleted) 0.dp else 1.2.dp,
                        color = if (item.isCompleted) Color.Transparent else theme.getBorder()
                    ),
                    shape = RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (item.isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = theme.getButtonText(),
                    modifier = Modifier.size(11.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = item.text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                color = if (item.isCompleted) theme.getSecondaryText().copy(alpha = 0.45f) else theme.getPrimaryText(),
                textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun NoteCardPhotosPreview(
    imagePaths: List<String>,
    borderRadius: Float = 14f,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cornerRadius = borderRadius.dp

    when {
        imagePaths.size == 1 -> {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(File(imagePaths[0]))
                    .crossfade(true)
                    .build(),
                contentDescription = "Note photo preview",
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(cornerRadius))
            )
        }
        imagePaths.size == 2 -> {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                imagePaths.take(2).forEach { path ->
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(File(path))
                            .crossfade(true)
                            .build(),
                        contentDescription = "Note photo preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .weight(1f)
                            .height(105.dp)
                            .clip(RoundedCornerShape(cornerRadius))
                    )
                }
            }
        }
        else -> {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                imagePaths.take(3).forEachIndexed { index, path ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(85.dp)
                            .clip(RoundedCornerShape(cornerRadius))
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(File(path))
                                .crossfade(true)
                                .build(),
                            contentDescription = "Note photo thumbnail",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )
                        if (index == 2 && imagePaths.size > 3) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+${imagePaths.size - 2}",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
