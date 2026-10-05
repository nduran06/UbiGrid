package eci.smartcity.ubigrid.model.enums;

/**
 * Enum representing different types of notifications in the system.
 */
public enum NotificationType {
    ROUTE_UPDATE("Route Update", "Updates about route changes or traffic"),
    ETA_CHANGE("ETA Change", "Changes to estimated arrival time"),
    VEHICLE_ARRIVAL("Vehicle Arrival", "Notification of vehicle arrival"),
    VEHICLE_DEPARTURE("Vehicle Departure", "Notification of vehicle departure"),
    TRAFFIC_ALERT("Traffic Alert", "Traffic incidents or congestion alerts"),
    WEATHER_ALERT("Weather Alert", "Weather warnings affecting travel"),
    SYSTEM_MAINTENANCE("System Maintenance", "Scheduled system maintenance"),
    ACCOUNT_UPDATE("Account Update", "Updates or changes to user account"),
    BOOKING_CONFIRMATION("Booking Confirmation", "Confirmation of a new booking"),
    BOOKING_CANCELLATION("Booking Cancellation", "Notification of a cancelled booking"),
    PAYMENT_CONFIRMATION("Payment Confirmation", "Confirmation of payment receipt"),
    GENERAL("General", "General system notifications");
    
    private final String displayName;
    private final String description;
    
    NotificationType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Get the category for grouping notifications
     * @return The notification category
     */
    public String getCategory() {
        if (this == ROUTE_UPDATE || this == ETA_CHANGE || 
            this == TRAFFIC_ALERT || this == WEATHER_ALERT) {
            return "TRAVEL";
        } else if (this == VEHICLE_ARRIVAL || this == VEHICLE_DEPARTURE) {
            return "VEHICLE";
        } else if (this == BOOKING_CONFIRMATION || this == BOOKING_CANCELLATION) {
            return "BOOKING";
        } else if (this == PAYMENT_CONFIRMATION) {
            return "PAYMENT";
        } else if (this == ACCOUNT_UPDATE) {
            return "ACCOUNT";
        } else {
            return "SYSTEM";
        }
    }
    
    /**
     * Check if this notification is high priority
     * @return True if high priority, false otherwise
     */
    public boolean isHighPriority() {
        return this == VEHICLE_ARRIVAL || 
               this == TRAFFIC_ALERT || 
               this == WEATHER_ALERT ||
               this == BOOKING_CANCELLATION;
    }
}