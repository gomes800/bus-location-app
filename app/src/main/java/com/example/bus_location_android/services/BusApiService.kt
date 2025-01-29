package com.example.bus_location_android.services

import com.example.bus_location_android.domain.BusLocation
import retrofit2.Call
import retrofit2.http.GET

interface BusApiService {
    @GET("/bus")
    fun getBusLocations(): Call<List<BusLocation>>
}