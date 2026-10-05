package eci.smartcity.ubigrid.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import eci.smartcity.ubigrid.model.NotificationPreference;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for accessing NotificationPreference entities in MongoDB.
 */
@Repository
public interface NotificationPreferenceRepository extends MongoRepository<NotificationPreference, String> {
    
    // Find preferences by user ID
    Optional<NotificationPreference> findByUserId(String userId);
    
    // Find users who have enabled a specific notification type
    List<NotificationPreference> findByPreferencesContaining(String notificationType);
    
    // Find users who have enabled email notifications
    List<NotificationPreference> findByEmailEnabledIsTrue();
    
    // Find users who have enabled push notifications
    List<NotificationPreference> findByPushEnabledIsTrue();
    
    // Find users who have enabled SMS notifications
    List<NotificationPreference> findBySmsEnabledIsTrue();
    
    // Find users who have Do Not Disturb enabled
    List<NotificationPreference> findByDoNotDisturbEnabledIsTrue();
    
    // Check if a user exists
    boolean existsByUserId(String userId);
    
    // Delete preferences by user ID
    void deleteByUserId(String userId);
}
