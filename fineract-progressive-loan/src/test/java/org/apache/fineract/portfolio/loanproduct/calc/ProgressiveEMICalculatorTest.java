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
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearCustomStrategyType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.data.LoanTermVariationsData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTermVariationType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModelRepaymentPeriod;
import org.apache.fineract.portfolio.loanaccount.service.ProgressiveLoanInterestScheduleModelParserServiceGsonImpl;
import org.apache.fineract.portfolio.loanproduct.calc.data.InterestPeriod;
import org.apache.fineract.portfolio.loanproduct.calc.data.PeriodDueDetails;
import org.apache.fineract.portfolio.loanproduct.calc.data.ProgressiveLoanInterestScheduleModel;
import org.apache.fineract.portfolio.loanproduct.calc.data.RepaymentPeriod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestMethod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductMinimumRepaymentScheduleRelatedDetail;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.lang.NonNull;

@Slf4j
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
    private final ProgressiveLoanInterestScheduleModelParserServiceGsonImpl interestScheduleModelService = new ProgressiveLoanInterestScheduleModelParserServiceGsonImpl();

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

    @BeforeEach
    public void setupTestDefaults() {
        Mockito.when(loanProductRelatedDetail.isInterestRecognitionOnDisbursementDate()).thenReturn(false);
        Mockito.when(loanProductRelatedDetail.getDaysInYearCustomStrategy()).thenReturn(null);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);
        Mockito.when(loanProductRelatedDetail.getInterestMethod()).thenReturn(InterestMethod.DECLINING_BALANCE);
        Mockito.when(loanProductRelatedDetail.getInterestCalculationPeriodMethod()).thenReturn(InterestCalculationPeriodMethod.DAILY);
        Mockito.when(loanProductRelatedDetail.isAllowPartialPeriodInterestCalculation()).thenReturn(true);
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
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

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
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

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
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 17.43, 0.0, 0.0, 1.33, 16.1, 83.9);
        checkPeriod(interestSchedule, 0, 1, 17.43, 0.013315561644, 1.3315561644, 1.33, 16.1, 83.9);
        checkPeriod(interestSchedule, 1, 0, 17.43, 0.012456493151, 1.04509977537, 1.05, 16.38, 67.52);
        checkPeriod(interestSchedule, 2, 0, 17.43, 0.013315561644, 0.899066722202, 0.90, 16.53, 50.99);
        checkPeriod(interestSchedule, 3, 0, 17.43, 0.012886027397, 0.657058536972, 0.66, 16.77, 34.22);
        checkPeriod(interestSchedule, 4, 0, 17.43, 0.013315561644, 0.455658519458, 0.46, 16.97, 17.25);
        checkPeriod(interestSchedule, 5, 0, 17.47, 0.012886027397, 0.222283972598, 0.22, 17.25, 0.0);
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
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 17.13, 0.0, 0.0, 0.79, 16.34, 83.66);
        checkPeriod(interestSchedule, 0, 1, 17.13, 0.007901833333, 0.790183333301, 0.79, 16.34, 83.66);
        checkPeriod(interestSchedule, 1, 0, 17.13, 0.007901833333, 0.66106737664, 0.66, 16.47, 67.19);
        checkPeriod(interestSchedule, 2, 0, 17.13, 0.007901833333, 0.530924181643, 0.53, 16.60, 50.59);
        checkPeriod(interestSchedule, 3, 0, 17.13, 0.007901833333, 0.399753748317, 0.40, 16.73, 33.86);
        checkPeriod(interestSchedule, 4, 0, 17.13, 0.007901833333, 0.267556076655, 0.27, 16.86, 17.0);
        checkPeriod(interestSchedule, 5, 0, 17.13, 0.007901833333, 0.134331166661, 0.13, 17.00, 0.0);
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
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 17.13, 0.0, 0.0, 0.79, 16.34, 83.66);
        checkPeriod(interestSchedule, 0, 1, 17.13, 0.007901833333, 0.790183333301, 0.79, 16.34, 83.66);
        checkPeriod(interestSchedule, 1, 0, 17.13, 0.007901833333, 0.66106737664, 0.66, 16.47, 67.19);
        checkPeriod(interestSchedule, 2, 0, 17.13, 0.007901833333, 0.530924181643, 0.53, 16.60, 50.59);
        checkPeriod(interestSchedule, 3, 0, 17.13, 0.007901833333, 0.399753748317, 0.40, 16.73, 33.86);
        checkPeriod(interestSchedule, 4, 0, 17.13, 0.007901833333, 0.267556076655, 0.27, 16.86, 17.0);
        checkPeriod(interestSchedule, 5, 0, 17.13, 0.007901833333, 0.134331166661, 0.13, 17.00, 0.0);

        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 3, 1), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 17.13, 0.0, 0.0, 0.79, 16.34, 83.66);
        checkPeriod(interestSchedule, 0, 1, 17.13, 0.007901833333, 0.790183333301, 0.79, 16.34, 83.66);
        checkPeriod(interestSchedule, 1, 0, 17.13, 0.007901833333, 0.66106737664, 0.66, 16.47, 167.19);
        checkPeriod(interestSchedule, 2, 0, 42.63, 0.007901833333, 1.32110751494, 1.32, 41.31, 125.88);
        checkPeriod(interestSchedule, 3, 0, 42.63, 0.007901833333, 0.994682779959, 0.99, 41.64, 84.24);
        checkPeriod(interestSchedule, 4, 0, 42.63, 0.007901833333, 0.665650439972, 0.67, 41.96, 42.28);
        checkPeriod(interestSchedule, 5, 0, 42.61, 0.007901833333, 0.334089513318, 0.33, 42.28, 0.0);
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
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        final BigDecimal interestRateNewValue = BigDecimal.valueOf(4.0);
        final LocalDate interestChangeDate = LocalDate.of(2024, 2, 2);
        emiCalculator.changeInterestRate(interestSchedule, interestChangeDate, interestRateNewValue);

        checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 0, 1, 17.01, 0.005833333333, 0.5833333333, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 1, 0, 16.88, 0.003333333333, 0.278566666639, 0.28, 16.60, 66.97);
        checkPeriod(interestSchedule, 2, 0, 16.88, 0.003333333333, 0.223233333311, 0.22, 16.66, 50.31);
        checkPeriod(interestSchedule, 3, 0, 16.88, 0.003333333333, 0.167699999983, 0.17, 16.71, 33.60);
        checkPeriod(interestSchedule, 4, 0, 16.88, 0.003333333333, 0.111999999989, 0.11, 16.77, 16.83);
        checkPeriod(interestSchedule, 5, 0, 16.89, 0.003333333333, 0.0560999999943, 0.06, 16.83, 0.0);
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
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestModel, LocalDate.of(2024, 1, 1), disbursedAmount);
        emiCalculator.payInterest(interestModel, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 1), toMoney(0.58));
        emiCalculator.payPrincipal(interestModel, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 1), toMoney(16.43));

        emiCalculator.payPrincipal(interestModel, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 2, 1), toMoney(16.90));

        final BigDecimal interestRateNewValue = BigDecimal.valueOf(4.0);
        final LocalDate interestChangeDate = LocalDate.of(2024, 2, 2);
        emiCalculator.changeInterestRate(interestModel, interestChangeDate, interestRateNewValue);

        checkPeriod(interestModel, 0, 0, 17.01, 0.0, 0.0, 0.58, 16.43, 66.67);
        checkPeriod(interestModel, 0, 1, 17.01, 0.005833333333, 0.5833333333, 0.58, 16.43, 66.67);
        checkPeriod(interestModel, 1, 0, 17.01, 0.003333333333, 0.222233333311, 0.0, 17.01, 66.56);
        checkPeriod(interestModel, 2, 0, 16.83, 0.003333333333, 0.221866666644, 0.44, 16.39, 50.17);
        checkPeriod(interestModel, 3, 0, 16.83, 0.003333333333, 0.167233333317, 0.17, 16.66, 33.51);
        checkPeriod(interestModel, 4, 0, 16.83, 0.003333333333, 0.111699999989, 0.11, 16.72, 16.79);
        checkPeriod(interestModel, 5, 0, 16.85, 0.003333333333, 0.0559666666611, 0.06, 16.79, 0.0);
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
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestModel, LocalDate.of(2024, 1, 1), disbursedAmount);

        emiCalculator.payPrincipal(interestModel, LocalDate.of(2024, 7, 1), LocalDate.of(2024, 1, 15), toMoney(17.01));

        final BigDecimal interestRateNewValue = BigDecimal.valueOf(4.0);
        final LocalDate interestChangeDate = LocalDate.of(2024, 1, 20);
        emiCalculator.changeInterestRate(interestModel, interestChangeDate, interestRateNewValue);

        checkPeriod(interestModel, 0, 0, 16.80, 0.0, 0.0, 0.44, 16.36, 66.63);
        checkPeriod(interestModel, 0, 1, 16.80, 0.002634408602, 0.2634408602, 0.44, 16.36, 66.63);
        checkPeriod(interestModel, 0, 2, 16.80, 0.000752688172, 0.0624655913944, 0.44, 16.36, 66.63);
        checkPeriod(interestModel, 0, 3, 16.80, 0.001397849462, 0.116007526851, 0.44, 16.36, 66.63);
        checkPeriod(interestModel, 1, 0, 16.80, 0.003333333333, 0.222099999978, 0.22, 16.58, 50.05);
        checkPeriod(interestModel, 2, 0, 16.80, 0.003333333333, 0.166833333317, 0.17, 16.63, 33.42);
        checkPeriod(interestModel, 3, 0, 16.80, 0.003333333333, 0.111399999989, 0.11, 16.69, 16.73);
        checkPeriod(interestModel, 4, 0, 16.79, 0.003333333333, 0.055766666661, 0.06, 16.73, 0.0);
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
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        final BigDecimal interestRateNewValue = BigDecimal.valueOf(4.0);
        final LocalDate interestChangeDate = LocalDate.of(2024, 2, 15);
        emiCalculator.changeInterestRate(interestSchedule, interestChangeDate, interestRateNewValue);

        checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 0, 1, 17.01, 0.005833333333, 0.5833333333, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 1, 0, 16.90, 0.002614942529, 0.218530747114, 0.37, 16.53, 67.04);
        checkPeriod(interestSchedule, 1, 1, 16.90, 0.001839080460, 0.153691954042, 0.37, 16.53, 67.04);
        checkPeriod(interestSchedule, 2, 0, 16.90, 0.003333333333, 0.223466666644, 0.22, 16.68, 50.36);
        checkPeriod(interestSchedule, 3, 0, 16.90, 0.003333333333, 0.16786666665, 0.17, 16.73, 33.63);
        checkPeriod(interestSchedule, 4, 0, 16.90, 0.003333333333, 0.112099999989, 0.11, 16.79, 16.84);
        checkPeriod(interestSchedule, 5, 0, 16.90, 0.003333333333, 0.0561333333276, 0.06, 16.84, 0.0);
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
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

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

        checkPeriod(interestSchedule, 0, 1, 17.01, 0.005833333333, 0.5833333333, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 1, 0, 17.01, 0.002816091954, 0.235340804584, 0.24, 16.77, 66.80);
        checkPeriod(interestSchedule, 1, 1, 17.01, 0.003017241379, 0.201551724117, 0.24, 16.77, 66.80);
        checkPeriod(interestSchedule, 2, 0, 17.01, 0.005833333333, 0.389666666644, 0.59, 16.42, 50.38);
        checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.293883333317, 0.29, 16.72, 33.66);
        checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.196349999989, 0.20, 16.81, 16.85);
        checkPeriod(interestSchedule, 5, 0, 16.95, 0.005833333333, 0.098291666661, 0.10, 16.85, 0.0);

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

        checkPeriod(interestSchedule, 0, 1, 17.01, 0.005833333333, 0.5833333333, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 1, 0, 17.01, 0.002816091954, 0.235340804584, 0.24, 16.77, 50.38);
        checkPeriod(interestSchedule, 1, 1, 17.01, 0.003017241379, 0.201551724117, 0.24, 16.77, 50.38);
        checkPeriod(interestSchedule, 2, 0, 17.01, 0.005833333333, 0.293883333317, 0.49, 16.52, 50.28);
        checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.293299999983, 0.29, 16.72, 33.56);
        checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.195766666655, 0.20, 16.81, 16.75);
        checkPeriod(interestSchedule, 5, 0, 16.85, 0.005833333333, 0.0977083333278, 0.10, 16.75, 0.0);

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
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

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

        checkPeriod(interestSchedule, 0, 1, 17.01, 0.005833333333, 0.5833333333, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 1, 0, 17.01, 0.002816091954, 0.235340804584, 0.24, 16.77, 0.0);
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
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

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
        checkPeriod(interestSchedule, 0, 1, 17.01, 0.002634408602, 0.2634408602, 0.26, 16.75, 15.21);
        checkPeriod(interestSchedule, 0, 2, 17.01, 0.003198924731, 0.0486556451585, 0.26, 16.75, 15.21);
        checkPeriod(interestSchedule, 1, 0, 17.01, 0.005833333333, 0.0887249999948, 0.0, 17.01, 15.21);
        checkPeriod(interestSchedule, 2, 0, 17.01, 0.005833333333, 0.0887249999949, 0.0, 17.01, 15.21);
        checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.0887249999949, 0.0, 17.01, 15.21);
        checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.0887249999949, 0.0, 17.01, 15.21);
        checkPeriod(interestSchedule, 5, 0, 15.71, 0.005833333333, 0.0887249999949, 0.5, 15.21, 0.0);

        emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 7, 1), LocalDate.of(2024, 1, 15), toMoney(15.21));

        // check periods in model
        checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.26, 16.75, 0.0);
        checkPeriod(interestSchedule, 0, 1, 17.01, 0.002634408602, 0.2634408602, 0.26, 16.75, 0.0);
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
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        Money disbursedAmount = toMoney(100);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 17.13, 0.0, 0.0, 0.79, 16.34, 83.66);
        checkPeriod(interestSchedule, 0, 1, 17.13, 0.007901833333, 0.790183333301, 0.79, 16.34, 83.66);
        checkPeriod(interestSchedule, 1, 0, 17.13, 0.007901833333, 0.66106737664, 0.66, 16.47, 67.19);
        checkPeriod(interestSchedule, 2, 0, 17.13, 0.007901833333, 0.530924181643, 0.53, 16.60, 50.59);
        checkPeriod(interestSchedule, 3, 0, 17.13, 0.007901833333, 0.399753748317, 0.40, 16.73, 33.86);
        checkPeriod(interestSchedule, 4, 0, 17.13, 0.007901833333, 0.267556076655, 0.27, 16.86, 17.0);
        checkPeriod(interestSchedule, 5, 0, 17.13, 0.007901833333, 0.134331166661, 0.13, 17.00, 0.0);

        disbursedAmount = toMoney(200.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 8), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 51.33, 0.0, 0.0, 2.01, 49.32, 250.68);
        checkPeriod(interestSchedule, 0, 1, 51.33, 0.001784284946, 0.178428494616, 2.01, 49.32, 250.68);
        checkPeriod(interestSchedule, 0, 2, 51.33, 0.006117548387, 1.8352645161, 2.01, 49.32, 250.68);
        checkPeriod(interestSchedule, 1, 0, 51.33, 0.007901833333, 1.98083157992, 1.98, 49.35, 201.33);
        checkPeriod(interestSchedule, 2, 0, 51.33, 0.007901833333, 1.59087610493, 1.59, 49.74, 151.59);
        checkPeriod(interestSchedule, 3, 0, 51.33, 0.007901833333, 1.19783891495, 1.2, 50.13, 101.46);
        checkPeriod(interestSchedule, 4, 0, 51.33, 0.007901833333, 0.801720009967, 0.80, 50.53, 50.93);
        checkPeriod(interestSchedule, 5, 0, 51.33, 0.007901833333, 0.40244037165, 0.40, 50.93, 0.0);
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
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 17.13, 0.0, 0.0, 0.79, 16.34, 83.66);
        checkPeriod(interestSchedule, 0, 1, 17.13, 0.007901833333, 0.790183333301, 0.79, 16.34, 83.66);
        checkPeriod(interestSchedule, 1, 0, 17.13, 0.007901833333, 0.66106737664, 0.66, 16.47, 67.19);
        checkPeriod(interestSchedule, 2, 0, 17.13, 0.007901833333, 0.530924181643, 0.53, 16.60, 50.59);
        checkPeriod(interestSchedule, 3, 0, 17.13, 0.007901833333, 0.399753748317, 0.40, 16.73, 33.86);
        checkPeriod(interestSchedule, 4, 0, 17.13, 0.007901833333, 0.267556076655, 0.27, 16.86, 17.0);
        checkPeriod(interestSchedule, 5, 0, 17.13, 0.007901833333, 0.134331166661, 0.13, 17.00, 0.0);

        disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 2, 15), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 17.13, 0.0, 0.0, 0.79, 16.34, 83.66);
        checkPeriod(interestSchedule, 0, 1, 17.13, 0.007901833333, 0.790183333301, 0.79, 16.34, 83.66);
        checkPeriod(interestSchedule, 1, 0, 37.53, 0.003814678161, 0.31913597493, 1.07, 36.46, 147.20);
        checkPeriod(interestSchedule, 1, 1, 37.53, 0.004087155172, 0.75064691889, 1.07, 36.46, 147.20);
        checkPeriod(interestSchedule, 2, 0, 37.53, 0.007901833333, 1.16314986662, 1.16, 36.37, 110.83);
        checkPeriod(interestSchedule, 3, 0, 37.53, 0.007901833333, 0.875760188295, 0.88, 36.65, 74.18);
        checkPeriod(interestSchedule, 4, 0, 37.53, 0.007901833333, 0.586157996641, 0.59, 36.94, 37.24);
        checkPeriod(interestSchedule, 5, 0, 37.53, 0.007901833333, 0.294264273321, 0.29, 37.24, 0.0);
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
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 5), disbursedAmount);

        disbursedAmount = toMoney(50.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 8), disbursedAmount);

        // add disbursement on same date
        disbursedAmount = toMoney(25.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 8), disbursedAmount);

        checkTotalInterestDue(interestSchedule, 4.64);

        checkPeriod(interestSchedule, 0, 0, 29.94, 0.001019591398, 0.00, 1.15, 28.79, 146.21);
        checkPeriod(interestSchedule, 0, 1, 29.94, 0.000764693548, 0.0764693548332, 1.15, 28.79, 146.21);
        checkPeriod(interestSchedule, 0, 2, 29.94, 0.006117548387, 1.07057096772, 1.15, 28.79, 146.21);
        checkPeriod(interestSchedule, 1, 0, 29.94, 0.007901833333, 1.15532705162, 1.16, 28.78, 117.43);
        checkPeriod(interestSchedule, 2, 0, 29.94, 0.007901833333, 0.927912288294, 0.93, 29.01, 88.42);
        checkPeriod(interestSchedule, 3, 0, 29.94, 0.007901833333, 0.698680103304, 0.70, 29.24, 59.18);
        checkPeriod(interestSchedule, 4, 0, 29.94, 0.007901833333, 0.467630496646, 0.47, 29.47, 29.71);
        checkPeriod(interestSchedule, 5, 0, 29.94, 0.007901833333, 0.234763468323, 0.23, 29.71, 0.0);
    }

    @Test
    public void test_disbursedAmt100_dayInYearsActual_daysInMonthActual_repayEvery1Month() {

        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2023, 12, 12), LocalDate.of(2024, 1, 12)),
                repayment(2, LocalDate.of(2024, 1, 12), LocalDate.of(2024, 2, 12)),
                repayment(3, LocalDate.of(2024, 2, 12), LocalDate.of(2024, 3, 12)),
                repayment(4, LocalDate.of(2024, 3, 12), LocalDate.of(2024, 4, 12)),
                repayment(5, LocalDate.of(2024, 4, 12), LocalDate.of(2024, 5, 12)),
                repayment(6, LocalDate.of(2024, 5, 12), LocalDate.of(2024, 6, 12)));

        final BigDecimal interestRate = BigDecimal.valueOf(9.4822);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2023, 12, 12), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 17.13, 0.0, 0.0, 0.80, 16.33, 83.67);
        checkPeriod(interestSchedule, 0, 1, 17.13, 0.008044857759, 0.8044857759, 0.80, 16.33, 83.67);
        checkPeriod(interestSchedule, 1, 0, 17.13, 0.008031371585, 0.671984860516, 0.67, 16.46, 67.21);
        checkPeriod(interestSchedule, 2, 0, 17.13, 0.007513218579, 0.504963420695, 0.50, 16.63, 50.58);
        checkPeriod(interestSchedule, 3, 0, 17.13, 0.008031371585, 0.40622677477, 0.41, 16.72, 33.86);
        checkPeriod(interestSchedule, 4, 0, 17.13, 0.007772295082, 0.263169911477, 0.26, 16.87, 16.99);
        checkPeriod(interestSchedule, 5, 0, 17.13, 0.008031371585, 0.136453003229, 0.14, 16.99, 0.0);
    }

    @Test
    public void test_multidisbursement_total_repay1st_dayInYears360_daysInMonth30_repayEvery1Month() {

        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)),
                repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)),
                repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)),
                repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)),
                repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)),
                repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(7.0);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), toMoney(300.0));
        Assertions.assertEquals(6.15, toDouble(interestSchedule.getTotalDueInterest()));

        // pay back the whole loan on first day
        emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 1, 1), toMoney(51.03));
        emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 1, 1), toMoney(51.03));
        emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 1, 1), toMoney(51.03));
        emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 1, 1), toMoney(51.03));
        emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 1, 1), toMoney(51.03));
        emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 7, 1), LocalDate.of(2024, 1, 1), toMoney(44.85));

        checkTotalInterestDue(interestSchedule, 0.0);

        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), toMoney(200.0));
        checkTotalInterestDue(interestSchedule, 4.11);

        checkEmi(interestSchedule, 4, 85.04);
        checkEmi(interestSchedule, 5, 78.91);
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
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

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
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 16.77, 0.0, 0.0, 0.18, 16.59, 83.41);
        checkPeriod(interestSchedule, 0, 1, 16.77, 0.001823500000, 0.18235, 0.18, 16.59, 83.41);
        checkPeriod(interestSchedule, 1, 0, 16.77, 0.001823500000, 0.152098135, 0.15, 16.62, 66.79);
        checkPeriod(interestSchedule, 2, 0, 16.77, 0.001823500000, 0.121791565, 0.12, 16.65, 50.14);
        checkPeriod(interestSchedule, 3, 0, 16.77, 0.001823500000, 0.09143029, 0.09, 16.68, 33.46);
        checkPeriod(interestSchedule, 4, 0, 16.77, 0.001823500000, 0.06101431, 0.06, 16.71, 16.75);
        checkPeriod(interestSchedule, 5, 0, 16.78, 0.001823500000, 0.030543625, 0.03, 16.75, 0.0);
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
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 33.57, 0.0, 0.0, 0.36, 33.21, 66.79);
        checkPeriod(interestSchedule, 0, 1, 33.57, 0.003647000000, 0.3647, 0.36, 33.21, 66.79);
        checkPeriod(interestSchedule, 1, 0, 33.57, 0.003647000000, 0.24358313, 0.24, 33.33, 33.46);
        checkPeriod(interestSchedule, 2, 0, 33.58, 0.003647000000, 0.12202862, 0.12, 33.46, 0.0);
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
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.DAYS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(15);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 16.90, 0.0, 0.0, 0.40, 16.50, 83.50);
        checkPeriod(interestSchedule, 0, 1, 16.90, 0.003950916667, 0.3950916667, 0.40, 16.50, 83.50);
        checkPeriod(interestSchedule, 1, 0, 16.90, 0.003950916667, 0.329901541694, 0.33, 16.57, 66.93);
        checkPeriod(interestSchedule, 2, 0, 16.90, 0.003950916667, 0.264434852522, 0.26, 16.64, 50.29);
        checkPeriod(interestSchedule, 3, 0, 16.90, 0.003950916667, 0.198691599183, 0.20, 16.70, 33.59);
        checkPeriod(interestSchedule, 4, 0, 16.90, 0.003950916667, 0.132711290845, 0.13, 16.77, 16.82);
        checkPeriod(interestSchedule, 5, 0, 16.89, 0.003950916667, 0.066454418339, 0.07, 16.82, 0.0);
    }

    @Test
    public void test_disbursedAmt1000_actual_actual_repayEvery1Month_verify_due_principal_amounts() {

        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)),
                repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)),
                repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)),
                repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)),
                repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)),
                repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)),
                repayment(7, LocalDate.of(2024, 7, 1), LocalDate.of(2024, 8, 1)),
                repayment(8, LocalDate.of(2024, 8, 1), LocalDate.of(2024, 9, 1)),
                repayment(9, LocalDate.of(2024, 9, 1), LocalDate.of(2024, 10, 1)),
                repayment(10, LocalDate.of(2024, 10, 1), LocalDate.of(2024, 11, 1)),
                repayment(11, LocalDate.of(2024, 11, 1), LocalDate.of(2024, 12, 1)),
                repayment(12, LocalDate.of(2024, 12, 1), LocalDate.of(2025, 1, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(25);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(1000.0);
        LocalDate disbursementDate = LocalDate.of(2024, 1, 1);

        emiCalculator.addDisbursement(interestSchedule, disbursementDate, disbursedAmount);

        for (int i = 0; i < 12; i++) {
            LocalDate dueDate = expectedRepaymentPeriods.get(i).getDueDate();
            PeriodDueDetails dueAmounts = emiCalculator.getDueAmounts(interestSchedule, dueDate, disbursementDate);
            Money duePrincipal = dueAmounts.getDuePrincipal();
            if (duePrincipal.isGreaterThanZero()) {
                emiCalculator.payPrincipal(interestSchedule, dueDate, disbursementDate, duePrincipal);
            }
        }

        checkPeriod(interestSchedule, 0, 95.02, 0.0, 95.02, 0.0, true);
        checkPeriod(interestSchedule, 1, 95.02, 0.0, 95.02, 0.0, true);
        checkPeriod(interestSchedule, 2, 95.02, 0.0, 95.02, 0.0, true);
        checkPeriod(interestSchedule, 3, 95.02, 0.0, 95.02, 0.0, true);
        checkPeriod(interestSchedule, 4, 95.02, 0.0, 95.02, 0.0, true);
        checkPeriod(interestSchedule, 5, 95.02, 0.0, 95.02, 0.0, true);
        checkPeriod(interestSchedule, 6, 95.02, 0.0, 95.02, 0.0, true);
        checkPeriod(interestSchedule, 7, 95.02, 0.0, 95.02, 0.0, true);
        checkPeriod(interestSchedule, 8, 95.02, 0.0, 95.02, 0.0, true);
        checkPeriod(interestSchedule, 9, 95.02, 0.0, 95.02, 0.0, true);
        checkPeriod(interestSchedule, 10, 49.8, 0.0, 49.8, 0.0, true);
        checkPeriod(interestSchedule, 11, 0.0, 0.0, 0.0, 0.0, true);
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
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(1000.0);
        emiCalculator.addDisbursement(interestModel, LocalDate.of(2024, 1, 1), disbursedAmount);

        checkPeriod(interestModel, 0, 0, 1005.83, 0.0, 0.0, 5.83, 1000.0, 0.0);
        checkPeriod(interestModel, 0, 1, 1005.83, 0.005833333333, 5.833333333, 5.83, 1000.0, 0.0);

        final LocalDate dueDate = LocalDate.of(2024, 2, 1);
        final LocalDate startDay = LocalDate.of(2024, 1, 1);

        checkDailyInterest(interestModel, dueDate, startDay, 1, 0.19, 0.19);
        checkDailyInterest(interestModel, dueDate, startDay, 2, 0.19, 0.38);
        checkDailyInterest(interestModel, dueDate, startDay, 3, 0.18, 0.56);
        checkDailyInterest(interestModel, dueDate, startDay, 4, 0.19, 0.75);
        checkDailyInterest(interestModel, dueDate, startDay, 5, 0.19, 0.94);
        checkDailyInterest(interestModel, dueDate, startDay, 6, 0.19, 1.13);
        checkDailyInterest(interestModel, dueDate, startDay, 7, 0.19, 1.32);
        checkDailyInterest(interestModel, dueDate, startDay, 8, 0.19, 1.51);
        checkDailyInterest(interestModel, dueDate, startDay, 9, 0.18, 1.69);
        checkDailyInterest(interestModel, dueDate, startDay, 10, 0.19, 1.88);
        checkDailyInterest(interestModel, dueDate, startDay, 11, 0.19, 2.07);
        checkDailyInterest(interestModel, dueDate, startDay, 12, 0.19, 2.26);
        checkDailyInterest(interestModel, dueDate, startDay, 13, 0.19, 2.45);
        checkDailyInterest(interestModel, dueDate, startDay, 14, 0.18, 2.63);
        checkDailyInterest(interestModel, dueDate, startDay, 15, 0.19, 2.82);
        checkDailyInterest(interestModel, dueDate, startDay, 16, 0.19, 3.01);
        checkDailyInterest(interestModel, dueDate, startDay, 17, 0.19, 3.20);
        checkDailyInterest(interestModel, dueDate, startDay, 18, 0.19, 3.39);
        checkDailyInterest(interestModel, dueDate, startDay, 19, 0.19, 3.58);
        checkDailyInterest(interestModel, dueDate, startDay, 20, 0.18, 3.76);
        checkDailyInterest(interestModel, dueDate, startDay, 21, 0.19, 3.95);
        checkDailyInterest(interestModel, dueDate, startDay, 22, 0.19, 4.14);
        checkDailyInterest(interestModel, dueDate, startDay, 23, 0.19, 4.33);
        checkDailyInterest(interestModel, dueDate, startDay, 24, 0.19, 4.52);
        checkDailyInterest(interestModel, dueDate, startDay, 25, 0.18, 4.7);
        checkDailyInterest(interestModel, dueDate, startDay, 26, 0.19, 4.89);
        checkDailyInterest(interestModel, dueDate, startDay, 27, 0.19, 5.08);
        checkDailyInterest(interestModel, dueDate, startDay, 28, 0.19, 5.27);
        checkDailyInterest(interestModel, dueDate, startDay, 29, 0.19, 5.46);
        checkDailyInterest(interestModel, dueDate, startDay, 30, 0.19, 5.65);
        checkDailyInterest(interestModel, dueDate, startDay, 31, 0.18, 5.83);
    }

    @Test
    public void test_dailyInterest_chargeback_disbursedAmt1000_dayInYears360_daysInMonth30_repayIn1Month() {

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
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(1000.0);
        emiCalculator.addDisbursement(interestModel, LocalDate.of(2024, 1, 1), disbursedAmount);

        checkPeriod(interestModel, 0, 0, 1005.83, 0.0, 0.0, 5.83, 1000.0, 0.0);
        checkPeriod(interestModel, 0, 1, 1005.83, 0.005833333333, 5.833333333, 5.83, 1000.0, 0.0);

        final LocalDate dueDate = LocalDate.of(2024, 2, 1);
        final LocalDate startDay = LocalDate.of(2024, 1, 1);

        emiCalculator.payInterest(interestModel, dueDate, startDay.plusDays(3), toMoney(0.56));
        emiCalculator.creditInterest(interestModel, startDay.plusDays(3), toMoney(0.0));
        emiCalculator.addBalanceCorrection(interestModel, startDay.plusDays(3), toMoney(0.0));

        checkDailyInterest(interestModel, dueDate, startDay, 1, 0.19, 0.19);
        checkDailyInterest(interestModel, dueDate, startDay, 2, 0.19, 0.38);
        checkDailyInterest(interestModel, dueDate, startDay, 3, 0.18, 0.56);
        checkDailyInterest(interestModel, dueDate, startDay, 4, 0.19, 0.75);
        checkDailyInterest(interestModel, dueDate, startDay, 5, 0.19, 0.94);
        checkDailyInterest(interestModel, dueDate, startDay, 6, 0.19, 1.13);
        checkDailyInterest(interestModel, dueDate, startDay, 7, 0.19, 1.32);
        checkDailyInterest(interestModel, dueDate, startDay, 8, 0.19, 1.51);
        checkDailyInterest(interestModel, dueDate, startDay, 9, 0.18, 1.69);
        checkDailyInterest(interestModel, dueDate, startDay, 10, 0.19, 1.88);
        checkDailyInterest(interestModel, dueDate, startDay, 11, 0.19, 2.07);
        checkDailyInterest(interestModel, dueDate, startDay, 12, 0.19, 2.26);
        checkDailyInterest(interestModel, dueDate, startDay, 13, 0.19, 2.45);
        checkDailyInterest(interestModel, dueDate, startDay, 14, 0.18, 2.63);
        checkDailyInterest(interestModel, dueDate, startDay, 15, 0.19, 2.82);
        checkDailyInterest(interestModel, dueDate, startDay, 16, 0.19, 3.01);
        checkDailyInterest(interestModel, dueDate, startDay, 17, 0.19, 3.20);
        checkDailyInterest(interestModel, dueDate, startDay, 18, 0.19, 3.39);
        checkDailyInterest(interestModel, dueDate, startDay, 19, 0.19, 3.58);
        checkDailyInterest(interestModel, dueDate, startDay, 20, 0.18, 3.76);
        checkDailyInterest(interestModel, dueDate, startDay, 21, 0.19, 3.95);
        checkDailyInterest(interestModel, dueDate, startDay, 22, 0.19, 4.14);
        checkDailyInterest(interestModel, dueDate, startDay, 23, 0.19, 4.33);
        checkDailyInterest(interestModel, dueDate, startDay, 24, 0.19, 4.52);
        checkDailyInterest(interestModel, dueDate, startDay, 25, 0.18, 4.7);
        checkDailyInterest(interestModel, dueDate, startDay, 26, 0.19, 4.89);
        checkDailyInterest(interestModel, dueDate, startDay, 27, 0.19, 5.08);
        checkDailyInterest(interestModel, dueDate, startDay, 28, 0.19, 5.27);
        checkDailyInterest(interestModel, dueDate, startDay, 29, 0.19, 5.46);
        checkDailyInterest(interestModel, dueDate, startDay, 30, 0.19, 5.65);
        checkDailyInterest(interestModel, dueDate, startDay, 31, 0.18, 5.83);
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
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount1st = toMoney(1000.0);
        final Money disbursedAmount2nd = toMoney(1000.0);
        emiCalculator.addDisbursement(interestModel, LocalDate.of(2024, 1, 1), disbursedAmount1st);
        emiCalculator.addDisbursement(interestModel, LocalDate.of(2024, 1, 15), disbursedAmount2nd);

        checkPeriod(interestModel, 0, 0, 2009.03, 0.0, 0.0, 9.03, 2000.0, 0.0);
        checkPeriod(interestModel, 0, 1, 2009.03, 0.002634408602, 2.634408602, 9.03, 2000.0, 0.0);
        checkPeriod(interestModel, 0, 2, 2009.03, 0.003198924731, 6.397849462, 9.03, 2000.0, 0.0);

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
        checkDailyInterest(interestModel, dueDate, startDay, 8, 0.19, 1.51);
        checkDailyInterest(interestModel, dueDate, startDay, 9, 0.18, 1.69);
        checkDailyInterest(interestModel, dueDate, startDay, 10, 0.19, 1.88);
        checkDailyInterest(interestModel, dueDate, startDay, 11, 0.19, 2.07);
        checkDailyInterest(interestModel, dueDate, startDay, 12, 0.19, 2.26);
        checkDailyInterest(interestModel, dueDate, startDay, 13, 0.19, 2.45);
        checkDailyInterest(interestModel, dueDate, startDay, 14, 0.18, 2.63);

        // 2nd 1000 disbursement accruals
        // Total Interest: 6.40 (17 days)
        checkDailyInterest(interestModel, dueDate, startDay, 15, 0.38, 3.01);
        checkDailyInterest(interestModel, dueDate, startDay, 16, 0.38, 3.39);
        checkDailyInterest(interestModel, dueDate, startDay, 17, 0.37, 3.76);
        checkDailyInterest(interestModel, dueDate, startDay, 18, 0.38, 4.14);
        checkDailyInterest(interestModel, dueDate, startDay, 19, 0.38, 4.52);
        checkDailyInterest(interestModel, dueDate, startDay, 20, 0.37, 4.89);
        checkDailyInterest(interestModel, dueDate, startDay, 21, 0.38, 5.27);
        checkDailyInterest(interestModel, dueDate, startDay, 22, 0.38, 5.65);
        checkDailyInterest(interestModel, dueDate, startDay, 23, 0.37, 6.02);
        checkDailyInterest(interestModel, dueDate, startDay, 24, 0.38, 6.4);
        checkDailyInterest(interestModel, dueDate, startDay, 25, 0.37, 6.77);
        checkDailyInterest(interestModel, dueDate, startDay, 26, 0.38, 7.15);
        checkDailyInterest(interestModel, dueDate, startDay, 27, 0.38, 7.53);
        checkDailyInterest(interestModel, dueDate, startDay, 28, 0.37, 7.90);
        checkDailyInterest(interestModel, dueDate, startDay, 29, 0.38, 8.28);
        checkDailyInterest(interestModel, dueDate, startDay, 30, 0.38, 8.66);
        checkDailyInterest(interestModel, dueDate, startDay, 31, 0.37, 9.03);
    }

    @Test
    public void test_singleInterestPauseAmt100_dayInYears360_daysInMonth30_repayEvery1Month() {
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)),
                repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)),
                repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)),
                repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)),
                repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)),
                repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(7.0);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);
        emiCalculator.applyInterestPause(interestSchedule, LocalDate.of(2024, 2, 5), LocalDate.of(2024, 2, 10));

        checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 0, 1, 17.01, 0.005833333333, 0.5833333333, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 1, 0, 17.01, 0.000603448276, 0.0504301724109, 0.39, 16.62, 66.95);
        checkPeriod(interestSchedule, 1, 1, 17.01, 0.001206896552, 0.0, 0.39, 16.62, 66.95);
        checkPeriod(interestSchedule, 1, 2, 17.01, 0.004022988506, 0.336201149446, 0.39, 16.62, 66.95);
        checkPeriod(interestSchedule, 2, 0, 17.01, 0.005833333333, 0.390541666643, 0.39, 16.62, 50.33);
        checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.29359166665, 0.29, 16.72, 33.61);
        checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.196058333322, 0.2, 16.81, 16.8);
        checkPeriod(interestSchedule, 5, 0, 16.9, 0.005833333333, 0.0979999999944, 0.1, 16.8, 0.0);
    }

    @Test
    public void test_interestPauseBetweenTwoPeriodsAmt100_dayInYears360_daysInMonth30_repayEvery1Month() {
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)),
                repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)),
                repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)),
                repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)),
                repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)),
                repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(7.0);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);
        emiCalculator.applyInterestPause(interestSchedule, LocalDate.of(2024, 2, 10), LocalDate.of(2024, 3, 10));

        checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 0, 1, 17.01, 0.005833333333, 0.5833333333, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 1, 0, 17.01, 0.001609195402, 0.134480459762, 0.13, 16.88, 66.69);
        checkPeriod(interestSchedule, 1, 1, 17.01, 0.004224137931, 0.0, 0.13, 16.88, 66.69);
        checkPeriod(interestSchedule, 2, 0, 17.01, 0.001693548387, 0.0, 0.28, 16.73, 49.96);
        checkPeriod(interestSchedule, 2, 1, 17.01, 0.004139784946, 0.276082258049, 0.28, 16.73, 49.96);
        checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.291433333317, 0.29, 16.72, 33.24);
        checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.193899999989, 0.19, 16.82, 16.42);
        checkPeriod(interestSchedule, 5, 0, 16.52, 0.005833333333, 0.095783333328, 0.1, 16.42, 0.0);
    }

    @Test
    public void test_interestPauseFirstDayOfMonthAmt100_dayInYears360_daysInMonth30_repayEvery1Month() {
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)),
                repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)),
                repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)),
                repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)),
                repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)),
                repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(7.0);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);
        List<LoanTermVariationsData> loanTermVariations = new ArrayList<>();

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, loanTermVariations, installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);
        emiCalculator.applyInterestPause(interestSchedule, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 5));

        checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.56, 16.45, 83.55);
        checkPeriod(interestSchedule, 0, 1, 17.01, 0.00564516129, 0.564516129, 0.56, 16.45, 83.55);
        checkPeriod(interestSchedule, 0, 2, 17.01, 0.000188172043, 0.0, 0.56, 16.45, 83.55);
        checkPeriod(interestSchedule, 1, 0, 17.01, 0.000804597701, 0.0, 0.42, 16.59, 66.96);
        checkPeriod(interestSchedule, 1, 1, 17.01, 0.005028735632, 0.420150862055, 0.42, 16.59, 66.96);
        checkPeriod(interestSchedule, 2, 0, 17.01, 0.005833333333, 0.390599999978, 0.39, 16.62, 50.34);
        checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.293649999983, 0.29, 16.72, 33.62);
        checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.196116666655, 0.2, 16.81, 16.81);
        checkPeriod(interestSchedule, 5, 0, 16.91, 0.005833333333, 0.0980583333276, 0.1, 16.81, 0.0);
    }

    @Test
    public void test_interestPauseLastDayOfMonthAmt100_dayInYears360_daysInMonth30_repayEvery1Month() {
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)),
                repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)),
                repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)),
                repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)),
                repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)),
                repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(7.0);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);
        List<LoanTermVariationsData> loanTermVariations = new ArrayList<>();

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, loanTermVariations, installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);
        emiCalculator.applyInterestPause(interestSchedule, LocalDate.of(2024, 1, 10), LocalDate.of(2024, 1, 31));

        checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.17, 16.84, 83.16);
        checkPeriod(interestSchedule, 0, 1, 17.01, 0.001505376344, 0.1505376344, 0.17, 16.84, 83.16);
        checkPeriod(interestSchedule, 0, 2, 17.01, 0.004139784946, 0.0, 0.17, 16.84, 83.16);
        checkPeriod(interestSchedule, 0, 3, 17.01, 0.000188172043, 0.0188172043, 0.17, 16.84, 83.16);
        checkPeriod(interestSchedule, 1, 0, 17.01, 0.005833333333, 0.485099999971, 0.49, 16.52, 66.64);
        checkPeriod(interestSchedule, 2, 0, 17.01, 0.005833333333, 0.38873333331, 0.39, 16.62, 50.02);
        checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.291783333317, 0.29, 16.72, 33.3);
        checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.194249999989, 0.19, 16.82, 16.48);
        checkPeriod(interestSchedule, 5, 0, 16.58, 0.005833333333, 0.0961333333278, 0.1, 16.48, 0.0);
    }

    @Test
    public void test_interestPauseWholeMonthAmt100_dayInYears360_daysInMonth30_repayEvery1Month() {
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)),
                repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)),
                repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)),
                repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)),
                repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)),
                repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(7.0);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);
        List<LoanTermVariationsData> loanTermVariations = new ArrayList<>();

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, loanTermVariations, installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);
        emiCalculator.applyInterestPause(interestSchedule, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 29));

        checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.56, 16.45, 83.55);
        checkPeriod(interestSchedule, 0, 1, 17.01, 0.00564516129, 0.564516129, 0.56, 16.45, 83.55);
        checkPeriod(interestSchedule, 0, 2, 17.01, 0.000188172043, 0.0, 0.56, 16.45, 83.55);
        checkPeriod(interestSchedule, 1, 0, 17.01, 0.005632183908, 0.0, 0.02, 16.99, 66.56);
        checkPeriod(interestSchedule, 1, 1, 17.01, 0.000201149425, 0.0168060344588, 0.02, 16.99, 66.56);
        checkPeriod(interestSchedule, 2, 0, 17.01, 0.005833333333, 0.388266666645, 0.39, 16.62, 49.94);
        checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.29131666665, 0.29, 16.72, 33.22);
        checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.193783333322, 0.19, 16.82, 16.4);
        checkPeriod(interestSchedule, 5, 0, 16.5, 0.005833333333, 0.0956666666613, 0.1, 16.4, 0.0);
    }

    @Test
    public void test_interestPauseTwoWholeMonthsAmt100_dayInYears360_daysInMonth30_repayEvery1Month() {
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)),
                repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)),
                repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)),
                repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)),
                repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)),
                repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(7.0);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);
        List<LoanTermVariationsData> loanTermVariations = new ArrayList<>();

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, loanTermVariations, installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);
        emiCalculator.applyInterestPause(interestSchedule, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 31));

        checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.56, 16.45, 83.55);
        checkPeriod(interestSchedule, 0, 1, 17.01, 0.00564516129, 0.564516129, 0.56, 16.45, 83.55);
        checkPeriod(interestSchedule, 0, 2, 17.01, 0.000188172043, 0.0, 0.56, 16.45, 83.55);
        checkPeriod(interestSchedule, 1, 0, 17.01, 0.005833333333, 0.0, 0.0, 17.01, 66.54);
        checkPeriod(interestSchedule, 2, 0, 17.01, 0.00564516129, 0.0, 0.01, 17.0, 49.54);
        checkPeriod(interestSchedule, 2, 1, 17.01, 0.000188172043, 0.0125209677412, 0.01, 17.0, 49.54);
        checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.288983333317, 0.29, 16.72, 32.82);
        checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.191449999989, 0.19, 16.82, 16.0);
        checkPeriod(interestSchedule, 5, 0, 16.09, 0.005833333333, 0.0933333333279, 0.09, 16.0, 0.0);
    }

    @Test
    public void test_interestPauseAndExistedMultipleInterestPeriodsAmt100_dayInYears360_daysInMonth30_repayEvery1Month() {
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)),
                repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)),
                repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)),
                repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)),
                repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)),
                repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(7.0);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);
        List<LoanTermVariationsData> loanTermVariations = new ArrayList<>();

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, loanTermVariations, installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);
        emiCalculator.changeInterestRate(interestSchedule, LocalDate.of(2024, 2, 11), BigDecimal.valueOf(7.0));
        emiCalculator.changeInterestRate(interestSchedule, LocalDate.of(2024, 2, 21), BigDecimal.valueOf(7.0));
        emiCalculator.applyInterestPause(interestSchedule, LocalDate.of(2024, 2, 5), LocalDate.of(2024, 2, 15));

        checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 0, 1, 17.01, 0.005833333333, 0.5833333333, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 1, 0, 17.01, 0.000603448276, 0.0504301724109, 0.3, 16.71, 66.86);
        checkPeriod(interestSchedule, 1, 1, 17.01, 0.002212643678, 0.0, 0.3, 16.71, 66.86);
        checkPeriod(interestSchedule, 1, 2, 17.01, 0.001005747126, 0.0840502873475, 0.3, 16.71, 66.86);
        checkPeriod(interestSchedule, 1, 3, 17.01, 0.002011494253, 0.168100574723, 0.3, 16.71, 66.86);
        checkPeriod(interestSchedule, 2, 0, 17.01, 0.005833333333, 0.390016666645, 0.39, 16.62, 50.24);
        checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.29306666665, 0.29, 16.72, 33.52);
        checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.195533333322, 0.2, 16.81, 16.71);
        checkPeriod(interestSchedule, 5, 0, 16.81, 0.005833333333, 0.0974749999944, 0.1, 16.71, 0.0);
    }

    @Test
    public void test_interestPauseBetweenRepaymentPeriodsAndExistedMultipleInterestPeriods_Amt100_dayInYears360_daysInMonth30_repayEvery1Month() {
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)),
                repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)),
                repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)),
                repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)),
                repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)),
                repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(7.0);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);
        List<LoanTermVariationsData> loanTermVariations = new ArrayList<>();

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, loanTermVariations, installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);
        emiCalculator.changeInterestRate(interestSchedule, LocalDate.of(2024, 2, 6), BigDecimal.valueOf(7.0));
        emiCalculator.changeInterestRate(interestSchedule, LocalDate.of(2024, 2, 21), BigDecimal.valueOf(7.0));
        emiCalculator.changeInterestRate(interestSchedule, LocalDate.of(2024, 3, 6), BigDecimal.valueOf(7.0));
        emiCalculator.changeInterestRate(interestSchedule, LocalDate.of(2024, 3, 21), BigDecimal.valueOf(7.0));
        emiCalculator.applyInterestPause(interestSchedule, LocalDate.of(2024, 2, 10), LocalDate.of(2024, 3, 10));

        checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 0, 1, 17.01, 0.005833333333, 0.5833333333, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 1, 0, 17.01, 0.000804597701, 0.0672402298812, 0.13, 16.88, 66.69);
        checkPeriod(interestSchedule, 1, 1, 17.01, 0.000804597701, 0.0672402298824, 0.13, 16.88, 66.69);
        checkPeriod(interestSchedule, 1, 2, 17.01, 0.004224137931, 0.0, 0.13, 16.88, 66.69);
        checkPeriod(interestSchedule, 2, 0, 17.01, 0.001693548387, 0.0, 0.28, 16.73, 49.96);
        checkPeriod(interestSchedule, 2, 1, 17.01, 0.001881720430, 0.125491935477, 0.28, 16.73, 49.96);
        checkPeriod(interestSchedule, 2, 1, 17.01, 0.00188172043, 0.125491935477, 0.28, 16.73, 49.96);
        checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.291433333317, 0.29, 16.72, 33.24);
        checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.193899999989, 0.19, 16.82, 16.42);
        checkPeriod(interestSchedule, 5, 0, 16.52, 0.005833333333, 0.095783333328, 0.1, 16.42, 0.0);
    }

    @Test
    public void test_interestPauseOnFirstDayOfMonthAndExistedMultipleInterestPeriods_Amt100_dayInYears360_daysInMonth30_repayEvery1Month() {
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)),
                repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)),
                repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)),
                repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)),
                repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)),
                repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(7.0);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);
        List<LoanTermVariationsData> loanTermVariations = new ArrayList<>();

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, loanTermVariations, installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);
        emiCalculator.changeInterestRate(interestSchedule, LocalDate.of(2024, 2, 1), BigDecimal.valueOf(7.0));
        emiCalculator.changeInterestRate(interestSchedule, LocalDate.of(2024, 2, 6), BigDecimal.valueOf(7.0));
        emiCalculator.applyInterestPause(interestSchedule, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 5));

        checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.56, 16.45, 83.55);
        checkPeriod(interestSchedule, 0, 1, 17.01, 0.00564516129, 0.564516129, 0.56, 16.45, 83.55);
        checkPeriod(interestSchedule, 0, 2, 17.01, 0.000188172043, 0.0, 0.56, 16.45, 83.55);
        checkPeriod(interestSchedule, 1, 0, 17.01, 0.000804597701, 0.0, 0.42, 16.59, 66.96);
        checkPeriod(interestSchedule, 1, 1, 17.01, 0.005028735632, 0.420150862055, 0.42, 16.59, 66.96);
        checkPeriod(interestSchedule, 2, 0, 17.01, 0.005833333333, 0.390599999978, 0.39, 16.62, 50.34);
        checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.293649999983, 0.29, 16.72, 33.62);
        checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.196116666655, 0.2, 16.81, 16.81);
        checkPeriod(interestSchedule, 5, 0, 16.91, 0.005833333333, 0.0980583333276, 0.1, 16.81, 0.0);
    }

    @Test
    public void test_interestPauseBorderedByPeriod_Amt100_dayInYears360_daysInMonth30_repayEvery1Month() {
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)),
                repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)),
                repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)),
                repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)),
                repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)),
                repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(7.0);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);
        List<LoanTermVariationsData> loanTermVariations = new ArrayList<>();

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, loanTermVariations, installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);
        emiCalculator.applyInterestPause(interestSchedule, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1));

        checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.56, 16.45, 83.55);
        checkPeriod(interestSchedule, 0, 1, 17.01, 0.00564516129, 0.564516129, 0.56, 16.45, 83.55);
        checkPeriod(interestSchedule, 0, 2, 17.01, 0.000188172043, 0.0, 0.56, 16.45, 83.55);
        checkPeriod(interestSchedule, 1, 0, 17.01, 0.005833333333, 0.0, 0.0, 17.01, 66.54);
        checkPeriod(interestSchedule, 2, 0, 17.01, 0.005833333333, 0.388149999977, 0.39, 16.62, 49.92);
        checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.291199999983, 0.29, 16.72, 33.2);
        checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.193666666656, 0.19, 16.82, 16.38);
        checkPeriod(interestSchedule, 5, 0, 16.48, 0.005833333333, 0.0955499999946, 0.1, 16.38, 0.0);
    }

    @Test
    public void test_interestPauseOnTheFirstDayOfFirstPeriodAmt100_dayInYears360_daysInMonth30_repayEvery1Month() {
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)),
                repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)),
                repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)),
                repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)),
                repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)),
                repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(7.0);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);
        List<LoanTermVariationsData> loanTermVariations = new ArrayList<>();

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, loanTermVariations, installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);
        emiCalculator.applyInterestPause(interestSchedule, LocalDate.of(2024, 1, 2), LocalDate.of(2024, 1, 10));
        checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.43, 16.58, 83.42);
        checkPeriod(interestSchedule, 0, 1, 17.01, 0.000188172043, 0.0188172043, 0.43, 16.58, 83.42);
        checkPeriod(interestSchedule, 0, 2, 17.01, 0.001505376344, 0.0, 0.43, 16.58, 83.42);
        checkPeriod(interestSchedule, 1, 0, 17.01, 0.005833333333, 0.486616666638, 0.49, 16.52, 66.90);
        checkPeriod(interestSchedule, 2, 0, 17.01, 0.005833333333, 0.390249999978, 0.39, 16.62, 50.28);
        checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.293299999983, 0.29, 16.72, 33.56);
        checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.195766666655, 0.20, 16.81, 16.75);
        checkPeriod(interestSchedule, 5, 0, 16.85, 0.005833333333, 0.0977083333278, 0.10, 16.75, 0.0);
    }

    @Test
    public void test_reschedule_disbursedAmt100_dayInYears360_daysInMonth30_repayEvery1Month() {
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 1)),
                repayment(2, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)),
                repayment(3, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)),
                repayment(4, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)),
                repayment(5, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)),
                repayment(6, LocalDate.of(2024, 7, 1), LocalDate.of(2024, 8, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(7);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);
        List<LoanTermVariationsData> loanTermVariationsData = List.of(//
                new LoanTermVariationsData(1L, LoanTermVariationType.DUE_DATE.getValue(), //
                        LocalDate.of(2024, 2, 1), null, //
                        LocalDate.of(2024, 3, 1), false));

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, loanTermVariationsData, installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 1.17, 15.84, 84.16);
        checkPeriod(interestSchedule, 0, 1, 17.01, 0.005833333333, 1.1666666667, 1.17, 15.84, 84.16);
        checkPeriod(interestSchedule, 1, 0, 17.01, 0.005833333333, 0.490933333306, 0.49, 16.52, 67.64);
        checkPeriod(interestSchedule, 2, 0, 17.01, 0.005833333333, 0.394566666645, 0.39, 16.62, 51.02);
        checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.29761666665, 0.30, 16.71, 34.31);
        checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.200141666655, 0.20, 16.81, 17.50);
        checkPeriod(interestSchedule, 5, 0, 17.60, 0.005833333333, 0.102083333328, 0.10, 17.50, 0.0);
    }

    @Test
    public void test_two_reschedules_disbursedAmt100_dayInYears360_daysInMonth30_repayEvery1Month() {
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 1)),
                repayment(2, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)),
                repayment(3, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 6, 1)),
                repayment(4, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)),
                repayment(5, LocalDate.of(2024, 7, 1), LocalDate.of(2024, 8, 1)),
                repayment(6, LocalDate.of(2024, 8, 1), LocalDate.of(2024, 9, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(7);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);
        List<LoanTermVariationsData> loanTermVariationsData = List.of(//
                new LoanTermVariationsData(1L, LoanTermVariationType.DUE_DATE.getValue(), //
                        LocalDate.of(2024, 2, 1), null, //
                        LocalDate.of(2024, 3, 1), false), //
                new LoanTermVariationsData(2L, LoanTermVariationType.DUE_DATE.getValue(), //
                        LocalDate.of(2024, 5, 1), null, //
                        LocalDate.of(2024, 6, 1), false));

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, loanTermVariationsData, installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 1.17, 15.84, 84.16);
        checkPeriod(interestSchedule, 0, 1, 17.01, 0.005833333333, 1.1666666667, 1.17, 15.84, 84.16);
        checkPeriod(interestSchedule, 1, 0, 17.01, 0.005833333333, 0.490933333306, 0.49, 16.52, 67.64);
        checkPeriod(interestSchedule, 2, 0, 17.01, 0.005833333333, 0.789133333354, 0.79, 16.22, 51.42);
        checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.299949999983, 0.30, 16.71, 34.71);
        checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.202474999988, 0.20, 16.81, 17.90);
        checkPeriod(interestSchedule, 5, 0, 18.00, 0.005833333333, 0.104416666661, 0.10, 17.90, 0.0);
    }

    @Test
    public void test_reschedule_partial_period_disbursedAmt100_dayInYears360_daysInMonth30_repayEvery1Month() {
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)),
                repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 4, 15)),
                repayment(3, LocalDate.of(2024, 4, 15), LocalDate.of(2024, 5, 15)),
                repayment(4, LocalDate.of(2024, 5, 15), LocalDate.of(2024, 6, 15)),
                repayment(5, LocalDate.of(2024, 6, 15), LocalDate.of(2024, 7, 15)),
                repayment(6, LocalDate.of(2024, 7, 15), LocalDate.of(2024, 8, 15)));

        final BigDecimal interestRate = BigDecimal.valueOf(7);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);
        List<LoanTermVariationsData> loanTermVariationsData = List.of(//
                new LoanTermVariationsData(1L, LoanTermVariationType.DUE_DATE.getValue(), //
                        LocalDate.of(2024, 3, 1), null, //
                        LocalDate.of(2024, 4, 15), false));

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, loanTermVariationsData, installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 0, 1, 17.01, 0.005833333333, 0.5833333333, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 1, 0, 17.01, 0.005833333333, 1.20247944445, 1.20, 15.81, 67.76);
        checkPeriod(interestSchedule, 2, 0, 17.01, 0.005833333333, 0.395266666644, 0.40, 16.61, 51.15);
        checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.298374999983, 0.3, 16.71, 34.44);
        checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.200899999989, 0.20, 16.81, 17.63);
        checkPeriod(interestSchedule, 5, 0, 17.73, 0.005833333333, 0.102841666661, 0.10, 17.63, 0.0);
    }

    @Test
    public void test_actual_actual_repayment_schedule_across_multiple_years() {
        MathContext mc = new MathContext(12, RoundingMode.HALF_UP);
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2023, 11, 13), LocalDate.of(2023, 12, 13)),
                repayment(2, LocalDate.of(2023, 12, 13), LocalDate.of(2024, 1, 13)),
                repayment(3, LocalDate.of(2024, 1, 13), LocalDate.of(2024, 2, 13)),
                repayment(4, LocalDate.of(2024, 2, 13), LocalDate.of(2024, 3, 13)),
                repayment(5, LocalDate.of(2024, 3, 13), LocalDate.of(2024, 4, 13)),
                repayment(6, LocalDate.of(2024, 4, 13), LocalDate.of(2024, 5, 13)));

        final BigDecimal interestRate = BigDecimal.valueOf(9.99);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(5000.0);
        LocalDate disbursementDate = LocalDate.of(2023, 11, 13);

        emiCalculator.addDisbursement(interestSchedule, disbursementDate, disbursedAmount);

        checkPeriod(interestSchedule, 0, 857.71, 41.05, 816.66, 4183.34, false);
        checkPeriod(interestSchedule, 1, 857.71, 35.45, 822.26, 3361.08, false);
        checkPeriod(interestSchedule, 2, 857.71, 28.44, 829.27, 2531.81, false);
        checkPeriod(interestSchedule, 3, 857.71, 20.04, 837.67, 1694.14, false);
        checkPeriod(interestSchedule, 4, 857.71, 14.33, 843.38, 850.76, false);
        checkPeriod(interestSchedule, 5, 857.73, 6.97, 850.76, 0, false);

        emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2023, 12, 13), LocalDate.of(2023, 12, 10),
                Money.of(currency, BigDecimal.valueOf(820.76)));
        emiCalculator.payInterest(interestSchedule, LocalDate.of(2023, 12, 13), LocalDate.of(2023, 12, 10),
                Money.of(currency, BigDecimal.valueOf(36.95)));

        checkPeriod(interestSchedule, 0, 857.71, 36.95, 820.76, 4179.24, true);
        checkPeriod(interestSchedule, 1, 857.71, 38.85, 818.86, 3360.38, false);
        checkPeriod(interestSchedule, 2, 857.71, 28.43, 829.28, 2531.1, false);
        checkPeriod(interestSchedule, 3, 857.71, 20.04, 837.67, 1693.43, false);
        checkPeriod(interestSchedule, 4, 857.71, 14.33, 843.38, 850.05, false);
        checkPeriod(interestSchedule, 5, 857.01, 6.96, 850.05, 0, false);
    }

    @Test
    public void test_actual_actual_repayment_schedule_across_multiple_years_overdue() {
        MathContext mc = new MathContext(12, RoundingMode.HALF_UP);
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2023, 11, 13), LocalDate.of(2023, 12, 13)),
                repayment(2, LocalDate.of(2023, 12, 13), LocalDate.of(2024, 1, 13)),
                repayment(3, LocalDate.of(2024, 1, 13), LocalDate.of(2024, 2, 13)),
                repayment(4, LocalDate.of(2024, 2, 13), LocalDate.of(2024, 3, 13)),
                repayment(5, LocalDate.of(2024, 3, 13), LocalDate.of(2024, 4, 13)),
                repayment(6, LocalDate.of(2024, 4, 13), LocalDate.of(2024, 5, 13)));

        final BigDecimal interestRate = BigDecimal.valueOf(9.99);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(5000.0);
        LocalDate disbursementDate = LocalDate.of(2023, 11, 13);

        emiCalculator.addDisbursement(interestSchedule, disbursementDate, disbursedAmount);

        for (int i = 0; i < 6; i++) {
            LocalDate dueDate = expectedRepaymentPeriods.get(i).getDueDate();
            PeriodDueDetails dueAmounts = emiCalculator.getDueAmounts(interestSchedule, dueDate, dueDate);
            Money duePrincipal = dueAmounts.getDuePrincipal();
            emiCalculator.addBalanceCorrection(interestSchedule, dueDate, duePrincipal);
        }

        checkPeriod(interestSchedule, 0, 857.71, 41.05, 816.66, 5000.0, false);
        checkPeriod(interestSchedule, 1, 857.71, 42.37, 815.34, 5000.0, false);
        checkPeriod(interestSchedule, 2, 857.71, 42.31, 815.4, 5000.0, false);
        checkPeriod(interestSchedule, 3, 857.71, 39.58, 818.13, 5000.0, false);
        checkPeriod(interestSchedule, 4, 857.71, 42.31, 815.4, 5000.0, false);
        checkPeriod(interestSchedule, 5, 960.01, 40.94, 919.07, 5000.0, false);
    }

    @Test
    public void test_repayment_actual_actual_repayment_schedule_across_multiple_years_overdue() {
        MathContext mc = new MathContext(12, RoundingMode.HALF_UP);
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2023, 11, 13), LocalDate.of(2023, 12, 13)),
                repayment(2, LocalDate.of(2023, 12, 13), LocalDate.of(2024, 1, 13)),
                repayment(3, LocalDate.of(2024, 1, 13), LocalDate.of(2024, 2, 13)),
                repayment(4, LocalDate.of(2024, 2, 13), LocalDate.of(2024, 3, 13)),
                repayment(5, LocalDate.of(2024, 3, 13), LocalDate.of(2024, 4, 13)),
                repayment(6, LocalDate.of(2024, 4, 13), LocalDate.of(2024, 5, 13)));

        final BigDecimal interestRate = BigDecimal.valueOf(9.99);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(5000.0);
        LocalDate disbursementDate = LocalDate.of(2023, 11, 13);

        emiCalculator.addDisbursement(interestSchedule, disbursementDate, disbursedAmount);

        emiCalculator.payInterest(interestSchedule, LocalDate.of(2023, 12, 13), LocalDate.of(2023, 12, 10),
                Money.of(currency, BigDecimal.valueOf(36.95)));
        emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2023, 12, 13), LocalDate.of(2023, 12, 10),
                Money.of(currency, BigDecimal.valueOf(820.76)));

        for (int i = 1; i < 6; i++) {
            LocalDate dueDate = expectedRepaymentPeriods.get(i).getDueDate();
            PeriodDueDetails dueAmounts = emiCalculator.getDueAmounts(interestSchedule, dueDate, dueDate);
            Money duePrincipal = dueAmounts.getDuePrincipal();
            emiCalculator.addBalanceCorrection(interestSchedule, dueDate, duePrincipal);
        }

        checkPeriod(interestSchedule, 0, 857.71, 36.95, 820.76, 4179.24, true);
        checkPeriod(interestSchedule, 1, 857.71, 38.85, 818.86, 4179.24, false);
        checkPeriod(interestSchedule, 2, 857.71, 35.36, 822.35, 4179.24, false);
        checkPeriod(interestSchedule, 3, 857.71, 33.08, 824.63, 4179.24, false);
        checkPeriod(interestSchedule, 4, 857.71, 35.36, 822.35, 4179.24, false);
        checkPeriod(interestSchedule, 5, 925.27, 34.22, 891.05, 4179.24, false);
    }

    @Test
    public void test_360_30_repayment_schedule_disbursement_month_end() {
        MathContext mc = new MathContext(12, RoundingMode.HALF_UP);
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2023, 10, 31), LocalDate.of(2023, 11, 30)),
                repayment(2, LocalDate.of(2023, 11, 30), LocalDate.of(2023, 12, 31)),
                repayment(3, LocalDate.of(2023, 12, 31), LocalDate.of(2024, 1, 31)),
                repayment(4, LocalDate.of(2024, 1, 31), LocalDate.of(2024, 2, 29)),
                repayment(5, LocalDate.of(2024, 2, 29), LocalDate.of(2024, 3, 31)),
                repayment(6, LocalDate.of(2024, 3, 31), LocalDate.of(2024, 4, 30)));

        final BigDecimal interestRate = BigDecimal.valueOf(9.99);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(2450.0);
        LocalDate disbursementDate = LocalDate.of(2023, 10, 31);

        emiCalculator.addDisbursement(interestSchedule, disbursementDate, disbursedAmount);

        checkPeriod(interestSchedule, 0, 420.31, 20.40, 399.91, 2050.09, false);
        checkPeriod(interestSchedule, 1, 420.31, 17.07, 403.24, 1646.85, false);
        checkPeriod(interestSchedule, 2, 420.31, 13.71, 406.60, 1240.25, false);
        checkPeriod(interestSchedule, 3, 420.31, 10.33, 409.98, 830.27, false);
        checkPeriod(interestSchedule, 4, 420.31, 6.91, 413.40, 416.87, false);
        checkPeriod(interestSchedule, 5, 420.34, 3.47, 416.87, 0.00, false);
    }

    @Test
    public void test_360_30_repayment_schedule_disbursement_near_month_end() {
        MathContext mc = new MathContext(12, RoundingMode.HALF_UP);
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2023, 10, 30), LocalDate.of(2023, 11, 30)),
                repayment(2, LocalDate.of(2023, 11, 30), LocalDate.of(2023, 12, 30)),
                repayment(3, LocalDate.of(2023, 12, 30), LocalDate.of(2024, 1, 30)),
                repayment(4, LocalDate.of(2024, 1, 30), LocalDate.of(2024, 2, 29)),
                repayment(5, LocalDate.of(2024, 2, 29), LocalDate.of(2024, 3, 30)),
                repayment(6, LocalDate.of(2024, 3, 30), LocalDate.of(2024, 4, 30)));

        final BigDecimal interestRate = BigDecimal.valueOf(45.0);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(10000.0);
        LocalDate disbursementDate = LocalDate.of(2023, 10, 30);

        emiCalculator.addDisbursement(interestSchedule, disbursementDate, disbursedAmount);

        checkPeriod(interestSchedule, 0, 1892.12, 375.00, 1517.12, 8482.88, false);
        checkPeriod(interestSchedule, 1, 1892.12, 318.11, 1574.01, 6908.87, false);
        checkPeriod(interestSchedule, 2, 1892.12, 259.08, 1633.04, 5275.83, false);
        checkPeriod(interestSchedule, 3, 1892.12, 197.84, 1694.28, 3581.55, false);
        checkPeriod(interestSchedule, 4, 1892.12, 134.31, 1757.81, 1823.74, false);
        checkPeriod(interestSchedule, 5, 1892.13, 68.39, 1823.74, 0.00, false);
    }

    @Test
    public void test_360_30_repayment_schedule_disbursement_repay_every_2_months() {
        MathContext mc = new MathContext(12, RoundingMode.HALF_UP);
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2023, 10, 29), LocalDate.of(2023, 12, 29)),
                repayment(2, LocalDate.of(2023, 12, 29), LocalDate.of(2024, 2, 29)),
                repayment(3, LocalDate.of(2024, 2, 29), LocalDate.of(2024, 4, 29)),
                repayment(4, LocalDate.of(2024, 4, 29), LocalDate.of(2024, 6, 29)),
                repayment(5, LocalDate.of(2024, 6, 29), LocalDate.of(2024, 8, 29)),
                repayment(6, LocalDate.of(2024, 8, 29), LocalDate.of(2024, 10, 29)));

        final BigDecimal interestRate = BigDecimal.valueOf(19.99);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(2);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(3333.0);
        LocalDate disbursementDate = LocalDate.of(2023, 10, 29);

        emiCalculator.addDisbursement(interestSchedule, disbursementDate, disbursedAmount);

        checkPeriod(interestSchedule, 0, 622.04, 111.04, 511.00, 2822.00, false);
        checkPeriod(interestSchedule, 1, 622.04, 94.02, 528.02, 2293.98, false);
        checkPeriod(interestSchedule, 2, 622.04, 76.43, 545.61, 1748.37, false);
        checkPeriod(interestSchedule, 3, 622.04, 58.25, 563.79, 1184.58, false);
        checkPeriod(interestSchedule, 4, 622.04, 39.47, 582.57, 602.01, false);
        checkPeriod(interestSchedule, 5, 622.07, 20.06, 602.01, 0.00, false);
    }

    @Test
    public void test_actual_actual_repayment_schedule_disbursement_month_end() {
        MathContext mc = new MathContext(12, RoundingMode.HALF_UP);
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2023, 10, 31), LocalDate.of(2023, 11, 30)),
                repayment(2, LocalDate.of(2023, 11, 30), LocalDate.of(2023, 12, 31)),
                repayment(3, LocalDate.of(2023, 12, 31), LocalDate.of(2024, 1, 31)),
                repayment(4, LocalDate.of(2024, 1, 31), LocalDate.of(2024, 2, 29)),
                repayment(5, LocalDate.of(2024, 2, 29), LocalDate.of(2024, 3, 31)),
                repayment(6, LocalDate.of(2024, 3, 31), LocalDate.of(2024, 4, 30)));

        final BigDecimal interestRate = BigDecimal.valueOf(45.0);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(245000.0);
        LocalDate disbursementDate = LocalDate.of(2023, 10, 31);

        emiCalculator.addDisbursement(interestSchedule, disbursementDate, disbursedAmount);

        checkPeriod(interestSchedule, 0, 46343.27, 9061.64, 37281.63, 207718.37, false);
        checkPeriod(interestSchedule, 1, 46343.27, 7938.83, 38404.44, 169313.93, false);
        checkPeriod(interestSchedule, 2, 46343.27, 6453.36, 39889.91, 129424.02, false);
        checkPeriod(interestSchedule, 3, 46343.27, 4614.71, 41728.56, 87695.46, false);
        checkPeriod(interestSchedule, 4, 46343.27, 3342.49, 43000.78, 44694.68, false);
        checkPeriod(interestSchedule, 5, 46343.25, 1648.57, 44694.68, 0.00, false);
    }

    @Test
    public void test_actual_actual_repayment_schedule_disbursement_near_month_end() {
        MathContext mc = new MathContext(12, RoundingMode.HALF_UP);
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2021, 10, 30), LocalDate.of(2021, 11, 30)),
                repayment(2, LocalDate.of(2021, 11, 30), LocalDate.of(2021, 12, 30)),
                repayment(3, LocalDate.of(2021, 12, 30), LocalDate.of(2022, 1, 30)),
                repayment(4, LocalDate.of(2022, 1, 30), LocalDate.of(2022, 2, 28)),
                repayment(5, LocalDate.of(2022, 2, 28), LocalDate.of(2022, 3, 30)),
                repayment(6, LocalDate.of(2022, 3, 30), LocalDate.of(2022, 4, 30)));

        final BigDecimal interestRate = BigDecimal.valueOf(9.4822);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(1500.0);
        LocalDate disbursementDate = LocalDate.of(2021, 10, 30);

        emiCalculator.addDisbursement(interestSchedule, disbursementDate, disbursedAmount);

        checkPeriod(interestSchedule, 0, 256.95, 12.08, 244.87, 1255.13, false);
        checkPeriod(interestSchedule, 1, 256.95, 9.78, 247.17, 1007.96, false);
        checkPeriod(interestSchedule, 2, 256.95, 8.12, 248.83, 759.13, false);
        checkPeriod(interestSchedule, 3, 256.95, 5.72, 251.23, 507.90, false);
        checkPeriod(interestSchedule, 4, 256.95, 3.96, 252.99, 254.91, false);
        checkPeriod(interestSchedule, 5, 256.96, 2.05, 254.91, 0.00, false);
    }

    @Test
    public void test_actual_actual_repayment_schedule_disbursement_near_month_end_repay_every_2_months() {
        MathContext mc = new MathContext(12, RoundingMode.HALF_UP);
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2022, 10, 29), LocalDate.of(2022, 12, 29)),
                repayment(2, LocalDate.of(2022, 12, 29), LocalDate.of(2023, 2, 28)),
                repayment(3, LocalDate.of(2023, 2, 28), LocalDate.of(2023, 4, 29)),
                repayment(4, LocalDate.of(2023, 4, 29), LocalDate.of(2023, 6, 29)),
                repayment(5, LocalDate.of(2023, 6, 29), LocalDate.of(2023, 8, 29)),
                repayment(6, LocalDate.of(2023, 8, 29), LocalDate.of(2023, 10, 29)));

        final BigDecimal interestRate = BigDecimal.valueOf(7);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(5000);
        LocalDate disbursementDate = LocalDate.of(2022, 10, 29);

        emiCalculator.addDisbursement(interestSchedule, disbursementDate, disbursedAmount);

        checkPeriod(interestSchedule, 0, 867.68, 58.49, 809.19, 4190.81, false);
        checkPeriod(interestSchedule, 1, 867.68, 49.03, 818.65, 3372.16, false);
        checkPeriod(interestSchedule, 2, 867.68, 38.80, 828.88, 2543.28, false);
        checkPeriod(interestSchedule, 3, 867.68, 29.75, 837.93, 1705.35, false);
        checkPeriod(interestSchedule, 4, 867.68, 19.95, 847.73, 857.62, false);
        checkPeriod(interestSchedule, 5, 867.65, 10.03, 857.62, 0.00, false);
    }

    @Test
    public void test_actual_actual_fraction_period_calculation_with_interestRecognitionFromDisbursementDate_false() {
        MathContext mc = new MathContext(12, RoundingMode.HALF_UP);
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2023, 11, 13), LocalDate.of(2023, 12, 13)),
                repayment(2, LocalDate.of(2023, 12, 13), LocalDate.of(2024, 1, 13)),
                repayment(3, LocalDate.of(2024, 1, 13), LocalDate.of(2024, 2, 13)),
                repayment(4, LocalDate.of(2024, 2, 13), LocalDate.of(2024, 3, 13)),
                repayment(5, LocalDate.of(2024, 3, 13), LocalDate.of(2024, 4, 13)),
                repayment(6, LocalDate.of(2024, 4, 13), LocalDate.of(2024, 5, 13)));

        final BigDecimal interestRate = BigDecimal.valueOf(9.99);

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator
                .generatePeriodInterestScheduleModel(expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), null, mc);

        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2023, 11, 13), Money.of(currency, BigDecimal.valueOf(5000), mc));
        checkPeriod(interestSchedule, 0, 857.71, 41.05, 816.66, 4183.34, false);
        checkPeriod(interestSchedule, 1, 857.71, 35.45, 822.26, 3361.08, false);
        checkPeriod(interestSchedule, 2, 857.71, 28.44, 829.27, 2531.81, false);
        checkPeriod(interestSchedule, 3, 857.71, 20.04, 837.67, 1694.14, false);
        checkPeriod(interestSchedule, 4, 857.71, 14.33, 843.38, 850.76, false);
        checkPeriod(interestSchedule, 5, 857.73, 6.97, 850.76, 0.00, false);
    }

    @Test
    public void test_actual_actual_fraction_period_calculation_with_interestRecognitionFromDisbursementDate_true() {
        MathContext mc = new MathContext(12, RoundingMode.HALF_UP);
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2023, 11, 13), LocalDate.of(2023, 12, 13)),
                repayment(2, LocalDate.of(2023, 12, 13), LocalDate.of(2024, 1, 13)),
                repayment(3, LocalDate.of(2024, 1, 13), LocalDate.of(2024, 2, 13)),
                repayment(4, LocalDate.of(2024, 2, 13), LocalDate.of(2024, 3, 13)),
                repayment(5, LocalDate.of(2024, 3, 13), LocalDate.of(2024, 4, 13)),
                repayment(6, LocalDate.of(2024, 4, 13), LocalDate.of(2024, 5, 13)));

        final BigDecimal interestRate = BigDecimal.valueOf(9.99);

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.ACTUAL.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);
        Mockito.when(loanProductRelatedDetail.isInterestRecognitionOnDisbursementDate()).thenReturn(true);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator
                .generatePeriodInterestScheduleModel(expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), null, mc);

        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2023, 11, 13), Money.of(currency, BigDecimal.valueOf(5000), mc));
        checkPeriod(interestSchedule, 0, 857.72, 41.05, 816.67, 4183.33, false);
        checkPeriod(interestSchedule, 1, 857.72, 35.46, 822.26, 3361.07, false);
        checkPeriod(interestSchedule, 2, 857.72, 28.44, 829.28, 2531.79, false);
        checkPeriod(interestSchedule, 3, 857.72, 20.04, 837.68, 1694.11, false);
        checkPeriod(interestSchedule, 4, 857.72, 14.33, 843.39, 850.72, false);
        checkPeriod(interestSchedule, 5, 857.69, 6.97, 850.72, 0.00, false);
    }

    @Nested
    class ChargeBackPrincipalAmt100DayInYears360DaysInMonth30RepayEvery1MonthTests {

        @Test
        public void test_S1_full_chargeback_on_due_date_before_maturity_date() {
            final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                    repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)),
                    repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)),
                    repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)),
                    repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)),
                    repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)),
                    repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

            final BigDecimal interestRate = BigDecimal.valueOf(7.0);
            final Integer installmentAmountInMultiplesOf = null;

            Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
            Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
            Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
            Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
            Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
            Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

            final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                    expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

            final Money disbursedAmount = toMoney(100.0);
            emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

            checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.58, 16.43, 83.57);
            checkPeriod(interestSchedule, 0, 1, 17.01, 0.005833333333, 0.5833333333, 0.58, 16.43, 83.57);
            checkPeriod(interestSchedule, 1, 0, 17.01, 0.005833333333, 0.487491666639, 0.49, 16.52, 67.05);
            checkPeriod(interestSchedule, 2, 0, 17.01, 0.005833333333, 0.391124999979, 0.39, 16.62, 50.43);
            checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.294174999983, 0.29, 16.72, 33.71);
            checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.196641666655, 0.2, 16.81, 16.90);
            checkPeriod(interestSchedule, 5, 0, 17.00, 0.005833333333, 0.0985833333276, 0.1, 16.9, 0.0);

            // repay 1st period
            emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 1), toMoney(16.43));
            emiCalculator.payInterest(interestSchedule, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 1), toMoney(0.58));
            // repay 2nd period
            emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 1), toMoney(16.52));
            emiCalculator.payInterest(interestSchedule, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 1), toMoney(0.49));

            // full chargeback on duedate
            emiCalculator.creditPrincipal(interestSchedule, LocalDate.of(2024, 3, 1), toMoney(17.01));

            checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.58, 16.43, 83.57);
            checkPeriod(interestSchedule, 0, 1, 17.01, 0.005833333333, 0.5833333333, 0.58, 16.43, 83.57);
            checkPeriod(interestSchedule, 1, 0, 17.01, 0.005833333333, 0.487491666639, 0.49, 16.52, 84.06);
            checkPeriod(interestSchedule, 2, 0, 17.01, 0.005833333333, 0.490349999973, 0.49, 33.53, 50.53);
            checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.294758333316, 0.29, 16.72, 33.81);
            checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.197224999989, 0.2, 16.81, 17.00);
            checkPeriod(interestSchedule, 5, 0, 17.10, 0.005833333333, 0.0991666666611, 0.1, 17.00, 0.0);
        }

        @Test
        public void test_S2_S3_partial_and_full_chargeback_on_due_date_before_maturity_date() {
            final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                    repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)),
                    repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)),
                    repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)),
                    repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)),
                    repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)),
                    repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

            final BigDecimal interestRate = BigDecimal.valueOf(7.0);
            final Integer installmentAmountInMultiplesOf = null;

            Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
            Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
            Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
            Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
            Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
            Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

            final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                    expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

            final Money disbursedAmount = toMoney(100.0);
            emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

            checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.58, 16.43, 83.57);
            checkPeriod(interestSchedule, 0, 1, 17.01, 0.005833333333, 0.5833333333, 0.58, 16.43, 83.57);
            checkPeriod(interestSchedule, 1, 0, 17.01, 0.005833333333, 0.487491666639, 0.49, 16.52, 67.05);
            checkPeriod(interestSchedule, 2, 0, 17.01, 0.005833333333, 0.391124999979, 0.39, 16.62, 50.43);
            checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.294174999983, 0.29, 16.72, 33.71);
            checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.196641666655, 0.2, 16.81, 16.90);
            checkPeriod(interestSchedule, 5, 0, 17.00, 0.005833333333, 0.0985833333276, 0.1, 16.9, 0.0);

            // repay 1st period
            emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 1), toMoney(16.43));
            emiCalculator.payInterest(interestSchedule, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 1), toMoney(0.58));
            // repay 2nd period
            emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 1), toMoney(16.52));
            emiCalculator.payInterest(interestSchedule, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 1), toMoney(0.49));

            // partial chargeback
            emiCalculator.creditPrincipal(interestSchedule, LocalDate.of(2024, 3, 1), toMoney(15.0));

            checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.58, 16.43, 83.57);
            checkPeriod(interestSchedule, 0, 1, 17.01, 0.005833333333, 0.5833333333, 0.58, 16.43, 83.57);
            checkPeriod(interestSchedule, 1, 0, 17.01, 0.005833333333, 0.487491666639, 0.49, 16.52, 82.05);
            checkPeriod(interestSchedule, 2, 0, 17.01, 0.005833333333, 0.478624999974, 0.48, 31.53, 50.52);
            checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.294699999983, 0.29, 16.72, 33.80);
            checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.197166666655, 0.2, 16.81, 16.99);
            checkPeriod(interestSchedule, 5, 0, 17.09, 0.005833333333, 0.0991083333276, 0.1, 16.99, 0.0);

            // full chargeback
            emiCalculator.creditPrincipal(interestSchedule, LocalDate.of(2024, 3, 1), toMoney(17.01));

            checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.58, 16.43, 83.57);
            checkPeriod(interestSchedule, 0, 1, 17.01, 0.005833333333, 0.5833333333, 0.58, 16.43, 83.57);
            checkPeriod(interestSchedule, 1, 0, 17.01, 0.005833333333, 0.487491666639, 0.49, 16.52, 99.06);
            checkPeriod(interestSchedule, 2, 0, 17.01, 0.005833333333, 0.577849999968, 0.58, 48.44, 50.62);
            checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.295283333316, 0.3, 16.71, 33.91);
            checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.197808333322, 0.2, 16.81, 17.1);
            checkPeriod(interestSchedule, 5, 0, 17.20, 0.005833333333, 0.0997499999943, 0.1, 17.1, 0.0);
        }

        @Test
        public void test_S4_full_chargeback_in_middle_of_instalment_before_maturity_date() {
            final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                    repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)),
                    repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)),
                    repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)),
                    repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)),
                    repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)),
                    repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

            final BigDecimal interestRate = BigDecimal.valueOf(7.0);
            final Integer installmentAmountInMultiplesOf = null;

            Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
            Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
            Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
            Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
            Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
            Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

            final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                    expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

            final Money disbursedAmount = toMoney(100.0);
            emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

            checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.58, 16.43, 83.57);
            checkPeriod(interestSchedule, 0, 1, 17.01, 0.005833333333, 0.5833333333, 0.58, 16.43, 83.57);
            checkPeriod(interestSchedule, 1, 0, 17.01, 0.005833333333, 0.487491666639, 0.49, 16.52, 67.05);
            checkPeriod(interestSchedule, 2, 0, 17.01, 0.005833333333, 0.391124999979, 0.39, 16.62, 50.43);
            checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.294174999983, 0.29, 16.72, 33.71);
            checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.196641666655, 0.2, 16.81, 16.90);
            checkPeriod(interestSchedule, 5, 0, 17.00, 0.005833333333, 0.0985833333276, 0.1, 16.9, 0.0);

            // repay 1st period
            emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 1), toMoney(16.43));
            emiCalculator.payInterest(interestSchedule, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 1), toMoney(0.58));
            // repay 2nd period
            emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 1), toMoney(16.52));
            emiCalculator.payInterest(interestSchedule, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 1), toMoney(0.49));

            // full chargeback on duedate
            emiCalculator.creditPrincipal(interestSchedule, LocalDate.of(2024, 3, 15), toMoney(17.01));

            checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.58, 16.43, 83.57);
            checkPeriod(interestSchedule, 0, 1, 17.01, 0.005833333333, 0.5833333333, 0.58, 16.43, 83.57);
            checkPeriod(interestSchedule, 1, 0, 17.01, 0.005833333333, 0.487491666639, 0.49, 16.52, 67.05);
            checkPeriod(interestSchedule, 2, 0, 17.01, 0.002634408602, 0.176637096765, 0.45, 33.57, 50.49);
            checkPeriod(interestSchedule, 2, 1, 17.01, 0.003198924731, 0.268901612888, 0.45, 33.57, 50.49);
            checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.294524999983, 0.29, 16.72, 33.77);
            checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.196991666655, 0.2, 16.81, 16.96);
            checkPeriod(interestSchedule, 5, 0, 17.06, 0.005833333333, 0.0989333333277, 0.1, 16.96, 0.0);
        }
    }

    @Test
    public void test_chargeback_principalAndInterest_Amt100_dayInYears360_daysInMonth30_repayEvery1Month() {
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)),
                repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)),
                repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)),
                repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)),
                repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)),
                repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(7.0);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 0, 1, 17.01, 0.005833333333, 0.5833333333, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 1, 0, 17.01, 0.005833333333, 0.487491666639, 0.49, 16.52, 67.05);
        checkPeriod(interestSchedule, 2, 0, 17.01, 0.005833333333, 0.391124999979, 0.39, 16.62, 50.43);
        checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.294174999983, 0.29, 16.72, 33.71);
        checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.196641666655, 0.2, 16.81, 16.90);
        checkPeriod(interestSchedule, 5, 0, 17.00, 0.005833333333, 0.0985833333276, 0.1, 16.9, 0.0);

        // repay 1st period
        LocalDate txnDate = LocalDate.of(2024, 2, 1);
        emiCalculator.payPrincipal(interestSchedule, txnDate, txnDate, toMoney(16.43));
        emiCalculator.payInterest(interestSchedule, txnDate, txnDate, toMoney(0.58));

        // repay 2nd period
        txnDate = LocalDate.of(2024, 3, 1);
        emiCalculator.payPrincipal(interestSchedule, txnDate, txnDate, toMoney(16.52));
        emiCalculator.payInterest(interestSchedule, txnDate, txnDate, toMoney(0.49));

        // chargeback
        txnDate = LocalDate.of(2024, 3, 1);
        emiCalculator.creditPrincipal(interestSchedule, txnDate, toMoney(16.52));
        emiCalculator.creditInterest(interestSchedule, txnDate, toMoney(0.49));

        checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 0, 1, 17.01, 0.005833333333, 0.5833333333, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 1, 0, 17.01, 0.005833333333, 0.487491666639, 0.49, 16.52, 83.57);
        checkPeriod(interestSchedule, 2, 0, 17.01, 0.005833333333, 0.977491666638, 0.98, 33.04, 50.53);
        checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.294758333316, 0.29, 16.72, 33.81);
        checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.197224999989, 0.2, 16.81, 17.0);
        checkPeriod(interestSchedule, 5, 0, 17.10, 0.005833333333, 0.0991666666611, 0.1, 17.0, 0.0);
    }

    @Test
    public void test_chargeback_principalAndInterest_Amt100_dayInYears360_daysInMonth30_repayEvery1Month__() {
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)),
                repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)),
                repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)),
                repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)),
                repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)),
                repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(7.0);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 0, 1, 17.01, 0.005833333333, 0.5833333333, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 1, 0, 17.01, 0.005833333333, 0.487491666639, 0.49, 16.52, 67.05);
        checkPeriod(interestSchedule, 2, 0, 17.01, 0.005833333333, 0.391124999979, 0.39, 16.62, 50.43);
        checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.294174999983, 0.29, 16.72, 33.71);
        checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.196641666655, 0.2, 16.81, 16.90);
        checkPeriod(interestSchedule, 5, 0, 17.00, 0.005833333333, 0.0985833333276, 0.1, 16.9, 0.0);

        // repay 1st period
        LocalDate txnDate = LocalDate.of(2024, 2, 1);
        emiCalculator.payPrincipal(interestSchedule, txnDate, txnDate, toMoney(16.43));
        emiCalculator.payInterest(interestSchedule, txnDate, txnDate, toMoney(0.58));

        // repay 2nd period
        txnDate = LocalDate.of(2024, 3, 1);
        emiCalculator.payPrincipal(interestSchedule, txnDate, txnDate, toMoney(16.52));
        emiCalculator.payInterest(interestSchedule, txnDate, txnDate, toMoney(0.49));

        // chargeback
        txnDate = LocalDate.of(2024, 7, 1);
        emiCalculator.creditPrincipal(interestSchedule, txnDate, toMoney(16.52));
        emiCalculator.creditInterest(interestSchedule, txnDate, toMoney(0.49));

        checkPeriod(interestSchedule, 5, 0, 17.00, 0.005833333333, 0.0985833333276, 0.59, 33.42, 0.0);
    }

    @Test
    public void test_chargeback_less_principal_Amt100_dayInYears360_daysInMonth30_repayEvery1Month() {
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)),
                repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)),
                repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)),
                repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)),
                repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)),
                repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(7.0);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 0, 1, 17.01, 0.005833333333, 0.5833333333, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 1, 0, 17.01, 0.005833333333, 0.487491666639, 0.49, 16.52, 67.05);
        checkPeriod(interestSchedule, 2, 0, 17.01, 0.005833333333, 0.391124999979, 0.39, 16.62, 50.43);
        checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.294174999983, 0.29, 16.72, 33.71);
        checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.196641666655, 0.2, 16.81, 16.90);
        checkPeriod(interestSchedule, 5, 0, 17.00, 0.005833333333, 0.0985833333276, 0.1, 16.9, 0.0);

        // repay 1st period
        LocalDate txnDate = LocalDate.of(2024, 2, 1);
        emiCalculator.payPrincipal(interestSchedule, txnDate, txnDate, toMoney(16.43));
        emiCalculator.payInterest(interestSchedule, txnDate, txnDate, toMoney(0.58));

        // repay 2nd period
        txnDate = LocalDate.of(2024, 3, 1);
        emiCalculator.payPrincipal(interestSchedule, txnDate, txnDate, toMoney(16.52));
        emiCalculator.payInterest(interestSchedule, txnDate, txnDate, toMoney(0.49));

        // chargeback
        txnDate = LocalDate.of(2024, 3, 1);
        emiCalculator.creditPrincipal(interestSchedule, txnDate, toMoney(14.51));
        emiCalculator.creditInterest(interestSchedule, txnDate, toMoney(0.49));

        checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 0, 1, 17.01, 0.005833333333, 0.5833333333, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 1, 0, 17.01, 0.005833333333, 0.487491666639, 0.49, 16.52, 81.56);
        checkPeriod(interestSchedule, 2, 0, 17.01, 0.005833333333, 0.96576666664, 0.97, 31.04, 50.52);
        checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.294699999983, 0.29, 16.72, 33.80);
        checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.197166666655, 0.2, 16.81, 16.99);
        checkPeriod(interestSchedule, 5, 0, 17.09, 0.005833333333, 0.0991083333276, 0.1, 16.99, 0.0);
    }

    @Test
    public void test_chargeback_less_principal_and_no_chargeback_interest_Amt100_dayInYears360_daysInMonth30_repayEvery1Month() {
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)),
                repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)),
                repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)),
                repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)),
                repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)),
                repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(7.0);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 0, 1, 17.01, 0.005833333333, 0.5833333333, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 1, 0, 17.01, 0.005833333333, 0.487491666639, 0.49, 16.52, 67.05);
        checkPeriod(interestSchedule, 2, 0, 17.01, 0.005833333333, 0.391124999979, 0.39, 16.62, 50.43);
        checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.294174999983, 0.29, 16.72, 33.71);
        checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.196641666655, 0.2, 16.81, 16.90);
        checkPeriod(interestSchedule, 5, 0, 17.00, 0.005833333333, 0.0985833333276, 0.1, 16.9, 0.0);

        // repay 1st period
        LocalDate txnDate = LocalDate.of(2024, 2, 1);
        emiCalculator.payPrincipal(interestSchedule, txnDate, txnDate, toMoney(16.43));
        emiCalculator.payInterest(interestSchedule, txnDate, txnDate, toMoney(0.58));

        // repay 2nd period
        txnDate = LocalDate.of(2024, 3, 1);
        emiCalculator.payPrincipal(interestSchedule, txnDate, txnDate, toMoney(16.52));
        emiCalculator.payInterest(interestSchedule, txnDate, txnDate, toMoney(0.49));

        // chargeback
        txnDate = LocalDate.of(2024, 3, 1);
        emiCalculator.creditPrincipal(interestSchedule, txnDate, toMoney(15.0));

        checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 0, 1, 17.01, 0.005833333333, 0.5833333333, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 1, 0, 17.01, 0.005833333333, 0.487491666639, 0.49, 16.52, 82.05);
        checkPeriod(interestSchedule, 2, 0, 17.01, 0.005833333333, 0.478624999974, 0.48, 31.53, 50.52);
        checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.294699999983, 0.29, 16.72, 33.80);
        checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.197166666655, 0.2, 16.81, 16.99);
        checkPeriod(interestSchedule, 5, 0, 17.09, 0.005833333333, 0.0991083333276, 0.1, 16.99, 0.0);
    }

    @Test
    public void test_multi_chargeback_Amt100_dayInYears360_daysInMonth30_repayEvery1Month() {
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)),
                repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)),
                repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)),
                repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)),
                repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)),
                repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(7.0);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 0, 1, 17.01, 0.005833333333, 0.5833333333, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 1, 0, 17.01, 0.005833333333, 0.487491666639, 0.49, 16.52, 67.05);
        checkPeriod(interestSchedule, 2, 0, 17.01, 0.005833333333, 0.391124999979, 0.39, 16.62, 50.43);
        checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.294174999983, 0.29, 16.72, 33.71);
        checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.196641666655, 0.2, 16.81, 16.90);
        checkPeriod(interestSchedule, 5, 0, 17.00, 0.005833333333, 0.0985833333276, 0.1, 16.9, 0.0);

        // repay 1st period
        LocalDate txnDate = LocalDate.of(2024, 2, 1);
        emiCalculator.payPrincipal(interestSchedule, txnDate, txnDate, toMoney(16.43));
        emiCalculator.payInterest(interestSchedule, txnDate, txnDate, toMoney(0.58));

        // repay 2nd period
        txnDate = LocalDate.of(2024, 3, 1);
        emiCalculator.payPrincipal(interestSchedule, txnDate, txnDate, toMoney(16.52));
        emiCalculator.payInterest(interestSchedule, txnDate, txnDate, toMoney(0.49));

        // chargeback 1st
        txnDate = LocalDate.of(2024, 3, 1);
        emiCalculator.creditPrincipal(interestSchedule, txnDate, toMoney(15.0));
        // chargeback 2nd
        emiCalculator.creditPrincipal(interestSchedule, txnDate, toMoney(16.52));
        emiCalculator.creditInterest(interestSchedule, txnDate, toMoney(0.49));

        checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 0, 1, 17.01, 0.005833333333, 0.5833333333, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 1, 0, 17.01, 0.005833333333, 0.487491666639, 0.49, 16.52, 98.57);
        checkPeriod(interestSchedule, 2, 0, 17.01, 0.005833333333, 1.06499166663, 1.06, 47.96, 50.61);
        checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.295224999983, 0.30, 16.71, 33.90);
        checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.197749999989, 0.20, 16.81, 17.09);
        checkPeriod(interestSchedule, 5, 0, 17.19, 0.005833333333, 0.0996916666611, 0.10, 17.09, 0.0);
    }

    @Nested
    class LeapYear366OnlyForPeriodWith29thOfFebruaryTest {

        /**
         * February is split and leap year
         */
        @Test
        public void test_leap_year_only_actual_for_loan_S1() {
            final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                    repayment(1, LocalDate.of(2023, 12, 12), LocalDate.of(2024, 1, 12)),
                    repayment(1, LocalDate.of(2024, 1, 12), LocalDate.of(2024, 2, 12)),
                    repayment(1, LocalDate.of(2024, 2, 12), LocalDate.of(2024, 3, 12)),
                    repayment(1, LocalDate.of(2024, 3, 12), LocalDate.of(2024, 4, 12)),
                    repayment(1, LocalDate.of(2024, 4, 12), LocalDate.of(2024, 5, 12)),
                    repayment(1, LocalDate.of(2024, 5, 12), LocalDate.of(2024, 6, 12)));

            final BigDecimal interestRate = BigDecimal.valueOf(9.482);
            final Integer installmentAmountInMultiplesOf = null;

            Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
            Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.ACTUAL.getValue());
            Mockito.when(loanProductRelatedDetail.getDaysInYearCustomStrategy())
                    .thenReturn(DaysInYearCustomStrategyType.FEB_29_PERIOD_ONLY);
            Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.ACTUAL.getValue());
            Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
            Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
            Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

            final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                    expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

            final Money disbursedAmount = toMoney(10000.0);
            emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2023, 12, 12), disbursedAmount);

            checkPeriod(interestSchedule, 0, 1713.21, 80.53, 1632.68, 8367.32, false);
            checkPeriod(interestSchedule, 1, 1713.21, 67.38, 1645.83, 6721.49, false);
            checkPeriod(interestSchedule, 2, 1713.21, 50.50, 1662.71, 5058.78, false);
            checkPeriod(interestSchedule, 3, 1713.21, 40.74, 1672.47, 3386.31, false);
            checkPeriod(interestSchedule, 4, 1713.21, 26.39, 1686.82, 1699.49, false);
            checkPeriod(interestSchedule, 5, 1713.18, 13.69, 1699.49, 0.00, false);

        }

        /**
         * No february but leap year
         */
        @Test
        public void test_leap_year_only_actual_for_loan_S2() {
            final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                    repayment(1, LocalDate.of(2024, 7, 23), LocalDate.of(2024, 8, 23)),
                    repayment(1, LocalDate.of(2024, 8, 23), LocalDate.of(2024, 9, 23)),
                    repayment(1, LocalDate.of(2024, 9, 23), LocalDate.of(2024, 10, 23)),
                    repayment(1, LocalDate.of(2024, 10, 23), LocalDate.of(2024, 11, 23)));

            final BigDecimal interestRate = BigDecimal.valueOf(12.0);
            final Integer installmentAmountInMultiplesOf = null;

            Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
            Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.ACTUAL.getValue());
            Mockito.when(loanProductRelatedDetail.getDaysInYearCustomStrategy())
                    .thenReturn(DaysInYearCustomStrategyType.FEB_29_PERIOD_ONLY);
            Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.ACTUAL.getValue());
            Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
            Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
            Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

            final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                    expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

            final Money disbursedAmount = toMoney(15000.0);
            emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 7, 23), disbursedAmount);

            checkPeriod(interestSchedule, 0, 3845.41, 152.88, 3692.53, 11307.47, false);
            checkPeriod(interestSchedule, 1, 3845.41, 115.24, 3730.17, 7577.30, false);
            checkPeriod(interestSchedule, 2, 3845.41, 74.74, 3770.67, 3806.63, false);
            checkPeriod(interestSchedule, 3, 3845.43, 38.80, 3806.63, 0.00, false);
        }

        /**
         * February in one period
         */
        @Test
        public void test_leap_year_only_actual_for_loan_S3() {
            final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                    repayment(1, LocalDate.of(2023, 10, 31), LocalDate.of(2023, 11, 30)),
                    repayment(1, LocalDate.of(2023, 11, 30), LocalDate.of(2023, 12, 31)),
                    repayment(1, LocalDate.of(2023, 12, 31), LocalDate.of(2024, 1, 31)),
                    repayment(1, LocalDate.of(2024, 1, 31), LocalDate.of(2024, 2, 29)),
                    repayment(1, LocalDate.of(2024, 2, 29), LocalDate.of(2024, 3, 31)),
                    repayment(1, LocalDate.of(2024, 3, 31), LocalDate.of(2024, 4, 30)));

            final BigDecimal interestRate = BigDecimal.valueOf(45.00);
            final Integer installmentAmountInMultiplesOf = null;

            Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
            Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.ACTUAL.getValue());
            Mockito.when(loanProductRelatedDetail.getDaysInYearCustomStrategy())
                    .thenReturn(DaysInYearCustomStrategyType.FEB_29_PERIOD_ONLY);
            Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.ACTUAL.getValue());
            Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
            Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
            Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

            final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                    expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

            final Money disbursedAmount = toMoney(245000.0);
            emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2023, 10, 31), disbursedAmount);

            checkPeriod(interestSchedule, 0, 46348.39, 9061.64, 37286.75, 207713.25, false);
            checkPeriod(interestSchedule, 1, 46348.39, 7938.63, 38409.76, 169303.49, false);
            checkPeriod(interestSchedule, 2, 46348.39, 6470.64, 39877.75, 129425.74, false);
            checkPeriod(interestSchedule, 3, 46348.39, 4614.77, 41733.62, 87692.12, false);
            checkPeriod(interestSchedule, 4, 46348.39, 3351.52, 42996.87, 44695.25, false);
            checkPeriod(interestSchedule, 5, 46348.36, 1653.11, 44695.25, 0.00, false);

        }

        /**
         * No Feb month - leap and non leap year split
         */
        @Test
        public void test_leap_year_only_actual_for_loan_S4() {
            final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                    repayment(1, LocalDate.of(2024, 10, 31), LocalDate.of(2024, 11, 30)),
                    repayment(1, LocalDate.of(2024, 11, 30), LocalDate.of(2024, 12, 31)),
                    repayment(1, LocalDate.of(2024, 12, 31), LocalDate.of(2025, 1, 31)),
                    repayment(1, LocalDate.of(2025, 1, 31), LocalDate.of(2025, 2, 28)),
                    repayment(1, LocalDate.of(2025, 2, 28), LocalDate.of(2025, 3, 31)),
                    repayment(1, LocalDate.of(2025, 3, 31), LocalDate.of(2025, 4, 30)));

            final BigDecimal interestRate = BigDecimal.valueOf(9.99);
            final Integer installmentAmountInMultiplesOf = null;

            Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
            Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.ACTUAL.getValue());
            Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.ACTUAL.getValue());
            Mockito.when(loanProductRelatedDetail.getDaysInYearCustomStrategy())
                    .thenReturn(DaysInYearCustomStrategyType.FEB_29_PERIOD_ONLY);
            Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
            Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
            Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

            final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                    expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

            final Money disbursedAmount = toMoney(2450);
            emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 10, 31), disbursedAmount);

            checkPeriod(interestSchedule, 0, 420.24, 20.12, 400.12, 2049.88, false);
            checkPeriod(interestSchedule, 1, 420.24, 17.39, 402.85, 1647.03, false);
            checkPeriod(interestSchedule, 2, 420.24, 13.97, 406.27, 1240.76, false);
            checkPeriod(interestSchedule, 3, 420.24, 9.51, 410.73, 830.03, false);
            checkPeriod(interestSchedule, 4, 420.24, 7.04, 413.20, 416.83, false);
            checkPeriod(interestSchedule, 5, 420.25, 3.42, 416.83, 0.00, false);
        }

        /**
         * no leap year
         */
        @Test
        public void test_leap_year_only_actual_for_loan_S5() {
            final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                    repayment(1, LocalDate.of(2022, 10, 29), LocalDate.of(2022, 12, 29)),
                    repayment(1, LocalDate.of(2022, 12, 29), LocalDate.of(2023, 2, 28)),
                    repayment(1, LocalDate.of(2023, 2, 28), LocalDate.of(2023, 4, 29)),
                    repayment(1, LocalDate.of(2023, 4, 29), LocalDate.of(2023, 6, 29)),
                    repayment(1, LocalDate.of(2023, 6, 29), LocalDate.of(2023, 8, 29)),
                    repayment(1, LocalDate.of(2023, 8, 29), LocalDate.of(2023, 10, 29)));

            final BigDecimal interestRate = BigDecimal.valueOf(7.00);
            final Integer installmentAmountInMultiplesOf = null;

            Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
            Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.ACTUAL.getValue());
            Mockito.when(loanProductRelatedDetail.getDaysInYearCustomStrategy())
                    .thenReturn(DaysInYearCustomStrategyType.FEB_29_PERIOD_ONLY);
            Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.ACTUAL.getValue());
            Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
            Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(2);
            Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

            final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                    expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

            final Money disbursedAmount = toMoney(5000.0);
            emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2022, 10, 29), disbursedAmount);

            checkPeriod(interestSchedule, 0, 867.68, 58.49, 809.19, 4190.81, false);
            checkPeriod(interestSchedule, 1, 867.68, 49.03, 818.65, 3372.16, false);
            checkPeriod(interestSchedule, 2, 867.68, 38.80, 828.88, 2543.28, false);
            checkPeriod(interestSchedule, 3, 867.68, 29.75, 837.93, 1705.35, false);
            checkPeriod(interestSchedule, 4, 867.68, 19.95, 847.73, 857.62, false);
            checkPeriod(interestSchedule, 5, 867.65, 10.03, 857.62, 0.00, false);
        }

        @Test
        public void test_leap_year_only_actual_no_effect_on_360_loan() {
            final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                    repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)),
                    repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)),
                    repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)),
                    repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)),
                    repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)),
                    repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

            final BigDecimal interestRate = BigDecimal.valueOf(7.0);
            final Integer installmentAmountInMultiplesOf = null;

            Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
            Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
            Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
            Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
            Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
            Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);
            Mockito.when(loanProductRelatedDetail.getDaysInYearCustomStrategy())
                    .thenReturn(DaysInYearCustomStrategyType.FEB_29_PERIOD_ONLY);

            final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                    expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

            final Money disbursedAmount = toMoney(100.0);
            emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

            checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.58, 16.43, 83.57);
            checkPeriod(interestSchedule, 0, 1, 17.01, 0.005833333333, 0.5833333333, 0.58, 16.43, 83.57);
            checkPeriod(interestSchedule, 1, 0, 17.01, 0.005833333333, 0.487491666639, 0.49, 16.52, 67.05);
            checkPeriod(interestSchedule, 2, 0, 17.01, 0.005833333333, 0.391124999979, 0.39, 16.62, 50.43);
            checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.294174999983, 0.29, 16.72, 33.71);
            checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.196641666655, 0.2, 16.81, 16.90);
            checkPeriod(interestSchedule, 5, 0, 17.00, 0.005833333333, 0.0985833333276, 0.1, 16.9, 0.0);
        }
    }

    @Test
    public void test_s5_chargeback_in_period_Amt100_dayInYears360_daysInMonth30_repayEvery1Month() {
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)),
                repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)),
                repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)),
                repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)),
                repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)),
                repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(7.0);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        emiCalculator.addDisbursement(interestSchedule, LocalDate.of(2024, 1, 1), disbursedAmount);

        checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 0, 1, 17.01, 0.005833333333, 0.5833333333, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 1, 0, 17.01, 0.005833333333, 0.487491666639, 0.49, 16.52, 67.05);
        checkPeriod(interestSchedule, 2, 0, 17.01, 0.005833333333, 0.391124999979, 0.39, 16.62, 50.43);
        checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.294174999983, 0.29, 16.72, 33.71);
        checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.196641666655, 0.2, 16.81, 16.90);
        checkPeriod(interestSchedule, 5, 0, 17.00, 0.005833333333, 0.0985833333276, 0.1, 16.9, 0.0);

        // repay 1st period
        LocalDate txnDate = LocalDate.of(2024, 2, 1);
        emiCalculator.payPrincipal(interestSchedule, txnDate, txnDate, toMoney(16.43));
        emiCalculator.payInterest(interestSchedule, txnDate, txnDate, toMoney(0.58));

        // repay 2nd period
        txnDate = LocalDate.of(2024, 3, 1);
        emiCalculator.payPrincipal(interestSchedule, txnDate, txnDate, toMoney(16.52));
        emiCalculator.payInterest(interestSchedule, txnDate, txnDate, toMoney(0.49));

        // chargeback
        txnDate = LocalDate.of(2024, 3, 15);
        emiCalculator.creditPrincipal(interestSchedule, txnDate, toMoney(16.52));
        emiCalculator.creditInterest(interestSchedule, txnDate, toMoney(0.49));

        PeriodDueDetails dueAmounts = emiCalculator.getDueAmounts(interestSchedule, LocalDate.of(2024, 4, 1), txnDate);
        Assertions.assertEquals(33.09, toDouble(interestSchedule.repaymentPeriods().get(2).getDuePrincipal()));
        Assertions.assertEquals(33.35, toDouble(dueAmounts.getDuePrincipal()));
        Assertions.assertEquals(0.67, toDouble(dueAmounts.getDueInterest()));

        checkPeriod(interestSchedule, 0, 0, 17.01, 0.0, 0.0, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 0, 1, 17.01, 0.005833333333, 0.5833333333, 0.58, 16.43, 83.57);
        checkPeriod(interestSchedule, 1, 0, 17.01, 0.005833333333, 0.487491666639, 0.49, 16.52, 67.05);
        checkPeriod(interestSchedule, 2, 0, 17.01, 0.002634408602, 0.176637096765, 0.93, 33.09, 50.48);
        checkPeriod(interestSchedule, 2, 1, 17.01, 0.003198924731, 0.75733413977, 0.93, 33.09, 50.48);
        checkPeriod(interestSchedule, 3, 0, 17.01, 0.005833333333, 0.29446666665, 0.29, 16.72, 33.76);
        checkPeriod(interestSchedule, 4, 0, 17.01, 0.005833333333, 0.196933333322, 0.20, 16.81, 16.95);
        checkPeriod(interestSchedule, 5, 0, 17.05, 0.005833333333, 0.0988749999945, 0.10, 16.95, 0.0);
    }

    @Test
    public void test_interest_schedule_model_service_serialization() {
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = List.of(
                repayment(1, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1)),
                repayment(2, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 1)),
                repayment(3, LocalDate.of(2024, 3, 1), LocalDate.of(2024, 4, 1)),
                repayment(4, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 1)),
                repayment(5, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 1)),
                repayment(6, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 7, 1)));

        final BigDecimal interestRate = BigDecimal.valueOf(7.0);
        final Integer installmentAmountInMultiplesOf = null;

        Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
        Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
        Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
        Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);

        final ProgressiveLoanInterestScheduleModel interestScheduleExpected = emiCalculator.generatePeriodInterestScheduleModel(
                expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

        final Money disbursedAmount = toMoney(100.0);
        ProgressiveLoanInterestScheduleModel interestScheduleActual = copyJson(interestScheduleExpected);
        verifyAllPeriods(interestScheduleExpected, interestScheduleActual);
        emiCalculator.addDisbursement(interestScheduleExpected, LocalDate.of(2024, 1, 1), disbursedAmount);
        emiCalculator.addDisbursement(interestScheduleActual, LocalDate.of(2024, 1, 1), disbursedAmount);
        verifyAllPeriods(interestScheduleExpected, interestScheduleActual);
        interestScheduleActual = copyJson(interestScheduleExpected);
        verifyAllPeriods(interestScheduleExpected, interestScheduleActual);

        // repay 1st period
        LocalDate txnDate = LocalDate.of(2024, 2, 1);
        emiCalculator.payPrincipal(interestScheduleExpected, txnDate, txnDate, toMoney(16.43));
        emiCalculator.payInterest(interestScheduleExpected, txnDate, txnDate, toMoney(0.58));
        emiCalculator.payPrincipal(interestScheduleActual, txnDate, txnDate, toMoney(16.43));
        emiCalculator.payInterest(interestScheduleActual, txnDate, txnDate, toMoney(0.58));

        verifyAllPeriods(interestScheduleExpected, interestScheduleActual);
        interestScheduleActual = copyJson(interestScheduleExpected);
        verifyAllPeriods(interestScheduleExpected, interestScheduleActual);

        // repay 2nd period
        txnDate = LocalDate.of(2024, 3, 1);
        emiCalculator.payPrincipal(interestScheduleExpected, txnDate, txnDate, toMoney(16.52));
        emiCalculator.payInterest(interestScheduleExpected, txnDate, txnDate, toMoney(0.49));
        emiCalculator.payPrincipal(interestScheduleActual, txnDate, txnDate, toMoney(16.52));
        emiCalculator.payInterest(interestScheduleActual, txnDate, txnDate, toMoney(0.49));

        verifyAllPeriods(interestScheduleExpected, interestScheduleActual);
        interestScheduleActual = copyJson(interestScheduleExpected);
        verifyAllPeriods(interestScheduleExpected, interestScheduleActual);

        // chargeback
        txnDate = LocalDate.of(2024, 3, 15);
        emiCalculator.creditPrincipal(interestScheduleExpected, txnDate, toMoney(16.52));
        emiCalculator.creditInterest(interestScheduleExpected, txnDate, toMoney(0.49));
        emiCalculator.creditPrincipal(interestScheduleActual, txnDate, toMoney(16.52));
        emiCalculator.creditInterest(interestScheduleActual, txnDate, toMoney(0.49));

        verifyAllPeriods(interestScheduleExpected, interestScheduleActual);
        interestScheduleActual = copyJson(interestScheduleExpected);
        verifyAllPeriods(interestScheduleExpected, interestScheduleActual);

        PeriodDueDetails dueAmountsExpected = emiCalculator.getDueAmounts(interestScheduleExpected, LocalDate.of(2024, 4, 1), txnDate);
        PeriodDueDetails dueAmountsActual = emiCalculator.getDueAmounts(interestScheduleActual, LocalDate.of(2024, 4, 1), txnDate);

        Assertions.assertEquals(toDouble(dueAmountsExpected.getDuePrincipal()), toDouble(dueAmountsActual.getDuePrincipal()));
        Assertions.assertEquals(toDouble(dueAmountsExpected.getDueInterest()), toDouble(dueAmountsActual.getDueInterest()));

    }

    @Nested
    public class InterestTypeFlatAndCalculationPeriodDaily {

        @BeforeEach
        public void setupTestDefaults() {
            Mockito.when(loanProductRelatedDetail.isInterestRecognitionOnDisbursementDate()).thenReturn(false);
            Mockito.when(loanProductRelatedDetail.getDaysInYearCustomStrategy()).thenReturn(null);
            Mockito.when(loanProductRelatedDetail.getInterestMethod()).thenReturn(InterestMethod.FLAT);
            Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);
            Mockito.when(loanProductRelatedDetail.getInterestCalculationPeriodMethod()).thenReturn(InterestCalculationPeriodMethod.DAILY);
            Mockito.when(loanProductRelatedDetail.isAllowPartialPeriodInterestCalculation()).thenReturn(true);

        }

        @Test
        public void testFlatDaily_1_Month_Actual_Actual() {

            final BigDecimal interestRate = BigDecimal.valueOf(12.0);
            final LocalDate disbursementDate = LocalDate.of(2024, 1, 1);

            Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
            Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
            Mockito.when(loanProductRelatedDetail.getNumberOfRepayments()).thenReturn(4);
            Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);

            Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.ACTUAL.getValue());
            Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.ACTUAL.getValue());

            final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = generateExpectedRepaymentPeriods(disbursementDate);
            final Integer installmentAmountInMultiplesOf = null;

            final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                    expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

            emiCalculator.addDisbursement(interestSchedule, disbursementDate, toMoney(700.0));
            checkPeriod(interestSchedule, 0, 181.94, 7.11, 174.83, 525.17, false); //
            checkPeriod(interestSchedule, 1, 181.94, 6.66, 175.28, 349.89, false); //
            checkPeriod(interestSchedule, 2, 181.94, 7.11, 174.83, 175.06, false); //
            checkPeriod(interestSchedule, 3, 181.95, 6.89, 175.06, 0.00, false); //

            emiCalculator.addDisbursement(interestSchedule, disbursementDate, toMoney(300.0));
            checkPeriod(interestSchedule, 0, 259.92, 10.16, 249.76, 750.24, false); //
            checkPeriod(interestSchedule, 1, 259.92, 9.51, 250.41, 499.83, false); //
            checkPeriod(interestSchedule, 2, 259.92, 10.16, 249.76, 250.07, false); //
            checkPeriod(interestSchedule, 3, 259.91, 9.84, 250.07, 0.00, false); //

            emiCalculator.addDisbursement(interestSchedule, disbursementDate, toMoney(1000.0));
            checkPeriod(interestSchedule, 0, 519.84, 20.33, 499.51, 1500.49, false); //
            checkPeriod(interestSchedule, 1, 519.84, 19.02, 500.82, 999.67, false); //
            checkPeriod(interestSchedule, 2, 519.84, 20.33, 499.51, 500.16, false); //
            checkPeriod(interestSchedule, 3, 519.83, 19.67, 500.16, 0.00, false); //
        }

        @Test
        public void testFlatDaily_1_Month_360_30() {

            final BigDecimal interestRate = BigDecimal.valueOf(12.0);
            final LocalDate disbursementDate = LocalDate.of(2024, 1, 1);

            Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
            Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
            Mockito.when(loanProductRelatedDetail.getNumberOfRepayments()).thenReturn(4);
            Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);

            Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
            Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());

            final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = generateExpectedRepaymentPeriods(disbursementDate);
            final Integer installmentAmountInMultiplesOf = null;

            final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                    expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

            emiCalculator.addDisbursement(interestSchedule, disbursementDate, toMoney(750.0));
            checkPeriod(interestSchedule, 0, 195.00, 7.50, 187.50, 562.50, false); //
            checkPeriod(interestSchedule, 1, 195.00, 7.50, 187.50, 375.00, false); //
            checkPeriod(interestSchedule, 2, 195.00, 7.50, 187.50, 187.50, false); //
            checkPeriod(interestSchedule, 3, 195.00, 7.50, 187.50, 0.00, false); //

            emiCalculator.addDisbursement(interestSchedule, disbursementDate, toMoney(250.0));
            checkPeriod(interestSchedule, 0, 260.00, 10.00, 250.00, 750.00, false); //
            checkPeriod(interestSchedule, 1, 260.00, 10.00, 250.00, 500.00, false); //
            checkPeriod(interestSchedule, 2, 260.00, 10.00, 250.00, 250.00, false); //
            checkPeriod(interestSchedule, 3, 260.00, 10.00, 250.00, 0.00, false); //

            emiCalculator.addDisbursement(interestSchedule, disbursementDate.plusMonths(1).plusDays(5), toMoney(250.0));
            checkPeriod(interestSchedule, 0, 260.00, 10.00, 250.00, 750.00, false); //
            checkPeriod(interestSchedule, 1, 345.69, 12.07, 333.62, 666.38, false); //
            checkPeriod(interestSchedule, 2, 345.69, 12.50, 333.19, 333.19, false); //
            checkPeriod(interestSchedule, 3, 345.69, 12.50, 333.19, 0.00, false); //
        }

        @Test
        public void testFlatDaily_1_week_Actual_Actual() {

            final BigDecimal interestRate = BigDecimal.valueOf(12.0);
            final LocalDate disbursementDate = LocalDate.of(2024, 1, 1);

            Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
            Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.WEEKS);
            Mockito.when(loanProductRelatedDetail.getNumberOfRepayments()).thenReturn(4);
            Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);

            Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.ACTUAL.getValue());
            Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.ACTUAL.getValue());

            final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = generateExpectedRepaymentPeriods(disbursementDate);
            final Integer installmentAmountInMultiplesOf = null;

            final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                    expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

            emiCalculator.addDisbursement(interestSchedule, disbursementDate, toMoney(600.0));
            checkPeriod(interestSchedule, 0, 151.38, 1.38, 150.00, 450.00, false); //
            checkPeriod(interestSchedule, 1, 151.38, 1.38, 150.00, 300.00, false); //
            checkPeriod(interestSchedule, 2, 151.38, 1.38, 150.00, 150.00, false); //
            checkPeriod(interestSchedule, 3, 151.38, 1.38, 150.00, 0.00, false); //

            emiCalculator.addDisbursement(interestSchedule, disbursementDate, toMoney(400.0));
            checkPeriod(interestSchedule, 0, 252.30, 2.30, 250.00, 750.00, false); //
            checkPeriod(interestSchedule, 1, 252.30, 2.30, 250.00, 500.00, false); //
            checkPeriod(interestSchedule, 2, 252.30, 2.30, 250.00, 250.00, false); //
            checkPeriod(interestSchedule, 3, 252.30, 2.30, 250.00, 0.00, false); //

            emiCalculator.addDisbursement(interestSchedule, disbursementDate.plusDays(3), toMoney(1000.0));
            checkPeriod(interestSchedule, 0, 504.34, 3.61, 500.73, 1499.27, false); //
            checkPeriod(interestSchedule, 1, 504.34, 4.59, 499.75, 999.52, false); //
            checkPeriod(interestSchedule, 2, 504.34, 4.59, 499.75, 499.77, false); //
            checkPeriod(interestSchedule, 3, 504.36, 4.59, 499.77, 0.00, false); //
        }

        @Test
        public void testFlatDaily_30_Days_Actual_Actual() {

            final BigDecimal interestRate = BigDecimal.valueOf(12.0);
            final LocalDate disbursementDate = LocalDate.of(2024, 1, 1);

            Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
            Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.DAYS);
            Mockito.when(loanProductRelatedDetail.getNumberOfRepayments()).thenReturn(4);
            Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(30);

            Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.ACTUAL.getValue());
            Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.ACTUAL.getValue());

            final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = generateExpectedRepaymentPeriods(disbursementDate);
            final Integer installmentAmountInMultiplesOf = null;

            final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                    expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

            emiCalculator.addDisbursement(interestSchedule, disbursementDate, toMoney(400.0));
            checkPeriod(interestSchedule, 0, 103.93, 3.93, 100.00, 300.00, false); //
            checkPeriod(interestSchedule, 1, 103.93, 3.93, 100.00, 200.00, false); //
            checkPeriod(interestSchedule, 2, 103.93, 3.93, 100.00, 100.00, false); //
            checkPeriod(interestSchedule, 3, 103.93, 3.93, 100.00, 0.00, false); //

            emiCalculator.addDisbursement(interestSchedule, disbursementDate, toMoney(600.0));
            checkPeriod(interestSchedule, 0, 259.84, 9.84, 250.00, 750.00, false); //
            checkPeriod(interestSchedule, 1, 259.84, 9.84, 250.00, 500.00, false); //
            checkPeriod(interestSchedule, 2, 259.84, 9.84, 250.00, 250.00, false); //
            checkPeriod(interestSchedule, 3, 259.84, 9.84, 250.00, 0.00, false); //

            emiCalculator.addDisbursement(interestSchedule, disbursementDate.plusDays(15), toMoney(500.0));
            checkPeriod(interestSchedule, 0, 389.14, 12.30, 376.84, 1123.16, false); //
            checkPeriod(interestSchedule, 1, 389.14, 14.75, 374.39, 748.77, false); //
            checkPeriod(interestSchedule, 2, 389.14, 14.75, 374.39, 374.38, false); //
            checkPeriod(interestSchedule, 3, 389.13, 14.75, 374.38, 0.00, false); //
        }
    }

    @Nested
    public class InterestTypeFlatAndCalculationPeriodSameAsRepaymentPeriod {

        @BeforeEach
        public void setupTestDefaults() {
            Mockito.when(loanProductRelatedDetail.isInterestRecognitionOnDisbursementDate()).thenReturn(false);
            Mockito.when(loanProductRelatedDetail.getDaysInYearCustomStrategy()).thenReturn(null);
            Mockito.when(loanProductRelatedDetail.getInterestMethod()).thenReturn(InterestMethod.FLAT);
            Mockito.when(loanProductRelatedDetail.getCurrencyData()).thenReturn(currency);
            Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.ACTUAL.getValue());
            Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.ACTUAL.getValue());
            Mockito.when(loanProductRelatedDetail.getInterestCalculationPeriodMethod())
                    .thenReturn(InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD);
            Mockito.when(loanProductRelatedDetail.isAllowPartialPeriodInterestCalculation()).thenReturn(true);

        }

        @Test
        void test_sameAsRepayment_days_repay_every_6_periods_5() {

            final BigDecimal interestRate = BigDecimal.valueOf(12.2);
            final LocalDate disbursementDate = LocalDate.of(2024, 1, 1);

            Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
            Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.DAYS);
            Mockito.when(loanProductRelatedDetail.getNumberOfRepayments()).thenReturn(5);
            Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(6);

            final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = generateExpectedRepaymentPeriods(disbursementDate);
            final Integer installmentAmountInMultiplesOf = null;

            final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                    expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

            emiCalculator.addDisbursement(interestSchedule, disbursementDate, toMoney(1000.0));

            checkPeriod(interestSchedule, 0, 202.00, 2.00, 200.00, 800.00, false); //
            checkPeriod(interestSchedule, 1, 202.00, 2.00, 200.00, 600.00, false); //
            checkPeriod(interestSchedule, 2, 202.00, 2.00, 200.00, 400.00, false); //
            checkPeriod(interestSchedule, 3, 202.00, 2.00, 200.00, 200.00, false); //
            checkPeriod(interestSchedule, 4, 202.00, 2.00, 200.00, 0.00, false); //
        }

        @Test
        void test_sameAsRepayment_week_repay_every_1_periods_10() {

            final BigDecimal interestRate = BigDecimal.valueOf(5.2);
            final LocalDate disbursementDate = LocalDate.of(2024, 1, 1);

            final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = expectedRepaymentWeeks(disbursementDate, 10, 1);
            final Integer installmentAmountInMultiplesOf = null;

            Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
            Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_364.getValue());
            Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.ACTUAL.getValue());
            Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.WEEKS);
            Mockito.when(loanProductRelatedDetail.getInterestCalculationPeriodMethod())
                    .thenReturn(InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD);
            Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);

            final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                    expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

            emiCalculator.addDisbursement(interestSchedule, disbursementDate, toMoney(1000.0));

            checkPeriod(interestSchedule, 0, 101.00, 1.00, 100.00, 900.00, false); //
            checkPeriod(interestSchedule, 1, 101.00, 1.00, 100.00, 800.00, false); //
            checkPeriod(interestSchedule, 2, 101.00, 1.00, 100.00, 700.00, false); //
            checkPeriod(interestSchedule, 3, 101.00, 1.00, 100.00, 600.00, false); //
            checkPeriod(interestSchedule, 4, 101.00, 1.00, 100.00, 500.00, false); //
            checkPeriod(interestSchedule, 5, 101.00, 1.00, 100.00, 400.00, false); //
            checkPeriod(interestSchedule, 6, 101.00, 1.00, 100.00, 300.00, false); //
            checkPeriod(interestSchedule, 7, 101.00, 1.00, 100.00, 200.00, false); //
            checkPeriod(interestSchedule, 8, 101.00, 1.00, 100.00, 100.00, false); //
            checkPeriod(interestSchedule, 9, 101.00, 1.00, 100.00, 0.00, false); //
        }

        @Test
        void test_sameAsRepayment_month_repay_every_2_periods_8() {

            final BigDecimal interestRate = BigDecimal.valueOf(12.0);
            final LocalDate disbursementDate = LocalDate.of(2024, 1, 1);

            Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
            Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
            Mockito.when(loanProductRelatedDetail.getNumberOfRepayments()).thenReturn(8);
            Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(2);

            final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = generateExpectedRepaymentPeriods(disbursementDate);
            final Integer installmentAmountInMultiplesOf = null;

            final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                    expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

            emiCalculator.addDisbursement(interestSchedule, disbursementDate, toMoney(1000.0));

            checkPeriod(interestSchedule, 0, 145.00, 20.00, 125.00, 875.00, false); //
            checkPeriod(interestSchedule, 1, 145.00, 20.00, 125.00, 750.00, false); //
            checkPeriod(interestSchedule, 2, 145.00, 20.00, 125.00, 625.00, false); //
            checkPeriod(interestSchedule, 3, 145.00, 20.00, 125.00, 500.00, false); //
            checkPeriod(interestSchedule, 4, 145.00, 20.00, 125.00, 375.00, false); //
            checkPeriod(interestSchedule, 5, 145.00, 20.00, 125.00, 250.00, false); //
            checkPeriod(interestSchedule, 6, 145.00, 20.00, 125.00, 125.00, false); //
            checkPeriod(interestSchedule, 7, 145.00, 20.00, 125.00, 0.00, false); //
        }

        @Test
        void test_sameAsRepayment_month_repay_every_1_periods_3_second_disbursement_on_repayment_due_date_allow_partial() {

            final BigDecimal interestRate = BigDecimal.valueOf(12.0);
            final LocalDate disbursementDate = LocalDate.of(2024, 1, 1);

            Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
            Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
            Mockito.when(loanProductRelatedDetail.getNumberOfRepayments()).thenReturn(3);
            Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);

            Mockito.when(loanProductRelatedDetail.isAllowPartialPeriodInterestCalculation()).thenReturn(true);

            final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = generateExpectedRepaymentPeriods(disbursementDate);

            final Integer installmentAmountInMultiplesOf = null;

            final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                    expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

            emiCalculator.addDisbursement(interestSchedule, disbursementDate, toMoney(1000.0));

            checkPeriod(interestSchedule, 0, 343.33, 10.00, 333.33, 666.67, false); //
            checkPeriod(interestSchedule, 1, 343.33, 10.00, 333.33, 333.34, false); //
            checkPeriod(interestSchedule, 2, 343.34, 10.00, 333.34, 0.00, false); //

            emiCalculator.addDisbursement(interestSchedule, disbursementDate.plusMonths(1), toMoney(250.0));

            checkPeriod(interestSchedule, 0, 343.33, 10.00, 333.33, 916.67, false); //
            checkPeriod(interestSchedule, 1, 470.84, 12.50, 458.34, 458.33, false); //
            checkPeriod(interestSchedule, 2, 470.83, 12.50, 458.33, 0.00, false); //
        }

        @Test
        void test_sameAsRepayment_month_repay_every_1_periods_3_second_disbursement_on_repayment_due_date_disallow_partial() {

            final BigDecimal interestRate = BigDecimal.valueOf(12.0);
            final LocalDate disbursementDate = LocalDate.of(2024, 1, 1);

            Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
            Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
            Mockito.when(loanProductRelatedDetail.getNumberOfRepayments()).thenReturn(3);
            Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);

            Mockito.when(loanProductRelatedDetail.isAllowPartialPeriodInterestCalculation()).thenReturn(false);

            final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = generateExpectedRepaymentPeriods(disbursementDate);

            final Integer installmentAmountInMultiplesOf = null;

            final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                    expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

            emiCalculator.addDisbursement(interestSchedule, disbursementDate, toMoney(1000.0));

            checkPeriod(interestSchedule, 0, 343.33, 10.00, 333.33, 666.67, false); //
            checkPeriod(interestSchedule, 1, 343.33, 10.00, 333.33, 333.34, false); //
            checkPeriod(interestSchedule, 2, 343.34, 10.00, 333.34, 0.00, false); //

            emiCalculator.addDisbursement(interestSchedule, disbursementDate.plusMonths(1), toMoney(250.0));

            checkPeriod(interestSchedule, 0, 343.33, 10.00, 333.33, 916.67, false); //
            checkPeriod(interestSchedule, 1, 470.84, 12.50, 458.34, 458.33, false); //
            checkPeriod(interestSchedule, 2, 470.83, 12.50, 458.33, 0.00, false); //
        }

        @Test
        void test_sameAsRepayment_month_repay_every_1_periods_3() {

            final BigDecimal interestRate = BigDecimal.valueOf(12.0);
            final LocalDate disbursementDate = LocalDate.of(2024, 1, 1);

            Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
            Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
            Mockito.when(loanProductRelatedDetail.getNumberOfRepayments()).thenReturn(3);
            Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);

            final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = generateExpectedRepaymentPeriods(disbursementDate);

            final Integer installmentAmountInMultiplesOf = null;

            final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                    expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

            emiCalculator.addDisbursement(interestSchedule, disbursementDate, toMoney(1000.0));

            checkPeriod(interestSchedule, 0, 343.33, 10.00, 333.33, 666.67, false); //
            checkPeriod(interestSchedule, 1, 343.33, 10.00, 333.33, 333.34, false); //
            checkPeriod(interestSchedule, 2, 343.34, 10.00, 333.34, 0.00, false); //

            emiCalculator.addDisbursement(interestSchedule, disbursementDate.plusDays(4), toMoney(250.0));

            checkPeriod(interestSchedule, 0, 429.06, 12.18, 416.88, 833.12, false); //
            checkPeriod(interestSchedule, 1, 429.06, 12.50, 416.56, 416.56, false); //
            checkPeriod(interestSchedule, 2, 429.06, 12.50, 416.56, 0.00, false); //

            emiCalculator.addDisbursement(interestSchedule, disbursementDate.plusDays(4), toMoney(250.0));

            checkPeriod(interestSchedule, 0, 514.78, 14.35, 500.43, 999.57, false); //
            checkPeriod(interestSchedule, 1, 514.78, 15.00, 499.78, 499.79, false); //
            checkPeriod(interestSchedule, 2, 514.79, 15.00, 499.79, 0.00, false); //

        }

        @Test
        void test_sameAsRepayment_month_repay_every_1_periods_20() {

            final BigDecimal interestRate = BigDecimal.valueOf(12.0);
            final LocalDate disbursementDate = LocalDate.of(2024, 1, 1);

            Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
            Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
            Mockito.when(loanProductRelatedDetail.getNumberOfRepayments()).thenReturn(20);
            Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);

            final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = generateExpectedRepaymentPeriods(disbursementDate);
            final Integer installmentAmountInMultiplesOf = null;

            final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                    expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

            emiCalculator.addDisbursement(interestSchedule, disbursementDate, toMoney(1000.0));

            checkPeriod(interestSchedule, 0, 60.00, 10.00, 50.00, 950.00, false); //
            checkPeriod(interestSchedule, 1, 60.00, 10.00, 50.00, 900.00, false); //
            checkPeriod(interestSchedule, 2, 60.00, 10.00, 50.00, 850.00, false); //
            checkPeriod(interestSchedule, 3, 60.00, 10.00, 50.00, 800.00, false); //
            checkPeriod(interestSchedule, 4, 60.00, 10.00, 50.00, 750.00, false); //
            checkPeriod(interestSchedule, 5, 60.00, 10.00, 50.00, 700.00, false); //
            checkPeriod(interestSchedule, 6, 60.00, 10.00, 50.00, 650.00, false); //
            checkPeriod(interestSchedule, 7, 60.00, 10.00, 50.00, 600.00, false); //
            checkPeriod(interestSchedule, 8, 60.00, 10.00, 50.00, 550.00, false); //
            checkPeriod(interestSchedule, 9, 60.00, 10.00, 50.00, 500.00, false); //
            checkPeriod(interestSchedule, 10, 60.00, 10.00, 50.00, 450.00, false); //
            checkPeriod(interestSchedule, 11, 60.00, 10.00, 50.00, 400.00, false); //
            checkPeriod(interestSchedule, 12, 60.00, 10.00, 50.00, 350.00, false); //
            checkPeriod(interestSchedule, 13, 60.00, 10.00, 50.00, 300.00, false); //
            checkPeriod(interestSchedule, 14, 60.00, 10.00, 50.00, 250.00, false); //
            checkPeriod(interestSchedule, 15, 60.00, 10.00, 50.00, 200.00, false); //
            checkPeriod(interestSchedule, 16, 60.00, 10.00, 50.00, 150.00, false); //
            checkPeriod(interestSchedule, 17, 60.00, 10.00, 50.00, 100.00, false); //
            checkPeriod(interestSchedule, 18, 60.00, 10.00, 50.00, 50.00, false); //
            checkPeriod(interestSchedule, 19, 60.00, 10.00, 50.00, 0.00, false); //
        }

        @Test
        void test_sameAsRepayment_month_repay_every_1_periods_24() {

            final BigDecimal interestRate = BigDecimal.valueOf(12.0);
            final LocalDate disbursementDate = LocalDate.of(2024, 1, 1);

            Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
            Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
            Mockito.when(loanProductRelatedDetail.getNumberOfRepayments()).thenReturn(24);
            Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);

            final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = generateExpectedRepaymentPeriods(disbursementDate);
            final Integer installmentAmountInMultiplesOf = null;

            final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                    expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

            emiCalculator.addDisbursement(interestSchedule, disbursementDate, toMoney(7500.0));
            emiCalculator.addDisbursement(interestSchedule, disbursementDate, toMoney(2500.0));

            checkPeriod(interestSchedule, 0, 516.67, 100.00, 416.67, 9583.33, false); //
            checkPeriod(interestSchedule, 1, 516.67, 100.00, 416.67, 9166.66, false); //
            checkPeriod(interestSchedule, 2, 516.67, 100.00, 416.67, 8749.99, false); //
            checkPeriod(interestSchedule, 3, 516.67, 100.00, 416.67, 8333.32, false); //
            checkPeriod(interestSchedule, 4, 516.67, 100.00, 416.67, 7916.65, false); //
            checkPeriod(interestSchedule, 5, 516.67, 100.00, 416.67, 7499.98, false); //
            checkPeriod(interestSchedule, 6, 516.67, 100.00, 416.67, 7083.31, false); //
            checkPeriod(interestSchedule, 7, 516.67, 100.00, 416.67, 6666.64, false); //
            checkPeriod(interestSchedule, 8, 516.67, 100.00, 416.67, 6249.97, false); //
            checkPeriod(interestSchedule, 9, 516.67, 100.00, 416.67, 5833.30, false); //
            checkPeriod(interestSchedule, 10, 516.67, 100.00, 416.67, 5416.63, false); //
            checkPeriod(interestSchedule, 11, 516.67, 100.00, 416.67, 4999.96, false); //
            checkPeriod(interestSchedule, 12, 516.67, 100.00, 416.67, 4583.29, false); //
            checkPeriod(interestSchedule, 13, 516.67, 100.00, 416.67, 4166.62, false); //
            checkPeriod(interestSchedule, 14, 516.67, 100.00, 416.67, 3749.95, false); //
            checkPeriod(interestSchedule, 15, 516.67, 100.00, 416.67, 3333.28, false); //
            checkPeriod(interestSchedule, 16, 516.67, 100.00, 416.67, 2916.61, false); //
            checkPeriod(interestSchedule, 17, 516.67, 100.00, 416.67, 2499.94, false); //
            checkPeriod(interestSchedule, 18, 516.67, 100.00, 416.67, 2083.27, false); //
            checkPeriod(interestSchedule, 19, 516.67, 100.00, 416.67, 1666.60, false); //
            checkPeriod(interestSchedule, 20, 516.67, 100.00, 416.67, 1249.93, false); //
            checkPeriod(interestSchedule, 21, 516.67, 100.00, 416.67, 833.26, false); //
            checkPeriod(interestSchedule, 22, 516.67, 100.00, 416.67, 416.59, false); //
            checkPeriod(interestSchedule, 23, 516.59, 100.00, 416.59, 0.00, false); //

            emiCalculator.addDisbursement(interestSchedule, disbursementDate.plusMonths(3).plusDays(4), toMoney(1000.0));

            checkPeriod(interestSchedule, 0, 516.67, 100.00, 416.67, 9583.33, false); //
            checkPeriod(interestSchedule, 1, 516.67, 100.00, 416.67, 9166.66, false); //
            checkPeriod(interestSchedule, 2, 516.67, 100.00, 416.67, 8749.99, false); //
            checkPeriod(interestSchedule, 3, 574.22, 108.67, 465.55, 9284.44, false); //
            checkPeriod(interestSchedule, 4, 574.22, 110.00, 464.22, 8820.22, false); //
            checkPeriod(interestSchedule, 5, 574.22, 110.00, 464.22, 8356.00, false); //
            checkPeriod(interestSchedule, 6, 574.22, 110.00, 464.22, 7891.78, false); //
            checkPeriod(interestSchedule, 7, 574.22, 110.00, 464.22, 7427.56, false); //
            checkPeriod(interestSchedule, 8, 574.22, 110.00, 464.22, 6963.34, false); //
            checkPeriod(interestSchedule, 9, 574.22, 110.00, 464.22, 6499.12, false); //
            checkPeriod(interestSchedule, 10, 574.22, 110.00, 464.22, 6034.90, false); //
            checkPeriod(interestSchedule, 11, 574.22, 110.00, 464.22, 5570.68, false); //
            checkPeriod(interestSchedule, 12, 574.22, 110.00, 464.22, 5106.46, false); //
            checkPeriod(interestSchedule, 13, 574.22, 110.00, 464.22, 4642.24, false); //
            checkPeriod(interestSchedule, 14, 574.22, 110.00, 464.22, 4178.02, false); //
            checkPeriod(interestSchedule, 15, 574.22, 110.00, 464.22, 3713.80, false); //
            checkPeriod(interestSchedule, 16, 574.22, 110.00, 464.22, 3249.58, false); //
            checkPeriod(interestSchedule, 17, 574.22, 110.00, 464.22, 2785.36, false); //
            checkPeriod(interestSchedule, 18, 574.22, 110.00, 464.22, 2321.14, false); //
            checkPeriod(interestSchedule, 19, 574.22, 110.00, 464.22, 1856.92, false); //
            checkPeriod(interestSchedule, 20, 574.22, 110.00, 464.22, 1392.70, false); //
            checkPeriod(interestSchedule, 21, 574.22, 110.00, 464.22, 928.48, false); //
            checkPeriod(interestSchedule, 22, 574.22, 110.00, 464.22, 464.26, false); //
            checkPeriod(interestSchedule, 23, 574.26, 110.00, 464.26, 0.00, false); //
        }

        @Test
        void test_sameAsRepayment_month_repay_every_1_periods_3__not_calculate_exact_days() {

            final BigDecimal interestRate = BigDecimal.valueOf(12.0);
            final LocalDate disbursementDate = LocalDate.of(2024, 1, 1);

            Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
            Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
            Mockito.when(loanProductRelatedDetail.getNumberOfRepayments()).thenReturn(3);
            Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
            Mockito.when(loanProductRelatedDetail.isAllowPartialPeriodInterestCalculation()).thenReturn(false);

            final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = generateExpectedRepaymentPeriods(disbursementDate);

            final Integer installmentAmountInMultiplesOf = null;

            final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                    expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

            emiCalculator.addDisbursement(interestSchedule, disbursementDate, toMoney(1000.0));

            checkPeriod(interestSchedule, 0, 343.33, 10.00, 333.33, 666.67, false); //
            checkPeriod(interestSchedule, 1, 343.33, 10.00, 333.33, 333.34, false); //
            checkPeriod(interestSchedule, 2, 343.34, 10.00, 333.34, 0.00, false); //

            emiCalculator.addDisbursement(interestSchedule, disbursementDate.plusDays(4), toMoney(250.0));
            checkPeriod(interestSchedule, 0, 429.17, 12.50, 416.67, 833.33, false); //
            checkPeriod(interestSchedule, 1, 429.17, 12.50, 416.67, 416.66, false); //
            checkPeriod(interestSchedule, 2, 429.16, 12.50, 416.66, 0.00, false); //

            emiCalculator.addDisbursement(interestSchedule, disbursementDate.plusDays(17), toMoney(250.0));
            checkPeriod(interestSchedule, 0, 515.00, 15.00, 500.00, 1000.00, false); //
            checkPeriod(interestSchedule, 1, 515.00, 15.00, 500.00, 500.00, false); //
            checkPeriod(interestSchedule, 2, 515.00, 15.00, 500.00, 0.00, false); //

        }
    }

    @Nested
    public class DecliningBalanceSameAsRepaymentPeriod {

        @BeforeEach
        void beforeEach() {
            Mockito.when(loanProductRelatedDetail.getInterestMethod()).thenReturn(InterestMethod.DECLINING_BALANCE);
            Mockito.when(loanProductRelatedDetail.getInterestCalculationPeriodMethod())
                    .thenReturn(InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD);
            Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.ACTUAL.getValue());
            Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.ACTUAL.getValue());
        }

        @Test
        void test_3_of_1_month_period_multi_disbursement__exact_days() {

            final BigDecimal interestRate = BigDecimal.valueOf(12.0);
            final LocalDate disbursementDate = LocalDate.of(2024, 6, 1);

            Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
            Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
            Mockito.when(loanProductRelatedDetail.getNumberOfRepayments()).thenReturn(3);
            Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);

            Mockito.when(loanProductRelatedDetail.isAllowPartialPeriodInterestCalculation()).thenReturn(true);

            final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = generateExpectedRepaymentPeriods(disbursementDate);
            final Integer installmentAmountInMultiplesOf = null;

            final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                    expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

            emiCalculator.addDisbursement(interestSchedule, disbursementDate, toMoney(500.0));
            checkPeriod(interestSchedule, 0, 170.01, 5.00, 165.01, 334.99, false); //
            checkPeriod(interestSchedule, 1, 170.01, 3.35, 166.66, 168.33, false); //
            checkPeriod(interestSchedule, 2, 170.01, 1.68, 168.33, 0.00, false); //

            emiCalculator.addDisbursement(interestSchedule, disbursementDate, toMoney(500.0));

            checkPeriod(interestSchedule, 0, 340.02, 10.00, 330.02, 669.98, false); //
            checkPeriod(interestSchedule, 1, 340.02, 6.70, 333.32, 336.66, false); //
            checkPeriod(interestSchedule, 2, 340.03, 3.37, 336.66, 0.00, false); //

            final LocalDate disbursementDate2 = LocalDate.of(2024, 7, 15);
            emiCalculator.addDisbursement(interestSchedule, disbursementDate2, toMoney(250.0));
            checkPeriod(interestSchedule, 0, 340.02, 10.00, 330.02, 669.98, false); //
            checkPeriod(interestSchedule, 1, 466.33, 8.07, 458.26, 461.72, false); //
            checkPeriod(interestSchedule, 2, 466.34, 4.62, 461.72, 0.00, false); //
        }

        @Test
        void test_3_of_1_month_period_multi_disbursement__exact_days__on_period_due_date() {

            final BigDecimal interestRate = BigDecimal.valueOf(12.0);
            final LocalDate disbursementDate = LocalDate.of(2024, 6, 1);

            Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
            Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
            Mockito.when(loanProductRelatedDetail.getNumberOfRepayments()).thenReturn(3);
            Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);

            Mockito.when(loanProductRelatedDetail.isAllowPartialPeriodInterestCalculation()).thenReturn(true);

            final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = generateExpectedRepaymentPeriods(disbursementDate);
            final Integer installmentAmountInMultiplesOf = null;

            final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                    expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

            emiCalculator.addDisbursement(interestSchedule, disbursementDate, toMoney(500.0));
            checkPeriod(interestSchedule, 0, 170.01, 5.00, 165.01, 334.99, false); //
            checkPeriod(interestSchedule, 1, 170.01, 3.35, 166.66, 168.33, false); //
            checkPeriod(interestSchedule, 2, 170.01, 1.68, 168.33, 0.00, false); //

            emiCalculator.addDisbursement(interestSchedule, disbursementDate, toMoney(500.0));
            checkPeriod(interestSchedule, 0, 340.02, 10.00, 330.02, 669.98, false); //
            checkPeriod(interestSchedule, 1, 340.02, 6.70, 333.32, 336.66, false); //
            checkPeriod(interestSchedule, 2, 340.03, 3.37, 336.66, 0.00, false); //

            final LocalDate disbursementDate2 = LocalDate.of(2024, 7, 1);
            emiCalculator.addDisbursement(interestSchedule, disbursementDate2, toMoney(250.0));
            checkPeriod(interestSchedule, 0, 340.02, 10.00, 330.02, 919.98, false); //
            checkPeriod(interestSchedule, 1, 466.90, 9.20, 457.70, 462.28, false); //
            checkPeriod(interestSchedule, 2, 466.90, 4.62, 462.28, 0.00, false); //
        }

        @Test
        void test_3_of_1_month_period_multi_disbursement__NOT_exact_days__different_days() {

            final BigDecimal interestRate = BigDecimal.valueOf(12.0);
            final LocalDate disbursementDate = LocalDate.of(2024, 6, 1);

            Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
            Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
            Mockito.when(loanProductRelatedDetail.getNumberOfRepayments()).thenReturn(3);
            Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);

            Mockito.when(loanProductRelatedDetail.isAllowPartialPeriodInterestCalculation()).thenReturn(false);

            final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = generateExpectedRepaymentPeriods(disbursementDate);
            final Integer installmentAmountInMultiplesOf = null;

            final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                    expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

            emiCalculator.addDisbursement(interestSchedule, disbursementDate, toMoney(500.0));
            checkPeriod(interestSchedule, 0, 170.01, 5.00, 165.01, 334.99, false); //
            checkPeriod(interestSchedule, 1, 170.01, 3.35, 166.66, 168.33, false); //
            checkPeriod(interestSchedule, 2, 170.01, 1.68, 168.33, 0.00, false); //

            emiCalculator.addDisbursement(interestSchedule, disbursementDate, toMoney(500.0));
            checkPeriod(interestSchedule, 0, 340.02, 10.00, 330.02, 669.98, false); //
            checkPeriod(interestSchedule, 1, 340.02, 6.70, 333.32, 336.66, false); //
            checkPeriod(interestSchedule, 2, 340.03, 3.37, 336.66, 0.00, false); //

            final LocalDate disbursementDate2 = LocalDate.of(2024, 7, 1);
            final LocalDate disbursementDate3 = LocalDate.of(2024, 7, 15);
            final LocalDate disbursementDate4 = LocalDate.of(2024, 7, 23);
            emiCalculator.addDisbursement(interestSchedule, disbursementDate2, toMoney(50.0));
            checkPeriod(interestSchedule, 0, 340.02, 10.00, 330.02, 719.98, false); //
            checkPeriod(interestSchedule, 1, 365.40, 7.20, 358.20, 361.78, false); //
            checkPeriod(interestSchedule, 2, 365.40, 3.62, 361.78, 0.00, false); //

            emiCalculator.addDisbursement(interestSchedule, disbursementDate3, toMoney(50.0));
            checkPeriod(interestSchedule, 0, 340.02, 10.00, 330.02, 769.98, false); //
            checkPeriod(interestSchedule, 1, 390.77, 7.70, 383.07, 386.91, false); //
            checkPeriod(interestSchedule, 2, 390.78, 3.87, 386.91, 0.00, false); //

            emiCalculator.addDisbursement(interestSchedule, disbursementDate3, toMoney(50.0));
            checkPeriod(interestSchedule, 0, 340.02, 10.00, 330.02, 819.98, false); //
            checkPeriod(interestSchedule, 1, 416.15, 8.20, 407.95, 412.03, false); //
            checkPeriod(interestSchedule, 2, 416.15, 4.12, 412.03, 0.00, false); //

            emiCalculator.addDisbursement(interestSchedule, disbursementDate4, toMoney(50.0));
            checkPeriod(interestSchedule, 0, 340.02, 10.00, 330.02, 869.98, false); //
            checkPeriod(interestSchedule, 1, 441.53, 8.70, 432.83, 437.15, false); //
            checkPeriod(interestSchedule, 2, 441.52, 4.37, 437.15, 0.00, false); //

            emiCalculator.addDisbursement(interestSchedule, disbursementDate4, toMoney(50.0));
            checkPeriod(interestSchedule, 0, 340.02, 10.00, 330.02, 919.98, false); //
            checkPeriod(interestSchedule, 1, 466.90, 9.20, 457.70, 462.28, false); //
            checkPeriod(interestSchedule, 2, 466.90, 4.62, 462.28, 0.00, false); //
        }

        @Test
        void test_3_of_1_month_period_multi_disbursement__NOT_exact_days() {

            final BigDecimal interestRate = BigDecimal.valueOf(12.0);
            final LocalDate disbursementDate = LocalDate.of(2024, 6, 1);

            Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(interestRate);
            Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
            Mockito.when(loanProductRelatedDetail.getNumberOfRepayments()).thenReturn(3);
            Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);

            Mockito.when(loanProductRelatedDetail.isAllowPartialPeriodInterestCalculation()).thenReturn(false);

            final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = generateExpectedRepaymentPeriods(disbursementDate);
            final Integer installmentAmountInMultiplesOf = null;

            final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                    expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

            emiCalculator.addDisbursement(interestSchedule, disbursementDate, toMoney(500.0));
            checkPeriod(interestSchedule, 0, 170.01, 5.00, 165.01, 334.99, false); //
            checkPeriod(interestSchedule, 1, 170.01, 3.35, 166.66, 168.33, false); //
            checkPeriod(interestSchedule, 2, 170.01, 1.68, 168.33, 0.00, false); //

            emiCalculator.addDisbursement(interestSchedule, disbursementDate, toMoney(500.0));
            checkPeriod(interestSchedule, 0, 340.02, 10.00, 330.02, 669.98, false); //
            checkPeriod(interestSchedule, 1, 340.02, 6.70, 333.32, 336.66, false); //
            checkPeriod(interestSchedule, 2, 340.03, 3.37, 336.66, 0.00, false); //

            final LocalDate disbursementDate2 = LocalDate.of(2024, 7, 15);
            emiCalculator.addDisbursement(interestSchedule, disbursementDate2, toMoney(130.0));
            checkPeriod(interestSchedule, 0, 340.02, 10.00, 330.02, 799.98, false); //
            checkPeriod(interestSchedule, 1, 406.00, 8.00, 398.00, 401.98, false); //
            checkPeriod(interestSchedule, 2, 406.00, 4.02, 401.98, 0.00, false); //

            emiCalculator.addDisbursement(interestSchedule, disbursementDate2, toMoney(120.0));
            checkPeriod(interestSchedule, 0, 340.02, 10.00, 330.02, 919.98, false); //
            checkPeriod(interestSchedule, 1, 466.90, 9.20, 457.70, 462.28, false); //
            checkPeriod(interestSchedule, 2, 466.90, 4.62, 462.28, 0.00, false); //
        }
    }

    @Nested
    public class RescheduleInstallmentExtendRepaymentPeriod {

        @BeforeEach
        void beforeEach() {
            Mockito.when(loanProductRelatedDetail.getInterestMethod()).thenReturn(InterestMethod.DECLINING_BALANCE);
            Mockito.when(loanProductRelatedDetail.getInterestCalculationPeriodMethod()).thenReturn(InterestCalculationPeriodMethod.DAILY);
            Mockito.when(loanProductRelatedDetail.getDaysInYearType()).thenReturn(DaysInYearType.DAYS_360.getValue());
            Mockito.when(loanProductRelatedDetail.getDaysInMonthType()).thenReturn(DaysInMonthType.DAYS_30.getValue());
            Mockito.when(loanProductRelatedDetail.getAnnualNominalInterestRate()).thenReturn(BigDecimal.valueOf(12.0));
            Mockito.when(loanProductRelatedDetail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
            Mockito.when(loanProductRelatedDetail.getNumberOfRepayments()).thenReturn(3);
            Mockito.when(loanProductRelatedDetail.getRepayEvery()).thenReturn(1);
        }

        @Test
        public void testExtraTermsDecliningBalance() {
            final LocalDate disbursementDate = LocalDate.of(2024, 6, 1);
            final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = generateExpectedRepaymentPeriods(disbursementDate);
            final Integer installmentAmountInMultiplesOf = null;

            final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                    expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

            emiCalculator.addDisbursement(interestSchedule, disbursementDate, toMoney(1000));

            checkPeriod(interestSchedule, 0, 340.02, 10.00, 330.02, 669.98, false); //
            checkPeriod(interestSchedule, 1, 340.02, 6.70, 333.32, 336.66, false); //
            checkPeriod(interestSchedule, 2, 340.03, 3.37, 336.66, 0.00, false); //

            // add extra repayment period 1
            emiCalculator.addRepaymentPeriods(interestSchedule, LocalDate.of(2024, 7, 2), 1);

            checkPeriod(interestSchedule, 0, 340.02, 10.00, 330.02, 669.98, false); //
            checkPeriod(interestSchedule, 1, 227.81, 6.70, 221.11, 448.87, false); //
            checkPeriod(interestSchedule, 2, 227.81, 4.49, 223.32, 225.55, false); //
            checkPeriod(interestSchedule, 3, 227.81, 2.26, 225.55, 0.00, false); //

            emiCalculator.addRepaymentPeriods(interestSchedule, LocalDate.of(2024, 8, 2), 2);

            checkPeriod(interestSchedule, 0, 340.02, 10.00, 330.02, 669.98, false); //
            checkPeriod(interestSchedule, 1, 227.81, 6.70, 221.11, 448.87, false); //
            checkPeriod(interestSchedule, 2, 115.04, 4.49, 110.55, 338.32, false); //
            checkPeriod(interestSchedule, 3, 115.04, 3.38, 111.66, 226.66, false); //
            checkPeriod(interestSchedule, 4, 115.04, 2.27, 112.77, 113.89, false); //
            checkPeriod(interestSchedule, 5, 115.03, 1.14, 113.89, 0.00, false); //

        }

        @Test
        public void testExtraTermsFlat() {
            final LocalDate disbursementDate = LocalDate.of(2024, 6, 1);
            final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = generateExpectedRepaymentPeriods(disbursementDate);
            final Integer installmentAmountInMultiplesOf = null;

            Mockito.when(loanProductRelatedDetail.getInterestMethod()).thenReturn(InterestMethod.FLAT);
            Mockito.when(loanProductRelatedDetail.getInterestCalculationPeriodMethod())
                    .thenReturn(InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD);
            Mockito.when(loanProductRelatedDetail.isAllowPartialPeriodInterestCalculation()).thenReturn(true);

            final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                    expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

            emiCalculator.addDisbursement(interestSchedule, disbursementDate, toMoney(1000));

            checkPeriod(interestSchedule, 0, 343.33, 10.00, 333.33, 666.67, false); //
            checkPeriod(interestSchedule, 1, 343.33, 10.00, 333.33, 333.34, false); //
            checkPeriod(interestSchedule, 2, 343.34, 10.00, 333.34, 0.00, false); //

            // add extra repayment period 1
            emiCalculator.addRepaymentPeriods(interestSchedule, LocalDate.of(2024, 7, 2), 1);

            checkPeriod(interestSchedule, 0, 343.33, 10.00, 333.33, 666.67, false); //
            checkPeriod(interestSchedule, 1, 232.22, 10.00, 222.22, 444.45, false); //
            checkPeriod(interestSchedule, 2, 232.22, 10.00, 222.22, 222.23, false); //
            checkPeriod(interestSchedule, 3, 232.23, 10.00, 222.23, 0.00, false); //

            emiCalculator.addRepaymentPeriods(interestSchedule, LocalDate.of(2024, 8, 2), 2);

            checkPeriod(interestSchedule, 0, 343.33, 10.00, 333.33, 666.67, false); //
            checkPeriod(interestSchedule, 1, 232.22, 10.00, 222.22, 444.45, false); //
            checkPeriod(interestSchedule, 2, 121.11, 10.00, 111.11, 333.34, false); //
            checkPeriod(interestSchedule, 3, 121.11, 10.00, 111.11, 222.23, false); //
            checkPeriod(interestSchedule, 4, 121.11, 10.00, 111.11, 111.12, false); //
            checkPeriod(interestSchedule, 5, 121.12, 10.00, 111.12, 0.00, false); //

        }

        @Test
        public void testExtraTermsDecliningBalanceWithFullyRepaidPeriods() {
            final LocalDate disbursementDate = LocalDate.of(2024, 6, 1);

            Mockito.when(loanProductRelatedDetail.getNumberOfRepayments()).thenReturn(4);

            final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = generateExpectedRepaymentPeriods(disbursementDate);
            final Integer installmentAmountInMultiplesOf = null;

            final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                    expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

            emiCalculator.addDisbursement(interestSchedule, disbursementDate, toMoney(1000));

            checkPeriod(interestSchedule, 0, 256.28, 10.00, 246.28, 753.72, false); //
            checkPeriod(interestSchedule, 1, 256.28, 7.54, 248.74, 504.98, false); //
            checkPeriod(interestSchedule, 2, 256.28, 5.05, 251.23, 253.75, false); //
            checkPeriod(interestSchedule, 3, 256.29, 2.54, 253.75, 0.00, false); //

            // simulate merchant issued refund allocated on last installment ( fully repaid ) on first date
            emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 10, 1), LocalDate.of(2024, 6, 1),
                    Money.of(currency, BigDecimal.valueOf(256.29D)));

            // simulate early repayment on day 0
            emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 7, 1), LocalDate.of(2024, 6, 1),
                    Money.of(currency, BigDecimal.valueOf(256.28D)));

            // verify 1st and 4th installments are fully repaid
            checkPeriod(interestSchedule, 0, 256.28, 0.00, 256.28, 487.43, true); //
            checkPeriod(interestSchedule, 1, 256.28, 9.74, 246.54, 240.89, false); //
            checkPeriod(interestSchedule, 2, 243.30, 2.41, 240.89, 0.00, false); //
            checkPeriod(interestSchedule, 3, 256.29, 0.00, 256.29, 0.00, true); //

            // add extra repayment period 1
            emiCalculator.addRepaymentPeriods(interestSchedule, LocalDate.of(2024, 6, 15), 1);

            // verify 1st and 4th installments are not modified, additional principal paid are substract from last
            // period
            checkPeriod(interestSchedule, 0, 256.28, 0.00, 256.28, 487.43, true); //
            checkPeriod(interestSchedule, 1, 206.04, 9.74, 196.30, 291.13, false); //
            checkPeriod(interestSchedule, 2, 206.04, 2.91, 203.13, 88.00, false); //
            checkPeriod(interestSchedule, 3, 256.29, 0.00, 256.29, 88.00, true); //
            checkPeriod(interestSchedule, 4, 89.76, 1.76, 88.00, 0.00, false); //

            emiCalculator.addRepaymentPeriods(interestSchedule, LocalDate.of(2024, 6, 15), 2);

            // verify 1st and 4th installments are not modified, additional principal paid are substract from last
            // periods
            checkPeriod(interestSchedule, 0, 256.28, 0.00, 256.28, 487.43, true); //
            checkPeriod(interestSchedule, 1, 126.91, 9.74, 117.17, 370.26, false); //
            checkPeriod(interestSchedule, 2, 126.91, 3.70, 123.21, 247.05, false); //
            checkPeriod(interestSchedule, 3, 256.29, 0.00, 256.29, 247.05, true); //
            checkPeriod(interestSchedule, 4, 126.91, 4.94, 121.97, 125.08, false); //
            checkPeriod(interestSchedule, 5, 126.33, 1.25, 125.08, 0.00, false); //
            checkPeriod(interestSchedule, 6, 0.00, 0.00, 0.00, 0.00, true); //
        }

        @Test
        public void testExtraTermsFlatWithFullyRepaidPeriods() {
            final LocalDate disbursementDate = LocalDate.of(2024, 6, 1);

            Mockito.when(loanProductRelatedDetail.getNumberOfRepayments()).thenReturn(4);
            Mockito.when(loanProductRelatedDetail.getInterestMethod()).thenReturn(InterestMethod.FLAT);
            Mockito.when(loanProductRelatedDetail.getInterestCalculationPeriodMethod())
                    .thenReturn(InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD);
            Mockito.when(loanProductRelatedDetail.isAllowPartialPeriodInterestCalculation()).thenReturn(true);

            final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = generateExpectedRepaymentPeriods(disbursementDate);
            final Integer installmentAmountInMultiplesOf = null;

            final ProgressiveLoanInterestScheduleModel interestSchedule = emiCalculator.generatePeriodInterestScheduleModel(
                    expectedRepaymentPeriods, loanProductRelatedDetail, List.of(), installmentAmountInMultiplesOf, mc);

            emiCalculator.addDisbursement(interestSchedule, disbursementDate, toMoney(1000));

            checkPeriod(interestSchedule, 0, 260.00, 10.00, 250.00, 750.00, false); //
            checkPeriod(interestSchedule, 1, 260.00, 10.00, 250.00, 500.00, false); //
            checkPeriod(interestSchedule, 2, 260.00, 10.00, 250.00, 250.00, false); //
            checkPeriod(interestSchedule, 3, 260.00, 10.00, 250.00, 0.00, false); //

            // simulate merchant issued refund allocated on last installment ( fully repaid ) on first date
            emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 10, 1), LocalDate.of(2024, 6, 1),
                    Money.of(currency, BigDecimal.valueOf(250.0D)));
            emiCalculator.payInterest(interestSchedule, LocalDate.of(2024, 10, 1), LocalDate.of(2024, 6, 1),
                    Money.of(currency, BigDecimal.valueOf(10.0D)));

            checkPeriod(interestSchedule, 0, 260.00, 10.00, 250.00, 500.00, false); //
            checkPeriod(interestSchedule, 1, 260.00, 10.00, 250.00, 250.00, false); //
            checkPeriod(interestSchedule, 2, 260.00, 10.00, 250.00, 0.00, false); //
            checkPeriod(interestSchedule, 3, 260.00, 10.00, 250.00, 0.00, true); //

            // simulate early repayment on day 0
            emiCalculator.payPrincipal(interestSchedule, LocalDate.of(2024, 7, 1), LocalDate.of(2024, 6, 1),
                    Money.of(currency, BigDecimal.valueOf(250.0D)));
            emiCalculator.payInterest(interestSchedule, LocalDate.of(2024, 7, 1), LocalDate.of(2024, 6, 1),
                    Money.of(currency, BigDecimal.valueOf(10.0D)));

            // verify 1st and 4th installments are fully repaid
            checkPeriod(interestSchedule, 0, 260.00, 10.00, 250.00, 500.00, true); //
            checkPeriod(interestSchedule, 1, 260.00, 10.00, 250.00, 250.00, false); //
            checkPeriod(interestSchedule, 2, 260.00, 10.00, 250.00, 0.00, false); //
            checkPeriod(interestSchedule, 3, 260.00, 10.00, 250.00, 0.00, true); //

            // add extra repayment period 1
            emiCalculator.addRepaymentPeriods(interestSchedule, LocalDate.of(2024, 6, 15), 1);

            // verify 1st and 4th installments are not modified, additional principal paid are substract from last
            // period
            checkPeriod(interestSchedule, 0, 260.00, 10.00, 250.00, 500.00, true); //
            checkPeriod(interestSchedule, 1, 210.00, 10.00, 200.00, 300.00, false); //
            checkPeriod(interestSchedule, 2, 210.00, 10.00, 200.00, 100.00, false); //
            checkPeriod(interestSchedule, 3, 260.00, 10.00, 250.00, 100.00, true); //
            checkPeriod(interestSchedule, 4, 110.00, 10.00, 100.00, 0.00, false); //

            // add extra repayment period 2
            emiCalculator.addRepaymentPeriods(interestSchedule, LocalDate.of(2024, 6, 15), 2);

            // verify 1st and 4th installments are not modified, additional principal paid are substract from last
            // periods
            checkPeriod(interestSchedule, 0, 260.00, 10.00, 250.00, 500.00, true); //
            checkPeriod(interestSchedule, 1, 152.86, 10.00, 142.86, 357.14, false); //
            checkPeriod(interestSchedule, 2, 152.86, 10.00, 142.86, 214.28, false); //
            checkPeriod(interestSchedule, 3, 260.00, 10.00, 250.00, 214.28, true); //
            checkPeriod(interestSchedule, 4, 152.86, 10.00, 142.86, 71.42, false); //
            checkPeriod(interestSchedule, 5, 81.42, 10.00, 71.42, 0.00, false); //
            checkPeriod(interestSchedule, 6, 10.00, 10.00, 0.00, 0.00, false); //

        }

    }

    // utilities
    private List<LoanScheduleModelRepaymentPeriod> generateExpectedRepaymentPeriods(LocalDate disbursementDate) {
        return switch (loanProductRelatedDetail.getRepaymentPeriodFrequencyType()) {
            case MONTHS -> expectedRepaymentsMonthly(disbursementDate, loanProductRelatedDetail.getNumberOfRepayments(),
                    loanProductRelatedDetail.getRepayEvery());
            case WEEKS -> expectedRepaymentWeeks(disbursementDate, loanProductRelatedDetail.getNumberOfRepayments(),
                    loanProductRelatedDetail.getRepayEvery());
            case DAYS -> expectedRepaymentDays(disbursementDate, loanProductRelatedDetail.getNumberOfRepayments(),
                    loanProductRelatedDetail.getRepayEvery());
            default -> throw new UnsupportedOperationException();
        };
    }

    List<LoanScheduleModelRepaymentPeriod> expectedRepaymentDays(final LocalDate disbursementDate, final int periods, final int length) {
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = new ArrayList<>(periods);
        IntStream.range(0, periods).forEach(i -> expectedRepaymentPeriods
                .add(repayment(i + 1, disbursementDate.plusDays((long) i * length), disbursementDate.plusDays((long) (i + 1) * length))));
        return expectedRepaymentPeriods;
    }

    List<LoanScheduleModelRepaymentPeriod> expectedRepaymentsMonthly(final LocalDate disbursementDate, final int periods,
            final int length) {
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = new ArrayList<>(periods);
        IntStream.range(0, periods).forEach(i -> expectedRepaymentPeriods.add(
                repayment(i + 1, disbursementDate.plusMonths((long) i * length), disbursementDate.plusMonths((long) (i + 1) * length))));
        return expectedRepaymentPeriods;
    }

    List<LoanScheduleModelRepaymentPeriod> expectedRepaymentWeeks(final LocalDate disbursementDate, final int periods, final int length) {
        final List<LoanScheduleModelRepaymentPeriod> expectedRepaymentPeriods = new ArrayList<>(periods);
        IntStream.range(0, periods).forEach(i -> expectedRepaymentPeriods
                .add(repayment(i + 1, disbursementDate.plusWeeks((long) i * length), disbursementDate.plusWeeks((long) (i + 1) * length))));
        return expectedRepaymentPeriods;
    }

    private static LoanScheduleModelRepaymentPeriod repayment(int periodNumber, LocalDate fromDate, LocalDate dueDate) {
        final Money zeroAmount = Money.zero(currency);
        return LoanScheduleModelRepaymentPeriod.repayment(periodNumber, fromDate, dueDate, zeroAmount, zeroAmount, zeroAmount, zeroAmount,
                zeroAmount, zeroAmount, false, mc);
    }

    @NonNull
    private static LoanRepaymentScheduleInstallment createPeriod(int periodId, LocalDate start, LocalDate end) {
        LoanRepaymentScheduleInstallment period = Mockito.mock(LoanRepaymentScheduleInstallment.class);
        Mockito.when(period.getInstallmentNumber()).thenReturn(periodId);
        Mockito.when(period.getFromDate()).thenReturn(start);
        Mockito.when(period.getDueDate()).thenReturn(end);

        return period;
    }

    private static void checkDailyInterest(final ProgressiveLoanInterestScheduleModel interestModel, final LocalDate repaymentPeriodDueDate,
            final LocalDate interestStartDay, final int dayOffset, final double dailyInterest, final double interest) {
        Money previousInterest = emiCalculator.getPeriodInterestTillDate(interestModel, repaymentPeriodDueDate,
                interestStartDay.plusDays(dayOffset - 1), true);
        Money currentInterest = emiCalculator.getPeriodInterestTillDate(interestModel, repaymentPeriodDueDate,
                interestStartDay.plusDays(dayOffset), true);
        Assertions.assertEquals(dailyInterest, toDouble(currentInterest.minus(previousInterest)));
        Assertions.assertEquals(interest, toDouble(currentInterest));
    }

    private static void checkEmi(final ProgressiveLoanInterestScheduleModel interestScheduleModel, final int repaymentIdx,
            final double emiValue) {
        Assertions.assertEquals(emiValue, toDouble(interestScheduleModel.repaymentPeriods().get(repaymentIdx).getEmi()));
    }

    private static void checkTotalInterestDue(final ProgressiveLoanInterestScheduleModel interestScheduleModel,
            final double totalInterestDue) {
        Assertions.assertEquals(totalInterestDue, toDouble(interestScheduleModel.getTotalDueInterest()));
    }

    private static void checkPeriod(final ProgressiveLoanInterestScheduleModel interestScheduleModel, final int repaymentIdx,
            final int interestIdx, final double emiValue, final double rateFactor, final double interestDue, final double principalDue,
            final double remaingBalance) {
        checkPeriod(interestScheduleModel, repaymentIdx, interestIdx, emiValue, rateFactor, interestDue, interestDue, principalDue,
                remaingBalance);
    }

    private static void checkPeriod(final ProgressiveLoanInterestScheduleModel interestScheduleModel, final int repaymentIdx,
            final double emiValue, final double interestDueCumulated, final double principalDue, final double remainingBalance,
            final boolean fullyRepaid) {
        final RepaymentPeriod repaymentPeriod = interestScheduleModel.repaymentPeriods().get(repaymentIdx);

        Assertions.assertEquals(emiValue, toDouble(repaymentPeriod.getEmi()));
        Assertions.assertEquals(interestDueCumulated, toDouble(repaymentPeriod.getDueInterest()));
        Assertions.assertEquals(principalDue, toDouble(repaymentPeriod.getDuePrincipal()));
        Assertions.assertEquals(remainingBalance, toDouble(repaymentPeriod.getOutstandingLoanBalance()));
        Assertions.assertEquals(fullyRepaid, repaymentPeriod.isFullyPaid());
    }

    private static void checkPeriod(final ProgressiveLoanInterestScheduleModel interestScheduleModel, final int repaymentIdx,
            final int interestIdx, final double emiValue, final double rateFactor, final double interestDue,
            final double interestDueCumulated, final double principalDue, final double remaingBalance) {
        Assertions.assertTrue(repaymentIdx < interestScheduleModel.repaymentPeriods().size(),
                repaymentIdx + "th repaymentPeriod is not found.");
        final RepaymentPeriod repaymentPeriod = interestScheduleModel.repaymentPeriods().get(repaymentIdx);
        Assertions.assertTrue(interestIdx < repaymentPeriod.getInterestPeriods().size(),
                repaymentIdx + "th repaymentPeriod's " + interestIdx + "th interest period is not found.");
        final InterestPeriod interestPeriod = repaymentPeriod.getInterestPeriods().get(interestIdx);

        Assertions.assertAll("Check period", () -> Assertions.assertEquals(emiValue, toDouble(repaymentPeriod.getEmi())),
                () -> Assertions.assertEquals(rateFactor, toDouble(applyMathContext(interestPeriod.getRateFactor()))),
                () -> Assertions.assertEquals(interestDue, toDouble(interestPeriod.getCalculatedDueInterest())),
                () -> Assertions.assertEquals(interestDueCumulated, toDouble(repaymentPeriod.getDueInterest())),
                () -> Assertions.assertEquals(principalDue, toDouble(repaymentPeriod.getDuePrincipal())),
                () -> Assertions.assertEquals(remaingBalance, toDouble(repaymentPeriod.getOutstandingLoanBalance())));
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

    private static void verifyAllPeriods(final ProgressiveLoanInterestScheduleModel expectedModel,
            final ProgressiveLoanInterestScheduleModel actualModel) {
        for (int repInd = 0; repInd < expectedModel.repaymentPeriods().size(); repInd++) {
            RepaymentPeriod repaymentPeriod = expectedModel.repaymentPeriods().get(repInd);
            for (int interestPeriodIndex = 0; interestPeriodIndex < repaymentPeriod.getInterestPeriods().size(); interestPeriodIndex++) {
                final InterestPeriod interestPeriod = repaymentPeriod.getInterestPeriods().get(interestPeriodIndex);
                checkPeriod(actualModel, repInd, interestPeriodIndex, toDouble(repaymentPeriod.getEmi()),
                        toDouble(applyMathContext(interestPeriod.getRateFactor())), toDouble(interestPeriod.getCalculatedDueInterest()),
                        toDouble(repaymentPeriod.getDueInterest()), toDouble(repaymentPeriod.getDuePrincipal()),
                        toDouble(repaymentPeriod.getOutstandingLoanBalance()));
            }
        }
    }

    private ProgressiveLoanInterestScheduleModel copyJson(ProgressiveLoanInterestScheduleModel toCopy) {
        String json = interestScheduleModelService.toJson(toCopy);
        return interestScheduleModelService.fromJson(json, toCopy.loanProductRelatedDetail(), toCopy.mc(),
                toCopy.installmentAmountInMultiplesOf());
    }
}
