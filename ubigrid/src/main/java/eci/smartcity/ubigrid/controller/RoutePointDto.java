package eci.smartcity.ubigrid.controller;

/**
 * A single waypoint of a computed route, in the order a vehicle would drive through it.
 */
public record RoutePointDto(double lat, double lng) {
}
