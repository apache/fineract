package org.apache.fineract.notification.eventandlistener.loan;


import org.apache.fineract.infrastructure.security.service.BasicAuthTenantDetailsService;
import org.apache.fineract.notification.eventandlistener.Listener;
import org.apache.fineract.notification.eventandlistener.notification.NotificationEvent;
import org.apache.fineract.useradministration.service.PermissionReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
public class NewLoanEventListener extends Listener implements ApplicationListener<NewLoanEvent> {

    @Autowired
    public NewLoanEventListener(final NotificationEvent notificationEvent,
                                final PermissionReadPlatformService permissionReadPlatformService,
                                final BasicAuthTenantDetailsService basicAuthTenantDetailsService) {
        super(notificationEvent, permissionReadPlatformService, basicAuthTenantDetailsService);
    }

    @Override
    public void onApplicationEvent(final NewLoanEvent event) {
        buildNotification(
                event.getTenantIdentifier(),
                "APPROVE_LOAN",
                "loan",
                event.getObjectId(),
                "New loan created",
                event.getEventType(),
                event.getCurrentUser().getUsername(),
                event.getOfficeId()
        );
    }
}
