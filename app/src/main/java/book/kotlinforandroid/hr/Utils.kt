package book.kotlinforandroid.hr

import kotlin.math.sqrt

object Utils {
    var ipAddress = "192.168.1.1"

    private var userEmail = ""
    var userId = 0

    fun setEmail(email: String) {
        userEmail = email
    }

    fun clearEmail() {
        userEmail = ""
    }

    private var ibiList = mutableListOf<Int>()
    private var ibiListWithInvalid = mutableListOf<Int>()
    private var slidingWindowSize = 120
    var nbOfValues = 0
    var nbOfValuesWithInvalid = 0

    fun getIbiList(): List<Int> {
        return ibiList.toList()
    }

    fun getIbiListWithInvalid(): List<Int> {
        return ibiListWithInvalid.toList()
    }

    fun updateIbiList(ibi: Int) {
        if (ibiList.size >= slidingWindowSize) {
            ibiList.removeAt(0)
        }
        ibiList.add(ibi)
    }

    fun updateIbiListWithInvalid(ibi: Int) {
        if (ibiListWithInvalid.size >= slidingWindowSize) {
            ibiListWithInvalid.removeAt(0)
        }
        ibiListWithInvalid.add(ibi)
    }

    fun clearList() {
        ibiList.clear()
    }

    fun clearListWithInvalid() {
        ibiListWithInvalid.clear()
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

    fun calculateHRVWithInvalid(): Double {
        val squaredDifferences = mutableListOf<Double>()

        // calculate squared differences between adjacent IBIs
        if (ibiListWithInvalid.size >= 3) {
            for (i in 1 until ibiListWithInvalid.size) {
                val difference = ibiListWithInvalid[i] - ibiListWithInvalid[i - 1]
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
