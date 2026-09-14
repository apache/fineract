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

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.cob.domain.LockingService;
import org.apache.fineract.cob.exceptions.LockedReadException;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.serialization.ThrowableSerialization;
import org.springframework.batch.core.annotation.OnProcessError;
import org.springframework.batch.core.annotation.OnReadError;
import org.springframework.batch.core.annotation.OnSkipInProcess;
import org.springframework.batch.core.annotation.OnSkipInRead;
import org.springframework.batch.core.annotation.OnSkipInWrite;
import org.springframework.batch.core.annotation.OnWriteError;
import org.springframework.batch.item.Chunk;
import org.springframework.lang.NonNull;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Account-agnostic Spring Batch item listener for COB chunk processing. It records read/process/write errors against
 * the corresponding account lock so a failed account can be diagnosed and unlocked. Concrete account types (loan,
 * savings, ...) supply the {@link LockOwner} to scope the lock updates.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractItemListener<S extends AbstractPersistableCustom<Long>> {

    private final LockingService lockingService;
    private final TransactionTemplate requiresNewTransactionJdbcTemplate;

    private void updateAccountLockWithError(List<Long> accountIds, String msg, Throwable e) {
        requiresNewTransactionJdbcTemplate.executeWithoutResult(status -> {
            String stacktrace = ThrowableSerialization.serialize(e);
            for (Long accountId : accountIds) {
                lockingService.updateLockError(accountId, getLockOwner(), String.format(msg, accountId), stacktrace);
            }
        });
    }

    @OnReadError
    public void onReadError(Exception e) {
        if (e instanceof LockedReadException ee) {
            log.warn("Error was triggered during reading of account (id={}) due to: {}", ee.getId(), ThrowableSerialization.serialize(e));
            updateAccountLockWithError(List.of(ee.getId()), getAccountTypeLabel() + " (id: %d) reading is failed", e);
        } else {
            log.error("Could not handle read error", e);
        }
    }

    @OnProcessError
    public void onProcessError(@NonNull S item, Exception e) {
        log.warn("Error was triggered during processing of account (id={}) due to: {}", item.getId(), ThrowableSerialization.serialize(e));
        updateAccountLockWithError(List.of(item.getId()), getAccountTypeLabel() + " (id: %d) processing is failed", e);
    }

    @OnWriteError
    public void onWriteError(Exception e, @NonNull Chunk<? extends S> items) {
        List<Long> accountIds = items.getItems().stream().map(AbstractPersistableCustom::getId).toList();
        log.warn("Error was triggered during writing of accounts (ids={}) due to: {}", accountIds, ThrowableSerialization.serialize(e));
        updateAccountLockWithError(accountIds, getAccountTypeLabel() + " (id: %d) writing is failed", e);
    }

    @OnSkipInRead
    public void onSkipInRead(@NonNull Throwable e) {
        log.warn("Skipping was triggered during read!");
    }

    @OnSkipInProcess
    public void onSkipInProcess(@NonNull S item, @NonNull Throwable e) {
        log.warn("Skipping was triggered during processing of account (id={})", item.getId());
    }

    @OnSkipInWrite
    public void onSkipInWrite(@NonNull S item, @NonNull Throwable e) {
        log.warn("Skipping was triggered during writing of account (id={})", item.getId());
    }

    protected abstract LockOwner getLockOwner();

    /**
     * Human-readable noun for the account type handled by this listener (e.g. {@code "Loan"}, {@code "Savings"}). Used
     * to build the lock error messages. Concrete account types may override it; defaults to the generic
     * {@code "Account"}.
     */
    protected String getAccountTypeLabel() {
        return "Account";
    }

}
