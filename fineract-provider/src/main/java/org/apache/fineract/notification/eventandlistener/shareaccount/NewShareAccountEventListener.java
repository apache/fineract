package org.apache.fineract.notification.eventandlistener.shareaccount;

import org.apache.fineract.infrastructure.security.service.BasicAuthTenantDetailsService;
import org.apache.fineract.notification.eventandlistener.Listener;
import org.apache.fineract.notification.eventandlistener.notification.NotificationEvent;
import org.apache.fineract.useradministration.service.PermissionReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
public class NewShareAccountEventListener extends Listener implements ApplicationListener<NewShareAccountEvent> {

    @Autowired
    public NewShareAccountEventListener(final NotificationEvent notificationEvent,
                                        final PermissionReadPlatformService permissionReadPlatformService,
                                        final BasicAuthTenantDetailsService basicAuthTenantDetailsService) {
        super(notificationEvent, permissionReadPlatformService, basicAuthTenantDetailsService);
    }

    @Override
    public void onApplicationEvent(NewShareAccountEvent event) {
        buildNotification(
                event.getTenantIdentifier(),
                "APPROVE_SHAREACCOUNT",
                "shareAccount",
                event.getObjectId(),
                "New share account created",
                event.getEventType(),
                event.getCurrentUser().getUsername(),
                event.getOfficeId()
        );
    }
}
