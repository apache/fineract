package org.apache.fineract.notification.eventandlistener.savingsaccountclosed;

import org.apache.fineract.infrastructure.security.service.BasicAuthTenantDetailsService;
import org.apache.fineract.notification.eventandlistener.Listener;
import org.apache.fineract.notification.eventandlistener.notification.NotificationEvent;
import org.apache.fineract.useradministration.service.PermissionReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
public class SavingsAccountClosedEventListener extends Listener implements ApplicationListener<SavingsAccountClosedEvent> {

    @Autowired
    public SavingsAccountClosedEventListener(final NotificationEvent notificationEvent,
                                             final PermissionReadPlatformService permissionReadPlatformService,
                                             final BasicAuthTenantDetailsService basicAuthTenantDetailsService) {
        super(notificationEvent, permissionReadPlatformService, basicAuthTenantDetailsService);
    }

    @Override
    public void onApplicationEvent(SavingsAccountClosedEvent event) {
        buildNotification(
                event.getTenantIdentifier(),
                "READ_SAVINGSACCOUNT",
                "savingsAccount",
                event.getObjectId(),
                "Savings has gone into dormant",
                event.getEventType(),
                event.getCurrentUser().getUsername(),
                event.getOfficeId()
        );
    }
}
