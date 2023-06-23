package book.kotlinforandroid.hr.activities

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import book.kotlinforandroid.hr.R
import book.kotlinforandroid.hr.Utils
import book.kotlinforandroid.hr.databinding.ActivityDetailsBinding
import book.kotlinforandroid.hr.model.HeartRateData
import book.kotlinforandroid.hr.model.HeartRateStatus
import book.kotlinforandroid.hr.tracker.TrackerDataNotifier
import book.kotlinforandroid.hr.tracker.TrackerDataObserver

class DetailsActivity : Activity() {

    private val APP_TAG = "DetailsActivity"
    private lateinit var binding: ActivityDetailsBinding

    private var status = 0
    private var heartRateDataLast = HeartRateData()
    private var hrvLast = 0.0
    private var resetSignal = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.txtValueHRV.text = getString(R.string.HRVDefaultValue)

        //// get data
        val intent = intent
        val isMeasuring = intent.getBooleanExtra(getString(R.string.ExtraIsMeasuring), false)
        val startedOnce = intent.getBooleanExtra(getString(R.string.ExtraStarted), false)
        resetSignal = intent.getBooleanExtra(getString(R.string.ExtraReset), false)

        ////// set status
        status = if (!startedOnce || resetSignal)
            HeartRateStatus.HR_STATUS_NOT_STARTED.status
        else {
            if (!isMeasuring)
                HeartRateStatus.HR_STATUS_STOPPED.status
            else
                HeartRateStatus.HR_STATUS_CALCULATING.status
        }

        //// get values
        val hr = intent.getIntExtra(getString(R.string.ExtraHr), 0)
        val ibi = intent.getIntExtra(getString(R.string.ExtraIbi), 0)
        val qIbi = intent.getIntExtra(getString(R.string.ExtraQualityIbi), 1)

        val hrData = HeartRateData(status, hr, ibi, qIbi)
        heartRateDataLast = HeartRateData(status, hr, ibi, qIbi)
        updateUi(hrData)
        updateHRV()

        TrackerDataNotifier.getInstance().addObserver(trackerDataObserver)
    }


    private val trackerDataObserver: TrackerDataObserver = object : TrackerDataObserver {
        override fun onHeartRateTrackerDataChanged(heartRateData: HeartRateData) {
            runOnUiThread {
                updateIbiList(heartRateData.ibi, heartRateData.qIbi, heartRateData.hrStatus)
                updateUi(heartRateData)
                updateHRV()
            }
        }

        override fun onError(errorID: Int) {
            runOnUiThread {
                Toast.makeText(applicationContext, getString(errorID), Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    fun updateIbiList(ibiValue: Int, ibiQualityStatus: Int, hrStatus: Int) {
        if (hrStatus == 1 && ibiQualityStatus == 0 && ibiValue != 0) {
            Utils.updateIbiList(ibiValue)
        }
    }

    fun updateHRV() {
        val rmssd = Utils.calculateHRV()
        hrvLast = rmssd
        val formattedNumber = String.format("%.3f", rmssd)
        binding.txtValueHRV.text = formattedNumber
    }

    private fun updateUi(hrData: HeartRateData) {
        binding.txtHeartRateStatus.text = hrData.hrStatus.toString()
        setStatus(hrData.hrStatus)
        if (hrData.hrStatus == HeartRateStatus.HR_STATUS_FIND_HR.status || hrData.hrStatus == HeartRateStatus.HR_STATUS_CALCULATING.status) {
            binding.txtHeartRate.text = hrData.hr.toString()
            binding.txtHeartRateStatus.setTextColor(Color.WHITE)
            binding.txtIbi.text = hrData.ibi.toString()
            binding.txtIbiStatus.text = hrData.qIbi.toString()
            binding.txtIbiStatus.setTextColor(if (hrData.qIbi == 0) Color.WHITE else Color.RED)
            Log.d(
                APP_TAG,
                "HR: " + hrData.hr.toString() + " HR_IBI: " + hrData.ibi.toString() + " (" + hrData.qIbi.toString() + ") "
            )
        } else if (!resetSignal) {
            binding.txtHeartRate.text = heartRateDataLast.hr.toString()
            binding.txtHeartRateStatus.setTextColor(Color.RED)
            binding.txtIbi.text = heartRateDataLast.ibi.toString()
            binding.txtIbiStatus.text = getString(R.string.IbiStatusDefaultValue)
            binding.txtIbiStatus.setTextColor(Color.RED)
            binding.txtValueHRV.text = hrvLast.toString()
        } else {
            binding.txtHeartRate.text = getString(R.string.HeartRateDefaultValue)
            binding.txtHeartRateStatus.setTextColor(Color.RED)
            binding.txtIbi.text = getString(R.string.IbiDefaultValue)
            binding.txtIbiStatus.text = getString(R.string.IbiStatusDefaultValue)
            binding.txtIbiStatus.setTextColor(Color.RED)
            binding.txtValueHRV.text = hrvLast.toString()
        }
    }


    private fun setStatus(status: Int) {
        Log.i(APP_TAG, "HR Status: $status")
        var stringId = R.string.DetailsStatusRunning
        when (status) {
//            HeartRateStatus.HR_STATUS_FIND_HR.status -> stringId = R.string.DetailsStatusRunning
//            HeartRateStatus.HR_STATUS_NONE.status -> {}
            HeartRateStatus.HR_STATUS_STOPPED.status -> stringId = R.string.DetailsStatusStopped
            HeartRateStatus.HR_STATUS_CALCULATING.status -> stringId = R.string.DetailsStatusRunning
            HeartRateStatus.HR_STATUS_NOT_STARTED.status -> stringId =
                R.string.DetailsStatusNotStarted
            HeartRateStatus.HR_STATUS_ATTACHED.status -> stringId = R.string.DetailsStatusAttached
            HeartRateStatus.HR_STATUS_DETECT_MOVE.status -> stringId =
                R.string.DetailsStatusMoveDetection
            HeartRateStatus.HR_STATUS_DETACHED.status -> stringId = R.string.DetailsStatusDetached
            HeartRateStatus.HR_STATUS_LOW_RELIABILITY.status -> stringId =
                R.string.DetailsStatusLowReliability
            HeartRateStatus.HR_STATUS_VERY_LOW_RELIABILITY.status -> stringId =
                R.string.DetailsStatusVeryLowReliability
            HeartRateStatus.HR_STATUS_NO_DATA_FLUSH.status -> stringId =
                R.string.DetailsStatusNoDataFlush
        }
        binding.txtStatus.text = getString(stringId)
    }

    override fun onDestroy() {
        super.onDestroy()
        TrackerDataNotifier.getInstance().removeObserver(trackerDataObserver)
    }

}
