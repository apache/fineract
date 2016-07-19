package org.apache.fineract.notification;

import org.apache.fineract.notification.domain.NotificationGenerator;
import org.apache.fineract.notification.domain.NotificationMapper;
import org.apache.fineract.notification.service.NotificationGeneratorReadPlatformService;
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

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StorageTest {

    private NotificationWritePlatformServiceImpl notificationWritePlatformServiceImpl;

    @Mock
    private NotificationGeneratorReadPlatformService notificationGeneratorReadPlatformService;

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
                notificationGeneratorReadPlatformService,
                appUserRepository,
                notificationMapperWritePlatformService);
    }

    @Test
    public void testNotificationStorage() {

        Long userId = 1L;
        String objectType = "CLIENT";
        Long objectIdentifier = 1L;
        String action = "created";
        String actor = "admin";
        String notificationContent = "A client was created";
        boolean isSystemGenerated = false;

        NotificationGenerator notificationGenerator = new NotificationGenerator.NotificationGeneratorBuilder()
                .withObjectType(objectType)
                .withObjectIdentifier(objectIdentifier)
                .withAction(action)
                .withActor(actor)
                .withNotificationContent(notificationContent)
                .withCreatedAt(getCurrentDateTime())
                .withSystemGenerated(isSystemGenerated)
                .build();


        AppUser appUser = this.appUserRepository.findOne(1L);

        NotificationMapper notificationMapper = new NotificationMapper.NotificationMapperBuilder()
                .withNotification(notificationGenerator)
                .withUser(appUser)
                .withCreatedAt(getCurrentDateTime())
                .build();


        when(this.notificationGeneratorWritePlatformService.create(refEq(notificationGenerator))).thenReturn(1L);

        when(this.appUserRepository.findOne(userId)).thenReturn(appUser);

        when(this.notificationGeneratorReadPlatformService.findById(1L)).thenReturn(notificationGenerator);

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

        verify(this.notificationGeneratorWritePlatformService, times(1)).create(refEq(notificationGenerator));
        verify(this.notificationMapperWritePlatformService, times(1)).create(refEq(notificationMapper));
        verify(this.notificationGeneratorReadPlatformService, times(1)).findById(1L);
        assertEquals(actualGeneratedNotificationId, new Long(1));
    }

    private String getCurrentDateTime() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date);
    }
}
