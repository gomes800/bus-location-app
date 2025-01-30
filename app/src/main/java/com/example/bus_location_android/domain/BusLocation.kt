package com.example.bus_location_android.domain

import com.fasterxml.jackson.annotation.JsonProperty

data class BusLocation(
    @JsonProperty("ordem")
    val order: String,
    val latitude: String,
    val longitude: String,
    @JsonProperty("datahora")
    val dateTime: String,
    @JsonProperty("velocidade")
    val speed: String,
    @JsonProperty("linha")
    val line: String,
    @JsonProperty("datahoraenvio")
    val dateTimeShipping: String,
    @JsonProperty("datahoraservidor")
    val dateTimeServer: String
)