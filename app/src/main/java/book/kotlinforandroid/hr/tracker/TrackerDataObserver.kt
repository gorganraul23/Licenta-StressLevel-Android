package book.kotlinforandroid.hr.tracker

import book.kotlinforandroid.hr.model.HeartRateData
import book.kotlinforandroid.hr.model.PpgData

interface TrackerDataObserver {

    fun onHeartRateTrackerDataChanged(heartRateData: HeartRateData)

    fun onPpgGreenTrackerDataChanged(ppgGreenData: PpgData)

    fun onError(errorID: Int)
}