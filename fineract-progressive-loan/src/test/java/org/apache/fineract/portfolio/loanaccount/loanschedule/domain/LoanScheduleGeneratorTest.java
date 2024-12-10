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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanSchedulePlan;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanSchedulePlanDisbursementPeriod;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanSchedulePlanDownPaymentPeriod;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanSchedulePlanRepaymentPeriod;
import org.apache.fineract.portfolio.loanproduct.calc.ProgressiveEMICalculator;
import org.junit.jupiter.api.Test;

class LoanScheduleGeneratorTest {

    private static final ProgressiveEMICalculator emiCalculator = new ProgressiveEMICalculator();
    private static final ApplicationCurrency APPLICATION_CURRENCY = new ApplicationCurrency("USD", "USD", 2, 1, "USD", "$");
    private static final CurrencyData CURRENCY = APPLICATION_CURRENCY.toData();
    private static final BigDecimal DISBURSEMENT_AMOUNT = BigDecimal.valueOf(192.22);
    private static final BigDecimal DISBURSEMENT_AMOUNT_100 = BigDecimal.valueOf(100);
    private static final BigDecimal NOMINAL_INTEREST_RATE = BigDecimal.valueOf(9.99);
    private static final int NUMBER_OF_REPAYMENTS = 6;
    private static final int REPAYMENT_FREQUENCY = 1;
    private static final String REPAYMENT_FREQUENCY_TYPE = "MONTHS";
    private static final LocalDate DISBURSEMENT_DATE = LocalDate.of(2024, 1, 15);
    private static final MathContext mc = new MathContext(12, RoundingMode.HALF_EVEN);
    private static final BigDecimal DOWN_PAYMENT_PORTION = BigDecimal.valueOf(25);

