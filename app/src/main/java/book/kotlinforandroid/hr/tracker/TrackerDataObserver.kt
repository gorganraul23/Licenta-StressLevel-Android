package book.kotlinforandroid.hr.tracker

import book.kotlinforandroid.hr.model.HeartRateData
import book.kotlinforandroid.hr.model.PpgData
import book.kotlinforandroid.hr.model.SkinTemperatureData

interface TrackerDataObserver {

    fun onHeartRateTrackerDataChanged(heartRateData: HeartRateData)

    fun onPpgGreenTrackerDataChanged(ppgGreenData: PpgData)

    fun onPpgRedTrackerDataChanged(ppgRedData: PpgData)

    fun onPpgIrTrackerDataChanged(ppgIrData: PpgData)

    fun onSkinTemperatureTrackerDataChanged(skinTemperatureData: SkinTemperatureData)

    fun onError(errorID: Int)
}