package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChecklistItem
import com.example.ui.theme.LocalAppTheme

@Composable
fun ChecklistSection(
    items: List<ChecklistItem>,
    onToggle: (String) -> Unit,
    onTextChange: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    onMove: (Int, Int) -> Unit,
    onAddItem: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppTheme.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        if (items.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val completedCount = items.count { it.isCompleted }
                Text(
                    text = "CHECKLIST",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = theme.getSecondaryText(),
                    letterSpacing = 1.2.sp
                )

                Text(
                    text = "$completedCount of ${items.size} done",
                    style = MaterialTheme.typography.labelMedium,
                    color = theme.getSecondaryText(),
                    fontSize = 11.sp
                )
            }
        }

        items.forEachIndexed { index, item ->
            ChecklistRow(
                item = item,
                canMoveUp = index > 0,
                canMoveDown = index < items.size - 1,
                onToggle = { onToggle(item.id) },
                onTextChange = { onTextChange(item.id, it) },
                onDelete = { onDelete(item.id) },
                onMoveUp = { onMove(index, index - 1) },
                onMoveDown = { onMove(index, index + 1) },
                onEnterNext = onAddItem
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Clean "+ Add item" button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onAddItem() }
                .padding(vertical = 10.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .border(BorderStroke(1.dp, theme.getBorder()), RoundedCornerShape(7.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add checklist item",
                    tint = theme.getSecondaryText(),
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = "Add checklist item",
                style = MaterialTheme.typography.bodyMedium,
                color = theme.getSecondaryText(),
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
fun ChecklistRow(
    item: ChecklistItem,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onToggle: () -> Unit,
    onTextChange: (String) -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onEnterNext: () -> Unit
) {
    val theme = LocalAppTheme.current

    // Text fade animation
    val textAlpha by animateFloatAsState(
        targetValue = if (item.isCompleted) 0.38f else 1.0f,
        animationSpec = tween(durationMillis = (240 / theme.animationScale).toInt()),
        label = "checklist_alpha"
    )

    // Strikethrough sweep line progress animation
    val strikethroughProgress by animateFloatAsState(
        targetValue = if (item.isCompleted) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = (260 / theme.animationScale).toInt(), easing = FastOutSlowInEasing),
        label = "checklist_strike"
    )

    // Checkbox bounce animation
    var isPressed by remember { mutableStateOf(false) }
    val checkboxScale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = (500f / theme.animationScale).coerceAtLeast(200f)),
        label = "checkbox_scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Custom Animated Checkbox
        Box(
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer {
                    scaleX = checkboxScale
                    scaleY = checkboxScale
                }
                .clip(RoundedCornerShape(7.dp))
                .background(
                    if (item.isCompleted) theme.getAccent() else Color.Transparent
                )
                .border(
                    BorderStroke(
                        width = if (item.isCompleted) 0.dp else 1.2.dp,
                        color = if (item.isCompleted) Color.Transparent else theme.getBorder()
                    ),
                    shape = RoundedCornerShape(7.dp)
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        },
                        onTap = { onToggle() }
                    )
                }
                .testTag("checklist_checkbox_${item.id}"),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = item.isCompleted,
                enter = fadeIn(animationSpec = tween(150)),
                exit = fadeOut(animationSpec = tween(100))
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = theme.getButtonText(),
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Text input field with strikethrough drawing
        val textColor = theme.getPrimaryText().copy(alpha = textAlpha)
        val strikeColor = theme.getSecondaryText().copy(alpha = 0.6f)

        BasicTextField(
            value = item.text,
            onValueChange = onTextChange,
            textStyle = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = textColor,
                lineHeight = 22.sp
            ),
            cursorBrush = SolidColor(theme.getPrimaryText()),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { onEnterNext() }),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawWithContent {
                            drawContent()
                            if (strikethroughProgress > 0f) {
                                val strokeY = size.height / 2f
                                drawLine(
                                    color = strikeColor,
                                    start = Offset(0f, strokeY),
                                    end = Offset(size.width * strikethroughProgress, strokeY),
                                    strokeWidth = 2.dp.toPx()
                                )
                            }
                        }
                ) {
                    if (item.text.isEmpty()) {
                        Text(
                            text = "List item",
                            style = MaterialTheme.typography.bodyMedium,
                            color = theme.getSecondaryText().copy(alpha = 0.5f),
                            fontSize = 15.sp
                        )
                    }
                    innerTextField()
                }
            },
            modifier = Modifier
                .weight(1f)
                .testTag("checklist_text_${item.id}")
        )

        // Actions: Move up, Move down, Delete
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (canMoveUp) {
                IconButton(
                    onClick = onMoveUp,
                    modifier = Modifier
                        .size(24.dp)
                        .testTag("checklist_move_up_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowUp,
                        contentDescription = "Move item up",
                        tint = theme.getSecondaryText().copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (canMoveDown) {
                IconButton(
                    onClick = onMoveDown,
                    modifier = Modifier
                        .size(24.dp)
                        .testTag("checklist_move_down_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = "Move item down",
                        tint = theme.getSecondaryText().copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(26.dp)
                    .testTag("checklist_delete_${item.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete item",
                    tint = theme.getSecondaryText().copy(alpha = 0.5f),
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}
