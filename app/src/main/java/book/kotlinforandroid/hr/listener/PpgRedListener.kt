package book.kotlinforandroid.hr.listener

import android.util.Log
import book.kotlinforandroid.hr.R
import book.kotlinforandroid.hr.model.PpgData
import book.kotlinforandroid.hr.tracker.TrackerDataNotifier
import com.samsung.android.service.health.tracking.HealthTracker
import com.samsung.android.service.health.tracking.data.DataPoint
import com.samsung.android.service.health.tracking.data.ValueKey

class PpgRedListener : BaseListener() {

    private val appTAG = "PpgRedListener"
    private val ppgValues = mutableListOf<Int>()

    init {
        val trackerEventListener = object : HealthTracker.TrackerEventListener {
            override fun onDataReceived(list: List<DataPoint>) {
                println(list.size)
                for (dataPoint in list) {
                    val ppgValue = dataPoint.getValue(ValueKey.PpgRedSet.PPG_RED)
                    ppgValues.add(ppgValue)
                }
                println(ppgValues)
                if (ppgValues.size >= 100) {
                    sendPPGData()
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

    private fun sendPPGData() {
        val request = PpgData(ArrayList(ppgValues))
        TrackerDataNotifier.getInstance().notifyPpgRedTrackerObservers(request)

        ppgValues.clear()
    }
}
