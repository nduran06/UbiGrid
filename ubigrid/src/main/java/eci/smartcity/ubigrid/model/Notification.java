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
 * Entity representing a user notification in the MongoDB database.
 * This class maps to documents in the "notifications" collection.
 */
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;
    
    @Indexed
    @Field("userId")
    private String userId;
    
    @Field("type")
    private String type; // Maps to NotificationType enum name
    
    @Field("title")
    private String title;
    
    @Field("message")
    private String message;
    
    @Field("read")
    private boolean read;
    
    @Indexed
    @Field("createdAt")
    private LocalDateTime createdAt;
    
    @Field("readAt")
    private LocalDateTime readAt;
    
    @Field("expiresAt")
    private LocalDateTime expiresAt;
    
    @Field("priority")
    private String priority; // HIGH, MEDIUM, LOW
    
    @Field("category")
    private String category;
    
    @Field("icon")
    private String icon;
    
    @Field("actionUrl")
    private String actionUrl;
    
    @Field("data")
    private Map<String, Object> data;
    
    // Constructors
    public Notification() {
        // Default constructor required by MongoDB
        this.read = false;
        this.createdAt = LocalDateTime.now();
        this.data = new HashMap<>();
        this.priority = "MEDIUM";
    }
    
    public Notification(String userId, NotificationType type, String title, String message) {
        this();
        this.userId = userId;
        this.type = type.name();
        this.title = title;
        this.message = message;
        this.category = type.getCategory();
        this.priority = type.isHighPriority() ? "HIGH" : "MEDIUM";
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
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public void setType(NotificationType type) {
        this.type = type.name();
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public boolean isRead() {
        return read;
    }
    
    public void setRead(boolean read) {
        this.read = read;
        if (read && readAt == null) {
            this.readAt = LocalDateTime.now();
        }
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getReadAt() {
        return readAt;
    }
    
    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public String getActionUrl() {
        return actionUrl;
    }
    
    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }
    
    public Map<String, Object> getData() {
        return data;
    }
    
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
    
    public void addData(String key, Object value) {
        if (this.data == null) {
            this.data = new HashMap<>();
        }
        this.data.put(key, value);
    }
    
    @Override
    public String toString() {
        return "Notification{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", read=" + read +
                ", createdAt=" + createdAt +
                ", priority='" + priority + '\'' +
                '}';
    }
}