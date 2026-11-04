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
package org.apache.fineract.portfolio.account.jobs.executestandinginstructions;

import static org.apache.fineract.portfolio.account.jobs.executestandinginstructions.StandingInstructionTestData.dueFixedSavingsToSavings;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.apache.fineract.portfolio.account.data.DueStandingInstruction;
import org.apache.fineract.portfolio.account.service.StandingInstructionExecutionService;
import org.apache.fineract.portfolio.savings.exception.InsufficientAccountBalanceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@ExtendWith(MockitoExtension.class)
class StandingInstructionItemListenerTest {

    @Mock
    private StandingInstructionExecutionService executionService;

    private final DueStandingInstruction due = new DueStandingInstruction(dueFixedSavingsToSavings(7L), new BigDecimal("100"),
            LocalDate.of(2026, 5, 1));

    /**
     * The record of a failure is written in a transaction of its own, so it outlives the rollback of the transfer that
     * failed. Losing it was what kept the original defect invisible: the run reverted, taking its own evidence with it.
     */
    @Test
    void aSkippedInstructionIsRecordedAgainstItsMandate() {
        new StandingInstructionItemListener(executionService).onSkipInWrite(due,
                new InsufficientAccountBalanceException("savingsaccount", BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.TEN));

        final ArgumentCaptor<String> errorLog = ArgumentCaptor.forClass(String.class);
        verify(executionService).recordFailure(eq(7L), errorLog.capture());
        assertThat(errorLog.getValue()).contains("InsufficientAccountBalance");
    }

    @Test
    void anUnexpectedFailureIsRecordedWithItsMessage() {
        new StandingInstructionItemListener(executionService).onSkipInWrite(due, new IllegalStateException("connection reset"));

        final ArgumentCaptor<String> errorLog = ArgumentCaptor.forClass(String.class);
        verify(executionService).recordFailure(eq(7L), errorLog.capture());
        assertThat(errorLog.getValue()).contains("connection reset");
    }

    /**
     * Inside the failing instruction's transaction, the record waits until that transaction has completed: until then
     * the instruction's row is still locked by its claim, and on MySQL the history insert would wait on that lock.
     */
    @Test
    void insideATransactionTheFailureIsRecordedOnceItCompletes() {
        TransactionSynchronizationManager.initSynchronization();
        try {
            new StandingInstructionItemListener(executionService).onSkipInWrite(due, new IllegalStateException("boom"));

            verifyNoInteractions(executionService);
            TransactionSynchronizationManager.getSynchronizations()
                    .forEach(synchronization -> synchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
            verify(executionService).recordFailure(eq(7L), anyString());
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    /** One instruction's history failing to write must not take the rest of the partition down with it. */
    @Test
    void aHistoryWriteThatFailsDoesNotPropagate() {
        doThrow(new IllegalStateException("history table gone")).when(executionService).recordFailure(anyLong(), anyString());

        assertThatCode(() -> new StandingInstructionItemListener(executionService).onSkipInWrite(due, new IllegalStateException("boom")))
                .doesNotThrowAnyException();
    }

    /**
     * An instruction that fails while being prepared (for example while reading what a loan owes) never reaches the
     * write, so its failure has to be recorded here too or it would leave no trace.
     */
    @Test
    void anInstructionSkippedWhileBeingPreparedIsRecordedAgainstItsMandate() {
        new StandingInstructionItemListener(executionService).onSkipInProcess(due.instruction(),
                new InsufficientAccountBalanceException("savingsaccount", BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.TEN));

        final ArgumentCaptor<String> errorLog = ArgumentCaptor.forClass(String.class);
        verify(executionService).recordFailure(eq(7L), errorLog.capture());
        assertThat(errorLog.getValue()).contains("InsufficientAccountBalance");
    }
}
