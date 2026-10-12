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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import lombok.Getter;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.lang.NonNull;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;

public class ExtendedDataSourceTransactionManager extends DataSourceTransactionManager {

    private final List<TransactionLifecycleCallback> lifecycleCallbacks = new CopyOnWriteArrayList<>();

    @Getter
    private final boolean readOnly;

    public ExtendedDataSourceTransactionManager(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    protected void doBegin(@NonNull Object transaction, @NonNull TransactionDefinition definition) {
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
    protected void doCleanupAfterCompletion(@NonNull Object transaction) {
        super.doCleanupAfterCompletion(transaction);
        invokeLifecycleCallbacks(TransactionLifecycleCallback::afterCompletion);
    }

    private void invokeLifecycleCallbacks(Consumer<TransactionLifecycleCallback> f) {
        lifecycleCallbacks.forEach(f);
    }

    public void setLifecycleCallbacks(List<TransactionLifecycleCallback> lifecycleCallbacks) {
        this.lifecycleCallbacks.addAll(lifecycleCallbacks);
    }

}
