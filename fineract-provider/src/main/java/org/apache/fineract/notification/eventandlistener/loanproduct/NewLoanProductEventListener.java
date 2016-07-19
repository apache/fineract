package org.apache.fineract.notification.eventandlistener.loanproduct;


import org.apache.fineract.infrastructure.security.service.BasicAuthTenantDetailsService;
import org.apache.fineract.notification.eventandlistener.Listener;
import org.apache.fineract.notification.eventandlistener.notification.NotificationEvent;
import org.apache.fineract.useradministration.service.PermissionReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
public class NewLoanProductEventListener extends Listener implements ApplicationListener<NewLoanProductEvent> {

    @Autowired
    public NewLoanProductEventListener(final NotificationEvent notificationEvent,
                                       final PermissionReadPlatformService permissionReadPlatformService,
                                       final BasicAuthTenantDetailsService basicAuthTenantDetailsService) {
        super(notificationEvent, permissionReadPlatformService, basicAuthTenantDetailsService);

    }

    @Override
    public void onApplicationEvent(final NewLoanProductEvent event) {
        buildNotification(
                event.getTenantIdentifier(),
                "APPROVE_LOAN",
                "loanProduct",
                event.getObjectId(),
                "New loan product created",
                event.getEventType(),
                event.getCurrentUser().getUsername(),
                event.getOfficeId()
        );
    }
}
