package com.gomes800.bus_location_backend.controllers;

import com.gomes800.bus_location_backend.domain.BusLocation;
import com.gomes800.bus_location_backend.services.BusService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class BusController {

    private final BusService busService;

    public BusController(BusService busService) {
        this.busService = busService;
    }

    @GetMapping("/bus")
    public Mono<List<BusLocation>> getBus(@RequestParam String line) {
        return busService.getBusPerLine(line);
    }
}
