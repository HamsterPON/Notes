package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NoteSortOption
import com.example.data.NoteViewMode
import com.example.ui.components.ColorPickerSheet
import com.example.ui.components.ThemePreviewCard
import com.example.ui.components.ThemeSelectorRow
import com.example.ui.theme.BaseThemeType
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.ThemeConfig
import com.example.ui.theme.themeCardBackground
import com.example.ui.theme.toHexColorString
import com.example.ui.util.DateFormatter
import com.example.ui.viewmodel.NotesViewModel

@Composable
fun SettingsScreen(
    viewModel: NotesViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val themeRepository = viewModel.themeRepository
    val currentTheme by themeRepository.currentTheme.collectAsState()
    val savedCustomThemes by themeRepository.savedCustomThemes.collectAsState()

    // Preferences
    val sortOption by viewModel.sortOption.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val autoSaveEnabled by themeRepository.autoSaveEnabled.collectAsState()
    val confirmDelete by themeRepository.confirmDeleteEnabled.collectAsState()

    // Google Sync
    val connectedAccount by viewModel.connectedAccount.collectAsState()
    val syncState by viewModel.syncState.collectAsState()

    var activeColorTarget by remember { mutableStateOf<String?>(null) }
    var activeColorInitialHex by remember { mutableStateOf(0L) }
    var activeColorTitle by remember { mutableStateOf("") }

    var showSaveCustomDialog by remember { mutableStateOf(false) }
    var customThemeNameInput by remember { mutableStateOf("") }
    var renameTargetTheme by remember { mutableStateOf<ThemeConfig?>(null) }
    var renameInput by remember { mutableStateOf("") }
    var deleteTargetTheme by remember { mutableStateOf<ThemeConfig?>(null) }

    // Backup import dialog
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonInput by remember { mutableStateOf("") }

    // Sort dropdown
    var showSortMenu by remember { mutableStateOf(false) }

    fun openColorPicker(title: String, target: String, currentHex: Long) {
        activeColorTitle = title
        activeColorTarget = target
        activeColorInitialHex = currentHex
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        containerColor = currentTheme.getBackground(),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = currentTheme.getPrimaryText()
                        )
                    }

                    Text(
                        text = "Settings & Themes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = currentTheme.getPrimaryText()
                    )
                }

                IconButton(
                    onClick = { themeRepository.resetToPreset(currentTheme.baseType) },
                    modifier = Modifier.testTag("btn_reset_theme")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset theme",
                        tint = currentTheme.getSecondaryText()
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Live UI Preview Component
            ThemePreviewCard(
                theme = currentTheme,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Section: Google Account & Cloud Sync
            SettingsSectionHeader(
                title = "GOOGLE ACCOUNT & CLOUD SYNC",
                subtitle = "Synchronize your notes and checklists across devices",
                textColor = currentTheme.getPrimaryText(),
                mutedColor = currentTheme.getSecondaryText()
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .themeCardBackground(currentTheme)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (connectedAccount != null) {
                    // Connected User Card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(currentTheme.getAccent().copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = currentTheme.getAccent(),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = connectedAccount!!.displayName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = currentTheme.getPrimaryText()
                                )
                                Text(
                                    text = connectedAccount!!.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = currentTheme.getSecondaryText()
                                )
                            }
                        }

                        TextButton(onClick = { viewModel.signOutGoogle() }) {
                            Text("Sign Out", color = Color(0xFFEF4444), fontSize = 12.sp)
                        }
                    }

                    HorizontalDivider(color = currentTheme.getBorder().copy(alpha = 0.5f))

                    // Sync Status & Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (syncState.isSyncing) "Syncing in progress..." else "Sync status: Up to date",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = currentTheme.getPrimaryText()
                            )
                            Text(
                                text = if (syncState.lastSyncTimestamp > 0) {
                                    "Last synced ${DateFormatter.formatModifiedDate(syncState.lastSyncTimestamp)}"
                                } else {
                                    "Not synced yet"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = currentTheme.getSecondaryText()
                            )
                        }

                        Button(
                            onClick = { viewModel.syncNow() },
                            enabled = !syncState.isSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = currentTheme.getButton()),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = if (syncState.isSyncing) Icons.Default.Sync else Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = currentTheme.getButtonText(),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sync Now", color = currentTheme.getButtonText(), fontSize = 12.sp)
                        }
                    }

                    HorizontalDivider(color = currentTheme.getBorder().copy(alpha = 0.5f))

                    // Auto-sync switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Auto-sync Changes",
                                style = MaterialTheme.typography.titleSmall,
                                color = currentTheme.getPrimaryText()
                            )
                            Text(
                                text = "Sync notes automatically on edit",
                                style = MaterialTheme.typography.bodySmall,
                                color = currentTheme.getSecondaryText()
                            )
                        }

                        Switch(
                            checked = syncState.isAutoSyncEnabled,
                            onCheckedChange = { viewModel.setAutoSync(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = currentTheme.getButtonText(),
                                checkedTrackColor = currentTheme.getButton()
                            )
                        )
                    }

                    // Cloud backup & restore buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.backupToCloud() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cloud Backup", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.restoreFromCloud() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CloudDownload, null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cloud Restore", fontSize = 12.sp)
                        }
                    }
                } else {
                    // Sign In prompt
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Sign in to backup and synchronize your notes seamlessly across all your devices.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = currentTheme.getSecondaryText(),
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Button(
                            onClick = { viewModel.signInWithGoogle(context) },
                            colors = ButtonDefaults.buttonColors(containerColor = currentTheme.getButton()),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = currentTheme.getButtonText(),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Sign in with Google",
                                color = currentTheme.getButtonText(),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section: General Productivity Settings
            SettingsSectionHeader(
                title = "PRODUCTIVITY & BEHAVIOR",
                subtitle = "Configure organization, sorting, and defaults",
                textColor = currentTheme.getPrimaryText(),
                mutedColor = currentTheme.getSecondaryText()
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .themeCardBackground(currentTheme)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Default View Mode (Grid vs List)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Default Layout",
                            style = MaterialTheme.typography.titleSmall,
                            color = currentTheme.getPrimaryText()
                        )
                        Text(
                            text = if (viewMode == NoteViewMode.GRID) "2-column staggered grid" else "Single column document list",
                            style = MaterialTheme.typography.bodySmall,
                            color = currentTheme.getSecondaryText()
                        )
                    }

                    Row {
                        Surface(
                            onClick = { themeRepository.setViewMode(NoteViewMode.GRID) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (viewMode == NoteViewMode.GRID) currentTheme.getButton() else currentTheme.getSurface(),
                            border = BorderStroke(1.dp, currentTheme.getBorder()),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = "Grid",
                                tint = if (viewMode == NoteViewMode.GRID) currentTheme.getButtonText() else currentTheme.getPrimaryText(),
                                modifier = Modifier.padding(8.dp).size(18.dp)
                            )
                        }

                        Surface(
                            onClick = { themeRepository.setViewMode(NoteViewMode.LIST) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (viewMode == NoteViewMode.LIST) currentTheme.getButton() else currentTheme.getSurface(),
                            border = BorderStroke(1.dp, currentTheme.getBorder())
                        ) {
                            Icon(
                                imageVector = Icons.Default.ViewAgenda,
                                contentDescription = "List",
                                tint = if (viewMode == NoteViewMode.LIST) currentTheme.getButtonText() else currentTheme.getPrimaryText(),
                                modifier = Modifier.padding(8.dp).size(18.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = currentTheme.getBorder().copy(alpha = 0.5f))

                // Default Sort Option
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Default Note Sorting",
                            style = MaterialTheme.typography.titleSmall,
                            color = currentTheme.getPrimaryText()
                        )
                        Text(
                            text = when (sortOption) {
                                NoteSortOption.MODIFIED_DESC -> "Modified (Newest first)"
                                NoteSortOption.MODIFIED_ASC -> "Modified (Oldest first)"
                                NoteSortOption.CREATED_DESC -> "Created (Newest first)"
                                NoteSortOption.TITLE_ASC -> "Title (Alphabetical)"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = currentTheme.getSecondaryText()
                        )
                    }

                    Box {
                        OutlinedButton(
                            onClick = { showSortMenu = true },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Change", fontSize = 12.sp)
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier.background(currentTheme.getSurface())
                        ) {
                            NoteSortOption.values().forEach { opt ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            when (opt) {
                                                NoteSortOption.MODIFIED_DESC -> "Modified (Newest first)"
                                                NoteSortOption.MODIFIED_ASC -> "Modified (Oldest first)"
                                                NoteSortOption.CREATED_DESC -> "Created (Newest first)"
                                                NoteSortOption.TITLE_ASC -> "Title (A to Z)"
                                            }
                                        )
                                    },
                                    onClick = {
                                        themeRepository.setSortOption(opt)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = currentTheme.getBorder().copy(alpha = 0.5f))

                // Auto-save toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Auto-save Notes",
                            style = MaterialTheme.typography.titleSmall,
                            color = currentTheme.getPrimaryText()
                        )
                        Text(
                            text = "Save changes in real time while typing",
                            style = MaterialTheme.typography.bodySmall,
                            color = currentTheme.getSecondaryText()
                        )
                    }

                    Switch(
                        checked = autoSaveEnabled,
                        onCheckedChange = { themeRepository.setAutoSave(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = currentTheme.getButtonText(),
                            checkedTrackColor = currentTheme.getButton()
                        )
                    )
                }

                HorizontalDivider(color = currentTheme.getBorder().copy(alpha = 0.5f))

                // Confirm before delete toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Confirm Deletion",
                            style = MaterialTheme.typography.titleSmall,
                            color = currentTheme.getPrimaryText()
                        )
                        Text(
                            text = "Ask before moving notes to Recently Deleted",
                            style = MaterialTheme.typography.bodySmall,
                            color = currentTheme.getSecondaryText()
                        )
                    }

                    Switch(
                        checked = confirmDelete,
                        onCheckedChange = { themeRepository.setConfirmDelete(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = currentTheme.getButtonText(),
                            checkedTrackColor = currentTheme.getButton()
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section: Built-in Themes
            SettingsSectionHeader(
                title = "THEMES",
                subtitle = "Select a built-in visual aesthetic style",
                textColor = currentTheme.getPrimaryText(),
                mutedColor = currentTheme.getSecondaryText()
            )

            ThemeSelectorRow(
                selectedType = currentTheme.baseType,
                onSelectPreset = { presetType ->
                    themeRepository.selectPreset(presetType)
                },
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Section: Customization (Colors)
            SettingsSectionHeader(
                title = "COLORS",
                subtitle = "Customizable color combinations with live updating",
                textColor = currentTheme.getPrimaryText(),
                mutedColor = currentTheme.getSecondaryText()
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .themeCardBackground(currentTheme)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ColorSettingItem(
                    label = "Background Color",
                    colorHex = currentTheme.backgroundColor,
                    textColor = currentTheme.getPrimaryText(),
                    onClick = {
                        openColorPicker("Background Color", "bg", currentTheme.backgroundColor)
                    }
                )

                HorizontalDivider(color = currentTheme.getBorder().copy(alpha = 0.5f))

                ColorSettingItem(
                    label = "Card & Surface Color",
                    colorHex = currentTheme.surfaceColor,
                    textColor = currentTheme.getPrimaryText(),
                    onClick = {
                        openColorPicker("Card & Surface Color", "surface", currentTheme.surfaceColor)
                    }
                )

                HorizontalDivider(color = currentTheme.getBorder().copy(alpha = 0.5f))

                ColorSettingItem(
                    label = "Primary Text Color",
                    colorHex = currentTheme.primaryTextColor,
                    textColor = currentTheme.getPrimaryText(),
                    onClick = {
                        openColorPicker("Primary Text Color", "text_primary", currentTheme.primaryTextColor)
                    }
                )

                HorizontalDivider(color = currentTheme.getBorder().copy(alpha = 0.5f))

                ColorSettingItem(
                    label = "Secondary Text Color",
                    colorHex = currentTheme.secondaryTextColor,
                    textColor = currentTheme.getPrimaryText(),
                    onClick = {
                        openColorPicker("Secondary Text Color", "text_sec", currentTheme.secondaryTextColor)
                    }
                )

                HorizontalDivider(color = currentTheme.getBorder().copy(alpha = 0.5f))

                ColorSettingItem(
                    label = "Accent Color",
                    colorHex = currentTheme.accentColor,
                    textColor = currentTheme.getPrimaryText(),
                    onClick = {
                        openColorPicker("Accent Color", "accent", currentTheme.accentColor)
                    }
                )

                HorizontalDivider(color = currentTheme.getBorder().copy(alpha = 0.5f))

                ColorSettingItem(
                    label = "Button Color",
                    colorHex = currentTheme.buttonColor,
                    textColor = currentTheme.getPrimaryText(),
                    onClick = {
                        openColorPicker("Button Color", "btn", currentTheme.buttonColor)
                    }
                )

                HorizontalDivider(color = currentTheme.getBorder().copy(alpha = 0.5f))

                ColorSettingItem(
                    label = "Border Color",
                    colorHex = currentTheme.borderColor,
                    textColor = currentTheme.getPrimaryText(),
                    onClick = {
                        openColorPicker("Border Color", "border", currentTheme.borderColor)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section: Effects & Geometry
            SettingsSectionHeader(
                title = "EFFECTS & STYLING",
                subtitle = "Adjust geometry, transparency, shadows, and glow",
                textColor = currentTheme.getPrimaryText(),
                mutedColor = currentTheme.getSecondaryText()
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .themeCardBackground(currentTheme)
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Border Radius Slider
                SliderSettingItem(
                    label = "Border Radius",
                    valueText = "${currentTheme.borderRadiusDp.toInt()} dp",
                    value = currentTheme.borderRadiusDp,
                    range = 8f..32f,
                    onValueChange = { value ->
                        themeRepository.updateCurrentTheme { cur -> cur.copy(borderRadiusDp = value) }
                    },
                    textColor = currentTheme.getPrimaryText(),
                    secondaryColor = currentTheme.getSecondaryText(),
                    accentColor = currentTheme.getAccent()
                )

                // Shadow Intensity
                SliderSettingItem(
                    label = "Shadow Intensity",
                    valueText = "${(currentTheme.shadowIntensity * 100).toInt()}%",
                    value = currentTheme.shadowIntensity,
                    range = 0f..1f,
                    onValueChange = { value ->
                        themeRepository.updateCurrentTheme { it.copy(shadowIntensity = value) }
                    },
                    textColor = currentTheme.getPrimaryText(),
                    secondaryColor = currentTheme.getSecondaryText(),
                    accentColor = currentTheme.getAccent()
                )

                // Glass Transparency (Relevant for Liquid Glass & custom)
                if (currentTheme.baseType == BaseThemeType.LIQUID_GLASS || currentTheme.isLiquidGlass || currentTheme.baseType == BaseThemeType.CUSTOM) {
                    SliderSettingItem(
                        label = "Glass Transparency",
                        valueText = "${((1f - currentTheme.glassTransparency) * 100).toInt()}% blur",
                        value = currentTheme.glassTransparency,
                        range = 0.25f..1.0f,
                        onValueChange = { value ->
                            themeRepository.updateCurrentTheme { it.copy(glassTransparency = value) }
                        },
                        textColor = currentTheme.getPrimaryText(),
                        secondaryColor = currentTheme.getSecondaryText(),
                        accentColor = currentTheme.getAccent()
                    )
                }

                // Glow Intensity & Color
                if (currentTheme.baseType == BaseThemeType.GLOW || currentTheme.isGlow || currentTheme.baseType == BaseThemeType.CUSTOM) {
                    SliderSettingItem(
                        label = "Glow Aura Intensity",
                        valueText = "${(currentTheme.glowIntensity * 100).toInt()}%",
                        value = currentTheme.glowIntensity,
                        range = 0f..1f,
                        onValueChange = { value ->
                            themeRepository.updateCurrentTheme { it.copy(glowIntensity = value) }
                        },
                        textColor = currentTheme.getPrimaryText(),
                        secondaryColor = currentTheme.getSecondaryText(),
                        accentColor = currentTheme.getAccent()
                    )

                    SliderSettingItem(
                        label = "Glow Aura Radius",
                        valueText = "${currentTheme.glowRadiusDp.toInt()} dp",
                        value = currentTheme.glowRadiusDp,
                        range = 8f..40f,
                        onValueChange = { value ->
                            themeRepository.updateCurrentTheme { it.copy(glowRadiusDp = value) }
                        },
                        textColor = currentTheme.getPrimaryText(),
                        secondaryColor = currentTheme.getSecondaryText(),
                        accentColor = currentTheme.getAccent()
                    )

                    ColorSettingItem(
                        label = "Glow Aura Color",
                        colorHex = currentTheme.glowColor,
                        textColor = currentTheme.getPrimaryText(),
                        onClick = {
                            openColorPicker("Glow Aura Color", "glow_color", currentTheme.glowColor)
                        }
                    )
                }

                // Glossy reflection toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Glossy Light Reflections",
                            style = MaterialTheme.typography.titleSmall,
                            color = currentTheme.getPrimaryText()
                        )
                        Text(
                            text = "Specular highlight gradient across cards",
                            style = MaterialTheme.typography.bodySmall,
                            color = currentTheme.getSecondaryText()
                        )
                    }

                    Switch(
                        checked = currentTheme.isGlossy,
                        onCheckedChange = { checked ->
                            themeRepository.updateCurrentTheme { it.copy(isGlossy = checked) }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = currentTheme.getButtonText(),
                            checkedTrackColor = currentTheme.getButton()
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section: Animations
            SettingsSectionHeader(
                title = "ANIMATIONS",
                subtitle = "Fine-tune transition physics and motion speed",
                textColor = currentTheme.getPrimaryText(),
                mutedColor = currentTheme.getSecondaryText()
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .themeCardBackground(currentTheme)
                    .padding(18.dp)
            ) {
                SliderSettingItem(
                    label = "Animation Speed Scale",
                    valueText = String.format("%.1fx", currentTheme.animationScale),
                    value = currentTheme.animationScale,
                    range = 0.5f..1.5f,
                    onValueChange = { value ->
                        themeRepository.updateCurrentTheme { it.copy(animationScale = value) }
                    },
                    textColor = currentTheme.getPrimaryText(),
                    secondaryColor = currentTheme.getSecondaryText(),
                    accentColor = currentTheme.getAccent()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section: Custom Themes
            SettingsSectionHeader(
                title = "CUSTOM THEMES",
                subtitle = "Save, duplicate, rename, or restore your designs",
                textColor = currentTheme.getPrimaryText(),
                mutedColor = currentTheme.getSecondaryText()
            )

            // Save Current Button
            Surface(
                onClick = {
                    customThemeNameInput = "${currentTheme.name} Custom"
                    showSaveCustomDialog = true
                },
                shape = RoundedCornerShape(16.dp),
                color = currentTheme.getSurface(),
                border = BorderStroke(1.dp, currentTheme.getBorder()),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("btn_save_current_theme")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(currentTheme.getAccent()),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = currentTheme.getButtonText(),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Save Current as Custom Theme",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = currentTheme.getPrimaryText()
                        )
                        Text(
                            text = "Store all custom colors, radius, and glow values",
                            style = MaterialTheme.typography.bodySmall,
                            color = currentTheme.getSecondaryText()
                        )
                    }
                }
            }

            // List of saved custom themes
            if (savedCustomThemes.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .themeCardBackground(currentTheme)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    savedCustomThemes.forEach { savedTheme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { themeRepository.applyTheme(savedTheme) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(savedTheme.accentColor))
                                        .border(1.dp, currentTheme.getBorder(), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = savedTheme.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = currentTheme.getPrimaryText(),
                                        fontWeight = if (currentTheme.id == savedTheme.id) FontWeight.Bold else FontWeight.Medium
                                    )
                                    Text(
                                        text = "Custom Preset",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = currentTheme.getSecondaryText()
                                    )
                                }
                            }

                            Row {
                                IconButton(
                                    onClick = { themeRepository.duplicateTheme(savedTheme) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Duplicate theme",
                                        tint = currentTheme.getSecondaryText(),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        renameTargetTheme = savedTheme
                                        renameInput = savedTheme.name
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Rename theme",
                                        tint = currentTheme.getSecondaryText(),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { deleteTargetTheme = savedTheme },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete theme",
                                        tint = currentTheme.getSecondaryText(),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section: Data Backup & Restore
            SettingsSectionHeader(
                title = "LOCAL DATA BACKUP & RESTORE",
                subtitle = "Export or import your complete database offline",
                textColor = currentTheme.getPrimaryText(),
                mutedColor = currentTheme.getSecondaryText()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.exportBackup { json ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Notes Backup", json)
                            clipboard.setPrimaryClip(clip)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.FileUpload, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export (Copy)", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = {
                        showImportDialog = true
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.FileDownload, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import Backup", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    // Color Picker Sheet
    activeColorTarget?.let { target ->
        ColorPickerSheet(
            title = activeColorTitle,
            initialColorHex = activeColorInitialHex,
            onColorSelected = { selectedHex ->
                themeRepository.updateCurrentTheme { current ->
                    when (target) {
                        "bg" -> current.copy(backgroundColor = selectedHex, baseType = BaseThemeType.CUSTOM)
                        "surface" -> current.copy(surfaceColor = selectedHex, baseType = BaseThemeType.CUSTOM)
                        "text_primary" -> current.copy(primaryTextColor = selectedHex, baseType = BaseThemeType.CUSTOM)
                        "text_sec" -> current.copy(secondaryTextColor = selectedHex, baseType = BaseThemeType.CUSTOM)
                        "accent" -> current.copy(accentColor = selectedHex, baseType = BaseThemeType.CUSTOM)
                        "btn" -> current.copy(buttonColor = selectedHex, baseType = BaseThemeType.CUSTOM)
                        "border" -> current.copy(borderColor = selectedHex, baseType = BaseThemeType.CUSTOM)
                        "glow_color" -> current.copy(glowColor = selectedHex, baseType = BaseThemeType.CUSTOM)
                        else -> current
                    }
                }
            },
            onDismiss = { activeColorTarget = null }
        )
    }

    // Save Custom Theme Dialog
    if (showSaveCustomDialog) {
        AlertDialog(
            onDismissRequest = { showSaveCustomDialog = false },
            title = { Text("Save Custom Theme") },
            text = {
                Column {
                    Text("Enter a name for this personalized visual theme:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customThemeNameInput,
                        onValueChange = { customThemeNameInput = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        themeRepository.saveAsCustomTheme(customThemeNameInput)
                        showSaveCustomDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveCustomDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Rename Theme Dialog
    renameTargetTheme?.let { targetTheme ->
        AlertDialog(
            onDismissRequest = { renameTargetTheme = null },
            title = { Text("Rename Theme") },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        themeRepository.renameCustomTheme(targetTheme.id, renameInput)
                        renameTargetTheme = null
                    }
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { renameTargetTheme = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Theme Dialog
    deleteTargetTheme?.let { targetTheme ->
        AlertDialog(
            onDismissRequest = { deleteTargetTheme = null },
            title = { Text("Delete Theme") },
            text = { Text("Are you sure you want to delete '${targetTheme.name}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        themeRepository.deleteCustomTheme(targetTheme.id)
                        deleteTargetTheme = null
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTargetTheme = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Import Backup Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Notes Backup") },
            text = {
                Column {
                    Text("Paste your exported JSON notes backup string below:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importJsonInput,
                        onValueChange = { importJsonInput = it },
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importJsonInput.isNotBlank()) {
                            viewModel.importBackup(importJsonInput)
                            importJsonInput = ""
                        }
                        showImportDialog = false
                    }
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    subtitle: String,
    textColor: Color,
    mutedColor: Color
) {
    Column(modifier = Modifier.padding(bottom = 10.dp, start = 4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = mutedColor,
            letterSpacing = 1.3.sp,
            fontSize = 11.sp
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = mutedColor.copy(alpha = 0.8f),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun ColorSettingItem(
    label: String,
    colorHex: Long,
    textColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            fontWeight = FontWeight.Medium
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = colorHex.toHexColorString(),
                style = MaterialTheme.typography.labelMedium,
                color = textColor.copy(alpha = 0.6f),
                fontSize = 12.sp,
                modifier = Modifier.padding(end = 10.dp)
            )

            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(colorHex))
                    .border(1.dp, Color.Black.copy(alpha = 0.2f), CircleShape)
            )
        }
    }
}

@Composable
private fun SliderSettingItem(
    label: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    textColor: Color,
    secondaryColor: Color,
    accentColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelMedium,
                color = secondaryColor,
                fontSize = 12.sp
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor
            )
        )
    }
}
