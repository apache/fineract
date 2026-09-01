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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashMap;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.BusinessEventListener;
import org.apache.fineract.infrastructure.event.business.domain.BulkBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.BusinessEvent;
import org.apache.fineract.infrastructure.event.external.service.ExternalEventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.transaction.TransactionExecution;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({ "rawtypes", "unchecked" })
@MockitoSettings(strictness = Strictness.LENIENT)
class BusinessEventNotifierServiceImplTest {

    @Mock
    private ExternalEventService externalEventService;

    @Mock
    private ExternalBusinessEventConfigurationService externalBusinessEventConfigurationService;

    @Mock
    private FineractProperties fineractProperties;

    @Mock
    private TransactionHelper transactionHelper;

    @InjectMocks
    private BusinessEventNotifierServiceImpl underTest;

    @Test
    public void testNotifyPostBusinessEventShouldCollectEventsWithinTransaction() {
        // given
        setBusinessDate();
        configureExternalEventsProperties(true);
        MockBusinessEvent event = new MockBusinessEvent();
        BusinessEventListener<MockBusinessEvent> postListener = mockListener();
        underTest.addPostBusinessEventListener(MockBusinessEvent.class, postListener);
        TransactionExecution mockTransaction = mock(TransactionExecution.class);
        underTest.afterBegin(mockTransaction, null);
        // when
        when(externalBusinessEventConfigurationService.isExternalEventConfiguredForPosting(Mockito.any())).thenReturn(true);
        when(transactionHelper.hasTransaction()).thenReturn(true);
        underTest.notifyPostBusinessEvent(event);
        // then
        verify(postListener).onBusinessEvent(event);
        verifyNoInteractions(externalEventService);
        // simulate finish transaction
        underTest.beforeCommit(mockTransaction);
        verify(externalEventService).postEvent(event);
        underTest.afterCommit(mockTransaction, null);
        verifyNoInteractions(mockTransaction);
    }

    private void setBusinessDate() {
        HashMap<BusinessDateType, LocalDate> map = new HashMap<>(2);
        map.put(BusinessDateType.BUSINESS_DATE, LocalDate.of(2023, 2, 1));
        map.put(BusinessDateType.COB_DATE, LocalDate.of(2023, 1, 31));
        ThreadLocalContextUtil.setBusinessDates(map);
    }

    @Test
    public void testNotifyPostBusinessEventShouldCollectEventsWithinTransactionInNestedTransaction() {
        // given
        setBusinessDate();
        configureExternalEventsProperties(true);
        MockBusinessEvent event = new MockBusinessEvent();
        MockBusinessEvent nestedEvent = new MockBusinessEvent();
        BusinessEventListener<MockBusinessEvent> postListener = mockListener();
        underTest.addPostBusinessEventListener(MockBusinessEvent.class, postListener);
        TransactionExecution mockTransaction = mock(TransactionExecution.class);
        // when
        when(externalBusinessEventConfigurationService.isExternalEventConfiguredForPosting(Mockito.any())).thenReturn(true);
        when(transactionHelper.hasTransaction()).thenReturn(true);

        // simulate outer transaction
        underTest.afterBegin(mockTransaction, null);
        underTest.notifyPostBusinessEvent(event);
        verify(postListener).onBusinessEvent(event);
        verifyNoInteractions(externalEventService);
        // simulate nested transaction
        underTest.afterBegin(mockTransaction, null);
        underTest.notifyPostBusinessEvent(nestedEvent);
        verify(postListener).onBusinessEvent(nestedEvent);
        verifyNoInteractions(externalEventService);
        // simulate commit nested transaction
        underTest.beforeCommit(mockTransaction);
        underTest.afterCommit(mockTransaction, null);
        verify(externalEventService).postEvent(nestedEvent);
        // simulate commit outer transaction
        underTest.beforeCommit(mockTransaction);
        verify(externalEventService).postEvent(event);
        underTest.afterCommit(mockTransaction, null);
        verifyNoInteractions(mockTransaction);
    }

