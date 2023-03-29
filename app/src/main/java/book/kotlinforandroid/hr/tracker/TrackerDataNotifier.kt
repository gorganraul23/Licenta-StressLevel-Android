package book.kotlinforandroid.hr.tracker

import book.kotlinforandroid.hr.model.HeartRateData
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

    fun notifyError(errorId: Int) {
        observers.forEach(Consumer { observer: TrackerDataObserver ->
            observer.onError(errorId)
        })
    }

}
