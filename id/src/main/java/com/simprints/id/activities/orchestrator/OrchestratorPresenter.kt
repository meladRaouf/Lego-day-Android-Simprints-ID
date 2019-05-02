package com.simprints.id.activities.orchestrator

import android.annotation.SuppressLint
import android.content.Intent
import com.simprints.id.activities.orchestrator.di.OrchestratorComponentInjector
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.domain.moduleapi.app.DomainToAppResponse
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppResponse
import com.simprints.id.orchestrator.OrchestratorManager
import com.simprints.id.orchestrator.modality.ModalityStepRequest
import com.simprints.id.services.scheduledSync.SyncSchedulerHelper
import com.simprints.moduleapi.app.responses.IAppResponse
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import javax.inject.Inject

class OrchestratorPresenter: OrchestratorContract.Presenter {

    @Inject lateinit var orchestratorManager: OrchestratorManager
    @Inject lateinit var sessionEventsManager: SessionEventsManager
    @Inject lateinit var syncSchedulerHelper: SyncSchedulerHelper

    @Inject
    lateinit var view: OrchestratorContract.View

    override lateinit var appRequest: AppRequest

    init {
        OrchestratorComponentInjector.inject(this)
    }

    @SuppressLint("CheckResult")
    override fun start() {
        subscribeForModalitiesResponses()

        syncSchedulerHelper.scheduleBackgroundSyncs()
        syncSchedulerHelper.startDownSyncOnLaunchIfPossible()
    }

    @SuppressLint("CheckResult")
    internal fun subscribeForModalitiesResponses() =
        getSessionId().flatMapObservable {
            orchestratorManager.startFlow(appRequest, it).also {
                subscribeForFinalAppResponse()
            }
        }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeBy(
            onNext = {
                handleNextModalityRequest(it)
            },
            onError = {
                handleErrorInTheModalitiesFlow(it)
            })

    @SuppressLint("CheckResult")
    internal fun subscribeForFinalAppResponse() =
        orchestratorManager.getAppResponse()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    handleAppResponse(it)
                },
                onError = {
                    handleErrorInTheModalitiesFlow(it)
                })

    private fun handleAppResponse(appResponse: AppResponse) =
        view.setResultAndFinish(appResponse)

    @SuppressLint("CheckResult")
    internal fun getSessionId(): Single<String> =
        sessionEventsManager.getCurrentSession().map { it.id }

    private fun handleNextModalityRequest(modalityRequest: ModalityStepRequest) =
        view.startNextActivity(modalityRequest.requestCode, modalityRequest.intent)

    private fun handleErrorInTheModalitiesFlow(it: Throwable) {
        it.printStackTrace()
        view.setCancelResultAndFinish()
        Timber.d(it.message)
    }

    override fun handleResult(requestCode: Int, resultCode: Int, data: Intent?) {
        orchestratorManager.onModalStepRequestDone(requestCode, resultCode, data)
    }

    override fun fromDomainToAppResponse(response: AppResponse?): IAppResponse? =
        DomainToAppResponse.fromDomainToAppResponse(response)
}
