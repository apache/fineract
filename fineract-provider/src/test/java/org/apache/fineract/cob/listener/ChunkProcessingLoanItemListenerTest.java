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
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.cob.domain.LockingService;
import org.apache.fineract.cob.exceptions.COBAccountFailedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.core.retry.RetryException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
class ChunkProcessingLoanItemListenerTest {

    @Mock
    private LockingService lockingService;
    @Mock
    private PlatformTransactionManager transactionManager;

    private ChunkProcessingLoanItemListener listener;

    @BeforeEach
    void setUp() {
        listener = new ChunkProcessingLoanItemListener(lockingService, new TransactionTemplate(transactionManager));
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clear();
        }
    }

    @Test
    void skipInWriteRecordsTheLoansOwnFailure() {
        listener.onSkipInWrite(1L, new IllegalStateException("business step failed for this loan"));

        ArgumentCaptor<String> stacktrace = ArgumentCaptor.forClass(String.class);
        verify(lockingService).updateLockError(eq(1L), eq(LockOwner.LOAN_COB_CHUNK_PROCESSING), eq("Loan (id: 1) writing is failed"),
                stacktrace.capture());
        assertThat(stacktrace.getValue()).contains("business step failed for this loan");
        verify(transactionManager).commit(any());
    }

    @Test
    void aFailedChunkSaysItsLoansWereRolledBackWithIt() {
        listener.onWriteError(new IllegalStateException("chunk failed"), new Chunk<>(List.of(1L, 2L)));

        verify(lockingService).updateLockError(eq(1L), eq(LockOwner.LOAN_COB_CHUNK_PROCESSING),
                eq("Loan (id: 1) was rolled back with its failed chunk"), anyString());
        verify(lockingService).updateLockError(eq(2L), eq(LockOwner.LOAN_COB_CHUNK_PROCESSING),
                eq("Loan (id: 2) was rolled back with its failed chunk"), anyString());
    }

    @Test
    void aFailedSingleLoanChunkIsThatLoansOwnFailure() {
        listener.onWriteError(new IllegalStateException("loan failed"), new Chunk<>(List.of(3L)));

        verify(lockingService).updateLockError(eq(3L), eq(LockOwner.LOAN_COB_CHUNK_PROCESSING), eq("Loan (id: 3) writing is failed"),
                anyString());
    }

    @Test
    void aChunkAboutToBeScannedRecordsNothingSoNoneOfItsAccountsBecomesOverrulable() {
        // Fault-tolerant steps report a failed chunk wrapped in a RetryException, then write it again account by
        // account.
        listener.onWriteError(new RetryException("retry exhausted", new IllegalStateException("chunk failed")),
                new Chunk<>(List.of(1L, 2L, 3L)));

        verifyNoInteractions(lockingService, transactionManager);
    }

    @Test
    void aChunkThatIsNotScannedRecordsTheFailingAccountsOwnErrorAndTheChunkErrorOnTheOthers() {
        // Inline COB: no scan follows, so the writer naming the account is the only way to single it out.
        listener.onWriteError(new COBAccountFailedException(2L, new IllegalStateException("business step failed for loan 2")),
                new Chunk<>(List.of(1L, 2L, 3L)));

        ArgumentCaptor<String> stacktrace = ArgumentCaptor.forClass(String.class);
        verify(lockingService).updateLockError(eq(2L), eq(LockOwner.LOAN_COB_CHUNK_PROCESSING), eq("Loan (id: 2) writing is failed"),
                stacktrace.capture());
        assertThat(stacktrace.getValue()).startsWith(IllegalStateException.class.getName()).contains("business step failed for loan 2");
        verify(lockingService).updateLockError(eq(1L), eq(LockOwner.LOAN_COB_CHUNK_PROCESSING),
                eq("Loan (id: 1) was rolled back with its failed chunk"), anyString());
        verify(lockingService).updateLockError(eq(3L), eq(LockOwner.LOAN_COB_CHUNK_PROCESSING),
                eq("Loan (id: 3) was rolled back with its failed chunk"), anyString());
    }

    @Test
    void aChunkErrorAfterAWriteErrorThatWasAlreadyRecordedRecordsNothingMore() {
        listener.onChunkError(new IllegalStateException("chunk failed"), new Chunk<>(List.of(1L, 2L)));

        verifyNoInteractions(lockingService, transactionManager);
    }

    @Test
    void aFaultTolerantChunkThatCannotBeScannedIsRecordedWhenTheStepGivesUpOnIt() {
        listener.onChunkError(
                new RetryException("retry exhausted", new COBAccountFailedException(2L, new IllegalStateException("loan 2 failed"))),
                new Chunk<>(List.of(1L, 2L)));

        verify(lockingService).updateLockError(eq(2L), eq(LockOwner.LOAN_COB_CHUNK_PROCESSING), eq("Loan (id: 2) writing is failed"),
                anyString());
        verify(lockingService).updateLockError(eq(1L), eq(LockOwner.LOAN_COB_CHUNK_PROCESSING),
                eq("Loan (id: 1) was rolled back with its failed chunk"), anyString());
    }

    @Test
    void theAccountThatExceedsTheSkipLimitGetsItsOwnError() {
        // Batch raises this from its skip decision in scan mode, calls no skip or write callback, and passes an empty
        // chunk.
        listener.onChunkError(
                new SkipLimitExceededException(1, new COBAccountFailedException(4L, new IllegalStateException("loan 4 failed"))),
                new Chunk<>());

        ArgumentCaptor<String> stacktrace = ArgumentCaptor.forClass(String.class);
        verify(lockingService).updateLockError(eq(4L), eq(LockOwner.LOAN_COB_CHUNK_PROCESSING), eq("Loan (id: 4) writing is failed"),
                stacktrace.capture());
        assertThat(stacktrace.getValue()).contains("loan 4 failed");
    }

    @Test
    void insideATransactionTheErrorIsNotSavedBeforeTheTransactionCompletes() {
        TransactionSynchronizationManager.initSynchronization();

        listener.onWriteError(new IllegalStateException("chunk failed"), new Chunk<>(List.of(1L, 2L)));
        listener.onSkipInWrite(1L, new IllegalStateException("loan failed"));

        // Saved by the transaction manager once the chunk transaction has released its connection and row locks.
        verifyNoInteractions(lockingService, transactionManager);
        assertThat(TransactionSynchronizationManager.getSynchronizations()).hasSize(2);
    }

    @Test
    void aFailureToSaveTheErrorNeverEscapesTheWriteErrorCallback() {
        // Batch 6 calls onWriteError before deciding to enter scan mode; an exception from here would fail the step.
        doThrow(new UnexpectedRollbackException("rolled back")).when(lockingService).updateLockError(anyLong(),
                eq(LockOwner.LOAN_COB_CHUNK_PROCESSING), anyString(), anyString());

        assertThatNoException()
                .isThrownBy(() -> listener.onWriteError(new IllegalStateException("chunk failed"), new Chunk<>(List.of(1L))));
    }

    @Test
    void aFailureToSaveTheErrorNeverEscapesTheSkipCallback() {
        doThrow(new UnexpectedRollbackException("rolled back")).when(transactionManager).commit(any());

        assertThatNoException().isThrownBy(() -> listener.onSkipInWrite(1L, new IllegalStateException("loan failed")));
    }
}
