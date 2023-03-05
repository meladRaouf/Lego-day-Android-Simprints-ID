package com.simprints.feature.dashboard.settings.syncinfo.moduleselection.repository

import com.simprints.core.domain.modality.Modes
import com.simprints.eventsystem.events_sync.down.EventDownSyncScopeRepository
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import com.simprints.infra.enrolment.records.domain.models.SubjectQuery
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.logging.LoggingConstants.CrashReportingCustomKeys.MODULE_IDS
import com.simprints.infra.logging.Simber
import javax.inject.Inject

// TODO move into the event system infra module?
internal class ModuleRepositoryImpl @Inject constructor(
    private val configManager: ConfigManager,
    private val enrolmentRecordManager: EnrolmentRecordManager,
    private val eventDownSyncScopeRepository: EventDownSyncScopeRepository
) : ModuleRepository {

    override suspend fun getModules(): List<Module> =
        configManager.getProjectConfiguration().synchronization.down.moduleOptions.map {
            Module(it, isModuleSelected(it))
        }

    override suspend fun saveModules(modules: List<Module>) {
        setSelectedModules(modules.filter { it.isSelected })
        handleUnselectedModules(modules.filter { !it.isSelected })
    }

    override suspend fun getMaxNumberOfModules(): Int =
        configManager.getProjectConfiguration().synchronization.down.maxNbOfModules

    private suspend fun isModuleSelected(moduleName: String): Boolean {
        return configManager.getDeviceConfiguration().selectedModules.contains(moduleName)
    }

    private suspend fun setSelectedModules(selectedModules: List<Module>) {
        configManager.updateDeviceConfiguration {
            it.apply {
                this.selectedModules = selectedModules.map { module -> module.name }
                logMessageForCrashReport("Modules set to ${this.selectedModules}")
                setCrashlyticsKeyForModules(this.selectedModules)
            }
        }
    }

    private suspend fun handleUnselectedModules(unselectedModules: List<Module>) {
        val queries = unselectedModules.map {
            SubjectQuery(moduleId = it.name)
        }
        enrolmentRecordManager.delete(queries)

        // Delete operations for unselected modules to ensure full sync if they are reselected
        // in the future
        eventDownSyncScopeRepository.deleteOperations(
            unselectedModules.map { it.name },
            configManager.getProjectConfiguration().general.modalities.map { it.toMode() }
        )
    }

    private fun GeneralConfiguration.Modality.toMode(): Modes =
        when (this) {
            GeneralConfiguration.Modality.FACE -> Modes.FACE
            GeneralConfiguration.Modality.FINGERPRINT -> Modes.FINGERPRINT
        }

    private fun setCrashlyticsKeyForModules(modules: List<String>) {
        Simber.tag(MODULE_IDS, true).i(modules.toString())
    }

    private fun logMessageForCrashReport(message: String) {
        Simber.tag(LoggingConstants.CrashReportTag.SETTINGS.name).i(message)
    }
}