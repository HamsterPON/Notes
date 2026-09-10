package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class ChecklistItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val isCompleted: Boolean = false
) {
    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("text", text)
        obj.put("isCompleted", isCompleted)
        return obj
    }

    companion object {
        fun fromJson(obj: JSONObject): ChecklistItem {
            return ChecklistItem(
                id = obj.optString("id", UUID.randomUUID().toString()),
                text = obj.optString("text", ""),
                isCompleted = obj.optBoolean("isCompleted", false)
            )
        }
    }
}

@Entity(tableName = "notes")
@TypeConverters(Converters::class)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val imagePaths: List<String> = emptyList(),
    val checklistItems: List<ChecklistItem> = emptyList(),
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val folder: String = "Notes",
    val tags: List<String> = emptyList(),
    val colorHex: Long? = null,
    val reminderTime: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("title", title)
        obj.put("content", content)
        val imagesArr = JSONArray()
        imagePaths.forEach { imagesArr.put(it) }
        obj.put("imagePaths", imagesArr)

        val checklistArr = JSONArray()
        checklistItems.forEach { checklistArr.put(it.toJson()) }
        obj.put("checklistItems", checklistArr)

        obj.put("isPinned", isPinned)
        obj.put("isFavorite", isFavorite)
        obj.put("isArchived", isArchived)
        obj.put("isDeleted", isDeleted)
        obj.put("deletedAt", deletedAt ?: -1L)
        obj.put("folder", folder)

        val tagsArr = JSONArray()
        tags.forEach { tagsArr.put(it) }
        obj.put("tags", tagsArr)

        obj.put("colorHex", colorHex ?: -1L)
        obj.put("reminderTime", reminderTime ?: -1L)
        obj.put("createdAt", createdAt)
        obj.put("modifiedAt", modifiedAt)
        return obj
    }

    companion object {
        fun fromJson(obj: JSONObject): NoteEntity {
            val imgList = mutableListOf<String>()
            val imgArr = obj.optJSONArray("imagePaths")
            if (imgArr != null) {
                for (i in 0 until imgArr.length()) {
                    imgList.add(imgArr.getString(i))
                }
            }

            val clList = mutableListOf<ChecklistItem>()
            val clArr = obj.optJSONArray("checklistItems")
            if (clArr != null) {
                for (i in 0 until clArr.length()) {
                    clList.add(ChecklistItem.fromJson(clArr.getJSONObject(i)))
                }
            }

            val tagList = mutableListOf<String>()
            val tagArr = obj.optJSONArray("tags")
            if (tagArr != null) {
                for (i in 0 until tagArr.length()) {
                    tagList.add(tagArr.getString(i))
                }
            }

            val delAt = obj.optLong("deletedAt", -1L)
            val color = obj.optLong("colorHex", -1L)
            val reminder = obj.optLong("reminderTime", -1L)

            return NoteEntity(
                id = obj.optLong("id", 0L),
                title = obj.optString("title", ""),
                content = obj.optString("content", ""),
                imagePaths = imgList,
                checklistItems = clList,
                isPinned = obj.optBoolean("isPinned", false),
                isFavorite = obj.optBoolean("isFavorite", false),
                isArchived = obj.optBoolean("isArchived", false),
                isDeleted = obj.optBoolean("isDeleted", false),
                deletedAt = if (delAt > 0) delAt else null,
                folder = obj.optString("folder", "Notes"),
                tags = tagList,
                colorHex = if (color > 0) color else null,
                reminderTime = if (reminder > 0) reminder else null,
                createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                modifiedAt = obj.optLong("modifiedAt", System.currentTimeMillis())
            )
        }
    }
}

class Converters {
    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        if (list.isNullOrEmpty()) return "[]"
        val array = JSONArray()
        list.forEach { array.put(it) }
        return array.toString()
    }

    @TypeConverter
    fun toStringList(data: String?): List<String> {
        if (data.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(data)
            val list = mutableListOf<String>()
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromChecklist(list: List<ChecklistItem>?): String {
        if (list.isNullOrEmpty()) return "[]"
        val array = JSONArray()
        list.forEach { item ->
            array.put(item.toJson())
        }
        return array.toString()
    }

    @TypeConverter
    fun toChecklist(data: String?): List<ChecklistItem> {
        if (data.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(data)
            val list = mutableListOf<ChecklistItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(ChecklistItem.fromJson(obj))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }
}
