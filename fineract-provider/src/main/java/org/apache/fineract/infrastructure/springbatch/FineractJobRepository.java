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

import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Keeps Spring Batch's in-memory checkpoint consistent when the transaction containing its JDBC update rolls back.
 * <p>
 * Because the job repository shares the chunk's JPA transaction (see {@code ScheduledJobRunnerConfig}), a failure
 * during the final JPA commit rolls the metadata back too. Batch 6 has by then already incremented the in-memory
 * version and counters, so {@code AbstractStep} cannot persist FAILED afterwards - the database version it writes
 * against is older - and the step stays recorded as STARTED. This restores the last committed values when the
 * transaction actually rolls back, so the failure can be recorded and the step remains restartable.
 * <p>
 * The restore state is read only once a rollback has actually happened. Reading it up front would put a
 * {@code getStepExecution(..)} round trip - which fans out over the step execution, its job execution, job instance,
 * job parameters and both execution contexts - on the first repository write of every chunk, to prepare state that a
 * successful chunk never uses. Chunks overwhelmingly succeed, so the happy path now pays nothing.
 * <p>
 * This is applied as a decorator around the repository the factory bean produces, rather than by substituting the
 * target inside {@code JobRepositoryFactoryBean#getTarget()}. That method, and the {@code createXxxDao()} covariant
 * overrides needed to reproduce its DAO wiring, live on {@code JobRepositoryFactoryBean}, which is
 * {@code @Deprecated(since = "6.0", forRemoval = true)} and scheduled for removal in Batch 6.2 - and reproducing that
 * wiring by hand silently drops anything the framework later adds to it. A decorator needs none of it.
 */
@Slf4j
public final class FineractJobRepository {

    private FineractJobRepository() {}

    /**
     * Wraps {@code delegate} so that {@code update(StepExecution)} and {@code updateExecutionContext(StepExecution)}
     * restore the last committed checkpoint if their transaction rolls back. Every other call passes straight through,
     * including any method a future Batch version adds to {@link JobRepository}.
     *
     * @param transactionManager
     *            the repository's own transaction manager, used to read the committed state back after a rollback
     */
    public static JobRepository withCheckpointRollback(JobRepository delegate, PlatformTransactionManager transactionManager) {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTarget(delegate);
        proxyFactory.setInterfaces(JobRepository.class);
        proxyFactory.addAdvice(new CheckpointRollbackAdvice(delegate, readCommittedStateTemplate(transactionManager)));
        return (JobRepository) proxyFactory.getProxy(FineractJobRepository.class.getClassLoader());
    }

    /**
     * REQUIRES_NEW is not optional here. {@code afterCompletion} runs before the transaction manager cleans up, so the
     * rolled-back EntityManager and its connection are still bound to the thread; a plain read would join that dead
     * transaction. {@link TransactionSynchronization#afterCompletion(int)} says as much and prescribes REQUIRES_NEW,
     * which suspends the stale resources and reads on a connection of its own.
     */
    private static TransactionTemplate readCommittedStateTemplate(PlatformTransactionManager transactionManager) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        // The JobRepository proxy applies its own non-read-only REQUIRED definition to getStepExecution. Fineract
        // validates participating definitions, so marking this outer transaction read-only would make that repository
        // call fail before it can reload the checkpoint. This transaction only executes the repository read regardless.
        return template;
    }

    private record CheckpointRollbackAdvice(JobRepository delegate, TransactionTemplate readCommittedState) implements MethodInterceptor {

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            Object[] arguments = invocation.getArguments();
            if (arguments.length == 1 && arguments[0] instanceof StepExecution stepExecution && isCheckpointWrite(invocation)) {
                armRestore(stepExecution);
            }
            return invocation.proceed();
        }

        private boolean isCheckpointWrite(MethodInvocation invocation) {
            String name = invocation.getMethod().getName();
            return "update".equals(name) || "updateExecutionContext".equals(name);
        }

        private void armRestore(StepExecution stepExecution) {
            if (!TransactionSynchronizationManager.isSynchronizationActive()
                    || !TransactionSynchronizationManager.isActualTransactionActive()) {
                // Nothing can roll back underneath us, so there is nothing to restore.
                return;
            }
            if (isAlreadyArmed(stepExecution)) {
                // Batch writes the context and the execution in the same chunk transaction; one hook covers both.
                return;
            }
            TransactionSynchronizationManager.registerSynchronization(new CheckpointRollback(stepExecution, this::readCommitted));
        }

        private boolean isAlreadyArmed(StepExecution stepExecution) {
            // Ids are per tenant and the synchronization list is thread-local and transaction-scoped. Each
            // transaction is routed to one tenant, so ids from different tenants cannot collide here.
            return TransactionSynchronizationManager.getSynchronizations().stream()
                    .anyMatch(synchronization -> synchronization instanceof CheckpointRollback rollback
                            && rollback.current().getId() == stepExecution.getId());
        }

        private StepExecution readCommitted(long stepExecutionId) {
            return readCommittedState.execute(status -> delegate.getStepExecution(stepExecutionId));
        }
    }

    @FunctionalInterface
    private interface CommittedStateReader {

        StepExecution read(long stepExecutionId);
    }

    private record CheckpointRollback(StepExecution current, CommittedStateReader reader) implements TransactionSynchronization {

        @Override
        public void afterCompletion(int status) {
            if (status != STATUS_ROLLED_BACK) {
                return;
            }
            StepExecution previous;
            try {
                previous = reader.read(current.getId());
            } catch (RuntimeException e) {
                // Leave the failure that caused the rollback to surface rather than replacing it with this one.
                // Spring swallows anything thrown from afterCompletion, so log it where it can still be seen.
                log.error("Could not read the committed checkpoint of step execution {}; its in-memory state stays ahead of the database",
                        current.getId(), e);
                return;
            }
            if (previous == null) {
                log.warn("No persisted step execution for id {}; its checkpoint cannot be restored after rollback", current.getId());
                return;
            }
            current.setVersion(previous.getVersion());
            restoreExecutionContext(current.getExecutionContext(), previous.getExecutionContext());
            current.setReadCount(previous.getReadCount());
            current.setWriteCount(previous.getWriteCount());
            current.setFilterCount(previous.getFilterCount());
            current.setReadSkipCount(previous.getReadSkipCount());
            current.setWriteSkipCount(previous.getWriteSkipCount());
            current.setProcessSkipCount(previous.getProcessSkipCount());
            current.setCommitCount(previous.getCommitCount());
            current.setRollbackCount(previous.getRollbackCount() + 1);
        }

        /**
         * Rewrites {@code target} in place instead of replacing the instance on the step execution.
         * <p>
         * Batch hands the context out during {@code beforeStep} and components keep the reference - {@code
         * AbstractItemProcessor} stores it for the whole step - so swapping the object would leave them reading the
         * pre-rollback one while the step execution carries another. Restoring through the same instance keeps every
         * holder in agreement.
         */
        private void restoreExecutionContext(ExecutionContext target, ExecutionContext committed) {
            Set<String> committedKeys = committed.toMap().keySet();
            new HashSet<>(target.toMap().keySet()).stream().filter(key -> !committedKeys.contains(key)).forEach(target::remove);
            committed.entrySet().forEach(entry -> target.put(entry.getKey(), entry.getValue()));
            // A context just loaded from the repository is not dirty, and this one now matches the database again.
            target.clearDirtyFlag();
        }
    }
}
