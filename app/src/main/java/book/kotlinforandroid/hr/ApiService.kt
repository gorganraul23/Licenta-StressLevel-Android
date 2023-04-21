package book.kotlinforandroid.hr

import book.kotlinforandroid.hr.model.HeartRateData
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("api/sensor-data")
    fun sendSensorData(@Body sensorData: HeartRateData): Call<Void>

}
