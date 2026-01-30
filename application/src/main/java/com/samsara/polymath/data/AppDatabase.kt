package com.samsara.polymath.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Persona::class, Task::class, Comment::class, PersonaStatistics::class, Tag::class, PersonaTag::class], version = 13, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personaDao(): PersonaDao
    abstract fun taskDao(): TaskDao
    abstract fun commentDao(): CommentDao
    abstract fun personaStatisticsDao(): PersonaStatisticsDao
    abstract fun tagDao(): TagDao
    abstract fun personaTagDao(): PersonaTagDao
    
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
                .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13)
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

        private val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Create comments table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS comments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        taskId INTEGER NOT NULL,
                        text TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(taskId) REFERENCES tasks(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS index_comments_taskId ON comments(taskId)")
            }
        }

        private val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Add rank tracking columns to tasks table
                database.execSQL("ALTER TABLE tasks ADD COLUMN previousOrder INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE tasks ADD COLUMN rankStatus TEXT NOT NULL DEFAULT 'STABLE'")
            }
        }

        private val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Add rank tracking columns to personas table
                database.execSQL("ALTER TABLE personas ADD COLUMN previousOpenCount INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE personas ADD COLUMN rankStatus TEXT NOT NULL DEFAULT 'STABLE'")
            }
        }

        private val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Add lastOpenedAt column for decay tracking (default to current time)
                database.execSQL("ALTER TABLE personas ADD COLUMN lastOpenedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
            }
        }

        private val MIGRATION_9_10 = object : androidx.room.migration.Migration(9, 10) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Create persona_statistics table for historical tracking
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS persona_statistics (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        personaId INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL,
                        openCount INTEGER NOT NULL,
                        totalTasks INTEGER NOT NULL,
                        completedTasks INTEGER NOT NULL,
                        score REAL NOT NULL,
                        FOREIGN KEY(personaId) REFERENCES personas(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS index_persona_statistics_personaId ON persona_statistics(personaId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_persona_statistics_timestamp ON persona_statistics(timestamp)")
            }
        }

        private val MIGRATION_10_11 = object : androidx.room.migration.Migration(10, 11) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Add isRecurring column to tasks table
                database.execSQL("ALTER TABLE tasks ADD COLUMN isRecurring INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_12_13 = object : androidx.room.migration.Migration(12, 13) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE tasks ADD COLUMN recurringFrequency TEXT DEFAULT NULL")
                database.execSQL("ALTER TABLE tasks ADD COLUMN recurringDays TEXT DEFAULT NULL")
            }
        }

        private val MIGRATION_11_12 = object : androidx.room.migration.Migration(11, 12) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Create tags table
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS tags (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        color TEXT,
                        created_at INTEGER NOT NULL,
                        `order` INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                
                // Create junction table
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS persona_tags (
                        personaId INTEGER NOT NULL,
                        tagId INTEGER NOT NULL,
                        assigned_at INTEGER NOT NULL,
                        PRIMARY KEY(personaId, tagId),
                        FOREIGN KEY(personaId) REFERENCES personas(id) ON DELETE CASCADE,
                        FOREIGN KEY(tagId) REFERENCES tags(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                
                // Create indices for better query performance
                database.execSQL("CREATE INDEX IF NOT EXISTS index_persona_tags_personaId ON persona_tags(personaId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_persona_tags_tagId ON persona_tags(tagId)")
                
                // Pre-populate with default tags
                val currentTime = System.currentTimeMillis()
                val defaultTags = listOf(
                    Triple("Physical", "#007AFF", 1),
                    Triple("Martial Arts", "#FF2D55", 2),
                    Triple("Financial", "#FFD93D", 3),
                    Triple("Career", "#F38181", 4),
                    Triple("Technical", "#32D74B", 5),
                    Triple("Programming", "#00C7BE", 6),
                    Triple("Creative", "#FF6B6B", 7),
                    Triple("Music", "#FFCC00", 8),
                    Triple("Family", "#AF52DE", 9),
                    Triple("Relationships", "#5AC8FA", 10),
                    Triple("Spiritual", "#5856D6", 11),
                    Triple("Language", "#34C759", 12),
                    Triple("Lifestyle", "#FF9500", 13),
                    Triple("Travel", "#4ECDC4", 14),
                    Triple("Aspirational", "#AA96DA", 15)
                )
                
                defaultTags.forEach { (name, color, order) ->
                    database.execSQL(
                        "INSERT INTO tags (name, color, created_at, `order`) VALUES (?, ?, ?, ?)",
                        arrayOf(name, color, currentTime, order)
                    )
                }
            }
        }
    }
}

