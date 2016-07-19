package org.apache.fineract.notification.service;

import org.apache.fineract.notification.domain.NotificationMapper;
import org.apache.fineract.notification.domain.NotificationMapperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationMapperWritePlatformServiceImpl implements NotificationMapperWritePlatformService {

    private final NotificationMapperRepository notificationMapperRepository;

    @Autowired
    public NotificationMapperWritePlatformServiceImpl(NotificationMapperRepository notificationMapperRepository) {
        this.notificationMapperRepository = notificationMapperRepository;
    }

    @Override
    public Long create(NotificationMapper notificationMapper) {
        this.notificationMapperRepository.save(notificationMapper);
        return notificationMapper.getId();
    }
}
