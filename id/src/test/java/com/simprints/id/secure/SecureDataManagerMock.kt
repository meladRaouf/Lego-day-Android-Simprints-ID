package com.simprints.id.secure

import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.shared.whenever

fun mockLoginInfoManager(loginInfoManagerMock: LoginInfoManager,
                                encryptedProjectSecret: String = "encryptedProjectSecret",
                                projectId: String = "project_id",
                                signedInUserId: String = "signedInUserId"): LoginInfoManager {

    whenever(loginInfoManagerMock.encryptedProjectSecret).thenReturn(encryptedProjectSecret)
    whenever(loginInfoManagerMock.signedInProjectId).thenReturn(projectId)
    whenever(loginInfoManagerMock.signedInUserId).thenReturn(signedInUserId)
    return loginInfoManagerMock
}
