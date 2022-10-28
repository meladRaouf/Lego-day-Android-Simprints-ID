package com.simprints.fingerprint.scanner.controllers.v2

import com.simprints.fingerprint.scanner.adapters.v2.toScannerVersion
import com.simprints.fingerprint.scanner.data.local.FirmwareLocalDataSource
import com.simprints.fingerprint.scanner.domain.BatteryInfo
import com.simprints.fingerprint.scanner.domain.ota.AvailableOta
import com.simprints.fingerprint.scanner.domain.ota.DownloadableFirmwareVersion.Chip
import com.simprints.fingerprint.scanner.domain.versions.ScannerFirmwareVersions
import com.simprints.fingerprint.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.scanner.exceptions.safe.OtaAvailableException
import com.simprints.fingerprint.tools.BatteryLevelChecker
import com.simprints.fingerprintscanner.v2.scanner.Scanner
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.Vero2Configuration
import kotlinx.coroutines.delay
import kotlinx.coroutines.rx2.await
import javax.inject.Inject

/**
 * For handling the initial setup to the scanner upon connection, such as retrieving and checking
 * the firmware version and battery level, and determining whether OTA is necessary.
 */
class ScannerInitialSetupHelper @Inject constructor(
    private val connectionHelper: ConnectionHelper,
    private val batteryLevelChecker: BatteryLevelChecker,
    private val configManager: ConfigManager,
    private val firmwareLocalDataSource: FirmwareLocalDataSource
) {

    private lateinit var scannerVersion: ScannerVersion


    /**
     * This method is responsible for checking if any firmware updates are available, the current
     * scanner firmware version [ScannerVersion] and the current battery information [BatteryInfo].
     *
     * @param scanner  the scanner object to read battery and version info from
     * @param macAddress  the mac address of the scanner, for establishing connection if disconnected
     * @param withBatteryInfo  the callback that receives the retrieved [BatteryInfo] to the calling function
     * @param withScannerVersion  the callback that receives the retrieved [ScannerVersion] to the calling function
     *
     * @throws OtaAvailableException If an OTA update is available and the battery is sufficiently charged.
     */
    suspend fun setupScannerWithOtaCheck(
        scanner: Scanner,
        macAddress: String,
        withScannerVersion: (ScannerVersion) -> Unit,
        withBatteryInfo: (BatteryInfo) -> Unit
    ) {
        delay(100) // Speculatively needed
        val unifiedVersionInfo = scanner.getVersionInformation().await()

        unifiedVersionInfo.toScannerVersion().also {
            withScannerVersion(it)
            scannerVersion = it
        }

        scanner.enterMainMode().await()
        delay(100) // Speculatively needed
        val batteryInfo = getBatteryInfo(scanner, withBatteryInfo)
        ifAvailableOtasPrepareScannerThenThrow(
            scannerVersion.hardwareVersion,
            scanner,
            macAddress,
            batteryInfo
        )
    }


    private suspend fun getBatteryInfo(
        scanner: Scanner,
        withBatteryInfo: (BatteryInfo) -> Unit
    ): BatteryInfo {
        val batteryPercent = scanner.getBatteryPercentCharge().await()
        val batteryVoltage = scanner.getBatteryVoltageMilliVolts().await()
        val batteryMilliAmps = scanner.getBatteryCurrentMilliAmps().await()
        val batteryTemperature = scanner.getBatteryTemperatureDeciKelvin().await()

        return BatteryInfo(
            batteryPercent,
            batteryVoltage,
            batteryMilliAmps,
            batteryTemperature
        ).also {
            withBatteryInfo(it)
        }
    }

    private suspend fun ifAvailableOtasPrepareScannerThenThrow(
        hardwareVersion: String,
        scanner: Scanner,
        macAddress: String,
        batteryInfo: BatteryInfo
    ) {
        val availableVersions =
            configManager.getProjectConfiguration().fingerprint?.vero2?.firmwareVersions?.get(
                hardwareVersion
            )
        val availableOtas = determineAvailableOtas(scannerVersion.firmware, availableVersions)
        val requiresOtaUpdate = availableOtas.isNotEmpty()
            && !batteryInfo.isLowBattery()
            && !batteryLevelChecker.isLowBattery()

        if (requiresOtaUpdate) {
            connectionHelper.reconnect(scanner, macAddress)
            throw OtaAvailableException(availableOtas)
        }
    }

    private fun determineAvailableOtas(
        current: ScannerFirmwareVersions,
        available: Vero2Configuration.Vero2FirmwareVersions?
    ): List<AvailableOta> {
        if (available == null) {
            return emptyList()
        }
        val localFiles = firmwareLocalDataSource.getAvailableScannerFirmwareVersions()
        return listOfNotNull(
            if (
                localFiles[Chip.CYPRESS]?.contains(available.cypress) == true
                && current.cypress != available.cypress
            ) AvailableOta.CYPRESS else null,
            if (localFiles[Chip.STM]?.contains(available.stm) == true &&
                current.stm != available.stm
            ) AvailableOta.STM else null,
            if (localFiles[Chip.UN20]?.contains(available.un20) == true &&
                current.un20 != available.un20
            ) AvailableOta.UN20 else null
        )
    }
}
