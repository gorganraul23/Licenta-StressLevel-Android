package book.kotlinforandroid.hr

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private var retrofit: Retrofit? = null

    fun getRetrofitInstance(): Retrofit {
        if(retrofit == null){
            retrofit = Retrofit.Builder()
                .baseUrl("http://192.168.1.4:8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }
}