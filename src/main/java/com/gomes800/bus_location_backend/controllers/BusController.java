package com.gomes800.bus_location_backend.controllers;

import com.gomes800.bus_location_backend.domain.BusLocation;
import com.gomes800.bus_location_backend.services.BusService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import java.util.List;

@RestController
@RequestMapping("/bus")
public class BusController {

    private final BusService busService;

    public BusController(BusService busService) {
        this.busService = busService;
    }

    @GetMapping("/locations")
    public Mono<List<BusLocation>> getBusLocations(
            @RequestParam String line,
            @RequestParam(defaultValue = "false") boolean forceRefresh
    ) {
        return busService.getBusLocations(line, forceRefresh)
                .onErrorResume(e -> Mono.error(new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Erro ao buscar localizações"))
                );
    }

    @GetMapping("/lines")
    public Mono<List<String>> getAvailableLines() {
        return busService.getAvailableLines()
                .onErrorReturn(List.of());
    }
}