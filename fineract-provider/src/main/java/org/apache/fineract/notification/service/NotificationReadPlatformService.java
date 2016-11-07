package org.apache.fineract.notification.service;


import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.notification.data.NotificationData;

public interface NotificationReadPlatformService {

    boolean hasUnreadNotifications(Long appUserId);

    Page<NotificationData> getAllUnreadNotifications(SearchParameters searchParameters);

    Page<NotificationData> getAllNotifications(SearchParameters searchParameters);

    void updateNotificationReadStatus();
}