    @Test
    public void testNotifyPostBusinessEventShouldCollectEventsWithinTransactionInNestedRollbackTransaction() {
        // given
        setBusinessDate();
        configureExternalEventsProperties(true);
        MockBusinessEvent event = new MockBusinessEvent();
        MockBusinessEvent nestedEvent = new MockBusinessEvent();
        BusinessEventListener<MockBusinessEvent> postListener = mockListener();
        underTest.addPostBusinessEventListener(MockBusinessEvent.class, postListener);
        TransactionExecution mockTransaction = mock(TransactionExecution.class);
        // when
        when(externalBusinessEventConfigurationService.isExternalEventConfiguredForPosting(Mockito.any())).thenReturn(true);
        when(transactionHelper.hasTransaction()).thenReturn(true);

        // simulate outer transaction
        underTest.afterBegin(mockTransaction, null);
        underTest.notifyPostBusinessEvent(event);
        verify(postListener).onBusinessEvent(event);
        verifyNoInteractions(externalEventService);
        // simulate nested transaction
        underTest.afterBegin(mockTransaction, null);
        underTest.notifyPostBusinessEvent(nestedEvent);
        verify(postListener).onBusinessEvent(nestedEvent);
        verifyNoInteractions(externalEventService);
        // simulate commit nested transaction
        underTest.afterRollback(mockTransaction, null);
        verifyNoInteractions(externalEventService);
        // simulate commit outer transaction
        underTest.beforeCommit(mockTransaction);
        verify(externalEventService).postEvent(event);
        underTest.afterCommit(mockTransaction, null);
        verifyNoInteractions(mockTransaction);
    }

    @Test
    public void testNotifyPostBusinessEventShouldCollectEventsWithinTransactionAndNotSendExternalOnRollback() {
        // given
        setBusinessDate();
        configureExternalEventsProperties(true);
        MockBusinessEvent event = new MockBusinessEvent();
        BusinessEventListener<MockBusinessEvent> postListener = mockListener();
        underTest.addPostBusinessEventListener(MockBusinessEvent.class, postListener);
        TransactionExecution mockTransaction = mock(TransactionExecution.class);
        underTest.afterBegin(mockTransaction, null);
        // when
        when(externalBusinessEventConfigurationService.isExternalEventConfiguredForPosting(Mockito.any())).thenReturn(true);
        when(transactionHelper.hasTransaction()).thenReturn(true);
        underTest.notifyPostBusinessEvent(event);
        // then
        verify(postListener).onBusinessEvent(event);
        verifyNoInteractions(externalEventService);
        // simulate rollback transaction
        verifyNoInteractions(externalEventService);
        underTest.afterRollback(mockTransaction, null);
        verifyNoInteractions(externalEventService);
        verifyNoInteractions(mockTransaction);
    }

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

