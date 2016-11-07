package org.apache.fineract.notification.service;

import org.apache.fineract.notification.domain.NotificationGenerator;

public interface NotificationGeneratorWritePlatformService  {

    Long create(NotificationGenerator notificationGenerator);

}
