/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.notification.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.notification.domain.Notification;
import org.apache.fineract.notification.domain.NotificationMapper;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationWritePlatformServiceImpl implements NotificationWritePlatformService {

    private final NotificationGeneratorWritePlatformService notificationGeneratorWritePlatformService;
    private final NotificationGeneratorReadRepositoryWrapper notificationGeneratorReadRepositoryWrapper;
    private final AppUserRepository appUserRepository;
    private final NotificationMapperWritePlatformService notificationMapperWritePlatformService;

    @Override
    public Long notify(Long userId, String objectType, Long objectIdentifier, String action, Long actorId, String notificationContent,
            boolean isSystemGenerated) {

        Long generatedNotificationId = insertIntoNotificationGenerator(objectType, objectIdentifier, action, actorId, notificationContent,
                isSystemGenerated);
        insertIntoNotificationMapper(userId, generatedNotificationId);
        return generatedNotificationId;
    }

    private Long insertIntoNotificationMapper(Long userId, Long generatedNotificationId) {
        AppUser appUser = this.appUserRepository.findById(userId).orElse(null);
        NotificationMapper notificationMapper = new NotificationMapper(
                this.notificationGeneratorReadRepositoryWrapper.findById(generatedNotificationId), appUser, false,
                DateUtils.getDateOfTenant());

        this.notificationMapperWritePlatformService.create(notificationMapper);
        return notificationMapper.getId();
    }

    private Long insertIntoNotificationGenerator(String objectType, Long objectIdentifier, String action, Long actorId,
            String notificationContent, boolean isSystemGenerated) {

        Notification notification = new Notification(objectType, objectIdentifier, action, actorId, isSystemGenerated, notificationContent,
                DateUtils.getDateOfTenant());

        return this.notificationGeneratorWritePlatformService.create(notification);
    }

    @Override
    public Long notify(Collection<Long> userIds, String objectType, Long objectId, String action, Long actorId, String notificationContent,
            boolean isSystemGenerated) {

        Long generatedNotificationId = insertIntoNotificationGenerator(objectType, objectId, action, actorId, notificationContent,
                isSystemGenerated);

        insertIntoNotificationMapper(userIds, generatedNotificationId);
        return generatedNotificationId;
    }

    private List<Long> insertIntoNotificationMapper(Collection<Long> userIds, Long generatedNotificationId) {
        List<Long> mappedIds = new ArrayList<>();
        for (Long userId : userIds) {
            AppUser appUser = this.appUserRepository.findById(userId).orElseThrow();
            NotificationMapper notificationMapper = new NotificationMapper(
                    this.notificationGeneratorReadRepositoryWrapper.findById(generatedNotificationId), appUser, false,
                    DateUtils.getDateOfTenant());
            this.notificationMapperWritePlatformService.create(notificationMapper);
            mappedIds.add(notificationMapper.getId());
        }
        return mappedIds;
    }
}
