package eci.smartcity.ubigrid.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import eci.smartcity.ubigrid.model.Notification;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for accessing Notification entities in MongoDB.
 */
@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

	// Find notifications by user ID
	List<Notification> findByUserId(String userId);

	// Find notifications by user ID with pagination
	Page<Notification> findByUserId(String userId, Pageable pageable);

	// Find unread notifications by user ID
	List<Notification> findByUserIdAndReadIsFalse(String userId);

	// Find notifications by user ID and type
	List<Notification> findByUserIdAndType(String userId, String type);

	// Find notifications by user ID and category
	List<Notification> findByUserIdAndCategory(String userId, String category);

	// Find notifications by user ID, read status, and ordered by creation date
	List<Notification> findByUserIdAndReadOrderByCreatedAtDesc(String userId, boolean read);

	// Count unread notifications by user ID
	long countByUserIdAndReadIsFalse(String userId);

	// Mark all notifications as read for a user
	@Query("{ 'userId': ?0, 'read': false }")
	List<Notification> findUnreadByUserId(String userId);

	// Find notifications that have expired
	List<Notification> findByExpiresAtBeforeAndReadIsFalse(LocalDateTime now);

	// Delete expired notifications
	void deleteByExpiresAtBefore(LocalDateTime now);

	// Delete notifications by user ID
	void deleteByUserId(String userId);

	// Find notification by ID and user ID (for security)
	Notification findByIdAndUserId(String id, String userId);
}