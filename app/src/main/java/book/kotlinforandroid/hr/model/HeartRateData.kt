package book.kotlinforandroid.hr.model

class HeartRateData(
    var hrStatus: Int = HeartRateStatus.HR_STATUS_NONE.status,
    var hr: Int = 0,
    var ibi: Int = 0,
    var qIbi: Int = 1
) {

    companion object {
        const val IBI_QUALITY_SHIFT = 15
        const val IBI_MASK = 0x1
        const val IBI_QUALITY_MASK = 0x7FFF
    }

    fun getHrIbi(): Int {
        return qIbi shl IBI_QUALITY_SHIFT or ibi
    }
}
