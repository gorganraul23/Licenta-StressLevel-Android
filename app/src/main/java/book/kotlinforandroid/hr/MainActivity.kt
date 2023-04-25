package book.kotlinforandroid.hr

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import book.kotlinforandroid.hr.connection.ConnectionManager
import book.kotlinforandroid.hr.connection.ConnectionObserver
import book.kotlinforandroid.hr.databinding.ActivityMainBinding
import book.kotlinforandroid.hr.listener.HeartRateListener
import book.kotlinforandroid.hr.model.HeartRateData
import book.kotlinforandroid.hr.model.HeartRateStatus
import book.kotlinforandroid.hr.model.SaveSensorDataRequest
import book.kotlinforandroid.hr.model.StartSessionResponse
import book.kotlinforandroid.hr.tracker.TrackerDataNotifier
import book.kotlinforandroid.hr.tracker.TrackerDataObserver
import com.samsung.android.service.health.tracking.HealthTrackerException
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.String
import java.util.concurrent.TimeUnit

class MainActivity : Activity() {

    private val APP_TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding

    private var permissionGranted = false

    private var connectionManager: ConnectionManager? = null
    private var heartRateListener: HeartRateListener? = null
    private var connected = false
    private var isMeasuring = false
    private var startedOnce = false
    private var resetSignal = false
    private var heartRateDataLast = HeartRateData()
    private var hrvLast = 0.0

    private var sent = false;
    private var session_id = 0;

    private val handler = Handler()
    private lateinit var runnable: Runnable
    private var startTime: Long = 0
    private var elapsedTime: Long = 0

    val retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.1.5:8000/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiService = retrofit.create(ApiService::class.java)


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
                Log.i(APP_TAG, "HR Status: " + heartRateData.hrStatus)

                if (heartRateData.hrStatus == HeartRateStatus.HR_STATUS_FIND_HR.status) {
                    binding.txtHeartRate.text = heartRateData.hr.toString()
                    Log.i(APP_TAG, "HR: " + heartRateData.hr)
                } else {
                    binding.txtHeartRate.text = getString(R.string.HeartRateDefaultValue)
                }

                if (heartRateData.hrStatus == 1 && heartRateData.qIbi == 0 && heartRateData.ibi != 0) {
                    Utils.addIbi(heartRateData.ibi)
                    val rmssd = Utils.calculateHRV()
                    hrvLast = rmssd
                    val formattedNumber = String.format("%.3f", rmssd)
                    binding.txtHRV.text = formattedNumber

                    //// request session id
                    if (!sent) {
                        sent = true
                        apiService.startSession().enqueue(object : Callback<StartSessionResponse> {
                            override fun onResponse(call: Call<StartSessionResponse>, response: Response<StartSessionResponse>) {
                                // handle the response
                                session_id = response.body()!!.session_id
                                println(response.body()?.session_id)
                            }
                            override fun onFailure(call: Call<StartSessionResponse>, t: Throwable) {
                                // handle the failure
                                println(t.message)
                            }
                        })
                    }

                    //// send data
                    val data = SaveSensorDataRequest(session_id, rmssd, heartRateData.hr, heartRateData.ibi)
                    apiService.sendSensorData(data).enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            // handle the response
                            println(response.message())
                        }
                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            // handle the failure
                            println(t.message)
                        }
                    })

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

            /// setting listener
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

        Toast.makeText(applicationContext, getString(R.string.MeasureStart), Toast.LENGTH_LONG)
            .show()
        isMeasuring = true
        startedOnce = true
        resetSignal = false
        heartRateListener?.startTracker()

        binding.butStart.isEnabled = false
        binding.butStop.isEnabled = true
        binding.butReset.isEnabled = false
        binding.txtStatus.text = getString(R.string.StatusStartedOnce)

        startTime = System.currentTimeMillis() - elapsedTime
        runnable = object : Runnable {
            override fun run() {
                elapsedTime = System.currentTimeMillis() - startTime
                val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60
                val timeString = String.format("%02d:%02d", minutes, seconds)
                binding.txtTimeMainValue.text = timeString
                handler.postDelayed(this, 1000)
            }
        }
        handler.postDelayed(runnable, 0)

    }

    //// stop button
    fun stopMeasurement(view: View) {

        Toast.makeText(applicationContext, getString(R.string.MeasureStop), Toast.LENGTH_LONG)
            .show()
        isMeasuring = false
        heartRateListener?.stopTracker()

        binding.butStart.isEnabled = true
        binding.butStop.isEnabled = false
        binding.butReset.isEnabled = true

        handler.removeCallbacks(runnable)
        elapsedTime = System.currentTimeMillis() - startTime
        val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60
        val timeString = String.format("%02d:%02d", minutes, seconds)
        binding.txtTimeMainValue.text = timeString
    }

    /// reset button
    fun resetMeasurement(view: View) {
        startTime = 0
        elapsedTime = 0
        val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60
        val timeString = String.format("%02d:%02d", minutes, seconds)
        binding.txtTimeMainValue.text = timeString
        Utils.clearList()
        binding.txtHRV.text = getString(R.string.HRVDefaultValue)
        binding.txtHeartRate.text = getString(R.string.HeartRateDefaultValue)
        resetSignal = true
        sent = false
        Toast.makeText(applicationContext, getString(R.string.TimerReset), Toast.LENGTH_LONG).show()

        apiService.endSession(session_id).enqueue(object : Callback<StartSessionResponse> {
            override fun onResponse(call: Call<StartSessionResponse>, response: Response<StartSessionResponse>) {
                // handle the response
                println(response.body()?.session_id)
            }
            override fun onFailure(call: Call<StartSessionResponse>, t: Throwable) {
                // handle the failure
                println(t.message)
            }
        })
    }

    //// click on details
    fun goToDetails(view: View) {
        if (isPermissionsOrConnectionInvalid()) {
            return
        }
        val intent = Intent(this, DetailsActivity::class.java)
        intent.putExtra(getString(R.string.ExtraHr), heartRateDataLast.hr)
        intent.putExtra(getString(R.string.ExtraHrStatus), heartRateDataLast.hrStatus)
        intent.putExtra(getString(R.string.ExtraIbi), heartRateDataLast.ibi)
        intent.putExtra(getString(R.string.ExtraQualityIbi), heartRateDataLast.qIbi)
        intent.putExtra(getString(R.string.ExtraIsMeasuring), isMeasuring)
        intent.putExtra(getString(R.string.ExtraStarted), startedOnce)
        intent.putExtra(getString(R.string.ExtraReset), resetSignal)
        startActivity(intent)
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

    override fun onDestroy() {
        super.onDestroy()
        heartRateListener?.stopTracker()
        TrackerDataNotifier.getInstance().removeObserver(trackerDataObserver)
        if (connectionManager != null) {
            connectionManager!!.disconnect()
        }
    }

}