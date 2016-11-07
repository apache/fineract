package org.apache.fineract.notification.eventandlistener.savingsaccountclosed;

import org.apache.fineract.notification.eventandlistener.Event;
import org.apache.fineract.useradministration.domain.AppUser;

public class SavingsAccountClosedEvent extends Event {
    public SavingsAccountClosedEvent(Object source, String eventType, AppUser currentUser, String tenantIdentifier, Long objectId, Long officeId) {
        super(source, eventType, currentUser, tenantIdentifier, objectId, officeId);
    }
}
