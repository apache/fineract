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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.AbstractPlatformServiceUnavailableException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.portfolio.account.data.AccountTransferDTO;
import org.apache.fineract.portfolio.account.data.StandingInstructionData;
import org.apache.fineract.portfolio.account.data.StandingInstructionDuesData;
import org.apache.fineract.portfolio.account.domain.AccountTransferRecurrenceType;
import org.apache.fineract.portfolio.account.domain.AccountTransferStandingInstructionCustomRepositoryImpl;
import org.apache.fineract.portfolio.account.domain.AccountTransferStandingInstructionsHistoryCustomRepositoryImpl;
import org.apache.fineract.portfolio.account.domain.StandingInstructionStatus;
import org.apache.fineract.portfolio.account.domain.StandingInstructionType;
import org.apache.fineract.portfolio.account.service.AccountTransfersWritePlatformService;
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
    private final AccountTransfersWritePlatformService accountTransfersWritePlatformService;
    private final AccountTransferStandingInstructionCustomRepositoryImpl accountTransferStandingInstructionCustomRepositoryImpl;
    private final AccountTransferStandingInstructionsHistoryCustomRepositoryImpl accountTransferStandingInstructionsHistoryCustomRepositoryImpl;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws JobExecutionException {
        Collection<StandingInstructionData> instructionData = standingInstructionReadPlatformService
                .retrieveAll(StandingInstructionStatus.ACTIVE.getValue());
        List<Throwable> errors = new ArrayList<>();
        for (StandingInstructionData data : instructionData) {
            boolean isDueForTransfer = false;
            AccountTransferRecurrenceType recurrenceType = data.recurrenceType();
            StandingInstructionType instructionType = data.instructionType();
            LocalDate transactionDate = DateUtils.getBusinessLocalDate();
            if (recurrenceType.isPeriodicRecurrence()) {
                final ScheduledDateGenerator scheduledDateGenerator = new DefaultScheduledDateGenerator();
                PeriodFrequencyType frequencyType = data.recurrenceFrequency();
                LocalDate startDate = data.validFrom();
                if (frequencyType.isMonthly()) {
                    startDate = startDate.withDayOfMonth(data.recurrenceOnDay());
                    if (DateUtils.isBefore(startDate, data.validFrom())) {
                        startDate = startDate.plusMonths(1);
                    }
                } else if (frequencyType.isYearly()) {
                    startDate = startDate.withDayOfMonth(data.recurrenceOnDay()).withMonth(data.recurrenceOnMonth());
                    if (DateUtils.isBefore(startDate, data.validFrom())) {
                        startDate = startDate.plusYears(1);
                    }
                }
                isDueForTransfer = scheduledDateGenerator.isDateFallsInSchedule(frequencyType, data.recurrenceInterval(), startDate,
                        transactionDate);

            }
            BigDecimal transactionAmount = data.amount();
            if (data.toAccountType().isLoanAccount()
                    && (recurrenceType.isDuesRecurrence() || (isDueForTransfer && instructionType.isDuesAmoutTransfer()))) {
                StandingInstructionDuesData standingInstructionDuesData = standingInstructionReadPlatformService
                        .retriveLoanDuesData(data.toAccount().getId());
                if (data.instructionType().isDuesAmoutTransfer()) {
                    transactionAmount = standingInstructionDuesData.totalDueAmount();
                }
                if (recurrenceType.isDuesRecurrence()) {
                    isDueForTransfer = isDueForTransfer(standingInstructionDuesData);
                }
            }

            if (isDueForTransfer && transactionAmount != null && transactionAmount.compareTo(BigDecimal.ZERO) > 0) {
                final SavingsAccount fromSavingsAccount = null;
                final boolean isRegularTransaction = true;
                final boolean isExceptionForBalanceCheck = false;
                AccountTransferDTO accountTransferDTO = new AccountTransferDTO(transactionDate, transactionAmount, data.fromAccountType(),
                        data.toAccountType(), data.fromAccount().getId(), data.toAccount().getId(),
                        data.name() + " Standing instruction trasfer ", null, null, null, null, data.toTransferType(), null, null,
                        data.transferType().getValue(), null, null, ExternalId.empty(), null, null, fromSavingsAccount,
                        isRegularTransaction, isExceptionForBalanceCheck);
                final boolean transferCompleted = transferAmount(errors, accountTransferDTO, data.getId());

                if (transferCompleted) {
                    accountTransferStandingInstructionCustomRepositoryImpl.updateLastRunDateById(data.getId(), transactionDate);
                }

            }
        }
        if (!errors.isEmpty()) {
            throw new JobExecutionException(errors);
        }
        return RepeatStatus.FINISHED;
    }

    private boolean transferAmount(final List<Throwable> errors, final AccountTransferDTO accountTransferDTO, final Long instructionId) {
        boolean transferCompleted = true;
        final StringBuilder errorLog = new StringBuilder();

        try {
            accountTransfersWritePlatformService.transferFunds(accountTransferDTO);
        } catch (final PlatformApiDataValidationException e) {
            errors.add(new Exception("Validation exception while transfering funds for standing Instruction id" + instructionId + " from "
                    + accountTransferDTO.getFromAccountId() + " to " + accountTransferDTO.getToAccountId(), e));
            errorLog.append("Validation exception while trasfering funds ").append(e.getDefaultUserMessage());
        } catch (final InsufficientAccountBalanceException e) {
            errors.add(new Exception("InsufficientAccountBalance Exception while trasfering funds for standing Instruction id"
                    + instructionId + " from " + accountTransferDTO.getFromAccountId() + " to " + accountTransferDTO.getToAccountId(), e));
            errorLog.append("InsufficientAccountBalance Exception ");
        } catch (final AbstractPlatformServiceUnavailableException e) {
            errors.add(new Exception("Platform exception while trasfering funds for standing Instruction id" + instructionId + " from "
                    + accountTransferDTO.getFromAccountId() + " to " + accountTransferDTO.getToAccountId(), e));
            errorLog.append("Platform exception while trasfering funds ").append(e.getDefaultUserMessage());
        } catch (Exception e) {
            errors.add(new Exception("Unhandled System Exception while trasfering funds for standing Instruction id" + instructionId
                    + " from " + accountTransferDTO.getFromAccountId() + " to " + accountTransferDTO.getToAccountId(), e));
            errorLog.append("Exception while trasfering funds ").append(e.getMessage());

        }

        if (!errorLog.isEmpty()) {
            transferCompleted = false;
        }

        accountTransferStandingInstructionsHistoryCustomRepositoryImpl.createNewHistory(instructionId,
                accountTransferDTO.getTransactionAmount(), transferCompleted, String.valueOf(errorLog));

        return transferCompleted;
    }

    public boolean isDueForTransfer(StandingInstructionDuesData standingInstructionDuesData) {
        return standingInstructionDuesData.dueDate() != null
                && !standingInstructionDuesData.dueDate().isAfter(LocalDate.now(DateUtils.getDateTimeZoneOfTenant()));
    }
}
