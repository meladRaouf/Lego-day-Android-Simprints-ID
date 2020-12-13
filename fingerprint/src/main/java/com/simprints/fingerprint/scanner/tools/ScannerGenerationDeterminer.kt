package com.simprints.fingerprint.scanner.tools

import com.simprints.fingerprint.scanner.domain.ScannerGeneration

/**
 * Helper class for determining which Vero generation a particular MAC address corresponds to.
 */
class ScannerGenerationDeterminer {

    /**
     * Vero 2 serial numbers are taken from the range 000000 - 099999. Some Vero 1 serial numbers
     * are known to be in this range as well, so these should be taken into account.
     * @param serialNumber The serial number in "SPXXXXXX" format
     */
    fun determineScannerGenerationFromSerialNumber(serialNumber: String): ScannerGeneration {
        val serialInt = serialNumber.substring(serialIntIndices).toInt()
        return if (
            serialInt < VERO_2_CUT_OFF &&
            !VERO_1_SERIAL_NUMBERS_BELOW_CUT_OFF.contains(serialNumber)
        ) {
            ScannerGeneration.VERO_2
        } else {
            ScannerGeneration.VERO_1
        }
    }

    companion object {
        val serialIntIndices = 2 until 8

        const val VERO_2_CUT_OFF = 100000

        val VERO_1_SERIAL_NUMBERS_BELOW_CUT_OFF = listOf(
            "SP064060", "SP018257", "SP072107", "SP009113", "SP047961", "SP023967",
            "SP023026", "SP085717", "SP017069", "SP000652", "SP055227", "SP031393",
            "SP094196", "SP022685", "SP082543", "SP086686", "SP049615", "SP067714",
            "SP068034", "SP045406", "SP077816", "SP036211", "SP037323", "SP035143",
            "SP006219", "SP033848", "SP057763", "SP040603", "SP003998", "SP066311",
            "SP091614", "SP056658", "SP085179", "SP029024", "SP052909", "SP060713",
            "SP055826", "SP049905", "SP098483", "SP099914", "SP019317", "SP031069",
            "SP031385", "SP001541", "SP063876", "SP000388", "SP064704", "SP044287",
            "SP059041", "SP004323", "SP079255", "SP077640", "SP042802", "SP086414",
            "SP069593", "SP084947", "SP029227", "SP093312", "SP003312", "SP061806",
            "SP045814", "SP092123", "SP033182", "SP025819", "SP045157", "SP052563",
            "SP088811", "SP019632", "SP012877", "SP044766", "SP014017", "SP084183",
            "SP013842", "SP046829", "SP091924", "SP075606", "SP084941", "SP054233",
            "SP033860", "SP099322", "SP067628", "SP026913", "SP017123", "SP050674",
            "SP076713", "SP084710", "SP043043", "SP074601", "SP044907", "SP049921",
            "SP074136", "SP027435", "SP011685", "SP083006", "SP080277", "SP008638",
            "SP020949", "SP070539", "SP070926", "SP046882", "SP054431", "SP024902",
            "SP039732", "SP046231", "SP045788", "SP064816", "SP043240", "SP020361",
            "SP017010", "SP077410", "SP026253", "SP038382", "SP004918", "SP045180",
            "SP015632", "SP058935", "SP019767", "SP003145", "SP030277", "SP005795",
            "SP089470", "SP059171", "SP039840", "SP048470", "SP067479", "SP051660",
            "SP059154", "SP034426", "SP068480", "SP049633", "SP072545", "SP046016",
            "SP024433", "SP006409", "SP019489", "SP011875", "SP094925", "SP042495",
            "SP038000", "SP035700", "SP078739", "SP017131", "SP098335", "SP053901",
            "SP020195", "SP061209", "SP035367", "SP024588", "SP063440", "SP005144",
            "SP002493", "SP077960", "SP071792", "SP074824", "SP008889", "SP066771",
            "SP009025", "SP008391", "SP074966", "SP002166", "SP009600", "SP081244",
            "SP067383", "SP029377", "SP024406", "SP073224", "SP075888", "SP062817",
            "SP053023", "SP040557", "SP026338", "SP028168", "SP058716", "SP072814",
            "SP087835", "SP017660", "SP050417", "SP014393", "SP026489", "SP013645",
            "SP049210", "SP072232", "SP024613", "SP048940", "SP027352", "SP035320",
            "SP040374", "SP083799", "SP033266", "SP071655", "SP061671", "SP012096",
            "SP022476", "SP036263", "SP058394", "SP011727", "SP038391", "SP068010",
            "SP055571", "SP063753", "SP052101", "SP000552", "SP093775", "SP086489",
            "SP041683", "SP025252", "SP047256", "SP080402", "SP021501", "SP020615",
            "SP012292", "SP022464", "SP026612", "SP084160", "SP069151", "SP055191",
            "SP032318", "SP019433", "SP059388", "SP012461", "SP038709", "SP069281",
            "SP042753", "SP086201", "SP083979", "SP014471", "SP032720", "SP045246",
            "SP099082", "SP066294", "SP029819", "SP071000", "SP041841", "SP067085",
            "SP030094", "SP094605", "SP067422", "SP031288", "SP015370", "SP042812",
            "SP078245", "SP066085", "SP019514", "SP013388", "SP036521", "SP042072",
            "SP001168", "SP080054", "SP039296", "SP092532", "SP088405", "SP009395",
            "SP077742", "SP086242", "SP018413", "SP056448", "SP018247", "SP065308",
            "SP089153", "SP078782", "SP079476", "SP082915", "SP031294", "SP032482",
            "SP054542", "SP036299", "SP060538", "SP007859", "SP096569", "SP090264",
            "SP079613", "SP092938", "SP081636", "SP094661"
        )
    }
}
