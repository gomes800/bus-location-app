package com.gomes800.bus_location_backend.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gomes800.bus_location_backend.domain.BusLocation;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class BusService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private List<BusLocation> cachedBusLocations = List.of();
    private String selectedLine = "";

    public BusService(WebClient.Builder webClientBuilder) {

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(256 * 1024 * 1024))
                .build();
        this.webClient = webClientBuilder
                .baseUrl("https://dados.mobilidade.rio")
                .exchangeStrategies(strategies)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    private Mono<List<BusLocation>> fetchBusLocations(String line) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusSeconds(30);
        LocalDateTime endDate = now;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd+HH:mm:ss");
        String startDateStr = startDate.format(formatter);
        String endDateStr = endDate.format(formatter);

        return webClient.get()
                .uri("/gps/sppo?dataInicial={startDate}&dataFinal={endDate}", startDateStr, endDateStr)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(40))
                .map(data -> filterByLine(data, line));
    }

    private List<BusLocation> filterByLine(String jsonData, String line) {
        try {
            List<BusLocation> busLocationList = objectMapper.readValue(jsonData, new TypeReference<List<BusLocation>>() {});

            return busLocationList.stream()
                    .filter(bus -> line.equals(bus.getLine()))
                    .collect(Collectors.groupingBy((BusLocation::getOrder)))
                    .values() .stream()
                    .map(group -> group.stream()
                            .max((o1, o2) -> Long.compare(Long.parseLong(o1.getDateTime()), Long.parseLong(o2.getDateTime())))
                                    .orElse(null))
                            .filter(Objects::nonNull)
                            .peek(bus -> {
                                bus.setLatitude(bus.getLatitude().replace(",", "."));
                                bus.setLongitude(bus.getLongitude().replace(",", "."));
                            })
                            .collect(Collectors.toList());

        } catch (IOException e) {
            throw new RuntimeException("Erro ao processar JSON", e);
        }
    }

    private void fetchAndUpdateCache() {
        if (selectedLine != null) {
            fetchBusLocations(selectedLine)
                    .doOnNext(busLocations -> this.cachedBusLocations = busLocations)
                    .subscribe();
        }
    }

    @Scheduled(fixedRate = 30000)
    public void updateBusLocations() {
        fetchAndUpdateCache();
    }

    public List<BusLocation> getCachedBusLocations() {
        if (selectedLine == null || selectedLine.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhuma linha foi selecionada!");
        }
        return cachedBusLocations;
    }

    public List<BusLocation> setSelectedLine(String line) {
        if (this.selectedLine.equals(line)) {
            return null;
        }
        this.selectedLine = line;
        this.cachedBusLocations = fetchBusLocations(line).block();
        return cachedBusLocations;
    }


}
