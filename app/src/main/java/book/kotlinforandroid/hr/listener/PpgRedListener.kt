package book.kotlinforandroid.hr.listener

import android.util.Log
import book.kotlinforandroid.hr.R
import book.kotlinforandroid.hr.tracker.TrackerDataNotifier
import com.samsung.android.service.health.tracking.HealthTracker
import com.samsung.android.service.health.tracking.data.DataPoint

class PpgRedListener : BaseListener() {

    private val appTAG = "PpgRedListener"

    init {
        val trackerEventListener = object : HealthTracker.TrackerEventListener {
            override fun onDataReceived(list: List<DataPoint>) {
                //Log.i(APP_TAG, "Data received")
            }

            override fun onFlushCompleted() {
                Log.i(appTAG, " onFlushCompleted called")
            }

            override fun onError(trackerError: HealthTracker.TrackerError) {
                Log.e(appTAG, " onError called: $trackerError")
                setHandlerRunning(false)
                if (trackerError == HealthTracker.TrackerError.PERMISSION_ERROR) {
                    TrackerDataNotifier.getInstance().notifyError(R.string.NoPermission)
                }
                if (trackerError == HealthTracker.TrackerError.SDK_POLICY_ERROR) {
                    println(trackerError.toString())
                    TrackerDataNotifier.getInstance().notifyError(R.string.SdkPolicyError)
                }
            }
        }
        setTrackerEventListener(trackerEventListener)
    }
}
