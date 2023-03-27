package book.kotlinforandroid.hr.listener

import android.os.Handler
import android.util.Log
import com.samsung.android.service.health.tracking.HealthTracker

open class BaseListener {

    private val APP_TAG = "BaseListener"

    private var handler: Handler? = null
    private var healthTracker: HealthTracker? = null
    private var isHandlerRunning = false

    private var trackerEventListener: HealthTracker.TrackerEventListener? = null

    fun setHandler(handler: Handler){
        this.handler = handler
    }

    fun setHealthTracker(healthTracker: HealthTracker){
        this.healthTracker = healthTracker
    }

    fun setHandlerRunning(handlerRunning: Boolean) {
        isHandlerRunning = handlerRunning
    }

    fun setTrackerEventListener(tracker: HealthTracker.TrackerEventListener){
        this.trackerEventListener = tracker
    }


    fun startTracker(){
        Log.i(APP_TAG, "startTracker called")
        Log.d(APP_TAG, "tracker: $healthTracker")
        Log.d(APP_TAG, "eventListener: $trackerEventListener")

        if(!isHandlerRunning){
            handler?.post {
                healthTracker?.setEventListener(trackerEventListener)
                setHandlerRunning(true)
            }
        }
    }

    fun stopTracker(){
        Log.i(APP_TAG, "stopTracker called")
        Log.d(APP_TAG, "tracker: $healthTracker")
        Log.d(APP_TAG, "eventListener: $trackerEventListener")

        if(isHandlerRunning){
            healthTracker?.unsetEventListener()
            setHandlerRunning(false)
            handler?.removeCallbacksAndMessages(null)
        }
    }

}