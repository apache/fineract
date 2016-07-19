package org.apache.fineract.notification.eventandlistener.interestposted;

import org.apache.fineract.infrastructure.security.service.BasicAuthTenantDetailsService;
import org.apache.fineract.notification.eventandlistener.Listener;
import org.apache.fineract.notification.eventandlistener.notification.NotificationEvent;
import org.apache.fineract.useradministration.service.PermissionReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
public class InterestPostedEventListener extends Listener implements ApplicationListener<InterestPostedEvent> {

    @Autowired
    public InterestPostedEventListener(final NotificationEvent notificationEvent,
                                       final PermissionReadPlatformService permissionReadPlatformService,
                                       final BasicAuthTenantDetailsService basicAuthTenantDetailsService) {
        super(notificationEvent, permissionReadPlatformService, basicAuthTenantDetailsService);
    }

    @Override
    public void onApplicationEvent(InterestPostedEvent event) {
        buildNotification(
                event.getTenantIdentifier(),
                "READ_SAVINGSACCOUNT",
                "savingsAccount",
                event.getObjectId(),
                "Interest posted to account",
                event.getEventType(),
                event.getCurrentUser().getUsername(),
                event.getOfficeId()
        );
    }
}
