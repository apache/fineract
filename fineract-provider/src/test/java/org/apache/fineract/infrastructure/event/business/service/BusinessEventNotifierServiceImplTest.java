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
package org.apache.fineract.infrastructure.event.business.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.event.business.BusinessEventListener;
import org.apache.fineract.infrastructure.event.business.domain.BulkBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.BusinessEvent;
import org.apache.fineract.infrastructure.event.external.service.ExternalEventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({ "rawtypes", "unchecked" })
@MockitoSettings(strictness = Strictness.LENIENT)
class BusinessEventNotifierServiceImplTest {

    @Mock
    private ExternalEventService externalEventService;
    @Mock
    private FineractProperties fineractProperties;

    @InjectMocks
    private BusinessEventNotifierServiceImpl underTest;

    @Test
    public void testNotifyPostBusinessEventShouldNotifyPostListeners() {
        // given
        configureExternalEventsProperties(false);

        MockBusinessEvent event = new MockBusinessEvent();
        BusinessEventListener<MockBusinessEvent> postListener = mockListener();
        underTest.addPostBusinessEventListener(MockBusinessEvent.class, postListener);
        // when
        underTest.notifyPostBusinessEvent(event);
        // then
        verify(postListener).onBusinessEvent(event);
        verifyNoInteractions(externalEventService);
    }

    @Test
    public void testNotifyPostBusinessEventShouldNotifyPostListenersAndPostAnExternalEvent() {
        // given
        configureExternalEventsProperties(true);

        MockBusinessEvent event = new MockBusinessEvent();
        BusinessEventListener<MockBusinessEvent> postListener = mockListener();
        underTest.addPostBusinessEventListener(MockBusinessEvent.class, postListener);
        // when
        underTest.notifyPostBusinessEvent(event);
        // then
        verify(postListener).onBusinessEvent(event);
        verify(externalEventService).postEvent(event);
    }

    @Test
    public void testNotifyPostBusinessEventShouldNotifyPostListenersAndPostAnBulkExternalEventWhenRecordingEnabled() {
        // given
        configureExternalEventsProperties(true);

        MockBusinessEvent event = new MockBusinessEvent();
        BusinessEventListener<MockBusinessEvent> postListener = mockListener();
        underTest.addPostBusinessEventListener(MockBusinessEvent.class, postListener);
        underTest.startExternalEventRecording();
        underTest.notifyPostBusinessEvent(event);
        // when
        underTest.stopExternalEventRecording();
        // then
        verify(postListener).onBusinessEvent(event);

        ArgumentCaptor<BulkBusinessEvent> argumentCaptor = ArgumentCaptor.forClass(BulkBusinessEvent.class);
        verify(externalEventService).postEvent(argumentCaptor.capture());
        BulkBusinessEvent capturedEvent = argumentCaptor.getValue();
        assertThat(capturedEvent.get()).hasSize(1);
        assertThat(capturedEvent.get().get(0)).isEqualTo(event);
    }

    @Test
    public void testNotifyPreBusinessEventShouldNotifyPreListeners() {
        // given
        configureExternalEventsProperties(false);

        MockBusinessEvent event = new MockBusinessEvent();
        BusinessEventListener<MockBusinessEvent> preListener = mockListener();
        underTest.addPreBusinessEventListener(MockBusinessEvent.class, preListener);
        // when
        underTest.notifyPreBusinessEvent(event);
        // then
        verify(preListener).onBusinessEvent(event);
        verifyNoInteractions(externalEventService);
    }

    @Test
    public void testNotifyPreBusinessEventShouldNotifyPreListenersWithoutPostingAnExternalEvent() {
        // given
        configureExternalEventsProperties(true);

        MockBusinessEvent event = new MockBusinessEvent();
        BusinessEventListener<MockBusinessEvent> preListener = mockListener();
        underTest.addPreBusinessEventListener(MockBusinessEvent.class, preListener);
        // when
        underTest.notifyPreBusinessEvent(event);
        // then
        verify(preListener).onBusinessEvent(event);
        verifyNoInteractions(externalEventService);
    }

    private void configureExternalEventsProperties(boolean isExternalEventsEnabled) {
        FineractProperties.FineractEventsProperties eventsProperties = new FineractProperties.FineractEventsProperties();
        FineractProperties.FineractExternalEventsProperties externalProperties = new FineractProperties.FineractExternalEventsProperties();
        eventsProperties.setExternal(externalProperties);
        externalProperties.setEnabled(isExternalEventsEnabled);
        given(fineractProperties.getEvents()).willReturn(eventsProperties);
    }

    private BusinessEventListener<MockBusinessEvent> mockListener() {
        return (BusinessEventListener<MockBusinessEvent>) mock(BusinessEventListener.class);
    }

    private static class MockBusinessEvent implements BusinessEvent<Object> {

        @Override
        public Object get() {
            return null;
        }

        @Override
        public String getType() {
            return null;
        }

        @Override
        public String getCategory() {
            return null;
        }
    }

}
