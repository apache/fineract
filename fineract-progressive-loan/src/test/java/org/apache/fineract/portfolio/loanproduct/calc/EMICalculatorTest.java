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
package org.apache.fineract.portfolio.loanproduct.calc;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EMICalculatorTest {

    private static EMICalculator emiCalculator = new EMICalculator();

    private static MockedStatic<MoneyHelper> moneyHelper = Mockito.mockStatic(MoneyHelper.class);

    private static List<LoanRepaymentScheduleInstallment> periods;
    private final BigDecimal interestRate = BigDecimal.valueOf(0.094822);
    private final BigDecimal principal = BigDecimal.valueOf(100);

    @BeforeAll
    public static void init() {
        periods = new ArrayList<>();
        LocalDate startDate = LocalDate.of(2024, 01, 1);
        periods.add(createPeriod(1, startDate, startDate.plusMonths(1)));
        periods.add(createPeriod(2, startDate.plusMonths(1), startDate.plusMonths(2)));
        periods.add(createPeriod(3, startDate.plusMonths(2), startDate.plusMonths(3)));
        periods.add(createPeriod(4, startDate.plusMonths(3), startDate.plusMonths(4)));
        periods.add(createPeriod(5, startDate.plusMonths(4), startDate.plusMonths(5)));
        periods.add(createPeriod(6, startDate.plusMonths(5), startDate.plusMonths(6)));

        // When
        moneyHelper.when(() -> MoneyHelper.getRoundingMode()).thenReturn(RoundingMode.HALF_EVEN);
        moneyHelper.when(() -> MoneyHelper.getMathContext()).thenReturn(new MathContext(12, RoundingMode.HALF_EVEN));
    }

    @Test
    public void testRateFactorFunctionDay365() {
        // Given
        final DaysInYearType daysInYearType = DaysInYearType.DAYS_365;
        final String[] expectedValues = new String[] { "1.00805337534", "1.00753380274", "1.00805337534", "1.00779358904", "1.00805337534",
                "1.00779358904" };

        // Then
        for (LoanRepaymentScheduleInstallment period : periods) {
            final Integer daysInPeriod = Math.toIntExact(DateUtils.getDifferenceInDays(period.getFromDate(), period.getDueDate()));
            final Integer daysInYear = DateUtils.daysInYear(daysInYearType, period.getFromDate());
            BigDecimal rateFactor = emiCalculator.rateFactor(interestRate, daysInPeriod, daysInYear, MoneyHelper.getMathContext());

            Assertions.assertEquals(expectedValues[period.getInstallmentNumber() - 1], rateFactor.toString());
        }
    }

    @Test
    public void testRateFactorFunctionActual() {
        // Given
        final DaysInYearType daysInYearType = DaysInYearType.ACTUAL;
        final String[] expectedValues = new String[] { "1.00803137158", "1.00751321858", "1.00803137158", "1.00777229508", "1.00803137158",
                "1.00777229508" };

        // Then
        for (LoanRepaymentScheduleInstallment period : periods) {
            final Integer daysInPeriod = Math.toIntExact(DateUtils.getDifferenceInDays(period.getFromDate(), period.getDueDate()));
            final Integer daysInYear = DateUtils.daysInYear(daysInYearType, period.getFromDate());

            BigDecimal rateFactor = emiCalculator.rateFactor(interestRate, daysInPeriod, daysInYear, MoneyHelper.getMathContext());

            Assertions.assertEquals(expectedValues[period.getInstallmentNumber() - 1], rateFactor.toString());
        }
    }

    @Test
    public void testFnValueFunctionDay365() {
        // Given
        final DaysInYearType daysInYearType = DaysInYearType.DAYS_365;
        final MathContext mc = MoneyHelper.getMathContext();
        final String[] expectedValues = new String[] { "1.00000000000", "2.00753380274", "3.02370122596", "4.04726671069", "5.07986086861",
                "6.11945121660" };

        final List<BigDecimal> fnValuesCalculated = new ArrayList<>();
        BigDecimal previousFnValue = BigDecimal.ZERO;
        for (LoanRepaymentScheduleInstallment period : periods) {
            final Integer daysInPeriod = Math.toIntExact(DateUtils.getDifferenceInDays(period.getFromDate(), period.getDueDate()));
            final Integer daysInYear = DateUtils.daysInYear(daysInYearType, period.getFromDate());
            final BigDecimal rateFactor = emiCalculator.rateFactor(interestRate, daysInPeriod, daysInYear, MoneyHelper.getMathContext());

            final BigDecimal currentFnValue = emiCalculator.fnValue(previousFnValue, rateFactor, mc);
            fnValuesCalculated.add(currentFnValue);

            previousFnValue = currentFnValue;
        }

        int idx = 0;
        for (BigDecimal fnValue : fnValuesCalculated) {
            Assertions.assertEquals(expectedValues[idx++], fnValue.toString());
        }
    }

    @Test
    public void testEMICalculation() {
        final MathContext mc = MoneyHelper.getMathContext();
        final List<Integer> repaymentPeriodDays = Arrays.asList(31, 29, 31, 30, 31, 30);

        final EMICalculationResult result = emiCalculator.calculateEMI(repaymentPeriodDays, principal, interestRate,
                DaysInYearType.DAYS_365.getValue(), mc);

        // 17.13
        Assertions.assertEquals(BigDecimal.valueOf(17.1293512777), result.getEqualMonthlyInstallmentValue());

        Assertions.assertEquals(BigDecimal.valueOf(0.00805337534), result.getRateFactorMinus1ForRepaymentPeriod(1));
        Assertions.assertEquals(BigDecimal.valueOf(0.00753380274), result.getRateFactorMinus1ForRepaymentPeriod(2));
        Assertions.assertEquals(BigDecimal.valueOf(0.00805337534), result.getRateFactorMinus1ForRepaymentPeriod(3));
        Assertions.assertEquals(BigDecimal.valueOf(0.00779358904), result.getRateFactorMinus1ForRepaymentPeriod(4));
        Assertions.assertEquals(BigDecimal.valueOf(0.00805337534), result.getRateFactorMinus1ForRepaymentPeriod(5));
        Assertions.assertEquals(BigDecimal.valueOf(0.00779358904), result.getRateFactorMinus1ForRepaymentPeriod(6));
    }

    @NotNull
    private static LoanRepaymentScheduleInstallment createPeriod(int periodId, LocalDate start, LocalDate end) {
        LoanRepaymentScheduleInstallment period = Mockito.mock(LoanRepaymentScheduleInstallment.class);
        Mockito.when(period.getInstallmentNumber()).thenReturn(periodId);
        Mockito.when(period.getFromDate()).thenReturn(start);
        Mockito.when(period.getDueDate()).thenReturn(end);

        return period;
    }

}
