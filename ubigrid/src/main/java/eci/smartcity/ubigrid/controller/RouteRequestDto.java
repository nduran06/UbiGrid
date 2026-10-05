package eci.smartcity.ubigrid.controller;

import eci.smartcity.ubigrid.model.enums.RoutePreference;

/**
 * Pickup request: where the rider wants to be picked up, and how the route
 * should be optimized. The nearest available vehicle is assigned server-side.
 */
public record RouteRequestDto(double destinationLatitude, double destinationLongitude, RoutePreference routePreference) {

    public RouteRequestDto {
        if (routePreference == null) {
            routePreference = RoutePreference.FASTEST;
        }
    }
}
