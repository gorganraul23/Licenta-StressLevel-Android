package book.kotlinforandroid.hr.tracker

import book.kotlinforandroid.hr.model.HeartRateData

interface TrackerDataObserver {

    fun onHeartRateTrackerDataChanged(heartRateData: HeartRateData)

    fun onError(errorID: Int)
}