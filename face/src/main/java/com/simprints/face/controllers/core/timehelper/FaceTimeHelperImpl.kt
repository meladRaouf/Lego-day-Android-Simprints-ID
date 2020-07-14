package com.simprints.face.controllers.core.timehelper

import com.simprints.id.tools.TimeHelper

class FaceTimeHelperImpl(private val timeHelper: TimeHelper) : FaceTimeHelper {
    override fun now(): Long = timeHelper.now()
}