package org.apache.fineract.notification.eventandlistener;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.security.service.BasicAuthTenantDetailsService;
import org.apache.fineract.notification.data.NotificationData;
import org.apache.fineract.notification.eventandlistener.notification.NotificationEvent;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.service.PermissionReadPlatformService;

import javax.jms.Queue;
import java.util.List;

public class Listener {

    private final NotificationEvent notificationEvent;
    private final PermissionReadPlatformService permissionReadPlatformService;
    private final BasicAuthTenantDetailsService basicAuthTenantDetailsService;

    public Listener(NotificationEvent notificationEvent, PermissionReadPlatformService permissionReadPlatformService,
                    BasicAuthTenantDetailsService basicAuthTenantDetailsService) {
        this.notificationEvent = notificationEvent;
        this.permissionReadPlatformService = permissionReadPlatformService;
        this.basicAuthTenantDetailsService = basicAuthTenantDetailsService;
    }

    public void buildNotification(String tenantIdentifier, String permission, String objectType,
                                  Long objectIdentifier, String notificationContent, String eventType,
                                  String appUserName, Long officeId) {

        FineractPlatformTenant tenant = this.basicAuthTenantDetailsService
                .loadTenantById(tenantIdentifier,false );
        ThreadLocalContextUtil.setTenant(tenant);

        Queue queue = new ActiveMQQueue("NotificationQueue");

        List<Long> userIds = permissionReadPlatformService.retrieveUsersWithSpecificPermission(permission);

        NotificationData notificationData = new NotificationData.NotificationBuilder()
                .withUserIds(userIds)
                .withObjectType(objectType)
                .withObjectIdentifier(objectIdentifier)
                .withNotificationContent(notificationContent)
                .withAction(eventType)
                .withActor(appUserName)
                .withTenantIdentifier(tenantIdentifier)
                .withOfficeId(officeId)
                .build();
        notificationEvent.broadcastNotification(queue, notificationData);

    }
}
