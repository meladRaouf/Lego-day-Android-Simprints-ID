package com.simprints.face.controllers.core.repository

import com.simprints.face.data.db.person.FaceIdentity
import com.simprints.face.data.db.person.FaceSample
import com.simprints.id.data.db.person.local.FaceIdentityLocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import java.io.Serializable
import com.simprints.id.data.db.person.domain.FaceSample as CoreFaceSample

class FaceDbManagerImpl(private val coreFaceIdentityLocalDataSource: FaceIdentityLocalDataSource) : FaceDbManager {

    override suspend fun loadPeople(query: Serializable): Flow<FaceIdentity> =
        coreFaceIdentityLocalDataSource
            .loadFaceIdentities(query)
            .map {
                FaceIdentity(it.personId, it.faces.map { face -> face.fromCoreToDomain() })
            }
}

fun CoreFaceSample.fromCoreToDomain() = FaceSample(id, template)