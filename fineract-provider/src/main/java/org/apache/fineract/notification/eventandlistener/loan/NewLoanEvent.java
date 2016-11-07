package org.apache.fineract.notification.eventandlistener.loan;

import org.apache.fineract.notification.eventandlistener.Event;
import org.apache.fineract.useradministration.domain.AppUser;

public class NewLoanEvent extends Event {

    public NewLoanEvent(Object source, String eventType, AppUser currentUser, String tenantIdentifier,
                        Long objectId, Long officeId) {
        super(source, eventType, currentUser, tenantIdentifier, objectId, officeId);
    }
}
