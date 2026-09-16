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
package org.apache.fineract.infrastructure.springbatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Covers the checkpoint restore in isolation. {@code BatchJpaTransactionTest} proves the end-to-end outcome against a
 * real database; this pins the details that outcome does not reveal - above all that the restore reuses the step
 * execution's own {@link ExecutionContext} instance.
 */
public class FineractJobRepositoryTest {

    private static final long STEP_EXECUTION_ID = 10L;

    private final JobRepository delegate = mock(JobRepository.class);
    private final JobRepository repository = FineractJobRepository.withCheckpointRollback(delegate, noOpTransactionManager());

    @AfterEach
    public void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    @Test
    public void restoresThroughTheSameExecutionContextInstance() {
        StepExecution current = stepExecution();
        ExecutionContext held = current.getExecutionContext();
        held.putInt("cursor", 100);
        current.getExecutionContext().putInt("uncommitted", 1);
        when(delegate.getStepExecution(STEP_EXECUTION_ID)).thenReturn(committed());

        rollbackAfter(() -> repository.update(current));

        assertThat(current.getExecutionContext())
                .as("components capture the context in beforeStep; swapping the instance would strand them").isSameAs(held);
        assertThat(held.getInt("cursor")).isEqualTo(42);
        assertThat(held.containsKey("uncommitted")).as("keys written by the rolled-back chunk must be dropped").isFalse();
        assertThat(held.isDirty()).as("the context now matches the database, as a freshly loaded one would").isFalse();
    }

    @Test
    public void restoresVersionAndCountersAndCountsTheRollback() {
        StepExecution current = stepExecution();
        current.setVersion(9);
        current.setWriteCount(100);
        current.setCommitCount(5);
        current.setRollbackCount(2);
        when(delegate.getStepExecution(STEP_EXECUTION_ID)).thenReturn(committed());

        rollbackAfter(() -> repository.update(current));

        assertThat(current.getVersion()).isEqualTo(3);
        assertThat(current.getWriteCount()).isZero();
        assertThat(current.getCommitCount()).isEqualTo(1);
        assertThat(current.getRollbackCount()).isEqualTo(1);
    }

    @Test
    public void readsNothingWhenTheTransactionCommits() {
        StepExecution current = stepExecution();

        withSynchronization(() -> {
            repository.update(current);
            repository.updateExecutionContext(current);
            complete(TransactionSynchronization.STATUS_COMMITTED);
        });

        verify(delegate, never()).getStepExecution(STEP_EXECUTION_ID);
    }

    @Test
    public void armsOnceForBothCheckpointWritesOfAChunk() {
        StepExecution current = stepExecution();
        when(delegate.getStepExecution(STEP_EXECUTION_ID)).thenReturn(committed());

        rollbackAfter(() -> {
            repository.updateExecutionContext(current);
            repository.update(current);
        });

        verify(delegate).getStepExecution(STEP_EXECUTION_ID);
    }

    @Test
    public void leavesStateAloneWhenTheCommittedRowIsGone() {
        StepExecution current = stepExecution();
        current.setWriteCount(100);
        when(delegate.getStepExecution(STEP_EXECUTION_ID)).thenReturn(null);

        rollbackAfter(() -> repository.update(current));

        assertThat(current.getWriteCount()).as("a missing row must not become a NullPointerException").isEqualTo(100);
    }

    @Test
    public void leavesStateAloneWhenTheCommittedRowCannotBeRead() {
        StepExecution current = stepExecution();
        current.setWriteCount(100);
        when(delegate.getStepExecution(STEP_EXECUTION_ID)).thenThrow(new IllegalStateException("no such row"));

        rollbackAfter(() -> repository.update(current));

        assertThat(current.getWriteCount()).as("the read failure must not replace the failure that caused the rollback").isEqualTo(100);
    }

    private StepExecution stepExecution() {
        JobExecution jobExecution = new JobExecution(1L, new JobInstance(1L, "job"), new JobParameters());
        return new StepExecution(STEP_EXECUTION_ID, "step", jobExecution);
    }

    /** The last committed state: cursor at 42, nothing written since, one commit. */
    private StepExecution committed() {
        StepExecution previous = stepExecution();
        previous.setVersion(3);
        previous.setCommitCount(1);
        previous.getExecutionContext().putInt("cursor", 42);
        return previous;
    }

    private void rollbackAfter(Runnable work) {
        withSynchronization(() -> {
            work.run();
            complete(TransactionSynchronization.STATUS_ROLLED_BACK);
        });
    }

    private void withSynchronization(Runnable work) {
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        try {
            work.run();
        } finally {
            TransactionSynchronizationManager.setActualTransactionActive(false);
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.clearSynchronization();
            }
        }
    }

    /** Mirrors what AbstractPlatformTransactionManager does: snapshot, clear, then notify. */
    private void complete(int status) {
        List<TransactionSynchronization> synchronizations = List.copyOf(TransactionSynchronizationManager.getSynchronizations());
        TransactionSynchronizationManager.clearSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(false);
        synchronizations.forEach(synchronization -> synchronization.afterCompletion(status));
    }

    private static PlatformTransactionManager noOpTransactionManager() {
        return new PlatformTransactionManager() {

            @Override
            public TransactionStatus getTransaction(TransactionDefinition definition) {
                return new SimpleTransactionStatus();
            }

            @Override
            public void commit(TransactionStatus status) {
                // the restore only reads; nothing to flush
            }

            @Override
            public void rollback(TransactionStatus status) {
                // the restore only reads; nothing to undo
            }
        };
    }
}
