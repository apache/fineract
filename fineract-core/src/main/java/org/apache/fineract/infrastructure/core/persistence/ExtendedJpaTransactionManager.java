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

import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import java.sql.Connection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import lombok.Getter;
import org.springframework.jdbc.datasource.ConnectionHandle;
import org.springframework.jdbc.datasource.JdbcTransactionObjectSupport;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaDialect;
import org.springframework.transaction.InvalidIsolationLevelException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class ExtendedJpaTransactionManager extends JpaTransactionManager {

    private final List<TransactionLifecycleCallback> lifecycleCallbacks = new CopyOnWriteArrayList<>();

    @Getter
    private final boolean readOnly;

    public ExtendedJpaTransactionManager(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        if (getJpaDialect() instanceof EclipseLinkJpaDialect) {
            setJpaDialect(new LockFreeEclipseLinkJpaDialect());
        }
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        // Custom isolation levels are forbidden on this (JPA/EclipseLink) transaction manager. EclipseLinkJpaDialect
        // applies a non-default isolation by transiently mutating the shared per-session DatabaseLogin under a lock;
        // our
        // LockFreeEclipseLinkJpaDialect removes that lock from connection acquisition, so a concurrent transaction
        // could
        // bleed the wrong isolation. Keeping every transaction at the pool/baseline isolation is what makes the
        // lock-free
        // dialect safe. If a transaction genuinely needs a specific isolation level, run it through the JDBC
        // transaction
        // manager ("jdbcTransactionManager"), which applies isolation per-connection without touching the shared
        // session.
        if (definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
            throw new InvalidIsolationLevelException("Custom isolation level " + definition.getIsolationLevel()
                    + " is not supported by the JPA transaction manager; use the JDBC transaction manager (\"jdbcTransactionManager\") "
                    + "for transactions that require a specific isolation level");
        }

        super.doBegin(transaction, definition);

        if (definition.isReadOnly() || isReadOnlyTx(transaction) || isReadOnly()) {
            EntityManager entityManager = getCurrentEntityManager();
            if (entityManager != null) {
                entityManager.setFlushMode(FlushModeType.COMMIT);
            }
        }

        invokeLifecycleCallbacks(TransactionLifecycleCallback::afterBegin);
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) {
        if (isReadOnlyTx(status.getTransaction()) || isReadOnly()) {
            EntityManager entityManager = getCurrentEntityManager();
            if (entityManager != null) {
                entityManager.clear();
            }
        }

        super.doCommit(status);
        invokeLifecycleCallbacks(TransactionLifecycleCallback::afterCommit);
    }

    @Override
    protected void doCleanupAfterCompletion(Object transaction) {
        super.doCleanupAfterCompletion(transaction);
        invokeLifecycleCallbacks(TransactionLifecycleCallback::afterCompletion);
    }

    private boolean isReadOnlyTx(Object transaction) {
        JdbcTransactionObjectSupport txObject = (JdbcTransactionObjectSupport) transaction;
        return txObject.isReadOnly();
    }

    private EntityManager getCurrentEntityManager() {
        EntityManagerHolder holder = (EntityManagerHolder) TransactionSynchronizationManager.getResource(obtainEntityManagerFactory());
        if (holder != null) {
            return holder.getEntityManager();
        }
        return null;
    }

    private void invokeLifecycleCallbacks(Consumer<TransactionLifecycleCallback> f) {
        lifecycleCallbacks.forEach(f::accept);
    }

    public void setLifecycleCallbacks(List<TransactionLifecycleCallback> lifecycleCallbacks) {
        this.lifecycleCallbacks.addAll(lifecycleCallbacks);
    }

    private static final class LockFreeEclipseLinkJpaDialect extends EclipseLinkJpaDialect {

        LockFreeEclipseLinkJpaDialect() {
            // EclipseLinkConnectionHandle.getConnection() acquires the singleton transactionIsolationLock
            // before calling entityManager.unwrap(Connection.class). When the pool is exhausted,
            // one thread holds that lock for 30s (Hikari timeout) while ALL other threads serialize
            // behind it — converting parallel timeouts into a sequential cascade (N threads × 30s).
            // With lazyDatabaseTransaction=true, no isolation level changes are made, so the lock
            // in getConnection() is unnecessary. We return a lock-free handle instead.
            setLazyDatabaseTransaction(true);
        }

        @Override
        public ConnectionHandle getJdbcConnection(EntityManager em, boolean readOnly) {
            return new LockFreeConnectionHandle(em);
        }

        private static final class LockFreeConnectionHandle implements ConnectionHandle {

            private final EntityManager em;
            private Connection connection;

            LockFreeConnectionHandle(EntityManager em) {
                this.em = em;
            }

            @Override
            public Connection getConnection() {
                if (connection == null) {
                    connection = em.unwrap(Connection.class);
                }
                return connection;
            }
        }
    }
}
