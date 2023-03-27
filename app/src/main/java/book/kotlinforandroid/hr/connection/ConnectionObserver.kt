package book.kotlinforandroid.hr.connection

import com.samsung.android.service.health.tracking.HealthTrackerException

interface ConnectionObserver {

    fun onConnectionResult(stringResourceId: Int)

    fun onError(e: HealthTrackerException)
}