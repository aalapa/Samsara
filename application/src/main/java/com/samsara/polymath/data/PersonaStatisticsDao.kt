package com.samsara.polymath.data

import androidx.room.*

@Dao
interface PersonaStatisticsDao {
    @Query("SELECT * FROM persona_statistics WHERE personaId = :personaId AND timestamp >= :startTime ORDER BY timestamp ASC")
    suspend fun getStatisticsForPersona(personaId: Long, startTime: Long): List<PersonaStatistics>
    
    @Query("SELECT * FROM persona_statistics WHERE timestamp >= :startTime ORDER BY timestamp ASC")
    suspend fun getStatisticsSince(startTime: Long): List<PersonaStatistics>
    
    @Insert
    suspend fun insertStatistics(statistics: PersonaStatistics): Long
    
    @Query("DELETE FROM persona_statistics WHERE timestamp < :timestamp")
    suspend fun deleteOldStatistics(timestamp: Long)
}

