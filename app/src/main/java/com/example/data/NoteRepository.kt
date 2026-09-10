package com.example.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

class NoteRepository(
    private val noteDao: NoteDao,
    private val context: Context
) {
    val activeNotes: Flow<List<NoteEntity>> = noteDao.getActiveNotes()
    val archivedNotes: Flow<List<NoteEntity>> = noteDao.getArchivedNotes()
    val deletedNotes: Flow<List<NoteEntity>> = noteDao.getDeletedNotes()
    val favoriteNotes: Flow<List<NoteEntity>> = noteDao.getFavoriteNotes()
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()

    fun getNote(id: Long): Flow<NoteEntity?> = noteDao.getNoteById(id)

    suspend fun getNoteOnce(id: Long): NoteEntity? = noteDao.getNoteByIdOnce(id)

    suspend fun insert(note: NoteEntity): Long = noteDao.insertNote(note)

    suspend fun insertAll(notes: List<NoteEntity>) = noteDao.insertAll(notes)

    suspend fun update(note: NoteEntity) = noteDao.updateNote(note)

    suspend fun moveToTrash(note: NoteEntity) {
        val trashed = note.copy(
            isDeleted = true,
            deletedAt = System.currentTimeMillis(),
            isPinned = false
        )
        noteDao.updateNote(trashed)
    }

    suspend fun restoreFromTrash(id: Long) {
        noteDao.restoreNote(id)
    }

    suspend fun permanentlyDelete(note: NoteEntity) {
        noteDao.deleteNote(note)
    }

    suspend fun permanentlyDeleteById(id: Long) {
        noteDao.deleteNoteById(id)
    }

    suspend fun emptyTrash() {
        noteDao.emptyTrash()
    }

    suspend fun toggleFavorite(note: NoteEntity) {
        val updated = note.copy(
            isFavorite = !note.isFavorite,
            modifiedAt = System.currentTimeMillis()
        )
        noteDao.updateNote(updated)
    }

    suspend fun toggleArchive(note: NoteEntity) {
        val updated = note.copy(
            isArchived = !note.isArchived,
            isPinned = false,
            modifiedAt = System.currentTimeMillis()
        )
        noteDao.updateNote(updated)
    }

    suspend fun duplicateNote(note: NoteEntity): Long {
        val copy = note.copy(
            id = 0,
            title = if (note.title.isNotBlank()) "${note.title} (Copy)" else "Untitled (Copy)",
            isPinned = false,
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis()
        )
        return noteDao.insertNote(copy)
    }

    suspend fun exportNotesAsJson(): String = withContext(Dispatchers.IO) {
        val notes = noteDao.getAllNotes().first()
        val root = JSONObject()
        root.put("version", 2)
        root.put("exportedAt", System.currentTimeMillis())
        val notesArray = JSONArray()
        notes.forEach { notesArray.put(it.toJson()) }
        root.put("notes", notesArray)
        root.toString(2)
    }

    suspend fun importNotesFromJson(jsonString: String): Int = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            val notesArray = root.optJSONArray("notes") ?: return@withContext 0
            val importedList = mutableListOf<NoteEntity>()
            for (i in 0 until notesArray.length()) {
                val obj = notesArray.getJSONObject(i)
                val note = NoteEntity.fromJson(obj).copy(id = 0)
                importedList.add(note)
            }
            if (importedList.isNotEmpty()) {
                noteDao.insertAll(importedList)
            }
            importedList.size
        } catch (e: Exception) {
            0
        }
    }

    suspend fun calculateStorageUsage(): Pair<Long, Int> = withContext(Dispatchers.IO) {
        val imagesDir = File(context.filesDir, "images")
        var totalBytes = 0L
        var imageCount = 0
        if (imagesDir.exists()) {
            imagesDir.listFiles()?.forEach { file ->
                if (file.isFile) {
                    totalBytes += file.length()
                    imageCount++
                }
            }
        }
        val dbFile = context.getDatabasePath("notes_database")
        if (dbFile.exists()) {
            totalBytes += dbFile.length()
        }
        Pair(totalBytes, imageCount)
    }

    suspend fun clearImageCache() = withContext(Dispatchers.IO) {
        val cacheDir = context.cacheDir
        cacheDir.deleteRecursively()
        cacheDir.mkdirs()
    }

    suspend fun saveImageLocally(uri: Uri): String = withContext(Dispatchers.IO) {
        val imagesDir = File(context.filesDir, "images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
        val fileName = "img_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
        val destinationFile = File(imagesDir, fileName)

        context.contentResolver.openInputStream(uri)?.use { inputStream: InputStream ->
            FileOutputStream(destinationFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        destinationFile.absolutePath
    }

    fun createCameraTempFile(): Pair<Uri, File> {
        val cacheDir = File(context.cacheDir, "camera")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        val tempFile = File(cacheDir, "camera_${System.currentTimeMillis()}.jpg")
        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, tempFile)
        return Pair(uri, tempFile)
    }

    suspend fun persistCameraImage(tempFile: File): String = withContext(Dispatchers.IO) {
        val imagesDir = File(context.filesDir, "images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
        val permanentFile = File(imagesDir, "photo_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg")
        if (tempFile.exists()) {
            tempFile.copyTo(permanentFile, overwrite = true)
            tempFile.delete()
        }
        permanentFile.absolutePath
    }
}
