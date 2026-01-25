package com.samsara.polymath.repository

import com.samsara.polymath.data.PersonaStatistics
import com.samsara.polymath.data.PersonaStatisticsDao

class PersonaStatisticsRepository(private val dao: PersonaStatisticsDao) {
    
    suspend fun getStatisticsForPersona(personaId: Long, startTime: Long): List<PersonaStatistics> {
        return dao.getStatisticsForPersona(personaId, startTime)
    }
    
    suspend fun getStatisticsSince(startTime: Long): List<PersonaStatistics> {
        return dao.getStatisticsSince(startTime)
    }
    
    suspend fun insertStatistics(statistics: PersonaStatistics): Long {
        return dao.insertStatistics(statistics)
    }
    
    suspend fun deleteOldStatistics(timestamp: Long) {
        dao.deleteOldStatistics(timestamp)
    }
}

