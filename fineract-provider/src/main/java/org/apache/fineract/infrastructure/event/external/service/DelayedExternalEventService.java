package org.apache.fineract.infrastructure.event.external.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.businessevent.domain.BulkBusinessEvent;
import org.apache.fineract.portfolio.businessevent.domain.BusinessEvent;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DelayedExternalEventService {

    private final ThreadLocal<List<BusinessEvent<?>>> localEventStorage = ThreadLocal.withInitial(ArrayList::new);

    private final ExternalEventService delegate;

    public <T> void enqueueEvent(BusinessEvent<T> event) {
        if (event == null) {
            throw new IllegalArgumentException("event cannot be null");
        }

        localEventStorage.get().add(event);
    }

    public boolean hasEnqueuedEvents() {
        return !localEventStorage.get().isEmpty();
    }

    public void postEnqueuedEvents() {
        List<BusinessEvent<?>> enqueuedEvents = localEventStorage.get();
        if (enqueuedEvents.isEmpty()) {
            throw new IllegalStateException("No events have been enqueued");
        }

        delegate.postEvent(new BulkBusinessEvent(enqueuedEvents));
    }
}
