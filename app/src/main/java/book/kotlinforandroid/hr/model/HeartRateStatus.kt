package book.kotlinforandroid.hr.model

enum class HeartRateStatus (val status: Int) {
    HR_STATUS_NONE(0),
    HR_STATUS_FIND_HR(1),
    HR_STATUS_ATTACHED(-1),
    HR_STATUS_DETECT_MOVE(-2),
    HR_STATUS_DETACHED(-3),
    HR_STATUS_LOW_RELIABILITY(-8),
    HR_STATUS_VERY_LOW_RELIABILITY(-10),
    HR_STATUS_NO_DATA_FLUSH(-99),
}