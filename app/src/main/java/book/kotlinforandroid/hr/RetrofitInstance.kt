package book.kotlinforandroid.hr

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private var retrofit: Retrofit? = null
    private var baseUrl: String = "http://192.168.1.1:8000/"

    fun getRetrofitInstance(): Retrofit {
        if (retrofit == null) {
            retrofit = createRetrofitInstance()
        }
        return retrofit!!
    }

    fun updateBaseUrl(newIp: String) {
        baseUrl = "http://$newIp:8000/"
        retrofit = createRetrofitInstance()
    }

    private fun createRetrofitInstance(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}