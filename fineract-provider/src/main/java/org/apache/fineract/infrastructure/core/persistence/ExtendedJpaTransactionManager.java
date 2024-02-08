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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import org.springframework.jdbc.datasource.JdbcTransactionObjectSupport;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class ExtendedJpaTransactionManager extends JpaTransactionManager {

    private final List<TransactionLifecycleCallback> lifecycleCallbacks = new CopyOnWriteArrayList<>();

    public ExtendedJpaTransactionManager() {
        setValidateExistingTransaction(true);
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        super.doBegin(transaction, definition);
        if (isReadOnlyTx(transaction)) {
            EntityManager entityManager = getCurrentEntityManager();
            if (entityManager != null) {
                entityManager.setFlushMode(FlushModeType.COMMIT);
            }
        }
        invokeLifecycleCallbacks(TransactionLifecycleCallback::afterBegin);
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) {
        if (isReadOnlyTx(status.getTransaction())) {
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
}
