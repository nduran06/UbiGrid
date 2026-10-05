package eci.smartcity.ubigrid.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eci.smartcity.ubigrid.model.GeoLocation;
import eci.smartcity.ubigrid.model.traffic.TrafficData;
import eci.smartcity.ubigrid.repository.traffic.TrafficDataRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Stand-in for the city's traffic sensor network. A fixed set of sensors on
 * real Bogotá corridors publishes a reading (vehicle count, average speed,
 * congestion level) every few seconds, following a rush-hour profile plus
 * per-sensor noise and occasional incidents. Readings are kept as an
 * in-memory snapshot, which TrafficServiceImpl uses to price route segments,
 * and persisted to the "traffic_data" collection as a real ingestion
 * pipeline would. Everything here is fake data.
 *
 * The first snapshot is generated at startup without touching MongoDB, so
 * routing works even when the database is unreachable. Periodic publishing is
 * disabled with ubigrid.traffic.feed.enabled=false (the snapshot then stays
 * static), which UbigridApplicationTests uses to avoid a live MongoDB.
 */
@Component
public class TrafficFeedSimulator {

    private static final Logger logger = LoggerFactory.getLogger(TrafficFeedSimulator.class);
    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    private static final double FREE_FLOW_KMH = 58.0;
    private static final int INCIDENT_TICKS = 8;

    private record Sensor(String id, String name, double lat, double lon, double weight, double bias) {
    }

    /** Mutable per-sensor state evolving between ticks. */
    private static final class SensorState {
        double drift;
        int incidentTicksLeft;
    }

    private static final List<Sensor> SENSORS = List.of(
            new Sensor("sns-001", "Autopista Norte con Calle 127", 4.7180, -74.0450, 1.0, 0.05),
            new Sensor("sns-002", "Av. Boyacá con Calle 80", 4.7160, -74.0990, 1.1, 0.00),
            new Sensor("sns-003", "Av. Caracas con Calle 72", 4.6580, -74.0610, 1.3, 0.40),
            new Sensor("sns-004", "Carrera 7 con Calle 100", 4.6860, -74.0340, 1.0, 0.10),
            new Sensor("sns-005", "Av. Suba con Calle 127", 4.7120, -74.0720, 0.9, 0.00),
            new Sensor("sns-006", "NQS con Calle 80", 4.6900, -74.0930, 1.3, 0.40),
            new Sensor("sns-007", "Av. El Dorado con Carrera 68", 4.6540, -74.1050, 1.1, 0.05),
            new Sensor("sns-008", "Calle 26 con Carrera 30", 4.6490, -74.0820, 1.1, 0.15),
            new Sensor("sns-009", "Av. Ciudad de Cali con Calle 80", 4.7120, -74.1130, 0.9, 0.00),
            new Sensor("sns-010", "Carrera 68 con Calle 100", 4.6980, -74.0780, 1.0, 0.10),
            new Sensor("sns-011", "Autopista Norte con Calle 170", 4.7480, -74.0460, 0.9, 0.00),
            new Sensor("sns-012", "Calle 134 con Carrera 19", 4.7260, -74.0540, 0.9, 0.05),
            new Sensor("sns-013", "Av. Américas con Carrera 68", 4.6350, -74.1170, 1.0, 0.05),
            new Sensor("sns-014", "Av. Suba con Av. Ciudad de Cali", 4.7390, -74.0880, 0.9, 0.00));

    private final TrafficDataRepository trafficDataRepository;
    private final boolean enabled;
    private final int intervalSeconds;
    private final Random random = new Random();
    private final Map<String, SensorState> states = new HashMap<>();
    private ScheduledExecutorService scheduler;

    private volatile List<TrafficData> latestReadings = List.of();

    public TrafficFeedSimulator(TrafficDataRepository trafficDataRepository,
            @Value("${ubigrid.traffic.feed.enabled:true}") boolean enabled,
            @Value("${ubigrid.traffic.feed.interval-seconds:10}") int intervalSeconds) {
        this.trafficDataRepository = trafficDataRepository;
        this.enabled = enabled;
        this.intervalSeconds = intervalSeconds;
        SENSORS.forEach(sensor -> states.put(sensor.id(), new SensorState()));
    }

    @PostConstruct
    void start() {
        latestReadings = generateReadings();
        if (!enabled) {
            return;
        }
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "traffic-feed");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(this::publish, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
        logger.info("Traffic feed started: {} sensors publishing every {}s", SENSORS.size(), intervalSeconds);
    }

    @PreDestroy
    void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    /** Latest reading of every sensor; never empty. */
    public List<TrafficData> getLatestReadings() {
        return latestReadings;
    }

    private void publish() {
        try {
            List<TrafficData> readings = generateReadings();
            latestReadings = readings;

            long incidents = readings.stream().filter(r -> "true".equals(r.getMetadata().get("incident"))).count();
            double avgCongestion = readings.stream().mapToDouble(TrafficData::getCongestionLevel).average().orElse(0);
            logger.info("Traffic feed: received {} sensor readings (avg congestion {}, {} active incidents)",
                    readings.size(), String.format("%.2f", avgCongestion), incidents);

            trafficDataRepository.saveAll(readings);
        } catch (Exception e) {
            logger.warn("Traffic feed: could not persist readings: {}", e.getMessage());
        }
    }

    private synchronized List<TrafficData> generateReadings() {
        LocalDateTime now = LocalDateTime.now(BOGOTA);
        double hour = now.getHour() + now.getMinute() / 60.0;
        double profile = rushHourProfile(hour);

        List<TrafficData> readings = new ArrayList<>();
        for (Sensor sensor : SENSORS) {
            SensorState state = states.get(sensor.id());

            state.drift = Math.max(-0.2, Math.min(0.2, state.drift * 0.9 + random.nextGaussian() * 0.04));
            if (state.incidentTicksLeft > 0) {
                state.incidentTicksLeft--;
            } else if (random.nextDouble() < 0.008) {
                state.incidentTicksLeft = INCIDENT_TICKS;
            }
            boolean incident = state.incidentTicksLeft > 0;

            double congestion = profile * sensor.weight() + sensor.bias() + state.drift;
            if (incident) {
                congestion = Math.max(congestion, 0.92);
            }
            congestion = Math.max(0.03, Math.min(1.0, congestion));

            double speed = Math.max(6.0, FREE_FLOW_KMH * (1 - 0.82 * congestion) + random.nextGaussian());
            int vehicles = (int) Math.max(0, 40 + 260 * congestion + random.nextGaussian() * 12);

            Map<String, String> metadata = new HashMap<>();
            metadata.put("sensorName", sensor.name());
            metadata.put("incident", String.valueOf(incident));

            readings.add(TrafficData.builder()
                    .sensorId(sensor.id())
                    .locationId(sensor.id())
                    .location(new GeoLocation(sensor.lon(), sensor.lat()))
                    .timestamp(now)
                    .vehicleCount(vehicles)
                    .averageSpeed(round(speed))
                    .congestionLevel(round(congestion))
                    .vehicleTypes(new HashMap<>())
                    .metadata(metadata)
                    .sourceType("SENSOR")
                    .build());
        }
        return List.copyOf(readings);
    }

    /** Morning and evening peaks plus a small midday bump, never fully empty. */
    private double rushHourProfile(double hour) {
        return 0.12
                + 0.38 * gaussian(hour, 7.5, 1.5)
                + 0.42 * gaussian(hour, 17.75, 1.8)
                + 0.20 * gaussian(hour, 13.0, 2.0);
    }

    private double gaussian(double x, double mean, double width) {
        double z = (x - mean) / width;
        return Math.exp(-z * z);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
