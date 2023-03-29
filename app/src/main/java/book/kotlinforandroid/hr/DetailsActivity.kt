package book.kotlinforandroid.hr

import android.app.Activity
import android.graphics.Color
import android.os.Build.VERSION_CODES.N
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import book.kotlinforandroid.hr.databinding.ActivityDetailsBinding
import book.kotlinforandroid.hr.model.HeartRateData
import book.kotlinforandroid.hr.model.HeartRateStatus
import book.kotlinforandroid.hr.tracker.TrackerDataNotifier
import book.kotlinforandroid.hr.tracker.TrackerDataObserver
import kotlin.Int
import kotlin.math.sqrt

class DetailsActivity : Activity() {

    private val APP_TAG = "DetailsActivity"
    private lateinit var binding: ActivityDetailsBinding

    private val valuesIBI = listOf<Int>().toMutableList()
    private var time = 0
    private var status = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.txtValueHRV.text = "0.0"

        //// get data
        val intent = intent
        val isMeasuring = intent.getBooleanExtra(getString(R.string.ExtraIsMeasuring), false)
        val startedOnce = intent.getBooleanExtra(getString(R.string.ExtraStarted), false)

        ////// check status
        status = if(!startedOnce)
            HeartRateStatus.HR_STATUS_NOT_STARTED.status
        else{
            if(!isMeasuring)
                HeartRateStatus.HR_STATUS_STOPPED.status
            else
                HeartRateStatus.HR_STATUS_CALCULATING.status
        }

        //// get values
        val hr = intent.getIntExtra(getString(R.string.ExtraHr), 0)
        val ibi = intent.getIntExtra(getString(R.string.ExtraIbi), 0)
        val qIbi = intent.getIntExtra(getString(R.string.ExtraQualityIbi), 1)

        val hrData = HeartRateData(status, hr, ibi, qIbi)
        updateUi(hrData)

        TrackerDataNotifier.getInstance().addObserver(trackerDataObserver)
    }


    private val trackerDataObserver: TrackerDataObserver = object : TrackerDataObserver {
        override fun onHeartRateTrackerDataChanged(heartRateData: HeartRateData) {
            runOnUiThread {
                updateValuesHRV(heartRateData.ibi, heartRateData.qIbi, heartRateData.status)
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

    private fun updateUi(hrData: HeartRateData) {
        binding.txtHeartRateStatus.text = hrData.status.toString()
        setStatus(hrData.status)
        if (hrData.status == HeartRateStatus.HR_STATUS_FIND_HR.status || hrData.status == HeartRateStatus.HR_STATUS_CALCULATING.status) {
            binding.txtHeartRate.text = hrData.hr.toString()
            binding.txtHeartRateStatus.setTextColor(Color.WHITE)
            binding.txtIbi.text = hrData.ibi.toString()
            binding.txtIbiStatus.text = hrData.qIbi.toString()
            binding.txtIbiStatus.setTextColor(if (hrData.qIbi == 0) Color.WHITE else Color.RED)
            Log.d(APP_TAG, "HR : " + hrData.hr.toString() + " HR_IBI : " + hrData.ibi.toString() + "(" + hrData.qIbi.toString() + ") ")
        }
        else {
            binding.txtHeartRate.text = getString(R.string.HeartRateDefaultValue)
            binding.txtHeartRateStatus.setTextColor(Color.RED)
            binding.txtIbi.text = getString(R.string.IbiDefaultValue)
            binding.txtIbiStatus.text = getString(R.string.IbiStatusDefaultValue)
            binding.txtIbiStatus.setTextColor(Color.RED)
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
            HeartRateStatus.HR_STATUS_NOT_STARTED.status -> stringId = R.string.DetailsStatusNotStarted
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

    fun updateValuesHRV(ibiValue: Int, ibiQualityStatus: Int, hrStatus: Int) {
        if (hrStatus == 1 && ibiQualityStatus == 0 && ibiValue != 0) {
            valuesIBI.add(ibiValue)
        }
        time++
        binding.txtTimeValue.text = time.toString()
    }

    fun updateHRV() {
        val nnDifferences = mutableListOf<Double>()
        if (valuesIBI.size >= 3) {
            for (i in 1 until valuesIBI.size) {
                nnDifferences.add((valuesIBI[i] - valuesIBI[i - 1]).toDouble())
            }
            val sumOfSquares = nnDifferences.sumOf { it * it }
            val rmssd = sqrt(sumOfSquares / (nnDifferences.size - 1))

            val formattedNumber = String.format("%.3f", rmssd)
            binding.txtValueHRV.text = formattedNumber
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        TrackerDataNotifier.getInstance().removeObserver(trackerDataObserver)
    }
}