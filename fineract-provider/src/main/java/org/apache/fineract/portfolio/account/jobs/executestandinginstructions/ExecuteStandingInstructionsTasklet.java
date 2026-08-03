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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.AbstractPlatformServiceUnavailableException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.AccountTransferDTO;
import org.apache.fineract.portfolio.account.data.StandingInstructionData;
import org.apache.fineract.portfolio.account.data.StandingInstructionDuesData;
import org.apache.fineract.portfolio.account.domain.AccountTransferRecurrenceType;
import org.apache.fineract.portfolio.account.domain.StandingInstructionStatus;
import org.apache.fineract.portfolio.account.domain.StandingInstructionType;
import org.apache.fineract.portfolio.account.service.StandingInstructionExecutionService;
import org.apache.fineract.portfolio.account.service.StandingInstructionReadPlatformService;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.DefaultScheduledDateGenerator;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.ScheduledDateGenerator;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.exception.InsufficientAccountBalanceException;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
@RequiredArgsConstructor
public class ExecuteStandingInstructionsTasklet implements Tasklet {

    private final StandingInstructionReadPlatformService standingInstructionReadPlatformService;
    private final StandingInstructionExecutionService standingInstructionExecutionService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        Collection<StandingInstructionData> instructionData = standingInstructionReadPlatformService
                .retrieveAll(StandingInstructionStatus.ACTIVE.getValue());
        int processed = 0;
        int succeeded = 0;
        int failed = 0;
        for (StandingInstructionData data : instructionData) {
            boolean isDueForTransfer = false;
            AccountTransferRecurrenceType recurrenceType = data.getRecurrenceType();
            StandingInstructionType instructionType = data.getInstructionType();
            LocalDate transactionDate = DateUtils.getBusinessLocalDate();
            if (recurrenceType.isPeriodicRecurrence()) {
                final ScheduledDateGenerator scheduledDateGenerator = new DefaultScheduledDateGenerator();
                PeriodFrequencyType frequencyType = data.getRecurrenceFrequency();
                LocalDate startDate = data.getValidFrom();
                if (frequencyType.isMonthly()) {
                    startDate = startDate.withDayOfMonth(data.getRecurrenceOnDay());
                    if (DateUtils.isBefore(startDate, data.getValidFrom())) {
                        startDate = startDate.plusMonths(1);
                    }
                } else if (frequencyType.isYearly()) {
                    startDate = startDate.withDayOfMonth(data.getRecurrenceOnDay()).withMonth(data.getRecurrenceOnMonth());
                    if (DateUtils.isBefore(startDate, data.getValidFrom())) {
                        startDate = startDate.plusYears(1);
                    }
                }
                isDueForTransfer = scheduledDateGenerator.isDateFallsInSchedule(frequencyType, data.getRecurrenceInterval(), startDate,
                        transactionDate);

            }
            BigDecimal transactionAmount = data.getAmount();
            if (PortfolioAccountType.LOAN.equals(data.getToAccountType())
                    && (recurrenceType.isDuesRecurrence() || (isDueForTransfer && instructionType.isDuesAmoutTransfer()))) {
                StandingInstructionDuesData standingInstructionDuesData = standingInstructionReadPlatformService
                        .retriveLoanDuesData(data.getToAccount().getId());
                if (data.getInstructionType().isDuesAmoutTransfer()) {
                    transactionAmount = standingInstructionDuesData.totalDueAmount();
                }
                if (recurrenceType.isDuesRecurrence()) {
                    isDueForTransfer = isDueForTransfer(standingInstructionDuesData);
                }
            }

            if (isDueForTransfer && transactionAmount != null && transactionAmount.compareTo(BigDecimal.ZERO) > 0) {
                processed++;
                if (executeInstruction(data, transactionAmount, transactionDate)) {
                    succeeded++;
                } else {
                    failed++;
                }
            }
        }
        // An insufficient-funds / business-rule failure of one instruction is a normal per-mandate outcome recorded in
        // history, not a job failure: log a run summary instead of throwing (which used to fail the whole batch run).
        log.info("Standing instruction execution finished: processed={}, succeeded={}, failed={}", processed, succeeded, failed);
        return RepeatStatus.FINISHED;
    }

    /**
     * Executes one instruction with full isolation. The transfer runs in its own transaction (rolls back only itself on
     * failure) and the outcome is recorded in a separate committed transaction, so a failing instruction never reverts
     * a sibling and always leaves a durable history row. Returns {@code true} on success.
     */
    private boolean executeInstruction(final StandingInstructionData data, final BigDecimal transactionAmount,
            final LocalDate transactionDate) {
        final SavingsAccount fromSavingsAccount = null;
        final boolean isRegularTransaction = true;
        final boolean isExceptionForBalanceCheck = false;
        final AccountTransferDTO accountTransferDTO = new AccountTransferDTO(transactionDate, transactionAmount, data.getFromAccountType(),
                data.getToAccountType(), data.getFromAccount().getId(), data.getToAccount().getId(),
                data.getName() + " Standing instruction trasfer ", null, null, null, null, data.toTransferType(), null, null,
                data.getTransferType().getValue(), null, null, ExternalId.empty(), null, null, fromSavingsAccount, isRegularTransaction,
                isExceptionForBalanceCheck);
        try {
            standingInstructionExecutionService.execute(accountTransferDTO, data.getId(), transactionDate, transactionAmount);
            return true;
        } catch (final InsufficientAccountBalanceException e) {
            recordFailure(data, "InsufficientAccountBalance Exception ", e);
        } catch (final PlatformApiDataValidationException e) {
            recordFailure(data, "Validation exception while trasfering funds " + e.getDefaultUserMessage(), e);
        } catch (final AbstractPlatformServiceUnavailableException e) {
            recordFailure(data, "Platform exception while trasfering funds " + e.getDefaultUserMessage(), e);
        } catch (final RuntimeException e) {
            recordFailure(data, "Exception while trasfering funds " + e.getMessage(), e);
        }
        return false;
    }

    private void recordFailure(final StandingInstructionData data, final String errorLog, final RuntimeException cause) {
        log.error("Standing instruction {} (from {} to {}) failed: {}", data.getId(), data.getFromAccount().getId(),
                data.getToAccount().getId(), errorLog, cause);
        try {
            standingInstructionExecutionService.recordFailure(data.getId(), errorLog);
        } catch (final RuntimeException e) {
            // A history-write failure must never abort the remaining instructions; log and continue.
            log.error("Failed to record failure history for standing instruction {}", data.getId(), e);
        }
    }

    public boolean isDueForTransfer(StandingInstructionDuesData standingInstructionDuesData) {
        return standingInstructionDuesData.dueDate() != null
                && !standingInstructionDuesData.dueDate().isAfter(LocalDate.now(DateUtils.getDateTimeZoneOfTenant()));
    }

}
