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
import java.util.List;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanproduct.calc.emi.FnValueFunctions;
import org.apache.fineract.portfolio.loanproduct.calc.ratefactor.RateFactorFunctions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EMICalculationFunctionsTest {

    private static MockedStatic<MoneyHelper> moneyHelper = Mockito.mockStatic(MoneyHelper.class);

    private static List<LoanRepaymentScheduleInstallment> periods;
    private final BigDecimal interestRate = BigDecimal.valueOf(0.09482);

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
        moneyHelper.when(() -> MoneyHelper.getMathContext()).thenReturn(new MathContext(8, RoundingMode.HALF_EVEN));
    }

    @Test
    public void testRateFactorFunctionDay365() {
        // Given
        final DaysInYearType daysInYearType = DaysInYearType.DAYS_365;
        final String[] expectedValues = new String[] { "1.0080532", "1.0075336", "1.0080532", "1.0077934", "1.0080532", "1.0077934" };

        // Then
        for (LoanRepaymentScheduleInstallment period : periods) {
            final Long daysInPeriod = DateUtils.getDifferenceInDays(period.getFromDate(), period.getDueDate());
            final Integer daysInYear = DateUtils.daysInYear(daysInYearType, period.getFromDate());
            BigDecimal rateFactor = RateFactorFunctions.rateFactor(interestRate, daysInPeriod, daysInYear, MoneyHelper.getMathContext());

            Assertions.assertEquals(expectedValues[period.getInstallmentNumber() - 1], rateFactor.toString());
        }
    }

    @Test
    public void testRateFactorFunctionActual() {
        // Given
        final DaysInYearType daysInYearType = DaysInYearType.ACTUAL;
        final String[] expectedValues = new String[] { "1.0080312", "1.0075131", "1.0080312", "1.0077721", "1.0080312", "1.0077721" };

        // Then
        for (LoanRepaymentScheduleInstallment period : periods) {
            final Long daysInPeriod = DateUtils.getDifferenceInDays(period.getFromDate(), period.getDueDate());
            final Integer daysInYear = DateUtils.daysInYear(daysInYearType, period.getFromDate());

            BigDecimal rateFactor = RateFactorFunctions.rateFactor(interestRate, daysInPeriod, daysInYear, MoneyHelper.getMathContext());

            Assertions.assertEquals(expectedValues[period.getInstallmentNumber() - 1], rateFactor.toString());
        }
    }

    @Test
    public void testFnValueFunctionDay365() {
        // Given
        final DaysInYearType daysInYearType = DaysInYearType.DAYS_365;
        final MathContext mc = MoneyHelper.getMathContext();
        final String[] expectedValues = new String[] { "1.0000000", "2.0075336", "3.0237007", "4.0472656", "5.0798590", "6.1194484" };

        final List<BigDecimal> fnValuesCalculated = new ArrayList<>();
        BigDecimal previousFnValue = BigDecimal.ZERO;
        for (LoanRepaymentScheduleInstallment period : periods) {
            final Long daysInPeriod = DateUtils.getDifferenceInDays(period.getFromDate(), period.getDueDate());
            final Integer daysInYear = DateUtils.daysInYear(daysInYearType, period.getFromDate());
            final BigDecimal rateFactor = RateFactorFunctions.rateFactor(interestRate, daysInPeriod, daysInYear,
                    MoneyHelper.getMathContext());

            final BigDecimal currentFnValue = FnValueFunctions.fnValue(previousFnValue, rateFactor, mc);
            fnValuesCalculated.add(currentFnValue);

            previousFnValue = currentFnValue;
        }

        int idx = 0;
        for (BigDecimal fnValue : fnValuesCalculated) {
            Assertions.assertEquals(expectedValues[idx++], fnValue.toString());
        }
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
