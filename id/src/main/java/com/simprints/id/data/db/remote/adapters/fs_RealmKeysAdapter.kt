package com.simprints.id.data.db.remote.adapters

import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.remote.models.fs_RealmKeys

fun fs_RealmKeys.toLocalDbKey(): LocalDbKey =
    LocalDbKey(
        projectId = this.projectId,
        value = this.value.toBytes(),
        legacyApiKey = this.legacyValue
    )
