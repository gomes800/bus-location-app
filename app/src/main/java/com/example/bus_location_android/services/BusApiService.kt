package com.example.bus_location_android.services

import android.telecom.Call
import com.example.bus_location_android.domain.BusLocation
import retrofit2.http.GET

interface BusApiService {
    @GET("bus")
    fun getBusLocations(): retrofit2.Call<List<BusLocation>>
}