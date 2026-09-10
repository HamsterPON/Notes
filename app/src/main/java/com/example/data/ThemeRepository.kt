package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.ui.theme.BaseThemeType
import com.example.ui.theme.ThemeConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import java.util.UUID

enum class NoteSortOption(val displayName: String) {
    MODIFIED_DESC("Date Modified (Newest)"),
    MODIFIED_ASC("Date Modified (Oldest)"),
    CREATED_DESC("Date Created (Newest)"),
    TITLE_ASC("Title (A-Z)")
}

enum class NoteViewMode {
    GRID, LIST
}

class ThemeRepository(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("notes_app_theme_prefs", Context.MODE_PRIVATE)

    private val _currentTheme = MutableStateFlow(loadInitialTheme())
    val currentTheme: StateFlow<ThemeConfig> = _currentTheme.asStateFlow()

    private val _savedCustomThemes = MutableStateFlow(loadCustomThemes())
    val savedCustomThemes: StateFlow<List<ThemeConfig>> = _savedCustomThemes.asStateFlow()

    // General user preferences
    private val _sortOption = MutableStateFlow(
        try {
            NoteSortOption.valueOf(prefs.getString("note_sort_option", NoteSortOption.MODIFIED_DESC.name) ?: NoteSortOption.MODIFIED_DESC.name)
        } catch (e: Exception) {
            NoteSortOption.MODIFIED_DESC
        }
    )
    val sortOption: StateFlow<NoteSortOption> = _sortOption.asStateFlow()

    private val _viewMode = MutableStateFlow(
        try {
            NoteViewMode.valueOf(prefs.getString("note_view_mode", NoteViewMode.GRID.name) ?: NoteViewMode.GRID.name)
        } catch (e: Exception) {
            NoteViewMode.GRID
        }
    )
    val viewMode: StateFlow<NoteViewMode> = _viewMode.asStateFlow()

    private val _autoSaveEnabled = MutableStateFlow(prefs.getBoolean("pref_auto_save", true))
    val autoSaveEnabled: StateFlow<Boolean> = _autoSaveEnabled.asStateFlow()

    private val _confirmDeleteEnabled = MutableStateFlow(prefs.getBoolean("pref_confirm_delete", false))
    val confirmDeleteEnabled: StateFlow<Boolean> = _confirmDeleteEnabled.asStateFlow()

    private val _remindersEnabled = MutableStateFlow(prefs.getBoolean("pref_reminders_enabled", true))
    val remindersEnabled: StateFlow<Boolean> = _remindersEnabled.asStateFlow()

    private fun loadInitialTheme(): ThemeConfig {
        val json = prefs.getString("current_theme_config", null)
        if (!json.isNullOrBlank()) {
            val parsed = ThemeConfig.fromJson(json)
            if (parsed != null) return parsed
        }
        return ThemeConfig.WhitePreset
    }

    private fun loadCustomThemes(): List<ThemeConfig> {
        val json = prefs.getString("saved_custom_themes", null)
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<ThemeConfig>()
            for (i in 0 until array.length()) {
                val item = ThemeConfig.fromJson(array.getString(i))
                if (item != null) list.add(item)
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun persistCustomThemes(themes: List<ThemeConfig>) {
        val array = JSONArray()
        themes.forEach { array.put(it.toJson()) }
        prefs.edit().putString("saved_custom_themes", array.toString()).apply()
        _savedCustomThemes.value = themes
    }

    fun applyTheme(config: ThemeConfig) {
        _currentTheme.value = config
        prefs.edit().putString("current_theme_config", config.toJson()).apply()
    }

    fun selectPreset(type: BaseThemeType) {
        val preset = ThemeConfig.getDefaultPreset(type)
        applyTheme(preset)
    }

    fun updateCurrentTheme(modifier: (ThemeConfig) -> ThemeConfig) {
        val updated = modifier(_currentTheme.value)
        applyTheme(updated)
    }

    fun resetToPreset(type: BaseThemeType) {
        val defaultPreset = ThemeConfig.getDefaultPreset(type)
        applyTheme(defaultPreset)
    }

    fun saveAsCustomTheme(name: String, config: ThemeConfig = _currentTheme.value): ThemeConfig {
        val customTheme = config.copy(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "Custom Theme" },
            baseType = BaseThemeType.CUSTOM
        )
        val currentList = _savedCustomThemes.value.toMutableList()
        currentList.add(0, customTheme)
        persistCustomThemes(currentList)
        applyTheme(customTheme)
        return customTheme
    }

    fun duplicateTheme(config: ThemeConfig): ThemeConfig {
        return saveAsCustomTheme("${config.name} Copy", config)
    }

    fun renameCustomTheme(id: String, newName: String) {
        val updatedList = _savedCustomThemes.value.map {
            if (it.id == id) it.copy(name = newName) else it
        }
        persistCustomThemes(updatedList)
        if (_currentTheme.value.id == id) {
            val updatedCurrent = _currentTheme.value.copy(name = newName)
            applyTheme(updatedCurrent)
        }
    }

    fun deleteCustomTheme(id: String) {
        val updatedList = _savedCustomThemes.value.filter { it.id != id }
        persistCustomThemes(updatedList)
        if (_currentTheme.value.id == id) {
            applyTheme(ThemeConfig.WhitePreset)
        }
    }

    fun setSortOption(option: NoteSortOption) {
        prefs.edit().putString("note_sort_option", option.name).apply()
        _sortOption.value = option
    }

    fun setViewMode(mode: NoteViewMode) {
        prefs.edit().putString("note_view_mode", mode.name).apply()
        _viewMode.value = mode
    }

    fun setAutoSave(enabled: Boolean) {
        prefs.edit().putBoolean("pref_auto_save", enabled).apply()
        _autoSaveEnabled.value = enabled
    }

    fun setConfirmDelete(enabled: Boolean) {
        prefs.edit().putBoolean("pref_confirm_delete", enabled).apply()
        _confirmDeleteEnabled.value = enabled
    }

    fun setRemindersEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("pref_reminders_enabled", enabled).apply()
        _remindersEnabled.value = enabled
    }
}
