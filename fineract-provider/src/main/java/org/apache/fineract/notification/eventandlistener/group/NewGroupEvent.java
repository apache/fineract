package org.apache.fineract.notification.eventandlistener.group;

import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.notification.eventandlistener.Event;
import org.apache.fineract.useradministration.domain.AppUser;

public class NewGroupEvent extends Event {

    public NewGroupEvent(Object source, String eventType, AppUser currentUser, String tenantIdentifier, Long objectId,
                         Long officeId) {
        super(source, eventType, currentUser, tenantIdentifier, objectId, officeId);
    }
}
