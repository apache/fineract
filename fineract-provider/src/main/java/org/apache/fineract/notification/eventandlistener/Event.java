package org.apache.fineract.notification.eventandlistener;

import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.context.ApplicationEvent;

public class Event extends ApplicationEvent {
    private final String eventType;
    private final AppUser currentUser;
    private final String tenantIdentifier;
    private Long objectId;
    private Long officeId;

    public Event(final Object source, final String eventType, final AppUser currentUser, final String tenantIdentifier,
                 final Long objectId, final Long officeId) {
        super(source);
        this.eventType = eventType;
        this.currentUser = currentUser;
        this.tenantIdentifier = tenantIdentifier;
        this.officeId = officeId;
        this.objectId = objectId;

    }

    public Long getObjectId() {
        return objectId;
    }

    public Long getOfficeId() {
        return officeId;
    }

    public String getTenantIdentifier() {
        return tenantIdentifier;
    }

    public String getEventType() {
        return eventType;
    }

    public AppUser getCurrentUser() {
        return currentUser;
    }
}
