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
package org.apache.fineract.portfolio.loanaccount.loanschedule.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearCustomStrategyType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanSchedulePlan;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanSchedulePlanDisbursementPeriod;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanSchedulePlanPeriod;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanSchedulePlanRepaymentPeriod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmbeddableProgressiveLoanScheduleGeneratorTest {

    @Test
    void testGenerate() {
        MathContext mc = new MathContext(12, RoundingMode.HALF_UP);
        EmbeddableProgressiveLoanScheduleGenerator calculator = new EmbeddableProgressiveLoanScheduleGenerator();

        final CurrencyData currency = new CurrencyData("usd", "US Dollar", 2, null, "usd", "$");
        final LocalDate startDate = LocalDate.of(2024, 1, 1);
        final LocalDate disbursementDate = LocalDate.of(2024, 1, 1);
        final BigDecimal disbursedAmount = BigDecimal.valueOf(100);

        final int noRepayments = 6;
        final int repaymentFrequency = 1;
        final String repaymentFrequencyType = "MONTHS";
        final BigDecimal downPaymentPercentage = BigDecimal.ZERO;
        final boolean isDownPaymentEnabled = BigDecimal.ZERO.compareTo(downPaymentPercentage) != 0;
        final BigDecimal annualNominalInterestRate = BigDecimal.valueOf(7.0);
        final DaysInMonthType daysInMonthType = DaysInMonthType.DAYS_30;
        final DaysInYearType daysInYearType = DaysInYearType.DAYS_360;
        final Integer installmentAmountInMultiplesOf = null;
        final Integer fixedLength = null;
        final Boolean interestRecognitionOnDisbursementDate = false;
        final DaysInYearCustomStrategyType daysInYearCustomStrategy = null;
        final InterestMethod interestMethod = InterestMethod.DECLINING_BALANCE;
        final boolean allowPartialPeriodInterestCalculation = true;

        var config = new LoanRepaymentScheduleModelData(startDate, currency, disbursedAmount, disbursementDate, noRepayments,
                repaymentFrequency, repaymentFrequencyType, annualNominalInterestRate, isDownPaymentEnabled, daysInMonthType,
                daysInYearType, downPaymentPercentage, installmentAmountInMultiplesOf, fixedLength, interestRecognitionOnDisbursementDate,
                daysInYearCustomStrategy, interestMethod, allowPartialPeriodInterestCalculation);

        final LoanSchedulePlan plan = calculator.generate(mc, config);

        Assertions.assertEquals(182, plan.getLoanTermInDays());
        Assertions.assertEquals(100.00, toDouble(plan.getTotalDisbursedAmount()));
        Assertions.assertEquals(2.05, toDouble(plan.getTotalInterestAmount()));
        Assertions.assertEquals(102.05, toDouble(plan.getTotalRepaymentAmount()));

        Assertions.assertEquals(7, plan.getPeriods().size());
        checkPeriod(plan.getPeriods().get(0), LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 1), 100.0, 100.0);
        checkPeriod(plan.getPeriods().get(1), 1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1), 16.43, 0.58, 0.0, 0.0, 17.01, 83.57,
                85.04);
        checkPeriod(plan.getPeriods().get(2), 2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1), 16.52, 0.49, 0.0, 0.0, 17.01, 67.05,
                68.03);
        checkPeriod(plan.getPeriods().get(3), 3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1), 16.62, 0.39, 0.0, 0.0, 17.01, 50.43,
                51.02);
        checkPeriod(plan.getPeriods().get(4), 4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1), 16.72, 0.29, 0.0, 0.0, 17.01, 33.71,
                34.01);
        checkPeriod(plan.getPeriods().get(5), 5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1), 16.81, 0.20, 0.0, 0.0, 17.01, 16.90,
                17.00);
        checkPeriod(plan.getPeriods().get(6), 6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1), 16.90, 0.10, 0.0, 0.0, 17.00, 0.0,
                0.0);
    }

    private static void checkPeriod(LoanSchedulePlanPeriod period, LocalDate fromDate, LocalDate dueDate, double principal,
            double outstandingBalance) {
        checkPeriod(period, null, fromDate, dueDate, principal, 0.0, 0.0, 0.0, 0.0, outstandingBalance, 0.0);
    }

    private static void checkPeriod(LoanSchedulePlanPeriod period, Integer periodNumber, LocalDate fromDate, LocalDate dueDate,
            double principal, double interest, double fee, double penalty, double totalDue, double outstandingBalance,
            double totalOutstandingBalance) {
        Assertions.assertEquals(periodNumber, period.periodNumber());
        Assertions.assertEquals(fromDate, period.periodFromDate());
        Assertions.assertEquals(dueDate, period.periodDueDate());
        if (period instanceof LoanSchedulePlanDisbursementPeriod disbursementPeriod) {
            Assertions.assertEquals(principal, toDouble(disbursementPeriod.getPrincipalAmount()));
            Assertions.assertEquals(outstandingBalance, toDouble(disbursementPeriod.getOutstandingLoanBalance()));
        } else if (period instanceof LoanSchedulePlanRepaymentPeriod repaymentPeriod) {
            Assertions.assertEquals(principal, toDouble(repaymentPeriod.getPrincipalAmount()));
            Assertions.assertEquals(interest, toDouble(repaymentPeriod.getInterestAmount()));
            Assertions.assertEquals(fee, toDouble(repaymentPeriod.getFeeAmount()));
            Assertions.assertEquals(penalty, toDouble(repaymentPeriod.getPenaltyAmount()));
            Assertions.assertEquals(totalDue, toDouble(repaymentPeriod.getTotalDueAmount()));
            Assertions.assertEquals(outstandingBalance, toDouble(repaymentPeriod.getOutstandingLoanBalance()));
            Assertions.assertEquals(totalOutstandingBalance, toDouble(repaymentPeriod.getTotalOutstandingLoanBalance()));
        }
    }

    private static double toDouble(final BigDecimal value) {
        return value == null ? 0.0 : value.doubleValue();
    }
}
