package book.kotlinforandroid.hr.model

data class SavePpgDataRequest(
    val sessionId: Int,
    val ppgValues: List<Int>
)
