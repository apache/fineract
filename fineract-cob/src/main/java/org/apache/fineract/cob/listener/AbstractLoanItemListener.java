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
import org.apache.fineract.cob.exceptions.COBAccountFailedException;
import org.apache.fineract.cob.exceptions.LockedReadException;
import org.apache.fineract.infrastructure.core.persistence.AfterTransactionWork;
import org.apache.fineract.infrastructure.core.serialization.ThrowableSerialization;
import org.jspecify.annotations.NonNull;
import org.springframework.batch.core.listener.ChunkListener;
import org.springframework.batch.core.listener.ItemReadListener;
import org.springframework.batch.core.listener.ItemWriteListener;
import org.springframework.batch.core.listener.SkipListener;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.core.retry.RetryException;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Records COB failures on the account locks, so that the accounts stay locked with the reason attached and a later run
 * can take them over.
 * <p>
 * The items are account ids (see {@code AbstractCOBItemWriter}).
 * <p>
 * An error is recorded only once the outcome of the account is final. An error makes the lock overrulable, so an API
 * request on the account may then run inline COB on it; were the error recorded on an account that the step is still
 * going to write - every account of a chunk that is about to be written again one account at a time (scan mode) - the
 * account would be closed by inline COB and then by the step for the same business date, running its business steps and
 * raising their events twice. So:
 * <ul>
 * <li>a chunk that fails in a fault-tolerant step, which Batch reports wrapped in a {@link RetryException}, records
 * nothing: scan mode follows and settles every account of it, and {@link #onSkipInWrite} records the failing one;</li>
 * <li>a chunk that fails where no scan follows - inline COB, whose step is not fault-tolerant, or a fault-tolerant step
 * whose failure cannot be skipped - records its failure on all its accounts, the failing account's own where the writer
 * names it ({@link COBAccountFailedException}) and that its chunk was rolled back with it on the others;</li>
 * <li>an account that exhausts the skip limit in scan mode, which Batch reports to no item listener, gets its error
 * from {@link #onChunkError}.</li>
 * </ul>
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
 * <p>
 * Saving the error never throws. Spring Batch 6 calls {@code onWriteError} before it decides whether the chunk can be
 * recovered by skipping, so an exception from here would replace the original failure and fail the step instead of
 * letting the failing account be skipped.
 * <p>
 * The listener interfaces are implemented rather than the listener annotations used, because Batch 6 reads no
 * annotation for {@code onChunkError}.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractLoanItemListener
        implements ItemReadListener<Long>, ItemWriteListener<Long>, SkipListener<Long, Long>, ChunkListener<Long, Long> {

    private static final String CHUNK_FAILED = "Loan (id: %d) was rolled back with its failed chunk";
    private static final String WRITING_FAILED = "Loan (id: %d) writing is failed";
    private static final String READING_FAILED = "Loan (id: %d) reading is failed";

    private final LockingService loanLockingService;
    private final TransactionTemplate requiresNewTransactionTemplate;

    private void updateAccountLockWithError(List<Long> loanIds, String msg, Throwable e) {
        // The failure of the account itself, rather than the wrappers that only name the account or report the retry.
        Throwable failure = COBAccountFailedException.find(e).map(Throwable::getCause).orElse(e);
        String stacktrace = ThrowableSerialization.serialize(failure);
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

    @Override
    public void onReadError(@NonNull Exception e) {
        if (e instanceof LockedReadException ee) {
            log.warn("Error was triggered during reading of Loan (id={}) due to: {}", ee.getId(), ThrowableSerialization.serialize(e));
            updateAccountLockWithError(List.of(ee.getId()), READING_FAILED, e);
        } else {
            log.error("Could not handle read error", e);
        }
    }

    @Override
    public void onWriteError(@NonNull Exception e, @NonNull Chunk<? extends Long> loanIds) {
        List<Long> ids = List.copyOf(loanIds.getItems());
        log.warn("Error was triggered during writing of Loans (ids={}) due to: {}", ids, ThrowableSerialization.serialize(e));
        if (e instanceof RetryException) {
            // Scan mode follows, or, if the failure cannot be skipped, onChunkError.
            return;
        }
        recordChunkFailure(ids, e);
    }

    /**
     * Called when scan mode skips an account whose business steps or write failed on their own.
     */
    @Override
    public void onSkipInWrite(@NonNull Long loanId, @NonNull Throwable e) {
        log.warn("Skipping was triggered during writing of Loan (id={}) due to: {}", loanId, ThrowableSerialization.serialize(e));
        updateAccountLockWithError(List.of(loanId), WRITING_FAILED, e);
    }

    @Override
    public void onSkipInRead(@NonNull Throwable e) {
        log.warn("Skipping was triggered during read!");
    }

    /**
     * Called before Batch fails the step over a chunk. Two of these failures no write callback has recorded: a chunk of
     * a fault-tolerant step whose failure turned out not to be skippable, so that no scan follows; and an account that
     * failed in scan mode once the skip limit was used up, which Batch raises from its skip decision, before any skip
     * or write callback, and reports with an empty chunk.
     */
    @Override
    public void onChunkError(@NonNull Exception e, @NonNull Chunk<Long> chunk) {
        if (e instanceof RetryException && !chunk.isEmpty()) {
            recordChunkFailure(List.copyOf(chunk.getItems()), e);
        } else if (e instanceof SkipLimitExceededException) {
            COBAccountFailedException.find(e).ifPresentOrElse(
                    accountFailure -> updateAccountLockWithError(List.of(accountFailure.getAccountId()), WRITING_FAILED, accountFailure),
                    () -> log.error("The COB skip limit was exceeded by an account that could not be identified", e));
        }
    }

    private void recordChunkFailure(List<Long> ids, Exception e) {
        if (ids.size() == 1) {
            updateAccountLockWithError(ids, WRITING_FAILED, e);
            return;
        }
        COBAccountFailedException.find(e).filter(accountFailure -> ids.contains(accountFailure.getAccountId()))
                .ifPresentOrElse(accountFailure -> {
                    Long failedId = accountFailure.getAccountId();
                    updateAccountLockWithError(List.of(failedId), WRITING_FAILED, accountFailure);
                    updateAccountLockWithError(ids.stream().filter(id -> !id.equals(failedId)).toList(), CHUNK_FAILED, e);
                }, () -> updateAccountLockWithError(ids, CHUNK_FAILED, e));
    }

    protected abstract LockOwner getLockOwner();

}
