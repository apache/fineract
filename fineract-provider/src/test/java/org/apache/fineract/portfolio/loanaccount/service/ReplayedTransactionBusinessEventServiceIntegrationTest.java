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

import java.util.Optional;
import org.apache.fineract.AbstractSpringTest;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanAdjustTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

class ReplayedTransactionBusinessEventServiceIntegrationTest extends AbstractSpringTest {

    @MockBean
    private BusinessEventNotifierService businessEventNotifierService;

    @MockBean
    private LoanTransactionRepository loanTransactionRepository;

    @Autowired
    private DummyTasklet dummyTasklet;

    @Mock
    private StepContribution stepContribution;

    @Mock
    private ChunkContext chunkContext;

    @BeforeEach
    void setUp() {
        dummyTasklet.setChangedTransactionDetail(null);
    }

    @Test
    public void testWhenParamIsNull() throws Exception {
        // given
        dummyTasklet.setChangedTransactionDetail(null);
        // when
        dummyTasklet.execute(stepContribution, chunkContext);
        // then
        verify(businessEventNotifierService, Mockito.times(1)).startExternalEventRecording();
        verify(businessEventNotifierService, Mockito.times(0))
                .notifyPostBusinessEvent(Mockito.any(LoanAdjustTransactionBusinessEvent.class));
        verify(businessEventNotifierService, Mockito.times(1)).stopExternalEventRecording();
        verify(businessEventNotifierService, Mockito.times(1)).resetEventRecording();
    }

    @Test
    public void testWhenParamHasNoMapping() throws Exception {
        // given
        ChangedTransactionDetail changedTransactionDetail = new ChangedTransactionDetail();
        dummyTasklet.setChangedTransactionDetail(changedTransactionDetail);
        // when
        dummyTasklet.execute(stepContribution, chunkContext);
        // then
        verify(businessEventNotifierService, Mockito.times(1)).startExternalEventRecording();
        verify(businessEventNotifierService, Mockito.times(0))
                .notifyPostBusinessEvent(Mockito.any(LoanAdjustTransactionBusinessEvent.class));
        verify(businessEventNotifierService, Mockito.times(1)).stopExternalEventRecording();
        verify(businessEventNotifierService, Mockito.times(1)).resetEventRecording();
    }

    @Test
    public void testWhenParamHasOneNewTransaction() throws Exception {
        // given
        LoanTransaction oldLoanTransaction = Mockito.mock(LoanTransaction.class);
        LoanTransaction newLoanTransaction = Mockito.mock(LoanTransaction.class);
        lenient().when(loanTransactionRepository.findById(1L)).thenReturn(Optional.of(oldLoanTransaction));
        ChangedTransactionDetail changedTransactionDetail = new ChangedTransactionDetail();
        changedTransactionDetail.getNewTransactionMappings().put(1L, newLoanTransaction);
        dummyTasklet.setChangedTransactionDetail(changedTransactionDetail);
        // when
        dummyTasklet.execute(stepContribution, chunkContext);
        // then
        verify(businessEventNotifierService, Mockito.times(1)).startExternalEventRecording();
        verify(businessEventNotifierService, Mockito.times(1))
                .notifyPostBusinessEvent(Mockito.any(LoanAdjustTransactionBusinessEvent.class));
        verify(businessEventNotifierService, Mockito.times(1)).stopExternalEventRecording();
        verify(businessEventNotifierService, Mockito.times(1)).resetEventRecording();
    }

    @Test
    public void testWhenParamHasTwoNewTransaction() throws Exception {
        // given
        LoanTransaction oldLoanTransaction = Mockito.mock(LoanTransaction.class);
        LoanTransaction newLoanTransaction = Mockito.mock(LoanTransaction.class);
        lenient().when(loanTransactionRepository.findById(1L)).thenReturn(Optional.of(oldLoanTransaction));
        lenient().when(loanTransactionRepository.findById(2L)).thenReturn(Optional.of(oldLoanTransaction));
        ChangedTransactionDetail changedTransactionDetail = new ChangedTransactionDetail();
        changedTransactionDetail.getNewTransactionMappings().put(1L, newLoanTransaction);
        changedTransactionDetail.getNewTransactionMappings().put(2L, newLoanTransaction);
        dummyTasklet.setChangedTransactionDetail(changedTransactionDetail);
        // when
        dummyTasklet.execute(stepContribution, chunkContext);
        // then
        verify(businessEventNotifierService, Mockito.times(1)).startExternalEventRecording();
        verify(businessEventNotifierService, Mockito.times(2))
                .notifyPostBusinessEvent(Mockito.any(LoanAdjustTransactionBusinessEvent.class));
        verify(businessEventNotifierService, Mockito.times(1)).stopExternalEventRecording();
        verify(businessEventNotifierService, Mockito.times(1)).resetEventRecording();
    }

    @Test
    public void testWhenParamHasError() {
        // given
        doThrow(new RuntimeException()).when(businessEventNotifierService).notifyPostBusinessEvent(Mockito.any());
        LoanTransaction oldLoanTransaction = Mockito.mock(LoanTransaction.class);
        LoanTransaction newLoanTransaction = Mockito.mock(LoanTransaction.class);
        lenient().when(loanTransactionRepository.findById(1L)).thenReturn(Optional.of(oldLoanTransaction));
        ChangedTransactionDetail changedTransactionDetail = new ChangedTransactionDetail();
        changedTransactionDetail.getNewTransactionMappings().put(1L, newLoanTransaction);
        dummyTasklet.setChangedTransactionDetail(changedTransactionDetail);
        // when
        assertThrows(RuntimeException.class, () -> dummyTasklet.execute(stepContribution, chunkContext));
        // then
        verify(businessEventNotifierService, Mockito.times(1)).startExternalEventRecording();
        verify(businessEventNotifierService, Mockito.times(1))
                .notifyPostBusinessEvent(Mockito.any(LoanAdjustTransactionBusinessEvent.class));
        verify(businessEventNotifierService, Mockito.times(0)).stopExternalEventRecording();
        verify(businessEventNotifierService, Mockito.times(1)).resetEventRecording();
    }
}
