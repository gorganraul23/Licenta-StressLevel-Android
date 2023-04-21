package book.kotlinforandroid.hr.listener

import android.util.Log
import book.kotlinforandroid.hr.R
import book.kotlinforandroid.hr.model.HeartRateData
import book.kotlinforandroid.hr.tracker.TrackerDataNotifier
import com.samsung.android.service.health.tracking.HealthTracker
import com.samsung.android.service.health.tracking.data.DataPoint
import com.samsung.android.service.health.tracking.data.ValueKey

class HeartRateListener : BaseListener() {

    private val APP_TAG = "HeartRateListener"

    init {
        val trackerEventListener = object : HealthTracker.TrackerEventListener {
            override fun onDataReceived(list: List<DataPoint>) {
                for (dataPoint in list) {
                    readValuesFromDataPoint(dataPoint)
                }
            }

            override fun onFlushCompleted() {
                Log.i(APP_TAG, " onFlushCompleted called")
            }

            override fun onError(trackerError: HealthTracker.TrackerError) {
                Log.e(APP_TAG, " onError called: $trackerError")
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

        hrData.hrStatus = dataPoint.getValue(ValueKey.HeartRateSet.STATUS)
        hrData.hr = dataPoint.getValue(ValueKey.HeartRateSet.HEART_RATE)

        val hrIbi = dataPoint.getValue(ValueKey.HeartRateSet.HEART_RATE_IBI)
        hrData.qIbi = hrIbi shr HeartRateData.IBI_QUALITY_SHIFT and HeartRateData.IBI_MASK
        hrData.ibi = hrIbi and HeartRateData.IBI_QUALITY_MASK
        TrackerDataNotifier.getInstance().notifyHeartRateTrackerObservers(hrData)
        Log.d(APP_TAG, dataPoint.toString())
    }

}
