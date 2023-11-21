package com.simprints.infra.enrolment.records.store.local

import com.simprints.infra.enrolment.records.store.domain.models.FingerprintIdentity
import com.simprints.infra.enrolment.records.store.exceptions.InvalidQueryToLoadRecordsException
import kotlinx.coroutines.flow.Flow
import java.io.Serializable

interface FingerprintIdentityLocalDataSource {

    @Throws(InvalidQueryToLoadRecordsException::class)
    suspend fun loadFingerprintIdentities(query: Serializable): Flow<FingerprintIdentity>
}