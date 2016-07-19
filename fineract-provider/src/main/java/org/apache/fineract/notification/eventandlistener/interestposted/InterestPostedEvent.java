package org.apache.fineract.notification.eventandlistener.interestposted;

import org.apache.fineract.notification.eventandlistener.Event;
import org.apache.fineract.useradministration.domain.AppUser;

public class InterestPostedEvent extends Event {
    public InterestPostedEvent(Object source, String eventType, AppUser currentUser, String tenantIdentifier,
                               Long objectId, Long officeId) {
        super(source, eventType, currentUser, tenantIdentifier, objectId, officeId);
    }
}
