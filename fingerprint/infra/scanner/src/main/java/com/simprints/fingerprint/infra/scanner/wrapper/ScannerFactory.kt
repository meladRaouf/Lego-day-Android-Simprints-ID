package com.simprints.fingerprint.infra.scanner.wrapper

import com.simprints.core.DispatcherIO
import com.simprints.fingerprint.infra.scanner.capture.FingerprintCaptureWrapperFactory
import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprint.infra.scanner.helpers.ConnectionHelper
import com.simprints.fingerprint.infra.scanner.helpers.CypressOtaHelper
import com.simprints.fingerprint.infra.scanner.helpers.ScannerInitialSetupHelper
import com.simprints.fingerprint.infra.scanner.helpers.StmOtaHelper
import com.simprints.fingerprint.infra.scanner.helpers.Un20OtaHelper
import com.simprints.fingerprint.infra.scanner.tools.ScannerGenerationDeterminer
import com.simprints.fingerprint.infra.scanner.tools.SerialNumberConverter
import com.simprints.fingerprint.infra.scanner.v2.scanner.create
import com.simprints.fingerprint.infra.scanner.v2.tools.ScannerUiHelper
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Singleton
import com.simprints.fingerprint.infra.scanner.v1.Scanner as ScannerV1
import com.simprints.fingerprint.infra.scanner.v2.scanner.Scanner as ScannerV2

@Singleton
class ScannerFactory @Inject internal constructor(
    private val bluetoothAdapter: ComponentBluetoothAdapter,
    private val configRepository: ConfigRepository,
    private val scannerUiHelper: ScannerUiHelper,
    private val serialNumberConverter: SerialNumberConverter,
    private val scannerGenerationDeterminer: ScannerGenerationDeterminer,
    private val scannerInitialSetupHelper: ScannerInitialSetupHelper,
    private val connectionHelper: ConnectionHelper,
    private val cypressOtaHelper: CypressOtaHelper,
    private val stmOtaHelper: StmOtaHelper,
    private val un20OtaHelper: Un20OtaHelper,
    private val fingerprintCaptureWrapperFactory: FingerprintCaptureWrapperFactory,
    @DispatcherIO private val ioDispatcher: CoroutineDispatcher,
) {

    var scannerV1: ScannerV1? = null
    var scannerV2: ScannerV2? = null
    var scannerOtaOperationsWrapper: ScannerOtaOperationsWrapper? = null
    var scannerWrapper: ScannerWrapper? = null


    suspend fun initScannerOperationWrappers(macAddress: String) {
        val availableScannerGenerations =
            configRepository.getProjectConfiguration().fingerprint?.allowedScanners ?: listOf()

        val scannerGenerationToUse = when (availableScannerGenerations.size) {
            1 -> availableScannerGenerations.single()
            else -> scannerGenerationDeterminer.determineScannerGenerationFromSerialNumber(
                serialNumberConverter.convertMacAddressToSerialNumber(macAddress)
            )
        }.also {
            Simber.i("Using scanner generation $it")
        }

        when (scannerGenerationToUse) {
            FingerprintConfiguration.VeroGeneration.VERO_1 -> {
                scannerV1 = ScannerV1(macAddress, bluetoothAdapter)
                scannerWrapper = createScannerWrapperV1()
                //Cache the scannerV1 instance to be used by the FingerprintCaptureWrapperFactory
                fingerprintCaptureWrapperFactory.createV1(scannerV1!!)
            }

            FingerprintConfiguration.VeroGeneration.VERO_2 -> {
                scannerV2 = ScannerV2.create()
                scannerWrapper = createScannerWrapperV2(macAddress)
                // Create OTA wrapper for V2 scanner only as V1 scanner doesn't support OTA
                scannerOtaOperationsWrapper = createScannerOtaOperationsWrapper(macAddress)
                //Cache the scannerV2 instance to be used by the FingerprintCaptureWrapperFactory
                fingerprintCaptureWrapperFactory.createV2(scannerV2!!)
            }
        }
    }

    private fun createScannerOtaOperationsWrapper(macAddress: String): ScannerOtaOperationsWrapper =
        ScannerOtaOperationsWrapper(
            macAddress,
            scannerV2!!,
            cypressOtaHelper,
            stmOtaHelper,
            un20OtaHelper,
            ioDispatcher,
        )

    private fun createScannerWrapperV1(): ScannerWrapper {
        return ScannerWrapperV1(
            scannerV1!!,
            ioDispatcher,
        )
    }

    private fun createScannerWrapperV2(macAddress: String): ScannerWrapper {
        return ScannerWrapperV2(
            scannerV2!!,
            scannerUiHelper,
            macAddress,
            scannerInitialSetupHelper,
            connectionHelper,
            ioDispatcher,
        )
    }
}
