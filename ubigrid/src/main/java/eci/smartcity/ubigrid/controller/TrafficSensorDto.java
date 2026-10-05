package eci.smartcity.ubigrid.controller;

/**
 * Latest reading of one traffic sensor, as shown on the front-end map.
 */
public record TrafficSensorDto(
        String sensorId,
        String name,
        double lat,
        double lng,
        double congestionLevel,
        String trafficLevel,
        double averageSpeedKmh,
        int vehicleCount,
        boolean incident,
        String timestamp) {
}
