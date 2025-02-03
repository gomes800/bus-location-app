package com.example.bus_location_android.services

import com.example.bus_location_android.domain.BusLocation
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface BusApiService {
    @GET("/bus")
    fun getBusLocations(): Call<List<BusLocation>>

    @POST("bus/select-line")
    fun selectBusLine(@Query("line") line: String): Call<List<BusLocation>>
}