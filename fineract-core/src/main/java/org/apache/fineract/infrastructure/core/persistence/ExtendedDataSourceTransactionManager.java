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

import jakarta.persistence.EntityTransaction;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import lombok.Getter;
import org.jspecify.annotations.NonNull;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * The JDBC transaction manager, made to recognise the JPA transaction it shares its {@code DataSource} with.
 * <p>
 * The JPA transaction manager exposes the JDBC connection of its transaction as a {@link ConnectionHolder} bound to the
 * same {@code DataSource}, so that plain JDBC code joins the JPA transaction. It marks that holder neither active nor
 * synchronised, though, and a plain {@link DataSourceTransactionManager} therefore does not see an existing
 * transaction: {@code REQUIRED} would begin and commit "its own" transaction on the JPA transaction's connection,
 * committing the JPA work done so far, and {@code REQUIRES_NEW} would suspend nothing and do the same. This manager
 * treats such a holder as the existing transaction it is:
 * <ul>
 * <li>{@code REQUIRED}, {@code SUPPORTS} and {@code MANDATORY} participate in the JPA transaction, and a participant
 * that fails marks the JPA transaction rollback-only, as a failed JPA participant would;</li>
 * <li>{@code REQUIRES_NEW} and {@code NOT_SUPPORTED} suspend it and work on a connection of their own;</li>
 * <li>{@code NEVER} fails, and {@code NESTED} is refused: a JDBC savepoint on the JPA transaction's connection would
 * roll back database state underneath the persistence context.</li>
 * </ul>
 */
public class ExtendedDataSourceTransactionManager extends DataSourceTransactionManager {

    private final List<TransactionLifecycleCallback> lifecycleCallbacks = new CopyOnWriteArrayList<>();

    @Getter
    private final boolean readOnly;

    public ExtendedDataSourceTransactionManager(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    protected boolean isExistingTransaction(@NonNull Object transaction) {
        return super.isExistingTransaction(transaction) || isForeignTransactionBound();
    }

    @Override
    protected boolean useSavepointForNestedTransaction() {
        // Without a savepoint, a nested transaction begins a new one, which doBegin refuses on a foreign connection.
        return !isForeignTransactionBound();
    }

    @Override
    protected void doBegin(@NonNull Object transaction, @NonNull TransactionDefinition definition) {
        if (isForeignTransactionBound()) {
            throw new IllegalTransactionStateException("Cannot begin a JDBC transaction on the connection of a transaction this manager "
                    + "did not open (typically a JPA one); NESTED is not supported across the JPA and JDBC transaction managers");
        }
        super.doBegin(transaction, definition);

        if (isReadOnly()) {
            setEnforceReadOnly(true);
        }

        invokeLifecycleCallbacks(TransactionLifecycleCallback::afterBegin);
    }

    @Override
    protected void doCommit(@NonNull DefaultTransactionStatus status) {
        super.doCommit(status);
        invokeLifecycleCallbacks(TransactionLifecycleCallback::afterCommit);
    }

    @Override
    protected void doSetRollbackOnly(@NonNull DefaultTransactionStatus status) {
        super.doSetRollbackOnly(status);
        if (isForeignTransactionBound()) {
            // The JPA transaction decides its outcome from the EntityTransaction, not from the connection holder.
            markJpaTransactionsRollbackOnly();
        }
    }

    @Override
    protected void doCleanupAfterCompletion(@NonNull Object transaction) {
        super.doCleanupAfterCompletion(transaction);
        invokeLifecycleCallbacks(TransactionLifecycleCallback::afterCompletion);
        // The connection is released by now; see AfterTransactionWork.
        AfterTransactionWork.runReadyWork();
    }

    /**
     * A connection holder bound to this manager's {@code DataSource} that this manager did not open: every holder it
     * opens, and every holder {@code DataSourceUtils} binds to a synchronisation, is synchronised with its transaction,
     * the one the JPA transaction manager exposes is not.
     */
    private boolean isForeignTransactionBound() {
        return TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.getResource(obtainDataSource()) instanceof ConnectionHolder holder
                && !holder.isSynchronizedWithTransaction();
    }

    private static void markJpaTransactionsRollbackOnly() {
        TransactionSynchronizationManager.getResourceMap().values().stream() //
                .filter(EntityManagerHolder.class::isInstance) //
                .map(EntityManagerHolder.class::cast) //
                .filter(holder -> holder.getEntityManager() != null) //
                .forEach(holder -> {
                    holder.setRollbackOnly();
                    EntityTransaction entityTransaction = holder.getEntityManager().getTransaction();
                    if (entityTransaction.isActive()) {
                        entityTransaction.setRollbackOnly();
                    }
                });
    }

    private void invokeLifecycleCallbacks(Consumer<TransactionLifecycleCallback> f) {
        lifecycleCallbacks.forEach(f);
    }

    public void setLifecycleCallbacks(List<TransactionLifecycleCallback> lifecycleCallbacks) {
        this.lifecycleCallbacks.addAll(lifecycleCallbacks);
    }

}
