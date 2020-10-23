package com.simprints.fingerprint.scanner.domain.versions

data class ChipApiVersion(val major: Int, val minor: Int) {

    fun combined(): Int = (major shl 16) + minor.toShort()

    operator fun compareTo(other: ChipApiVersion) = this.combined() - other.combined()

    override fun toString() = "$major.$minor"

    companion object {
        val UNKNOWN = ChipApiVersion(-1, -1)
    }
}
