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
    private var pollingJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private lateinit var selectedLine: String
    private val updateInterval = 30000L

    private var isLineSelected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        selectedLine = intent.getStringExtra("SELECTED_LINE") ?: ""

        if (selectedLine.isBlank()) {
            Toast.makeText(this, "Erro: Nenhuma linha foi selecionada!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as? SupportMapFragment

        if (mapFragment == null) {
            Toast.makeText(this, "Erro ao carregar o mapa!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

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
            if (!isLineSelected) {
                selectBusLine()
                isLineSelected = true
            }

            while (isActive) {
                fetchBusLocations()
                delay(updateInterval)
            }
        }
    }

    private fun selectBusLine() {
        if (selectedLine.isBlank()) {
            Toast.makeText(this, "Erro: Nenhuma linha selecionada.", Toast.LENGTH_LONG).show()
            return
        }

        RetrofitClient.instance.selectBusLine(selectedLine).enqueue(object : Callback<List<BusLocation>> {
            override fun onResponse(call: Call<List<BusLocation>>, response: Response<List<BusLocation>>) {
                if (response.isSuccessful) {
                    val busList = response.body()
                    if (busList.isNullOrEmpty()) {
                        Toast.makeText(this@MapsActivity, "Nenhum ônibus disponível para esta linha.", Toast.LENGTH_LONG).show()
                        googleMap.clear()
                        return
                    }
                    updateMap(busList)
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Erro desconhecido"
                    Toast.makeText(this@MapsActivity, "Erro na API: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<List<BusLocation>>, t: Throwable) {
                Toast.makeText(this@MapsActivity, "Falha na conexão: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun fetchBusLocations() {
        if (selectedLine.isBlank()) {
            Toast.makeText(this, "Erro: Nenhuma linha selecionada.", Toast.LENGTH_LONG).show()
            return
        }

        RetrofitClient.instance.getBusLocations().enqueue(object : Callback<List<BusLocation>> {
            override fun onResponse(call: Call<List<BusLocation>>, response: Response<List<BusLocation>>) {
                if (response.isSuccessful) {
                    val busList = response.body()
                    if (busList.isNullOrEmpty()) {
                        Toast.makeText(this@MapsActivity, "Nenhum ônibus disponível para esta linha.", Toast.LENGTH_LONG).show()
                        googleMap.clear()
                        return
                    }
                    updateMap(busList)
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Erro desconhecido"
                    Toast.makeText(this@MapsActivity, "Erro na API: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<List<BusLocation>>, t: Throwable) {
                Toast.makeText(this@MapsActivity, "Falha na conexão: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }


    private fun updateMap(busLocations: List<BusLocation>) {
        googleMap.clear()
        busLocations.forEach { bus ->
            try {
                val lat = bus.latitude.replace(",", ".").toDoubleOrNull()
                val lng = bus.longitude.replace(",", ".").toDoubleOrNull()

                if (lat == null || lng == null) {
                    Toast.makeText(this, "Erro: Coordenadas inválidas para o ônibus ${bus.line}", Toast.LENGTH_SHORT).show()
                    return@forEach
                }

                val position = LatLng(lat, lng)
                googleMap.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title("Ônibus ${bus.line}")
                )
            } catch (e: Exception) {
                Toast.makeText(this, "Erro ao processar localização do ônibus.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}