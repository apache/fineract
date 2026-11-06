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
package org.apache.fineract.cob.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.cob.domain.LockingService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
class ChunkProcessingLoanItemListenerTest {

    @Mock
    private LockingService lockingService;
    @Mock
    private PlatformTransactionManager transactionManager;
    @Mock
    private Loan loan;

    private ChunkProcessingLoanItemListener listener;

    @BeforeEach
    void setUp() {
        listener = new ChunkProcessingLoanItemListener(lockingService, new TransactionTemplate(transactionManager));
    }

    @Test
    void skipInWriteRecordsTheLoansOwnFailure() {
        when(loan.getId()).thenReturn(1L);

        listener.onSkipInWrite(loan, new IllegalStateException("business step failed for this loan"));

        ArgumentCaptor<String> stacktrace = ArgumentCaptor.forClass(String.class);
        verify(lockingService).updateLockError(eq(1L), eq(LockOwner.LOAN_COB_CHUNK_PROCESSING), eq("Loan (id: 1) writing is failed"),
                stacktrace.capture());
        assertThat(stacktrace.getValue()).contains("business step failed for this loan");
        verify(transactionManager).commit(any());
    }

    @Test
    void aFailureToSaveTheErrorNeverEscapesTheWriteErrorCallback() {
        // Batch 6 calls onWriteError before deciding to enter scan mode; an exception from here would fail the step.
        when(loan.getId()).thenReturn(1L);
        doThrow(new UnexpectedRollbackException("rolled back")).when(lockingService).updateLockError(anyLong(), eq(LockOwner.LOAN_COB_CHUNK_PROCESSING),
                anyString(), anyString());

        assertThatNoException().isThrownBy(() -> listener.onWriteError(new IllegalStateException("chunk failed"), new Chunk<>(List.of(loan))));
    }

    @Test
    void aFailureToSaveTheErrorNeverEscapesTheSkipCallback() {
        when(loan.getId()).thenReturn(1L);
        doThrow(new UnexpectedRollbackException("rolled back")).when(transactionManager).commit(any());

        assertThatNoException().isThrownBy(() -> listener.onSkipInWrite(loan, new IllegalStateException("loan failed")));
    }
}
