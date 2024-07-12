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
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleModelDownPaymentPeriod;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModelDisbursementPeriod;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModelPeriod;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.PreGeneratedLoanSchedulePeriod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProgressiveEMICalculatorTest {

    private static final ProgressiveEMICalculator emiCalculator = new ProgressiveEMICalculator();

    private static MockedStatic<MoneyHelper> moneyHelper = Mockito.mockStatic(MoneyHelper.class);
    private static LoanProductRelatedDetail loanProductRelatedDetail = Mockito.mock(LoanProductRelatedDetail.class);

    private static final MonetaryCurrency monetaryCurrency = MonetaryCurrency
            .fromApplicationCurrency(new ApplicationCurrency("USD", "USD", 2, 1, "USD", "$"));

    private static List<LoanRepaymentScheduleInstallment> periods;
    private final BigDecimal interestRate = BigDecimal.valueOf(0.094822);

    @BeforeAll
    public static void init() {
        periods = new ArrayList<>();
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        periods.add(createPeriod(1, startDate, startDate.plusMonths(1)));
        periods.add(createPeriod(2, startDate.plusMonths(1), startDate.plusMonths(2)));
        periods.add(createPeriod(3, startDate.plusMonths(2), startDate.plusMonths(3)));
        periods.add(createPeriod(4, startDate.plusMonths(3), startDate.plusMonths(4)));
        periods.add(createPeriod(5, startDate.plusMonths(4), startDate.plusMonths(5)));
        periods.add(createPeriod(6, startDate.plusMonths(5), startDate.plusMonths(6)));

        // When
        moneyHelper.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
        moneyHelper.when(MoneyHelper::getMathContext).thenReturn(new MathContext(12, RoundingMode.HALF_EVEN));
    }

    private BigDecimal getRateFactorsByMonth(final DaysInYearType daysInYearType, final DaysInMonthType daysInMonthType,
            final BigDecimal interestRate, LoanRepaymentScheduleInstallment period) {
        final BigDecimal daysInPeriod = BigDecimal.valueOf(DateUtils.getDifferenceInDays(period.getFromDate(), period.getDueDate()));
        final BigDecimal daysInYear = BigDecimal.valueOf(daysInYearType.getNumberOfDays(period.getFromDate()));
        final BigDecimal daysInMonth = BigDecimal.valueOf(daysInMonthType.getNumberOfDays(period.getFromDate()));
        return emiCalculator.rateFactorByRepaymentEveryMonth(interestRate, BigDecimal.ONE, daysInMonth, daysInYear, daysInPeriod,
                daysInPeriod, MoneyHelper.getMathContext());
    }

    @Test
    public void testRateFactorByRepaymentEveryMonthMethod_DayInYear365_DaysInMonthActual() {
        // Given
        final DaysInYearType daysInYearType = DaysInYearType.DAYS_365;
        final DaysInMonthType daysInMonthType = DaysInMonthType.ACTUAL;
        final String[] expectedValues = new String[] { "1.00805337534", "1.00753380274", "1.00805337534", "1.00779358904", "1.00805337534",
                "1.00779358904" };

        // Then
        for (LoanRepaymentScheduleInstallment period : periods) {
            BigDecimal rateFactor = getRateFactorsByMonth(daysInYearType, daysInMonthType, interestRate, period);
            Assertions.assertEquals(expectedValues[period.getInstallmentNumber() - 1], rateFactor.toString());
        }
    }

    @Test
    public void testRateFactorByRepaymentEveryMonthMethod_DayInYearActual_DaysInMonthActual() {
        // Given
        final DaysInYearType daysInYearType = DaysInYearType.ACTUAL;
        final DaysInMonthType daysInMonthType = DaysInMonthType.ACTUAL;
        final String[] expectedValues = new String[] { "1.00803137158", "1.00751321858", "1.00803137158", "1.00777229508", "1.00803137158",
                "1.00777229508" };

        // Then
        for (LoanRepaymentScheduleInstallment period : periods) {
            BigDecimal rateFactor = getRateFactorsByMonth(daysInYearType, daysInMonthType, interestRate, period);
            Assertions.assertEquals(expectedValues[period.getInstallmentNumber() - 1], rateFactor.toString());
        }
    }

    @Test
    public void testFnValueFunction_RepayEvery1Month_DayInYear365_DaysInMonthActual() {
        // Given
        final DaysInYearType daysInYearType = DaysInYearType.DAYS_365;
        final DaysInMonthType daysInMonthType = DaysInMonthType.ACTUAL;
        final MathContext mc = MoneyHelper.getMathContext();
        final String[] expectedValues = new String[] { "1.00000000000", "2.00753380274", "3.02370122596", "4.04726671069", "5.07986086861",
                "6.11945121660" };

        final List<BigDecimal> fnValuesCalculated = new ArrayList<>();
        BigDecimal previousFnValue = BigDecimal.ZERO;
        for (LoanRepaymentScheduleInstallment period : periods) {
            BigDecimal rateFactor = getRateFactorsByMonth(daysInYearType, daysInMonthType, interestRate, period);

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
    public void testEMICalculation_principal100_dayInYearsActual_daysInMonthActual_repayEvery1Month() {
        final MathContext mc = MoneyHelper.getMathContext();
        final List<LoanScheduleModelPeriod> expectedRepaymentPeriods = new ArrayList<>();
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(9.4822);
        final BigDecimal principal = BigDecimal.valueOf(100);
        final Money outstandingBalance = Money.of(monetaryCurrency, principal);

        Mockito.when(loanProductRelatedDetail.getNominalInterestRatePerPeriod()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);

        final EMICalculationResult result = emiCalculator.calculateEMIValueAndRateFactors(outstandingBalance, loanProductRelatedDetail,
                expectedRepaymentPeriods, 1, 6, mc);

        // 17.13
        Assertions.assertEquals(BigDecimal.valueOf(17.13), result.getEqualMonthlyInstallmentValue().getAmount());

        Assertions.assertEquals(BigDecimal.valueOf(0.00803137158), result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(BigDecimal.valueOf(0.00751321858), result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(BigDecimal.valueOf(0.00803137158), result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(BigDecimal.valueOf(0.00777229508), result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(BigDecimal.valueOf(0.00803137158), result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(BigDecimal.valueOf(0.00777229508), result.getNextRepaymentPeriodRateFactorMinus1());

        // no more period, no more interest
        Assertions.assertEquals(BigDecimal.valueOf(0), result.getNextRepaymentPeriodRateFactorMinus1());

        result.reset();
        Assertions.assertEquals(BigDecimal.valueOf(0.00803137158), result.getNextRepaymentPeriodRateFactorMinus1());
    }

    @Test
    public void testEMICalculation_principal10000_dayInYearsActual_daysInMonthActual_repayEvery1Month_DisbursementInPreviousYear() {
        final MathContext mc = MoneyHelper.getMathContext();
        final List<LoanScheduleModelPeriod> expectedRepaymentPeriods = new ArrayList<>();
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(1, LocalDate.of(2023, 12, 12), LocalDate.of(2024, 1, 12)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(2, LocalDate.of(2024, 1, 12), LocalDate.of(2024, 2, 12)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(3, LocalDate.of(2024, 2, 12), LocalDate.of(2024, 3, 12)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(4, LocalDate.of(2024, 3, 12), LocalDate.of(2024, 4, 12)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(5, LocalDate.of(2024, 4, 12), LocalDate.of(2024, 5, 12)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(6, LocalDate.of(2024, 5, 12), LocalDate.of(2024, 6, 12)));

        final BigDecimal interestRate = BigDecimal.valueOf(9.4822);
        final BigDecimal principal = BigDecimal.valueOf(10_000);
        final Money outstandingBalance = Money.of(monetaryCurrency, principal);

        Mockito.when(loanProductRelatedDetail.getNominalInterestRatePerPeriod()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);

        final EMICalculationResult result = emiCalculator.calculateEMIValueAndRateFactors(outstandingBalance, loanProductRelatedDetail,
                expectedRepaymentPeriods, 1, 6, mc);

        // 1713.12
        Assertions.assertEquals(BigDecimal.valueOf(1713.12), result.getEqualMonthlyInstallmentValue().getAmount());

        Assertions.assertEquals(BigDecimal.valueOf(0.00804485776), result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(BigDecimal.valueOf(0.00803137158), result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(BigDecimal.valueOf(0.00751321858), result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(BigDecimal.valueOf(0.00803137158), result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(BigDecimal.valueOf(0.00777229508), result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(BigDecimal.valueOf(0.00803137158), result.getNextRepaymentPeriodRateFactorMinus1());

        // no more period, no more interest
        Assertions.assertEquals(BigDecimal.ZERO, result.getNextRepaymentPeriodRateFactorMinus1());
    }

    @Test
    public void testEMICalculation_principal100_dayInYears365_daysInMonthActual_repayEvery1Month() {
        final MathContext mc = MoneyHelper.getMathContext();
        final List<LoanScheduleModelPeriod> expectedRepaymentPeriods = new ArrayList<>();
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(9.4822);
        final BigDecimal principal = BigDecimal.valueOf(100);
        final Money outstandingBalance = Money.of(monetaryCurrency, principal);

        Mockito.when(loanProductRelatedDetail.getNominalInterestRatePerPeriod()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_365.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);

        final EMICalculationResult result = emiCalculator.calculateEMIValueAndRateFactors(outstandingBalance, loanProductRelatedDetail,
                expectedRepaymentPeriods, 1, 6, mc);

        // 17.13
        Assertions.assertEquals(BigDecimal.valueOf(17.13), result.getEqualMonthlyInstallmentValue().getAmount());

        Assertions.assertEquals(BigDecimal.valueOf(0.00805337534), result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(BigDecimal.valueOf(0.00753380274), result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(BigDecimal.valueOf(0.00805337534), result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(BigDecimal.valueOf(0.00779358904), result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(BigDecimal.valueOf(0.00805337534), result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(BigDecimal.valueOf(0.00779358904), result.getNextRepaymentPeriodRateFactorMinus1());

        // no more period, no more interest
        Assertions.assertEquals(BigDecimal.ZERO, result.getNextRepaymentPeriodRateFactorMinus1());

        result.reset();
        // check reset
        Assertions.assertEquals(BigDecimal.valueOf(0.00805337534), result.getNextRepaymentPeriodRateFactorMinus1());
    }

    @Test
    public void testEMICalculation_principal100_dayInYears360_daysInMonth30_repayEvery1Month() {
        final MathContext mc = MoneyHelper.getMathContext();
        final List<LoanScheduleModelPeriod> expectedRepaymentPeriods = new ArrayList<>();
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(9.4822);
        final BigDecimal principal = BigDecimal.valueOf(100);
        final Money outstandingBalance = Money.of(monetaryCurrency, principal);

        Mockito.when(loanProductRelatedDetail.getNominalInterestRatePerPeriod()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);

        final EMICalculationResult result = emiCalculator.calculateEMIValueAndRateFactors(outstandingBalance, loanProductRelatedDetail,
                expectedRepaymentPeriods, 1, 6, mc);

        // 17.13
        Assertions.assertEquals(BigDecimal.valueOf(17.13), result.getEqualMonthlyInstallmentValue().getAmount());

        Assertions.assertEquals(BigDecimal.valueOf(0.00790183333), result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(BigDecimal.valueOf(0.00790183333), result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(BigDecimal.valueOf(0.00790183333), result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(BigDecimal.valueOf(0.00790183333), result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(BigDecimal.valueOf(0.00790183333), result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(BigDecimal.valueOf(0.00790183333), result.getNextRepaymentPeriodRateFactorMinus1());

        // no more period, no more interest
        Assertions.assertEquals(BigDecimal.ZERO, result.getNextRepaymentPeriodRateFactorMinus1());

        result.reset();
        // check reset
        Assertions.assertEquals(BigDecimal.valueOf(0.00790183333), result.getNextRepaymentPeriodRateFactorMinus1());
    }

    @Test
    public void testEMICalculation_principal100_dayInYears364_daysInMonthDoesntMatter_repayEvery1Week() {
        final MathContext mc = MoneyHelper.getMathContext();
        final List<LoanScheduleModelPeriod> expectedRepaymentPeriods = new ArrayList<>();
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 8)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(2, LocalDate.of(2024, 1, 8), LocalDate.of(2024, 1, 15)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(3, LocalDate.of(2024, 1, 15), LocalDate.of(2024, 1, 22)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(4, LocalDate.of(2024, 1, 22), LocalDate.of(2024, 1, 29)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(5, LocalDate.of(2024, 1, 29), LocalDate.of(2024, 1, 5)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(6, LocalDate.of(2024, 2, 5), LocalDate.of(2024, 2, 12)));

        final BigDecimal interestRate = BigDecimal.valueOf(9.4822);
        final BigDecimal principal = BigDecimal.valueOf(100);
        final Money outstandingBalance = Money.of(monetaryCurrency, principal);

        Mockito.when(loanProductRelatedDetail.getNominalInterestRatePerPeriod()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_364.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.INVALID.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.WEEKS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);

        final EMICalculationResult result = emiCalculator.calculateEMIValueAndRateFactors(outstandingBalance, loanProductRelatedDetail,
                expectedRepaymentPeriods, 1, 6, mc);

        // 16.77
        Assertions.assertEquals(BigDecimal.valueOf(16.77), result.getEqualMonthlyInstallmentValue().getAmount());

        final BigDecimal fixValue = new BigDecimal("0.0018235");
        Assertions.assertEquals(fixValue, result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(fixValue, result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(fixValue, result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(fixValue, result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(fixValue, result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(fixValue, result.getNextRepaymentPeriodRateFactorMinus1());

        // no more period, no more interest
        Assertions.assertEquals(BigDecimal.ZERO, result.getNextRepaymentPeriodRateFactorMinus1());

        result.reset();
        // check reset
        Assertions.assertEquals(fixValue, result.getNextRepaymentPeriodRateFactorMinus1());
    }

    @Test
    public void testEMICalculation_principal100_dayInYears360_daysInMonthDoesntMatter_repayEvery2Week() {
        final MathContext mc = MoneyHelper.getMathContext();
        final List<LoanScheduleModelPeriod> expectedRepaymentPeriods = new ArrayList<>();
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 15)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(2, LocalDate.of(2024, 1, 15), LocalDate.of(2024, 1, 29)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(3, LocalDate.of(2024, 1, 29), LocalDate.of(2024, 2, 12)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(4, LocalDate.of(2024, 2, 12), LocalDate.of(2024, 2, 26)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(5, LocalDate.of(2024, 2, 26), LocalDate.of(2024, 3, 11)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(6, LocalDate.of(2024, 3, 11), LocalDate.of(2024, 3, 25)));

        final BigDecimal interestRate = BigDecimal.valueOf(9.4822);
        final BigDecimal principal = BigDecimal.valueOf(100);
        final Money outstandingBalance = Money.of(monetaryCurrency, principal);

        Mockito.when(loanProductRelatedDetail.getNominalInterestRatePerPeriod()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.INVALID.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.WEEKS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(2);

        final EMICalculationResult result = emiCalculator.calculateEMIValueAndRateFactors(outstandingBalance, loanProductRelatedDetail,
                expectedRepaymentPeriods, 1, 6, mc);

        // 16.88
        Assertions.assertEquals(new BigDecimal("16.88"), result.getEqualMonthlyInstallmentValue().getAmount());

        final BigDecimal fixValue = new BigDecimal("0.00368752222");
        Assertions.assertEquals(fixValue, result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(fixValue, result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(fixValue, result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(fixValue, result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(fixValue, result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(fixValue, result.getNextRepaymentPeriodRateFactorMinus1());

        // no more period, no more interest
        Assertions.assertEquals(BigDecimal.ZERO, result.getNextRepaymentPeriodRateFactorMinus1());

        result.reset();
        // check reset
        Assertions.assertEquals(fixValue, result.getNextRepaymentPeriodRateFactorMinus1());
    }

    @Test
    public void testEMICalculation_principal100_dayInYears360_daysInMonthDoesntMatter_repayEvery15Days() {
        final MathContext mc = MoneyHelper.getMathContext();
        final List<LoanScheduleModelPeriod> expectedRepaymentPeriods = new ArrayList<>();
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 16)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(2, LocalDate.of(2024, 1, 16), LocalDate.of(2024, 1, 31)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(3, LocalDate.of(2024, 1, 31), LocalDate.of(2024, 2, 15)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(4, LocalDate.of(2024, 2, 15), LocalDate.of(2024, 3, 1)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(5, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 16)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(6, LocalDate.of(2024, 3, 16), LocalDate.of(2024, 3, 31)));

        final BigDecimal interestRate = BigDecimal.valueOf(9.4822);
        final BigDecimal principal = BigDecimal.valueOf(100);
        final Money outstandingBalance = Money.of(monetaryCurrency, principal);

        Mockito.when(loanProductRelatedDetail.getNominalInterestRatePerPeriod()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.INVALID.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.DAYS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(15);

        final EMICalculationResult result = emiCalculator.calculateEMIValueAndRateFactors(outstandingBalance, loanProductRelatedDetail,
                expectedRepaymentPeriods, 1, 6, mc);

        // 16.90
        Assertions.assertEquals(new BigDecimal("16.90"), result.getEqualMonthlyInstallmentValue().getAmount());

        final BigDecimal fixValue = new BigDecimal("0.00395091667");
        Assertions.assertEquals(fixValue, result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(fixValue, result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(fixValue, result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(fixValue, result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(fixValue, result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(fixValue, result.getNextRepaymentPeriodRateFactorMinus1());

        // no more period, no more interest
        Assertions.assertEquals(BigDecimal.ZERO, result.getNextRepaymentPeriodRateFactorMinus1());

        result.reset();
        // check reset
        Assertions.assertEquals(fixValue, result.getNextRepaymentPeriodRateFactorMinus1());
    }

    @Test
    public void testEMICalculation_Disbursement_DownPayment_Principal100_dayInYears360_daysInMonth30_repayEvery1Month() {
        final MathContext mc = MoneyHelper.getMathContext();

        final BigDecimal interestRate = BigDecimal.valueOf(9.4822);
        final Money principal = Money.of(monetaryCurrency, BigDecimal.valueOf(100));
        final Money downPaymentValue = Money.of(monetaryCurrency, BigDecimal.valueOf(25));
        final Money outstandingBalance = principal.minus(downPaymentValue);

        Mockito.when(loanProductRelatedDetail.getNominalInterestRatePerPeriod()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);

        final List<LoanScheduleModelPeriod> expectedRepaymentPeriods = new ArrayList<>();
        expectedRepaymentPeriods
                .add(LoanScheduleModelDisbursementPeriod.disbursement(LocalDate.of(2024, 1, 1), principal, BigDecimal.ZERO));
        expectedRepaymentPeriods
                .add(LoanScheduleModelDownPaymentPeriod.downPayment(1, LocalDate.of(2024, 1, 1), downPaymentValue, outstandingBalance));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(2, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(3, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(4, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(5, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(6, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)));

        final EMICalculationResult result = emiCalculator.calculateEMIValueAndRateFactors(outstandingBalance, loanProductRelatedDetail,
                expectedRepaymentPeriods, 1, 5, mc);

        // 15.36
        Assertions.assertEquals(new BigDecimal("15.36"), result.getEqualMonthlyInstallmentValue().getAmount());

        Assertions.assertEquals(BigDecimal.valueOf(0.00790183333), result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(BigDecimal.valueOf(0.00790183333), result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(BigDecimal.valueOf(0.00790183333), result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(BigDecimal.valueOf(0.00790183333), result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(BigDecimal.valueOf(0.00790183333), result.getNextRepaymentPeriodRateFactorMinus1());

        // no more period, no more interest
        Assertions.assertEquals(BigDecimal.ZERO, result.getNextRepaymentPeriodRateFactorMinus1());

        result.reset();
        // check reset
        Assertions.assertEquals(BigDecimal.valueOf(0.00790183333), result.getNextRepaymentPeriodRateFactorMinus1());
    }

    @Test
    public void testEMICalculation_Principal1000_NoInterest_repayEvery1Month() {
        final MathContext mc = MoneyHelper.getMathContext();

        final BigDecimal interestRate = BigDecimal.valueOf(0);
        final Money outstandingBalance = Money.of(monetaryCurrency, BigDecimal.valueOf(1000));

        Mockito.when(loanProductRelatedDetail.getNominalInterestRatePerPeriod()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);

        final List<LoanScheduleModelPeriod> expectedRepaymentPeriods = new ArrayList<>();
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)));
        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)));

        final EMICalculationResult result = emiCalculator.calculateEMIValueAndRateFactors(outstandingBalance, loanProductRelatedDetail,
                expectedRepaymentPeriods, 1, 4, mc);

        // 250.00
        Assertions.assertEquals(new BigDecimal("250.00"), result.getEqualMonthlyInstallmentValue().getAmount());

        Assertions.assertEquals(BigDecimal.ZERO, result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(BigDecimal.ZERO, result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(BigDecimal.ZERO, result.getNextRepaymentPeriodRateFactorMinus1());
        Assertions.assertEquals(BigDecimal.ZERO, result.getNextRepaymentPeriodRateFactorMinus1());

        // no more period, no more interest
        Assertions.assertEquals(BigDecimal.ZERO, result.getNextRepaymentPeriodRateFactorMinus1());

        result.reset();
        // check reset
        Assertions.assertEquals(BigDecimal.ZERO, result.getNextRepaymentPeriodRateFactorMinus1());
    }

    @Test
    public void testUnsupportedRepaymentEveryYear() {
        final MathContext mc = MoneyHelper.getMathContext();
        final List<LoanScheduleModelPeriod> expectedRepaymentPeriods = new ArrayList<>();

        final BigDecimal interestRate = BigDecimal.valueOf(9.4822);
        final BigDecimal principal = BigDecimal.valueOf(100);
        final Money outstandingBalance = Money.of(monetaryCurrency, principal);

        Mockito.when(loanProductRelatedDetail.getNominalInterestRatePerPeriod()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.INVALID.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.YEARS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);

        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 16)));

        try {
            final EMICalculationResult result = emiCalculator.calculateEMIValueAndRateFactors(outstandingBalance, loanProductRelatedDetail,
                    expectedRepaymentPeriods, 1, 6, mc);
            Assertions.fail();
        } catch (Exception e) {
            Assertions.assertInstanceOf(UnsupportedOperationException.class, e);
        }
    }

    @Test
    public void testUnsupportedRepaymentEveryWholeTerm() {
        final MathContext mc = MoneyHelper.getMathContext();
        final List<LoanScheduleModelPeriod> expectedRepaymentPeriods = new ArrayList<>();

        final BigDecimal interestRate = BigDecimal.valueOf(9.4822);
        final BigDecimal principal = BigDecimal.valueOf(100);
        final Money outstandingBalance = Money.of(monetaryCurrency, principal);

        Mockito.when(loanProductRelatedDetail.getNominalInterestRatePerPeriod()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.INVALID.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.WHOLE_TERM);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);

        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 16)));

        try {
            final EMICalculationResult result = emiCalculator.calculateEMIValueAndRateFactors(outstandingBalance, loanProductRelatedDetail,
                    expectedRepaymentPeriods, 1, 6, mc);
            Assertions.fail();
        } catch (Exception e) {
            Assertions.assertInstanceOf(UnsupportedOperationException.class, e);
        }
    }

    @Test
    public void testInvalidRepaymentEveryValue() {
        final MathContext mc = MoneyHelper.getMathContext();
        final List<LoanScheduleModelPeriod> expectedRepaymentPeriods = new ArrayList<>();

        final BigDecimal interestRate = BigDecimal.valueOf(9.4822);
        final BigDecimal principal = BigDecimal.valueOf(100);
        final Money outstandingBalance = Money.of(monetaryCurrency, principal);

        Mockito.when(loanProductRelatedDetail.getNominalInterestRatePerPeriod()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.INVALID.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.INVALID);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);

        expectedRepaymentPeriods.add(new PreGeneratedLoanSchedulePeriod(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 16)));

        try {
            final EMICalculationResult result = emiCalculator.calculateEMIValueAndRateFactors(outstandingBalance, loanProductRelatedDetail,
                    expectedRepaymentPeriods, 1, 6, mc);
            Assertions.fail();
        } catch (Exception e) {
            Assertions.assertInstanceOf(UnsupportedOperationException.class, e);
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
