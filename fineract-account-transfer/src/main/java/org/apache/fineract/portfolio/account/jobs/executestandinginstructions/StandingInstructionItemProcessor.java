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
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
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
import org.springframework.batch.infrastructure.item.ItemProcessor;

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

        boolean isDueForTransfer = recurrenceType.isPeriodicRecurrence() && isDueOnDate(data.getRecurrenceFrequencyEnum(),
                data.getRecurrenceInterval(), data.getValidFrom(), data.getRecurrenceOnDay(), data.getRecurrenceOnMonth(), transactionDate);
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

    /**
     * A recurrence day the month does not have (e.g. the 31st in April) falls on the last day of that month, the same
     * way savings charges handle their fee day. Yearly recurrence is monthly recurrence every 12 months, starting in
     * the recurrence month. Daily and weekly recurrence keep using {@link DefaultScheduledDateGenerator}, which loan
     * schedules share.
     */
    static boolean isDueOnDate(final PeriodFrequencyType frequencyType, final int recurrenceInterval, final LocalDate validFrom,
            final int recurrenceOnDay, final int recurrenceOnMonth, final LocalDate date) {
        if (!frequencyType.isMonthly() && !frequencyType.isYearly()) {
            final ScheduledDateGenerator scheduledDateGenerator = new DefaultScheduledDateGenerator();
            return scheduledDateGenerator.isDateFallsInSchedule(frequencyType, recurrenceInterval, validFrom, date);
        }
        final int monthsPerPeriod = frequencyType.isYearly() ? 12 : 1;
        YearMonth firstMonth = frequencyType.isYearly() ? YearMonth.of(validFrom.getYear(), recurrenceOnMonth) : YearMonth.from(validFrom);
        if (DateUtils.isBefore(dueDateIn(firstMonth, recurrenceOnDay), validFrom)) {
            firstMonth = firstMonth.plusMonths(monthsPerPeriod);
        }
        final YearMonth month = YearMonth.from(date);
        final long monthsSinceFirst = firstMonth.until(month, ChronoUnit.MONTHS);
        return monthsSinceFirst >= 0 && monthsSinceFirst % ((long) recurrenceInterval * monthsPerPeriod) == 0
                && DateUtils.isEqual(dueDateIn(month, recurrenceOnDay), date);
    }

    private static LocalDate dueDateIn(final YearMonth month, final int recurrenceOnDay) {
        return month.atDay(Math.min(recurrenceOnDay, month.lengthOfMonth()));
    }

    public boolean isDueForTransfer(final StandingInstructionDuesData standingInstructionDuesData) {
        return standingInstructionDuesData.dueDate() != null
                && !DateUtils.isAfter(standingInstructionDuesData.dueDate(), DateUtils.getLocalDateOfTenant());
    }
}
