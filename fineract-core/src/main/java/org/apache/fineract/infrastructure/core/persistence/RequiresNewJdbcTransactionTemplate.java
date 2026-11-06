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

import java.util.Objects;
import javax.sql.DataSource;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * A {@code REQUIRES_NEW} template on the JDBC transaction manager that refuses to start where it cannot keep its
 * promise.
 * <p>
 * A {@link DataSourceTransactionManager} recognises only transactions it opened itself. The JPA transaction manager
 * exposes the JDBC connection of its transaction as a {@link ConnectionHolder} bound to the same {@code DataSource},
 * but marks it neither active nor synchronised. The JDBC manager therefore sees no existing transaction, suspends
 * nothing, and runs the "new" transaction on the JPA transaction's own connection: its commit commits the JPA work done
 * so far, and, when the JPA transaction is already marked rollback-only, its commit rolls that connection back and
 * throws {@code UnexpectedRollbackException}. Either way the caller does not get an independent transaction.
 * <p>
 * This template detects exactly that situation - a connection holder bound to its {@code DataSource} that the JDBC
 * manager did not open - and throws {@link IllegalTransactionStateException} before anything runs, instead of silently
 * joining the outer transaction. Inside a JPA transaction, use the JPA {@code requiresNewTransactionTemplate}, which
 * does suspend it. Outside any transaction, inside a transaction this JDBC manager opened, or where the JPA transaction
 * has been suspended (for example under {@code Propagation.NOT_SUPPORTED}), it behaves as a plain {@code REQUIRES_NEW}
 * template.
 */
public class RequiresNewJdbcTransactionTemplate extends TransactionTemplate {

    private final DataSource dataSource;

    /**
     * @param transactionManager
     *            the JDBC transaction manager; it must be a {@link DataSourceTransactionManager} with a
     *            {@code DataSource}, which is the one this template watches
     */
    public RequiresNewJdbcTransactionTemplate(PlatformTransactionManager transactionManager) {
        super(transactionManager);
        if (!(transactionManager instanceof DataSourceTransactionManager jdbcTransactionManager)) {
            throw new IllegalArgumentException(
                    "A JDBC REQUIRES_NEW template needs a DataSourceTransactionManager, got " + transactionManager.getClass().getName());
        }
        this.dataSource = Objects.requireNonNull(jdbcTransactionManager.getDataSource(), "The JDBC transaction manager has no DataSource");
        setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public <T extends @Nullable Object> T execute(@NonNull TransactionCallback<T> action) throws TransactionException {
        if (isForeignTransactionBound()) {
            throw new IllegalTransactionStateException("A transaction that the JDBC transaction manager did not open - typically a JPA "
                    + "transaction - is bound to this thread, so a JDBC REQUIRES_NEW transaction would run on its connection instead "
                    + "of an independent one. Use the JPA \"requiresNewTransactionTemplate\" inside a JPA transaction.");
        }
        return super.execute(action);
    }

    /**
     * The condition under which {@code DataSourceTransactionManager.doBegin} reuses a bound connection instead of
     * taking a fresh one: a holder that is not synchronised with a transaction. Every holder the JDBC manager opens,
     * and every holder {@code DataSourceUtils} binds to a synchronisation, is marked synchronised; the one the JPA
     * transaction manager exposes is not.
     */
    private boolean isForeignTransactionBound() {
        return TransactionSynchronizationManager.getResource(dataSource) instanceof ConnectionHolder holder
                && !holder.isSynchronizedWithTransaction();
    }
}
