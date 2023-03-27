package book.kotlinforandroid.hr.connection

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import book.kotlinforandroid.hr.listener.BaseListener
import book.kotlinforandroid.hr.listener.HeartRateListener
import book.kotlinforandroid.hr.R
import com.samsung.android.service.health.tracking.ConnectionListener
import com.samsung.android.service.health.tracking.HealthTracker
import com.samsung.android.service.health.tracking.HealthTrackerException
import com.samsung.android.service.health.tracking.HealthTrackingService
import com.samsung.android.service.health.tracking.data.HealthTrackerType

class ConnectionManager(observer: ConnectionObserver) {

    private val APP_TAG = "ConnectionManager"
    private var connectionObserver: ConnectionObserver? = observer
    private var healthTrackingService: HealthTrackingService? = null

    private val connectionListener : ConnectionListener = object : ConnectionListener{
        override fun onConnectionSuccess() {
            Log.i(APP_TAG, "Connected")
            connectionObserver?.onConnectionResult(R.string.ConnectedToHs)

            if(!isHeartRateAvailable(healthTrackingService)){
                Log.i(APP_TAG, "Device does not support Heart Rate tracking")
                connectionObserver?.onConnectionResult(R.string.NoHrSupport)
            }
        }

        override fun onConnectionEnded() {
            Log.i(APP_TAG, "Disconnected")
        }

        override fun onConnectionFailed(p0: HealthTrackerException) {
            connectionObserver?.onError(p0)
        }
    }

    fun connect(context: Context){
        healthTrackingService = HealthTrackingService(connectionListener, context)
        healthTrackingService!!.connectService()
    }

    fun disconnect(){
        if(healthTrackingService != null)
            healthTrackingService!!.disconnectService()
    }

    fun initHeartRate(heartRateListener: HeartRateListener){
        val heartRateTracker: HealthTracker = healthTrackingService!!.getHealthTracker(HealthTrackerType.HEART_RATE)
        heartRateListener.setHealthTracker(heartRateTracker)

        setHandlerForBaseListener(heartRateListener)
    }

    private fun setHandlerForBaseListener(baseListener: BaseListener){
        baseListener.setHandler(Handler(Looper.getMainLooper()))
    }

    fun isHeartRateAvailable(healthTrackingService: HealthTrackingService?): Boolean{
        if(healthTrackingService!!.trackingCapability.supportHealthTrackerTypes.contains(HealthTrackerType.HEART_RATE))
            return true
        return false
    }
}