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
package org.apache.fineract.portfolio.account.jobs.executestandinginstructions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.exception.AbstractPlatformServiceUnavailableException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.portfolio.account.data.DueStandingInstruction;
import org.apache.fineract.portfolio.account.data.StandingInstructionData;
import org.apache.fineract.portfolio.account.service.StandingInstructionExecutionService;
import org.apache.fineract.portfolio.savings.exception.InsufficientAccountBalanceException;
import org.jspecify.annotations.NonNull;
import org.springframework.batch.core.listener.SkipListener;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Records the outcome of an instruction the step gave up on, whether it failed while being prepared (process) or while
 * transferring (write).
 *
 * <p>
 * Spring Batch calls the listener while the failing instruction's transaction is still open, and rolls that transaction
 * back only afterwards. The record is therefore written once the transaction has completed, in a transaction of its own
 * ({@link StandingInstructionExecutionService#recordFailure}), so it outlives the transfer that failed. Writing it
 * earlier would wait on the lock the instruction's claim still holds: on MySQL the history row's foreign key to the
 * instruction takes a shared lock on that row. Before this job was made fault tolerant the record was rolled back along
 * with the run that produced it, which is what kept the underlying defect hidden.
 * </p>
 *
 * <p>
 * The listener implements {@link SkipListener} rather than using the {@code @OnSkipInWrite} annotation, because the
 * chunk-oriented step builder registers annotated methods for chunk, read, process and write events only.
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class StandingInstructionItemListener implements SkipListener<StandingInstructionData, DueStandingInstruction> {

    private final StandingInstructionExecutionService standingInstructionExecutionService;

    @Override
    public void onSkipInRead(@NonNull final Throwable failure) {
        // The skip policy accepts no read failure (see StandingInstructionReadException); this is only a safety net.
        log.error("A page of due standing instructions was skipped", failure);
    }

    @Override
    public void onSkipInProcess(@NonNull final StandingInstructionData instruction, @NonNull final Throwable failure) {
        recordSkip(instruction, failure);
    }

    @Override
    public void onSkipInWrite(@NonNull final DueStandingInstruction due, @NonNull final Throwable failure) {
        recordSkip(due.instruction(), failure);
    }

    private void recordSkip(final StandingInstructionData instruction, final Throwable failure) {
        final Long instructionId = instruction.getId();
        final String errorLog = describe(failure);
        log.error("Standing instruction {} (from {} to {}) failed: {}", instructionId, instruction.getFromAccount().getId(),
                instruction.getToAccount().getId(), errorLog, failure);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

                @Override
                public void afterCompletion(final int status) {
                    recordFailure(instructionId, errorLog);
                }
            });
        } else {
            recordFailure(instructionId, errorLog);
        }
    }

    private void recordFailure(final Long instructionId, final String errorLog) {
        try {
            standingInstructionExecutionService.recordFailure(instructionId, errorLog);
        } catch (final RuntimeException e) {
            // A history-write failure must never abort the remaining instructions; log and continue.
            log.error("Failed to record failure history for standing instruction {}", instructionId, e);
        }
    }

    private String describe(final Throwable failure) {
        if (failure instanceof InsufficientAccountBalanceException) {
            return "InsufficientAccountBalance Exception ";
        } else if (failure instanceof PlatformApiDataValidationException e) {
            return "Validation exception while trasfering funds " + e.getDefaultUserMessage();
        } else if (failure instanceof AbstractPlatformServiceUnavailableException e) {
            return "Platform exception while trasfering funds " + e.getDefaultUserMessage();
        }
        return "Exception while trasfering funds " + failure.getMessage();
    }
}
