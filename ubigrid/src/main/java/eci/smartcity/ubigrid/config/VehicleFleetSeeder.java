package eci.smartcity.ubigrid.config;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import eci.smartcity.ubigrid.model.GeoLocation;
import eci.smartcity.ubigrid.model.Vehicle;
import eci.smartcity.ubigrid.model.enums.VehicleType;
import eci.smartcity.ubigrid.repository.VehicleRepository;

/**
 * Seeds a small fleet around Bogotá on first startup (empty "vehicles"
 * collection only), so VehicleRouteService.computeOptimalRoute() has a real
 * vehicle with a known location to route from. No road-network data is
 * needed: the routing algorithm only depends on a vehicle's currentLocation.
 *
 * Disabled with ubigrid.seed.vehicles=false so UbigridApplicationTests can
 * load its context without a live MongoDB: unlike the rest of the app's
 * Mongo usage, a CommandLineRunner forces an actual connection at startup.
 */
@Component
@ConditionalOnProperty(prefix = "ubigrid.seed", name = "vehicles", havingValue = "true", matchIfMissing = true)
public class VehicleFleetSeeder implements CommandLineRunner {

    private final VehicleRepository vehicleRepository;

    public VehicleFleetSeeder(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public void run(String... args) {
        if (vehicleRepository.count() > 0) {
            return;
        }

        vehicleRepository.saveAll(List.of(
                vehicle("veh-bus-402", VehicleType.BUS, 4.7210, -74.0621),
                vehicle("veh-patrulla-17", VehicleType.EMERGENCY, 4.6980, -74.0810),
                vehicle("veh-taxi-eco14", VehicleType.CAR, 4.7050, -74.0500),
                vehicle("veh-camion-3", VehicleType.TRUCK, 4.7300, -74.0900)));
    }

    private Vehicle vehicle(String id, VehicleType type, double latitude, double longitude) {
        return Vehicle.builder()
                .vehicleId(id)
                .vehicleType(type)
                .status("AVAILABLE")
                .currentLocation(new GeoLocation(longitude, latitude))
                .currentSpeed(0.0)
                .heading(0.0)
                .lastUpdateTime(LocalDateTime.now())
                .isConnected(true)
                .isEmergencyVehicle(type == VehicleType.EMERGENCY)
                .build();
    }
}
