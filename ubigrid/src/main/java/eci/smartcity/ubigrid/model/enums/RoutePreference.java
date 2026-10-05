package eci.smartcity.ubigrid.model.enums;

/**
 * Enum representing different route preferences for vehicle routing.
 */
public enum RoutePreference {
	FASTEST, // Prioritize speed, taking the quickest route
	SHORTEST, // Prioritize distance, taking the shortest route
	EFFICIENT, // Balance between speed and fuel efficiency
	ECO_FRIENDLY, // Prioritize fuel efficiency and lower emissions
	AVOID_TOLLS, // Avoid toll roads when possible
	AVOID_HIGHWAYS, // Avoid highways when possible
	SCENIC; // Prefer scenic routes when available

	/**
	 * Gets the weighting factor for distance based on the route preference.
	 * 
	 * @return A weighting factor between 0.0 and 1.0
	 */
	public double getDistanceWeight() {
		switch (this) {
		case SHORTEST:
			return 1.0;
		case EFFICIENT:
			return 0.7;
		case ECO_FRIENDLY:
			return 0.6;
		case FASTEST:
			return 0.3;
		case AVOID_TOLLS:
			return 0.5;
		case AVOID_HIGHWAYS:
			return 0.5;
		case SCENIC:
			return 0.4;
		default:
			return 0.5;
		}
	}

	/**
	 * Gets the weighting factor for time based on the route preference.
	 * 
	 * @return A weighting factor between 0.0 and 1.0
	 */
	public double getTimeWeight() {
		switch (this) {
		case FASTEST:
			return 1.0;
		case EFFICIENT:
			return 0.7;
		case SHORTEST:
			return 0.4;
		case ECO_FRIENDLY:
			return 0.5;
		case AVOID_TOLLS:
			return 0.6;
		case AVOID_HIGHWAYS:
			return 0.3;
		case SCENIC:
			return 0.3;
		default:
			return 0.5;
		}
	}

	/**
	 * Gets the weighting factor for eco-friendliness based on the route preference.
	 * 
	 * @return A weighting factor between 0.0 and 1.0
	 */
	public double getEcoWeight() {
		switch (this) {
		case ECO_FRIENDLY:
			return 1.0;
		case EFFICIENT:
			return 0.8;
		case SCENIC:
			return 0.6;
		case SHORTEST:
			return 0.4;
		case FASTEST:
			return 0.2;
		case AVOID_TOLLS:
			return 0.4;
		case AVOID_HIGHWAYS:
			return 0.7;
		default:
			return 0.5;
		}
	}
}