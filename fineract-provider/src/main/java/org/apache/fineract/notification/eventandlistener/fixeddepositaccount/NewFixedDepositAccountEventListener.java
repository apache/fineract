package org.apache.fineract.notification.eventandlistener.fixeddepositaccount;

import org.apache.fineract.infrastructure.security.service.BasicAuthTenantDetailsService;
import org.apache.fineract.notification.eventandlistener.Listener;
import org.apache.fineract.notification.eventandlistener.notification.NotificationEvent;
import org.apache.fineract.useradministration.service.PermissionReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
public class NewFixedDepositAccountEventListener extends Listener implements ApplicationListener<NewFixedDepositAccountEvent>{

    @Autowired
    public NewFixedDepositAccountEventListener(final NotificationEvent notificationEvent,
                                               final PermissionReadPlatformService permissionReadPlatformService,
                                               final BasicAuthTenantDetailsService basicAuthTenantDetailsService) {
        super(notificationEvent, permissionReadPlatformService, basicAuthTenantDetailsService);
    }

    @Override
    public void onApplicationEvent(final NewFixedDepositAccountEvent event) {
        buildNotification(
                event.getTenantIdentifier(),
                "APPROVE_FIXEDDEPOSITACCOUNT",
                "fixedDeposit",
                event.getObjectId(),
                "New fixed deposit account created",
                event.getEventType(),
                event.getCurrentUser().getUsername(),
                event.getOfficeId()
        );
    }
}
