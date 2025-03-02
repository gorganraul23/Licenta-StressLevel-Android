package book.kotlinforandroid.hr.model

data class SaveSkinTemperatureDataRequest(
    val sessionId: Int,
    val objectTemperature: Float,
    val ambientTemperature: Float,
    val status: Int
)
