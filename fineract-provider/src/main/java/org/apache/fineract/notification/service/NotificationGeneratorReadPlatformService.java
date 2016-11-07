package org.apache.fineract.notification.service;

import org.apache.fineract.notification.domain.NotificationGenerator;

import java.util.List;


public interface NotificationGeneratorReadPlatformService {


    NotificationGenerator findById(Long id);

    List<NotificationGenerator> fetchAllNotifications();

    void delete(Long id);

}
