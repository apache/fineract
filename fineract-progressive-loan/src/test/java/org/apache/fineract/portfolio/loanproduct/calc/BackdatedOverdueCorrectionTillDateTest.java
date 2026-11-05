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
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.DefaultScheduledDateGenerator;
import org.apache.fineract.portfolio.loanaccount.service.ProgressiveLoanInterestScheduleModelParserServiceGsonImpl;
import org.apache.fineract.portfolio.loanproduct.calc.data.ProgressiveLoanInterestScheduleModel;
import org.apache.fineract.portfolio.loanproduct.calc.data.RepaymentPeriod;
import org.apache.fineract.portfolio.loanproduct.calc.data.RepaymentScheduleInstallmentData;
import org.apache.fineract.portfolio.loanproduct.domain.ILoanConfigurationDetails;
import org.apache.fineract.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestMethod;
import org.apache.fineract.portfolio.loanproduct.domain.RecalculationFrequencyType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Interest till a historical date of a backdated loan whose model holds overdue corrections up to the maturity date
 * (FINERACT-2968). The calculation copy is truncated at the target date, which removes the interest periods holding the
 * later corrections.
 */
@ExtendWith(MockitoExtension.class)
class BackdatedOverdueCorrectionTillDateTest {

    private static final ProgressiveEMICalculator emiCalculator = new ProgressiveEMICalculator(new DefaultScheduledDateGenerator());
    private static MockedStatic<ThreadLocalContextUtil> threadLocalContextUtil;
    private static MockedStatic<MoneyHelper> moneyHelper;
    private static final MathContext mc = new MathContext(12, RoundingMode.HALF_EVEN);
    private static final CurrencyData currency = new CurrencyData("EUR", "EUR", 2, 1, "E", "EUR");
    private static final ILoanConfigurationDetails detail = Mockito.mock(ILoanConfigurationDetails.class);
    private static final List<LocalDate> DUE_DATES = List.of(LocalDate.of(2023, 10, 31), LocalDate.of(2023, 11, 30),
            LocalDate.of(2023, 12, 31), LocalDate.of(2024, 1, 31), LocalDate.of(2024, 2, 29), LocalDate.of(2024, 3, 31),
            LocalDate.of(2024, 4, 30));
    private final ProgressiveLoanInterestScheduleModelParserServiceGsonImpl interestScheduleModelService = new ProgressiveLoanInterestScheduleModelParserServiceGsonImpl();

    @BeforeAll
    static void init() {
        threadLocalContextUtil = Mockito.mockStatic(ThreadLocalContextUtil.class);
        moneyHelper = Mockito.mockStatic(MoneyHelper.class);
        moneyHelper.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
        moneyHelper.when(MoneyHelper::getMathContext).thenReturn(new MathContext(12, RoundingMode.HALF_EVEN));
    }

    @AfterAll
    static void tearDown() {
        threadLocalContextUtil.close();
        moneyHelper.close();
    }

    @BeforeEach
    void setupDefaults() {
        Mockito.lenient().when(detail.isInterestRecognitionOnDisbursementDate()).thenReturn(false);
        Mockito.lenient().when(detail.getDaysInYearCustomStrategy()).thenReturn(null);
        Mockito.lenient().when(detail.getCurrencyData()).thenReturn(currency);
        Mockito.lenient().when(detail.getInterestMethod()).thenReturn(InterestMethod.DECLINING_BALANCE);
        Mockito.lenient().when(detail.getInterestCalculationPeriodMethod()).thenReturn(InterestCalculationPeriodMethod.DAILY);
        Mockito.lenient().when(detail.isAllowPartialPeriodInterestCalculation()).thenReturn(true);
        Mockito.lenient().when(detail.getGraceOnPrincipalPayment()).thenReturn(0);
        Mockito.lenient().when(detail.getGraceOnInterestPayment()).thenReturn(0);
        Mockito.lenient().when(detail.getAnnualNominalInterestRate()).thenReturn(BigDecimal.valueOf(9.99));
        Mockito.lenient().when(detail.getDaysInYearType()).thenReturn(DaysInYearType.ACTUAL.getValue());
        Mockito.lenient().when(detail.getDaysInMonthType()).thenReturn(DaysInMonthType.ACTUAL.getValue());
        Mockito.lenient().when(detail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        Mockito.lenient().when(detail.getRepayEvery()).thenReturn(1);
        Mockito.lenient().when(detail.isInterestRecalculationEnabled()).thenReturn(true);
        Mockito.lenient().when(detail.getRestFrequencyType()).thenReturn(RecalculationFrequencyType.DAILY);
    }

    @Test
    void interestTillHistoricalDateIgnoresOverdueCorrectionsOnRemovedInterestPeriods() {
        final ProgressiveLoanInterestScheduleModel model = unpaidBackdatedModelWithOverdueCorrectionsTill(LocalDate.of(2025, 1, 21));
        final LocalDate maturityDate = model.getMaturityDate();
        Assertions.assertTrue(model.hasOverdueCorrectionsOnDate(maturityDate), "The model must hold an overdue correction at maturity");
        final String modelBefore = interestScheduleModelService.toJson(model);

        final LocalDate targetDate = LocalDate.of(2024, 3, 3);
        final RepaymentPeriod targetPeriod = model.findRepaymentPeriod(targetDate).orElseThrow();
        for (final RepaymentPeriod period : model.repaymentPeriods()) {
            if (period.getDueDate().isAfter(targetDate)) {
                // every unpaid period after the target date is processed by the periodic accrual
                Assertions.assertDoesNotThrow(() -> emiCalculator.getPeriodInterestTillDate(model, period.getFromDate(),
                        period.getDueDate(), targetDate, true, false), "Interest till " + targetDate + " of " + period.getDueDate());
            }
        }

        final Money interestTillTargetDate = emiCalculator.getPeriodInterestTillDate(model, targetPeriod.getFromDate(),
                targetPeriod.getDueDate(), targetDate, true, false);
        Assertions.assertTrue(interestTillTargetDate.isGreaterThanZero(), "Interest till " + targetDate);
        Assertions.assertEquals(modelBefore, interestScheduleModelService.toJson(model),
                "Calculating interest till a date must not change the model");
    }

    private ProgressiveLoanInterestScheduleModel unpaidBackdatedModelWithOverdueCorrectionsTill(final LocalDate businessDate) {
        final List<RepaymentScheduleInstallmentData> installments = new ArrayList<>();
        for (int i = 1; i < DUE_DATES.size(); i++) {
            installments.add(RepaymentScheduleInstallmentData.of(DUE_DATES.get(i - 1), DUE_DATES.get(i), false, false, BigDecimal.ZERO,
                    BigDecimal.ZERO));
        }
        final ProgressiveLoanInterestScheduleModel model = emiCalculator.generateInstallmentInterestScheduleModel(installments, detail,
                null, mc);
        emiCalculator.addDisbursement(model, DUE_DATES.getFirst(), Money.of(currency, BigDecimal.valueOf(2450), mc));
        emiCalculator.recalculateModelOverdueAmountsTillDate(model, businessDate, false);
        return model;
    }
}
