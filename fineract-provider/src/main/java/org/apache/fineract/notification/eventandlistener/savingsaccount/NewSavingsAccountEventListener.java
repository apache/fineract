package org.apache.fineract.notification.eventandlistener.savingsaccount;

import org.apache.fineract.infrastructure.security.service.BasicAuthTenantDetailsService;
import org.apache.fineract.notification.eventandlistener.Listener;
import org.apache.fineract.notification.eventandlistener.notification.NotificationEvent;
import org.apache.fineract.useradministration.service.PermissionReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
public class NewSavingsAccountEventListener extends Listener implements ApplicationListener<NewSavingsAccountEvent> {

    @Autowired
    public NewSavingsAccountEventListener(final NotificationEvent notificationEvent,
                                          final PermissionReadPlatformService permissionReadPlatformService,
                                          final BasicAuthTenantDetailsService basicAuthTenantDetailsService) {
        super(notificationEvent, permissionReadPlatformService, basicAuthTenantDetailsService);

    }

    @Override
    public void onApplicationEvent(NewSavingsAccountEvent event) {
        buildNotification(
                event.getTenantIdentifier(),
                "APPROVE_SAVINGSACCOUNT",
                "savingsAccount",
                event.getObjectId(),
                "New savings account created",
                event.getEventType(),
                event.getCurrentUser().getUsername(),
                event.getOfficeId()
        );
    }
}
