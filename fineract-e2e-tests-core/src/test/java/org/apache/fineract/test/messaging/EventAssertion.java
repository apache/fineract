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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
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
@SuppressFBWarnings({ "VA_FORMAT_STRING_USES_NEWLINE" })
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

    private <R, T extends Event<R>> void internalAssertEventRaised(Class<T> eventClazz, Long id, boolean removeEventIfFound) {
        if (eventProperties.isEventVerificationDisabled()) {
            return;
        }
        T event = eventFactory.create(eventClazz);
        try {
            await().atMost(Duration.ofSeconds(eventProperties.getEventWaitTimeoutInSec())).until(() -> {
                if (removeEventIfFound) {
                    return eventStore.removeEventById(event, id).isPresent();
                } else {
                    return eventStore.findEventById(event, id).isPresent();
                }
            });
        } catch (ConditionTimeoutException e) {
            Assertions
                    .fail(event.getEventName() + " hasn't been received within " + eventProperties.getEventWaitTimeoutInSec() + " seconds");
        }
    }

    private <R, T extends Event<R>> void internalAssertEventNotRaised(Class<T> eventClazz, Long id) {
        if (eventProperties.isEventVerificationDisabled()) {
            return;
        }
        T event = eventFactory.create(eventClazz);
        try {
            await().atMost(Duration.ofSeconds(eventProperties.getEventWaitTimeoutInSec())).until(() -> {
                return eventStore.existsEventById(event, id);
            });

            String receivedEventsLogParam = eventStore.getReceivedEvents().stream().map(LoggedEvent::new).map(LoggedEvent::toString)
                    .reduce("", (s, e) -> format("%s%s%n", s, e));
            Assertions.fail("""
                    %s has been received, but it was unexpected.
                    Events received but not verified:
                    %s
                    """.formatted(event.getEventName(), receivedEventsLogParam));
        } catch (ConditionTimeoutException e) {
            // This is the expected outcome here!
        }
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
        return new EventAssertionBuilder<>(eventMessage);
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
                Assertions.assertThat(extractedValue).isEqualByComparingTo(value);
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
