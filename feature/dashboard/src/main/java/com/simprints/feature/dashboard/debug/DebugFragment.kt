package com.simprints.feature.dashboard.debug

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkManager
import com.simprints.core.DispatcherIO
import com.simprints.infra.uibase.viewbinding.viewBinding
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentDebugBinding
import com.simprints.infra.authlogic.AuthManager
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import com.simprints.infra.events.EventRepository
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.eventsync.status.models.EventSyncWorkerState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
internal class DebugFragment : Fragment(R.layout.fragment_debug) {

    @Inject
    lateinit var eventSyncManager: EventSyncManager

    @Inject
    lateinit var configManager: ConfigManager

    @Inject
    lateinit var authStore: AuthStore

    @Inject
    lateinit var authManager: AuthManager

    @Inject
    lateinit var eventRepository: EventRepository

    @Inject
    lateinit var enrolmentRecordManager: EnrolmentRecordManager

    @Inject
    @DispatcherIO
    lateinit var dispatcher: CoroutineDispatcher

    private val binding by viewBinding(FragmentDebugBinding::bind)
    private val wm: WorkManager
        get() = WorkManager.getInstance(requireContext())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        eventSyncManager.getLastSyncState().observe(viewLifecycleOwner) { state ->
            val states =
                (state.downSyncWorkersInfo.map { it.state } + state.upSyncWorkersInfo.map { it.state })
            val message =
                "${state.syncId.takeLast(5)} - " +
                    "${states.toDebugActivitySyncState().name} - " +
                    "${state.progress}/${state.total}"

            val ssb = SpannableStringBuilder(
                coloredText(
                    "\n$message",
                    Color.parseColor(getRandomColor())
                )
            )

            binding.logs.append(ssb)
        }

        binding.syncStart.setOnClickListener {
            eventSyncManager.sync()
        }

        binding.syncStop.setOnClickListener {
            eventSyncManager.stop()
        }

        binding.syncSchedule.setOnClickListener {
            eventSyncManager.scheduleSync()
        }

        binding.syncConfig.setOnClickListener {
            binding.logs.append("\nGetting Configs from BFSID")
            lifecycleScope.launch {
                try {
                    configManager.refreshProjectConfiguration(authStore.signedInProjectId)
                    binding.logs.append("\nGot Configs from BFSID")
                } catch (e: Exception) {
                    binding.logs.append("\nFailed to refresh the project configuration")
                }
            }
        }

        binding.syncDevice.setOnClickListener {
            authManager.startSecurityStateCheck()
        }

        binding.printRoomDb.setOnClickListener {
            binding.logs.text = ""
            runBlocking {
                val logStringBuilder = StringBuilder()
                logStringBuilder.append("\nSubjects ${enrolmentRecordManager.count()}")

                val events = eventRepository.loadAll().toList().groupBy { it.type }
                events.forEach {
                    logStringBuilder.append("\n${it.key} ${it.value.size}")
                }

                binding.logs.text = logStringBuilder.toString()
            }
        }

        binding.cleanAll.setOnClickListener {
            lifecycleScope.launch(dispatcher) {
                eventSyncManager.cancelScheduledSync()
                eventSyncManager.stop()
                eventRepository.deleteAll()
                eventSyncManager.resetDownSyncInfo()
                enrolmentRecordManager.deleteAll()
                wm.pruneWork()
            }
        }
    }

    private fun getRandomColor(): String =
        arrayOf("red", "black", "purple", "green", "blue").random()

    private fun coloredText(text: String, color: Int): SpannableString {
        val spannableString = SpannableString(text)
        spannableString.setSpan(
            ForegroundColorSpan(color), 0,
            text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannableString
    }

    private fun List<EventSyncWorkerState>.toDebugActivitySyncState(): DebugActivitySyncState =
        when {
            isEmpty() -> DebugActivitySyncState.NOT_RUNNING
            this.any { it is EventSyncWorkerState.Running } -> DebugActivitySyncState.RUNNING
            this.any { it is EventSyncWorkerState.Enqueued } -> DebugActivitySyncState.CONNECTING
            this.all { it is EventSyncWorkerState.Succeeded } -> DebugActivitySyncState.SUCCESS
            else -> DebugActivitySyncState.FAILED
        }

    enum class DebugActivitySyncState {
        RUNNING,
        NOT_RUNNING,
        CONNECTING,
        SUCCESS,
        FAILED
    }
}