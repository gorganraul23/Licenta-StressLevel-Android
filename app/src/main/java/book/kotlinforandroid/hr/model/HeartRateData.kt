package book.kotlinforandroid.hr.model

class HeartRateData(
    var hrStatus: Int = HeartRateStatus.HR_STATUS_NONE.status,
    var hr: Int = 0,
    var ibiOld: Int = 0,
    var ibiList: List<Int> = emptyList(),
    var qIbiList: List<Int> = emptyList()
) {

    companion object {
        const val IBI_QUALITY_SHIFT = 15
        const val IBI_MASK = 0x1
        const val IBI_QUALITY_MASK = 0x7FFF
    }
}
