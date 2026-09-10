package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ChecklistItem
import com.example.data.NoteDatabase
import com.example.data.NoteEntity
import com.example.data.NoteRepository
import com.example.data.NoteSortOption
import com.example.data.NoteViewMode
import com.example.data.ThemeRepository
import com.example.sync.CloudSyncManager
import com.example.sync.GoogleAccountInfo
import com.example.sync.GoogleAuthManager
import com.example.sync.SyncState
import com.example.ui.theme.ThemeConfig
import com.example.ui.util.ReminderScheduler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

sealed interface AppScreen {
    object Home : AppScreen
    data class Edit(val noteId: Long? = null) : AppScreen
    object Settings : AppScreen
}

enum class NotesCategoryTab(val title: String) {
    ALL("All Notes"),
    FAVORITES("Favorites"),
    ARCHIVED("Archived"),
    TRASH("Recently Deleted")
}

class NotesViewModel(application: Application) : AndroidViewModel(application) {
    private val database = NoteDatabase.getDatabase(application)
    val repository = NoteRepository(database.noteDao(), application)
    val themeRepository = ThemeRepository(application)
    val currentTheme: StateFlow<ThemeConfig> = themeRepository.currentTheme

    // Google Auth & Cloud Sync
    val authManager = GoogleAuthManager(application)
    val syncManager = CloudSyncManager(application, repository, authManager)
    val connectedAccount: StateFlow<GoogleAccountInfo?> = authManager.account
    val syncState: StateFlow<SyncState> = syncManager.syncState

    val currentScreen = MutableStateFlow<AppScreen>(AppScreen.Home)

    // Filtering & Categories
    val selectedCategoryTab = MutableStateFlow(NotesCategoryTab.ALL)
    val selectedFolder = MutableStateFlow("All")
    val selectedTag = MutableStateFlow<String?>(null)
    val searchQuery = MutableStateFlow("")

    val sortOption: StateFlow<NoteSortOption> = themeRepository.sortOption
    val viewMode: StateFlow<NoteViewMode> = themeRepository.viewMode

    val activeNotes = repository.activeNotes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val archivedNotes = repository.archivedNotes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val deletedNotes = repository.deletedNotes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val favoriteNotes = repository.favoriteNotes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Dynamic folders list extracted from active notes + standard presets
    val availableFolders: StateFlow<List<String>> = activeNotes.combine(selectedFolder) { notes, _ ->
        val standard = listOf("All", "Notes", "Personal", "Work", "Ideas")
        val custom = notes.map { it.folder }.filter { it.isNotBlank() && !standard.contains(it) }.distinct()
        standard + custom
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf("All", "Notes", "Personal", "Work", "Ideas")
    )

