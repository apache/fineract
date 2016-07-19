package org.apache.fineract.notification.eventandlistener.shareaccount;


import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.notification.eventandlistener.Event;
import org.apache.fineract.useradministration.domain.AppUser;

public class ShareAccountApprovedEvent extends Event {

    public ShareAccountApprovedEvent(Object source, String eventType, AppUser currentUser, String tenantIdentifier,
                                     Long objectId, Long officeId) {
        super(source, eventType, currentUser, tenantIdentifier, objectId, officeId);
    }
}
