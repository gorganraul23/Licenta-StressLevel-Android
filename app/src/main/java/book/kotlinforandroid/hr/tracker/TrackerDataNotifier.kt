package book.kotlinforandroid.hr.tracker

import book.kotlinforandroid.hr.model.HeartRateData
import book.kotlinforandroid.hr.model.PpgData
import book.kotlinforandroid.hr.model.SkinTemperatureData
import java.util.function.Consumer

class TrackerDataNotifier private constructor() {

    companion object {

        private lateinit var instance: TrackerDataNotifier

        fun getInstance(): TrackerDataNotifier {
            if (!Companion::instance.isInitialized) {
                instance = TrackerDataNotifier()
            }
            return instance
        }
    }

    private var observers: ArrayList<TrackerDataObserver> = ArrayList()

    fun addObserver(observer: TrackerDataObserver) {
        observers.add(observer)
    }

    fun removeObserver(observer: TrackerDataObserver) {
        observers.remove(observer)
    }

    fun notifyHeartRateTrackerObservers(hrData: HeartRateData?) {
        observers.forEach(Consumer { observer: TrackerDataObserver ->
            observer.onHeartRateTrackerDataChanged(hrData!!)
        })
    }

    fun notifyPpgGreenTrackerObservers(ppgGreenData: PpgData?) {
        observers.forEach(Consumer { observer: TrackerDataObserver ->
            observer.onPpgGreenTrackerDataChanged(ppgGreenData!!)
        })
    }

    fun notifyPpgRedTrackerObservers(ppgRedData: PpgData?) {
        observers.forEach(Consumer { observer: TrackerDataObserver ->
            observer.onPpgRedTrackerDataChanged(ppgRedData!!)
        })
    }

    fun notifyPpgIrTrackerObservers(ppgIrData: PpgData?) {
        observers.forEach(Consumer { observer: TrackerDataObserver ->
            observer.onPpgIrTrackerDataChanged(ppgIrData!!)
        })
    }

    fun notifySkinTemperatureTrackerObservers(skinTemperatureData: SkinTemperatureData?) {
        observers.forEach(Consumer { observer: TrackerDataObserver ->
            observer.onSkinTemperatureTrackerDataChanged(skinTemperatureData!!)
        })
    }

    fun notifyError(errorId: Int) {
        observers.forEach(Consumer { observer: TrackerDataObserver ->
            observer.onError(errorId)
        })
    }

}
