package com.simprints.id.services.scheduledSync.peopleDownSync.workers

import androidx.work.Data
import androidx.work.ListenableWorker

interface WorkerResultSetter {

    fun success(outputData: Data? = null): ListenableWorker.Result
    fun failure(outputData: Data? = null): ListenableWorker.Result
    fun retry(): ListenableWorker.Result
}