    // Dynamic tags list extracted from active notes
    val availableTags: StateFlow<List<String>> = activeNotes.combine(selectedTag) { notes, _ ->
        notes.flatMap { it.tags }.filter { it.isNotBlank() }.distinct()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Base notes for currently selected category tab
    private val currentTabNotes: Flow<List<NoteEntity>> = combine(
        activeNotes,
        archivedNotes,
        deletedNotes,
        selectedCategoryTab
    ) { active, archived, deleted, tab ->
        when (tab) {
            NotesCategoryTab.ALL -> active
            NotesCategoryTab.FAVORITES -> active.filter { it.isFavorite }
            NotesCategoryTab.ARCHIVED -> archived
            NotesCategoryTab.TRASH -> deleted
        }
    }

    // Filtered & Sorted Notes for Home Screen
    val displayNotes: StateFlow<List<NoteEntity>> = combine(
        currentTabNotes,
        selectedFolder,
        selectedTag,
        searchQuery,
        sortOption
    ) { baseList, folder, tag, query, sort ->
        var filtered = baseList

        // Filter by folder (unless "All")
        if (folder != "All") {
            filtered = filtered.filter { it.folder.equals(folder, ignoreCase = true) }
        }

        // Filter by Tag
        if (tag != null && tag.isNotBlank()) {
            filtered = filtered.filter { it.tags.any { t -> t.equals(tag, ignoreCase = true) } }
        }

        // Search Query
        val trimmedQuery = query.trim()
        if (trimmedQuery.isNotEmpty()) {
            filtered = filtered.filter { note ->
                note.title.contains(trimmedQuery, ignoreCase = true) ||
                    note.content.contains(trimmedQuery, ignoreCase = true) ||
                    note.folder.contains(trimmedQuery, ignoreCase = true) ||
                    note.tags.any { it.contains(trimmedQuery, ignoreCase = true) } ||
                    note.checklistItems.any { it.text.contains(trimmedQuery, ignoreCase = true) }
            }
        }

        // Sort
        when (sort) {
            NoteSortOption.MODIFIED_DESC -> filtered.sortedWith(compareByDescending<NoteEntity> { it.isPinned }.thenByDescending { it.modifiedAt })
            NoteSortOption.MODIFIED_ASC -> filtered.sortedWith(compareByDescending<NoteEntity> { it.isPinned }.thenBy { it.modifiedAt })
            NoteSortOption.CREATED_DESC -> filtered.sortedWith(compareByDescending<NoteEntity> { it.isPinned }.thenByDescending { it.createdAt })
            NoteSortOption.TITLE_ASC -> filtered.sortedWith(compareByDescending<NoteEntity> { it.isPinned }.thenBy { it.title.lowercase() })
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Active Note State
    val activeNoteId = MutableStateFlow<Long?>(null)
    val activeTitle = MutableStateFlow("")
    val activeContent = MutableStateFlow("")
    val activeImagePaths = MutableStateFlow<List<String>>(emptyList())
    val activeChecklistItems = MutableStateFlow<List<ChecklistItem>>(emptyList())
    val activeIsPinned = MutableStateFlow(false)
    val activeIsFavorite = MutableStateFlow(false)
    val activeIsArchived = MutableStateFlow(false)
    val activeFolder = MutableStateFlow("Notes")
    val activeTags = MutableStateFlow<List<String>>(emptyList())
    val activeColorHex = MutableStateFlow<Long?>(null)
    val activeReminderTime = MutableStateFlow<Long?>(null)
    val activeCreatedAt = MutableStateFlow(System.currentTimeMillis())
    val activeModifiedAt = MutableStateFlow(System.currentTimeMillis())
    val isAutoSaved = MutableStateFlow(true)

    // Camera capture
    var pendingCameraUri: Uri? = null
    var pendingCameraFile: File? = null

    // Snackbar
    val snackbarMessage = MutableStateFlow<String?>(null)
    private var autoSaveJob: Job? = null

    fun openNote(note: NoteEntity?) {
        if (note != null) {
            activeNoteId.value = note.id
            activeTitle.value = note.title
            activeContent.value = note.content
            activeImagePaths.value = note.imagePaths
            activeChecklistItems.value = note.checklistItems
            activeIsPinned.value = note.isPinned
            activeIsFavorite.value = note.isFavorite
            activeIsArchived.value = note.isArchived
            activeFolder.value = note.folder.ifBlank { "Notes" }
            activeTags.value = note.tags
            activeColorHex.value = note.colorHex
            activeReminderTime.value = note.reminderTime
            activeCreatedAt.value = note.createdAt
            activeModifiedAt.value = note.modifiedAt
        } else {
            activeNoteId.value = null
            activeTitle.value = ""
            activeContent.value = ""
            activeImagePaths.value = emptyList()
            activeChecklistItems.value = emptyList()
            activeIsPinned.value = false
            activeIsFavorite.value = false
            activeIsArchived.value = false
            activeFolder.value = if (selectedFolder.value != "All") selectedFolder.value else "Notes"
            activeTags.value = if (selectedTag.value != null) listOf(selectedTag.value!!) else emptyList()
            activeColorHex.value = null
            activeReminderTime.value = null
            val now = System.currentTimeMillis()
            activeCreatedAt.value = now
            activeModifiedAt.value = now
        }
        isAutoSaved.value = true
        currentScreen.value = AppScreen.Edit(note?.id)
    }

    fun navigateToHome() {
        saveCurrentNoteImmediately()
        currentScreen.value = AppScreen.Home
    }

    fun navigateToSettings() {
        currentScreen.value = AppScreen.Settings
    }

    fun updateTitle(newTitle: String) {
        activeTitle.value = newTitle
        triggerAutoSave()
    }

    fun updateContent(newContent: String) {
        activeContent.value = newContent
        triggerAutoSave()
    }

    fun toggleActivePin() {
        activeIsPinned.value = !activeIsPinned.value
        triggerAutoSave()
    }

    fun toggleActiveFavorite() {
        activeIsFavorite.value = !activeIsFavorite.value
        triggerAutoSave()
    }

    fun toggleActiveArchive() {
        activeIsArchived.value = !activeIsArchived.value
        triggerAutoSave()
    }

    fun setActiveFolder(folder: String) {
        activeFolder.value = folder
        triggerAutoSave()
    }

    fun addActiveTag(tag: String) {
        val clean = tag.trim().removePrefix("#")
        if (clean.isNotBlank() && !activeTags.value.contains(clean)) {
            activeTags.value = activeTags.value + clean
            triggerAutoSave()
        }
    }

    fun removeActiveTag(tag: String) {
        activeTags.value = activeTags.value.filter { it != tag }
        triggerAutoSave()
    }

    fun setActiveColor(color: Long?) {
        activeColorHex.value = color
        triggerAutoSave()
    }

    fun setActiveReminder(timestamp: Long?) {
        activeReminderTime.value = timestamp
        triggerAutoSave()
        val currentId = activeNoteId.value ?: return
        if (timestamp != null && timestamp > System.currentTimeMillis()) {
            ReminderScheduler.scheduleReminder(
                getApplication(),
                currentId,
                activeTitle.value,
                activeContent.value,
                timestamp
            )
            snackbarMessage.value = "Reminder set"
        } else {
            ReminderScheduler.cancelReminder(getApplication(), currentId)
        }
    }

    // --- Note Actions from Card ---

    fun toggleNotePin(note: NoteEntity) {
        viewModelScope.launch {
            val updated = note.copy(isPinned = !note.isPinned, modifiedAt = System.currentTimeMillis())
            repository.update(updated)
            triggerBackgroundSync()
        }
    }

    fun toggleNoteFavorite(note: NoteEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(note)
            triggerBackgroundSync()
        }
    }

    fun toggleNoteArchive(note: NoteEntity) {
        viewModelScope.launch {
            repository.toggleArchive(note)
            snackbarMessage.value = if (!note.isArchived) "Note archived" else "Note unarchived"
            triggerBackgroundSync()
        }
    }

    fun duplicateNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.duplicateNote(note)
            snackbarMessage.value = "Note duplicated"
            triggerBackgroundSync()
        }
    }

