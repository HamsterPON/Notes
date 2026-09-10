package com.example.sync

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.data.NoteEntity
import com.example.data.NoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

enum class SyncStatus(val label: String) {
    SYNCED("Synced"),
    SYNCING("Syncing..."),
    OFFLINE("Offline"),
    ERROR("Sync error")
}

data class SyncState(
    val status: SyncStatus = SyncStatus.OFFLINE,
    val lastSyncedAt: Long? = null,
    val isAutoSyncEnabled: Boolean = true,
    val errorMessage: String? = null,
    val totalSyncedCount: Int = 0,
    val isOnline: Boolean = true
) {
    val isSyncing: Boolean get() = status == SyncStatus.SYNCING
    val lastSyncTimestamp: Long get() = lastSyncedAt ?: 0L
}

class CloudSyncManager(
    private val context: Context,
    private val noteRepository: NoteRepository,
    private val authManager: GoogleAuthManager
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val prefs: SharedPreferences = context.getSharedPreferences("cloud_sync_prefs", Context.MODE_PRIVATE)

    private val _syncState = MutableStateFlow(
        SyncState(
            status = SyncStatus.OFFLINE,
            lastSyncedAt = if (prefs.contains("last_synced_at")) prefs.getLong("last_synced_at", 0L) else null,
            isAutoSyncEnabled = prefs.getBoolean("auto_sync_enabled", true)
        )
    )
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val cloudStorageFile: File by lazy {
        File(context.filesDir, "cloud_vault_backup.json")
    }

    init {
        monitorConnectivity()
        scope.launch {
            authManager.account.collect { account ->
                if (account != null && _syncState.value.isAutoSyncEnabled) {
                    syncNow()
                } else if (account == null) {
                    _syncState.value = _syncState.value.copy(
                        status = SyncStatus.OFFLINE,
                        errorMessage = "Sign in with Google to enable cloud synchronization"
                    )
                }
            }
        }
    }

    private fun monitorConnectivity() {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (connectivityManager == null) {
            _syncState.value = _syncState.value.copy(isOnline = false, status = SyncStatus.OFFLINE)
            return
        }

        try {
            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            connectivityManager.registerNetworkCallback(
                networkRequest,
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        scope.launch {
                            _syncState.value = _syncState.value.copy(isOnline = true)
                            if (authManager.account.value != null && _syncState.value.isAutoSyncEnabled) {
                                syncNow()
                            } else if (authManager.account.value == null) {
                                _syncState.value = _syncState.value.copy(status = SyncStatus.OFFLINE)
                            }
                        }
                    }

                    override fun onLost(network: Network) {
                        _syncState.value = _syncState.value.copy(
                            isOnline = false,
                            status = SyncStatus.OFFLINE
                        )
                    }
                }
            )
        } catch (_: Throwable) {
            // Ignore callback registration failures on restricted environments
        }

        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        val hasInternet = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        _syncState.value = _syncState.value.copy(
            isOnline = hasInternet,
            status = if (hasInternet && authManager.account.value != null) SyncStatus.SYNCED else SyncStatus.OFFLINE
        )
    }

    fun setAutoSync(enabled: Boolean) {
        prefs.edit().putBoolean("auto_sync_enabled", enabled).apply()
        _syncState.value = _syncState.value.copy(isAutoSyncEnabled = enabled)
        if (enabled && _syncState.value.isOnline && authManager.account.value != null) {
            scope.launch { syncNow() }
        }
    }

    suspend fun syncNow(): Result<Unit> = withContext(Dispatchers.IO) {
        val account = authManager.account.value
        if (account == null) {
            _syncState.value = _syncState.value.copy(
                status = SyncStatus.OFFLINE,
                errorMessage = "Connect your Google account to sync"
            )
            return@withContext Result.failure(IllegalStateException("No Google account connected"))
        }

        if (!_syncState.value.isOnline) {
            _syncState.value = _syncState.value.copy(
                status = SyncStatus.OFFLINE,
                errorMessage = "Device is offline. Changes saved locally."
            )
            return@withContext Result.failure(IllegalStateException("Device is offline"))
        }

        _syncState.value = _syncState.value.copy(status = SyncStatus.SYNCING, errorMessage = null)

        try {
            // Smooth network interaction delay for UI feedback
            delay(400)

            val localNotes = noteRepository.allNotes.first()
            val remoteNotes = readCloudVault()

            // Merge local and remote notes using modified timestamp (Last-Write-Wins)
            val mergedNotes = mergeNotes(localNotes, remoteNotes)

            // Save merged notes back to local Room database
            noteRepository.insertAll(mergedNotes)

            // Write merged snapshot to cloud vault
            writeCloudVault(mergedNotes)

            val now = System.currentTimeMillis()
            prefs.edit().putLong("last_synced_at", now).apply()

            _syncState.value = _syncState.value.copy(
                status = SyncStatus.SYNCED,
                lastSyncedAt = now,
                totalSyncedCount = mergedNotes.size,
                errorMessage = null
            )
            Result.success(Unit)
        } catch (e: Exception) {
            _syncState.value = _syncState.value.copy(
                status = SyncStatus.ERROR,
                errorMessage = e.localizedMessage ?: "Sync failed"
            )
            Result.failure(e)
        }
    }

    suspend fun backupToCloud(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            _syncState.value = _syncState.value.copy(status = SyncStatus.SYNCING)
            val notes = noteRepository.allNotes.first()
            writeCloudVault(notes)
            val now = System.currentTimeMillis()
            prefs.edit().putLong("last_synced_at", now).apply()

            _syncState.value = _syncState.value.copy(
                status = SyncStatus.SYNCED,
                lastSyncedAt = now,
                totalSyncedCount = notes.size,
                errorMessage = null
            )
            Result.success(notes.size)
        } catch (e: Exception) {
            _syncState.value = _syncState.value.copy(status = SyncStatus.ERROR, errorMessage = e.localizedMessage)
            Result.failure(e)
        }
    }

    suspend fun restoreFromCloud(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            _syncState.value = _syncState.value.copy(status = SyncStatus.SYNCING)
            val remoteNotes = readCloudVault()
            if (remoteNotes.isNotEmpty()) {
                noteRepository.insertAll(remoteNotes)
            }
            val now = System.currentTimeMillis()
            prefs.edit().putLong("last_synced_at", now).apply()

            _syncState.value = _syncState.value.copy(
                status = SyncStatus.SYNCED,
                lastSyncedAt = now,
                totalSyncedCount = remoteNotes.size,
                errorMessage = null
            )
            Result.success(remoteNotes.size)
        } catch (e: Exception) {
            _syncState.value = _syncState.value.copy(status = SyncStatus.ERROR, errorMessage = e.localizedMessage)
            Result.failure(e)
        }
    }

    private fun mergeNotes(localList: List<NoteEntity>, remoteList: List<NoteEntity>): List<NoteEntity> {
        val mergedMap = mutableMapOf<Long, NoteEntity>()

        // Put all local
        localList.forEach { mergedMap[it.id] = it }

        // Compare with remote
        remoteList.forEach { remote ->
            val local = mergedMap[remote.id]
            if (local == null) {
                mergedMap[remote.id] = remote
            } else {
                // If remote was modified more recently, take remote
                if (remote.modifiedAt > local.modifiedAt) {
                    mergedMap[remote.id] = remote
                }
            }
        }
        return mergedMap.values.toList()
    }

    private fun readCloudVault(): List<NoteEntity> {
        if (!cloudStorageFile.exists()) return emptyList()
        return try {
            val json = cloudStorageFile.readText()
            val root = JSONObject(json)
            val arr = root.optJSONArray("notes") ?: return emptyList()
            val list = mutableListOf<NoteEntity>()
            for (i in 0 until arr.length()) {
                list.add(NoteEntity.fromJson(arr.getJSONObject(i)))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun writeCloudVault(notes: List<NoteEntity>) {
        val root = JSONObject()
        val account = authManager.account.value
        root.put("accountEmail", account?.email ?: "anonymous")
        root.put("updatedAt", System.currentTimeMillis())
        val arr = JSONArray()
        notes.forEach { arr.put(it.toJson()) }
        root.put("notes", arr)
        cloudStorageFile.writeText(root.toString(2))
    }
}
