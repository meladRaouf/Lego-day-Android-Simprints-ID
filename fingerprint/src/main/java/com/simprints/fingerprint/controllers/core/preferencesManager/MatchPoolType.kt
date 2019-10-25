package com.simprints.fingerprint.controllers.core.preferencesManager

import androidx.annotation.Keep

@Keep
enum class MatchPoolType {
    USER,
    MODULE,
    PROJECT;

    companion object {
        fun fromQueryForIdentifyPool(queryForIdentifyPool: QueryForIdentifyPool): MatchPoolType =
            when {
                queryForIdentifyPool.userId != null -> USER
                queryForIdentifyPool.moduleId != null -> MODULE
                else -> PROJECT
            }
    }
}
