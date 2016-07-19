package org.apache.fineract.notification.eventandlistener.deposit;

import org.apache.fineract.notification.eventandlistener.Event;
import org.apache.fineract.useradministration.domain.AppUser;

public class DepositEvent extends Event {
    public DepositEvent(Object source, String eventType, AppUser currentUser, String tenantIdentifier, Long objectId, Long officeId) {
        super(source, eventType, currentUser, tenantIdentifier, objectId, officeId);
    }
}
