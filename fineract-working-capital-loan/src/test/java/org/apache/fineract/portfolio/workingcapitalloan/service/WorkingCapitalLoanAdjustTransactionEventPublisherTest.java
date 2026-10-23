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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.event.business.domain.BusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.transaction.WorkingCapitalLoanAdjustTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanTransactionData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkingCapitalLoanAdjustTransactionEventPublisherTest {

    private static final Long LOAN_ID = 11L;

    @Mock
    private WorkingCapitalLoanTransactionDataFactory transactionDataFactory;

    @Mock
    private BusinessEventNotifierService businessEventNotifierService;

    @InjectMocks
    private WorkingCapitalLoanAdjustTransactionEventPublisher publisher;

    @BeforeEach
    void runTheRecordingWindowAction() {
        // The notifier owns opening and closing the window; here it just has to run what it was handed.
        lenient().doAnswer(invocation -> {
            invocation.getArgument(0, Runnable.class).run();
            return null;
        }).when(businessEventNotifierService).withExternalEventRecording(any(Runnable.class));
    }

    @Test
    void snapshotsReturnsEmptyMapWithoutBuildingPayloadsWhenPostingIsDisabled() {
        postingEnabled(false);

        final Map<Long, WorkingCapitalLoanTransactionData> result = publisher.snapshots(List.of(mock(WorkingCapitalLoanTransaction.class)));

        assertThat(result).isEmpty();
        verifyNoInteractions(transactionDataFactory);
    }

    @Test
    void snapshotsKeepsOnePayloadPerTransactionIdWhenPostingIsEnabled() {
        postingEnabled(true);
        final List<WorkingCapitalLoanTransaction> transactions = List.of(mock(WorkingCapitalLoanTransaction.class),
                mock(WorkingCapitalLoanTransaction.class));
        final WorkingCapitalLoanTransactionData first = transaction(1L);
        final WorkingCapitalLoanTransactionData second = transaction(2L);
        when(transactionDataFactory.create(transactions)).thenReturn(List.of(first, second));

        final Map<Long, WorkingCapitalLoanTransactionData> result = publisher.snapshots(transactions);

        assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of(1L, first, 2L, second));
    }

    @Test
    void publishReversalDoesNothingWhenPostingIsDisabled() {
        postingEnabled(false);

        publisher.publishReversal(LOAN_ID, mock(WorkingCapitalLoanTransaction.class));

        verifyNoInteractions(transactionDataFactory);
        verify(businessEventNotifierService, never()).notifyPostBusinessEvent(any());
    }

    @Test
    void publishReversalPostsTheReversedTransactionAsTheTransactionToAdjust() {
        postingEnabled(true);
        final WorkingCapitalLoanTransaction reversedTransaction = mock(WorkingCapitalLoanTransaction.class);
        final WorkingCapitalLoanTransactionData snapshot = transaction(1L);
        when(transactionDataFactory.create(reversedTransaction)).thenReturn(snapshot);

        publisher.publishReversal(LOAN_ID, reversedTransaction);

        final WorkingCapitalLoanAdjustTransactionBusinessEvent event = publishedEvents(1).getFirst();
        assertThat(event.getAggregateRootId()).isEqualTo(LOAN_ID);
        assertThat(event.get().getTransactionToAdjust()).isSameAs(snapshot);
        assertThat(event.get().getNewTransactionDetail()).isNull();
    }

    @Test
    void publishReprocessedDoesNothingWhenPostingIsDisabled() {
        postingEnabled(false);

        publisher.publishReprocessed(LOAN_ID, Map.of(1L, transaction(1L)), List.of(mock(WorkingCapitalLoanTransaction.class)));

        verifyNoInteractions(transactionDataFactory);
        verify(businessEventNotifierService, never()).withExternalEventRecording(any(Runnable.class));
        verify(businessEventNotifierService, never()).notifyPostBusinessEvent(any());
    }

    @Test
    void publishReprocessedPairsTheStateBeforeAndAfterTheReplay() {
        postingEnabled(true);
        final WorkingCapitalLoanTransaction changed = changedTransaction(1L, false);
        final WorkingCapitalLoanTransactionData before = transaction(1L);
        final WorkingCapitalLoanTransactionData after = transaction(1L);
        when(transactionDataFactory.create(List.of(changed))).thenReturn(List.of(after));

        publisher.publishReprocessed(LOAN_ID, Map.of(1L, before), List.of(changed));

        final WorkingCapitalLoanAdjustTransactionBusinessEvent event = publishedEvents(1).getFirst();
        assertThat(event.getAggregateRootId()).isEqualTo(LOAN_ID);
        assertThat(event.get().getTransactionToAdjust()).isSameAs(before);
        assertThat(event.get().getNewTransactionDetail()).isSameAs(after);
    }

    @Test
    void publishReprocessedReportsAReversedTransactionAsAReversalWithoutAPreSnapshot() {
        postingEnabled(true);
        final WorkingCapitalLoanTransaction reversed = changedTransaction(2L, true);
        final WorkingCapitalLoanTransactionData after = transaction(2L);
        when(transactionDataFactory.create(List.of(reversed))).thenReturn(List.of(after));

        publisher.publishReprocessed(LOAN_ID, Map.of(), List.of(reversed));

        final WorkingCapitalLoanAdjustTransactionBusinessEvent event = publishedEvents(1).getFirst();
        assertThat(event.getAggregateRootId()).isEqualTo(LOAN_ID);
        assertThat(event.get().getTransactionToAdjust()).isSameAs(after);
        assertThat(event.get().getNewTransactionDetail()).isNull();
    }

    @Test
    void publishReprocessedSkipsATransactionThatHasNoPreSnapshot() {
        postingEnabled(true);
        final WorkingCapitalLoanTransaction withoutPreSnapshot = changedTransaction(3L, false);
        final WorkingCapitalLoanTransaction changed = changedTransaction(4L, false);
        final WorkingCapitalLoanTransactionData before = transaction(4L);
        final WorkingCapitalLoanTransactionData after = transaction(4L);
        when(transactionDataFactory.create(List.of(changed))).thenReturn(List.of(after));

        publisher.publishReprocessed(LOAN_ID, Map.of(4L, before), List.of(withoutPreSnapshot, changed));

        final WorkingCapitalLoanAdjustTransactionBusinessEvent event = publishedEvents(1).getFirst();
        assertThat(event.get().getTransactionToAdjust()).isSameAs(before);
        assertThat(event.get().getNewTransactionDetail()).isSameAs(after);
    }

    @Test
    void publishReprocessedDoesNotOpenARecordingWindowWhenNoTransactionIsLeft() {
        postingEnabled(true);

        publisher.publishReprocessed(LOAN_ID, Map.of(), List.of(changedTransaction(3L, false)));

        verify(businessEventNotifierService, never()).withExternalEventRecording(any(Runnable.class));
        verify(businessEventNotifierService, never()).notifyPostBusinessEvent(any());
    }

    @Test
    void publishReprocessedPostsInInputOrderInsideASingleRecordingWindow() {
        postingEnabled(true);
        final WorkingCapitalLoanTransaction changed = changedTransaction(1L, false);
        final WorkingCapitalLoanTransaction reversed = changedTransaction(2L, true);
        final WorkingCapitalLoanTransactionData changedBefore = transaction(1L);
        final WorkingCapitalLoanTransactionData changedAfter = transaction(1L);
        final WorkingCapitalLoanTransactionData reversedAfter = transaction(2L);
        when(transactionDataFactory.create(List.of(changed, reversed))).thenReturn(List.of(reversedAfter, changedAfter));

        publisher.publishReprocessed(LOAN_ID, Map.of(1L, changedBefore), List.of(changed, reversed));

        final List<WorkingCapitalLoanAdjustTransactionBusinessEvent> events = publishedEvents(2);
        assertThat(events.get(0).get().getTransactionToAdjust()).isSameAs(changedBefore);
        assertThat(events.get(0).get().getNewTransactionDetail()).isSameAs(changedAfter);
        assertThat(events.get(1).get().getTransactionToAdjust()).isSameAs(reversedAfter);
        assertThat(events.get(1).get().getNewTransactionDetail()).isNull();

        final InOrder inOrder = inOrder(businessEventNotifierService);
        inOrder.verify(businessEventNotifierService).withExternalEventRecording(any(Runnable.class));
        inOrder.verify(businessEventNotifierService, times(2)).notifyPostBusinessEvent(any());
    }

    @Test
    void publishReprocessedRethrowsWhenPublishingFails() {
        postingEnabled(true);
        final WorkingCapitalLoanTransaction changed = changedTransaction(1L, false);
        when(transactionDataFactory.create(List.of(changed))).thenReturn(List.of(transaction(1L)));
        final RuntimeException failure = new RuntimeException("boom");
        doThrow(failure).when(businessEventNotifierService).notifyPostBusinessEvent(any());

        assertThatThrownBy(() -> publisher.publishReprocessed(LOAN_ID, Map.of(1L, transaction(1L)), List.of(changed))).isSameAs(failure);

        final InOrder inOrder = inOrder(businessEventNotifierService);
        inOrder.verify(businessEventNotifierService).withExternalEventRecording(any(Runnable.class));
        inOrder.verify(businessEventNotifierService).notifyPostBusinessEvent(any());
    }

    private void postingEnabled(final boolean enabled) {
        when(businessEventNotifierService.isExternalEventPostingEnabled(WorkingCapitalLoanAdjustTransactionBusinessEvent.TYPE))
                .thenReturn(enabled);
    }

    private List<WorkingCapitalLoanAdjustTransactionBusinessEvent> publishedEvents(final int expectedCount) {
        final ArgumentCaptor<BusinessEvent<?>> captor = ArgumentCaptor.forClass(BusinessEvent.class);
        verify(businessEventNotifierService, times(expectedCount)).notifyPostBusinessEvent(captor.capture());
        return captor.getAllValues().stream().map(WorkingCapitalLoanAdjustTransactionBusinessEvent.class::cast).toList();
    }

    private WorkingCapitalLoanTransactionData transaction(final Long id) {
        return WorkingCapitalLoanTransactionData.builder().id(id).wcLoanId(LOAN_ID).build();
    }

    private WorkingCapitalLoanTransaction changedTransaction(final Long id, final boolean reversed) {
        final WorkingCapitalLoanTransaction transaction = mock(WorkingCapitalLoanTransaction.class);
        when(transaction.getId()).thenReturn(id);
        when(transaction.isReversed()).thenReturn(reversed);
        return transaction;
    }
}
