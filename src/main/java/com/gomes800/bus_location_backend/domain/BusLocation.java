package com.gomes800.bus_location_backend.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusLocation {

    private String ordem;
    private String latitude;
    private String longitude;
    private String datahora;
    private String velocidade;
    private String linha;
    private String datahoraenvio;
    private String datahoraservidor;
}
