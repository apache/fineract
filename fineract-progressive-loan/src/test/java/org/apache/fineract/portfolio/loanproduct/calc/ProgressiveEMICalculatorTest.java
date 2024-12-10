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
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.InterestPeriod;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.PeriodDueDetails;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.ProgressiveLoanInterestScheduleModel;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.RepaymentPeriod;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModelRepaymentPeriod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductMinimumRepaymentScheduleRelatedDetail;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProgressiveEMICalculatorTest {

    private static ProgressiveEMICalculator emiCalculator = new ProgressiveEMICalculator();

    private static MockedStatic<ThreadLocalContextUtil> threadLocalContextUtil = Mockito.mockStatic(ThreadLocalContextUtil.class);
    private static MockedStatic<MoneyHelper> moneyHelper = Mockito.mockStatic(MoneyHelper.class);
    private static MathContext mc = new MathContext(12, RoundingMode.HALF_EVEN);
    private static LoanProductMinimumRepaymentScheduleRelatedDetail loanProductRelatedDetail = Mockito
            .mock(LoanProductMinimumRepaymentScheduleRelatedDetail.class);

    private static final CurrencyData currency = new CurrencyData("USD", "USD", 2, 1, "$", "USD");

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

    @AfterAll
    public static void tearDown() {
        threadLocalContextUtil.close();
        moneyHelper.close();
    }

    private BigDecimal getRateFactorsByMonth(final DaysInYearType daysInYearType, final DaysInMonthType daysInMonthType,
            final BigDecimal interestRate, LoanRepaymentScheduleInstallment period) {
        final BigDecimal daysInPeriod = BigDecimal.valueOf(DateUtils.getDifferenceInDays(period.getFromDate(), period.getDueDate()));
        final BigDecimal daysInYear = BigDecimal.valueOf(daysInYearType.getNumberOfDays(period.getFromDate()));
        final BigDecimal daysInMonth = BigDecimal.valueOf(daysInMonthType.getNumberOfDays(period.getFromDate()));
        return emiCalculator.rateFactorByRepaymentEveryMonth(interestRate, BigDecimal.ONE, daysInMonth, daysInYear, daysInPeriod,
                daysInPeriod, mc);
    }

    @Test
    public void test_rateFactorByRepaymentEveryMonthMethod_DayInYear365_DaysInMonthActual() {
        // Given
        final DaysInYearType daysInYearType = DaysInYearType.DAYS_365;
        final DaysInMonthType daysInMonthType = DaysInMonthType.ACTUAL;
        final String[] expectedValues = new String[] { "0.008053375342", "0.007533802740", "0.008053375342", "0.007793589041",
                "0.008053375342", "0.007793589041" };

        // Then
        for (LoanRepaymentScheduleInstallment period : periods) {
            BigDecimal rateFactor = getRateFactorsByMonth(daysInYearType, daysInMonthType, interestRate, period);
            Assertions.assertEquals(expectedValues[period.getInstallmentNumber() - 1], rateFactor.toString());
        }
    }

    @Test
    public void test_rateFactorByRepaymentEveryMonthMethod_DayInYearActual_DaysInMonthActual() {
        // Given
        final DaysInYearType daysInYearType = DaysInYearType.ACTUAL;
        final DaysInMonthType daysInMonthType = DaysInMonthType.ACTUAL;

        final String[] expectedValues = new String[] { "0.008031371585", "0.007513218579", "0.008031371585", "0.007772295082",
                "0.008031371585", "0.007772295082" };

        // Then
        for (LoanRepaymentScheduleInstallment period : periods) {
            BigDecimal rateFactor = getRateFactorsByMonth(daysInYearType, daysInMonthType, interestRate, period);
            Assertions.assertEquals(expectedValues[period.getInstallmentNumber() - 1], rateFactor.toString());
        }
    }

    @Test
    public void test_fnValueFunction_RepayEvery1Month_DayInYear365_DaysInMonthActual() {
        // Given
        final DaysInYearType daysInYearType = DaysInYearType.DAYS_365;
        final DaysInMonthType daysInMonthType = DaysInMonthType.ACTUAL;

        final String[] expectedValues = new String[] { "1.00000000000", "2.00753380274", "3.02370122596", "4.04726671069", "5.07986086861",
                "6.11945121660" };

        final List<BigDecimal> fnValuesCalculated = new ArrayList<>();
        BigDecimal previousFnValue = BigDecimal.ZERO;
        for (LoanRepaymentScheduleInstallment period : periods) {
            BigDecimal rateFactorPlus1 = getRateFactorsByMonth(daysInYearType, daysInMonthType, interestRate, period).add(BigDecimal.ONE,
                    MoneyHelper.getMathContext());

            final BigDecimal currentFnValue = emiCalculator.fnValue(previousFnValue, rateFactorPlus1, mc);
            fnValuesCalculated.add(currentFnValue);

            previousFnValue = currentFnValue;
        }

        int idx = 0;
        for (BigDecimal fnValue : fnValuesCalculated) {
            Assertions.assertEquals(expectedValues[idx++], fnValue.toString());
        }
    }

    @Test
    public void test_generateInterestScheduleModel() {
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = new ArrayList<>();
        final Integer installmentAmountInMultiplesOf = null;

        expectedRepaymentPeriods.add(repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)));
        expectedRepaymentPeriods.add(repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)));
        expectedRepaymentPeriods.add(repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)));
        expectedRepaymentPeriods.add(repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)));

        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestScheduleModel = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, installmentAmountInMultiplesOf, mc);

        Assertions.assertTrue(interestScheduleModel != null);
        Assertions.assertTrue(interestScheduleModel.loanProductRelatedDetail() != null);
        Assertions.assertTrue(interestScheduleModel.installmentAmountInMultiplesOf() == null);
        Assertions.assertTrue(interestScheduleModel.repaymentPeriods() != null);
        Assertions.assertEquals(4, interestScheduleModel.repaymentPeriods().size());
        Assertions.assertEquals(121, interestScheduleModel.getLoanTermInDays());
    }

    @Test
    @Timeout(1) // seconds
    public void test_emi_calculator_performance() {
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = new ArrayList<>();

        expectedRepaymentPeriods.add(repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)));
        expectedRepaymentPeriods.add(repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)));
        expectedRepaymentPeriods.add(repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)));
        expectedRepaymentPeriods.add(repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)));
        expectedRepaymentPeriods.add(repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)));
        expectedRepaymentPeriods.add(repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));
        expectedRepaymentPeriods.add(repayment(7, LocalDate.of(2024, 7, 1), LocalDate.of(2024, 8, 1)));
        expectedRepaymentPeriods.add(repayment(8, LocalDate.of(2024, 8, 1), LocalDate.of(2024, 9, 1)));
        expectedRepaymentPeriods.add(repayment(9, LocalDate.of(2024, 9, 1), LocalDate.of(2024, 10, 1)));
        expectedRepaymentPeriods.add(repayment(10, LocalDate.of(2024, 10, 1), LocalDate.of(2024, 11, 1)));
        expectedRepaymentPeriods.add(repayment(11, LocalDate.of(2024, 11, 1), LocalDate.of(2024, 12, 1)));
        expectedRepaymentPeriods.add(repayment(12, LocalDate.of(2024, 12, 1), LocalDate.of(2025, 1, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(7.0);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        Assertions.assertEquals(interestSchedule.getLoanTermInDays(), 366);
        Assertions.assertEquals(interestSchedule.repaymentPeriods().size(), 12);

        List<RepaymentPeriod> repaymentPeriods = interestSchedule.repaymentPeriods();
        for (int i = 0; i < repaymentPeriods.size(); i++) {
            final RepaymentPeriod repaymentPeriod = repaymentPeriods.get(i);
            Assertions.assertTrue(0 < toDouble(repaymentPeriod.getDuePrincipal()));
            Assertions.assertTrue(0 < toDouble(repaymentPeriod.getDueInterest()));
            if (i == repaymentPeriods.size() - 1) {
                Assertions.assertEquals(0.0, toDouble(repaymentPeriod.getOutstandingLoanBalance()));
            } else {
                Assertions.assertEquals(8.65, toDouble(repaymentPeriod.getEmi()));
                Assertions.assertTrue(0 < toDouble(repaymentPeriod.getOutstandingLoanBalance()));
            }
        }
    }

    @Test
    public void test_emiAdjustment_newCalculatedEmiNotBetterThanOriginal() {
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = new ArrayList<>();

        expectedRepaymentPeriods.add(repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)));
        expectedRepaymentPeriods.add(repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)));
        expectedRepaymentPeriods.add(repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)));
        expectedRepaymentPeriods.add(repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)));
        expectedRepaymentPeriods.add(repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)));
        expectedRepaymentPeriods.add(repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(15.678);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_365.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 17.43, 0.0, 0.0, 1.33, 16.1, 83.9);
        checkPeriod(interestSchedule, 0, 1, 17.43, 0.013315561644, 1.33, 16.1, 83.9);
        checkPeriod(interestSchedule, 1, 0, 17.43, 0.012456493151, 1.05, 16.38, 67.52);
        checkPeriod(interestSchedule, 2, 0, 17.43, 0.013315561644, 0.90, 16.53, 50.99);
        checkPeriod(interestSchedule, 3, 0, 17.43, 0.012886027397, 0.66, 16.77, 34.22);
        checkPeriod(interestSchedule, 4, 0, 17.43, 0.013315561644, 0.46, 16.97, 17.25);
        checkPeriod(interestSchedule, 5, 0, 17.47, 0.012886027397, 0.22, 17.25, 0.0);
    }

    @Test
    public void test_disbursedAmt100_dayInYears360_daysInMonth30_repayEvery1Month() {
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = new ArrayList<>();

        expectedRepaymentPeriods.add(repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)));
        expectedRepaymentPeriods.add(repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)));
        expectedRepaymentPeriods.add(repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)));
        expectedRepaymentPeriods.add(repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)));
        expectedRepaymentPeriods.add(repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)));
        expectedRepaymentPeriods.add(repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(9.4822);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 17.13, 0.0, 0.0, 0.79, 16.34, 83.66);
        checkPeriod(interestSchedule, 0, 1, 17.13, 0.007901833333, 0.79, 16.34, 83.66);
        checkPeriod(interestSchedule, 1, 0, 17.13, 0.007901833333, 0.66, 16.47, 67.19);
        checkPeriod(interestSchedule, 2, 0, 17.13, 0.007901833333, 0.53, 16.60, 50.59);
        checkPeriod(interestSchedule, 3, 0, 17.13, 0.007901833333, 0.40, 16.73, 33.86);
        checkPeriod(interestSchedule, 4, 0, 17.13, 0.007901833333, 0.27, 16.86, 17.0);
        checkPeriod(interestSchedule, 5, 0, 17.13, 0.007901833333, 0.13, 17.00, 0.0);
    }

    @Test
    public void test_multi_disbursedAmt200_2ndOnDueDate_dayInYears360_daysInMonth30_repayEvery1Month() {
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = new ArrayList<>();

        expectedRepaymentPeriods.add(repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)));
        expectedRepaymentPeriods.add(repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)));
        expectedRepaymentPeriods.add(repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)));
        expectedRepaymentPeriods.add(repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)));
        expectedRepaymentPeriods.add(repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)));
        expectedRepaymentPeriods.add(repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(9.4822);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 17.13, 0.0, 0.0, 0.79, 16.34, 83.66);
        checkPeriod(interestSchedule, 0, 1, 17.13, 0.007901833333, 0.79, 16.34, 83.66);
        checkPeriod(interestSchedule, 1, 0, 17.13, 0.007901833333, 0.66, 16.47, 67.19);
        checkPeriod(interestSchedule, 2, 0, 17.13, 0.007901833333, 0.53, 16.60, 50.59);
        checkPeriod(interestSchedule, 3, 0, 17.13, 0.007901833333, 0.40, 16.73, 33.86);
        checkPeriod(interestSchedule, 4, 0, 17.13, 0.007901833333, 0.27, 16.86, 17.0);
        checkPeriod(interestSchedule, 5, 0, 17.13, 0.007901833333, 0.13, 17.00, 0.0);

        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 3, 1), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 17.13, 0.0, 0.0, 0.79, 16.34, 83.66);
        checkPeriod(interestSchedule, 0, 1, 17.13, 0.007901833333, 0.79, 16.34, 83.66);
        checkPeriod(interestSchedule, 1, 0, 17.13, 0.007901833333, 0.66, 16.47, 167.19);
        checkPeriod(interestSchedule, 2, 0, 42.63, 0.007901833333, 1.32, 1.32, 41.31, 125.88);
        checkPeriod(interestSchedule, 3, 0, 42.63, 0.007901833333, 0.99, 41.64, 84.24);
        checkPeriod(interestSchedule, 4, 0, 42.63, 0.007901833333, 0.67, 41.96, 42.28);
        checkPeriod(interestSchedule, 5, 0, 42.61, 0.007901833333, 0.33, 42.28, 0.0);
    }

    @Test
    public void test_reschedule_disbursedAmt100_dayInYears360_daysInMonth30_repayEvery1Month() {
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = new ArrayList<>();

        expectedRepaymentPeriods.add(repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)));
        expectedRepaymentPeriods.add(repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 15)));
        expectedRepaymentPeriods.add(repayment(3, LocalDate.of(2024, 3, 15), LocalDate.of(2024, 4, 15)));
        expectedRepaymentPeriods.add(repayment(4, LocalDate.of(2024, 4, 15), LocalDate.of(2024, 5, 15)));
        expectedRepaymentPeriods.add(repayment(5, LocalDate.of(2024, 5, 15), LocalDate.of(2024, 6, 15)));
        expectedRepaymentPeriods.add(repayment(6, LocalDate.of(2024, 6, 15), LocalDate.of(2024, 7, 15)));

        final BigDecimal interestRate = BigDecimal.valueOf(9.4822);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 17.13, 0.0, 0.0, 0.79, 16.34, 83.66);
        checkPeriod(interestSchedule, 0, 1, 17.13, 0.007901833333, 0.79, 16.34, 83.66);
        checkPeriod(interestSchedule, 1, 0, 17.13, 0.007901833333, 0.66, 16.47, 67.19);
        checkPeriod(interestSchedule, 2, 0, 17.13, 0.007901833333, 0.53, 16.60, 50.59);
        checkPeriod(interestSchedule, 3, 0, 17.13, 0.007901833333, 0.40, 16.73, 33.86);
        checkPeriod(interestSchedule, 4, 0, 17.13, 0.007901833333, 0.27, 16.86, 17.0);
        checkPeriod(interestSchedule, 5, 0, 17.13, 0.007901833333, 0.13, 17.00, 0.0);
    }

    @Test
    public void test_reschedule_interest_on0201_4per_disbursedAmt100_dayInYears360_daysInMonth30_repayEvery1Month() {
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = new ArrayList<>();

        expectedRepaymentPeriods.add(repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)));
        expectedRepaymentPeriods.add(repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)));
        expectedRepaymentPeriods.add(repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)));
        expectedRepaymentPeriods.add(repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)));
        expectedRepaymentPeriods.add(repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)));
        expectedRepaymentPeriods.add(repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(7.0);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        threadLocalContextUtil.when(ThreadLocalContextUtil::getBusinessDate).thenReturn(LocalDate.of(2024, 2, 14));

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        final BigDecimal interestRateNewValue = BigDecimal.valueOf(4.0);
        final LocalDate interestChangeDate = LocalDate.of(2024, 2, 2);
        emiCalculator.changeInterestRate(interestSchedule, interestChangeDate, interestRateNewValue);

        checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 0, 1, 17.01, 0.005833333333, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 1, 0, 16.88, 0.003333333333, 0.28, 16.60, 66.97);
        checkPeriod(interestSchedule, 2, 0, 16.88, 0.003333333333, 0.22, 16.66, 50.31);
        checkPeriod(interestSchedule, 3, 0, 16.88, 0.003333333333, 0.17, 16.71, 33.60);
        checkPeriod(interestSchedule, 4, 0, 16.88, 0.003333333333, 0.11, 16.77, 16.83);
        checkPeriod(interestSchedule, 5, 0, 16.89, 0.003333333333, 0.06, 16.83, 0.0);
    }

    @Test
    public void test_reschedule_interest_on0201_2nd_EMI_not_changeable_disbursedAmt100_dayInYears360_daysInMonth30_repayEvery1Month() {
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = new ArrayList<>();

        expectedRepaymentPeriods.add(repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)));
        expectedRepaymentPeriods.add(repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)));
        expectedRepaymentPeriods.add(repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)));
        expectedRepaymentPeriods.add(repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)));
        expectedRepaymentPeriods.add(repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)));
        expectedRepaymentPeriods.add(repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(7.0);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        threadLocalContextUtil.when(ThreadLocalContextUtil::getBusinessDate).thenReturn(LocalDate.of(2024, 2, 14));

        final ProgressiveLoanInterestScheduleModel interestModel = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestModel, LocalDate.of(2024, 1, 1), disbursedAmount);
        emiCalculator.payInterest(interestModel, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 1), toMoney(0.58));
        emiCalculator.payPrincipal(interestModel, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 1), toMoney(16.43));

        emiCalculator.payPrincipal(interestModel, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 2, 1), toMoney(16.90));

        final BigDecimal interestRateNewValue = BigDecimal.valueOf(4.0);
        final LocalDate interestChangeDate = LocalDate.of(2024, 2, 2);
        emiCalculator.changeInterestRate(interestModel, interestChangeDate, interestRateNewValue);

        checkPeriod(interestModel, 0, 0, 17.01, 0.0, 0.0, 0.58, 16.43, 66.67);
        checkPeriod(interestModel, 0, 1, 17.01, 0.005833333333, 0.58, 16.43, 66.67);
        checkPeriod(interestModel, 1, 0, 17.01, 0.003333333333, 0.22, 0.0, 17.01, 66.56);
        checkPeriod(interestModel, 2, 0, 16.83, 0.003333333333, 0.22, 0.44, 16.39, 50.17);
        checkPeriod(interestModel, 3, 0, 16.83, 0.003333333333, 0.17, 16.66, 33.51);
        checkPeriod(interestModel, 4, 0, 16.83, 0.003333333333, 0.11, 16.72, 16.79);
        checkPeriod(interestModel, 5, 0, 16.85, 0.003333333333, 0.06, 16.79, 0.0);
    }

    @Test
    public void test_reschedule_interest_on0120_adjsLst_dsbAmt100_dayInYears360_daysInMonth30_rpEvery1M() {

        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = new ArrayList<>();

        expectedRepaymentPeriods.add(repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)));
        expectedRepaymentPeriods.add(repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)));
        expectedRepaymentPeriods.add(repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)));
        expectedRepaymentPeriods.add(repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)));
        expectedRepaymentPeriods.add(repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)));
        expectedRepaymentPeriods.add(repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(7.0);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        threadLocalContextUtil.when(ThreadLocalContextUtil::getBusinessDate).thenReturn(LocalDate.of(2024, 2, 14));

        final ProgressiveLoanInterestScheduleModel interestModel = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestModel, LocalDate.of(2024, 1, 1), disbursedAmount);

        emiCalculator.payPrincipal(interestModel, LocalDate.of(2024, 7, 1), LocalDate.of(2024, 1, 15), toMoney(17.01));

        final BigDecimal interestRateNewValue = BigDecimal.valueOf(4.0);
        final LocalDate interestChangeDate = LocalDate.of(2024, 1, 20);
        emiCalculator.changeInterestRate(interestModel, interestChangeDate, interestRateNewValue);

        checkPeriod(interestModel, 0, 0, 16.80, 0.0, 0.0, 0.44, 16.36, 66.63);
        checkPeriod(interestModel, 0, 1, 16.80, 0.002634408602, 0.26, 0.44, 16.36, 66.63);
        checkPeriod(interestModel, 0, 2, 16.80, 0.000752688172, 0.06, 0.44, 16.36, 66.63);
        checkPeriod(interestModel, 0, 3, 16.80, 0.001397849462, 0.12, 0.44, 16.36, 66.63);
        checkPeriod(interestModel, 1, 0, 16.80, 0.003333333333, 0.22, 16.58, 50.05);
        checkPeriod(interestModel, 2, 0, 16.80, 0.003333333333, 0.17, 16.63, 33.42);
        checkPeriod(interestModel, 3, 0, 16.80, 0.003333333333, 0.11, 16.69, 16.73);
        checkPeriod(interestModel, 4, 0, 16.79, 0.003333333333, 0.06, 16.73, 0.0);
        checkPeriod(interestModel, 5, 0, 17.01, 0.003333333333, 0.0, 17.01, 0.0);
    }

    @Test
    public void test_reschedule_interest_on0215_4per_disbursedAmt100_dayInYears360_daysInMonth30_repayEvery1Month() {
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = new ArrayList<>();

        expectedRepaymentPeriods.add(repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)));
        expectedRepaymentPeriods.add(repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)));
        expectedRepaymentPeriods.add(repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)));
        expectedRepaymentPeriods.add(repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)));
        expectedRepaymentPeriods.add(repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)));
        expectedRepaymentPeriods.add(repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(7.0);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        threadLocalContextUtil.when(ThreadLocalContextUtil::getBusinessDate).thenReturn(LocalDate.of(2024, 2, 14));

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        final BigDecimal interestRateNewValue = BigDecimal.valueOf(4.0);
        final LocalDate interestChangeDate = LocalDate.of(2024, 2, 15);
        emiCalculator.changeInterestRate(interestSchedule, interestChangeDate, interestRateNewValue);

        checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 0, 1, 17.01, 0.005833333333, 0.58, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 1, 0, 16.90, 0.002614942529, 0.22, 0.37, 16.53, 67.04);
        checkPeriod(interestSchedule, 1, 1, 16.90, 0.001839080460, 0.15, 0.37, 16.53, 67.04);
        checkPeriod(interestSchedule, 2, 0, 16.90, 0.003333333333, 0.22, 16.68, 50.36);
        checkPeriod(interestSchedule, 3, 0, 16.90, 0.003333333333, 0.17, 16.73, 33.63);
        checkPeriod(interestSchedule, 4, 0, 16.90, 0.003333333333, 0.11, 16.79, 16.84);
        checkPeriod(interestSchedule, 5, 0, 16.90, 0.003333333333, 0.06, 16.84, 0.0);
    }

    /**
     * This test case tests a period early and late repayment with balance correction
     */
    @Test
    public void test_balance_correction_on0215_disbursedAmt100_dayInYears360_daysInMonth30_repayEvery1Month() {
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = new ArrayList<>();

        expectedRepaymentPeriods.add(repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)));
        expectedRepaymentPeriods.add(repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)));
        expectedRepaymentPeriods.add(repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)));
        expectedRepaymentPeriods.add(repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)));
        expectedRepaymentPeriods.add(repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)));
        expectedRepaymentPeriods.add(repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(7.0);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        threadLocalContextUtil.when(ThreadLocalContextUtil::getBusinessDate).thenReturn(LocalDate.of(2024, 2, 15));

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        // schedule 1st period 1st day
        PeriodDueDetails dueAmounts = emiCalculator.getDueAmounts(interestSchedule, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 1, 1));
        Assertions.assertEquals(17.01, toDouble(dueAmounts.getDuePrincipal()));
        Assertions.assertEquals(0.0, toDouble(dueAmounts.getDueInterest()));

        // schedule 2nd period last day
        dueAmounts = emiCalculator.getDueAmounts(interestSchedule, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 1));
        Assertions.assertEquals(16.52, toDouble(dueAmounts.getDuePrincipal()));
        Assertions.assertEquals(0.49, toDouble(dueAmounts.getDueInterest()));

        // pay off a period with balance correction
        final LocalDate op1stCorrectionPeriodDueDate = LocalDate.of(2024, 3, 1);
        final LocalDate op1stCorrectionDate = LocalDate.of(2024, 2, 15);
        final Money op1stCorrectionAmount = toMoney(16.77);

        // get remaining balance and dues for a date
        final PeriodDueDetails repaymentDetails1st = emiCalculator.getDueAmounts(interestSchedule, op1stCorrectionPeriodDueDate,
                op1stCorrectionDate);
        Assertions.assertEquals(16.77, toDouble(repaymentDetails1st.getDuePrincipal()));
        Assertions.assertEquals(0.24, toDouble(repaymentDetails1st.getDueInterest()));

        emiCalculator.payPrincipal(interestSchedule, op1stCorrectionPeriodDueDate, op1stCorrectionDate, op1stCorrectionAmount);
        emiCalculator.payInterest(interestSchedule, op1stCorrectionPeriodDueDate, op1stCorrectionDate, toMoney(0.24));

        checkPeriod(interestSchedule, 0, 1, 17.01, 0.005833333333, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 1, 0, 17.01, 0.002816091954, 0.24, 16.77, 66.80);
        checkPeriod(interestSchedule, 1, 1, 17.01, 0.003017241379, 0.20, 0.24, 16.77, 66.80);
        checkPeriod(interestSchedule, 2, 0, 17.01, 0.005833333333, 0.39, 0.59, 16.42, 50.38);
        checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.29, 16.72, 33.66);
        checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.20, 16.81, 16.85);
        checkPeriod(interestSchedule, 5, 0, 16.95, 0.005833333333, 0.10, 16.85, 0.0);

        // totally pay off another period with balance correction
        final LocalDate op2ndCorrectionPeriodDueDate = LocalDate.of(2024, 4, 1);
        final LocalDate op2ndCorrectionDate = LocalDate.of(2024, 3, 1);
        final Money op2ndCorrectionAmount = toMoney(16.42);

        // get remaining balance and dues for a date
        final PeriodDueDetails repaymentDetails2st = emiCalculator.getDueAmounts(interestSchedule, op2ndCorrectionPeriodDueDate,
                op2ndCorrectionDate);
        Assertions.assertEquals(16.81, toDouble(repaymentDetails2st.getDuePrincipal()));
        Assertions.assertEquals(0.20, toDouble(repaymentDetails2st.getDueInterest()));

        emiCalculator.payPrincipal(interestSchedule, op2ndCorrectionPeriodDueDate, op2ndCorrectionDate, op2ndCorrectionAmount);
        emiCalculator.payInterest(interestSchedule, op2ndCorrectionPeriodDueDate, op2ndCorrectionDate, toMoney(0.49));

        checkPeriod(interestSchedule, 0, 1, 17.01, 0.005833333333, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 1, 0, 17.01, 0.002816091954, 0.24, 16.77, 50.38);
        checkPeriod(interestSchedule, 1, 1, 17.01, 0.003017241379, 0.20, 0.24, 16.77, 50.38);
        checkPeriod(interestSchedule, 2, 0, 17.01, 0.005833333333, 0.29, 0.49, 16.52, 50.28);
        checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.29, 16.72, 33.56);
        checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.20, 16.81, 16.75);
        checkPeriod(interestSchedule, 5, 0, 16.85, 0.005833333333, 0.10, 16.75, 0.0);

        // check numbers on last period due date
        LocalDate periodDueDate = LocalDate.of(2024, 7, 1);
        LocalDate payDate = LocalDate.of(2024, 7, 1);
        final PeriodDueDetails repaymentDetails3rd = emiCalculator.getDueAmounts(interestSchedule, periodDueDate, payDate);
        Assertions.assertEquals(16.75, toDouble(repaymentDetails3rd.getDuePrincipal()));
        Assertions.assertEquals(0.1, toDouble(repaymentDetails3rd.getDueInterest()));

        // check numbers after the last period due date
        periodDueDate = LocalDate.of(2024, 7, 1);
        payDate = LocalDate.of(2024, 7, 15);
        final PeriodDueDetails repaymentDetails4th = emiCalculator.getDueAmounts(interestSchedule, periodDueDate, payDate);
        Assertions.assertEquals(16.75, toDouble(repaymentDetails4th.getDuePrincipal()));
        Assertions.assertEquals(0.1, toDouble(repaymentDetails4th.getDueInterest()));

        // balance update on the last period, check the right interest interval split
        emiCalculator.addBalanceCorrection(interestSchedule, LocalDate.of(2024, 6, 10), Money.of(currency, BigDecimal.ZERO));
        final RepaymentPeriod lastRepaymentPeriod = interestSchedule.repaymentPeriods().get(interestSchedule.repaymentPeriods().size() - 1);
        Assertions.assertEquals(2, lastRepaymentPeriod.getInterestPeriods().size());
        Assertions.assertEquals(LocalDate.of(2024, 6, 1), lastRepaymentPeriod.getInterestPeriods().get(0).getFromDate());
        Assertions.assertEquals(LocalDate.of(2024, 6, 10), lastRepaymentPeriod.getInterestPeriods().get(0).getDueDate());
        Assertions.assertEquals(LocalDate.of(2024, 6, 10), lastRepaymentPeriod.getInterestPeriods().get(1).getFromDate());
        Assertions.assertEquals(LocalDate.of(2024, 7, 1), lastRepaymentPeriod.getInterestPeriods().get(1).getDueDate());
    }

    @Test
    public void test_payoff_on0215_disbursedAmt100_dayInYears360_daysInMonth30_repayEvery1Month() {
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = new ArrayList<>();

        expectedRepaymentPeriods.add(repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)));
        expectedRepaymentPeriods.add(repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)));
        expectedRepaymentPeriods.add(repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)));
        expectedRepaymentPeriods.add(repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)));
        expectedRepaymentPeriods.add(repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)));
        expectedRepaymentPeriods.add(repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(7.0);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        threadLocalContextUtil.when(ThreadLocalContextUtil::getBusinessDate).thenReturn(LocalDate.of(2024, 2, 15));

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        // partially pay off a period with balance correction
        final LocalDate op1stCorrectionPeriodDueDate = LocalDate.of(2024, 3, 1);
        final LocalDate op1stCorrectionDate = LocalDate.of(2024, 2, 15);
        final Money op1stCorrectionAmount = toMoney(15.0);

        // get remaining balance and dues for a date
        final PeriodDueDetails repaymentDetails1st = emiCalculator.getDueAmounts(interestSchedule, op1stCorrectionPeriodDueDate,
                op1stCorrectionDate);
        Assertions.assertEquals(16.77, toDouble(repaymentDetails1st.getDuePrincipal()));
        Assertions.assertEquals(0.24, toDouble(repaymentDetails1st.getDueInterest()));

        // check getDueAmounts forcast
        PeriodDueDetails details = emiCalculator.getDueAmounts(interestSchedule, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 1));
        Assertions.assertEquals(16.52, toDouble(details.getDuePrincipal()));
        Assertions.assertEquals(0.49, toDouble(details.getDueInterest()));

        // apply balance change and check again
        emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 2, 1), op1stCorrectionDate, op1stCorrectionAmount);
        details = emiCalculator.getDueAmounts(interestSchedule, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 1));
        Assertions.assertEquals(16.52, toDouble(details.getDuePrincipal()));
        Assertions.assertEquals(0.49, toDouble(details.getDueInterest()));

        emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 15), toMoney(1.43));
        emiCalculator.payInterest(interestSchedule, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 15), toMoney(0.58));
        emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 2, 15), toMoney(16.77));
        emiCalculator.payInterest(interestSchedule, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 2, 15), toMoney(0.24));
        emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 2, 15), toMoney(17.01));
        emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 2, 15), toMoney(17.01));
        emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 2, 15), toMoney(17.01));
        emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 7, 1), LocalDate.of(2024, 2, 15), toMoney(15.77));

        // check periods in model

        checkPeriod(interestSchedule, 0, 1, 17.01, 0.005833333333, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 1, 0, 17.01, 0.002816091954, 0.24, 0.24, 16.77, 0.0);
        checkPeriod(interestSchedule, 1, 1, 17.01, 0.003017241379, 0.0, 0.24, 16.77, 0.0);
        checkPeriod(interestSchedule, 2, 0, 17.01, 0.005833333333, 0.0, 0.0, 17.01, 0.0);
        checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.0, 0.0, 17.01, 0.0);
        checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.0, 0.0, 17.01, 0.0);
        checkPeriod(interestSchedule, 5, 0, 15.77, 0.005833333333, 0.0, 0.0, 15.77, 0.0);
    }

    @Test
    public void test_payoff_on0115_disbursedAmt100_dayInYears360_daysInMonth30_repayEvery1Month() {
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = new ArrayList<>();

        expectedRepaymentPeriods.add(repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)));
        expectedRepaymentPeriods.add(repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)));
        expectedRepaymentPeriods.add(repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)));
        expectedRepaymentPeriods.add(repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)));
        expectedRepaymentPeriods.add(repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)));
        expectedRepaymentPeriods.add(repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(7.0);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        threadLocalContextUtil.when(ThreadLocalContextUtil::getBusinessDate).thenReturn(LocalDate.of(2024, 2, 15));

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        // get remaining balance and dues on due date
        PeriodDueDetails dueAmounts = emiCalculator.getDueAmounts(interestSchedule, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 1));
        Assertions.assertEquals(16.43, toDouble(dueAmounts.getDuePrincipal()));
        Assertions.assertEquals(0.58, toDouble(dueAmounts.getDueInterest()));

        // check numbers on payoff date
        dueAmounts = emiCalculator.getDueAmounts(interestSchedule, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 1, 15));
        Assertions.assertEquals(16.75, toDouble(dueAmounts.getDuePrincipal()));
        Assertions.assertEquals(0.26, toDouble(dueAmounts.getDueInterest()));

        emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 1, 15), toMoney(16.75));
        emiCalculator.payInterest(interestSchedule, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 1, 15), toMoney(0.26));

        // check again numbers are zero
        // dueAmounts = emiCalculator.getdueAmounts(interestSchedule, LocalDate.of(2024, 2, 1),
        // LocalDate.of(2024, 2, 1));

        emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 1, 15), toMoney(17.01));
        emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 1, 15), toMoney(17.01));
        emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 1, 15), toMoney(17.01));
        emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 1, 15), toMoney(17.01));

        dueAmounts = emiCalculator.getDueAmounts(interestSchedule, LocalDate.of(2024, 7, 1), LocalDate.of(2024, 7, 1));
        Assertions.assertEquals(15.21, toDouble(dueAmounts.getDuePrincipal()));
        Assertions.assertEquals(0.5, toDouble(dueAmounts.getDueInterest()));

        // check periods in model
        checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.26, 16.75, 15.21);
        checkPeriod(interestSchedule, 0, 1, 17.01, 0.002634408602, 0.26, 16.75, 15.21);
        checkPeriod(interestSchedule, 0, 2, 17.01, 0.003198924731, 0.05, 0.26, 16.75, 15.21);
        checkPeriod(interestSchedule, 1, 0, 17.01, 0.005833333333, 0.09, 0.0, 17.01, 15.21);
        checkPeriod(interestSchedule, 2, 0, 17.01, 0.005833333333, 0.09, 0.0, 17.01, 15.21);
        checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.09, 0.0, 17.01, 15.21);
        checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.09, 0.0, 17.01, 15.21);
        checkPeriod(interestSchedule, 5, 0, 15.71, 0.005833333333, 0.09, 0.5, 15.21, 0.0);

        emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 7, 1), LocalDate.of(2024, 1, 15), toMoney(15.21));

        // check periods in model
        checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.26, 16.75, 0.0);
        checkPeriod(interestSchedule, 0, 1, 17.01, 0.002634408602, 0.26, 16.75, 0.0);
        checkPeriod(interestSchedule, 0, 2, 17.01, 0.003198924731, 0.0, 0.26, 16.75, 0.0);
        checkPeriod(interestSchedule, 1, 0, 17.01, 0.005833333333, 0.0, 0.0, 17.01, 0.0);
        checkPeriod(interestSchedule, 2, 0, 17.01, 0.005833333333, 0.0, 0.0, 17.01, 0.0);
        checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.0, 0.0, 17.01, 0.0);
        checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.0, 0.0, 17.01, 0.0);
        checkPeriod(interestSchedule, 5, 0, 15.21, 0.005833333333, 0.0, 0.0, 15.21, 0.0);
    }

    @Test
    public void test_multiDisbursedAmt300InSamePeriod_dayInYears360_daysInMonth30_repayEvery1Month() {

        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = new ArrayList<>();

        expectedRepaymentPeriods.add(repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)));
        expectedRepaymentPeriods.add(repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)));
        expectedRepaymentPeriods.add(repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)));
        expectedRepaymentPeriods.add(repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)));
        expectedRepaymentPeriods.add(repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)));
        expectedRepaymentPeriods.add(repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(9.4822);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, installmentAmountInMultiplesOf, mc);

        Money disbursedAmount = toMoney(100);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 17.13, 0.0, 0.0, 0.79, 16.34, 83.66);
        checkPeriod(interestSchedule, 0, 1, 17.13, 0.007901833333, 0.79, 16.34, 83.66);
        checkPeriod(interestSchedule, 1, 0, 17.13, 0.007901833333, 0.66, 16.47, 67.19);
        checkPeriod(interestSchedule, 2, 0, 17.13, 0.007901833333, 0.53, 16.60, 50.59);
        checkPeriod(interestSchedule, 3, 0, 17.13, 0.007901833333, 0.40, 16.73, 33.86);
        checkPeriod(interestSchedule, 4, 0, 17.13, 0.007901833333, 0.27, 16.86, 17.0);
        checkPeriod(interestSchedule, 5, 0, 17.13, 0.007901833333, 0.13, 17.00, 0.0);

        disbursedAmount = toMoney(200.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 8), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 51.33, 0.0, 0.0, 2.02, 49.31, 250.69);
        checkPeriod(interestSchedule, 0, 1, 51.33, 0.001784284946, 0.18, 2.02, 49.31, 250.69);
        checkPeriod(interestSchedule, 0, 2, 51.33, 0.006117548387, 1.84, 2.02, 49.31, 250.69);
        checkPeriod(interestSchedule, 1, 0, 51.33, 0.007901833333, 1.98, 49.35, 201.34);
        checkPeriod(interestSchedule, 2, 0, 51.33, 0.007901833333, 1.59, 49.74, 151.60);
        checkPeriod(interestSchedule, 3, 0, 51.33, 0.007901833333, 1.20, 50.13, 101.47);
        checkPeriod(interestSchedule, 4, 0, 51.33, 0.007901833333, 0.80, 50.53, 50.94);
        checkPeriod(interestSchedule, 5, 0, 51.34, 0.007901833333, 0.40, 50.94, 0.0);
    }

    @Test
    public void test_multiDisbursedAmt200InDifferentPeriod_dayInYears360_daysInMonth30_repayEvery1Month() {

        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = new ArrayList<>();

        expectedRepaymentPeriods.add(repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)));
        expectedRepaymentPeriods.add(repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)));
        expectedRepaymentPeriods.add(repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)));
        expectedRepaymentPeriods.add(repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)));
        expectedRepaymentPeriods.add(repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)));
        expectedRepaymentPeriods.add(repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(9.4822);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, installmentAmountInMultiplesOf, mc);

        Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 17.13, 0.0, 0.0, 0.79, 16.34, 83.66);
        checkPeriod(interestSchedule, 0, 1, 17.13, 0.007901833333, 0.79, 16.34, 83.66);
        checkPeriod(interestSchedule, 1, 0, 17.13, 0.007901833333, 0.66, 16.47, 67.19);
        checkPeriod(interestSchedule, 2, 0, 17.13, 0.007901833333, 0.53, 16.60, 50.59);
        checkPeriod(interestSchedule, 3, 0, 17.13, 0.007901833333, 0.40, 16.73, 33.86);
        checkPeriod(interestSchedule, 4, 0, 17.13, 0.007901833333, 0.27, 16.86, 17.0);
        checkPeriod(interestSchedule, 5, 0, 17.13, 0.007901833333, 0.13, 17.00, 0.0);

        disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 2, 15), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 17.13, 0.0, 0.0, 0.79, 16.34, 83.66);
        checkPeriod(interestSchedule, 0, 1, 17.13, 0.007901833333, 0.79, 16.34, 83.66);
        checkPeriod(interestSchedule, 1, 0, 37.53, 0.003814678161, 0.32, 1.07, 36.46, 147.20);
        checkPeriod(interestSchedule, 1, 1, 37.53, 0.004087155172, 0.75, 1.07, 36.46, 147.20);
        checkPeriod(interestSchedule, 2, 0, 37.53, 0.007901833333, 1.16, 36.37, 110.83);
        checkPeriod(interestSchedule, 3, 0, 37.53, 0.007901833333, 0.88, 36.65, 74.18);
        checkPeriod(interestSchedule, 4, 0, 37.53, 0.007901833333, 0.59, 36.94, 37.24);
        checkPeriod(interestSchedule, 5, 0, 37.53, 0.007901833333, 0.29, 37.24, 0.0);
    }

    @Test
    public void test_multiDisbursedAmt150InSamePeriod_dayInYears360_daysInMonth30_repayEvery1Month_backdated_disbursement() {

        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = new ArrayList<>();

        expectedRepaymentPeriods.add(repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)));
        expectedRepaymentPeriods.add(repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)));
        expectedRepaymentPeriods.add(repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)));
        expectedRepaymentPeriods.add(repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)));
        expectedRepaymentPeriods.add(repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)));
        expectedRepaymentPeriods.add(repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(9.4822);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, installmentAmountInMultiplesOf, mc);

        Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 5), disbursedAmount);

        disbursedAmount = toMoney(50.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 8), disbursedAmount);

        // add disbursement on same date
        disbursedAmount = toMoney(25.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 8), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 29.94, 0.001019591398, 0.00, 1.15, 28.79, 146.21);
        checkPeriod(interestSchedule, 0, 1, 29.94, 0.000764693548, 0.08, 1.15, 28.79, 146.21);
        checkPeriod(interestSchedule, 0, 2, 29.94, 0.006117548387, 1.07, 1.15, 28.79, 146.21);
        checkPeriod(interestSchedule, 1, 0, 29.94, 0.007901833333, 1.16, 28.78, 117.43);
        checkPeriod(interestSchedule, 2, 0, 29.94, 0.007901833333, 0.93, 29.01, 88.42);
        checkPeriod(interestSchedule, 3, 0, 29.94, 0.007901833333, 0.70, 29.24, 59.18);
        checkPeriod(interestSchedule, 4, 0, 29.94, 0.007901833333, 0.47, 29.47, 29.71);
        checkPeriod(interestSchedule, 5, 0, 29.94, 0.007901833333, 0.23, 29.71, 0.0);
    }

    @Test
    public void test_disbursedAmt100_dayInYearsActual_daysInMonthActual_repayEvery1Month() {

        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2023, 12, 12), LocalDate.of(2024, 1, 12)),
                repayment(2, LocalDate.of(2024, 1, 12), LocalDate.of(2024, 2, 12)),
                repayment(3, LocalDate.of(2024, 2, 12), LocalDate.of(2024, 3, 1)),
                repayment(4, LocalDate.of(2024, 3, 12), LocalDate.of(2024, 4, 1)),
                repayment(5, LocalDate.of(2024, 4, 12), LocalDate.of(2024, 5, 1)),
                repayment(6, LocalDate.of(2024, 5, 12), LocalDate.of(2024, 6, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(9.4822);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2023, 12, 12), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 17.13, 0.0, 0.0, 0.80, 16.33, 83.67);
        checkPeriod(interestSchedule, 0, 1, 17.13, 0.008044857759, 0.80, 16.33, 83.67);
        checkPeriod(interestSchedule, 1, 0, 17.13, 0.008031371585, 0.67, 16.46, 67.21);
        checkPeriod(interestSchedule, 2, 0, 17.13, 0.007513218579, 0.50, 16.63, 50.58);
        checkPeriod(interestSchedule, 3, 0, 17.13, 0.008031371585, 0.41, 16.72, 33.86);
        checkPeriod(interestSchedule, 4, 0, 17.13, 0.007772295082, 0.26, 16.87, 16.99);
        checkPeriod(interestSchedule, 5, 0, 17.13, 0.008031371585, 0.14, 16.99, 0.0);
    }

    @Test
    public void test_disbursedAmt1000_NoInterest_repayEvery1Month() {

        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(2, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)),
                repayment(3, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)),
                repayment(4, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)),
                repayment(5, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)));

        final BigDecimal interestRate = BigDecimal.ZERO;
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(1000.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 250.0, 0.0, 0.0, 250.0, 750.0);
        checkPeriod(interestSchedule, 1, 0, 250.0, 0.0, 0.0, 250.0, 500.0);
        checkPeriod(interestSchedule, 2, 0, 250.0, 0.0, 0.0, 250.0, 250.0);
        checkPeriod(interestSchedule, 3, 0, 250.0, 0.0, 0.0, 250.0, 0.0);
    }

    @Test
    public void test_disbursedAmt100_dayInYears364_daysInMonthActual_repayEvery1Week() {

        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 8)),
                repayment(2, LocalDate.of(2024, 1, 8), LocalDate.of(2024, 1, 15)),
                repayment(3, LocalDate.of(2024, 1, 15), LocalDate.of(2024, 1, 22)),
                repayment(4, LocalDate.of(2024, 1, 22), LocalDate.of(2024, 1, 29)),
                repayment(5, LocalDate.of(2024, 1, 29), LocalDate.of(2024, 2, 5)),
                repayment(6, LocalDate.of(2024, 2, 5), LocalDate.of(2024, 2, 12)));

        final BigDecimal interestRate = BigDecimal.valueOf(9.4822);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_364.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.WEEKS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 16.77, 0.0, 0.0, 0.18, 16.59, 83.41);
        checkPeriod(interestSchedule, 0, 1, 16.77, 0.001823500000, 0.18, 16.59, 83.41);
        checkPeriod(interestSchedule, 1, 0, 16.77, 0.001823500000, 0.15, 16.62, 66.79);
        checkPeriod(interestSchedule, 2, 0, 16.77, 0.001823500000, 0.12, 16.65, 50.14);
        checkPeriod(interestSchedule, 3, 0, 16.77, 0.001823500000, 0.09, 16.68, 33.46);
        checkPeriod(interestSchedule, 4, 0, 16.77, 0.001823500000, 0.06, 16.71, 16.75);
        checkPeriod(interestSchedule, 5, 0, 16.78, 0.001823500000, 0.03, 16.75, 0.0);
    }

    @Test
    public void test_disbursedAmt100_dayInYears364_daysInMonthActual_repayEvery2Week() {

        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 15)),
                repayment(2, LocalDate.of(2024, 1, 15), LocalDate.of(2024, 1, 29)),
                repayment(3, LocalDate.of(2024, 1, 29), LocalDate.of(2024, 2, 12)));

        final BigDecimal interestRate = BigDecimal.valueOf(9.4822);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_364.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.WEEKS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(2);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 33.57, 0.0, 0.0, 0.36, 33.21, 66.79);
        checkPeriod(interestSchedule, 0, 1, 33.57, 0.003647000000, 0.36, 33.21, 66.79);
        checkPeriod(interestSchedule, 1, 0, 33.57, 0.003647000000, 0.24, 33.33, 33.46);
        checkPeriod(interestSchedule, 2, 0, 33.58, 0.003647000000, 0.12, 33.46, 0.0);
    }

    @Test
    public void test_disbursedAmt100_dayInYears360_daysInMonthDoesntMatter_repayEvery15Days() {

        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 16)),
                repayment(2, LocalDate.of(2024, 1, 16), LocalDate.of(2024, 1, 31)),
                repayment(3, LocalDate.of(2024, 1, 31), LocalDate.of(2024, 2, 15)),
                repayment(4, LocalDate.of(2024, 2, 15), LocalDate.of(2024, 3, 1)),
                repayment(5, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 16)),
                repayment(6, LocalDate.of(2024, 3, 16), LocalDate.of(2024, 3, 31)));

        final BigDecimal interestRate = BigDecimal.valueOf(9.4822);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.INVALID.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.DAYS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(15);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 16.90, 0.0, 0.0, 0.40, 16.50, 83.50);
        checkPeriod(interestSchedule, 0, 1, 16.90, 0.003950916667, 0.40, 16.50, 83.50);
        checkPeriod(interestSchedule, 1, 0, 16.90, 0.003950916667, 0.33, 16.57, 66.93);
        checkPeriod(interestSchedule, 2, 0, 16.90, 0.003950916667, 0.26, 16.64, 50.29);
        checkPeriod(interestSchedule, 3, 0, 16.90, 0.003950916667, 0.20, 16.70, 33.59);
        checkPeriod(interestSchedule, 4, 0, 16.90, 0.003950916667, 0.13, 16.77, 16.82);
        checkPeriod(interestSchedule, 5, 0, 16.89, 0.003950916667, 0.07, 16.82, 0.0);
    }

    @Test
    public void test_dailyInterest_disbursedAmt1000_dayInYears360_daysInMonth30_repayIn1Month() {

        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = new ArrayList<>();

        expectedRepaymentPeriods.add(repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(7.0);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestModel = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(1000.0);
        emiCalculator.addDisbursement(interestModel, LocalDate.of(2024, 1, 1), disbursedAmount);

        checkPeriod(interestModel, 0, 0, 1005.83, 0.0, 0.0, 5.83, 1000.0, 0.0);
        checkPeriod(interestModel, 0, 1, 1005.83, 0.005833333333, 5.83, 1000.0, 0.0);

        final LocalDate dueDate = LocalDate.of(2024, 2, 1);
        final LocalDate startDay = LocalDate.of(2024, 1, 1);

        checkDailyInterest(interestModel, dueDate, startDay, 1, 0.19, 0.19);
        checkDailyInterest(interestModel, dueDate, startDay, 2, 0.19, 0.38);
        checkDailyInterest(interestModel, dueDate, startDay, 3, 0.18, 0.56);
        checkDailyInterest(interestModel, dueDate, startDay, 4, 0.19, 0.75);
        checkDailyInterest(interestModel, dueDate, startDay, 5, 0.19, 0.94);
        checkDailyInterest(interestModel, dueDate, startDay, 6, 0.19, 1.13);
        checkDailyInterest(interestModel, dueDate, startDay, 7, 0.19, 1.32);
        checkDailyInterest(interestModel, dueDate, startDay, 8, 0.18, 1.50);
        checkDailyInterest(interestModel, dueDate, startDay, 9, 0.19, 1.69);
        checkDailyInterest(interestModel, dueDate, startDay, 10, 0.19, 1.88);
        checkDailyInterest(interestModel, dueDate, startDay, 11, 0.19, 2.07);
        checkDailyInterest(interestModel, dueDate, startDay, 12, 0.19, 2.26);
        checkDailyInterest(interestModel, dueDate, startDay, 13, 0.18, 2.44);
        checkDailyInterest(interestModel, dueDate, startDay, 14, 0.19, 2.63);
        checkDailyInterest(interestModel, dueDate, startDay, 15, 0.19, 2.82);
        checkDailyInterest(interestModel, dueDate, startDay, 16, 0.19, 3.01);
        checkDailyInterest(interestModel, dueDate, startDay, 17, 0.19, 3.20);
        checkDailyInterest(interestModel, dueDate, startDay, 18, 0.19, 3.39);
        checkDailyInterest(interestModel, dueDate, startDay, 19, 0.18, 3.57);
        checkDailyInterest(interestModel, dueDate, startDay, 20, 0.19, 3.76);
        checkDailyInterest(interestModel, dueDate, startDay, 21, 0.19, 3.95);
        checkDailyInterest(interestModel, dueDate, startDay, 22, 0.19, 4.14);
        checkDailyInterest(interestModel, dueDate, startDay, 23, 0.19, 4.33);
        checkDailyInterest(interestModel, dueDate, startDay, 24, 0.18, 4.51);
        checkDailyInterest(interestModel, dueDate, startDay, 25, 0.19, 4.70);
        checkDailyInterest(interestModel, dueDate, startDay, 26, 0.19, 4.89);
        checkDailyInterest(interestModel, dueDate, startDay, 27, 0.19, 5.08);
        checkDailyInterest(interestModel, dueDate, startDay, 28, 0.19, 5.27);
        checkDailyInterest(interestModel, dueDate, startDay, 29, 0.18, 5.45);
        checkDailyInterest(interestModel, dueDate, startDay, 30, 0.19, 5.64);
        checkDailyInterest(interestModel, dueDate, startDay, 31, 0.19, 5.83);
    }

    @Test
    public void test_dailyInterest_disbursedAmt2000_dayInYears360_daysInMonth30_repayIn2Month() {

        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = new ArrayList<>();

        expectedRepaymentPeriods.add(repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(7.0);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestModel = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount1st = toMoney(1000.0);
        final Money disbursedAmount2nd = toMoney(1000.0);
        emiCalculator.addDisbursement(interestModel, LocalDate.of(2024, 1, 1), disbursedAmount1st);
        emiCalculator.addDisbursement(interestModel, LocalDate.of(2024, 1, 15), disbursedAmount2nd);

        checkPeriod(interestModel, 0, 0, 2009.03, 0.0, 0.0, 9.03, 2000.0, 0.0);
        checkPeriod(interestModel, 0, 1, 2009.03, 0.002634408602, 2.63, 9.03, 2000.0, 0.0);
        checkPeriod(interestModel, 0, 2, 2009.03, 0.003198924731, 6.40, 9.03, 2000.0, 0.0);

        final LocalDate dueDate = LocalDate.of(2024, 2, 1);
        final LocalDate startDay = LocalDate.of(2024, 1, 1);

        // 1st 1000 disbursement accruals
        // Total Interest: 5.83 (31 days), 2.63 (14 days)
        checkDailyInterest(interestModel, dueDate, startDay, 1, 0.19, 0.19);
        checkDailyInterest(interestModel, dueDate, startDay, 2, 0.19, 0.38);
        checkDailyInterest(interestModel, dueDate, startDay, 3, 0.18, 0.56);
        checkDailyInterest(interestModel, dueDate, startDay, 4, 0.19, 0.75);
        checkDailyInterest(interestModel, dueDate, startDay, 5, 0.19, 0.94);
        checkDailyInterest(interestModel, dueDate, startDay, 6, 0.19, 1.13);
        checkDailyInterest(interestModel, dueDate, startDay, 7, 0.19, 1.32);
        checkDailyInterest(interestModel, dueDate, startDay, 8, 0.18, 1.50);
        checkDailyInterest(interestModel, dueDate, startDay, 9, 0.19, 1.69);
        checkDailyInterest(interestModel, dueDate, startDay, 10, 0.19, 1.88);
        checkDailyInterest(interestModel, dueDate, startDay, 11, 0.19, 2.07);
        checkDailyInterest(interestModel, dueDate, startDay, 12, 0.19, 2.26);
        checkDailyInterest(interestModel, dueDate, startDay, 13, 0.18, 2.44);
        checkDailyInterest(interestModel, dueDate, startDay, 14, 0.19, 2.63);

        // 2nd 1000 disbursement accruals
        // Total Interest: 6.40 (17 days)
        checkDailyInterest(interestModel, dueDate, startDay, 15, 0.38, 3.01);
        checkDailyInterest(interestModel, dueDate, startDay, 16, 0.37, 3.38);
        checkDailyInterest(interestModel, dueDate, startDay, 17, 0.38, 3.76);
        checkDailyInterest(interestModel, dueDate, startDay, 18, 0.38, 4.14);
        checkDailyInterest(interestModel, dueDate, startDay, 19, 0.37, 4.51);
        checkDailyInterest(interestModel, dueDate, startDay, 20, 0.38, 4.89);
        checkDailyInterest(interestModel, dueDate, startDay, 21, 0.38, 5.27);
        checkDailyInterest(interestModel, dueDate, startDay, 22, 0.37, 5.64);
        checkDailyInterest(interestModel, dueDate, startDay, 23, 0.38, 6.02);
        checkDailyInterest(interestModel, dueDate, startDay, 24, 0.37, 6.39);
        checkDailyInterest(interestModel, dueDate, startDay, 25, 0.38, 6.77);
        checkDailyInterest(interestModel, dueDate, startDay, 26, 0.38, 7.15);
        checkDailyInterest(interestModel, dueDate, startDay, 27, 0.37, 7.52);
        checkDailyInterest(interestModel, dueDate, startDay, 28, 0.38, 7.90);
        checkDailyInterest(interestModel, dueDate, startDay, 29, 0.38, 8.28);
        checkDailyInterest(interestModel, dueDate, startDay, 30, 0.37, 8.65);
        checkDailyInterest(interestModel, dueDate, startDay, 31, 0.38, 9.03);
    }

    private static LoanScheduleModelRepaymentPeriod repayment(int periodNumber, LocalDate fromDate, LocalDate dueDate) {
        final Money zeroAmount = Money.zero(currency);
        return LoanScheduleModelRepaymentPeriod.repayment(periodNumber, fromDate, dueDate, zeroAmount, zeroAmount, zeroAmount, zeroAmount,
                zeroAmount, zeroAmount, false, mc);
    }

    @NotNull
    private static LoanRepaymentScheduleInstallment createPeriod(int periodId, LocalDate start, LocalDate end) {
        LoanRepaymentScheduleInstallment period = Mockito.mock(LoanRepaymentScheduleInstallment.class);
        Mockito.when(period.getInstallmentNumber()).thenReturn(periodId);
        Mockito.when(period.getFromDate()).thenReturn(start);
        Mockito.when(period.getDueDate()).thenReturn(end);

        return period;
    }

    private static void checkDailyInterest(final ProgressiveLoanInterestScheduleModel interestModel, final LocalDate repaymentPeriodDueDate,
            final LocalDate interestStartDay, final int dayOffset, final double dailyInterest, final double interest) {
        Money previousInterest = emiCalculator
                .getDueAmounts(interestModel, repaymentPeriodDueDate, interestStartDay.plusDays(dayOffset - 1)).getDueInterest();
        Money currentInterest = emiCalculator.getDueAmounts(interestModel, repaymentPeriodDueDate, interestStartDay.plusDays(dayOffset))
                .getDueInterest();

        Assertions.assertEquals(dailyInterest, toDouble(currentInterest.minus(previousInterest)));
        Assertions.assertEquals(interest, toDouble(currentInterest));
    }

    private static void checkPeriod(final ProgressiveLoanInterestScheduleModel interestScheduleModel, final int repaymentIdx,
            final int interestIdx, final double emiValue, final double rateFactor, final double interestDue, final double principalDue,
            final double remaingBalance) {
        checkPeriod(interestScheduleModel, repaymentIdx, interestIdx, emiValue, rateFactor, interestDue, interestDue, principalDue,
                remaingBalance);
    }

    private static void checkPeriod(final ProgressiveLoanInterestScheduleModel interestScheduleModel, final int repaymentIdx,
            final int interestIdx, final double emiValue, final double rateFactor, final double interestDue,
            final double interestDueCumulated, final double principalDue, final double remaingBalance) {
        final RepaymentPeriod repaymentPeriod = interestScheduleModel.repaymentPeriods().get(repaymentIdx);
        final InterestPeriod interestPeriod = repaymentPeriod.getInterestPeriods().get(interestIdx);

        Assertions.assertEquals(emiValue, toDouble(repaymentPeriod.getEmi()));
        Assertions.assertEquals(rateFactor, toDouble(applyMathContext(interestPeriod.getRateFactor())));
        Assertions.assertEquals(interestDue, toDouble(interestPeriod.getCalculatedDueInterest()));
        Assertions.assertEquals(interestDueCumulated, toDouble(repaymentPeriod.getDueInterest()));
        Assertions.assertEquals(principalDue, toDouble(repaymentPeriod.getDuePrincipal()));
        Assertions.assertEquals(remaingBalance, toDouble(repaymentPeriod.getOutstandingLoanBalance()));
    }

    private static double toDouble(final Money value) {
        return value == null ? 0.0 : toDouble(value.getAmount());
    }

    private static double toDouble(final BigDecimal value) {
        return value == null ? 0.0 : value.doubleValue();
    }

    private static BigDecimal applyMathContext(final BigDecimal value) {
        return value.setScale(MoneyHelper.getMathContext().getPrecision(), MoneyHelper.getRoundingMode());
    }

    private static Money toMoney(final double value) {
        return Money.of(currency, BigDecimal.valueOf(value));
    }
}