    @Test
    void testGenerateLoanSchedule() {
        LoanRepaymentScheduleModelData modelData = new LoanRepaymentScheduleModelData(LocalDate.of(2024, 1, 1), CURRENCY,
                DISBURSEMENT_AMOUNT, DISBURSEMENT_DATE, NUMBER_OF_REPAYMENTS, REPAYMENT_FREQUENCY, REPAYMENT_FREQUENCY_TYPE,
                NOMINAL_INTEREST_RATE, false, DaysInMonthType.DAYS_30, DaysInYearType.DAYS_360, null, null, null);

        ScheduledDateGenerator scheduledDateGenerator = new DefaultScheduledDateGenerator();
        ProgressiveLoanScheduleGenerator generator = new ProgressiveLoanScheduleGenerator(scheduledDateGenerator, emiCalculator);

        LoanSchedulePlan loanSchedule = generator.generate(mc, modelData);

        assertEquals(7, loanSchedule.getPeriods().size(), "Expected 7 periods without the downpayment period.");

        checkDisbursementPeriod((LoanSchedulePlanDisbursementPeriod) loanSchedule.getPeriods().get(0), LocalDate.of(2024, 1, 15),
                DISBURSEMENT_AMOUNT, DISBURSEMENT_AMOUNT);

        checkRepaymentPeriod((LoanSchedulePlanRepaymentPeriod) loanSchedule.getPeriods().get(1), 1, LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 2, 1), BigDecimal.valueOf(31.97), BigDecimal.valueOf(0.88), BigDecimal.valueOf(32.85),
                BigDecimal.valueOf(160.25));
        checkRepaymentPeriod((LoanSchedulePlanRepaymentPeriod) loanSchedule.getPeriods().get(2), 2, LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 3, 1), BigDecimal.valueOf(31.52), BigDecimal.valueOf(1.33), BigDecimal.valueOf(32.85),
                BigDecimal.valueOf(128.73));
        checkRepaymentPeriod((LoanSchedulePlanRepaymentPeriod) loanSchedule.getPeriods().get(3), 3, LocalDate.of(2024, 3, 1),
                LocalDate.of(2024, 4, 1), BigDecimal.valueOf(31.78), BigDecimal.valueOf(1.07), BigDecimal.valueOf(32.85),
                BigDecimal.valueOf(96.95));
        checkRepaymentPeriod((LoanSchedulePlanRepaymentPeriod) loanSchedule.getPeriods().get(4), 4, LocalDate.of(2024, 4, 1),
                LocalDate.of(2024, 5, 1), BigDecimal.valueOf(32.04), BigDecimal.valueOf(0.81), BigDecimal.valueOf(32.85),
                BigDecimal.valueOf(64.91));
        checkRepaymentPeriod((LoanSchedulePlanRepaymentPeriod) loanSchedule.getPeriods().get(5), 5, LocalDate.of(2024, 5, 1),
                LocalDate.of(2024, 6, 1), BigDecimal.valueOf(32.31), BigDecimal.valueOf(0.54), BigDecimal.valueOf(32.85),
                BigDecimal.valueOf(32.60));
        checkRepaymentPeriod((LoanSchedulePlanRepaymentPeriod) loanSchedule.getPeriods().get(6), 6, LocalDate.of(2024, 6, 1),
                LocalDate.of(2024, 7, 1), BigDecimal.valueOf(32.60), BigDecimal.valueOf(0.27), BigDecimal.valueOf(32.87), BigDecimal.ZERO);
    }

    @Test
    void testGenerateLoanScheduleWithDownPayment() {
        LoanRepaymentScheduleModelData modelData = new LoanRepaymentScheduleModelData(LocalDate.of(2024, 1, 1), CURRENCY,
                DISBURSEMENT_AMOUNT_100, LocalDate.of(2024, 1, 1), NUMBER_OF_REPAYMENTS, REPAYMENT_FREQUENCY, REPAYMENT_FREQUENCY_TYPE,
                NOMINAL_INTEREST_RATE, true, DaysInMonthType.DAYS_30, DaysInYearType.DAYS_360, DOWN_PAYMENT_PORTION, null, null);

        ScheduledDateGenerator scheduledDateGenerator = new DefaultScheduledDateGenerator();
        ProgressiveLoanScheduleGenerator generator = new ProgressiveLoanScheduleGenerator(scheduledDateGenerator, emiCalculator);

        LoanSchedulePlan loanSchedule = generator.generate(mc, modelData);

        assertEquals(8, loanSchedule.getPeriods().size(), "Expected 8 periods with the downpayment period.");

        checkDisbursementPeriod((LoanSchedulePlanDisbursementPeriod) loanSchedule.getPeriods().get(0), LocalDate.of(2024, 1, 1),
                DISBURSEMENT_AMOUNT_100, DISBURSEMENT_AMOUNT_100);
        checkDownPaymentPeriod((LoanSchedulePlanDownPaymentPeriod) loanSchedule.getPeriods().get(1), 1, LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 1), BigDecimal.valueOf(25.0), BigDecimal.valueOf(25.0), BigDecimal.valueOf(75.0));
        checkRepaymentPeriod((LoanSchedulePlanRepaymentPeriod) loanSchedule.getPeriods().get(2), 2, LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 2, 1), BigDecimal.valueOf(12.25), BigDecimal.valueOf(0.62), BigDecimal.valueOf(12.87),
                BigDecimal.valueOf(62.75));
        checkRepaymentPeriod((LoanSchedulePlanRepaymentPeriod) loanSchedule.getPeriods().get(3), 3, LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 3, 1), BigDecimal.valueOf(12.35), BigDecimal.valueOf(0.52), BigDecimal.valueOf(12.87),
                BigDecimal.valueOf(50.40));
        checkRepaymentPeriod((LoanSchedulePlanRepaymentPeriod) loanSchedule.getPeriods().get(4), 4, LocalDate.of(2024, 3, 1),
                LocalDate.of(2024, 4, 1), BigDecimal.valueOf(12.45), BigDecimal.valueOf(0.42), BigDecimal.valueOf(12.87),
                BigDecimal.valueOf(37.95));
        checkRepaymentPeriod((LoanSchedulePlanRepaymentPeriod) loanSchedule.getPeriods().get(5), 5, LocalDate.of(2024, 4, 1),
                LocalDate.of(2024, 5, 1), BigDecimal.valueOf(12.55), BigDecimal.valueOf(0.32), BigDecimal.valueOf(12.87),
                BigDecimal.valueOf(25.40));
        checkRepaymentPeriod((LoanSchedulePlanRepaymentPeriod) loanSchedule.getPeriods().get(6), 6, LocalDate.of(2024, 5, 1),
                LocalDate.of(2024, 6, 1), BigDecimal.valueOf(12.66), BigDecimal.valueOf(0.21), BigDecimal.valueOf(12.87),
                BigDecimal.valueOf(12.74));
        checkRepaymentPeriod((LoanSchedulePlanRepaymentPeriod) loanSchedule.getPeriods().get(7), 7, LocalDate.of(2024, 6, 1),
                LocalDate.of(2024, 7, 1), BigDecimal.valueOf(12.74), BigDecimal.valueOf(0.11), BigDecimal.valueOf(12.85), BigDecimal.ZERO);
    }

    private void checkDisbursementPeriod(LoanSchedulePlanDisbursementPeriod period, LocalDate disbursementDate, BigDecimal principalAmount,
            BigDecimal outstandingAmount) {
        assertEquals(disbursementDate, period.getPeriodFromDate());
        assertEquals(disbursementDate, period.getPeriodDueDate());
        assertEquals(0, principalAmount.compareTo(period.getPrincipalAmount()));
        assertEquals(0, outstandingAmount.compareTo(period.getOutstandingLoanBalance()));

    }

    private void checkRepaymentPeriod(LoanSchedulePlanRepaymentPeriod period, int expectedPeriodNumber, LocalDate expectedFromDate,
            LocalDate expectedDueDate, BigDecimal expectedPrincipalDue, BigDecimal expectedInterestDue, BigDecimal expectedTotalDue,
            BigDecimal expectedOutstandingLoanBalance) {
        assertEquals(expectedPeriodNumber, period.getPeriodNumber());
        assertEquals(expectedFromDate, period.getPeriodFromDate());
        assertEquals(expectedDueDate, period.getPeriodDueDate());
        assertEquals(0, expectedPrincipalDue.compareTo(period.getPrincipalAmount()));
        assertEquals(0, expectedInterestDue.compareTo(period.getInterestAmount()));
        assertEquals(0, expectedTotalDue.compareTo(period.getTotalDueAmount()));
        assertEquals(0, expectedOutstandingLoanBalance.compareTo(period.getOutstandingLoanBalance()));
    }

    private void checkDownPaymentPeriod(LoanSchedulePlanDownPaymentPeriod period, int expectedPeriodNumber, LocalDate expectedFromDate,
            LocalDate expectedDueDate, BigDecimal expectedPrincipalDue, BigDecimal expectedTotalDue,
            BigDecimal expectedOutstandingLoanBalance) {
        assertEquals(expectedPeriodNumber, period.getPeriodNumber());
        assertEquals(expectedFromDate, period.getPeriodFromDate());
        assertEquals(expectedDueDate, period.getPeriodDueDate());
        assertEquals(0, expectedPrincipalDue.compareTo(period.getPrincipalAmount()));
        assertEquals(0, expectedTotalDue.compareTo(period.getTotalDueAmount()));
        assertEquals(0, expectedOutstandingLoanBalance.compareTo(period.getOutstandingLoanBalance()));
    }
}
