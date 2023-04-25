package book.kotlinforandroid.hr

import book.kotlinforandroid.hr.model.SaveSensorDataRequest
import book.kotlinforandroid.hr.model.StartSessionResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @POST("api/start-session")
    fun startSession(): Call<StartSessionResponse>

    @PUT("api/end-session/{id}")
    fun endSession(@Path("id") id: Int): Call<StartSessionResponse>

    @POST("api/save-sensor-data")
    fun sendSensorData(@Body sensorData: SaveSensorDataRequest): Call<Void>

}
