package org.apache.fineract.notification.service;

import org.apache.fineract.notification.domain.NotificationMapper;

import java.util.List;

public interface NotificationMapperReadPlatformService {


    NotificationMapper findById(Long id);

    List<NotificationMapper> fetchAllNotifications();

    void delete(Long id);

}
