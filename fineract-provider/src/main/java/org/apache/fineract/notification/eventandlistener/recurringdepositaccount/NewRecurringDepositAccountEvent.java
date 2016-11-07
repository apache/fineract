package org.apache.fineract.notification.eventandlistener.recurringdepositaccount;


import org.apache.fineract.notification.eventandlistener.Event;
import org.apache.fineract.useradministration.domain.AppUser;

public class NewRecurringDepositAccountEvent extends Event {

    public NewRecurringDepositAccountEvent(Object source, String eventType, AppUser currentUser, String tenantIdentifier,
                                           Long objectId, Long officeId) {
        super(source, eventType, currentUser, tenantIdentifier, objectId, officeId);
    }
}
