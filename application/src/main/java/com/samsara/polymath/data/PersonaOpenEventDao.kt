package com.samsara.polymath.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

data class DailyOpenCount(
    val dayMillis: Long,
    val openCount: Long
)

@Dao
interface PersonaOpenEventDao {

    @Insert
    suspend fun insert(event: PersonaOpenEvent): Long

    @Query("""
        SELECT (timestamp / 86400000) * 86400000 AS dayMillis,
               COUNT(*) AS openCount
        FROM persona_open_events
        WHERE personaId = :personaId AND timestamp >= :sinceMillis
        GROUP BY timestamp / 86400000
        ORDER BY dayMillis ASC
    """)
    suspend fun getDailyOpenCountsByPersona(personaId: Long, sinceMillis: Long): List<DailyOpenCount>

    @Query("""
        SELECT (timestamp / 86400000) * 86400000 AS dayMillis,
               COUNT(*) AS openCount
        FROM persona_open_events
        WHERE timestamp >= :sinceMillis
        GROUP BY timestamp / 86400000
        ORDER BY dayMillis ASC
    """)
    suspend fun getDailyOpenCountsGlobal(sinceMillis: Long): List<DailyOpenCount>

    @Query("SELECT * FROM persona_open_events WHERE timestamp >= :sinceMillis ORDER BY timestamp ASC")
    suspend fun getAllEventsSince(sinceMillis: Long): List<PersonaOpenEvent>

    @Query("DELETE FROM persona_open_events WHERE timestamp < :beforeMillis")
    suspend fun deleteOldEvents(beforeMillis: Long)
}