    fun moveToTrash(note: NoteEntity) {
        viewModelScope.launch {
            repository.moveToTrash(note)
            snackbarMessage.value = "Moved to Recently Deleted"
            if (activeNoteId.value == note.id) {
                currentScreen.value = AppScreen.Home
            }
            triggerBackgroundSync()
        }
    }

    fun restoreFromTrash(noteId: Long) {
        viewModelScope.launch {
            repository.restoreFromTrash(noteId)
            snackbarMessage.value = "Note restored"
            triggerBackgroundSync()
        }
    }

    fun permanentlyDelete(note: NoteEntity) {
        viewModelScope.launch {
            repository.permanentlyDelete(note)
            snackbarMessage.value = "Note permanently deleted"
            triggerBackgroundSync()
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            repository.emptyTrash()
            snackbarMessage.value = "Recently Deleted emptied"
            triggerBackgroundSync()
        }
    }

    // --- Checklist Operations ---

    fun addChecklistItem(initialText: String = ""): String {
        val newItemId = UUID.randomUUID().toString()
        val newItem = ChecklistItem(id = newItemId, text = initialText, isCompleted = false)
        activeChecklistItems.value = activeChecklistItems.value + newItem
        triggerAutoSave()
        return newItemId
    }

    fun updateChecklistItemText(id: String, newText: String) {
        activeChecklistItems.value = activeChecklistItems.value.map { item ->
            if (item.id == id) item.copy(text = newText) else item
        }
        triggerAutoSave()
    }

