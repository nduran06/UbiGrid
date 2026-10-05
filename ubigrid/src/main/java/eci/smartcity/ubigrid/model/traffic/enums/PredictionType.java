package eci.smartcity.ubigrid.model.traffic.enums;

/**
 * Prediction time horizons
 */
public enum PredictionType {
    SHORT_TERM(30),    // 30 minutes
    MEDIUM_TERM(120),  // 2 hours
    LONG_TERM(1440);   // 24 hours
    
    private final int minutes;
    
    PredictionType(int minutes) {
        this.minutes = minutes;
    }
    
    public int getMinutes() {
        return minutes;
    }
}