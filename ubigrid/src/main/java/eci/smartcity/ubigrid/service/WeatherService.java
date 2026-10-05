package eci.smartcity.ubigrid.service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Retrieves and processes weather data
 */
public interface WeatherService {

	/**
	 * Get weather forecast for a specific location and time
	 */
	Map<String, Object> getWeatherForecast(Double latitude, Double longitude, LocalDateTime forecastTime);
}