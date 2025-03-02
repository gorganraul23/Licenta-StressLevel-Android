package book.kotlinforandroid.hr.listener

import android.util.Log
import book.kotlinforandroid.hr.R
import book.kotlinforandroid.hr.model.HeartRateData
import book.kotlinforandroid.hr.tracker.TrackerDataNotifier
import com.samsung.android.service.health.tracking.HealthTracker
import com.samsung.android.service.health.tracking.data.DataPoint
import com.samsung.android.service.health.tracking.data.ValueKey

class HeartRateListener : BaseListener() {

    private val appTAG = "HeartRateListener"

    init {
        val trackerEventListener = object : HealthTracker.TrackerEventListener {
            override fun onDataReceived(list: List<DataPoint>) {
                for (dataPoint in list) {
                    readValuesFromDataPoint(dataPoint)
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


    fun readValuesFromDataPoint(dataPoint: DataPoint) {
        val hrData = HeartRateData()

        hrData.hrStatus = dataPoint.getValue(ValueKey.HeartRateSet.HEART_RATE_STATUS) // HR status
        hrData.hr = dataPoint.getValue(ValueKey.HeartRateSet.HEART_RATE)  // HR
        var ibiOld = dataPoint.getValue(ValueKey.HeartRateSet.HEART_RATE_IBI)
        ibiOld = ibiOld and HeartRateData.IBI_QUALITY_MASK
        hrData.ibiOld = ibiOld // old ibi

        hrData.ibiList = dataPoint.getValue(ValueKey.HeartRateSet.IBI_LIST) // ibi list
        hrData.qIbiList = dataPoint.getValue(ValueKey.HeartRateSet.IBI_STATUS_LIST) // ibi status list

        Log.i(appTAG, "Listener: ibiOld:${hrData.ibiOld}, IBI List: ${hrData.ibiList}, IBI Status List: ${hrData.qIbiList}")

        // Notify observers
        TrackerDataNotifier.getInstance().notifyHeartRateTrackerObservers(hrData)
    }

}
