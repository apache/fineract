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
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.DueStandingInstruction;
import org.apache.fineract.portfolio.account.data.StandingInstructionData;
import org.apache.fineract.portfolio.account.data.StandingInstructionDuesData;
import org.apache.fineract.portfolio.account.domain.AccountTransferRecurrenceType;
import org.apache.fineract.portfolio.account.domain.StandingInstructionType;
import org.apache.fineract.portfolio.account.service.StandingInstructionReadPlatformService;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.DefaultScheduledDateGenerator;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.ScheduledDateGenerator;
import org.springframework.batch.item.ItemProcessor;

/**
 * Decides whether an instruction read from the due set is actually payable today and, if so, what it should transfer.
 *
 * <p>
 * The read query can only filter on the validity window and the last run date; whether the recurrence falls on today,
 * and what a dues-based instruction owes, are computed here. Returning {@code null} filters the instruction out of the
 * chunk, so nothing that is not payable ever reaches the write step.
 * </p>
 */
@RequiredArgsConstructor
public class StandingInstructionItemProcessor implements ItemProcessor<StandingInstructionData, DueStandingInstruction> {

    private final StandingInstructionReadPlatformService standingInstructionReadPlatformService;

    @Override
    public DueStandingInstruction process(final StandingInstructionData data) {
        final LocalDate transactionDate = DateUtils.getBusinessLocalDate();
        final AccountTransferRecurrenceType recurrenceType = data.getRecurrenceTypeEnum();
        final StandingInstructionType instructionType = data.getInstructionTypeEnum();

        boolean isDueForTransfer = recurrenceType.isPeriodicRecurrence() && fallsInSchedule(data, transactionDate);
        BigDecimal transactionAmount = data.getAmount();

        if (PortfolioAccountType.LOAN.equals(data.getToAccountTypeEnum())
                && (recurrenceType.isDuesRecurrence() || (isDueForTransfer && instructionType.isDuesAmoutTransfer()))) {
            final StandingInstructionDuesData standingInstructionDuesData = standingInstructionReadPlatformService
                    .retriveLoanDuesData(data.getToAccount().getId());
            if (instructionType.isDuesAmoutTransfer()) {
                transactionAmount = standingInstructionDuesData.totalDueAmount();
            }
            if (recurrenceType.isDuesRecurrence()) {
                isDueForTransfer = isDueForTransfer(standingInstructionDuesData);
            }
        }

        if (!isDueForTransfer || transactionAmount == null || transactionAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return new DueStandingInstruction(data, transactionAmount, transactionDate);
    }

    private boolean fallsInSchedule(final StandingInstructionData data, final LocalDate transactionDate) {
        final ScheduledDateGenerator scheduledDateGenerator = new DefaultScheduledDateGenerator();
        final PeriodFrequencyType frequencyType = data.getRecurrenceFrequencyEnum();
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
        return scheduledDateGenerator.isDateFallsInSchedule(frequencyType, data.getRecurrenceInterval(), startDate, transactionDate);
    }

    public boolean isDueForTransfer(final StandingInstructionDuesData standingInstructionDuesData) {
        return standingInstructionDuesData.dueDate() != null
                && !DateUtils.isAfter(standingInstructionDuesData.dueDate(), DateUtils.getLocalDateOfTenant());
    }
}
