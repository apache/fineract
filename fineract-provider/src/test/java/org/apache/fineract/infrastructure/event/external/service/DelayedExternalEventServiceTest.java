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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.apache.fineract.portfolio.businessevent.domain.BulkBusinessEvent;
import org.apache.fineract.portfolio.businessevent.domain.BusinessEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({ "rawtypes", "unchecked" })
class DelayedExternalEventServiceTest {

    @Mock
    private ExternalEventService delegate;

    @InjectMocks
    private DelayedExternalEventService underTest;

    @BeforeEach
    public void setUp() {
        underTest.clearEnqueuedEvents();
    }

    @Test
    public void testEnqueueEventFailsWhenNullEventIsGiven() {
        // given
        // when & then
        assertThatThrownBy(() -> underTest.enqueueEvent(null)).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testEnqueueEventWorks() {
        // given
        BusinessEvent event = mock(BusinessEvent.class);
        // when
        underTest.enqueueEvent(event);
        // then
        List<BusinessEvent<?>> enqueuedEvents = underTest.getEnqueuedEvents();
        assertThat(enqueuedEvents).hasSize(1);
        assertThat(enqueuedEvents.get(0)).isEqualTo(event);
    }

    @Test
    public void testHasEnqueuedEventsReturnsTrueWhenEventIsEnqueued() {
        // given
        BusinessEvent event = mock(BusinessEvent.class);
        underTest.enqueueEvent(event);
        // when
        boolean result = underTest.hasEnqueuedEvents();
        // then
        assertThat(result).isTrue();
    }

    @Test
    public void testHasEnqueuedEventsReturnsFalseWhenNoEventIsEnqueued() {
        // given
        // when
        boolean result = underTest.hasEnqueuedEvents();
        // then
        assertThat(result).isFalse();
    }

    @Test
    public void testPostEnqueuedEventsFailsWhenNoEventIsEnqueued() {
        // given
        // when & then
        assertThatThrownBy(() -> underTest.postEnqueuedEvents()).isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void testPostEnqueuedEventsWorks() {
        // given
        ArgumentCaptor<BusinessEvent> delegateEventCaptor = ArgumentCaptor.forClass(BusinessEvent.class);

        BusinessEvent event = mock(BusinessEvent.class);
        underTest.enqueueEvent(event);
        // when
        underTest.postEnqueuedEvents();
        // then
        verify(delegate).postEvent(delegateEventCaptor.capture());
        BusinessEvent delegateEvent = delegateEventCaptor.getValue();
        assertThat(delegateEvent).isExactlyInstanceOf(BulkBusinessEvent.class);
        BulkBusinessEvent bulkDelegateEvent = (BulkBusinessEvent) delegateEvent;
        List<BusinessEvent<?>> enqueuedEvents = bulkDelegateEvent.get();
        assertThat(enqueuedEvents).hasSize(1);
        assertThat(enqueuedEvents.get(0)).isEqualTo(event);
    }
}
