package org.apache.fineract.notification.service;

import org.apache.fineract.notification.domain.NotificationGenerator;
import org.apache.fineract.notification.domain.NotificationGeneratorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationGeneratorWritePlatformServiceImpl implements NotificationGeneratorWritePlatformService {

    private final NotificationGeneratorRepository notificationGeneratorRepository;

    @Autowired
    public NotificationGeneratorWritePlatformServiceImpl(NotificationGeneratorRepository notificationGeneratorRepository) {
        this.notificationGeneratorRepository = notificationGeneratorRepository;
    }

    @Override
    public Long create(NotificationGenerator notificationGenerator) {
        this.notificationGeneratorRepository.save(notificationGenerator);
        return notificationGenerator.getId();
    }
}
