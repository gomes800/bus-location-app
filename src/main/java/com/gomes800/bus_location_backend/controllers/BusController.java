package com.gomes800.bus_location_backend.controllers;

import com.gomes800.bus_location_backend.domain.BusLocation;
import com.gomes800.bus_location_backend.services.BusService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/bus")
public class BusController {

    private final BusService busService;

    public BusController(BusService busService) {
        this.busService = busService;
    }

    @GetMapping
    public List<BusLocation> getBusLocations() {
        return busService.getCachedBusLocations();
    }

    @PostMapping("select-line")
    public List<BusLocation> selectLine(@RequestParam String line) {
        return busService.setSelectedLine(line);
    }

}
