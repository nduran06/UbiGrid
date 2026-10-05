package eci.smartcity.ubigrid.service;

import java.util.List;

import com.google.common.base.Optional;

import eci.smartcity.ubigrid.model.traffic.TrafficIncident;

/**
 * Detects and manages traffic incidents
 */
public interface TrafficIncidentService {

	/**
	 * Create a new traffic incident
	 */
	TrafficIncident createIncident(TrafficIncident incident);

	/**
	 * Update an existing incident
	 */
	Optional<TrafficIncident> updateIncident(String incidentId, TrafficIncident updatedIncident);

	/**
	 * Mark an incident as resolved
	 */
	boolean resolveIncident(String incidentId);

	/**
	 * Get all active incidents, optionally filtered by severity and type
	 */
	List<TrafficIncident> getActiveIncidents(Double minSeverity, String incidentType);

	/**
	 * Get details of a specific incident
	 */
	Optional<TrafficIncident> getIncidentById(String incidentId);

	/**
	 * Get all active incidents for a specific location
	 */
	List<TrafficIncident> getActiveIncidentsByLocation(String locationId);

	/**
	 * Detect incidents from traffic analysis results
	 */
	void detectIncidentsFromAnalysis();
}
