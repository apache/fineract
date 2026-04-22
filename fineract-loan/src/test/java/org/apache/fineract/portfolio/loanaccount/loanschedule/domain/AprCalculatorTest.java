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
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AprCalculatorTest {

    private static final int PRECISION = 19;
    private static final MockedStatic<MoneyHelper> MONEY_HELPER = mockStatic(MoneyHelper.class);
    private static final MathContext MATH_CONTEXT = new MathContext(PRECISION, RoundingMode.HALF_EVEN);

    @Mock
    private PaymentPeriodsInOneYearCalculator paymentPeriodsInOneYearCalculator;

    private AprCalculator aprCalculator;

    @BeforeAll
    static void init() {
        MONEY_HELPER.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
        MONEY_HELPER.when(MoneyHelper::getMathContext).thenReturn(MATH_CONTEXT);
    }

    @BeforeEach
    void setUp() {
        aprCalculator = new AprCalculator(paymentPeriodsInOneYearCalculator);
    }

    @AfterAll
    static void tearDown() {
        MONEY_HELPER.close();
    }

    /**
     * Test DAYS frequency with DaysInYearType.ACTUAL
     *
     * This test verifies the fix for FINERACT-2492 where ACTUAL was incorrectly using value 1
     * instead of delegating to PaymentPeriodsInOneYearCalculator which returns 365.
     */
    @Test
    void testCalculateFrom_DaysFrequency_WithActualDaysInYear() {
        // Given
        when(paymentPeriodsInOneYearCalculator.calculate(PeriodFrequencyType.DAYS)).thenReturn(365);

        BigDecimal interestRatePerPeriod = BigDecimal.valueOf(10.0); // 10% per day
        PeriodFrequencyType interestFrequencyType = PeriodFrequencyType.DAYS;
        DaysInYearType daysInYearType = DaysInYearType.ACTUAL;

        // When
        BigDecimal annualRate = aprCalculator.calculateFrom(interestFrequencyType, interestRatePerPeriod, 1, 1,
                PeriodFrequencyType.DAYS, daysInYearType);

        // Then
        // Annual rate should be 10% * 365 = 3650%
        assertEquals(0, BigDecimal.valueOf(3650.0).compareTo(annualRate),
                "Annual rate should be interestRatePerPeriod * 365 for ACTUAL");
        verify(paymentPeriodsInOneYearCalculator).calculate(PeriodFrequencyType.DAYS);
    }

    /**
     * Test WHOLE_TERM frequency with DAYS repayment and DaysInYearType.ACTUAL
     *
     * This is the exact scenario from FINERACT-2492 bug report.
     */
    @Test
    void testCalculateFrom_WholeTermFrequency_WithDaysRepaymentAndActualDaysInYear() {
        // Given
        when(paymentPeriodsInOneYearCalculator.calculate(PeriodFrequencyType.DAYS)).thenReturn(365);

        BigDecimal interestRatePerPeriod = BigDecimal.valueOf(10.0); // 10% whole term
        PeriodFrequencyType interestFrequencyType = PeriodFrequencyType.WHOLE_TERM;
        Integer numberOfRepayments = 3;
        Integer repaymentEvery = 1;
        PeriodFrequencyType repaymentFrequencyType = PeriodFrequencyType.DAYS;
        DaysInYearType daysInYearType = DaysInYearType.ACTUAL;

        // When
        BigDecimal annualRate = aprCalculator.calculateFrom(interestFrequencyType, interestRatePerPeriod, numberOfRepayments,
                repaymentEvery, repaymentFrequencyType, daysInYearType);

        // Then
        // ratePerPeriod = 10% / (3 * 1) = 3.33333333%
        // annualRate = 3.33333333% * 365 = 1216.66666667%
        BigDecimal expectedAnnualRate = BigDecimal.valueOf(10.0).divide(BigDecimal.valueOf(3), 8, java.math.RoundingMode.HALF_EVEN)
                .multiply(BigDecimal.valueOf(365));

        assertEquals(0, expectedAnnualRate.compareTo(annualRate), "Annual rate calculation should use 365 days for ACTUAL");
        verify(paymentPeriodsInOneYearCalculator).calculate(PeriodFrequencyType.DAYS);
    }

    /**
     * Test DAYS frequency with DaysInYearType.DAYS_360
     *
     * Verify that DAYS_360 works correctly and uses value 360 directly.
     */
    @Test
    void testCalculateFrom_DaysFrequency_WithDays360() {
        // Given
        BigDecimal interestRatePerPeriod = BigDecimal.valueOf(10.0);
        PeriodFrequencyType interestFrequencyType = PeriodFrequencyType.DAYS;
        DaysInYearType daysInYearType = DaysInYearType.DAYS_360;

        // When
        BigDecimal annualRate = aprCalculator.calculateFrom(interestFrequencyType, interestRatePerPeriod, 1, 1, PeriodFrequencyType.DAYS,
                daysInYearType);

        // Then
        // Annual rate should be 10% * 360 = 3600%
        assertEquals(0, BigDecimal.valueOf(3600.0).compareTo(annualRate), "Annual rate should use 360 days for DAYS_360");
    }

    /**
     * Test DAYS frequency with DaysInYearType.DAYS_364
     *
     * Verify that DAYS_364 works correctly and uses value 364 directly.
     */
    @Test
    void testCalculateFrom_DaysFrequency_WithDays364() {
        // Given
        BigDecimal interestRatePerPeriod = BigDecimal.valueOf(10.0);
        PeriodFrequencyType interestFrequencyType = PeriodFrequencyType.DAYS;
        DaysInYearType daysInYearType = DaysInYearType.DAYS_364;

        // When
        BigDecimal annualRate = aprCalculator.calculateFrom(interestFrequencyType, interestRatePerPeriod, 1, 1, PeriodFrequencyType.DAYS,
                daysInYearType);

        // Then
        // Annual rate should be 10% * 364 = 3640%
        assertEquals(0, BigDecimal.valueOf(3640.0).compareTo(annualRate), "Annual rate should use 364 days for DAYS_364");
    }

    /**
     * Test DAYS frequency with DaysInYearType.DAYS_365
     *
     * Verify that DAYS_365 works correctly and uses value 365 directly.
     */
    @Test
    void testCalculateFrom_DaysFrequency_WithDays365() {
        // Given
        BigDecimal interestRatePerPeriod = BigDecimal.valueOf(10.0);
        PeriodFrequencyType interestFrequencyType = PeriodFrequencyType.DAYS;
        DaysInYearType daysInYearType = DaysInYearType.DAYS_365;

        // When
        BigDecimal annualRate = aprCalculator.calculateFrom(interestFrequencyType, interestRatePerPeriod, 1, 1, PeriodFrequencyType.DAYS,
                daysInYearType);

        // Then
        // Annual rate should be 10% * 365 = 3650%
        assertEquals(0, BigDecimal.valueOf(3650.0).compareTo(annualRate), "Annual rate should use 365 days for DAYS_365");
    }

    /**
     * Test WHOLE_TERM frequency with WEEKS repayment and DaysInYearType.ACTUAL
     *
     * Verify that ACTUAL doesn't affect non-DAYS repayment frequencies.
     */
    @Test
    void testCalculateFrom_WholeTermFrequency_WithWeeksRepaymentAndActualDaysInYear() {
        // Given
        BigDecimal interestRatePerPeriod = BigDecimal.valueOf(10.0);
        PeriodFrequencyType interestFrequencyType = PeriodFrequencyType.WHOLE_TERM;
        Integer numberOfRepayments = 4;
        Integer repaymentEvery = 1;
        PeriodFrequencyType repaymentFrequencyType = PeriodFrequencyType.WEEKS;
        DaysInYearType daysInYearType = DaysInYearType.ACTUAL;

        // When
        BigDecimal annualRate = aprCalculator.calculateFrom(interestFrequencyType, interestRatePerPeriod, numberOfRepayments,
                repaymentEvery, repaymentFrequencyType, daysInYearType);

        // Then
        // ratePerPeriod = 10% / (4 * 1) = 2.5%
        // annualRate = 2.5% * 52 = 130%
        BigDecimal expectedAnnualRate = BigDecimal.valueOf(10.0).divide(BigDecimal.valueOf(4), 8, java.math.RoundingMode.HALF_EVEN)
                .multiply(BigDecimal.valueOf(52));

        assertEquals(0, expectedAnnualRate.compareTo(annualRate), "Annual rate for WEEKS should use 52 weeks");
    }

    /**
     * Test WHOLE_TERM frequency with MONTHS repayment and DaysInYearType.ACTUAL
     *
     * Verify that ACTUAL doesn't affect non-DAYS repayment frequencies.
     */
    @Test
    void testCalculateFrom_WholeTermFrequency_WithMonthsRepaymentAndActualDaysInYear() {
        // Given
        BigDecimal interestRatePerPeriod = BigDecimal.valueOf(12.0);
        PeriodFrequencyType interestFrequencyType = PeriodFrequencyType.WHOLE_TERM;
        Integer numberOfRepayments = 6;
        Integer repaymentEvery = 1;
        PeriodFrequencyType repaymentFrequencyType = PeriodFrequencyType.MONTHS;
        DaysInYearType daysInYearType = DaysInYearType.ACTUAL;

        // When
        BigDecimal annualRate = aprCalculator.calculateFrom(interestFrequencyType, interestRatePerPeriod, numberOfRepayments,
                repaymentEvery, repaymentFrequencyType, daysInYearType);

        // Then
        // ratePerPeriod = 12% / (6 * 1) = 2%
        // annualRate = 2% * 12 = 24%
        BigDecimal expectedAnnualRate = BigDecimal.valueOf(12.0).divide(BigDecimal.valueOf(6), 8, java.math.RoundingMode.HALF_EVEN)
                .multiply(BigDecimal.valueOf(12));

        assertEquals(0, expectedAnnualRate.compareTo(annualRate), "Annual rate for MONTHS should use 12 months");
    }

    /**
     * Test WEEKS frequency
     */
    @Test
    void testCalculateFrom_WeeksFrequency() {
        // Given
        BigDecimal interestRatePerPeriod = BigDecimal.valueOf(2.0);
        PeriodFrequencyType interestFrequencyType = PeriodFrequencyType.WEEKS;

        // When
        BigDecimal annualRate = aprCalculator.calculateFrom(interestFrequencyType, interestRatePerPeriod, 1, 1, PeriodFrequencyType.WEEKS,
                DaysInYearType.ACTUAL);

        // Then
        // Annual rate should be 2% * 52 = 104%
        assertEquals(0, BigDecimal.valueOf(104.0).compareTo(annualRate), "Annual rate for WEEKS should multiply by 52");
    }

    /**
     * Test MONTHS frequency
     */
    @Test
    void testCalculateFrom_MonthsFrequency() {
        // Given
        BigDecimal interestRatePerPeriod = BigDecimal.valueOf(2.0);
        PeriodFrequencyType interestFrequencyType = PeriodFrequencyType.MONTHS;

        // When
        BigDecimal annualRate = aprCalculator.calculateFrom(interestFrequencyType, interestRatePerPeriod, 1, 1, PeriodFrequencyType.MONTHS,
                DaysInYearType.ACTUAL);

        // Then
        // Annual rate should be 2% * 12 = 24%
        assertEquals(0, BigDecimal.valueOf(24.0).compareTo(annualRate), "Annual rate for MONTHS should multiply by 12");
    }

    /**
     * Test YEARS frequency
     */
    @Test
    void testCalculateFrom_YearsFrequency() {
        // Given
        BigDecimal interestRatePerPeriod = BigDecimal.valueOf(5.0);
        PeriodFrequencyType interestFrequencyType = PeriodFrequencyType.YEARS;

        // When
        BigDecimal annualRate = aprCalculator.calculateFrom(interestFrequencyType, interestRatePerPeriod, 1, 1, PeriodFrequencyType.YEARS,
                DaysInYearType.ACTUAL);

        // Then
        // Annual rate should be 5% * 1 = 5%
        assertEquals(0, BigDecimal.valueOf(5.0).compareTo(annualRate), "Annual rate for YEARS should multiply by 1");
    }

    /**
     * Test bug scenario with realistic values from FINERACT-2492
     *
     * Principal: 3,400
     * Interest rate: 10% WHOLE_TERM
     * Repayments: 3 daily
     * Expected interest per installment: 113.33
     */
    @Test
    void testCalculateFrom_BugReproductionScenario() {
        // Given
        when(paymentPeriodsInOneYearCalculator.calculate(PeriodFrequencyType.DAYS)).thenReturn(365);

        BigDecimal interestRatePerPeriod = BigDecimal.valueOf(10.0); // 10% whole term
        PeriodFrequencyType interestFrequencyType = PeriodFrequencyType.WHOLE_TERM;
        Integer numberOfRepayments = 3;
        Integer repaymentEvery = 1;
        PeriodFrequencyType repaymentFrequencyType = PeriodFrequencyType.DAYS;
        DaysInYearType daysInYearType = DaysInYearType.ACTUAL;

        // When
        BigDecimal annualRate = aprCalculator.calculateFrom(interestFrequencyType, interestRatePerPeriod, numberOfRepayments,
                repaymentEvery, repaymentFrequencyType, daysInYearType);

        // Then
        // The bug was causing annual rate to be 3.333% instead of 1216.667%
        // Verify it's much greater than 100 (definitely not 3.333)
        assertEquals(true, annualRate.compareTo(BigDecimal.valueOf(1000)) > 0,
                "Annual rate should be > 1000% (bug was producing 3.333%)");

        // Verify exact expected value: 10/3 * 365 = 1216.66666667
        BigDecimal expectedRate = BigDecimal.valueOf(10.0).divide(BigDecimal.valueOf(3), 8, java.math.RoundingMode.HALF_EVEN)
                .multiply(BigDecimal.valueOf(365));
        assertEquals(0, expectedRate.compareTo(annualRate), "Annual rate should be exactly 1216.67%");
    }
}
