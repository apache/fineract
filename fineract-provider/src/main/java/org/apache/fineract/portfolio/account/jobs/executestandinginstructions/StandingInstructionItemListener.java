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
import org.apache.fineract.portfolio.account.service.StandingInstructionExecutionService;
import org.apache.fineract.portfolio.savings.exception.InsufficientAccountBalanceException;
import org.springframework.batch.core.annotation.OnSkipInWrite;
import org.springframework.lang.NonNull;

/**
 * Records the outcome of an instruction the step gave up on.
 *
 * <p>
 * The listener runs after the failing instruction's transaction has been rolled back, and
 * {@link StandingInstructionExecutionService#recordFailure} commits in a transaction of its own, so the record of the
 * failure outlives the transfer that failed. Before this job was made fault tolerant that record was rolled back along
 * with the run that produced it, which is what kept the underlying defect hidden.
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class StandingInstructionItemListener {

    private final StandingInstructionExecutionService standingInstructionExecutionService;

    @OnSkipInWrite
    public void onSkipInWrite(@NonNull final DueStandingInstruction due, @NonNull final Throwable failure) {
        final Long instructionId = due.instruction().getId();
        final String errorLog = describe(failure);
        log.error("Standing instruction {} (from {} to {}) failed: {}", instructionId, due.instruction().getFromAccount().getId(),
                due.instruction().getToAccount().getId(), errorLog, failure);
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
