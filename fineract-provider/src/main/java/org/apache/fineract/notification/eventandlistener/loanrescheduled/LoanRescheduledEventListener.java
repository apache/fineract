package org.apache.fineract.notification.eventandlistener.loanrescheduled;

import org.apache.fineract.infrastructure.security.service.BasicAuthTenantDetailsService;
import org.apache.fineract.notification.eventandlistener.Listener;
import org.apache.fineract.notification.eventandlistener.notification.NotificationEvent;
import org.apache.fineract.useradministration.service.PermissionReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
public class LoanRescheduledEventListener extends Listener implements ApplicationListener<LoanRescheduledEvent> {

    @Autowired
    public LoanRescheduledEventListener(final NotificationEvent notificationEvent,
                                        final PermissionReadPlatformService permissionReadPlatformService,
                                        final BasicAuthTenantDetailsService basicAuthTenantDetailsService) {
        super(notificationEvent, permissionReadPlatformService, basicAuthTenantDetailsService);
    }

    @Override
    public void onApplicationEvent(LoanRescheduledEvent event) {
        buildNotification(
                event.getTenantIdentifier(),
                "READ_Rescheduled Loans",
                "loan",
                event.getObjectId(),
                "Loan has been rescheduled",
                event.getEventType(),
                event.getCurrentUser().getUsername(),
                event.getOfficeId()
        );
    }
}