        when(transactionHelper.hasTransaction()).thenReturn(false);
        when(externalBusinessEventConfigurationService.isExternalEventConfiguredForPosting(Mockito.any())).thenReturn(true);
        // when
        underTest.notifyPostBusinessEvent(event);
        // then
        verify(postListener).onBusinessEvent(event);
        verify(externalEventService).postEvent(event);
    }

    @Test
    public void testNotifyPostBusinessEventShouldNotPostAnythingWhenNoEventWasRaisedExternalEventWhenRecordingEnabled() {
        // given
        configureExternalEventsProperties(true);
        when(externalBusinessEventConfigurationService.isExternalEventConfiguredForPosting(Mockito.any())).thenReturn(true);
        // when
        underTest.withExternalEventRecording(() -> {
            // blank on purpose
        });
        // then
        verify(externalEventService, never()).postEvent(any());
    }

    @Test
    public void testNotifyPostBusinessEventShouldNotifyPostListenersAndPostARegularExternalEventWhenRecordingEnabled() {
        // given
        configureExternalEventsProperties(true);
        when(externalBusinessEventConfigurationService.isExternalEventConfiguredForPosting(Mockito.any())).thenReturn(true);
        MockBusinessEvent event = new MockBusinessEvent();
        BusinessEventListener<MockBusinessEvent> postListener = mockListener();
        underTest.addPostBusinessEventListener(MockBusinessEvent.class, postListener);
        // when
        underTest.withExternalEventRecording(() -> underTest.notifyPostBusinessEvent(event));
        // then
        verify(postListener).onBusinessEvent(event);
        verify(externalEventService).postEvent(event);
    }

    @Test
    public void testNotifyPostBusinessEventShouldNotifyPostListenersAndPostAnBulkExternalEventWhenRecordingEnabled() {
        // given
        configureExternalEventsProperties(true);
        when(externalBusinessEventConfigurationService.isExternalEventConfiguredForPosting(Mockito.any())).thenReturn(true);
        MockBusinessEvent event = new MockBusinessEvent();
        MockBusinessEvent event2 = new MockBusinessEvent();
        BusinessEventListener<MockBusinessEvent> postListener = mockListener();
        underTest.addPostBusinessEventListener(MockBusinessEvent.class, postListener);
        // when
        underTest.withExternalEventRecording(() -> {
            underTest.notifyPostBusinessEvent(event);
            underTest.notifyPostBusinessEvent(event2);
        });
        // then
        verify(postListener).onBusinessEvent(event);
        verify(postListener).onBusinessEvent(event2);

        ArgumentCaptor<BulkBusinessEvent> argumentCaptor = ArgumentCaptor.forClass(BulkBusinessEvent.class);
        verify(externalEventService).postEvent(argumentCaptor.capture());
        BulkBusinessEvent capturedEvent = argumentCaptor.getValue();
        assertThat(capturedEvent.get()).hasSize(2);
        assertThat(capturedEvent.get().get(0)).isEqualTo(event);
        assertThat(capturedEvent.get().get(1)).isEqualTo(event2);
    }

    @Test
    public void testNestedRecordingWindowShouldMergeIntoASingleBulkEventInsteadOfFlushingEarly() {
        // given
        configureExternalEventsProperties(true);
        when(externalBusinessEventConfigurationService.isExternalEventConfiguredForPosting(Mockito.any())).thenReturn(true);
        MockBusinessEvent beforeNested = new MockBusinessEvent();
        MockBusinessEvent nested = new MockBusinessEvent();
        MockBusinessEvent afterNested = new MockBusinessEvent();
        // when
        underTest.withExternalEventRecording(() -> {
            underTest.notifyPostBusinessEvent(beforeNested);
            underTest.withExternalEventRecording(() -> underTest.notifyPostBusinessEvent(nested));
            underTest.notifyPostBusinessEvent(afterNested);
        });
        // then the inner window posts nothing of its own and the events raised after it are still recorded
        ArgumentCaptor<BulkBusinessEvent> argumentCaptor = ArgumentCaptor.forClass(BulkBusinessEvent.class);
        verify(externalEventService).postEvent(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().get()).containsExactly(beforeNested, nested, afterNested);
    }

    @Test
    public void testNestedRecordingWindowShouldNotPostAnythingOfItsOwn() {
        // given
        configureExternalEventsProperties(true);
        when(externalBusinessEventConfigurationService.isExternalEventConfiguredForPosting(Mockito.any())).thenReturn(true);
        MockBusinessEvent nested = new MockBusinessEvent();
        // when
        underTest.withExternalEventRecording(() -> underTest.withExternalEventRecording(() -> {
            underTest.notifyPostBusinessEvent(nested);
            // nothing may be posted while the enclosing window is still open
            verify(externalEventService, never()).postEvent(any());
        }));
        // then
        verify(externalEventService).postEvent(nested);
    }

    @Test
    public void testFailingRecordingWindowShouldAbandonTheRecordingAndRethrow() {
        // given
        configureExternalEventsProperties(true);
        when(externalBusinessEventConfigurationService.isExternalEventConfiguredForPosting(Mockito.any())).thenReturn(true);
        MockBusinessEvent event = new MockBusinessEvent();
        RuntimeException failure = new RuntimeException("boom");
        // when
        assertThatThrownBy(() -> underTest.withExternalEventRecording(() -> {
            underTest.notifyPostBusinessEvent(event);
            throw failure;
        })).isSameAs(failure);
        // then
        verify(externalEventService, never()).postEvent(any());
    }

    @Test
    public void testFailingNestedRecordingWindowShouldAbandonTheWholeRecording() {
        // given
        configureExternalEventsProperties(true);
        when(externalBusinessEventConfigurationService.isExternalEventConfiguredForPosting(Mockito.any())).thenReturn(true);
        MockBusinessEvent outerEvent = new MockBusinessEvent();
        RuntimeException failure = new RuntimeException("boom");
        // when
        assertThatThrownBy(() -> underTest.withExternalEventRecording(() -> {
            underTest.notifyPostBusinessEvent(outerEvent);
            underTest.withExternalEventRecording(() -> {
                throw failure;
            });
        })).isSameAs(failure);
        // then
        verify(externalEventService, never()).postEvent(any());
    }

    @Test
    public void testEnclosingWindowShouldSurviveANestedWindowWhoseFailureIsCaught() {
        // given
        configureExternalEventsProperties(true);
        when(externalBusinessEventConfigurationService.isExternalEventConfiguredForPosting(Mockito.any())).thenReturn(true);
        MockBusinessEvent beforeNested = new MockBusinessEvent();
        MockBusinessEvent abandoned = new MockBusinessEvent();
        MockBusinessEvent afterNested = new MockBusinessEvent();
        // when a frame between the two windows swallows the failure and carries on
        underTest.withExternalEventRecording(() -> {
            underTest.notifyPostBusinessEvent(beforeNested);
            try {
                underTest.withExternalEventRecording(() -> {
                    underTest.notifyPostBusinessEvent(abandoned);
                    throw new RuntimeException("boom");
                });
            } catch (RuntimeException expected) {
                // deliberately swallowed: the enclosing window must still be open and still hold its own events
            }
            underTest.notifyPostBusinessEvent(afterNested);
        });
        // then the enclosing window still posts, without the abandoned window's event
        ArgumentCaptor<BulkBusinessEvent> argumentCaptor = ArgumentCaptor.forClass(BulkBusinessEvent.class);
        verify(externalEventService).postEvent(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().get()).containsExactly(beforeNested, afterNested);
    }

    @Test
    public void testARecordingWindowShouldBeUsableAgainAfterAPreviousOneFailed() {
        // given
        configureExternalEventsProperties(true);
        when(externalBusinessEventConfigurationService.isExternalEventConfiguredForPosting(Mockito.any())).thenReturn(true);
        MockBusinessEvent event = new MockBusinessEvent();
        assertThatThrownBy(() -> underTest.withExternalEventRecording(() -> {
            throw new RuntimeException("boom");
        })).isInstanceOf(RuntimeException.class);
        // when the depth counter must have been unwound, so this window behaves like a fresh outermost one
        underTest.withExternalEventRecording(() -> underTest.notifyPostBusinessEvent(event));
        // then
        verify(externalEventService).postEvent(event);
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

    @Test
    public void testNotifyPostBusinessEventShouldNotifyPostListenersAndShouldNotPostAnExternalEventIfNotConfiguredForPosting() {
        // given
        configureExternalEventsProperties(true);
        when(externalBusinessEventConfigurationService.isExternalEventConfiguredForPosting(Mockito.any())).thenReturn(false);
        MockBusinessEvent event = new MockBusinessEvent();
        BusinessEventListener<MockBusinessEvent> postListener = mockListener();
        underTest.addPostBusinessEventListener(MockBusinessEvent.class, postListener);
        // when
        underTest.notifyPostBusinessEvent(event);
        // then
        verify(postListener).onBusinessEvent(event);
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

    private static final class MockBusinessEvent implements BusinessEvent<Object> {

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

        @Override
        public Long getAggregateRootId() {
            return null;
        }
    }

}
