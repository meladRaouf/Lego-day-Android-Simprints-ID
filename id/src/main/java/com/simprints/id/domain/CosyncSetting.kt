package com.simprints.id.domain

import androidx.annotation.Keep

@Keep
enum class CosyncSetting {
    NONE,
    ONLY_BIOMETRICS,
    ONLY_ANALYTICS,
    ALL
}
