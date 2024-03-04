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
package org.apache.fineract.portfolio.loanaccount.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanAdjustTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressFBWarnings(value = "RV_EXCEPTION_NOT_THROWN", justification = "False positive")
class ReplayedTransactionBusinessEventServiceIntegrationTest {

    @Mock
    private BusinessEventNotifierService businessEventNotifierService;

    @Mock
    private LoanTransactionRepository loanTransactionRepository;

    private ReplayedTransactionBusinessEventService underTest;

    @BeforeEach
    public void setUp() {
        underTest = new ReplayedTransactionBusinessEventServiceImpl(businessEventNotifierService, loanTransactionRepository);
    }

    @Test
    public void testWhenParamIsNull() {
        // given
        ChangedTransactionDetail changedTransactionDetail = null;
        // when
        underTest.raiseTransactionReplayedEvents(changedTransactionDetail);
        // then
        verify(businessEventNotifierService, Mockito.times(0)).startExternalEventRecording();
        verify(businessEventNotifierService, Mockito.times(0))
                .notifyPostBusinessEvent(Mockito.any(LoanAdjustTransactionBusinessEvent.class));
        verify(businessEventNotifierService, Mockito.times(0)).stopExternalEventRecording();
        verify(businessEventNotifierService, Mockito.times(0)).resetEventRecording();
    }

    @Test
    public void testWhenParamHasNoMapping() {
        // given
        ChangedTransactionDetail changedTransactionDetail = new ChangedTransactionDetail();
        // when
        underTest.raiseTransactionReplayedEvents(changedTransactionDetail);
        // then
        verify(businessEventNotifierService, Mockito.times(0)).startExternalEventRecording();
        verify(businessEventNotifierService, Mockito.times(0))
                .notifyPostBusinessEvent(Mockito.any(LoanAdjustTransactionBusinessEvent.class));
        verify(businessEventNotifierService, Mockito.times(0)).stopExternalEventRecording();
        verify(businessEventNotifierService, Mockito.times(0)).resetEventRecording();
    }

    @Test
    public void testWhenParamHasOneNewTransaction() {
        // given
        LoanTransaction oldLoanTransaction = Mockito.mock(LoanTransaction.class);
        LoanTransaction newLoanTransaction = Mockito.mock(LoanTransaction.class);
        lenient().when(loanTransactionRepository.findById(1L)).thenReturn(Optional.of(oldLoanTransaction));
        ChangedTransactionDetail changedTransactionDetail = new ChangedTransactionDetail();
        changedTransactionDetail.getNewTransactionMappings().put(1L, newLoanTransaction);
        // when
        underTest.raiseTransactionReplayedEvents(changedTransactionDetail);
        // then
        verify(businessEventNotifierService, Mockito.times(1)).startExternalEventRecording();
        verify(businessEventNotifierService, Mockito.times(1))
                .notifyPostBusinessEvent(Mockito.any(LoanAdjustTransactionBusinessEvent.class));
        verify(businessEventNotifierService, Mockito.times(1)).stopExternalEventRecording();
        verify(businessEventNotifierService, Mockito.times(0)).resetEventRecording();
    }

    @Test
    public void testWhenParamHasTwoNewTransaction() {
        // given
        LoanTransaction oldLoanTransaction = Mockito.mock(LoanTransaction.class);
        LoanTransaction newLoanTransaction = Mockito.mock(LoanTransaction.class);
        lenient().when(loanTransactionRepository.findById(1L)).thenReturn(Optional.of(oldLoanTransaction));
        lenient().when(loanTransactionRepository.findById(2L)).thenReturn(Optional.of(oldLoanTransaction));
        ChangedTransactionDetail changedTransactionDetail = new ChangedTransactionDetail();
        changedTransactionDetail.getNewTransactionMappings().put(1L, newLoanTransaction);
        changedTransactionDetail.getNewTransactionMappings().put(2L, newLoanTransaction);
        // when
        underTest.raiseTransactionReplayedEvents(changedTransactionDetail);
        // then
        verify(businessEventNotifierService, Mockito.times(1)).startExternalEventRecording();
        verify(businessEventNotifierService, Mockito.times(2))
                .notifyPostBusinessEvent(Mockito.any(LoanAdjustTransactionBusinessEvent.class));
        verify(businessEventNotifierService, Mockito.times(1)).stopExternalEventRecording();
        verify(businessEventNotifierService, Mockito.times(0)).resetEventRecording();
    }

    @Test
    public void testWhenParamHasError() {
        // given
        doThrow(new RuntimeException()).when(businessEventNotifierService)
                .notifyPostBusinessEvent(Mockito.any(LoanAdjustTransactionBusinessEvent.class));
        LoanTransaction oldLoanTransaction = Mockito.mock(LoanTransaction.class);
        LoanTransaction newLoanTransaction = Mockito.mock(LoanTransaction.class);
        lenient().when(loanTransactionRepository.findById(1L)).thenReturn(Optional.of(oldLoanTransaction));
        ChangedTransactionDetail changedTransactionDetail = new ChangedTransactionDetail();
        changedTransactionDetail.getNewTransactionMappings().put(1L, newLoanTransaction);
        // when
        assertThrows(RuntimeException.class, () -> underTest.raiseTransactionReplayedEvents(changedTransactionDetail));
        // then
        verify(businessEventNotifierService, Mockito.times(1)).startExternalEventRecording();
        verify(businessEventNotifierService, Mockito.times(1))
                .notifyPostBusinessEvent(Mockito.any(LoanAdjustTransactionBusinessEvent.class));
        verify(businessEventNotifierService, Mockito.times(0)).stopExternalEventRecording();
        verify(businessEventNotifierService, Mockito.times(1)).resetEventRecording();
    }
}
