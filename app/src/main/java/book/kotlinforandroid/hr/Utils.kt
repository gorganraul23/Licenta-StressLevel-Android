package book.kotlinforandroid.hr

import kotlin.math.pow
import kotlin.math.sqrt

object Utils {

    private var ibiList = mutableListOf<Int>()
    var slidingWindowSize = 120
    const val referenceValuesNumber = 30
    var nbOfValues = 0

    var userEmail = ""
    var userId = 0

//    fun calculateHRV(): Double {
//        val nnDifferences = mutableListOf<Double>()
//        if (ibiList.size >= 3) {
//            for (i in 1 until ibiList.size) {
//                nnDifferences.add((ibiList[i] - ibiList[i - 1]).toDouble())
//            }
//            val sumOfSquares = nnDifferences.sumOf { it * it }
//
//            return sqrt(sumOfSquares / (nnDifferences.size - 1))
//        }
//        return 0.0
//    }

    fun getIbiList(): List<Int> {
        return ibiList.toList()
    }

    fun updateIbiList(ibi: Int) {
        if(ibiList.size >= slidingWindowSize){
            ibiList.removeAt(0)
        }
        ibiList.add(ibi)
    }

    fun clearList(){
        ibiList.clear();
    }

    fun setListLastNValues(n: Int){
        ibiList = ibiList.subList(ibiList.size-15, ibiList.size-1)
    }

    fun setEmail(email: String){
        userEmail = email
    }

    fun clearEmail(){
        userEmail = "";
    }

    fun calculateHRV(): Double {
        val squaredDifferences = mutableListOf<Double>()

        // calculate squared differences between adjacent IBIs
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

}
