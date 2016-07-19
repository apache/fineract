package org.apache.fineract.notification.eventandlistener.deposit;

import org.apache.fineract.infrastructure.security.service.BasicAuthTenantDetailsService;
import org.apache.fineract.notification.eventandlistener.Listener;
import org.apache.fineract.notification.eventandlistener.notification.NotificationEvent;
import org.apache.fineract.useradministration.service.PermissionReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
public class DepositEventListener extends Listener implements ApplicationListener<DepositEvent> {

    @Autowired
    public DepositEventListener(final NotificationEvent notificationEvent,
                                final PermissionReadPlatformService permissionReadPlatformService,
                                final BasicAuthTenantDetailsService basicAuthTenantDetailsService) {
        super(notificationEvent, permissionReadPlatformService, basicAuthTenantDetailsService);
    }

    @Override
    public void onApplicationEvent(DepositEvent event) {
        buildNotification(
                event.getTenantIdentifier(),
                "READ_SAVINGSACCOUNT",
                "savingsAccount",
                event.getObjectId(),
                "Deposit made",
                event.getEventType(),
                event.getCurrentUser().getUsername(),
                event.getOfficeId()
        );
    }
}
