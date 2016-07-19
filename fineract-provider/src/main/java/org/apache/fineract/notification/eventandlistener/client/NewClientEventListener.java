package org.apache.fineract.notification.eventandlistener.client;


import org.apache.fineract.infrastructure.security.service.BasicAuthTenantDetailsService;
import org.apache.fineract.notification.eventandlistener.Listener;
import org.apache.fineract.notification.eventandlistener.notification.NotificationEvent;
import org.apache.fineract.useradministration.service.PermissionReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
public class NewClientEventListener extends Listener implements ApplicationListener<NewClientEvent> {

    @Autowired
    public NewClientEventListener(final NotificationEvent notificationEvent,
                                  final PermissionReadPlatformService permissionReadPlatformService,
                                  final BasicAuthTenantDetailsService basicAuthTenantDetailsService) {
        super(notificationEvent, permissionReadPlatformService, basicAuthTenantDetailsService);
    }


    @Override
    public void onApplicationEvent(final NewClientEvent event) {
        buildNotification(
                event.getTenantIdentifier(),
                "ACTIVATE_CLIENT",
                "client",
                event.getObjectId(),
                "New client created",
                event.getEventType(),
                event.getCurrentUser().getUsername(),
                event.getOfficeId()
        );
    }

}
