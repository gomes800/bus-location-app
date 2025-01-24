package com.gomes800.bus_location_backend.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gomes800.bus_location_backend.domain.BusLocation;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BusService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public BusService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://dados.mobilidade.rio").build();
        this.objectMapper = new ObjectMapper();
    }

    public Mono<List<BusLocation>> getBusPerLine(String line) {
        return webClient.get()
                .uri("/gps/sppo?dataInicial=AAAA-MM-DD+HH:MM:SS&dataFinal=AAAA-MM-DD+HH:MM:SS")
                .retrieve()
                .bodyToMono(String.class)
                .map(data -> filterByLine(data, line));
    }

    private List<BusLocation> filterByLine(String jsonData, String line) {
        try {
            List<BusLocation> busLocationList = objectMapper.readValue(jsonData, new TypeReference<List<BusLocation>>() {});

            return busLocationList.stream()
                    .filter(bus -> line.equals(bus.getLine()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Erro ao processar JSON", e);
        }
    }
}
