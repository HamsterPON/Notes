package com.example.ui.screens

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.components.ChecklistSection
import com.example.ui.components.NoteFormattingToolbar
import com.example.ui.components.PhotoViewDialog
import com.example.ui.components.PresetNoteColors
import com.example.ui.theme.LocalAppTheme
import com.example.ui.util.DateFormatter
import com.example.ui.viewmodel.NotesViewModel
import java.io.File
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteEditScreen(
    viewModel: NotesViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppTheme.current
    val context = LocalContext.current

    val title by viewModel.activeTitle.collectAsState()
    val content by viewModel.activeContent.collectAsState()
    val imagePaths by viewModel.activeImagePaths.collectAsState()
    val checklistItems by viewModel.activeChecklistItems.collectAsState()
    val isPinned by viewModel.activeIsPinned.collectAsState()
    val isFavorite by viewModel.activeIsFavorite.collectAsState()
    val isArchived by viewModel.activeIsArchived.collectAsState()
    val folder by viewModel.activeFolder.collectAsState()
    val tags by viewModel.activeTags.collectAsState()
    val colorHex by viewModel.activeColorHex.collectAsState()
    val reminderTime by viewModel.activeReminderTime.collectAsState()
    val modifiedAt by viewModel.activeModifiedAt.collectAsState()
    val isAutoSaved by viewModel.isAutoSaved.collectAsState()
    val availableFolders by viewModel.availableFolders.collectAsState()

    var showMediaSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var previewPhotoPath by remember { mutableStateOf<String?>(null) }

    // Dialog states for metadata
    var showColorDialog by remember { mutableStateOf(false) }
    var showFolderDialog by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }
    var newTagInput by remember { mutableStateOf("") }
    var customFolderInput by remember { mutableStateOf("") }

    // Intercept hardware back button
    BackHandler {
        onBack()
    }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.addImages(uris)
        }
    }

    // Camera picture launcher
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        viewModel.onCameraImageCaptured(success)
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val uri = viewModel.prepareCameraCapture()
            takePictureLauncher.launch(uri)
        }
    }

    fun launchCamera() {
        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            val uri = viewModel.prepareCameraCapture()
            takePictureLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun openDateTimePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val timeCalendar = Calendar.getInstance()
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        calendar.set(year, month, dayOfMonth, hourOfDay, minute, 0)
                        viewModel.setActiveReminder(calendar.timeInMillis)
                    },
                    timeCalendar.get(Calendar.HOUR_OF_DAY),
                    timeCalendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // Background color: Note specific color tint or theme background
    val screenBackground = if (colorHex != null && colorHex!! > 0) {
        Color(colorHex!!)
    } else {
        theme.getBackground()
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        containerColor = screenBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("edit_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = theme.getPrimaryText()
                        )
                    }

                    // Auto-saved minimalist indicator
                    AnimatedVisibility(
                        visible = isAutoSaved,
                        enter = fadeIn() + scaleIn(initialScale = 0.85f),
                        exit = fadeOut()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.getSurface())
                                .border(1.dp, theme.getBorder(), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Saved",
                                tint = theme.getSecondaryText(),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Saved",
                                style = MaterialTheme.typography.labelMedium,
                                color = theme.getSecondaryText(),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Favorite button
                    IconButton(
                        onClick = { viewModel.toggleActiveFavorite() },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color(0xFFF43F5E) else theme.getSecondaryText().copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Pin button
                    IconButton(
                        onClick = { viewModel.toggleActivePin() },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("edit_pin_button")
                    ) {
                        Icon(
                            imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = if (isPinned) "Unpin" else "Pin",
                            tint = if (isPinned) theme.getAccent() else theme.getSecondaryText().copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Share note button
                    IconButton(
                        onClick = { viewModel.shareCurrentNote(context) },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = theme.getSecondaryText().copy(alpha = 0.6f),
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Archive note button
                    IconButton(
                        onClick = {
                            viewModel.toggleActiveArchive()
                            onBack()
                        },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = if (isArchived) Icons.Filled.Archive else Icons.Outlined.Archive,
                            contentDescription = "Archive",
                            tint = if (isArchived) theme.getAccent() else theme.getSecondaryText().copy(alpha = 0.6f),
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Delete note button
                    IconButton(
                        onClick = {
                            val currentId = viewModel.activeNoteId.value
                            if (currentId != null) {
                                val n = viewModel.activeNotes.value.find { it.id == currentId }
                                if (n != null) {
                                    viewModel.moveToTrash(n)
                                } else {
                                    viewModel.navigateToHome()
                                }
                            } else {
                                viewModel.navigateToHome()
                            }
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("edit_delete_note_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Delete note",
                            tint = theme.getSecondaryText().copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            // Formatting & Document Editor Action Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                HorizontalDivider(color = theme.getBorder(), thickness = 1.dp)

                NoteFormattingToolbar(
                    onHeadingClick = {
                        val current = viewModel.activeContent.value
                        viewModel.updateContent("# $current")
                    },
                    onBoldClick = {
                        val current = viewModel.activeContent.value
                        viewModel.updateContent("$current **bold text** ")
                    },
                    onItalicClick = {
                        val current = viewModel.activeContent.value
                        viewModel.updateContent("$current *italic text* ")
                    },
                    onBulletClick = {
                        val current = viewModel.activeContent.value
                        viewModel.updateContent("$current\n• ")
                    },
                    onNumberedClick = {
                        val current = viewModel.activeContent.value
                        viewModel.updateContent("$current\n1. ")
                    },
                    onQuoteClick = {
                        val current = viewModel.activeContent.value
                        viewModel.updateContent("$current\n> ")
                    },
                    onAddChecklistClick = {
                        viewModel.addChecklistItem("")
                    },
                    onColorClick = {
                        showColorDialog = true
                    },
                    onFolderClick = {
                        showFolderDialog = true
                    },
                    onTagClick = {
                        showTagDialog = true
                    },
                    onReminderClick = {
                        openDateTimePicker()
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            // Metadata chips row (Folder, Tags, Reminder)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                // Folder chip
                Surface(
                    onClick = { showFolderDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    color = theme.getSurface(),
                    border = BorderStroke(1.dp, theme.getBorder())
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = theme.getSecondaryText(),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = folder,
                            style = MaterialTheme.typography.labelMedium,
                            color = theme.getPrimaryText(),
                            fontSize = 11.sp
                        )
                    }
                }

                // Reminder chip (if set)
                if (reminderTime != null && reminderTime!! > 0) {
                    Surface(
                        onClick = { openDateTimePicker() },
                        shape = RoundedCornerShape(10.dp),
                        color = theme.getAccent().copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, theme.getAccent().copy(alpha = 0.35f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = theme.getAccent(),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = DateFormatter.formatDetailDate(reminderTime!!),
                                style = MaterialTheme.typography.labelMedium,
                                color = theme.getAccent(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear reminder",
                                tint = theme.getAccent(),
                                modifier = Modifier
                                    .size(12.dp)
                                    .clickable { viewModel.setActiveReminder(null) }
                            )
                        }
                    }
                }

                // Tags chips
                tags.forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = theme.getSurface(),
                        border = BorderStroke(1.dp, theme.getBorder())
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelMedium,
                                color = theme.getPrimaryText(),
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove tag",
                                tint = theme.getSecondaryText(),
                                modifier = Modifier
                                    .size(12.dp)
                                    .clickable { viewModel.removeActiveTag(tag) }
                            )
                        }
                    }
                }
            }

            // Note Title
            BasicTextField(
                value = title,
                onValueChange = { viewModel.updateTitle(it) },
                textStyle = MaterialTheme.typography.headlineLarge.copy(
                    color = theme.getPrimaryText(),
                    fontWeight = FontWeight.SemiBold
                ),
                cursorBrush = SolidColor(theme.getPrimaryText()),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (title.isEmpty()) {
                            Text(
                                text = "Untitled",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    color = theme.getSecondaryText().copy(alpha = 0.45f),
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_title_input")
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle with modified date
            Text(
                text = "Edited ${DateFormatter.formatDetailDate(modifiedAt)}",
                style = MaterialTheme.typography.labelMedium,
                color = theme.getSecondaryText().copy(alpha = 0.6f),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Attached Photos Section
            if (imagePaths.isNotEmpty()) {
                Text(
                    text = "PHOTOS (${imagePaths.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = theme.getSecondaryText(),
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(end = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                ) {
                    itemsIndexed(imagePaths, key = { index, path -> "$path-$index" }) { index, path ->
                        EditablePhotoCard(
                            path = path,
                            canMoveLeft = index > 0,
                            canMoveRight = index < imagePaths.size - 1,
                            onMoveLeft = { viewModel.moveImage(index, index - 1) },
                            onMoveRight = { viewModel.moveImage(index, index + 1) },
                            onDelete = { viewModel.removeImage(index) },
                            onViewFull = { previewPhotoPath = path }
                        )
                    }

                    item {
                        // Quick Add photo card
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(150.dp)
                                .clip(RoundedCornerShape((theme.borderRadiusDp * 0.65f).dp))
                                .background(theme.getSurface())
                                .border(BorderStroke(1.dp, theme.getBorder()), RoundedCornerShape((theme.borderRadiusDp * 0.65f).dp))
                                .clickable { showMediaSheet = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = "Add photo",
                                    tint = theme.getSecondaryText(),
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Add photo",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = theme.getSecondaryText(),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Interactive Checklists Section
            ChecklistSection(
                items = checklistItems,
                onToggle = { viewModel.toggleChecklistItem(it) },
                onTextChange = { id, text -> viewModel.updateChecklistItemText(id, text) },
                onDelete = { viewModel.deleteChecklistItem(it) },
                onMove = { from, to -> viewModel.moveChecklistItem(from, to) },
                onAddItem = { viewModel.addChecklistItem("") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Note Content Paragraph Input
            BasicTextField(
                value = content,
                onValueChange = { viewModel.updateContent(it) },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = theme.getPrimaryText(),
                    lineHeight = 26.sp
                ),
                cursorBrush = SolidColor(theme.getPrimaryText()),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 64.dp)
                    ) {
                        if (content.isEmpty()) {
                            Text(
                                text = "Start writing your thoughts, document details, or notes...",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = theme.getSecondaryText().copy(alpha = 0.5f),
                                    lineHeight = 26.sp
                                )
                            )
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_content_input")
            )
        }
    }

    // Modal Bottom Sheet for Media Sources
    if (showMediaSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMediaSheet = false },
            sheetState = sheetState,
            containerColor = theme.getSurface(),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "Add Photo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = theme.getPrimaryText(),
                    modifier = Modifier.padding(bottom = 18.dp)
                )

                // Option 1: Camera
                Surface(
                    onClick = {
                        showMediaSheet = false
                        launchCamera()
                    },
                    shape = RoundedCornerShape(18.dp),
                    color = theme.getBackground(),
                    border = BorderStroke(1.dp, theme.getBorder()),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("option_take_photo")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CameraAlt,
                            contentDescription = "Camera",
                            tint = theme.getPrimaryText(),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Take Photo",
                                style = MaterialTheme.typography.titleMedium,
                                color = theme.getPrimaryText(),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Capture instantly with your camera",
                                style = MaterialTheme.typography.bodyMedium,
                                color = theme.getSecondaryText()
                            )
                        }
                    }
                }

                // Option 2: Gallery
                Surface(
                    onClick = {
                        showMediaSheet = false
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = RoundedCornerShape(18.dp),
                    color = theme.getBackground(),
                    border = BorderStroke(1.dp, theme.getBorder()),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                        .testTag("option_choose_gallery")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PhotoLibrary,
                            contentDescription = "Gallery",
                            tint = theme.getPrimaryText(),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Choose from Library",
                                style = MaterialTheme.typography.titleMedium,
                                color = theme.getPrimaryText(),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Pick single or multiple images",
                                style = MaterialTheme.typography.bodyMedium,
                                color = theme.getSecondaryText()
                            )
                        }
                    }
                }
            }
        }
    }

    // Color Tint Dialog
    if (showColorDialog) {
        AlertDialog(
            onDismissRequest = { showColorDialog = false },
            title = {
                Text(
                    text = "Note Color Tint",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = theme.getPrimaryText()
                )
            },
            text = {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PresetNoteColors.forEach { (colorValue, name) ->
                        val isSelected = colorHex == colorValue
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (colorValue != null) Color(colorValue) else theme.getSurface())
                                .border(
                                    BorderStroke(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) theme.getAccent() else theme.getBorder()
                                    ),
                                    CircleShape
                                )
                                .clickable {
                                    viewModel.setActiveColor(colorValue)
                                    showColorDialog = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = name,
                                    tint = theme.getPrimaryText(),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showColorDialog = false }) {
                    Text("Done", color = theme.getAccent())
                }
            },
            containerColor = theme.getSurface()
        )
    }

    // Folder Selector Dialog
    if (showFolderDialog) {
        AlertDialog(
            onDismissRequest = { showFolderDialog = false },
            title = {
                Text(
                    text = "Select Folder",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = theme.getPrimaryText()
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    availableFolders.filter { it != "All" }.forEach { f ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    viewModel.setActiveFolder(f)
                                    showFolderDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = if (folder == f) theme.getAccent() else theme.getSecondaryText(),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = f,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (folder == f) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (folder == f) theme.getAccent() else theme.getPrimaryText()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customFolderInput,
                        onValueChange = { customFolderInput = it },
                        label = { Text("Or create new folder") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customFolderInput.isNotBlank()) {
                            viewModel.setActiveFolder(customFolderInput.trim())
                            customFolderInput = ""
                        }
                        showFolderDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.getButton())
                ) {
                    Text("Apply", color = theme.getButtonText())
                }
            },
            dismissButton = {
                TextButton(onClick = { showFolderDialog = false }) {
                    Text("Cancel", color = theme.getSecondaryText())
                }
            },
            containerColor = theme.getSurface()
        )
    }

    // Tag Adder Dialog
    if (showTagDialog) {
        AlertDialog(
            onDismissRequest = { showTagDialog = false },
            title = {
                Text(
                    text = "Add Tag",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = theme.getPrimaryText()
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = newTagInput,
                        onValueChange = { newTagInput = it },
                        label = { Text("Tag name (e.g. project, idea)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTagInput.isNotBlank()) {
                            viewModel.addActiveTag(newTagInput)
                            newTagInput = ""
                        }
                        showTagDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.getButton())
                ) {
                    Text("Add", color = theme.getButtonText())
                }
            },
            dismissButton = {
                TextButton(onClick = { showTagDialog = false }) {
                    Text("Cancel", color = theme.getSecondaryText())
                }
            },
            containerColor = theme.getSurface()
        )
    }

    // Full screen photo dialog
    previewPhotoPath?.let { path ->
        PhotoViewDialog(
            photoPath = path,
            onDismiss = { previewPhotoPath = null },
            onDeletePhoto = {
                val index = imagePaths.indexOf(path)
                if (index != -1) {
                    viewModel.removeImage(index)
                }
            }
        )
    }
}

@Composable
fun EditablePhotoCard(
    path: String,
    canMoveLeft: Boolean,
    canMoveRight: Boolean,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onDelete: () -> Unit,
    onViewFull: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalAppTheme.current
    val context = LocalContext.current
    val cornerRadius = (theme.borderRadiusDp * 0.65f).dp

    Box(
        modifier = modifier
            .width(135.dp)
            .height(150.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .border(BorderStroke(1.dp, theme.getBorder()), RoundedCornerShape(cornerRadius))
            .clickable { onViewFull() }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(File(path))
                .crossfade(true)
                .build(),
            contentDescription = "Photo thumbnail",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Delete button
        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(26.dp)
                .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                .testTag("delete_photo_thumb")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete photo",
                tint = Color.White,
                modifier = Modifier.size(15.dp)
            )
        }

        // Reorder arrows
        if (canMoveLeft || canMoveRight) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (canMoveLeft) {
                    IconButton(
                        onClick = onMoveLeft,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Move left",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(26.dp))
                }

                if (canMoveRight) {
                    IconButton(
                        onClick = onMoveRight,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                            contentDescription = "Move right",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(26.dp))
                }
            }
        }
    }
}
