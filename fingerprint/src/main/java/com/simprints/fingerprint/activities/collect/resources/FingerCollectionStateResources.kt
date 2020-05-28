package com.simprints.fingerprint.activities.collect.resources

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.collect.state.FingerCollectionState

@DrawableRes
fun FingerCollectionState.indicatorDrawableId(selected: Boolean): Int =
    if (selected) indicatorSelectedDrawableId() else indicatorDeselectedDrawableId()

@DrawableRes
fun FingerCollectionState.indicatorSelectedDrawableId(): Int =
    when (this) {
        is FingerCollectionState.NotCollected,
        is FingerCollectionState.Scanning,
        is FingerCollectionState.TransferringImage -> R.drawable.ic_blank_selected
        is FingerCollectionState.Skipped,
        is FingerCollectionState.NotDetected -> R.drawable.ic_alert_selected
        is FingerCollectionState.Collected -> if (scanResult.isGoodScan()) {
            R.drawable.ic_ok_selected
        } else {
            R.drawable.ic_alert_selected
        }
    }

@DrawableRes
fun FingerCollectionState.indicatorDeselectedDrawableId(): Int =
    when (this) {
        is FingerCollectionState.NotCollected,
        is FingerCollectionState.Scanning,
        is FingerCollectionState.TransferringImage -> R.drawable.ic_blank_deselected
        is FingerCollectionState.Skipped,
        is FingerCollectionState.NotDetected -> R.drawable.ic_alert_deselected
        is FingerCollectionState.Collected -> if (scanResult.isGoodScan()) {
            R.drawable.ic_ok_deselected
        } else {
            R.drawable.ic_alert_deselected
        }
    }

@StringRes
fun FingerCollectionState.buttonTextId(isAskingRescan: Boolean): Int =
    when (this) {
        is FingerCollectionState.NotCollected -> R.string.scan_label
        is FingerCollectionState.Scanning -> R.string.cancel_button
        is FingerCollectionState.TransferringImage -> R.string.please_wait_button
        is FingerCollectionState.Skipped,
        is FingerCollectionState.NotDetected -> R.string.rescan_label
        is FingerCollectionState.Collected -> if (scanResult.isGoodScan()) {
            if (isAskingRescan) {
                R.string.rescan_label_question
            } else {
                R.string.good_scan_message
            }
        } else {
            R.string.rescan_label
        }
    }

@Suppress("unused")
@ColorRes
fun FingerCollectionState.buttonTextColour(): Int =
    android.R.color.white

@ColorRes
fun FingerCollectionState.buttonBackgroundColour(): Int =
    when (this) {
        is FingerCollectionState.NotCollected -> R.color.simprints_grey
        is FingerCollectionState.Scanning,
        is FingerCollectionState.TransferringImage -> R.color.simprints_blue
        is FingerCollectionState.Skipped,
        is FingerCollectionState.NotDetected -> R.color.simprints_red
        is FingerCollectionState.Collected -> if (scanResult.isGoodScan()) {
            R.color.simprints_green
        } else {
            R.color.simprints_red
        }
    }

@StringRes
fun FingerCollectionState.resultTextId(): Int =
    when (this) {
        is FingerCollectionState.NotCollected -> R.string.empty
        is FingerCollectionState.Scanning -> R.string.empty
        is FingerCollectionState.TransferringImage -> if (scanResult.isGoodScan()) {
            R.string.good_scan_message
        } else {
            R.string.poor_scan_message
        }
        is FingerCollectionState.Skipped -> R.string.finger_skipped_message
        is FingerCollectionState.NotDetected -> R.string.no_finger_detected_message
        is FingerCollectionState.Collected -> if (scanResult.isGoodScan()) {
            R.string.good_scan_message
        } else {
            R.string.poor_scan_message
        }
    }

@ColorRes
fun FingerCollectionState.resultTextColour(): Int =
    when (this) {
        is FingerCollectionState.NotCollected,
        is FingerCollectionState.Scanning -> android.R.color.white
        is FingerCollectionState.TransferringImage -> if (scanResult.isGoodScan()) {
            R.color.simprints_green
        } else {
            R.color.simprints_red
        }
        is FingerCollectionState.Skipped,
        is FingerCollectionState.NotDetected -> R.color.simprints_red
        is FingerCollectionState.Collected -> if (scanResult.isGoodScan()) {
            R.color.simprints_green
        } else {
            R.color.simprints_red
        }
    }

@StringRes
fun FingerCollectionState.directionTextId(isLastFinger: Boolean): Int =
    when (this) {
        is FingerCollectionState.NotCollected -> R.string.please_scan
        is FingerCollectionState.Scanning -> R.string.scanning
        is FingerCollectionState.TransferringImage -> R.string.transfering_data
        is FingerCollectionState.Skipped -> R.string.good_scan_direction
        is FingerCollectionState.NotDetected -> R.string.poor_scan_direction
        is FingerCollectionState.Collected -> if (scanResult.isGoodScan()) {
            if (isLastFinger) R.string.empty else R.string.good_scan_direction
        } else {
            R.string.poor_scan_direction
        }
    }

@Suppress("unused")
@ColorRes
fun FingerCollectionState.directionTextColour(): Int =
    R.color.simprints_grey