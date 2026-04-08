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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.avro.BulkMessageItemV1;
import org.apache.fineract.infrastructure.core.boot.FineractProfiles;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventResponse;
import org.apache.fineract.infrastructure.event.external.repository.ExternalEventRepository;
import org.apache.fineract.infrastructure.event.external.repository.domain.ExternalEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@Profile(FineractProfiles.TEST)
@Slf4j
@AllArgsConstructor
public class InternalExternalEventService {

    private final ObjectMapper mapper;
    private final ExternalEventRepository externalEventRepository;

    public void deleteAllExternalEvents() {
        externalEventRepository.deleteAll();
    }

    public List<ExternalEventResponse> getAllExternalEvents(String idempotencyKey, String type, String category, Long aggregateRootId) {
        var specifications = new ArrayList<Specification<ExternalEvent>>();

        if (StringUtils.isNotEmpty(idempotencyKey)) {
            specifications.add(hasIdempotencyKey(idempotencyKey));
        }

        if (StringUtils.isNotEmpty(type)) {
            specifications.add(hasType(type));
        }

        if (StringUtils.isNotEmpty(category)) {
            specifications.add(hasCategory(category));
        }

        if (aggregateRootId != null) {
            specifications.add(hasAggregateRootId(aggregateRootId));
        }

        var reducedSpecification = specifications.stream().reduce(Specification::and)
                .orElse((Specification<ExternalEvent>) (root, query, criteriaBuilder) -> null);
        var externalEvents = externalEventRepository.findAll(reducedSpecification);

        try {
            return convertToReadableFormat(externalEvents);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException
                | JsonProcessingException e) {
            throw new RuntimeException("Error while converting external events to readable format", e);
        }
    }

    private Specification<ExternalEvent> hasIdempotencyKey(String idempotencyKey) {
        return (root, query, cb) -> cb.equal(root.get("idempotencyKey"), idempotencyKey);
    }

    private Specification<ExternalEvent> hasType(String type) {
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    private Specification<ExternalEvent> hasCategory(String category) {
        return (root, query, cb) -> cb.equal(root.get("category"), category);
    }

    private Specification<ExternalEvent> hasAggregateRootId(Long aggregateRootId) {
        return (root, query, cb) -> cb.equal(root.get("aggregateRootId"), aggregateRootId);
    }

    private List<ExternalEventResponse> convertToReadableFormat(List<ExternalEvent> externalEvents) throws ClassNotFoundException,
            NoSuchMethodException, InvocationTargetException, IllegalAccessException, JsonProcessingException {
        var eventMessages = new ArrayList<ExternalEventResponse>();
        for (var externalEvent : externalEvents) {
            var payLoadClass = Class.forName(externalEvent.getSchema());
            var byteBuffer = ByteBuffer.wrap(externalEvent.getData());
            var method = payLoadClass.getMethod("fromByteBuffer", ByteBuffer.class);
            var payLoad = method.invoke(null, byteBuffer);
            if (externalEvent.getType().equalsIgnoreCase("BulkBusinessEvent")) {
                var methodToGetDatas = payLoad.getClass().getMethod("getDatas", (Class<?>) null);
                var bulkMessages = (List<BulkMessageItemV1>) methodToGetDatas.invoke(payLoad);
                var bulkMessagePayload = new StringBuilder();
                for (var bulkMessage : bulkMessages) {
                    var bulkMessageData = retrieveBulkMessage(bulkMessage, externalEvent);
                    bulkMessagePayload.append(bulkMessageData);
                    bulkMessagePayload.append(System.lineSeparator());
                }
                eventMessages.add(new ExternalEventResponse(externalEvent.getId(), externalEvent.getType(), externalEvent.getCategory(),
                        externalEvent.getCreatedAt(), toJsonMap(bulkMessagePayload.toString()), externalEvent.getBusinessDate(),
                        externalEvent.getSchema(), externalEvent.getAggregateRootId()));

            } else {
                eventMessages.add(new ExternalEventResponse(externalEvent.getId(), externalEvent.getType(), externalEvent.getCategory(),
                        externalEvent.getCreatedAt(), toJsonMap(payLoad.toString()), externalEvent.getBusinessDate(),
                        externalEvent.getSchema(), externalEvent.getAggregateRootId()));
            }
        }

        return eventMessages;
    }

    private ExternalEventResponse retrieveBulkMessage(BulkMessageItemV1 messageItem, ExternalEvent externalEvent)
            throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException,
            JsonProcessingException {
        var messageBulkMessagePayLoad = Class.forName(messageItem.getDataschema());
        var methodForPayLoad = messageBulkMessagePayLoad.getMethod("fromByteBuffer", ByteBuffer.class);
        var payLoadBulkItem = methodForPayLoad.invoke(null, messageItem.getData());
        return new ExternalEventResponse(messageItem.getId(), messageItem.getType(), messageItem.getCategory(),
                externalEvent.getCreatedAt(), toJsonMap(payLoadBulkItem.toString()), externalEvent.getBusinessDate(),
                externalEvent.getSchema(), externalEvent.getAggregateRootId());
    }

    private Map<String, Object> toJsonMap(String json) throws JsonProcessingException {
        return mapper.readValue(json, new TypeReference<>() {});
    }

}
