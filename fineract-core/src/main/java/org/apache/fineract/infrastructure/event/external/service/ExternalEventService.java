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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.avro.BulkMessageItemV1;
import org.apache.fineract.avro.BulkMessagePayloadV1;
import org.apache.fineract.avro.generator.ByteBufferSerializable;
import org.apache.fineract.infrastructure.core.service.DataEnricherProcessor;
import org.apache.fineract.infrastructure.event.business.domain.BulkBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.BusinessEvent;
import org.apache.fineract.infrastructure.event.external.repository.ExternalEventRepository;
import org.apache.fineract.infrastructure.event.external.repository.domain.ExternalEvent;
import org.apache.fineract.infrastructure.event.external.service.idempotency.ExternalEventIdempotencyKeyGenerator;
import org.apache.fineract.infrastructure.event.external.service.message.BulkMessageItemFactory;
import org.apache.fineract.infrastructure.event.external.service.serialization.serializer.BusinessEventSerializer;
import org.apache.fineract.infrastructure.event.external.service.serialization.serializer.BusinessEventSerializerFactory;
import org.apache.fineract.infrastructure.event.external.service.support.ByteBufferConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ExternalEventService {

    private final ExternalEventRepository repository;
    private final ExternalEventIdempotencyKeyGenerator idempotencyKeyGenerator;
    private final BusinessEventSerializerFactory serializerFactory;
    private final ByteBufferConverter byteBufferConverter;
    private final BulkMessageItemFactory bulkMessageItemFactory;
    private final DataEnricherProcessor dataEnricherProcessor;

    private EntityManager entityManager;

    public <T> void postEvent(BusinessEvent<T> event) {
        if (event == null) {
            throw new IllegalArgumentException("event cannot be null");
        }

        try {
            flushChangesBeforeSerialization();
            ExternalEvent externalEvent;
            if (event instanceof BulkBusinessEvent) {
                externalEvent = handleBulkBusinessEvent((BulkBusinessEvent) event);
            } else {
                externalEvent = handleRegularBusinessEvent(event);
            }
            repository.save(externalEvent);
            log.debug("Saved message with idempotency key: [{}] of type [{}] and category [{}]", externalEvent.getIdempotencyKey(),
                    externalEvent.getType(), externalEvent.getCategory());
        } catch (IOException e) {
            throw new RuntimeException("Error while serializing event " + event.getClass().getSimpleName(), e);
        }

    }

    private ExternalEvent handleBulkBusinessEvent(BulkBusinessEvent bulkBusinessEvent) throws IOException {
        List<BulkMessageItemV1> messages = new ArrayList<>();
        List<BusinessEvent<?>> events = bulkBusinessEvent.get();
        for (int i = 0; i < events.size(); i++) {
            BusinessEvent<?> event = events.get(i);
            long id = (long) i + 1;
            BulkMessageItemV1 message = bulkMessageItemFactory.createBulkMessageItem(id, event);
            messages.add(message);
        }
        String idempotencyKey = idempotencyKeyGenerator.generate(bulkBusinessEvent);
        BulkMessagePayloadV1 avroDto = new BulkMessagePayloadV1(messages);
        byte[] data = byteBufferConverter.convert(avroDto.toByteBuffer());

        return new ExternalEvent(bulkBusinessEvent.getType(), bulkBusinessEvent.getCategory(), BulkMessagePayloadV1.class.getName(), data,
                idempotencyKey, bulkBusinessEvent.getAggregateRootId());
    }

    private <T> ExternalEvent handleRegularBusinessEvent(BusinessEvent<T> event) throws IOException {
        String eventType = event.getType();
        String eventCategory = event.getCategory();
        String idempotencyKey = idempotencyKeyGenerator.generate(event);
        BusinessEventSerializer serializer = serializerFactory.create(event);
        String schema = serializer.getSupportedSchema().getName();
        ByteBufferSerializable avroDto = dataEnricherProcessor.enrich(serializer.toAvroDTO(event));
        ByteBuffer buffer = avroDto.toByteBuffer();
        byte[] data = byteBufferConverter.convert(buffer);
        Long aggregateRootId = event.getAggregateRootId();

        return new ExternalEvent(eventType, eventCategory, schema, data, idempotencyKey, aggregateRootId);
    }

    private void flushChangesBeforeSerialization() {
        entityManager.flush();
    }

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
