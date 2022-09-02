/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.event.external.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.event.business.domain.BulkBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.BusinessEvent;
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

    public void clearEnqueuedEvents() {
        localEventStorage.get().clear();
    }

    public List<BusinessEvent<?>> getEnqueuedEvents() {
        return List.copyOf(localEventStorage.get());
    }

    public void postEnqueuedEvents() {
        List<BusinessEvent<?>> enqueuedEvents = localEventStorage.get();
        if (enqueuedEvents.isEmpty()) {
            throw new IllegalStateException("No events have been enqueued");
        }

        delegate.postEvent(new BulkBusinessEvent(enqueuedEvents));
    }
}
