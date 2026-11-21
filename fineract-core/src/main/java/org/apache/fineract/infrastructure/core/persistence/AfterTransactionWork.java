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
package org.apache.fineract.infrastructure.core.persistence;

import java.util.ArrayDeque;
import java.util.Deque;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Runs work once the current transaction has completed and has given back its connection.
 * <p>
 * A {@link TransactionSynchronization#afterCompletion(int)} callback is too early for work that opens a transaction of
 * its own: it runs while the completed transaction's connection is still bound and checked out, so the new transaction
 * needs a second connection from the pool - and with every worker of a saturated pool holding one, it waits for the
 * pool's timeout. Work registered here is instead run by {@link ExtendedJpaTransactionManager} and
 * {@link ExtendedDataSourceTransactionManager} at the very end of their cleanup, after the connection has been released
 * and the transaction's row locks are gone, in the order it was registered.
 * <p>
 * Without an active transaction synchronization the work runs at once. A failure of one piece of work is logged and
 * does not prevent the others from running, nor reaches the transaction manager.
 * <p>
 * Work that completes under a transaction no Fineract transaction manager cleans up - one run by another manager, or a
 * scope that has synchronization but no actual transaction - waits on its thread until the next Fineract transaction
 * there cleans up, or until a caller drains it with {@link #runReadyWork()} at a point of its own, as the COB writers
 * do at the end of their step. Pooled threads serve every tenant, so the work keeps the tenant and data source context
 * it was registered under and runs in it, whichever tenant the thread serves when it runs.
 */
@Slf4j
public final class AfterTransactionWork {

    private static final ThreadLocal<Deque<Runnable>> READY = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<Boolean> RUNNING = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private AfterTransactionWork() {}

    /**
     * Runs {@code work} after the current transaction has completed, whatever its outcome, or at once when there is no
     * transaction synchronization on this thread.
     */
    public static void runAfterCompletion(Runnable work) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            runSafely(work);
            return;
        }
        Runnable inRegisteringContext = inContext(ThreadLocalContextUtil.getTenant(), ThreadLocalContextUtil.getDataSourceContext(), work);
        // Held by the transaction's own synchronization until that transaction completes: work registered by an outer
        // transaction must not run when an inner one, started inside it, completes first.
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void afterCompletion(int status) {
                READY.get().addLast(inRegisteringContext);
            }
        });
    }

    /**
     * Runs the work of the transactions that have completed on this thread. Called by the Fineract transaction managers
     * once a transaction's resources have been cleaned up, and by callers that must not leave work behind on the
     * thread, at a point where no transaction is open, such as the end of a batch step.
     */
    public static void runReadyWork() {
        if (RUNNING.get()) {
            // Work that opens a transaction of its own reaches here again when that transaction cleans up; the outer
            // call carries on with the queue.
            return;
        }
        Deque<Runnable> ready = READY.get();
        if (ready.isEmpty()) {
            READY.remove();
            return;
        }
        RUNNING.set(Boolean.TRUE);
        try {
            for (Runnable work = ready.pollFirst(); work != null; work = ready.pollFirst()) {
                runSafely(work);
            }
        } finally {
            RUNNING.remove();
            READY.remove();
        }
    }

    private static Runnable inContext(FineractPlatformTenant tenant, String dataSourceContext, Runnable work) {
        return () -> {
            FineractPlatformTenant previousTenant = ThreadLocalContextUtil.getTenant();
            String previousDataSourceContext = ThreadLocalContextUtil.getDataSourceContext();
            ThreadLocalContextUtil.setTenant(tenant);
            ThreadLocalContextUtil.setDataSourceContext(dataSourceContext);
            try {
                work.run();
            } finally {
                ThreadLocalContextUtil.setTenant(previousTenant);
                ThreadLocalContextUtil.setDataSourceContext(previousDataSourceContext);
            }
        };
    }

    private static void runSafely(Runnable work) {
        try {
            work.run();
        } catch (RuntimeException e) {
            log.error("Work scheduled to run after a transaction failed", e);
        }
    }
}
