package book.kotlinforandroid.hr

import kotlin.math.sqrt

object Utils {

    private var ibiList = mutableListOf<Int>()
    var slidingWindowSize = 120
    var nbOfValues = 0

    var userEmail = ""
    var userId = 0

    var ipAddress = "192.168.1.5"

    fun getIbiList(): List<Int> {
        return ibiList.toList()
    }

    fun updateIbiList(ibi: Int) {
        if (ibiList.size >= slidingWindowSize) {
            ibiList.removeAt(0)
        }
        ibiList.add(ibi)
    }

    fun clearList() {
        ibiList.clear();
    }

    fun setEmail(email: String) {
        userEmail = email
    }

    fun clearEmail() {
        userEmail = "";
    }

    fun calculateHRV(): Double {
        val squaredDifferences = mutableListOf<Double>()

        // calculate squared differences between adjacent IBIs
        if (ibiList.size >= 3) {
            for (i in 1 until ibiList.size) {
                val difference = ibiList[i] - ibiList[i - 1]
                val square = difference * difference
                squaredDifferences.add(square.toDouble())
            }
            // calculate mean squared difference
            val meanSquaredDifference = squaredDifferences.average()
            // return square root
            return sqrt(meanSquaredDifference)
        }
        return 0.0
    }

}
