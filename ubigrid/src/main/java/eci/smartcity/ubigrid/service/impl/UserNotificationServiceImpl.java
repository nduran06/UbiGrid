package eci.smartcity.ubigrid.service.impl;

import org.springframework.stereotype.Service;

import eci.smartcity.ubigrid.model.Notification;
import eci.smartcity.ubigrid.model.NotificationPreference;
import eci.smartcity.ubigrid.model.RouteSegment;
import eci.smartcity.ubigrid.model.enums.NotificationType;
import eci.smartcity.ubigrid.model.traffic.TrafficIncident;
import eci.smartcity.ubigrid.repository.NotificationPreferenceRepository;
import eci.smartcity.ubigrid.repository.NotificationRepository;
import eci.smartcity.ubigrid.service.NotificationClient;
import eci.smartcity.ubigrid.service.UserNotificationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of the UserNotificationService interface.
 */
@Service
public class UserNotificationServiceImpl implements UserNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(UserNotificationServiceImpl.class);
    
    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    
    // In a real implementation, you would inject additional services for different notification channels
    // private final EmailService emailService;
    // private final PushNotificationService pushService;
    // private final SmsService smsService;
    
    @Autowired
    public UserNotificationServiceImpl(
            NotificationRepository notificationRepository,
            NotificationPreferenceRepository preferenceRepository) {
        this.notificationRepository = notificationRepository;
        this.preferenceRepository = preferenceRepository;
    }
    
    @Override
    @Transactional
    public Notification sendNotification(
            String userId, 
            NotificationType type, 
            String title, 
            String message, 
            Map<String, Object> data) {
        
        logger.debug("Sending notification to user {}: {}", userId, title);
        
        // Check if notifications are enabled for this type
        if (!isNotificationEnabled(userId, type)) {
            logger.debug("Notifications of type {} are disabled for user {}", type, userId);
            return null;
        }
        
        // Create notification entity
        Notification notification = new Notification(userId, type, title, message);
        
        if (data != null) {
            notification.setData(data);
        }
        
        // Set expiration date (e.g., 30 days from now)
        notification.setExpiresAt(LocalDateTime.now().plusDays(30));
        
        // Save notification to database
        notification = notificationRepository.save(notification);
        
        // Get user preferences
        NotificationPreference preferences = getNotificationPreferences(userId);
        
        // Send through appropriate channels based on user preferences
        sendThroughChannels(notification, preferences);
        
        return notification;
    }
    
    @Override
    @Transactional
    public List<Notification> sendBulkNotifications(
            List<String> userIds, 
            NotificationType type, 
            String title, 
            String message, 
            Map<String, Object> data) {
        
        logger.debug("Sending bulk notifications to {} users", userIds.size());
        
        List<Notification> notifications = new ArrayList<>();
        
        for (String userId : userIds) {
            Notification notification = sendNotification(userId, type, title, message, data);
            if (notification != null) {
                notifications.add(notification);
            }
        }
        
        return notifications;
    }
    
    @Override
    public List<Notification> getUserNotifications(String userId, boolean unreadOnly) {
        if (unreadOnly) {
            return notificationRepository.findByUserIdAndReadIsFalse(userId);
        } else {
            return notificationRepository.findByUserId(userId);
        }
    }
    
    @Override
    @Transactional
    public Notification markNotificationAsRead(String notificationId, String userId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId);
        
        if (notification != null && !notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            return notificationRepository.save(notification);
        }
        
        return notification;
    }
    
    @Override
    @Transactional
    public int markAllNotificationsAsRead(String userId) {
        List<Notification> unreadNotifications = notificationRepository.findUnreadByUserId(userId);
        
        LocalDateTime now = LocalDateTime.now();
        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
            notification.setReadAt(now);
        }
        
        notificationRepository.saveAll(unreadNotifications);
        
        return unreadNotifications.size();
    }
    
    @Override
    @Transactional
    public boolean deleteNotification(String notificationId, String userId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId);
        
        if (notification != null) {
            notificationRepository.delete(notification);
            return true;
        }
        
        return false;
    }
    
    @Override
    @Transactional
    public NotificationPreference updateNotificationPreferences(
            String userId, 
            Map<NotificationType, Boolean> preferences) {
        
        // Get existing preferences or create new ones
        NotificationPreference userPreferences = preferenceRepository.findByUserId(userId)
                .orElse(new NotificationPreference(userId));
        
        // Update preferences
        for (Map.Entry<NotificationType, Boolean> entry : preferences.entrySet()) {
            userPreferences.setNotificationEnabled(entry.getKey(), entry.getValue());
        }
        
        // Save and return updated preferences
        return preferenceRepository.save(userPreferences);
    }
    
    @Override
    public NotificationPreference getNotificationPreferences(String userId) {
        return preferenceRepository.findByUserId(userId)
                .orElse(new NotificationPreference(userId));
    }
    
    @Override
    public boolean isNotificationEnabled(String userId, NotificationType type) {
        Optional<NotificationPreference> preferences = preferenceRepository.findByUserId(userId);
        
        if (preferences.isPresent()) {
            return preferences.get().isNotificationEnabled(type);
        }
        
        // Default to enabled if preferences don't exist
        return true;
    }
    
    // Helper method to send notification through appropriate channels
    @Async
    private CompletableFuture<Void> sendThroughChannels(
            Notification notification, 
            NotificationPreference preferences) {
        
        try {
            // Check if notifications can be sent now (Do Not Disturb)
            if (!preferences.canSendNotificationNow() && 
                !isHighPriorityNotification(notification)) {
                logger.debug("Do Not Disturb is active for user {}, notification queued",
                        notification.getUserId());
                return CompletableFuture.completedFuture(null);
            }
            
            // In a real implementation, you would call each service based on preferences
            
            // Send push notification if enabled
            if (preferences.isPushEnabled()) {
                sendPushNotification(notification);
            }
            
            // Send email if enabled
            if (preferences.isEmailEnabled()) {
                sendEmailNotification(notification);
            }
            
            // Send SMS if enabled
            if (preferences.isSmsEnabled()) {
                sendSmsNotification(notification);
            }
            
        } catch (Exception e) {
            logger.error("Error sending notification through channels: {}", e.getMessage(), e);
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    private boolean isHighPriorityNotification(Notification notification) {
        return "HIGH".equals(notification.getPriority());
    }
    
    // In a real implementation, these methods would call actual notification services
    
    private void sendPushNotification(Notification notification) {
        // This would call a push notification service (Firebase, OneSignal, etc.)
        logger.debug("Sending push notification to user {}: {}", 
                notification.getUserId(), notification.getTitle());
    }
    
    private void sendEmailNotification(Notification notification) {
        // This would call an email service
        logger.debug("Sending email notification to user {}: {}", 
                notification.getUserId(), notification.getTitle());
    }
    
    private void sendSmsNotification(Notification notification) {
        // This would call an SMS service (Twilio, etc.)
        logger.debug("Sending SMS notification to user {}: {}", 
                notification.getUserId(), notification.getTitle());
    }
}
