package book.kotlinforandroid.hr.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import book.kotlinforandroid.hr.ApiService
import book.kotlinforandroid.hr.R
import book.kotlinforandroid.hr.RetrofitInstance
import book.kotlinforandroid.hr.Utils
import book.kotlinforandroid.hr.databinding.ActivityConfigBinding
import book.kotlinforandroid.hr.model.PingResponse
import org.w3c.dom.Document
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileNotFoundException
import java.net.InetAddress
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class ConfigActivity : Activity() {

    private val appTAG = "ConfigActivity"
    private lateinit var binding: ActivityConfigBinding
    private lateinit var ipAddressInput: EditText
    private lateinit var continueBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Utils.clearEmail()
        Utils.userId = 0

        // finding the Continue button
        continueBtn = findViewById(R.id.btnContinue)
        // finding the edit text ipAddress
        ipAddressInput = findViewById(R.id.IpInput)

        // Setting On Click Listener
        continueBtn.setOnClickListener {
            val ipAddress = ipAddressInput.text.toString()
            Utils.ipAddress = ipAddress
            Log.i(appTAG, "IP:" + Utils.ipAddress)
            checkIpReachable(Utils.ipAddress)
        }
    }

    private fun checkIpReachable(ip: String) {
        val tempApiService = Retrofit.Builder()
            .baseUrl("http://$ip:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)

        tempApiService.pingIpAddress().enqueue(object : Callback<PingResponse> {
            override fun onResponse(call: Call<PingResponse>, response: Response<PingResponse>) {
                Log.i(appTAG, response.message())

                if (response.body()?.reachable == true) {
                    Toast.makeText(this@ConfigActivity, "IP Address Found", Toast.LENGTH_SHORT).show()
                    RetrofitInstance.updateBaseUrl(ip)
                    val intent = Intent(this@ConfigActivity, LoginActivity::class.java)
                    startActivity(intent)
                }
            }

            override fun onFailure(call: Call<PingResponse>, t: Throwable) {
                Log.e(appTAG, t.message.toString())
                Toast.makeText(this@ConfigActivity, "Invalid IP Address", Toast.LENGTH_SHORT).show()
                RetrofitInstance.updateBaseUrl("192.168.1.1")
            }
        })
    }
}