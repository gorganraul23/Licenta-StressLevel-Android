package book.kotlinforandroid.hr

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import book.kotlinforandroid.hr.connection.ConnectionManager
import book.kotlinforandroid.hr.connection.ConnectionObserver
import book.kotlinforandroid.hr.databinding.ActivityMainBinding
import book.kotlinforandroid.hr.listener.HeartRateListener
import book.kotlinforandroid.hr.model.HeartRateData
import book.kotlinforandroid.hr.model.HeartRateStatus
import book.kotlinforandroid.hr.tracker.TrackerDataNotifier
import book.kotlinforandroid.hr.tracker.TrackerDataObserver
import com.samsung.android.service.health.tracking.HealthTrackerException
import java.lang.String
import kotlin.Array
import kotlin.Boolean
import kotlin.Int
import kotlin.IntArray
import kotlin.Throwable
import kotlin.arrayOf

class MainActivity : Activity() {

    private val APP_TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding

    private var permissionGranted = false

    private var connectionManager: ConnectionManager? = null
    private var heartRateListener: HeartRateListener? = null
    private var connected = false
    private var isMeasuring = false
    private var startedOnce = false
    private var heartRateDataLast = HeartRateData()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ////// permission for body sensors
        if (ActivityCompat.checkSelfPermission(
                applicationContext, getString(R.string.BodySensors)
            ) == PackageManager.PERMISSION_DENIED
        )
            requestPermissions(arrayOf(Manifest.permission.BODY_SENSORS), 0)
        else {
            permissionGranted = true
            createConnectionManager()
        }
    }

    ///// tracker

    val trackerDataObserver: TrackerDataObserver = object : TrackerDataObserver {
        override fun onHeartRateTrackerDataChanged(heartRateData: HeartRateData) {
            runOnUiThread {
                heartRateDataLast = heartRateData
                Log.i(APP_TAG, "HR Status: " + heartRateData.status)

                if (heartRateData.status == HeartRateStatus.HR_STATUS_FIND_HR.status) {
                    binding.txtHeartRate.text = String.valueOf(heartRateData.hr)
                    Log.i(APP_TAG, "HR: " + heartRateData.hr)
                } else {
                    binding.txtHeartRate.text = getString(R.string.HeartRateDefaultValue)
                }
            }
        }

        override fun onError(errorID: Int) {
            runOnUiThread {
                Toast.makeText(applicationContext, getString(errorID), Toast.LENGTH_LONG).show()
            }
        }
    }


    ////// connection

    private val connectionObserver = object : ConnectionObserver {
        override fun onConnectionResult(stringResourceId: Int) {
            runOnUiThread {
                Toast.makeText(
                    applicationContext,
                    getString(stringResourceId),
                    Toast.LENGTH_LONG
                ).show()
            }

            if (stringResourceId != R.string.ConnectedToHs) {
                finish()
            }

            connected = true

            /// setting listener and starting tracker
            TrackerDataNotifier.getInstance().addObserver(trackerDataObserver)
            heartRateListener = HeartRateListener()
            connectionManager?.initHeartRate(heartRateListener!!)
            //heartRateListener!!.startTracker()
        }

        override fun onError(e: HealthTrackerException) {
            if (e.errorCode == HealthTrackerException.OLD_PLATFORM_VERSION || e.errorCode == HealthTrackerException.PACKAGE_NOT_INSTALLED) {
                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.HealthPlatformVersionIsOutdated),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            if (e.hasResolution()) {
                e.resolve(this@MainActivity)
            } else {
                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.ConnectionError),
                        Toast.LENGTH_LONG
                    ).show()
                }
                Log.e(APP_TAG, "Could not connect to Health Tracking Service: ${e.message}")
            }
            finish()
        }
    }


    private fun createConnectionManager() {
        try {
            connectionManager = ConnectionManager(connectionObserver)
            connectionManager?.connect(applicationContext)
        } catch (t: Throwable) {
            Log.e(APP_TAG, t.message!!)
        }
    }

    //// start button
    fun startMeasurement(view: View) {
        if (isMeasuring)
            Toast.makeText(
                applicationContext,
                getString(R.string.AlreadyStarted),
                Toast.LENGTH_LONG
            )
                .show()
        else {
            Toast.makeText(applicationContext, getString(R.string.MeasureStart), Toast.LENGTH_LONG)
                .show()
            isMeasuring = true
            startedOnce = true
            heartRateListener?.startTracker()
            binding.butStart.background =
                ContextCompat.getDrawable(this, R.drawable.button_disabled_background)
            binding.butStop.background =
                ContextCompat.getDrawable(this, R.drawable.button_background)
            binding.txtStatus.text = getString(R.string.StatusStartedOnce)
        }
    }

    //// stop button
    fun stopMeasurement(view: View) {
        if (!isMeasuring)
            Toast.makeText(
                applicationContext,
                getString(R.string.AlreadyStopped),
                Toast.LENGTH_LONG
            )
                .show()
        else {
            Toast.makeText(applicationContext, getString(R.string.MeasureStop), Toast.LENGTH_LONG)
                .show()
            isMeasuring = false
            heartRateListener?.stopTracker()
            binding.butStart.background =
                ContextCompat.getDrawable(this, R.drawable.button_background)
            binding.butStop.background =
                ContextCompat.getDrawable(this, R.drawable.button_disabled_background)
        }
    }

    //// click on details
    fun goToDetails(view: View) {
        if (isPermissionsOrConnectionInvalid()) {
            return
        }
        val intent = Intent(this, DetailsActivity::class.java)
        intent.putExtra(getString(R.string.ExtraHr), 0)
        intent.putExtra(getString(R.string.ExtraHrStatus), 0)
        intent.putExtra(getString(R.string.ExtraIbi), 0)
        intent.putExtra(getString(R.string.ExtraQualityIbi), 1)
        intent.putExtra(getString(R.string.ExtraIsMeasuring), isMeasuring)
        intent.putExtra(getString(R.string.ExtraStarted), startedOnce)
        startActivity(intent)
    }


    override fun onDestroy() {
        super.onDestroy()
        heartRateListener?.stopTracker()
        TrackerDataNotifier.getInstance().removeObserver(trackerDataObserver)
        if (connectionManager != null) {
            connectionManager!!.disconnect()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out kotlin.String>,
        grantResults: IntArray
    ) {

        if (requestCode == 0) {
            permissionGranted = true
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    if (!shouldShowRequestPermissionRationale(permissions[i]))
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.PermissionDeniedPermanently),
                            Toast.LENGTH_LONG
                        )
                            .show()
                    else
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.PermissionDeniedRationale),
                            Toast.LENGTH_LONG
                        )
                            .show()
                    permissionGranted = false
                    break
                }
            }
            if (permissionGranted) {
                createConnectionManager()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    private fun isPermissionsOrConnectionInvalid(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                getString(R.string.BodySensors)
            ) == PackageManager.PERMISSION_DENIED
        )
            requestPermissions(arrayOf(Manifest.permission.BODY_SENSORS), 0)
        if (!permissionGranted) {
            Log.i(APP_TAG, "Could not get permissions. Terminating measurement")
            return true
        }
        if (!connected) {
            Toast.makeText(
                applicationContext,
                getString(R.string.ConnectionError),
                Toast.LENGTH_SHORT
            )
                .show()
            return true
        }
        return false
    }

}