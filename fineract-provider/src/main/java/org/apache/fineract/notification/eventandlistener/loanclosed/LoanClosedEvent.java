package org.apache.fineract.notification.eventandlistener.loanclosed;

import org.apache.fineract.notification.eventandlistener.Event;
import org.apache.fineract.useradministration.domain.AppUser;

public class LoanClosedEvent extends Event {
    public LoanClosedEvent(Object source, String eventType, AppUser currentUser, String tenantIdentifier,
                           Long objectId, Long officeId) {
        super(source, eventType, currentUser, tenantIdentifier, objectId, officeId);
    }
}
