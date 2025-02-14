package com.gomes800.bus_location_backend.services;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gomes800.bus_location_backend.domain.BusLocation;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BusService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final Map<String, List<BusLocation>> cache = new ConcurrentHashMap<>();
    private final Scheduler scheduler = Schedulers.boundedElastic();

    public BusService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://dados.mobilidade.rio")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(100 * 1024 * 1024))
                .build();

        this.objectMapper = new ObjectMapper();
    }

    private Mono<String> getDataFromApi() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd+HH:mm:ss");

        return webClient.get()
                .uri("/gps/sppo?dataInicial={start}&dataFinal={end}",
                        now.minusSeconds(10).format(formatter),
                        now.format(formatter))
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(json -> System.out.println("JSON recebido"))
                .timeout(Duration.ofSeconds(50));
    }

    private Mono<List<BusLocation>> fetchBusLocations(String line) {
        return getDataFromApi()
                .flatMapMany(this::parseJsonStream)
                .filter(bus -> line.equals(bus.getLine()))
                .collectList()
                .map(this::processLatestPositions)
                .onErrorResume(e -> {
                    System.err.println("Erro ao buscar localizações de ônibus: " + e.getMessage());
                    return Mono.empty();
                })
                .cache(Duration.ofSeconds(30))
                .subscribeOn(scheduler);
    }

    private Flux<BusLocation> parseJsonStream(String jsonData) {

        return Flux.create(sink -> {
            try (JsonParser parser = objectMapper.getFactory().createParser(jsonData)) {
                if (parser.nextToken() != JsonToken.START_ARRAY) {
                    sink.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de JSON inválido"));
                    return;
                }

                while (parser.nextToken() == JsonToken.START_OBJECT) {
                    BusLocation bus = objectMapper.readValue(parser, BusLocation.class);
                    normalizeCoordinates(bus);
                    sink.next(bus);
                }
                sink.complete();
            } catch (IOException e) {
                sink.error(new RuntimeException("Erro ao processar JSON", e));
            }
        });
    }

    private void normalizeCoordinates(BusLocation bus) {
        bus.setLatitude(bus.getLatitude().replace(',', '.'));
        bus.setLongitude(bus.getLongitude().replace(',', '.'));
    }

    private List<BusLocation> processLatestPositions(List<BusLocation> buses) {
        return buses.stream()
                .collect(Collectors.toMap(
                        BusLocation::getOrder,
                        Function.identity(),
                        (existing, replacement) ->
                                Long.parseLong(existing.getDateTime()) > Long.parseLong(replacement.getDateTime())
                                        ? existing : replacement
                ))
                .values()
                .stream()
                .collect(Collectors.toList());
    }

    @Scheduled(fixedRate = 30_000)
    public void updateAllLinesCache() {
    }

    public Mono<List<BusLocation>> getBusLocations(String line, boolean forceRefresh) {
        if(forceRefresh) {
            cache.remove(line);
        }
        return Mono.justOrEmpty(cache.get(line))
                .switchIfEmpty(fetchBusLocations(line)
                        .doOnNext(list -> cache.put(line, list))
                );
    }

    public Mono<List<String>> getAvailableLines() {
        return getDataFromApi()
                .flatMapMany(this::parseJsonStream)
                .map(BusLocation::getLine)
                .distinct()
                .collectList();
    }
}
