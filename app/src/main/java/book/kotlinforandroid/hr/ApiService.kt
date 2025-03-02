package book.kotlinforandroid.hr

import book.kotlinforandroid.hr.model.LoginResponse
import book.kotlinforandroid.hr.model.PingResponse
import book.kotlinforandroid.hr.model.SavePpgDataRequest
import book.kotlinforandroid.hr.model.SaveSensorDataRequest
import book.kotlinforandroid.hr.model.SaveSkinTemperatureDataRequest
import book.kotlinforandroid.hr.model.SetReferenceRequest
import book.kotlinforandroid.hr.model.StartSessionResponse
import book.kotlinforandroid.hr.model.UserCredentials
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @POST("api/start-session/{id}")
    fun startSession(@Path("id") id: Int): Call<StartSessionResponse>

    @PUT("api/set-reference")
    fun setReferenceValue(@Body referenceRequest: SetReferenceRequest): Call<Void>

    @PUT("api/end-session/{id}")
    fun endSession(@Path("id") id: Int): Call<StartSessionResponse>

    @POST("api/save-sensor-data")
    fun sendSensorData(@Body sensorData: SaveSensorDataRequest): Call<Void>

    @POST("api/save-ppg-green-data")
    fun sendPpgGreenData(@Body ppgGreenData: SavePpgDataRequest): Call<Void>

    @POST("api/save-ppg-red-data")
    fun sendPpgRedData(@Body ppgRedData: SavePpgDataRequest): Call<Void>

    @POST("api/save-ppg-ir-data")
    fun sendPpgIrData(@Body ppgRedData: SavePpgDataRequest): Call<Void>

    @POST("api/save-skin-temperature-data")
    fun sendSkinTemperatureData(@Body skinTemperatureData: SaveSkinTemperatureDataRequest): Call<Void>

    @POST("users/login/")
    fun login(@Body credentials: UserCredentials): Call<LoginResponse>

    @GET("api/ping")
    fun pingIpAddress(): Call<PingResponse>
}
