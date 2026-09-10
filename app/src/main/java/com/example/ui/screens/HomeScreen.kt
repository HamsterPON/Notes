package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NoteEntity
import com.example.data.NoteViewMode
import com.example.ui.components.AnimatedAddNoteFab
import com.example.ui.components.EmptyNotesView
import com.example.ui.components.NoteCard
import com.example.ui.components.NotesCategoryTabs
import com.example.ui.components.NotesFilterBar
import com.example.ui.components.PixelSearchBar
import com.example.ui.theme.LocalAppTheme
import com.example.ui.viewmodel.NotesCategoryTab
import com.example.ui.viewmodel.NotesViewModel

@Composable
fun HomeScreen(
    viewModel: NotesViewModel,
    onOpenNote: (NoteEntity?) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppTheme.current
    val notes by viewModel.displayNotes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val categoryTab by viewModel.selectedCategoryTab.collectAsState()
    val selectedFolder by viewModel.selectedFolder.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()
    val availableFolders by viewModel.availableFolders.collectAsState()
    val availableTags by viewModel.availableTags.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val connectedAccount by viewModel.connectedAccount.collectAsState()
    val syncState by viewModel.syncState.collectAsState()

    val snackbarMsg by viewModel.snackbarMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showEmptyTrashDialog by remember { mutableStateOf(false) }

    LaunchedEffect(snackbarMsg) {
        val msg = snackbarMsg
        if (msg != null) {
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSnackbarMessage()
        }
    }

    val pinnedNotes = remember(notes) { notes.filter { it.isPinned } }
    val unpinnedNotes = remember(notes) { notes.filter { !it.isPinned } }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = theme.getBackground(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (categoryTab != NotesCategoryTab.TRASH) {
                AnimatedAddNoteFab(
                    onClick = { onOpenNote(null) },
                    modifier = Modifier
                        .padding(bottom = 16.dp, end = 8.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Title, Cloud Sync & Settings button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 22.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Notes",
                        style = MaterialTheme.typography.headlineLarge,
                        color = theme.getPrimaryText(),
                        fontWeight = FontWeight.Medium,
                        letterSpacing = (-0.8).sp
                    )

                    if (connectedAccount != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { viewModel.syncNow() },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = if (syncState.isSyncing) Icons.Default.Sync else Icons.Default.CloudDone,
                                contentDescription = "Cloud synced",
                                tint = theme.getAccent(),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (notes.isNotEmpty()) {
                        Text(
                            text = "${notes.size} ${if (notes.size == 1) "note" else "notes"}",
                            style = MaterialTheme.typography.labelMedium,
                            color = theme.getSecondaryText(),
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.navigateToSettings() },
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("home_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Palette,
                            contentDescription = "Themes and Settings",
                            tint = theme.getPrimaryText(),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Search Bar
            PixelSearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.searchQuery.value = it },
                modifier = Modifier
                    .padding(horizontal = 18.dp, vertical = 6.dp)
            )

            // Category Tabs: All Notes, Favorites, Archived, Recently Deleted
            NotesCategoryTabs(
                selectedTab = categoryTab,
                onTabSelected = { viewModel.selectedCategoryTab.value = it }
            )

            // Filter Bar: Folders, Tags, ViewMode, Sort, Empty Trash
            NotesFilterBar(
                folders = availableFolders,
                selectedFolder = selectedFolder,
                onFolderSelected = { viewModel.selectedFolder.value = it },
                tags = availableTags,
                selectedTag = selectedTag,
                onTagSelected = { viewModel.selectedTag.value = it },
                sortOption = sortOption,
                onSortOptionSelected = { viewModel.themeRepository.setSortOption(it) },
                viewMode = viewMode,
                onToggleViewMode = {
                    val next = if (viewMode == NoteViewMode.GRID) NoteViewMode.LIST else NoteViewMode.GRID
                    viewModel.themeRepository.setViewMode(next)
                },
                isTrashActive = categoryTab == NotesCategoryTab.TRASH,
                onEmptyTrash = { showEmptyTrashDialog = true }
            )

            if (notes.isEmpty()) {
                EmptyNotesView(
                    isSearching = searchQuery.isNotBlank(),
                    categoryTab = categoryTab,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val gridCells = if (viewMode == NoteViewMode.GRID) StaggeredGridCells.Fixed(2) else StaggeredGridCells.Fixed(1)
                LazyVerticalStaggeredGrid(
                    columns = gridCells,
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 10.dp,
                        bottom = 100.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalItemSpacing = 12.dp,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Pinned notes section (only on All or Favorites)
                    if (categoryTab != NotesCategoryTab.TRASH && pinnedNotes.isNotEmpty()) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            Text(
                                text = "PINNED",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = theme.getSecondaryText(),
                                letterSpacing = 1.4.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                            )
                        }

                        items(pinnedNotes, key = { "pinned_${it.id}" }) { note ->
                            NoteCard(
                                note = note,
                                onClick = { onOpenNote(note) },
                                onPinClick = { viewModel.toggleNotePin(note) },
                                onFavoriteClick = { viewModel.toggleNoteFavorite(note) },
                                onDeleteClick = { viewModel.moveToTrash(note) },
                                onDuplicateClick = { viewModel.duplicateNote(note) },
                                onChecklistToggle = { itemId ->
                                    viewModel.toggleChecklistItemOnCard(note, itemId)
                                }
                            )
                        }

                        if (unpinnedNotes.isNotEmpty()) {
                            item(span = StaggeredGridItemSpan.FullLine) {
                                Text(
                                    text = "OTHER NOTES",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = theme.getSecondaryText(),
                                    letterSpacing = 1.4.sp,
                                    modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 4.dp)
                                )
                            }
                        }
                    }

                    // Regular unpinned notes
                    val regularList = if (categoryTab != NotesCategoryTab.TRASH) unpinnedNotes else notes
                    items(regularList, key = { "note_${it.id}" }) { note ->
                        NoteCard(
                            note = note,
                            onClick = {
                                if (categoryTab != NotesCategoryTab.TRASH) {
                                    onOpenNote(note)
                                }
                            },
                            onPinClick = { viewModel.toggleNotePin(note) },
                            onFavoriteClick = { viewModel.toggleNoteFavorite(note) },
                            onDeleteClick = {
                                if (note.isDeleted) {
                                    viewModel.permanentlyDelete(note)
                                } else {
                                    viewModel.moveToTrash(note)
                                }
                            },
                            onRestoreClick = { viewModel.restoreFromTrash(note.id) },
                            onDuplicateClick = { viewModel.duplicateNote(note) },
                            onChecklistToggle = { itemId ->
                                viewModel.toggleChecklistItemOnCard(note, itemId)
                            }
                        )
                    }
                }
            }
        }
    }

    // Confirmation dialog for Empty Trash
    if (showEmptyTrashDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyTrashDialog = false },
            title = {
                Text(
                    text = "Empty Recently Deleted?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = theme.getPrimaryText()
                )
            },
            text = {
                Text(
                    text = "All items in Recently Deleted will be permanently destroyed. This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = theme.getSecondaryText()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.emptyTrash()
                        showEmptyTrashDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Empty Trash", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyTrashDialog = false }) {
                    Text("Cancel", color = theme.getSecondaryText())
                }
            },
            containerColor = theme.getSurface()
        )
    }
}
