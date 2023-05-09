package book.kotlinforandroid.hr

import kotlin.math.sqrt

object Utils {

    private val ibiList = mutableListOf<Int>()
    private const val slidingWindowSize = 120;

    fun calculateHRV(): Double {
        val nnDifferences = mutableListOf<Double>()
        if (ibiList.size >= 3) {
            for (i in 1 until ibiList.size) {
                nnDifferences.add((ibiList[i] - ibiList[i - 1]).toDouble())
            }
            val sumOfSquares = nnDifferences.sumOf { it * it }

            return sqrt(sumOfSquares / (nnDifferences.size - 1))
        }
        return 0.0
    }

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

}
