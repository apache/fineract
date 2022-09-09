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

import java.io.IOException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.event.business.domain.BusinessEvent;
import org.apache.fineract.infrastructure.event.external.repository.ExternalEventRepository;
import org.apache.fineract.infrastructure.event.external.repository.domain.ExternalEvent;
import org.apache.fineract.infrastructure.event.external.service.idempotency.ExternalEventIdempotencyKeyGenerator;
import org.apache.fineract.infrastructure.event.external.service.serialization.BusinessEventSerializerFactory;
import org.apache.fineract.infrastructure.event.external.service.serialization.serializer.BusinessEventSerializer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ExternalEventService {

    private final ExternalEventRepository repository;
    private final ExternalEventIdempotencyKeyGenerator idempotencyKeyGenerator;
    private final BusinessEventSerializerFactory serializerFactory;

    private EntityManager entityManager;

    public <T> void postEvent(BusinessEvent<T> event) {
        if (event == null) {
            throw new IllegalArgumentException("event cannot be null");
        }

        String eventType = event.getType();
        String idempotencyKey = idempotencyKeyGenerator.generate(event);
        try {
            BusinessEventSerializer serializer = serializerFactory.create(event);
            String schema = serializer.getSupportedSchema().getName();
            flushChangesBeforeSerialization();
            byte[] data = serializer.serialize(event);
            ExternalEvent externalEvent = new ExternalEvent(eventType, schema, data, idempotencyKey);

            repository.save(externalEvent);
        } catch (IOException e) {
            throw new RuntimeException("Error while serializing event " + event.getClass().getSimpleName(), e);
        }

    }

    private void flushChangesBeforeSerialization() {
        entityManager.flush();
    }

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
