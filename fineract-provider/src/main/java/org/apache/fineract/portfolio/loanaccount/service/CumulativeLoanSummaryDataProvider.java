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
package org.apache.fineract.portfolio.loanaccount.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.data.LoanSummaryData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionBalance;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;
import org.springframework.stereotype.Component;

@Component
public class CumulativeLoanSummaryDataProvider extends CommonLoanSummaryDataProvider {

    @Override
    public boolean accept(String loanProcessingStrategyCode) {
        return !loanProcessingStrategyCode.equalsIgnoreCase("advanced-payment-allocation-strategy");
    }

    @Override
    public BigDecimal computeTotalUnpaidPayableNotDueInterestAmountOnActualPeriod(final Loan loan,
            final Collection<LoanSchedulePeriodData> periods, final LocalDate businessDate, final CurrencyData currency) {
        // Find the current Period (If exists one) based on the Business date
        final Optional<LoanSchedulePeriodData> optCurrentPeriod = periods.stream().filter(period -> !period.isDownPaymentPeriod() //
                && period.getPeriod() != null //
                && !businessDate.isBefore(period.getFromDate()) //
                && businessDate.isBefore(period.getDueDate())) //
                .findFirst();

        if (optCurrentPeriod.isPresent()) {
            final LoanSchedulePeriodData currentPeriod = optCurrentPeriod.get();
            final long remainingDays = currentPeriod.getDaysInPeriod()
                    - DateUtils.getDifferenceInDays(currentPeriod.getFromDate(), businessDate);

            return computeAccruedInterestTillDay(currentPeriod, remainingDays, currency);
        }
        // Default value equal to Zero
        return BigDecimal.ZERO;
    }

    @Override
    public LoanSummaryData withTransactionAmountsSummary(Long loanId, LoanSummaryData defaultSummaryData,
            LoanScheduleData repaymentSchedule, Collection<LoanTransactionBalance> loanTransactionBalances) {
        Loan loan = null;
        return super.withTransactionAmountsSummary(loan, defaultSummaryData, repaymentSchedule, loanTransactionBalances);
    }

    private static BigDecimal computeAccruedInterestTillDay(final LoanSchedulePeriodData period, final long untilDay,
            final CurrencyData currency) {
        Integer remainingDays = period.getDaysInPeriod();
        BigDecimal totalAccruedInterest = BigDecimal.ZERO;
        while (remainingDays > untilDay) {
            final BigDecimal accruedInterest = period.getInterestDue().subtract(totalAccruedInterest)
                    .divide(BigDecimal.valueOf(remainingDays), MoneyHelper.getMathContext());
            totalAccruedInterest = totalAccruedInterest.add(accruedInterest);
            remainingDays--;
        }

        totalAccruedInterest = totalAccruedInterest.subtract(period.getInterestPaid()).subtract(period.getInterestWaived());
        if (MathUtil.isLessThanZero(totalAccruedInterest)) {
            // Set Zero If the Interest Paid + Waived is greather than Interest Accrued
            totalAccruedInterest = BigDecimal.ZERO;
        }

        return Money.of(currency, totalAccruedInterest).getAmount();
    }
}
