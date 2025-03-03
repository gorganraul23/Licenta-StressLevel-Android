package book.kotlinforandroid.hr.listener

import android.os.Handler
import android.util.Log
import com.samsung.android.service.health.tracking.HealthTracker

open class BaseListener {

    private val appTAG = "BaseListener"

    private var handler: Handler? = null
    private var healthTracker: HealthTracker? = null
    private var isHandlerRunning = false

    private var trackerEventListener: HealthTracker.TrackerEventListener? = null

    fun setHandler(handler: Handler) {
        this.handler = handler
    }

    fun setHealthTracker(healthTracker: HealthTracker) {
        this.healthTracker = healthTracker
    }

    fun setHandlerRunning(handlerRunning: Boolean) {
        isHandlerRunning = handlerRunning
    }

    fun setTrackerEventListener(tracker: HealthTracker.TrackerEventListener) {
        this.trackerEventListener = tracker
    }


    fun startTracker() {
        Log.i(appTAG, "startTracker called")
        Log.d(appTAG, "tracker: $healthTracker")
        Log.d(appTAG, "eventListener: $trackerEventListener")

        if (!isHandlerRunning) {
            handler?.post {
                healthTracker?.setEventListener(trackerEventListener)
                setHandlerRunning(true)
            }
        }
    }

    fun stopTracker() {
        Log.i(appTAG, "stopTracker called")
        Log.d(appTAG, "tracker: $healthTracker")
        Log.d(appTAG, "eventListener: $trackerEventListener")

        if (isHandlerRunning) {
            healthTracker?.unsetEventListener()
            setHandlerRunning(false)
            handler?.removeCallbacksAndMessages(null)
        }
    }

}