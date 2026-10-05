package eci.smartcity.ubigrid.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import eci.smartcity.ubigrid.model.enums.NotificationType;

import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity representing user notification preferences in the MongoDB database.
 * This class maps to documents in the "notificationPreferences" collection.
 */
@Document(collection = "notificationPreferences")
public class NotificationPreference {

    @Id
    private String id;
    
    @Indexed(unique = true)
    @Field("userId")
    private String userId;
    
    @Field("preferences")
    private Map<String, Boolean> preferences; // NotificationType.name -> enabled
    
    @Field("emailEnabled")
    private boolean emailEnabled;
    
    @Field("pushEnabled")
    private boolean pushEnabled;
    
    @Field("smsEnabled")
    private boolean smsEnabled;
    
    @Field("notificationSoundEnabled")
    private boolean notificationSoundEnabled;
    
    @Field("doNotDisturbEnabled")
    private boolean doNotDisturbEnabled;
    
    @Field("doNotDisturbStart")
    private String doNotDisturbStart; // Time in HH:MM format
    
    @Field("doNotDisturbEnd")
    private String doNotDisturbEnd; // Time in HH:MM format
    
    @Field("updatedAt")
    private LocalDateTime updatedAt;
    
    // Constructors
    public NotificationPreference() {
        // Default constructor required by MongoDB
        this.preferences = new HashMap<>();
        this.emailEnabled = true;
        this.pushEnabled = true;
        this.smsEnabled = false;
        this.notificationSoundEnabled = true;
        this.doNotDisturbEnabled = false;
        this.doNotDisturbStart = "22:00";
        this.doNotDisturbEnd = "07:00";
        this.updatedAt = LocalDateTime.now();
        
        // Initialize default preferences for all notification types
        for (NotificationType type : NotificationType.values()) {
            this.preferences.put(type.name(), true);
        }
    }
    
    public NotificationPreference(String userId) {
        this();
        this.userId = userId;
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public Map<String, Boolean> getPreferences() {
        return preferences;
    }
    
    public void setPreferences(Map<String, Boolean> preferences) {
        this.preferences = preferences;
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isNotificationEnabled(NotificationType type) {
        Boolean enabled = preferences.get(type.name());
        return enabled != null ? enabled : true; // Default to enabled if not specified
    }
    
    public void setNotificationEnabled(NotificationType type, boolean enabled) {
        if (this.preferences == null) {
            this.preferences = new HashMap<>();
        }
        this.preferences.put(type.name(), enabled);
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isEmailEnabled() {
        return emailEnabled;
    }
    
    public void setEmailEnabled(boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isPushEnabled() {
        return pushEnabled;
    }
    
    public void setPushEnabled(boolean pushEnabled) {
        this.pushEnabled = pushEnabled;
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isSmsEnabled() {
        return smsEnabled;
    }
    
    public void setSmsEnabled(boolean smsEnabled) {
        this.smsEnabled = smsEnabled;
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isNotificationSoundEnabled() {
        return notificationSoundEnabled;
    }
    
    public void setNotificationSoundEnabled(boolean notificationSoundEnabled) {
        this.notificationSoundEnabled = notificationSoundEnabled;
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isDoNotDisturbEnabled() {
        return doNotDisturbEnabled;
    }
    
    public void setDoNotDisturbEnabled(boolean doNotDisturbEnabled) {
        this.doNotDisturbEnabled = doNotDisturbEnabled;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getDoNotDisturbStart() {
        return doNotDisturbStart;
    }
    
    public void setDoNotDisturbStart(String doNotDisturbStart) {
        this.doNotDisturbStart = doNotDisturbStart;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getDoNotDisturbEnd() {
        return doNotDisturbEnd;
    }
    
    public void setDoNotDisturbEnd(String doNotDisturbEnd) {
        this.doNotDisturbEnd = doNotDisturbEnd;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Check if notification should be sent based on Do Not Disturb settings
     * @return true if notifications can be sent now, false otherwise
     */
    public boolean canSendNotificationNow() {
        if (!doNotDisturbEnabled) {
            return true;
        }
        
        try {
            LocalDateTime now = LocalDateTime.now();
            int currentHour = now.getHour();
            int currentMinute = now.getMinute();
            
            int startHour = Integer.parseInt(doNotDisturbStart.split(":")[0]);
            int startMinute = Integer.parseInt(doNotDisturbStart.split(":")[1]);
            
            int endHour = Integer.parseInt(doNotDisturbEnd.split(":")[0]);
            int endMinute = Integer.parseInt(doNotDisturbEnd.split(":")[1]);
            
            // Create comparable integer values (HHMM format)
            int currentTime = currentHour * 100 + currentMinute;
            int startTime = startHour * 100 + startMinute;
            int endTime = endHour * 100 + endMinute;
            
            if (startTime < endTime) {
                // Simple case: 22:00 to 07:00
                return currentTime < startTime || currentTime >= endTime;
            } else {
                // Overnight case: 22:00 to 07:00 (next day)
                return !(currentTime >= startTime || currentTime < endTime);
            }
        } catch (Exception e) {
            // If there's an error parsing the times, default to allowing notifications
            return true;
        }
    }
    
    @Override
    public String toString() {
        return "NotificationPreference{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", emailEnabled=" + emailEnabled +
                ", pushEnabled=" + pushEnabled +
                ", smsEnabled=" + smsEnabled +
                ", doNotDisturbEnabled=" + doNotDisturbEnabled +
                ", updatedAt=" + updatedAt +
                '}';
    }
}