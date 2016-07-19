package org.apache.fineract.notification.service;


import java.util.List;

public interface NotificationWritePlatformService {
    Long notify(Long userId, String objectType, Long objectId, String action,
                String actor, String notificationContent, boolean isSystemGenerated);

    Long notify(List<Long> userIds, String objectType, Long objectId, String action,
                String actor, String notificationContent, boolean isSystemGenerated);
}
