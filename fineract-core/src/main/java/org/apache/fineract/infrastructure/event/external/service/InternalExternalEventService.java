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
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.avro.BulkMessageItemV1;
import org.apache.fineract.infrastructure.core.boot.FineractProfiles;
import org.apache.fineract.infrastructure.event.external.repository.ExternalEventRepository;
import org.apache.fineract.infrastructure.event.external.repository.domain.ExternalEvent;
import org.apache.fineract.infrastructure.event.external.service.validation.ExternalEventDTO;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@Profile(FineractProfiles.TEST)
@Slf4j
@AllArgsConstructor
public class InternalExternalEventService {

    private final ExternalEventRepository externalEventRepository;

    public void deleteAllExternalEvents() {
        externalEventRepository.deleteAll();
    }

    public List<ExternalEventDTO> getAllExternalEvents(String idempotencyKey, String type, String category, Long aggregateRootId) {
        List<Specification<ExternalEvent>> specifications = new ArrayList<>();

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

        Specification<ExternalEvent> reducedSpecification = specifications.stream().reduce(Specification::and)
                .orElse((Specification<ExternalEvent>) (root, query, criteriaBuilder) -> null);
        List<ExternalEvent> externalEvents = externalEventRepository.findAll(reducedSpecification);

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

    private List<ExternalEventDTO> convertToReadableFormat(List<ExternalEvent> externalEvents) throws ClassNotFoundException,
            NoSuchMethodException, InvocationTargetException, IllegalAccessException, JsonProcessingException {
        List<ExternalEventDTO> eventMessages = new ArrayList<>();
        for (ExternalEvent externalEvent : externalEvents) {
            Class<?> payLoadClass = Class.forName(externalEvent.getSchema());
            ByteBuffer byteBuffer = ByteBuffer.wrap(externalEvent.getData());
            Method method = payLoadClass.getMethod("fromByteBuffer", ByteBuffer.class);
            Object payLoad = method.invoke(null, byteBuffer);
            if (externalEvent.getType().equalsIgnoreCase("BulkBusinessEvent")) {
                Method methodToGetDatas = payLoad.getClass().getMethod("getDatas", (Class<?>) null);
                List<BulkMessageItemV1> bulkMessages = (List<BulkMessageItemV1>) methodToGetDatas.invoke(payLoad);
                StringBuilder bulkMessagePayload = new StringBuilder();
                for (BulkMessageItemV1 bulkMessage : bulkMessages) {
                    ExternalEventDTO bulkMessageData = retrieveBulkMessage(bulkMessage, externalEvent);
                    bulkMessagePayload.append(bulkMessageData);
                    bulkMessagePayload.append(System.lineSeparator());
                }
                eventMessages.add(new ExternalEventDTO(externalEvent.getId(), externalEvent.getType(), externalEvent.getCategory(),
                        externalEvent.getCreatedAt(), toJsonMap(bulkMessagePayload.toString()), externalEvent.getBusinessDate(),
                        externalEvent.getSchema(), externalEvent.getAggregateRootId()));

            } else {
                eventMessages.add(new ExternalEventDTO(externalEvent.getId(), externalEvent.getType(), externalEvent.getCategory(),
                        externalEvent.getCreatedAt(), toJsonMap(payLoad.toString()), externalEvent.getBusinessDate(),
                        externalEvent.getSchema(), externalEvent.getAggregateRootId()));
            }
        }

        return eventMessages;
    }

    private ExternalEventDTO retrieveBulkMessage(BulkMessageItemV1 messageItem, ExternalEvent externalEvent) throws ClassNotFoundException,
            InvocationTargetException, IllegalAccessException, NoSuchMethodException, JsonProcessingException {
        Class<?> messageBulkMessagePayLoad = Class.forName(messageItem.getDataschema());
        Method methodForPayLoad = messageBulkMessagePayLoad.getMethod("fromByteBuffer", ByteBuffer.class);
        Object payLoadBulkItem = methodForPayLoad.invoke(null, messageItem.getData());
        return new ExternalEventDTO((long) messageItem.getId(), messageItem.getType(), messageItem.getCategory(),
                externalEvent.getCreatedAt(), toJsonMap(payLoadBulkItem.toString()), externalEvent.getBusinessDate(),
                externalEvent.getSchema(), externalEvent.getAggregateRootId());
    }

    private Map<String, Object> toJsonMap(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, new TypeReference<>() {});
    }

}
