package org.apache.fineract.notification.eventandlistener.loanclosed;

import org.apache.fineract.infrastructure.security.service.BasicAuthTenantDetailsService;
import org.apache.fineract.notification.eventandlistener.Listener;
import org.apache.fineract.notification.eventandlistener.notification.NotificationEvent;
import org.apache.fineract.useradministration.service.PermissionReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
public class LoanClosedEventListener extends Listener implements ApplicationListener<LoanClosedEvent> {

    @Autowired
    public LoanClosedEventListener(final NotificationEvent notificationEvent,
                                   final PermissionReadPlatformService permissionReadPlatformService,
                                   final BasicAuthTenantDetailsService basicAuthTenantDetailsService) {
        super(notificationEvent, permissionReadPlatformService, basicAuthTenantDetailsService);
    }

    @Override
    public void onApplicationEvent(LoanClosedEvent event) {
        buildNotification(
                event.getTenantIdentifier(),
                "READ_LOAN",
                "loan",
                event.getObjectId(),
                "Repayment made",
                event.getEventType(),
                event.getCurrentUser().getUsername(),
                event.getOfficeId()
        );
    }
}
