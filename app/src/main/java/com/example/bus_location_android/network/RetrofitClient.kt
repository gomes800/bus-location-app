package com.example.bus_location_android.network

import com.example.bus_location_android.services.BusApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://192.168.1.158:8080"

    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(40, TimeUnit.SECONDS) // tempo de conexão
        .readTimeout(40, TimeUnit.SECONDS)    // tempo de leitura
        .writeTimeout(40, TimeUnit.SECONDS)   // tempo de escrita
        .build()

    val instance: BusApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BusApiService::class.java)
    }
}