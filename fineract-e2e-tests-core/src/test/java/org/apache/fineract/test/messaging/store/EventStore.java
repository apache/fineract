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
package org.apache.fineract.test.messaging.store;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.avro.BulkMessageItemV1;
import org.apache.fineract.avro.BulkMessagePayloadV1;
import org.apache.fineract.avro.MessageV1;
import org.apache.fineract.test.messaging.EventMessage;
import org.apache.fineract.test.messaging.event.Event;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventStore {

    public static final String BULK_BUSINESS_EVENT_TYPE = "BulkBusinessEvent";
    private final List<EventMessage<?>> receivedEvents = new CopyOnWriteArrayList<>();

    public <R, T extends Event<R>> boolean existsEventById(T type, Long id) {
        return findEventById(type, id).isPresent();
    }

    public <R, T extends Event<R>> Optional<EventMessage<R>> removeEventById(T type, Long id) {
        Optional<EventMessage<R>> event = findEventById(type, id);
        event.ifPresent(receivedEvents::remove);
        return event;
    }

    public <R, T extends Event<R>> Optional<EventMessage<R>> findEventById(T type, Long id) {
        List<EventMessage<R>> events = findByType(type);
        return events.stream().filter(em -> type.getIdExtractor().apply(em.getData()).equals(id)).findFirst();
    }

    public <R, T extends Event<R>> List<EventMessage<R>> findByType(T type) {
        return receivedEvents.stream().filter(em -> em.getType().equals(type.getEventName())).map(em -> (EventMessage<R>) em)
                .collect(Collectors.toList());
    }

    public List<EventMessage<?>> getReceivedEvents() {
        return receivedEvents;
    }

    void receive(byte[] message) throws Exception {
        MessageV1 msgObject = MessageV1.fromByteBuffer(ByteBuffer.wrap(message));
        String type = msgObject.getType();
        LocalDate businessDate = LocalDate.parse(msgObject.getBusinessDate(), DateTimeFormatter.ISO_LOCAL_DATE);
        Object dataObject = getDataObject(msgObject);
        if (BULK_BUSINESS_EVENT_TYPE.equals(type)) {
            BulkMessagePayloadV1 bulkPayload = (BulkMessagePayloadV1) dataObject;
            List<EventMessage<Object>> bulkEvents = bulkPayload.getDatas().stream()
                    .map((BulkMessageItemV1 item) -> getEventMessageFromBulkItem(item, businessDate)).toList();
            if (log.isDebugEnabled()) {
                bulkEvents.forEach(msg -> {
                    log.debug("Received event {}", new LoggedEvent(msg));
                });
            }
            receivedEvents.addAll(bulkEvents);
        } else {
            EventMessage<Object> msg = new EventMessage<>(type, businessDate, dataObject);
            if (log.isDebugEnabled()) {
                log.debug("Received event {}", new LoggedEvent(msg));
            }
            receivedEvents.add(msg);
        }
        log.trace("Data object within event {}", dataObject);
    }

    private EventMessage<Object> getEventMessageFromBulkItem(BulkMessageItemV1 item, LocalDate businessDate) {
        try {
            String dataschema = item.getDataschema();
            ByteBuffer data = item.getData();
            Object deserialized = deserialize(dataschema, data);
            return new EventMessage<>(item.getType(), businessDate, deserialized);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object getDataObject(MessageV1 msgObject)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        String dataschema = msgObject.getDataschema();
        ByteBuffer data = msgObject.getData();
        return deserialize(dataschema, data);
    }

    private Object deserialize(String dataschema, ByteBuffer data)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class<?> eventClass = Class.forName(dataschema);
        Method fromByteBuffer = eventClass.getMethod("fromByteBuffer", ByteBuffer.class);
        return fromByteBuffer.invoke(null, data);
    }

    public void reset() {
        receivedEvents.clear();
    }
}
