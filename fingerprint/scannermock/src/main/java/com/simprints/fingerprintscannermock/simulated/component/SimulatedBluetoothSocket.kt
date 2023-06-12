package com.simprints.fingerprintscannermock.simulated.component

import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothSocket
import com.simprints.fingerprintscannermock.simulated.SimulatedScannerManager
import java.io.InputStream
import java.io.OutputStream


class SimulatedBluetoothSocket(private val simulatedScannerManager: SimulatedScannerManager) : ComponentBluetoothSocket {

    override fun connect() = simulatedScannerManager.connect()

    override fun getInputStream(): InputStream = simulatedScannerManager.streamFromScannerToApp

    override fun getOutputStream(): OutputStream = simulatedScannerManager.streamFromAppToScanner

    override fun close() = simulatedScannerManager.close()
}