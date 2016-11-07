package org.apache.fineract.notification.eventandlistener.recurringdepositaccount;

import org.apache.fineract.infrastructure.security.service.BasicAuthTenantDetailsService;
import org.apache.fineract.notification.eventandlistener.Listener;
import org.apache.fineract.notification.eventandlistener.notification.NotificationEvent;
import org.apache.fineract.useradministration.service.PermissionReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
public class RecurringDepositAccountApprovedEventListener extends Listener implements
        ApplicationListener<RecurringDepositAccountApprovedEvent> {

    @Autowired
    public RecurringDepositAccountApprovedEventListener(final NotificationEvent notificationEvent,
                                                        final PermissionReadPlatformService permissionReadPlatformService,
                                                        final BasicAuthTenantDetailsService basicAuthTenantDetailsService) {
        super(notificationEvent, permissionReadPlatformService, basicAuthTenantDetailsService);
    }

    @Override
    public void onApplicationEvent(RecurringDepositAccountApprovedEvent event) {
        buildNotification(
                event.getTenantIdentifier(),
                "ACTIVATE_RECURRINGDEPOSITACCOUNT",
                "recurringDepositAccount",
                event.getObjectId(),
                "New recurring deposit account approved",
                event.getEventType(),
                event.getCurrentUser().getUsername(),
                event.getOfficeId()
        );
    }
}
