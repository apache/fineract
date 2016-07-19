package org.apache.fineract.notification.service;

import org.apache.fineract.notification.domain.NotificationGenerator;
import org.apache.fineract.notification.domain.NotificationGeneratorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationGeneratorReadPlatformServiceImpl implements NotificationGeneratorReadPlatformService {

    private final NotificationGeneratorRepository notificationGeneratorRepository;

    @Autowired
    public NotificationGeneratorReadPlatformServiceImpl(NotificationGeneratorRepository notificationGeneratorRepository) {
        this.notificationGeneratorRepository = notificationGeneratorRepository;
    }

    @Override
    public NotificationGenerator findById(Long id) {
        return this.notificationGeneratorRepository.findOne(id);
    }

    @Override
    public List<NotificationGenerator> fetchAllNotifications() {
        return this.notificationGeneratorRepository.findAll();
    }

    @Override
    public void delete(Long id) {
       this.notificationGeneratorRepository.delete(id);
    }

}
