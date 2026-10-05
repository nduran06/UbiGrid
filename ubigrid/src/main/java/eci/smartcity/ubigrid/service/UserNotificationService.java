package eci.smartcity.ubigrid.service;

import java.time.LocalDateTime;
import java.util.List;

import eci.smartcity.ubigrid.model.Notification;
import eci.smartcity.ubigrid.model.NotificationPreference;
import eci.smartcity.ubigrid.model.RouteSegment;
import eci.smartcity.ubigrid.model.enums.NotificationType;
import eci.smartcity.ubigrid.model.traffic.TrafficIncident;

import java.util.List;
import java.util.Map;

/**
 * Service interface for user notification operations.
 */
public interface UserNotificationService {
    
    /**
     * Send a notification to a user
     * 
     * @param userId The ID of the user to notify
     * @param type The type of notification
     * @param title The title of the notification
     * @param message The message content
     * @param data Additional data related to the notification
     * @return The created notification
     */
    Notification sendNotification(
            String userId, 
            NotificationType type, 
            String title, 
            String message, 
            Map<String, Object> data);
    
    /**
     * Send a notification to multiple users
     * 
     * @param userIds List of user IDs to notify
     * @param type The type of notification
     * @param title The title of the notification
     * @param message The message content
     * @param data Additional data related to the notification
     * @return List of created notifications
     */
    List<Notification> sendBulkNotifications(
            List<String> userIds, 
            NotificationType type, 
            String title, 
            String message, 
            Map<String, Object> data);
    
    /**
     * Get all notifications for a user
     * 
     * @param userId The ID of the user
     * @param unreadOnly If true, return only unread notifications
     * @return List of notifications
     */
    List<Notification> getUserNotifications(String userId, boolean unreadOnly);
    
    /**
     * Mark a notification as read
     * 
     * @param notificationId The ID of the notification
     * @param userId The ID of the user (for validation)
     * @return The updated notification
     */
    Notification markNotificationAsRead(String notificationId, String userId);
    
    /**
     * Mark all notifications as read for a user
     * 
     * @param userId The ID of the user
     * @return The number of notifications marked as read
     */
    int markAllNotificationsAsRead(String userId);
    
    /**
     * Delete a notification
     * 
     * @param notificationId The ID of the notification
     * @param userId The ID of the user (for validation)
     * @return True if successful, false otherwise
     */
    boolean deleteNotification(String notificationId, String userId);
    
    /**
     * Update user notification preferences
     * 
     * @param userId The ID of the user
     * @param preferences Map of notification types to preference settings
     * @return The updated notification preferences
     */
    NotificationPreference updateNotificationPreferences(
            String userId, 
            Map<NotificationType, Boolean> preferences);
    
    /**
     * Get user notification preferences
     * 
     * @param userId The ID of the user
     * @return The user's notification preferences
     */
    NotificationPreference getNotificationPreferences(String userId);
    
    /**
     * Check if a user has enabled notifications for a specific type
     * 
     * @param userId The ID of the user
     * @param type The notification type to check
     * @return True if notifications are enabled, false otherwise
     */
    boolean isNotificationEnabled(String userId, NotificationType type);
}