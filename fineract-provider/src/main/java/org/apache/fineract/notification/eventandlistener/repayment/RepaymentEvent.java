package org.apache.fineract.notification.eventandlistener.repayment;

import org.apache.fineract.notification.eventandlistener.Event;
import org.apache.fineract.useradministration.domain.AppUser;

public class RepaymentEvent extends Event{
    public RepaymentEvent(Object source, String eventType, AppUser currentUser, String tenantIdentifier,
                          Long objectId, Long officeId) {
        super(source, eventType, currentUser, tenantIdentifier, objectId, officeId);
    }
}
