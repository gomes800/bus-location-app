package com.gomes800.bus_location_backend.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusLocation {

    @JsonProperty("ordem")
    private String order;
    private String latitude;
    private String longitude;
    @JsonProperty("datahora")
    private String dateTime;
    @JsonProperty("velocidade")
    private String speed;
    @JsonProperty("linha")
    private String line;
    @JsonProperty("datahoraenvio")
    private String dateTimeShipping;
    @JsonProperty("datahoraservidor")
    private String dateTimeServer;

}
