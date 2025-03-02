package book.kotlinforandroid.hr.model

data class SaveSensorDataRequest(
    val sessionId: Int,
    val hrv: Double,
    val hrvWithInvalid: Double,
    val hr: Int,
    val ibiOld: Int,
    val ibiList: List<Int>,
    val ibiStatusList: List<Int>
)
