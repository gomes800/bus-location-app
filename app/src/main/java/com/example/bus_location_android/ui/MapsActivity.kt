package com.example.bus_location_android.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bus_location_android.R
import com.example.bus_location_android.domain.BusLocation
import com.example.bus_location_android.network.RetrofitClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private val updateInterval = 30000L
    private var pollingJob: Job? = null

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        val rio = LatLng(-22.9068, -43.1729)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(rio, 12f))

        startPolling()
    }

    override fun onPause() {
        super.onPause()
        pollingJob?.cancel()
    }

    private fun startPolling() {
        pollingJob = coroutineScope.launch {
            while (isActive) {
                fetchBusLocations()
                delay(updateInterval)
            }
        }
    }

    private fun fetchBusLocations() {
        RetrofitClient.instance.getBusLocations().enqueue(object : Callback<List<BusLocation>> {
            override fun onResponse(call: Call<List<BusLocation>>, response: Response<List<BusLocation>>) {
                if (response.isSuccessful) {
                    response.body()?.let { updateMap(it) }
                } else {
                    Toast.makeText(this@MapsActivity, "Erro ao carregar os dados", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<List<BusLocation>>, t: Throwable) {
                Toast.makeText(this@MapsActivity, "Erro ao carregar os dados: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun updateMap(busLocations: List<BusLocation>) {
        googleMap.clear()
        busLocations.forEach { bus ->
            val position = LatLng(
                bus.latitude.replace(",", ".").toDouble(),
                bus.longitude.replace(",", ".").toDouble()
            )
            googleMap.addMarker(
                MarkerOptions()
                    .position(position)
                    .title("Onibus ${bus.line}")
            )
        }
    }
}