package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NoteSortOption
import com.example.data.NoteViewMode
import com.example.sync.GoogleAccountInfo
import com.example.sync.SyncState
import com.example.ui.theme.LocalAppTheme
import com.example.ui.viewmodel.NotesCategoryTab

@Composable
fun NotesCategoryTabs(
    selectedTab: NotesCategoryTab,
    onTabSelected: (NotesCategoryTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppTheme.current
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 18.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NotesCategoryTab.values().forEach { tab ->
            val isSelected = selectedTab == tab
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) theme.getButton() else theme.getSurface(),
                label = "tab_bg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) theme.getButtonText() else theme.getSecondaryText(),
                label = "tab_text"
            )

            Surface(
                onClick = { onTabSelected(tab) },
                shape = RoundedCornerShape(12.dp),
                color = bgColor,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) Color.Transparent else theme.getBorder()
                )
            ) {
                Text(
                    text = tab.title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = textColor,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun NotesFilterBar(
    folders: List<String>,
    selectedFolder: String,
    onFolderSelected: (String) -> Unit,
    tags: List<String>,
    selectedTag: String?,
    onTagSelected: (String?) -> Unit,
    sortOption: NoteSortOption,
    onSortOptionSelected: (NoteSortOption) -> Unit,
    viewMode: NoteViewMode,
    onToggleViewMode: () -> Unit,
    isTrashActive: Boolean,
    onEmptyTrash: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppTheme.current
    var showSortMenu by remember { mutableStateOf(false) }
    val folderScroll = rememberScrollState()

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Folders list
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(folderScroll),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isTrashActive) {
                    folders.forEach { folder ->
                        val isSelected = selectedFolder.equals(folder, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) theme.getAccent().copy(alpha = 0.15f) else Color.Transparent)
                                .border(
                                    BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) theme.getAccent().copy(alpha = 0.4f) else theme.getBorder().copy(alpha = 0.5f)
                                    ),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onFolderSelected(folder) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = folder,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) theme.getAccent() else theme.getSecondaryText(),
                                fontSize = 11.sp
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Notes in trash are preserved for 30 days",
                        style = MaterialTheme.typography.labelSmall,
                        color = theme.getSecondaryText().copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }

            // Controls on right: View mode & Sort / Empty trash
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isTrashActive) {
                    IconButton(
                        onClick = onEmptyTrash,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Empty Trash",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    // View Mode toggle
                    IconButton(
                        onClick = onToggleViewMode,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (viewMode == NoteViewMode.GRID) Icons.Default.ViewAgenda else Icons.Default.GridView,
                            contentDescription = "Toggle Grid/List",
                            tint = theme.getPrimaryText(),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Sort menu button
                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort Notes",
                                tint = theme.getPrimaryText(),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier.background(theme.getSurface())
                        ) {
                            DropdownMenuItem(
                                text = { Text("Modified (Newest first)") },
                                onClick = {
                                    onSortOptionSelected(NoteSortOption.MODIFIED_DESC)
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    if (sortOption == NoteSortOption.MODIFIED_DESC) {
                                        Icon(Icons.Default.Check, null, tint = theme.getAccent())
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Modified (Oldest first)") },
                                onClick = {
                                    onSortOptionSelected(NoteSortOption.MODIFIED_ASC)
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    if (sortOption == NoteSortOption.MODIFIED_ASC) {
                                        Icon(Icons.Default.Check, null, tint = theme.getAccent())
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Created (Newest first)") },
                                onClick = {
                                    onSortOptionSelected(NoteSortOption.CREATED_DESC)
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    if (sortOption == NoteSortOption.CREATED_DESC) {
                                        Icon(Icons.Default.Check, null, tint = theme.getAccent())
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Title (A to Z)") },
                                onClick = {
                                    onSortOptionSelected(NoteSortOption.TITLE_ASC)
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    if (sortOption == NoteSortOption.TITLE_ASC) {
                                        Icon(Icons.Default.Check, null, tint = theme.getAccent())
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Tags bar if any tags exist
        if (tags.isNotEmpty() && !isTrashActive) {
            val tagScroll = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(tagScroll)
                    .padding(horizontal = 18.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tags.forEach { tag ->
                    val isTagActive = selectedTag == tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isTagActive) theme.getButton() else theme.getSurface())
                            .border(
                                BorderStroke(1.dp, if (isTagActive) Color.Transparent else theme.getBorder()),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                onTagSelected(if (isTagActive) null else tag)
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isTagActive) theme.getButtonText() else theme.getPrimaryText(),
                                fontSize = 11.sp
                            )
                            if (isTagActive) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear tag",
                                    tint = theme.getButtonText(),
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
