package book.kotlinforandroid.hr.model

data class SavePpgGreenDataRequest(
    val sessionId: Int,
    val ppgValues: List<Int>
)
