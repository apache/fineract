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
import org.apache.fineract.infrastructure.core.persistence.AfterTransactionWork;
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
 * Records COB failures on the account locks, so that the accounts stay locked with the reason attached and a later run
 * can take them over.
 * <p>
 * The items are account ids (see {@code AbstractCOBItemWriter}).
 * <p>
 * An error is saved only once the transaction the failure occurred in has completed and given back its connection
 * ({@link AfterTransactionWork}), in a transaction of its own:
 * <ul>
 * <li>the failed chunk transaction is usually rollback-only, and must not take the error down with it;</li>
 * <li>saving while it is still open would need a second connection from the pool, which concurrent partitions may all
 * be waiting for;</li>
 * <li>and the chunk transaction may hold row locks on the very lock rows the error goes to, which a second connection
 * would wait on behind its own thread.</li>
 * </ul>
 * The errors of one account are saved in the order they were raised, so the error of the scan-mode attempt replaces the
 * one recorded for its chunk.
 * <p>
 * Saving the error never throws. Spring Batch 6 calls {@code onWriteError} before it decides whether the chunk can be
 * recovered by skipping, so an exception from here would replace the original failure and fail the step instead of
 * letting the failing account be skipped.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractLoanItemListener {

    private static final String CHUNK_FAILED = "Loan (id: %d) was rolled back with its failed chunk";
    private static final String WRITING_FAILED = "Loan (id: %d) writing is failed";

    private final LockingService loanLockingService;
    private final TransactionTemplate requiresNewTransactionTemplate;

    private void updateAccountLockWithError(List<Long> loanIds, String msg, Throwable e) {
        String stacktrace = ThrowableSerialization.serialize(e);
        LockOwner lockOwner = getLockOwner();
        AfterTransactionWork.runAfterCompletion(() -> saveErrors(loanIds, lockOwner, msg, stacktrace));
    }

    private void saveErrors(List<Long> loanIds, LockOwner lockOwner, String msg, String stacktrace) {
        try {
            requiresNewTransactionTemplate.executeWithoutResult(status -> {
                for (Long loanId : loanIds) {
                    loanLockingService.updateLockError(loanId, lockOwner, String.format(msg, loanId), stacktrace);
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
    public void onProcessError(@NonNull Long loanId, Exception e) {
        log.warn("Error was triggered during processing of Loan (id={}) due to: {}", loanId, ThrowableSerialization.serialize(e));
        updateAccountLockWithError(List.of(loanId), "Loan (id: %d) processing is failed", e);
    }

    /**
     * A chunk of several accounts is about to be written again one account at a time (scan mode), so the error saved
     * here says only that the account went down with its chunk; the account that actually failed gets its own error
     * from {@link #onSkipInWrite}. A single-account chunk - scan mode itself, or a chunk of one - failed on its own
     * account.
     */
    @OnWriteError
    public void onWriteError(Exception e, @NonNull Chunk<? extends Long> loanIds) {
        List<Long> ids = List.copyOf(loanIds.getItems());
        log.warn("Error was triggered during writing of Loans (ids={}) due to: {}", ids, ThrowableSerialization.serialize(e));
        updateAccountLockWithError(ids, ids.size() == 1 ? WRITING_FAILED : CHUNK_FAILED, e);
    }

    @OnSkipInRead
    public void onSkipInRead(@NonNull Throwable e) {
        log.warn("Skipping was triggered during read!");
    }

    @OnSkipInProcess
    public void onSkipInProcess(@NonNull Long loanId, @NonNull Throwable e) {
        log.warn("Skipping was triggered during processing of Loan (id={})", loanId);
    }

    /**
     * Called when scan mode skips an account whose business steps or write failed on their own. Its error replaces the
     * chunk-level one {@link #onWriteError} recorded before.
     */
    @OnSkipInWrite
    public void onSkipInWrite(@NonNull Long loanId, @NonNull Throwable e) {
        log.warn("Skipping was triggered during writing of Loan (id={}) due to: {}", loanId, ThrowableSerialization.serialize(e));
        updateAccountLockWithError(List.of(loanId), WRITING_FAILED, e);
    }

    protected abstract LockOwner getLockOwner();

}
