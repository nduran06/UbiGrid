package eci.smartcity.ubigrid.service;

/**
 * Client interface for sending notifications to users
 * This would be implemented by a concrete class that integrates with
 * a notification service like Firebase Cloud Messaging, AWS SNS, etc.
 */

import java.util.Map;

public interface NotificationClient {

	/**
	 * Send a notification to a specific user
	 * 
	 * @param userId           ID of the user to receive the notification
	 * @param notificationType Type of notification (e.g., INCIDENT, ROUTE_UPDATE)
	 * @param payload          Map containing the notification data
	 */
	void sendNotification(String userId, String notificationType, Map<String, Object> payload);
}