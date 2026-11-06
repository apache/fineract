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
import org.jspecify.annotations.NonNull;
import org.springframework.batch.core.annotation.OnProcessError;
import org.springframework.batch.core.annotation.OnReadError;
import org.springframework.batch.core.annotation.OnSkipInProcess;
import org.springframework.batch.core.annotation.OnSkipInRead;
import org.springframework.batch.core.annotation.OnSkipInWrite;
import org.springframework.batch.core.annotation.OnWriteError;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Records COB failures on the account locks, so that the accounts stay locked with the reason attached.
 * <p>
 * The error is saved in a transaction of its own, committed whatever happens to the chunk transaction the failure
 * occurred in, which by then is usually marked rollback-only. That transaction must come from the JPA transaction
 * manager: the chunk runs in a JPA transaction, and a JDBC {@code DataSourceTransactionManager} does not recognise the
 * JDBC connection the JPA one exposes as an existing transaction, so its {@code REQUIRES_NEW} suspends nothing and runs
 * on the chunk's own connection. The update then rolls back together with the chunk, and the commit of the
 * rollback-only connection throws {@code UnexpectedRollbackException}.
 * <p>
 * Saving the error never throws. Spring Batch 6 calls {@code onWriteError} before it decides whether the chunk can be
 * recovered by skipping, so an exception from here would replace the original failure and fail the step instead of
 * letting the failing account be skipped.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractLoanItemListener<S extends AbstractPersistableCustom<Long>> {

    private final LockingService loanLockingService;
    private final TransactionTemplate requiresNewTransactionTemplate;

    private void updateAccountLockWithError(List<Long> loanIds, String msg, Throwable e) {
        try {
            requiresNewTransactionTemplate.executeWithoutResult(status -> {
                String stacktrace = ThrowableSerialization.serialize(e);
                for (Long loanId : loanIds) {
                    loanLockingService.updateLockError(loanId, getLockOwner(), String.format(msg, loanId), stacktrace);
                }
            });
        } catch (RuntimeException saveFailure) {
            log.error("Could not save the COB error on the locks of loans {}", loanIds, saveFailure);
        }
    }

    @OnReadError
    public void onReadError(Exception e) {
        if (e instanceof LockedReadException ee) {
            log.warn("Error was triggered during reading of Loan (id={}) due to: {}", ee.getId(), ThrowableSerialization.serialize(e));
            updateAccountLockWithError(List.of(ee.getId()), "Loan (id: %d) reading is failed", e);
        } else {
            log.error("Could not handle read error", e);
        }
    }

    @OnProcessError
    public void onProcessError(@NonNull S item, Exception e) {
        log.warn("Error was triggered during processing of Loan (id={}) due to: {}", item.getId(), ThrowableSerialization.serialize(e));
        updateAccountLockWithError(List.of(item.getId()), "Loan (id: %d) processing is failed", e);
    }

    @OnWriteError
    public void onWriteError(Exception e, @NonNull Chunk<? extends S> items) {
        List<Long> loanIds = items.getItems().stream().map(AbstractPersistableCustom::getId).toList();
        log.warn("Error was triggered during writing of Loans (ids={}) due to: {}", loanIds, ThrowableSerialization.serialize(e));
        updateAccountLockWithError(loanIds, "Loan (id: %d) writing is failed", e);
    }

    @OnSkipInRead
    public void onSkipInRead(@NonNull Throwable e) {
        log.warn("Skipping was triggered during read!");
    }

    @OnSkipInProcess
    public void onSkipInProcess(@NonNull S item, @NonNull Throwable e) {
        log.warn("Skipping was triggered during processing of Loan (id={})", item.getId());
    }

    /**
     * Called when scan mode skips an account whose business steps or write failed on their own. {@code onWriteError}
     * has already recorded the failure of the whole chunk on this account's lock; this replaces it with the account's
     * own failure.
     */
    @OnSkipInWrite
    public void onSkipInWrite(@NonNull S item, @NonNull Throwable e) {
        log.warn("Skipping was triggered during writing of Loan (id={}) due to: {}", item.getId(), ThrowableSerialization.serialize(e));
        updateAccountLockWithError(List.of(item.getId()), "Loan (id: %d) writing is failed", e);
    }

    protected abstract LockOwner getLockOwner();

}
