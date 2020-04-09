package com.simprints.fingerprint.scanner.pairing

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.text.isDigitsOnly
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothDevice
import java.util.regex.Pattern

class ScannerPairingManager(private val bluetoothAdapter: ComponentBluetoothAdapter) {

    /**
     * Turns user entered text into a valid serial number, e.g. "003456" -> "SP003456"
     * and "9" -> "SP000009"
     * @throws NumberFormatException if the text does not contain an appropriate number
     */
    fun interpretEnteredTextAsSerialNumber(text: String): String {
        if (!text.isDigitsOnly()) throw NumberFormatException("Non-digits found in serial number: $text")
        val number = text.toInt()
        if (number < 0 || number > 999999) throw NumberFormatException("Number out of range for serial number")
        return SERIAL_PREFIX + number.toString(10).padStart(6, '0')
    }

    /**
     * Un-pairs all other devices that follow Simprints' MAC address format, and begins pairing to
     * the given address
     */
    fun pairOnlyToDevice(address: String) {
        val device = bluetoothAdapter.getRemoteDevice(address)

        bluetoothAdapter.getBondedDevices().forEach {
            if (isScannerAddress(it.address) && it.address != device.address) {
                it.removeBond()
            }
        }

        device.createBond()
    }

    fun getPairedScannerAddresses(): List<String> =
        bluetoothAdapter
            .getBondedDevices()
            .map { it.address }
            .filter { isScannerAddress(it) }

    fun isOnlyPairedToOneScanner(): Boolean =
        getPairedScannerAddresses().count() == 1

    fun convertAddressToSerialNumber(address: String): String =
        SERIAL_PREFIX + address.replace(":", "").substring(7..11)
            .toInt(16).toString(10).padStart(6, '0')

    fun convertSerialNumberToAddress(serialNumber: String): String =
        serialNumber.removePrefix(SERIAL_PREFIX).toInt().let {
            serialHexToMacAddress(getMacHexFromInt(it))
        }

    @SuppressLint("DefaultLocale")
    private fun getMacHexFromInt(int: Int): String = Integer.toHexString(int)
        .toUpperCase().padStart(5, '0')

    private fun serialHexToMacAddress(hex: String): String = MAC_ADDRESS_PREFIX +
        StringBuilder(hex).insert(1, ":").insert(4, ":").toString()

    fun isScannerAddress(macAddress: String): Boolean =
        SCANNER_ADDRESS_REGEX.matcher(macAddress).matches()

    fun bluetoothPairStateChangeReceiver(onPairSuccess: () -> Unit, onPairFailed: () -> Unit): BroadcastReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                if (intent.action == ComponentBluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                    val bondState = intent.getIntExtra(ComponentBluetoothDevice.EXTRA_BOND_STATE, ComponentBluetoothDevice.BOND_NONE)
                    val failReason = intent.getIntExtra(ComponentBluetoothDevice.EXTRA_REASON, ComponentBluetoothDevice.BOND_SUCCESS)
                    val pairSucceeded = bondState == ComponentBluetoothDevice.BOND_BONDED
                    val pairingFailed = bondState == ComponentBluetoothDevice.BOND_NONE
                        && failReason != ComponentBluetoothDevice.BOND_SUCCESS
                        && failReason != ComponentBluetoothDevice.UNBOND_REASON_REMOVED
                    if (pairSucceeded) {
                        onPairSuccess()
                    } else if (pairingFailed) {
                        onPairFailed()
                    }
                }
            }
        }

    companion object {
        private const val MAC_ADDRESS_PREFIX = "F0:AC:D7:C"
        private val SCANNER_ADDRESS_REGEX = Pattern.compile("$MAC_ADDRESS_PREFIX\\p{XDigit}:\\p{XDigit}{2}:\\p{XDigit}{2}")
        private const val SERIAL_PREFIX = "SP"
    }
}