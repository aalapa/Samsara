package com.samsara.polymath.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Persona::class, Task::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personaDao(): PersonaDao
    abstract fun taskDao(): TaskDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "polymath_database"
                )
                .addMigrations(MIGRATION_3_4, MIGRATION_4_5)
                .fallbackToDestructiveMigration() // For development - remove in production
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        private val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Add backgroundColor and textColor columns to personas table
                database.execSQL("ALTER TABLE personas ADD COLUMN backgroundColor TEXT NOT NULL DEFAULT '#007AFF'")
                database.execSQL("ALTER TABLE personas ADD COLUMN textColor TEXT NOT NULL DEFAULT '#FFFFFF'")
            }
        }
        
        private val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Add backgroundColor column to tasks table
                database.execSQL("ALTER TABLE tasks ADD COLUMN backgroundColor TEXT NOT NULL DEFAULT '#FFFFFF'")
            }
        }
    }
}

