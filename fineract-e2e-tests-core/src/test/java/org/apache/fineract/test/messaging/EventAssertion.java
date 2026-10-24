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
package org.apache.fineract.test.messaging;

import static java.lang.String.format;
import static org.awaitility.Awaitility.await;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.test.messaging.config.EventProperties;
import org.apache.fineract.test.messaging.event.Event;
import org.apache.fineract.test.messaging.event.EventFactory;
import org.apache.fineract.test.messaging.store.EventStore;
import org.apache.fineract.test.messaging.store.LoggedEvent;
import org.assertj.core.api.Assertions;
import org.awaitility.core.ConditionTimeoutException;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class EventAssertion {

    private final EventStore eventStore;
    private final EventFactory eventFactory;
    private final EventProperties eventProperties;

    public <R, T extends Event<R>> void assertEventRaised(Class<T> eventClazz, Long id) {
        internalAssertEventRaised(eventClazz, id, true);
    }

    public <R, T extends Event<R>> void assertEventNotRaised(Class<T> eventClazz, Long id) {
        internalAssertEventNotRaised(eventClazz, id);
    }

    private <R, T extends Event<R>> void internalAssertEventRaised(final Class<T> eventClazz, final Long id,
            final boolean removeEventIfFound) {
        if (eventProperties.isEventVerificationDisabled()) {
            return;
        }
        final T event = eventFactory.create(eventClazz);
        awaitRaised(() -> {
            if (removeEventIfFound) {
                return eventStore.removeEventById(event, id).isPresent();
            } else {
                return eventStore.findEventById(event, id).isPresent();
            }
        }, event.getEventName());
    }

    private <R, T extends Event<R>> void internalAssertEventNotRaised(final Class<T> eventClazz, final Long id) {
        if (eventProperties.isEventVerificationDisabled()) {
            return;
        }
        final T event = eventFactory.create(eventClazz);
        assertNeverRaised(() -> {
            if (id == null) {
                return !eventStore.findByType(event).isEmpty();
            }
            return eventStore.findByType(event).stream().anyMatch(em -> event.getIdExtractor().apply(em.getData()).equals(id));
        }, () -> unexpectedEventsMessage(event.getEventName()));
    }

    public <R, T extends Event<R>> EventAssertionBuilder<R> assertEvent(Class<T> eventClazz, Long id) {
        EventMessage<R> eventMessage;
        if (eventProperties.isEventVerificationEnabled()) {
            internalAssertEventRaised(eventClazz, id, false);
            T event = eventFactory.create(eventClazz);
            eventMessage = eventStore.removeEventById(event, id).get();
        } else {
            eventMessage = (EventMessage<R>) new EmptyEventMessage();
        }
        log.debug("Assert event: {}", eventMessage.getIdempotencyKey());
        return new EventAssertionBuilder<>(eventMessage);
    }

    /**
     * Like {@link #assertEvent(Class, Long)}, but also accepts an event that an earlier step has already verified (and
     * so removed from the store), for steps that inspect the payload of an event raised by a previous action step.
     */
    public <R, T extends Event<R>> EventAssertionBuilder<R> assertEventReceived(final Class<T> eventClazz, final Long id) {
        if (eventProperties.isEventVerificationDisabled()) {
            return new EventAssertionBuilder<>((EventMessage<R>) new EmptyEventMessage());
        }
        final T event = eventFactory.create(eventClazz);
        final AtomicReference<EventMessage<R>> eventMessage = new AtomicReference<>();
        awaitRaised(() -> {
            eventStore.removeEventById(event, id).or(() -> eventStore.findVerifiedEventById(event, id)).ifPresent(eventMessage::set);
            return eventMessage.get() != null;
        }, event.getEventName());
        log.debug("Assert event: {}", eventMessage.get().getIdempotencyKey());
        return new EventAssertionBuilder<>(eventMessage.get());
    }

    public <R, T extends Event<R>> void assertEventNotRaised(final Class<T> eventClazz, final Predicate<? super EventMessage<R>> filter) {
        if (eventProperties.isEventVerificationDisabled()) {
            return;
        }
        final T event = eventFactory.create(eventClazz);
        assertNeverRaised(() -> eventStore.findByType(event).stream().anyMatch(filter),
                () -> unexpectedEventsMessage(event.getEventName()));
    }

    public List<BulkEventMessage> assertBulkEvent(final Predicate<BulkEventMessage> filter) {
        if (eventProperties.isEventVerificationDisabled()) {
            return List.of();
        }
        awaitRaised(() -> !eventStore.findBulkEvents(filter).isEmpty(), EventStore.BULK_BUSINESS_EVENT_TYPE);
        return eventStore.findBulkEvents(filter);
    }

    public void assertBulkEventNotRaised(final Predicate<BulkEventMessage> filter) {
        if (eventProperties.isEventVerificationDisabled()) {
            return;
        }
        assertNeverRaised(() -> !eventStore.findBulkEvents(filter).isEmpty(),
                () -> format("%s has been received, but it was unexpected. Items of the unexpected bulk events: %s",
                        EventStore.BULK_BUSINESS_EVENT_TYPE,
                        loggedLines(eventStore.findBulkEvents(filter).stream().flatMap(bulkEvent -> bulkEvent.items().stream()))));
    }

    private void awaitRaised(final Callable<Boolean> raised, final String eventName) {
        try {
            await().atMost(Duration.ofMillis(eventProperties.getWaitTimeoutInMillis())).until(raised);
        } catch (ConditionTimeoutException e) {
            Assertions.fail(eventName + " hasn't been received within " + eventProperties.getWaitTimeoutInMillis() / 1000 + " seconds");
        }
    }

    private void assertNeverRaised(final Callable<Boolean> raised, final Supplier<String> failureMessage) {
        try {
            await().atMost(Duration.ofMillis(eventProperties.getWaitTimeoutInMillis())).until(raised);
            Assertions.fail(failureMessage.get());
        } catch (ConditionTimeoutException e) {
            // This is the expected outcome here!
        }
    }

    private String unexpectedEventsMessage(final String eventName) {
        return format("%s has been received, but it was unexpected. Events received but not verified: %s", eventName,
                loggedLines(eventStore.getReceivedEvents().stream()));
    }

    private static String loggedLines(final Stream<? extends EventMessage<?>> events) {
        return events.map(LoggedEvent::new).map(LoggedEvent::toString).reduce("", (s, e) -> format("%s%s%n", s, e));
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public class EventAssertionBuilder<R> {

        private final EventMessage<R> eventMessage;

        public EventAssertionBuilder<R> isRaisedOnBusinessDate(LocalDate businessDate) {
            if (eventProperties.isEventVerificationEnabled()) {
                Assertions.assertThat(eventMessage.getBusinessDate()).isEqualTo(businessDate);
            }
            return this;
        }

        public <V> EventDataAssertionBuilder<R, V> extractingData(Function<R, V> valueExtractor) {
            V dataValue;
            if (eventProperties.isEventVerificationEnabled()) {
                dataValue = valueExtractor.apply(eventMessage.getData());
            } else {
                dataValue = null;
            }
            return new EventDataAssertionBuilder<>(eventMessage, dataValue);
        }

        public EventBigDecimalAssertionBuilder<R> extractingBigDecimal(Function<R, BigDecimal> valueExtractor) {
            BigDecimal dataValue;
            if (eventProperties.isEventVerificationEnabled()) {
                dataValue = valueExtractor.apply(eventMessage.getData());
            } else {
                dataValue = null;
            }
            return new EventBigDecimalAssertionBuilder<>(eventMessage, dataValue);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public class EventDataAssertionBuilder<R, V> {

        private final EventMessage<R> eventMessage;
        private final V extractedValue;

        public EventAssertionBuilder<R> isEqualTo(V value) {
            if (eventProperties.isEventVerificationEnabled()) {
                Assertions.assertThat(extractedValue).isEqualTo(value);
            }
            return new EventAssertionBuilder<>(eventMessage);
        }

        public EventAssertionBuilder<R> isNotEqualTo(V value) {
            if (eventProperties.isEventVerificationEnabled()) {
                Assertions.assertThat(extractedValue).isNotEqualTo(value);
            }
            return new EventAssertionBuilder<>(eventMessage);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public class EventBigDecimalAssertionBuilder<R> {

        private final EventMessage<R> eventMessage;
        private final BigDecimal extractedValue;

        public EventAssertionBuilder<R> isEqualTo(BigDecimal value) {
            if (eventProperties.isEventVerificationEnabled()) {
                if (extractedValue == null) {
                    Assertions.assertThat(extractedValue).isEqualTo(value);
                } else {
                    Assertions.assertThat(extractedValue).isEqualByComparingTo(value);
                }
            }
            return new EventAssertionBuilder<>(eventMessage);
        }

        public EventAssertionBuilder<R> isNotEqualTo(BigDecimal value) {
            if (eventProperties.isEventVerificationEnabled()) {
                Assertions.assertThat(extractedValue).isNotEqualByComparingTo(value);
            }
            return new EventAssertionBuilder<>(eventMessage);
        }
    }
}
