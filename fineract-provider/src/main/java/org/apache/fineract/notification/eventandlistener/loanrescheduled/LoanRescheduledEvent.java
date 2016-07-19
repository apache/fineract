package org.apache.fineract.notification.eventandlistener.loanrescheduled;

import org.apache.fineract.notification.eventandlistener.Event;
import org.apache.fineract.useradministration.domain.AppUser;

public class LoanRescheduledEvent extends Event {
    public LoanRescheduledEvent(Object source, String eventType, AppUser currentUser, String tenantIdentifier,
                                Long objectId, Long officeId) {
        super(source, eventType, currentUser, tenantIdentifier, objectId, officeId);
    }
}
