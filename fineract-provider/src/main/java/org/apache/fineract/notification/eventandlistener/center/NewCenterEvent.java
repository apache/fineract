package org.apache.fineract.notification.eventandlistener.center;

import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.notification.eventandlistener.Event;
import org.apache.fineract.useradministration.domain.AppUser;

public class NewCenterEvent extends Event {

    public NewCenterEvent(Object source, String eventType, AppUser currentUser, String tenantIdentifier,
                          Long objectId, Long officeId) {
        super(source, eventType, currentUser, tenantIdentifier, objectId, officeId);
    }
}
