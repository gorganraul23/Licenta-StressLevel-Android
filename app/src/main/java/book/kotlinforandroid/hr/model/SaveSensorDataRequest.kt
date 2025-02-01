package book.kotlinforandroid.hr.model

data class SaveSensorDataRequest(
    val sessionId: Int,
    val hrv: Double,
    val hr: Int,
    val ibi: Int,
    val ibiStatus: Int
)