    fun toggleChecklistItem(id: String) {
        activeChecklistItems.value = activeChecklistItems.value.map { item ->
            if (item.id == id) item.copy(isCompleted = !item.isCompleted) else item
        }
        triggerAutoSave()
    }

    fun deleteChecklistItem(id: String) {
        activeChecklistItems.value = activeChecklistItems.value.filter { it.id != id }
        triggerAutoSave()
    }

    fun moveChecklistItem(fromIndex: Int, toIndex: Int) {
        val list = activeChecklistItems.value.toMutableList()
        if (fromIndex in list.indices && toIndex in list.indices && fromIndex != toIndex) {
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
            activeChecklistItems.value = list
            triggerAutoSave()
        }
    }

    fun toggleChecklistItemOnCard(note: NoteEntity, itemId: String) {
        viewModelScope.launch {
            val updatedList = note.checklistItems.map { item ->
                if (item.id == itemId) item.copy(isCompleted = !item.isCompleted) else item
            }
            val updatedNote = note.copy(
                checklistItems = updatedList,
                modifiedAt = System.currentTimeMillis()
            )
            repository.update(updatedNote)
            triggerBackgroundSync()
        }
    }

    // --- Photos Operations ---

    fun addImages(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            val copiedPaths = uris.map { uri ->
                repository.saveImageLocally(uri)
            }
            activeImagePaths.value = activeImagePaths.value + copiedPaths
            triggerAutoSave()
        }
    }

    fun prepareCameraCapture(): Uri {
        val (uri, file) = repository.createCameraTempFile()
        pendingCameraUri = uri
        pendingCameraFile = file
        return uri
    }

    fun onCameraImageCaptured(success: Boolean) {
        if (success) {
            val file = pendingCameraFile ?: return
            viewModelScope.launch {
                val permanentPath = repository.persistCameraImage(file)
                activeImagePaths.value = activeImagePaths.value + permanentPath
                triggerAutoSave()
                pendingCameraFile = null
                pendingCameraUri = null
            }
        } else {
            pendingCameraFile?.delete()
            pendingCameraFile = null
            pendingCameraUri = null
        }
    }

    fun removeImage(index: Int) {
        val current = activeImagePaths.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            activeImagePaths.value = current
            triggerAutoSave()
        }
    }

    fun moveImage(fromIndex: Int, toIndex: Int) {
        val current = activeImagePaths.value.toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices && fromIndex != toIndex) {
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            activeImagePaths.value = current
            triggerAutoSave()
        }
    }

    // --- Save & Persistence ---

    private fun triggerAutoSave() {
        if (!themeRepository.autoSaveEnabled.value) {
            isAutoSaved.value = false
            return
        }
        isAutoSaved.value = false
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(400)
            saveCurrentNoteImmediately()
        }
    }

    fun saveCurrentNoteImmediately() {
        val title = activeTitle.value.trim()
        val content = activeContent.value.trim()
        val images = activeImagePaths.value
        val checklists = activeChecklistItems.value
        val isPinned = activeIsPinned.value
        val isFav = activeIsFavorite.value
        val isArch = activeIsArchived.value
        val folder = activeFolder.value.ifBlank { "Notes" }
        val tags = activeTags.value
        val color = activeColorHex.value
        val reminder = activeReminderTime.value
        val currentId = activeNoteId.value

        if (title.isEmpty() && content.isEmpty() && images.isEmpty() && checklists.isEmpty()) {
            if (currentId != null) {
                viewModelScope.launch {
                    repository.permanentlyDeleteById(currentId)
                    activeNoteId.value = null
                }
            }
            isAutoSaved.value = true
            return
        }

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            activeModifiedAt.value = now
            if (currentId == null) {
                val newNote = NoteEntity(
                    title = activeTitle.value,
                    content = activeContent.value,
                    imagePaths = images,
                    checklistItems = checklists,
                    isPinned = isPinned,
                    isFavorite = isFav,
                    isArchived = isArch,
                    folder = folder,
                    tags = tags,
                    colorHex = color,
                    reminderTime = reminder,
                    createdAt = activeCreatedAt.value,
                    modifiedAt = now
                )
                val newId = repository.insert(newNote)
                activeNoteId.value = newId
            } else {
                val existing = NoteEntity(
                    id = currentId,
                    title = activeTitle.value,
                    content = activeContent.value,
                    imagePaths = images,
                    checklistItems = checklists,
                    isPinned = isPinned,
                    isFavorite = isFav,
                    isArchived = isArch,
                    folder = folder,
                    tags = tags,
                    colorHex = color,
                    reminderTime = reminder,
                    createdAt = activeCreatedAt.value,
                    modifiedAt = now
                )
                repository.update(existing)
            }
            isAutoSaved.value = true
            triggerBackgroundSync()
        }
    }

    private fun triggerBackgroundSync() {
        viewModelScope.launch {
            if (syncManager.syncState.value.isAutoSyncEnabled && authManager.account.value != null) {
                syncManager.syncNow()
            }
        }
    }

    // --- Sharing & Formatting ---

    fun shareNote(context: Context, note: NoteEntity) {
        val builder = StringBuilder()
        if (note.title.isNotBlank()) {
            builder.append(note.title).append("\n\n")
        }
        if (note.content.isNotBlank()) {
            builder.append(note.content).append("\n\n")
        }
        if (note.checklistItems.isNotEmpty()) {
            builder.append("Checklist:\n")
            note.checklistItems.forEach { item ->
                val marker = if (item.isCompleted) "[✓]" else "[ ]"
                builder.append("$marker ${item.text}\n")
            }
        }
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, builder.toString().trim())
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Note")
        context.startActivity(shareIntent)
    }

    fun shareCurrentNote(context: Context) {
        saveCurrentNoteImmediately()
        val currentId = activeNoteId.value
        val note = NoteEntity(
            id = currentId ?: 0,
            title = activeTitle.value,
            content = activeContent.value,
            checklistItems = activeChecklistItems.value,
            imagePaths = activeImagePaths.value
        )
        shareNote(context, note)
    }

    // --- Google Auth & Sync Actions ---

    fun signInWithGoogle(activityContext: Context) {
        viewModelScope.launch {
            val result = authManager.signInWithCredentialManager(activityContext)
            result.onSuccess {
                snackbarMessage.value = "Connected as ${it.displayName}"
                syncManager.syncNow()
            }.onFailure {
                snackbarMessage.value = "Sign in failed: ${it.localizedMessage}"
            }
        }
    }

    fun signOutGoogle() {
        authManager.signOut()
        snackbarMessage.value = "Disconnected Google Account"
    }

    fun syncNow() {
        viewModelScope.launch {
            val res = syncManager.syncNow()
            if (res.isSuccess) {
                snackbarMessage.value = "Notes synchronized with cloud"
            } else {
                snackbarMessage.value = "Sync failed: ${res.exceptionOrNull()?.message}"
            }
        }
    }

    fun backupToCloud() {
        viewModelScope.launch {
            val res = syncManager.backupToCloud()
            if (res.isSuccess) {
                snackbarMessage.value = "Cloud backup completed (${res.getOrNull()} notes)"
            } else {
                snackbarMessage.value = "Backup failed"
            }
        }
    }

    fun restoreFromCloud() {
        viewModelScope.launch {
            val res = syncManager.restoreFromCloud()
            if (res.isSuccess) {
                snackbarMessage.value = "Restored ${res.getOrNull()} notes from cloud"
            } else {
                snackbarMessage.value = "Restore failed"
            }
        }
    }

    fun setAutoSync(enabled: Boolean) {
        syncManager.setAutoSync(enabled)
    }

    // --- Export & Import ---

    fun exportBackup(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportNotesAsJson()
            onReady(json)
            snackbarMessage.value = "Notes backup generated"
        }
    }

    fun importBackup(jsonString: String) {
        viewModelScope.launch {
            val count = repository.importNotesFromJson(jsonString)
            if (count > 0) {
                snackbarMessage.value = "Successfully imported $count notes"
                triggerBackgroundSync()
            } else {
                snackbarMessage.value = "Invalid backup format"
            }
        }
    }

    fun clearSnackbarMessage() {
        snackbarMessage.value = null
    }
}
