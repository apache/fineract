package org.apache.fineract.notification.eventandlistener.dividendposted;

import org.apache.fineract.notification.eventandlistener.Event;
import org.apache.fineract.useradministration.domain.AppUser;

public class DividendPostedEvent extends Event {
    public DividendPostedEvent(Object source, String eventType, AppUser currentUser, String tenantIdentifier, Long objectId, Long officeId) {
        super(source, eventType, currentUser, tenantIdentifier, objectId, officeId);
    }
}
