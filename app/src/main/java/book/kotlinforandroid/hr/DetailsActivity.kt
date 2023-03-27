package book.kotlinforandroid.hr

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import book.kotlinforandroid.hr.databinding.ActivityDetailsBinding
import book.kotlinforandroid.hr.model.HeartRateData
import book.kotlinforandroid.hr.model.HeartRateStatus
import book.kotlinforandroid.hr.tracker.TrackerDataNotifier
import book.kotlinforandroid.hr.tracker.TrackerDataObserver
import kotlin.Int

class DetailsActivity : Activity() {

    private val APP_TAG = "DetailsActivity"
    private lateinit var binding: ActivityDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        val status = intent.getIntExtra(getString(R.string.ExtraHrStatus), HeartRateStatus.HR_STATUS_NONE.status)
        val hr = intent.getIntExtra(getString(R.string.ExtraHr), 0)
        val ibi = intent.getIntExtra(getString(R.string.ExtraIbi), 0)
        val qIbi = intent.getIntExtra(getString(R.string.ExtraQualityIbi), 1)

        val hrData = HeartRateData(status, hr, ibi, qIbi)
        updateUi(hrData)

        TrackerDataNotifier.getInstance().addObserver(trackerDataObserver)
    }


    private val trackerDataObserver: TrackerDataObserver = object : TrackerDataObserver {
        override fun onHeartRateTrackerDataChanged(heartRateData: HeartRateData) {
            runOnUiThread { updateUi(heartRateData) }
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
        if (hrData.status == HeartRateStatus.HR_STATUS_FIND_HR.status) {
            binding.txtHeartRate.text = hrData.hr.toString()
            binding.txtHeartRateStatus.setTextColor(Color.WHITE)
            binding.txtIbi.text = hrData.ibi.toString()
            binding.txtIbiStatus.text = hrData.qIbi.toString()
            binding.txtIbiStatus.setTextColor(if (hrData.qIbi == 0) Color.WHITE else Color.RED)
            Log.d(APP_TAG,
                "HR : " + hrData.hr.toString() + " HR_IBI : " + hrData.ibi.toString() + "(" + hrData.qIbi.toString() + ") "
            )
        } else {
            binding.txtHeartRate.text = getString(R.string.HeartRateDefaultValue)
            binding.txtHeartRateStatus.setTextColor(Color.RED)
            binding.txtIbi.text = getString(R.string.IbiDefaultValue)
            binding.txtIbiStatus.text = getString(R.string.IbiStatusDefaultValue)
            binding.txtIbiStatus.setTextColor(Color.RED)
        }
    }


    private fun setStatus(status: Int) {
        Log.i(APP_TAG, "HR Status: $status")
        var stringId = R.string.DetailsStatusNone
        when (status) {
            HeartRateStatus.HR_STATUS_FIND_HR.status -> stringId = R.string.DetailsStatusFindHr
            HeartRateStatus.HR_STATUS_NONE.status -> {}
            HeartRateStatus.HR_STATUS_ATTACHED.status -> stringId = R.string.DetailsStatusAttached
            HeartRateStatus.HR_STATUS_DETECT_MOVE.status -> stringId = R.string.DetailsStatusMoveDetection
            HeartRateStatus.HR_STATUS_DETACHED.status -> stringId = R.string.DetailsStatusDetached
            HeartRateStatus.HR_STATUS_LOW_RELIABILITY.status -> stringId =
                R.string.DetailsStatusLowReliability
            HeartRateStatus.HR_STATUS_VERY_LOW_RELIABILITY.status -> stringId =
                R.string.DetailsStatusVeryLowReliability
            HeartRateStatus.HR_STATUS_NO_DATA_FLUSH.status -> stringId = R.string.DetailsStatusNoDataFlush
        }
        binding.txtStatus.text = getString(stringId)
    }

    override fun onDestroy() {
        super.onDestroy()
        TrackerDataNotifier.getInstance().removeObserver(trackerDataObserver)
    }
}