package com.simprints.fingerprint.data.domain.matching.result

enum class MatchingTier {
    TIER_1,
    TIER_2,
    TIER_3,
    TIER_4,
    TIER_5;

    companion object {
        @JvmStatic fun computeTier(score: Float): MatchingTier {
            return when {
                score < 20f -> TIER_5
                score < 35f -> TIER_4
                score < 50f -> TIER_3
                score < 75f -> TIER_2
                else -> TIER_1
            }
        }
    }
}