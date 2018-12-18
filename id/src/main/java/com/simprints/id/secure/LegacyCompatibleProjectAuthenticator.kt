package com.simprints.id.secure

import com.google.android.gms.safetynet.SafetyNetClient
import com.simprints.id.di.AppComponent
import com.simprints.id.exceptions.safe.data.db.SimprintsInternalServerException
import com.simprints.id.exceptions.safe.secure.AuthRequestInvalidCredentialsException
import com.simprints.id.exceptions.safe.secure.DifferentProjectIdReceivedFromIntentException
import com.simprints.id.exceptions.safe.secure.InvalidLegacyProjectIdReceivedFromIntentException
import com.simprints.id.secure.cryptography.Hasher
import com.simprints.id.secure.models.LegacyProject
import com.simprints.id.secure.models.NonceScope
import io.reactivex.Completable
import io.reactivex.Single
import java.io.IOException

class LegacyCompatibleProjectAuthenticator(component: AppComponent,
                                           safetyNetClient: SafetyNetClient,
                                           secureApiClient: SecureApiInterface,
                                           attestationManager: AttestationManager = AttestationManager()
) : ProjectAuthenticator(component, safetyNetClient, secureApiClient, attestationManager) {

    private val legacyProjectIdManager = LegacyProjectIdManager(secureApiClient)

    /**
     * @throws IOException
     * @throws DifferentProjectIdReceivedFromIntentException
     * @throws InvalidLegacyProjectIdReceivedFromIntentException
     * @throws AuthRequestInvalidCredentialsException
     * @throws SimprintsInternalServerException
     */
    fun authenticate(nonceScope: NonceScope, projectSecret: String, intentProjectId: String?, intentLegacyProjectId: String?): Completable =
        when {
            intentLegacyProjectId != null -> checkLegacyProjectIdMatchesProjectId(nonceScope.projectId, intentLegacyProjectId)
            intentProjectId != null -> checkIntentProjectIdMatchesProjectId(nonceScope.projectId, intentProjectId)
            else -> Completable.complete()
        }
        .andThen(createLocalDbKeyForProject(nonceScope.projectId, intentLegacyProjectId))
        .andThen(authenticate(nonceScope, projectSecret))

    private fun createLocalDbKeyForProject(projectId: String, legacyProjectId: String?): Completable {
        secureDataManager.setLocalDatabaseKey(projectId, legacyProjectId)
        return Completable.complete()
    }

    private fun checkIntentProjectIdMatchesProjectId(expectedProjectId: String, intentProjectId: String): Completable =
        Completable.create {
            if (expectedProjectId == intentProjectId)
                it.onComplete()
            else it.onError(DifferentProjectIdReceivedFromIntentException.withProjectIds(expectedProjectId, intentProjectId))
        }

    private fun checkLegacyProjectIdMatchesProjectId(expectedProjectId: String, legacyProjectId: String): Completable =
        Hasher().hash(legacyProjectId).let {
            legacyProjectIdManager.requestLegacyProject(it)
                .checkReceivedProjectIdIsAsExpected(expectedProjectId)
        }

    private fun Single<out LegacyProject>.checkReceivedProjectIdIsAsExpected(expectedProjectId: String): Completable =
        flatMapCompletable { legacyProject ->
            val receivedProjectId = legacyProject.projectId
            Completable.create {
                if (receivedProjectId == expectedProjectId)
                    it.onComplete()
                else it.onError(DifferentProjectIdReceivedFromIntentException.withProjectIds(expectedProjectId, receivedProjectId))
            }
        }
}
