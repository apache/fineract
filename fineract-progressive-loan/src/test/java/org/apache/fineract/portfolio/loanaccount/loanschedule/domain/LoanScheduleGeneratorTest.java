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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.loanproduct.calc.ProgressiveEMICalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanScheduleGeneratorTest {

    private static final ProgressiveEMICalculator emiCalculator = new ProgressiveEMICalculator();
    private static final ApplicationCurrency APPLICATION_CURRENCY = new ApplicationCurrency("USD", "USD", 2, 1, "USD", "$");
    private static final BigDecimal DISBURSEMENT_AMOUNT = BigDecimal.valueOf(192.22);
    private static final BigDecimal NOMINAL_INTEREST_RATE = BigDecimal.valueOf(9.99);
    private static final int NUMBER_OF_REPAYMENTS = 6;
    private static final int REPAYMENT_FREQUENCY = 1;
    private static final String REPAYMENT_FREQUENCY_TYPE = "MONTHS";
    private static final LocalDate DISBURSEMENT_DATE = LocalDate.of(2024, 1, 15);
    private static final MathContext mc = new MathContext(12, RoundingMode.HALF_EVEN);

    @Test
    void testGenerateLoanSchedule() {
        List<LoanScheduleModelPeriod> periods = getLoanScheduleModelPeriods();

        assertEquals(7, periods.size(), "Expected 7 periods including the downpayment period.");

        LoanScheduleModelDisbursementPeriod disbursementPeriod = (LoanScheduleModelDisbursementPeriod) periods.get(0);
        assertNotNull(disbursementPeriod);

        checkPeriod(periods.get(1), 1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1), BigDecimal.valueOf(31.97),
                BigDecimal.valueOf(0.88), BigDecimal.valueOf(32.85), BigDecimal.valueOf(160.25));
        checkPeriod(periods.get(2), 2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1), BigDecimal.valueOf(31.52),
                BigDecimal.valueOf(1.33), BigDecimal.valueOf(32.85), BigDecimal.valueOf(128.73));
        checkPeriod(periods.get(3), 3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1), BigDecimal.valueOf(31.78),
                BigDecimal.valueOf(1.07), BigDecimal.valueOf(32.85), BigDecimal.valueOf(96.95));
        checkPeriod(periods.get(4), 4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1), BigDecimal.valueOf(32.04),
                BigDecimal.valueOf(0.81), BigDecimal.valueOf(32.85), BigDecimal.valueOf(64.91));
        checkPeriod(periods.get(5), 5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1), BigDecimal.valueOf(32.31),
                BigDecimal.valueOf(0.54), BigDecimal.valueOf(32.85), BigDecimal.valueOf(32.60));
        checkPeriod(periods.get(6), 6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1), BigDecimal.valueOf(32.60),
                BigDecimal.valueOf(0.27), BigDecimal.valueOf(32.87), BigDecimal.ZERO);
    }

    private static List<LoanScheduleModelPeriod> getLoanScheduleModelPeriods() {
        LoanRepaymentScheduleModelData modelData = new LoanRepaymentScheduleModelData(LocalDate.of(2024, 1, 1), APPLICATION_CURRENCY,
                DISBURSEMENT_AMOUNT, DISBURSEMENT_DATE, NUMBER_OF_REPAYMENTS, REPAYMENT_FREQUENCY, REPAYMENT_FREQUENCY_TYPE,
                NOMINAL_INTEREST_RATE, true, DaysInMonthType.DAYS_30, DaysInYearType.DAYS_360, null, null, null);

        ScheduledDateGenerator scheduledDateGenerator = new DefaultScheduledDateGenerator();
        ProgressiveLoanScheduleGenerator generator = new ProgressiveLoanScheduleGenerator(scheduledDateGenerator, emiCalculator);

        LoanScheduleModel loanSchedule = generator.generate(mc, modelData);
        return loanSchedule.getPeriods();
    }

    private void checkPeriod(LoanScheduleModelPeriod period, int expectedPeriodNumber, LocalDate expectedFromDate,
            LocalDate expectedDueDate, BigDecimal expectedPrincipalDue, BigDecimal expectedInterestDue, BigDecimal expectedTotalDue,
            BigDecimal expectedOutstandingLoanBalance) {
        LoanScheduleModelRepaymentPeriod repaymentPeriod = (LoanScheduleModelRepaymentPeriod) period;
        assertEquals(expectedPeriodNumber, repaymentPeriod.getPeriodNumber());
        assertEquals(expectedFromDate, repaymentPeriod.getFromDate());
        assertEquals(expectedDueDate, repaymentPeriod.getDueDate());
        assertEquals(0, expectedPrincipalDue.compareTo(repaymentPeriod.getPrincipalDue().getAmount()));
        assertEquals(0, expectedInterestDue.compareTo(repaymentPeriod.getInterestDue().getAmount()));
        assertEquals(0, expectedTotalDue.compareTo(repaymentPeriod.getTotalDue().getAmount()));
        assertEquals(0, expectedOutstandingLoanBalance.compareTo(repaymentPeriod.getOutstandingLoanBalance().getAmount()));
    }
}
