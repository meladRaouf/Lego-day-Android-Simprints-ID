package com.simprints.infra.events.event.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.local.models.DbEvent
import kotlinx.coroutines.flow.Flow

@Dao
internal interface EventRoomDao {

    @Query("select * from DbEvent order by createdAt desc")
    suspend fun loadAll(): List<DbEvent>

    @Query("select * from DbEvent where sessionId = :sessionId order by createdAt desc")
    suspend fun loadFromSession(sessionId: String): List<DbEvent>

    @Query("select eventJson from DbEvent where sessionId = :sessionId order by createdAt desc")
    suspend fun loadEventJsonFromSession(sessionId: String): List<String>

    @Query("select * from DbEvent where projectId = :projectId order by createdAt desc")
    suspend fun loadFromProject(projectId: String): List<DbEvent>

    @Query("select * from DbEvent where sessionIsClosed = 0 and type = :type order by createdAt desc")
    suspend fun loadOpenedSessions(
        type: EventType = EventType.SESSION_CAPTURE
    ): List<DbEvent>

    @Query("select sessionId from DbEvent where projectId = :projectId and sessionIsClosed = :isClosed and type = :type")
    suspend fun loadAllClosedSessionIds(
        projectId: String,
        isClosed: Boolean = true,
        type: EventType = EventType.SESSION_CAPTURE
    ): List<String>

    @Query("select count(*) from DbEvent where projectId = :projectId")
    suspend fun countFromProject(projectId: String): Int

    @Query("select count(*) from DbEvent where type = :type AND projectId = :projectId")
    suspend fun countFromProjectByType(type: EventType, projectId: String): Int

    @Query("select count(*) from DbEvent where type = :type")
    suspend fun countFromType(type: EventType): Int

    @Query("select count(*) from DbEvent where projectId = :projectId and type = :type")
    fun observeCountFromType(projectId: String, type: EventType): Flow<Int>

    @Query("delete from DbEvent where id in (:ids)")
    suspend fun delete(ids: List<String>)

    @Query("delete from DbEvent where sessionId = :sessionId")
    suspend fun deleteAllFromSession(sessionId: String)

    @Query("delete from DbEvent")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(dbEvent: DbEvent)

}