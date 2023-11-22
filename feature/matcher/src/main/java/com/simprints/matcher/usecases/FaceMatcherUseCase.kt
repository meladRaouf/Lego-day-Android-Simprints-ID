package com.simprints.matcher.usecases

import com.simprints.infra.enrolment.records.sync.EnrolmentRecordManager
import com.simprints.infra.facebiosdk.matching.FaceIdentity
import com.simprints.infra.facebiosdk.matching.FaceMatcher
import com.simprints.infra.facebiosdk.matching.FaceSample
import com.simprints.infra.logging.LoggingConstants
import com.simprints.matcher.FaceMatchResult
import com.simprints.matcher.MatchParams
import com.simprints.matcher.MatchResultItem
import java.io.Serializable
import javax.inject.Inject

internal class FaceMatcherUseCase @Inject constructor(
    private val enrolmentRecordManager: EnrolmentRecordManager,
    private val faceMatcher: FaceMatcher,
) : MatcherUseCase {

    override val crashReportTag = LoggingConstants.CrashReportTag.FACE_MATCHING.name
    override val matcherName = faceMatcher.matcherName

    override suspend operator fun invoke(
        matchParams: MatchParams,
        onLoadingCandidates: (tag: String) -> Unit,
        onMatching: (tag: String) -> Unit,
    ): List<MatchResultItem> {
        if (matchParams.probeFaceSamples.isEmpty()) {
            return emptyList()
        }

        val samples = mapSamples(matchParams.probeFaceSamples)

        onLoadingCandidates(crashReportTag)
        val candidates = getCandidates(matchParams.queryForCandidates)

        onMatching(crashReportTag)
        return match(samples, candidates)
    }

    private fun mapSamples(probes: List<MatchParams.FaceSample>) = probes
        .map { FaceSample(it.faceId, it.template) }

    private suspend fun getCandidates(query: Serializable) = enrolmentRecordManager
        .loadFaceIdentities(query)
        .map {
            FaceIdentity(
                it.personId,
                it.faces.map { face -> FaceSample(face.id, face.template) }
            )
        }

    private suspend fun match(
        probes: List<FaceSample>,
        candidates: List<FaceIdentity>,
    ) = candidates
        .map { candidate ->
            FaceMatchResult.Item(
                candidate.faceId,
                faceMatcher.getHighestComparisonScoreForCandidate(probes, candidate)
            )
        }
        .sortedByDescending { it.confidence }

}
