package book.kotlinforandroid.hr.listener

import android.util.Log
import book.kotlinforandroid.hr.R
import book.kotlinforandroid.hr.model.SkinTemperatureData
import book.kotlinforandroid.hr.tracker.TrackerDataNotifier
import com.samsung.android.service.health.tracking.HealthTracker
import com.samsung.android.service.health.tracking.data.DataPoint
import com.samsung.android.service.health.tracking.data.ValueKey

class SkinTemperatureListener : BaseListener() {

    private val appTAG = "SkinTemperatureListener"

    init {
        val trackerEventListener = object : HealthTracker.TrackerEventListener {
            override fun onDataReceived(list: List<DataPoint>) {
                for (dataPoint in list) {
                    val objectTemperature = dataPoint.getValue(ValueKey.SkinTemperatureSet.OBJECT_TEMPERATURE)
                    val ambientTemperature = dataPoint.getValue(ValueKey.SkinTemperatureSet.AMBIENT_TEMPERATURE)
                    val status = dataPoint.getValue(ValueKey.SkinTemperatureSet.STATUS)
                    //println("Skin Temperature - object: ${objectTemperature}, ambient: $ambientTemperature, status: $status");
                    sendTemperatureData(objectTemperature, ambientTemperature, status)
                }

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

    private fun sendTemperatureData(objectTemperature: Float, ambientTemperature: Float, status: Int) {
        val request = SkinTemperatureData(objectTemperature, ambientTemperature, status)
        TrackerDataNotifier.getInstance().notifySkinTemperatureTrackerObservers(request)
    }
}
