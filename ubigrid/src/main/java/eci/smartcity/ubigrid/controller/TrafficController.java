package eci.smartcity.ubigrid.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eci.smartcity.ubigrid.service.impl.TrafficFeedSimulator;

/**
 * Exposes the latest readings of the (simulated) traffic sensor network, so
 * the front-end can show the data the router is pricing paths with.
 */
@RestController
@RequestMapping("/api/traffic")
public class TrafficController {

    private final TrafficFeedSimulator trafficFeed;

    public TrafficController(TrafficFeedSimulator trafficFeed) {
        this.trafficFeed = trafficFeed;
    }

    @GetMapping("/sensors")
    public List<TrafficSensorDto> sensors() {
        return trafficFeed.getLatestReadings().stream()
                .map(reading -> new TrafficSensorDto(
                        reading.getSensorId(),
                        reading.getMetadata().get("sensorName"),
                        reading.getLocation().getLatitude(),
                        reading.getLocation().getLongitude(),
                        reading.getCongestionLevel(),
                        levelOf(reading.getCongestionLevel()),
                        reading.getAverageSpeed(),
                        reading.getVehicleCount(),
                        "true".equals(reading.getMetadata().get("incident")),
                        reading.getTimestamp().toString()))
                .toList();
    }

    private String levelOf(double congestion) {
        if (congestion < 0.40) {
            return "LIGHT";
        }
        return congestion < 0.70 ? "MODERATE" : "HEAVY";
    }
}
