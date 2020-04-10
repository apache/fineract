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

package org.apache.fineract.notification;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import org.apache.fineract.notification.domain.Notification;
import org.apache.fineract.notification.domain.NotificationMapper;
import org.apache.fineract.notification.service.NotificationGeneratorReadRepositoryWrapper;
import org.apache.fineract.notification.service.NotificationGeneratorWritePlatformService;
import org.apache.fineract.notification.service.NotificationMapperWritePlatformService;
import org.apache.fineract.notification.service.NotificationWritePlatformServiceImpl;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.userdetails.User;

@RunWith(MockitoJUnitRunner.class)
public class StorageTest {

    private NotificationWritePlatformServiceImpl notificationWritePlatformServiceImpl;

    @Mock
    private NotificationGeneratorReadRepositoryWrapper notificationGeneratorReadRepositoryWrapper;

    @Mock
    private NotificationGeneratorWritePlatformService notificationGeneratorWritePlatformService;

    @Mock
    private NotificationMapperWritePlatformService notificationMapperWritePlatformService;

    @Mock
    private AppUserRepository appUserRepository;

    @Before
    public void setUp() {
        notificationWritePlatformServiceImpl = new NotificationWritePlatformServiceImpl(
                notificationGeneratorWritePlatformService,
                notificationGeneratorReadRepositoryWrapper,
                appUserRepository,
                notificationMapperWritePlatformService);
    }

    @Test
    public void testNotificationStorage() {

        Long userId = 1L;
        String objectType = "CLIENT";
        Long objectIdentifier = 1L;
        String action = "created";
        Long actor = 1L;
        String notificationContent = "A client was created";
        boolean isSystemGenerated = false;

        Notification notification = new Notification(
                objectType,
                objectIdentifier,
                action,
                actor,
                isSystemGenerated,
                notificationContent,
                getCurrentDateTime()
        );

        AppUser appUser = new AppUser(null, new User("J.J.", "", true, true,
                true, true, Collections.emptyList()),
                null, "user@com", "John", "", null, false,
                 false, null);

        NotificationMapper notificationMapper = new NotificationMapper(
                notification,
                appUser,
                false,
                getCurrentDateTime()
        );

        when(this.notificationGeneratorWritePlatformService.create(refEq(notification))).thenReturn(1L);

        when(this.appUserRepository.findById(userId)).thenReturn(Optional.of(appUser));

        when(this.notificationGeneratorReadRepositoryWrapper.findById(1L)).thenReturn(notification);

        when(this.notificationMapperWritePlatformService.create(refEq(notificationMapper))).thenReturn(1L);

        Long actualGeneratedNotificationId =
                notificationWritePlatformServiceImpl.notify(
                        userId,
                        objectType,
                        objectIdentifier,
                        action,
                        actor,
                        notificationContent,
                        isSystemGenerated
                );

        verify(this.notificationGeneratorWritePlatformService, times(1)).create(refEq(notification));
        verify(this.notificationMapperWritePlatformService, times(1)).create(refEq(notificationMapper));
        verify(this.notificationGeneratorReadRepositoryWrapper, times(1)).findById(1L);
        assertEquals(actualGeneratedNotificationId, Long.valueOf(1));
    }

    private String getCurrentDateTime() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date);
    }
}
