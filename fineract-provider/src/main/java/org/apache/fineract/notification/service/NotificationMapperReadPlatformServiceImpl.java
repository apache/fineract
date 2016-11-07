package org.apache.fineract.notification.service;

import org.apache.fineract.notification.domain.NotificationMapper;
import org.apache.fineract.notification.domain.NotificationMapperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationMapperReadPlatformServiceImpl implements NotificationMapperReadPlatformService {

    private final NotificationMapperRepository notificationMapperRepository;

    @Autowired
    public NotificationMapperReadPlatformServiceImpl(NotificationMapperRepository notificationMapperRepository) {
        this.notificationMapperRepository = notificationMapperRepository;
    }

    @Override
    public NotificationMapper findById(Long id) {
        return this.notificationMapperRepository.findOne(id);
    }

    @Override
    public List<NotificationMapper> fetchAllNotifications() {
        return this.notificationMapperRepository.findAll();
    }

    @Override
    public void delete(Long id) {
        this.notificationMapperRepository.delete(id);
    }

}
