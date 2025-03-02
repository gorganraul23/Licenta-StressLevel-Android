package book.kotlinforandroid.hr.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import book.kotlinforandroid.hr.R
import book.kotlinforandroid.hr.Utils
import book.kotlinforandroid.hr.Utils.isIBINormal
import book.kotlinforandroid.hr.databinding.ActivityDetailsBinding
import book.kotlinforandroid.hr.model.HeartRateData
import book.kotlinforandroid.hr.model.HeartRateStatus
import book.kotlinforandroid.hr.model.PpgData
import book.kotlinforandroid.hr.tracker.TrackerDataNotifier
import book.kotlinforandroid.hr.tracker.TrackerDataObserver

class DetailsActivity : Activity() {

    private val appTAG = "DetailsActivity"
    private lateinit var binding: ActivityDetailsBinding

    private var status = 0
    private var heartRateDataLast = HeartRateData()
    private var hrvLastValid = 0.0
    private var hrvLastInvalid = 0.0
    private var resetSignal = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        binding.txtValueHRVValid.text = getString(R.string.HRVDefaultValue)
        binding.txtValueHRVInvalid.text = getString(R.string.HRVDefaultValue)

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
        val ibiOld = intent.getIntExtra(getString(R.string.ExtraIbiOld), 0)
        val ibi = intent.getIntegerArrayListExtra(getString(R.string.ExtraIbi)) ?: arrayListOf()
        val qIbi = intent.getIntegerArrayListExtra(getString(R.string.ExtraQualityIbi)) ?: arrayListOf()

        val hrData = HeartRateData(status, hr, ibiOld, ibi, qIbi)
        heartRateDataLast = HeartRateData(status, hr, ibiOld, ibi, qIbi)
        updateUi(hrData)
        updateHRV()

        TrackerDataNotifier.getInstance().addObserver(trackerDataObserver)
    }


    private val trackerDataObserver: TrackerDataObserver = object : TrackerDataObserver {
        override fun onHeartRateTrackerDataChanged(heartRateData: HeartRateData) {
            runOnUiThread {
                if(heartRateData.ibiList.isNotEmpty()) {
                    heartRateData.ibiList.forEach { ibiValue ->
                        Utils.updateIbiListWithInvalid(ibiValue)
                    } // stores all IBIs (valid + invalid)

                    heartRateData.ibiList.forEachIndexed { index, ibiValue ->
                        if (heartRateData.qIbiList.getOrNull(index) == 0) {
                            Utils.updateIbiList(ibiValue)
                        }
                    }
                    updateUi(heartRateData)
                    updateHRV()
                }

            }
        }

        override fun onPpgGreenTrackerDataChanged(ppgGreenData: PpgData) {
        }

        override fun onPpgRedTrackerDataChanged(ppgRedData: PpgData) {
        }

        override fun onError(errorID: Int) {
            runOnUiThread {
                Toast.makeText(applicationContext, getString(errorID), Toast.LENGTH_LONG).show()
            }
        }
    }

    fun updateHRV() {
        hrvLastValid = Utils.calculateHRV()
        hrvLastInvalid = Utils.calculateHRVWithInvalid()
        val formattedValidNumber = String.format("%.3f", hrvLastValid)
        val formattedInvalidNumber = String.format("%.3f", hrvLastInvalid)
        binding.txtValueHRVValid.text = formattedValidNumber
        binding.txtValueHRVInvalid.text = formattedInvalidNumber
    }

    @SuppressLint("SetTextI18n")
    private fun updateUi(hrData: HeartRateData) {
        Log.d(appTAG, "Details: $hrData")
        binding.txtHeartRateStatus.text = hrData.hrStatus.toString()
        setStatus(hrData.hrStatus)
        if (hrData.hrStatus == HeartRateStatus.HR_STATUS_FIND_HR.status || hrData.hrStatus == HeartRateStatus.HR_STATUS_CALCULATING.status) {
            binding.txtHeartRate.text = hrData.hr.toString()
            binding.txtHeartRateStatus.setTextColor(Color.WHITE)

            //binding.txtIbi.text = hrData.ibiList.get(0).toString()
            if(hrData.ibiList.isNotEmpty())
                binding.txtIbi.text = hrData.ibiList.joinToString(", ")
            else
                binding.txtIbi.text = "0"

            //binding.txtIbiStatus.text = hrData.qIbiList[0].toString()
            if(hrData.qIbiList.isNotEmpty()) {
                binding.txtIbiStatus.text = hrData.qIbiList.joinToString(", ")
                binding.txtIbiStatus.setTextColor(if (hrData.qIbiList[0] == 0) Color.WHITE else Color.RED)
            }
            else
                binding.txtIbiStatus.text = "0"
        } else if (!resetSignal) {
            binding.txtHeartRate.text = heartRateDataLast.hr.toString()
            binding.txtHeartRateStatus.setTextColor(Color.RED)
            //binding.txtIbi.text = heartRateDataLast.ibiList[0].toString()
            if(hrData.ibiList.isNotEmpty())
                binding.txtIbi.text = hrData.ibiList.joinToString(", ")
            else
                binding.txtIbi.text = "0"
            binding.txtIbiStatus.text = getString(R.string.IbiStatusDefaultValue)
            binding.txtIbiStatus.setTextColor(Color.RED)
            binding.txtValueHRVValid.text = hrvLastValid.toString()
            binding.txtValueHRVInvalid.text = hrvLastInvalid.toString()
        } else {
            binding.txtHeartRate.text = getString(R.string.HeartRateDefaultValue)
            binding.txtHeartRateStatus.setTextColor(Color.RED)
            binding.txtIbi.text = getString(R.string.IbiDefaultValue)
            binding.txtIbiStatus.text = getString(R.string.IbiStatusDefaultValue)
            binding.txtIbiStatus.setTextColor(Color.RED)
            binding.txtValueHRVValid.text = hrvLastValid.toString()
            binding.txtValueHRVInvalid.text = hrvLastInvalid.toString()
        }
    }

    private fun setStatus(status: Int) {
        Log.i(appTAG, "HR Status: $status")
        var stringId = R.string.DetailsStatusRunning
        when (status) {
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
