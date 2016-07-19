package org.apache.fineract.notification.service;

import org.apache.fineract.notification.domain.NotificationMapper;


public interface NotificationMapperWritePlatformService {

    Long create(NotificationMapper notificationMapper);
}
