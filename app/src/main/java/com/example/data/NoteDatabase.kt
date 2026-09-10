package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [NoteEntity::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        fun getDatabase(context: Context): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val db = buildDatabase(context)
                try {
                    // Verify database open eagerly so any corruption or integrity mismatch is caught and healed
                    db.openHelper.writableDatabase
                } catch (_: Throwable) {
                    try {
                        db.close()
                    } catch (_: Throwable) {}
                    context.deleteDatabase("notes_database")
                    val freshDb = buildDatabase(context)
                    try {
                        freshDb.openHelper.writableDatabase
                    } catch (_: Throwable) {}
                    INSTANCE = freshDb
                    return@synchronized freshDb
                }
                INSTANCE = db
                db
            }
        }

        private fun buildDatabase(context: Context): NoteDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                NoteDatabase::class.java,
                "notes_database"
            )
            .fallbackToDestructiveMigration(true)
            .fallbackToDestructiveMigrationOnDowngrade(true)
            .build()
        }
    }
}
