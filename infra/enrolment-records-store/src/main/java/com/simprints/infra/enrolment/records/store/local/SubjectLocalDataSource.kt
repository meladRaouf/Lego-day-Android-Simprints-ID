package com.simprints.infra.enrolment.records.store.local

import com.simprints.infra.enrolment.records.store.domain.models.Subject
import com.simprints.infra.enrolment.records.store.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery
import kotlinx.coroutines.flow.Flow

interface SubjectLocalDataSource : FaceIdentityLocalDataSource, FingerprintIdentityLocalDataSource {

    suspend fun load(query: SubjectQuery): Flow<Subject>
    suspend fun delete(queries: List<SubjectQuery>)
    suspend fun deleteAll()
    suspend fun count(query: SubjectQuery = SubjectQuery()): Int

    suspend fun performActions(actions: List<SubjectAction>)
}
