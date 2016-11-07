package org.apache.fineract.notification.eventandlistener.center;

import org.apache.fineract.infrastructure.security.service.BasicAuthTenantDetailsService;
import org.apache.fineract.notification.eventandlistener.Listener;
import org.apache.fineract.notification.eventandlistener.notification.NotificationEvent;
import org.apache.fineract.useradministration.service.PermissionReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
public class NewCenterEventListener extends Listener implements ApplicationListener<NewCenterEvent> {

    @Autowired
    public NewCenterEventListener(final NotificationEvent notificationEvent,
                                  final PermissionReadPlatformService permissionReadPlatformService,
                                  final BasicAuthTenantDetailsService basicAuthTenantDetailsService) {
        super(notificationEvent, permissionReadPlatformService, basicAuthTenantDetailsService);
    }

    @Override
    public void onApplicationEvent(NewCenterEvent event) {
        buildNotification(
                event.getTenantIdentifier(),
                "ACTIVATE_CENTER",
                "center",
                event.getObjectId(),
                "New center created",
                event.getEventType(),
                event.getCurrentUser().getUsername(),
                event.getOfficeId()
        );
    }
}
