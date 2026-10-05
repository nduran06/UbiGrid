package eci.smartcity.ubigrid.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * GeoLocation for MongoDB GeoJSON support
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeoLocation {
	
	@Builder.Default
    private String type = "Point";
    private double[] coordinates; // [longitude, latitude]
    
    public GeoLocation(double longitude, double latitude) {
        this.coordinates = new double[]{longitude, latitude};
    }
    
    public double getLongitude() {
        return coordinates[0];
    }
    
    public double getLatitude() {
        return coordinates[1];
    }
}
