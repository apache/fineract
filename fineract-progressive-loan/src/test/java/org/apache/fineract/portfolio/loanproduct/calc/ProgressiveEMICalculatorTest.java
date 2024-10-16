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
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.InterestPeriod;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.PayableDetails;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.ProgressiveLoanInterestScheduleModel;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.RepaymentPeriod;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModelRepaymentPeriod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProgressiveEMICalculatorTest {

    private static ProgressiveEMICalculator emiCalculator = new ProgressiveEMICalculator();

    private static MockedStatic<ThreadLocalContextUtil> threadLocalContextUtil = Mockito.mockStatic(ThreadLocalContextUtil.class);
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
        final BigDecimal rateFactor = emiCalculator.rateFactorByRepaymentEveryMonth(interestRate, BigDecimal.ONE, daysInMonth, daysInYear,
                daysInPeriod, daysInPeriod);
        return rateFactor.setScale(12, MoneyHelper.getRoundingMode());
    }

    @Test
    public void testRateFactorByRepaymentEveryMonthMethod_DayInYear365_DaysInMonthActual() {
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
    public void testRateFactorByRepaymentEveryMonthMethod_DayInYearActual_DaysInMonthActual() {
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
    public void testFnValueFunction_RepayEvery1Month_DayInYear365_DaysInMonthActual() {
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

            final BigDecimal currentFnValue = emiCalculator.fnValue(previousFnValue, rateFactorPlus1);
            fnValuesCalculated.add(currentFnValue);

            previousFnValue = currentFnValue;
        }

        int idx = 0;
        for (BigDecimal fnValue : fnValuesCalculated) {
            Assertions.assertEquals(expectedValues[idx++], fnValue.toString());
        }
    }

    @Test
    public void testEMICalculator_generateInterestScheduleModel() {

        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = new ArrayList<>();
        final Integer installmentAmountInMultiplesOf = null;

        expectedRepaymentPeriods.add(repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)));
        expectedRepaymentPeriods.add(repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)));
        expectedRepaymentPeriods.add(repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)));
        expectedRepaymentPeriods.add(repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)));

        Mockito.when(loanProductRelatedDetail.getCurrency()).thenReturn(monetaryCurrency);

        final ProgressiveLoanInterestScheduleModel interestScheduleModel = emiCalculator
                .generateInterestScheduleModel(expectedRepaymentPeriods, loanProductRelatedDetail, installmentAmountInMultiplesOf);

        Assertions.assertTrue(interestScheduleModel != null);
        Assertions.assertTrue(interestScheduleModel.loanProductRelatedDetail() != null);
        Assertions.assertTrue(interestScheduleModel.installmentAmountInMultiplesOf() == null);
        Assertions.assertTrue(interestScheduleModel.repaymentPeriods() != null);
        Assertions.assertEquals(4, interestScheduleModel.repaymentPeriods().size());
        Assertions.assertEquals(121, interestScheduleModel.getLoanTermInDays());
    }

    @Test
    public void testEMICalculation_disbursedAmt100_dayInYears360_daysInMonth30_repayEvery1Month() {

        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = new ArrayList<>();

        expectedRepaymentPeriods.add(repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)));
        expectedRepaymentPeriods.add(repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)));
        expectedRepaymentPeriods.add(repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)));
        expectedRepaymentPeriods.add(repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)));
        expectedRepaymentPeriods.add(repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)));
        expectedRepaymentPeriods.add(repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = new BigDecimal("9.4822");
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrency()).thenReturn(monetaryCurrency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generateInterestScheduleModel(expectedRepaymentPeriods,
                loanProductRelatedDetail, installmentAmountInMultiplesOf);

        final Money disbursedAmount = Money.of(monetaryCurrency, BigDecimal.valueOf(100));
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
    public void testEMICalculation_disbursedAmt100_dayInYears360_daysInMonth30_repayEvery1Month_reschedule() {

        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = new ArrayList<>();

        expectedRepaymentPeriods.add(repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)));
        expectedRepaymentPeriods.add(repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 15)));
        expectedRepaymentPeriods.add(repayment(3, LocalDate.of(2024, 3, 15), LocalDate.of(2024, 4, 15)));
        expectedRepaymentPeriods.add(repayment(4, LocalDate.of(2024, 4, 15), LocalDate.of(2024, 5, 15)));
        expectedRepaymentPeriods.add(repayment(5, LocalDate.of(2024, 5, 15), LocalDate.of(2024, 6, 15)));
        expectedRepaymentPeriods.add(repayment(6, LocalDate.of(2024, 6, 15), LocalDate.of(2024, 7, 15)));

        final BigDecimal interestRate = new BigDecimal("9.4822");
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrency()).thenReturn(monetaryCurrency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generateInterestScheduleModel(expectedRepaymentPeriods,
                loanProductRelatedDetail, installmentAmountInMultiplesOf);

        final Money disbursedAmount = Money.of(monetaryCurrency, BigDecimal.valueOf(100));
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 17.13, 0.0, 0.0, 0.79, 16.34, 83.66);
        checkPeriod(interestSchedule, 0, 1, 17.13, 0.007901833333, 0.79, 16.34, 83.66);
        checkPeriod(interestSchedule, 1, 0, 17.13, 0.007901833333, 0.66, 16.47, 67.19);
        checkPeriod(interestSchedule, 2, 0, 17.13, 0.007901833333, 0.53, 16.60, 50.59);
        checkPeriod(interestSchedule, 3, 0, 17.13, 0.007901833333, 0.40, 16.73, 33.86);
        checkPeriod(interestSchedule, 4, 0, 17.13, 0.007901833333, 0.27, 16.86, 17.0);
        checkPeriod(interestSchedule, 5, 0, 17.13, 0.007901833333, 0.13, 17.00, 0.0);
    }

    @Disabled("till interest rate change got implemented")
    @Test
    public void testEMICalculation_disbursedAmt100_dayInYears360_daysInMonth30_repayEvery1Month_reschedule_interest_on0201_4per() {

        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = new ArrayList<>();

        expectedRepaymentPeriods.add(repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)));
        expectedRepaymentPeriods.add(repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)));
        expectedRepaymentPeriods.add(repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)));
        expectedRepaymentPeriods.add(repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)));
        expectedRepaymentPeriods.add(repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)));
        expectedRepaymentPeriods.add(repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = new BigDecimal("7");
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrency()).thenReturn(monetaryCurrency);

        threadLocalContextUtil.when(ThreadLocalContextUtil::getBusinessDate).thenReturn(LocalDate.of(2024, 2, 14));

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generateInterestScheduleModel(expectedRepaymentPeriods,
                loanProductRelatedDetail, installmentAmountInMultiplesOf);

        final Money disbursedAmount = Money.of(monetaryCurrency, BigDecimal.valueOf(100));
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        final BigDecimal interestRateNewValue = new BigDecimal("4");
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

    @Disabled("till interest rate change got implemented")
    @Test
    public void testEMICalculation_disbursedAmt100_dayInYears360_daysInMonth30_repayEvery1Month_reschedule_interest_on0215_4per() {

        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = new ArrayList<>();

        expectedRepaymentPeriods.add(repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)));
        expectedRepaymentPeriods.add(repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)));
        expectedRepaymentPeriods.add(repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)));
        expectedRepaymentPeriods.add(repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)));
        expectedRepaymentPeriods.add(repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)));
        expectedRepaymentPeriods.add(repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = new BigDecimal("7");
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrency()).thenReturn(monetaryCurrency);

        threadLocalContextUtil.when(ThreadLocalContextUtil::getBusinessDate).thenReturn(LocalDate.of(2024, 2, 14));

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generateInterestScheduleModel(expectedRepaymentPeriods,
                loanProductRelatedDetail, installmentAmountInMultiplesOf);

        final Money disbursedAmount = Money.of(monetaryCurrency, BigDecimal.valueOf(100));
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        final BigDecimal interestRateNewValue = new BigDecimal("4");
        final LocalDate interestChangeDate = LocalDate.of(2024, 2, 15);
        emiCalculator.changeInterestRate(interestSchedule, interestChangeDate, interestRateNewValue);

        checkPeriod(interestSchedule, 0, 0, 17.01, 0.005833333333, 0.58, 16.43, 83.57);
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
    public void testEMICalculation_disbursedAmt100_dayInYears360_daysInMonth30_repayEvery1Month_add_balance_correction_on0215() {

        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = new ArrayList<>();

        expectedRepaymentPeriods.add(repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)));
        expectedRepaymentPeriods.add(repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)));
        expectedRepaymentPeriods.add(repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)));
        expectedRepaymentPeriods.add(repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)));
        expectedRepaymentPeriods.add(repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)));
        expectedRepaymentPeriods.add(repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = new BigDecimal("7");
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrency()).thenReturn(monetaryCurrency);

        threadLocalContextUtil.when(ThreadLocalContextUtil::getBusinessDate).thenReturn(LocalDate.of(2024, 2, 15));

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generateInterestScheduleModel(expectedRepaymentPeriods,
                loanProductRelatedDetail, installmentAmountInMultiplesOf);

        final Money disbursedAmount = Money.of(monetaryCurrency, BigDecimal.valueOf(100));
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        // schedule 1st period 1st day
        PayableDetails payableDetails = emiCalculator.getPayableDetails(interestSchedule, LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 1, 1));
        Assertions.assertEquals(100, toDouble(payableDetails.getOutstandingBalance().getAmount()));
        Assertions.assertEquals(17.01, toDouble(payableDetails.getPayablePrincipal().getAmount()));
        Assertions.assertEquals(0.0, toDouble(payableDetails.getPayableInterest().getAmount()));

        // schedule 2nd period last day
        payableDetails = emiCalculator.getPayableDetails(interestSchedule, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 1));
        Assertions.assertEquals(83.57, toDouble(payableDetails.getOutstandingBalance().getAmount()));
        Assertions.assertEquals(16.52, toDouble(payableDetails.getPayablePrincipal().getAmount()));
        Assertions.assertEquals(0.49, toDouble(payableDetails.getPayableInterest().getAmount()));

        // pay off a period with balance correction
        final LocalDate op1stCorrectionPeriodDueDate = LocalDate.of(2024, 3, 1);
        final LocalDate op1stCorrectionDate = LocalDate.of(2024, 2, 15);
        final Money op1stCorrectionAmount = Money.of(monetaryCurrency, BigDecimal.valueOf(16.77));

        // get remaining balance and dues for a date
        final PayableDetails repaymentDetails1st = emiCalculator.getPayableDetails(interestSchedule, op1stCorrectionPeriodDueDate,
                op1stCorrectionDate);
        Assertions.assertEquals(83.57, toDouble(repaymentDetails1st.getOutstandingBalance().getAmount()));
        Assertions.assertEquals(16.77, toDouble(repaymentDetails1st.getPayablePrincipal().getAmount()));
        Assertions.assertEquals(0.24, toDouble(repaymentDetails1st.getPayableInterest().getAmount()));

        emiCalculator.payPrincipal(interestSchedule, op1stCorrectionPeriodDueDate, op1stCorrectionDate, op1stCorrectionAmount);
        emiCalculator.payInterest(interestSchedule, op1stCorrectionPeriodDueDate, op1stCorrectionDate,
                Money.of(monetaryCurrency, BigDecimal.valueOf(0.24)));

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
        final Money op2ndCorrectionAmount = Money.of(monetaryCurrency, BigDecimal.valueOf(16.42));

        // get remaining balance and dues for a date
        final PayableDetails repaymentDetails2st = emiCalculator.getPayableDetails(interestSchedule, op2ndCorrectionPeriodDueDate,
                op2ndCorrectionDate);
        Assertions.assertEquals(66.80, toDouble(repaymentDetails2st.getOutstandingBalance().getAmount()));
        Assertions.assertEquals(16.81, toDouble(repaymentDetails2st.getPayablePrincipal().getAmount()));
        Assertions.assertEquals(0.20, toDouble(repaymentDetails2st.getPayableInterest().getAmount()));

        emiCalculator.payPrincipal(interestSchedule, op2ndCorrectionPeriodDueDate, op2ndCorrectionDate, op2ndCorrectionAmount);
        emiCalculator.payInterest(interestSchedule, op2ndCorrectionPeriodDueDate, op2ndCorrectionDate,
                Money.of(monetaryCurrency, BigDecimal.valueOf(0.49)));

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
        final PayableDetails repaymentDetails3rd = emiCalculator.getPayableDetails(interestSchedule, periodDueDate, payDate);
        Assertions.assertEquals(16.75, toDouble(repaymentDetails3rd.getOutstandingBalance().getAmount()));
        Assertions.assertEquals(16.75, toDouble(repaymentDetails3rd.getPayablePrincipal().getAmount()));
        Assertions.assertEquals(0.1, toDouble(repaymentDetails3rd.getPayableInterest().getAmount()));

        // check numbers after the last period due date
        periodDueDate = LocalDate.of(2024, 7, 1);
        payDate = LocalDate.of(2024, 7, 15);
        final PayableDetails repaymentDetails4th = emiCalculator.getPayableDetails(interestSchedule, periodDueDate, payDate);
        Assertions.assertEquals(16.75, toDouble(repaymentDetails4th.getOutstandingBalance().getAmount()));
        Assertions.assertEquals(16.75, toDouble(repaymentDetails4th.getPayablePrincipal().getAmount()));
        Assertions.assertEquals(0.1, toDouble(repaymentDetails4th.getPayableInterest().getAmount()));

        // balance update on the last period, check the right interest interval split
        emiCalculator.addBalanceCorrection(interestSchedule, LocalDate.of(2024, 6, 10), Money.of(monetaryCurrency, BigDecimal.ZERO));
        final RepaymentPeriod lastRepaymentPeriod = interestSchedule.repaymentPeriods().get(interestSchedule.repaymentPeriods().size() - 1);
        Assertions.assertEquals(2, lastRepaymentPeriod.getInterestPeriods().size());
        Assertions.assertEquals(LocalDate.of(2024, 6, 1), lastRepaymentPeriod.getInterestPeriods().get(0).getFromDate());
        Assertions.assertEquals(LocalDate.of(2024, 6, 10), lastRepaymentPeriod.getInterestPeriods().get(0).getDueDate());
        Assertions.assertEquals(LocalDate.of(2024, 6, 10), lastRepaymentPeriod.getInterestPeriods().get(1).getFromDate());
        Assertions.assertEquals(LocalDate.of(2024, 7, 1), lastRepaymentPeriod.getInterestPeriods().get(1).getDueDate());
    }

    @Test
    public void testEMICalculation_disbursedAmt100_dayInYears360_daysInMonth30_repayEvery1Month_payoff_on0215() {

        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = new ArrayList<>();

        expectedRepaymentPeriods.add(repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)));
        expectedRepaymentPeriods.add(repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)));
        expectedRepaymentPeriods.add(repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)));
        expectedRepaymentPeriods.add(repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)));
        expectedRepaymentPeriods.add(repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)));
        expectedRepaymentPeriods.add(repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = new BigDecimal("7");
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrency()).thenReturn(monetaryCurrency);

        threadLocalContextUtil.when(ThreadLocalContextUtil::getBusinessDate).thenReturn(LocalDate.of(2024, 2, 15));

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generateInterestScheduleModel(expectedRepaymentPeriods,
                loanProductRelatedDetail, installmentAmountInMultiplesOf);

        final Money disbursedAmount = Money.of(monetaryCurrency, BigDecimal.valueOf(100));
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        // partially pay off a period with balance correction
        final LocalDate op1stCorrectionPeriodDueDate = LocalDate.of(2024, 3, 1);
        final LocalDate op1stCorrectionDate = LocalDate.of(2024, 2, 15);
        final Money op1stCorrectionAmount = Money.of(monetaryCurrency, BigDecimal.valueOf(15.0));

        // get remaining balance and dues for a date
        final PayableDetails repaymentDetails1st = emiCalculator.getPayableDetails(interestSchedule, op1stCorrectionPeriodDueDate,
                op1stCorrectionDate);
        Assertions.assertEquals(83.57, toDouble(repaymentDetails1st.getOutstandingBalance().getAmount()));
        Assertions.assertEquals(16.77, toDouble(repaymentDetails1st.getPayablePrincipal().getAmount()));
        Assertions.assertEquals(0.24, toDouble(repaymentDetails1st.getPayableInterest().getAmount()));

        PayableDetails details = null;
        // check getPayableDetails forcast
        details = emiCalculator.getPayableDetails(interestSchedule, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 1));
        Assertions.assertEquals(83.57, toDouble(details.getOutstandingBalance().getAmount()));
        Assertions.assertEquals(16.52, toDouble(details.getPayablePrincipal().getAmount()));
        Assertions.assertEquals(0.49, toDouble(details.getPayableInterest().getAmount()));

        // apply balance change and check again
        emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 2, 1), op1stCorrectionDate, op1stCorrectionAmount);
        details = emiCalculator.getPayableDetails(interestSchedule, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 1));
        Assertions.assertEquals(83.57, toDouble(details.getOutstandingBalance().getAmount()));
        Assertions.assertEquals(16.52, toDouble(details.getPayablePrincipal().getAmount()));
        Assertions.assertEquals(0.49, toDouble(details.getPayableInterest().getAmount()));

        emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 15),
                Money.of(monetaryCurrency, BigDecimal.valueOf(1.43)));
        emiCalculator.payInterest(interestSchedule, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 15),
                Money.of(monetaryCurrency, BigDecimal.valueOf(0.58)));
        emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 2, 15),
                Money.of(monetaryCurrency, BigDecimal.valueOf(16.77)));
        emiCalculator.payInterest(interestSchedule, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 2, 15),
                Money.of(monetaryCurrency, BigDecimal.valueOf(0.24)));
        emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 2, 15),
                Money.of(monetaryCurrency, BigDecimal.valueOf(17.01)));
        emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 2, 15),
                Money.of(monetaryCurrency, BigDecimal.valueOf(17.01)));
        emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 2, 15),
                Money.of(monetaryCurrency, BigDecimal.valueOf(17.01)));
        emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 7, 1), LocalDate.of(2024, 2, 15),
                Money.of(monetaryCurrency, BigDecimal.valueOf(15.77)));

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
    public void testEMICalculation_disbursedAmt100_dayInYears360_daysInMonth30_repayEvery1Month_payoff_on0115() {

        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = new ArrayList<>();

        expectedRepaymentPeriods.add(repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)));
        expectedRepaymentPeriods.add(repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)));
        expectedRepaymentPeriods.add(repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)));
        expectedRepaymentPeriods.add(repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)));
        expectedRepaymentPeriods.add(repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)));
        expectedRepaymentPeriods.add(repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = new BigDecimal("7");
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrency()).thenReturn(monetaryCurrency);

        threadLocalContextUtil.when(ThreadLocalContextUtil::getBusinessDate).thenReturn(LocalDate.of(2024, 2, 15));

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generateInterestScheduleModel(expectedRepaymentPeriods,
                loanProductRelatedDetail, installmentAmountInMultiplesOf);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        // get remaining balance and dues on due date
        PayableDetails payableDetails = emiCalculator.getPayableDetails(interestSchedule, LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 2, 1));
        Assertions.assertEquals(16.43, toDouble(payableDetails.getPayablePrincipal().getAmount()));
        Assertions.assertEquals(0.58, toDouble(payableDetails.getPayableInterest().getAmount()));

        // check numbers on payoff date
        payableDetails = emiCalculator.getPayableDetails(interestSchedule, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 1, 15));
        Assertions.assertEquals(16.75, toDouble(payableDetails.getPayablePrincipal().getAmount()));
        Assertions.assertEquals(0.26, toDouble(payableDetails.getPayableInterest().getAmount()));

        emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 1, 15), toMoney(16.75));
        emiCalculator.payInterest(interestSchedule, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 1, 15), toMoney(0.26));

        // check again numbers are zero
        // payableDetails = emiCalculator.getPayableDetails(interestSchedule, LocalDate.of(2024, 2, 1),
        // LocalDate.of(2024, 2, 1));

        emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 1, 15), toMoney(17.01));
        emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 1, 15), toMoney(17.01));
        emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 1, 15), toMoney(17.01));
        emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 1, 15), toMoney(17.01));

        payableDetails = emiCalculator.getPayableDetails(interestSchedule, LocalDate.of(2024, 7, 1), LocalDate.of(2024, 7, 1));
        Assertions.assertEquals(15.21, toDouble(payableDetails.getPayablePrincipal().getAmount()));
        Assertions.assertEquals(0.5, toDouble(payableDetails.getPayableInterest().getAmount()));

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
    public void testEMICalculation_multiDisbursedAmt300InSamePeriod_dayInYears360_daysInMonth30_repayEvery1Month() {

        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = new ArrayList<>();

        expectedRepaymentPeriods.add(repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)));
        expectedRepaymentPeriods.add(repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)));
        expectedRepaymentPeriods.add(repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)));
        expectedRepaymentPeriods.add(repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)));
        expectedRepaymentPeriods.add(repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)));
        expectedRepaymentPeriods.add(repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = new BigDecimal("9.4822");
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrency()).thenReturn(monetaryCurrency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generateInterestScheduleModel(expectedRepaymentPeriods,
                loanProductRelatedDetail, installmentAmountInMultiplesOf);

        Money disbursedAmount = Money.of(monetaryCurrency, BigDecimal.valueOf(100));
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 17.13, 0.0, 0.0, 0.79, 16.34, 83.66);
        checkPeriod(interestSchedule, 0, 1, 17.13, 0.007901833333, 0.79, 16.34, 83.66);
        checkPeriod(interestSchedule, 1, 0, 17.13, 0.007901833333, 0.66, 16.47, 67.19);
        checkPeriod(interestSchedule, 2, 0, 17.13, 0.007901833333, 0.53, 16.60, 50.59);
        checkPeriod(interestSchedule, 3, 0, 17.13, 0.007901833333, 0.40, 16.73, 33.86);
        checkPeriod(interestSchedule, 4, 0, 17.13, 0.007901833333, 0.27, 16.86, 17.0);
        checkPeriod(interestSchedule, 5, 0, 17.13, 0.007901833333, 0.13, 17.00, 0.0);

        disbursedAmount = Money.of(monetaryCurrency, BigDecimal.valueOf(200));
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
    public void testEMICalculation_multiDisbursedAmt200InDifferentPeriod_dayInYears360_daysInMonth30_repayEvery1Month() {

        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = new ArrayList<>();

        expectedRepaymentPeriods.add(repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)));
        expectedRepaymentPeriods.add(repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)));
        expectedRepaymentPeriods.add(repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)));
        expectedRepaymentPeriods.add(repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)));
        expectedRepaymentPeriods.add(repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)));
        expectedRepaymentPeriods.add(repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = new BigDecimal("9.4822");
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrency()).thenReturn(monetaryCurrency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generateInterestScheduleModel(expectedRepaymentPeriods,
                loanProductRelatedDetail, installmentAmountInMultiplesOf);

        Money disbursedAmount = Money.of(monetaryCurrency, BigDecimal.valueOf(100));
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 17.13, 0.0, 0.0, 0.79, 16.34, 83.66);
        checkPeriod(interestSchedule, 0, 1, 17.13, 0.007901833333, 0.79, 16.34, 83.66);
        checkPeriod(interestSchedule, 1, 0, 17.13, 0.007901833333, 0.66, 16.47, 67.19);
        checkPeriod(interestSchedule, 2, 0, 17.13, 0.007901833333, 0.53, 16.60, 50.59);
        checkPeriod(interestSchedule, 3, 0, 17.13, 0.007901833333, 0.40, 16.73, 33.86);
        checkPeriod(interestSchedule, 4, 0, 17.13, 0.007901833333, 0.27, 16.86, 17.0);
        checkPeriod(interestSchedule, 5, 0, 17.13, 0.007901833333, 0.13, 17.00, 0.0);

        disbursedAmount = Money.of(monetaryCurrency, BigDecimal.valueOf(100));
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
    public void testEMICalculation_multiDisbursedAmt150InSamePeriod_dayInYears360_daysInMonth30_repayEvery1Month_backdated_disbursement() {

        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = new ArrayList<>();

        expectedRepaymentPeriods.add(repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)));
        expectedRepaymentPeriods.add(repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)));
        expectedRepaymentPeriods.add(repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)));
        expectedRepaymentPeriods.add(repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)));
        expectedRepaymentPeriods.add(repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)));
        expectedRepaymentPeriods.add(repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = new BigDecimal("9.4822");
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrency()).thenReturn(monetaryCurrency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generateInterestScheduleModel(expectedRepaymentPeriods,
                loanProductRelatedDetail, installmentAmountInMultiplesOf);

        Money disbursedAmount = Money.of(monetaryCurrency, BigDecimal.valueOf(100));
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 5), disbursedAmount);

        disbursedAmount = Money.of(monetaryCurrency, BigDecimal.valueOf(50));
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 8), disbursedAmount);

        // add disbursement on same date
        disbursedAmount = Money.of(monetaryCurrency, BigDecimal.valueOf(25));
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
    public void testEMICalculation_disbursedAmt100_dayInYearsActual_daysInMonthActual_repayEvery1Month() {

        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2023, 12, 12), LocalDate.of(2024, 1, 12)),
                repayment(2, LocalDate.of(2024, 1, 12), LocalDate.of(2024, 2, 12)),
                repayment(3, LocalDate.of(2024, 2, 12), LocalDate.of(2024, 3, 1)),
                repayment(4, LocalDate.of(2024, 3, 12), LocalDate.of(2024, 4, 1)),
                repayment(5, LocalDate.of(2024, 4, 12), LocalDate.of(2024, 5, 1)),
                repayment(6, LocalDate.of(2024, 5, 12), LocalDate.of(2024, 6, 1)));

        final BigDecimal interestRate = new BigDecimal("9.4822");
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrency()).thenReturn(monetaryCurrency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generateInterestScheduleModel(expectedRepaymentPeriods,
                loanProductRelatedDetail, installmentAmountInMultiplesOf);

        final Money disbursedAmount = Money.of(monetaryCurrency, BigDecimal.valueOf(100));
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
    public void testEMICalculation_disbursedAmt1000_NoInterest_repayEvery1Month() {

        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(2, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)),
                repayment(3, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)),
                repayment(4, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)),
                repayment(5, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)));

        final BigDecimal interestRate = new BigDecimal("0");
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrency()).thenReturn(monetaryCurrency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generateInterestScheduleModel(expectedRepaymentPeriods,
                loanProductRelatedDetail, installmentAmountInMultiplesOf);

        final Money disbursedAmount = Money.of(monetaryCurrency, BigDecimal.valueOf(1000));
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 250.0, 0.0, 0.0, 250.0, 750.0);
        checkPeriod(interestSchedule, 1, 0, 250.0, 0.0, 0.0, 250.0, 500.0);
        checkPeriod(interestSchedule, 2, 0, 250.0, 0.0, 0.0, 250.0, 250.0);
        checkPeriod(interestSchedule, 3, 0, 250.0, 0.0, 0.0, 250.0, 0.0);
    }

    @Test
    public void testEMICalculation_disbursedAmt100_dayInYears364_daysInMonthActual_repayEvery1Week() {

        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 8)),
                repayment(2, LocalDate.of(2024, 1, 8), LocalDate.of(2024, 1, 15)),
                repayment(3, LocalDate.of(2024, 1, 15), LocalDate.of(2024, 1, 22)),
                repayment(4, LocalDate.of(2024, 1, 22), LocalDate.of(2024, 1, 29)),
                repayment(5, LocalDate.of(2024, 1, 29), LocalDate.of(2024, 2, 5)),
                repayment(6, LocalDate.of(2024, 2, 5), LocalDate.of(2024, 2, 12)));

        final BigDecimal interestRate = new BigDecimal("9.4822");
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_364.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.WEEKS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrency()).thenReturn(monetaryCurrency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generateInterestScheduleModel(expectedRepaymentPeriods,
                loanProductRelatedDetail, installmentAmountInMultiplesOf);

        final Money disbursedAmount = Money.of(monetaryCurrency, BigDecimal.valueOf(100));
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
    public void testEMICalculation_disbursedAmt100_dayInYears364_daysInMonthActual_repayEvery2Week() {

        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 15)),
                repayment(2, LocalDate.of(2024, 1, 15), LocalDate.of(2024, 1, 29)),
                repayment(3, LocalDate.of(2024, 1, 29), LocalDate.of(2024, 2, 12)));

        final BigDecimal interestRate = new BigDecimal("9.4822");
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_364.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.WEEKS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(2);
        Mockito.when(loanProductRelatedDetail.getCurrency()).thenReturn(monetaryCurrency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generateInterestScheduleModel(expectedRepaymentPeriods,
                loanProductRelatedDetail, installmentAmountInMultiplesOf);

        final Money disbursedAmount = Money.of(monetaryCurrency, BigDecimal.valueOf(100));
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 33.57, 0.0, 0.0, 0.36, 33.21, 66.79);
        checkPeriod(interestSchedule, 0, 1, 33.57, 0.003647000000, 0.36, 33.21, 66.79);
        checkPeriod(interestSchedule, 1, 0, 33.57, 0.003647000000, 0.24, 33.33, 33.46);
        checkPeriod(interestSchedule, 2, 0, 33.58, 0.003647000000, 0.12, 33.46, 0.0);
    }

    @Test
    public void testEMICalculation_disbursedAmt100_dayInYears360_daysInMonthDoesntMatter_repayEvery15Days() {

        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 16)),
                repayment(2, LocalDate.of(2024, 1, 16), LocalDate.of(2024, 1, 31)),
                repayment(3, LocalDate.of(2024, 1, 31), LocalDate.of(2024, 2, 15)),
                repayment(4, LocalDate.of(2024, 2, 15), LocalDate.of(2024, 3, 1)),
                repayment(5, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 16)),
                repayment(6, LocalDate.of(2024, 3, 16), LocalDate.of(2024, 3, 31)));

        final BigDecimal interestRate = new BigDecimal("9.4822");
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.INVALID.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.DAYS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(15);
        Mockito.when(loanProductRelatedDetail.getCurrency()).thenReturn(monetaryCurrency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generateInterestScheduleModel(expectedRepaymentPeriods,
                loanProductRelatedDetail, installmentAmountInMultiplesOf);

        final Money disbursedAmount = Money.of(monetaryCurrency, BigDecimal.valueOf(100));
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 16.90, 0.0, 0.0, 0.40, 16.50, 83.50);
        checkPeriod(interestSchedule, 0, 1, 16.90, 0.003950916667, 0.40, 16.50, 83.50);
        checkPeriod(interestSchedule, 1, 0, 16.90, 0.003950916667, 0.33, 16.57, 66.93);
        checkPeriod(interestSchedule, 2, 0, 16.90, 0.003950916667, 0.26, 16.64, 50.29);
        checkPeriod(interestSchedule, 3, 0, 16.90, 0.003950916667, 0.20, 16.70, 33.59);
        checkPeriod(interestSchedule, 4, 0, 16.90, 0.003950916667, 0.13, 16.77, 16.82);
        checkPeriod(interestSchedule, 5, 0, 16.89, 0.003950916667, 0.07, 16.82, 0.0);
    }

    private static LoanScheduleModelRepaymentPeriod repayment(int periodNumber, LocalDate fromDate, LocalDate dueDate) {
        final Money zeroAmount = Money.zero(monetaryCurrency);
        return LoanScheduleModelRepaymentPeriod.repayment(periodNumber, fromDate, dueDate, zeroAmount, zeroAmount, zeroAmount, zeroAmount,
                zeroAmount, zeroAmount, false);
    }

    @NotNull
    private static LoanRepaymentScheduleInstallment createPeriod(int periodId, LocalDate start, LocalDate end) {
        LoanRepaymentScheduleInstallment period = Mockito.mock(LoanRepaymentScheduleInstallment.class);
        Mockito.when(period.getInstallmentNumber()).thenReturn(periodId);
        Mockito.when(period.getFromDate()).thenReturn(start);
        Mockito.when(period.getDueDate()).thenReturn(end);

        return period;
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

        Assertions.assertEquals(emiValue, toDouble(repaymentPeriod.getEmi().getAmount()));
        Assertions.assertEquals(rateFactor, toDouble(applyMathContext(interestPeriod.getRateFactor())));
        Assertions.assertEquals(interestDue, toDouble(interestPeriod.getCalculatedDueInterest().getAmount()));
        Assertions.assertEquals(interestDueCumulated, toDouble(repaymentPeriod.getDueInterest().getAmount()));
        Assertions.assertEquals(principalDue, toDouble(repaymentPeriod.getDuePrincipal().getAmount()));
        Assertions.assertEquals(remaingBalance, toDouble(repaymentPeriod.getOutstandingLoanBalance().getAmount()));
    }

    private static double toDouble(final BigDecimal value) {
        return value == null ? 0 : value.doubleValue();
    }

    private static BigDecimal applyMathContext(final BigDecimal value) {
        return value.setScale(MoneyHelper.getMathContext().getPrecision(), MoneyHelper.getRoundingMode());
    }

    private static Money toMoney(final double value) {
        return Money.of(monetaryCurrency, BigDecimal.valueOf(value));
    }
}
