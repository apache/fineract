package org.apache.fineract.notification.eventandlistener.dividendposted;

import org.apache.fineract.infrastructure.security.service.BasicAuthTenantDetailsService;
import org.apache.fineract.notification.eventandlistener.Listener;
import org.apache.fineract.notification.eventandlistener.notification.NotificationEvent;
import org.apache.fineract.useradministration.service.PermissionReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
public class DividendPostedEventListener extends Listener implements ApplicationListener<DividendPostedEvent> {

    @Autowired
    public DividendPostedEventListener(final NotificationEvent notificationEvent,
                                       final PermissionReadPlatformService permissionReadPlatformService,
                                       final BasicAuthTenantDetailsService basicAuthTenantDetailsService) {
        super(notificationEvent, permissionReadPlatformService, basicAuthTenantDetailsService);
    }

    @Override
    public void onApplicationEvent(DividendPostedEvent event) {
        buildNotification(
                event.getTenantIdentifier(),
                "READ_DIVIDEND_SHAREPRODUCT",
                "shareProduct",
                event.getObjectId(),
                "Dividend posted to account",
                event.getEventType(),
                event.getCurrentUser().getUsername(),
                event.getOfficeId()
        );
    }
}
