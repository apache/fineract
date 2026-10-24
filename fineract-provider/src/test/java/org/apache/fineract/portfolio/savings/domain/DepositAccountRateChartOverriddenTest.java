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
package org.apache.fineract.portfolio.savings.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.savings.PreClosurePenalInterestOnType;
import org.apache.fineract.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.apache.fineract.portfolio.savings.SavingsPeriodFrequencyType;
import org.apache.fineract.portfolio.savings.SavingsPostingInterestPeriodType;
import org.apache.fineract.portfolio.savings.data.DepositAccountDataValidator;
import org.apache.fineract.portfolio.savings.data.DepositProductDataValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class DepositAccountRateChartOverriddenTest {

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(
                Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2024, 6, 1), BusinessDateType.COB_DATE, LocalDate.of(2024, 5, 31))));
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    private FixedDepositAccount createFixedDepositAccount(BigDecimal nominalRate, boolean isOverridden,
            DepositPreClosureDetail preClosureDetail, DepositAccountInterestRateChart chart) {
        FixedDepositAccount account = new FixedDepositAccount();
        ReflectionTestUtils.setField(account, "nominalAnnualInterestRate", nominalRate);
        ReflectionTestUtils.setField(account, "submittedOnDate", LocalDate.of(2024, 1, 1));
        ReflectionTestUtils.setField(account, "interestPostingPeriodType", SavingsPostingInterestPeriodType.MONTHLY.getValue());
        ReflectionTestUtils.setField(account, "interestCompoundingPeriodType", SavingsCompoundingInterestPeriodType.MONTHLY.getValue());

        DepositTermDetail termDetails = DepositTermDetail.createFrom(1, 12, SavingsPeriodFrequencyType.MONTHS,
                SavingsPeriodFrequencyType.MONTHS, 1, SavingsPeriodFrequencyType.MONTHS);
        DepositPreClosureDetail effectivePreClosure = preClosureDetail != null ? preClosureDetail
                : DepositPreClosureDetail.createFrom(false, null, null);

        DepositAccountTermAndPreClosure term = DepositAccountTermAndPreClosure.createNew(effectivePreClosure, termDetails, account,
                BigDecimal.valueOf(10000), BigDecimal.valueOf(10000), null, 6, SavingsPeriodFrequencyType.MONTHS, null, null, false, null,
                isOverridden);
        ReflectionTestUtils.setField(account, "accountTermAndPreClosure", term);

        if (chart != null) {
            ReflectionTestUtils.setField(account, "chart", chart);
        }
        return account;
    }

    private RecurringDepositAccount createRecurringDepositAccount(BigDecimal nominalRate, boolean isOverridden,
            DepositPreClosureDetail preClosureDetail, DepositAccountInterestRateChart chart) {
        RecurringDepositAccount account = new RecurringDepositAccount();
        ReflectionTestUtils.setField(account, "nominalAnnualInterestRate", nominalRate);
        ReflectionTestUtils.setField(account, "submittedOnDate", LocalDate.of(2024, 1, 1));

        DepositTermDetail termDetails = DepositTermDetail.createFrom(1, 12, SavingsPeriodFrequencyType.MONTHS,
                SavingsPeriodFrequencyType.MONTHS, 1, SavingsPeriodFrequencyType.MONTHS);
        DepositPreClosureDetail effectivePreClosure = preClosureDetail != null ? preClosureDetail
                : DepositPreClosureDetail.createFrom(false, null, null);

        DepositAccountTermAndPreClosure term = DepositAccountTermAndPreClosure.createNew(effectivePreClosure, termDetails, account,
                BigDecimal.valueOf(10000), BigDecimal.valueOf(10000), null, 6, SavingsPeriodFrequencyType.MONTHS, null, null, false, null,
                isOverridden);
        ReflectionTestUtils.setField(account, "accountTermAndPreClosure", term);

        if (chart != null) {
            ReflectionTestUtils.setField(account, "chart", chart);
        }
        return account;
    }

    @Test
    void testDepositAccountTermAndPreClosureHoldsOverriddenFlag() {
        DepositTermDetail termDetails = DepositTermDetail.createFrom(1, 12, SavingsPeriodFrequencyType.MONTHS,
                SavingsPeriodFrequencyType.MONTHS, 1, SavingsPeriodFrequencyType.MONTHS);
        DepositPreClosureDetail preClosureDetail = DepositPreClosureDetail.createFrom(true, BigDecimal.valueOf(1.0),
                PreClosurePenalInterestOnType.WHOLE_TERM);

        DepositAccountTermAndPreClosure term = DepositAccountTermAndPreClosure.createNew(preClosureDetail, termDetails, null,
                BigDecimal.valueOf(1000), BigDecimal.valueOf(1000), null, 12, SavingsPeriodFrequencyType.MONTHS, null, null, false, null,
                true);

        assertTrue(term.isRateChartOverridden());

        DepositAccountTermAndPreClosure copiedTerm = term.copy(BigDecimal.valueOf(2000));
        assertTrue(copiedTerm.isRateChartOverridden());
    }

    @Test
    void testDepositAccountTermAndPreClosureDefaultFalseFlag() {
        DepositTermDetail termDetails = DepositTermDetail.createFrom(1, 12, SavingsPeriodFrequencyType.MONTHS,
                SavingsPeriodFrequencyType.MONTHS, 1, SavingsPeriodFrequencyType.MONTHS);
        DepositPreClosureDetail preClosureDetail = DepositPreClosureDetail.createFrom(false, null, null);

        DepositAccountTermAndPreClosure term = DepositAccountTermAndPreClosure.createNew(preClosureDetail, termDetails, null,
                BigDecimal.valueOf(1000), BigDecimal.valueOf(1000), null, 12, SavingsPeriodFrequencyType.MONTHS, null, null, false, null,
                false);

        assertFalse(term.isRateChartOverridden());

        DepositAccountTermAndPreClosure copiedTerm = term.copy(BigDecimal.valueOf(2000));
        assertFalse(copiedTerm.isRateChartOverridden());
    }

    @Test
    void testFixedDepositEffectiveInterestRateUsesOverriddenRateInsteadOfChart() {
        DepositAccountInterestRateChart chart = mock(DepositAccountInterestRateChart.class);
        when(chart.getApplicableInterestRate(any(), any(), any(), any())).thenReturn(new BigDecimal("5.00"));

        FixedDepositAccount account = createFixedDepositAccount(new BigDecimal("7.50"), true, null, chart);

        BigDecimal effectiveRate = account.getEffectiveInterestRateAsFraction(MathContext.DECIMAL64, DateUtils.getBusinessLocalDate(),
                false);
        assertEquals(0, new BigDecimal("0.075").compareTo(effectiveRate));
    }

    @Test
    void testFixedDepositEffectiveInterestRateUsesChartWhenNotOverridden() {
        DepositAccountInterestRateChart chart = mock(DepositAccountInterestRateChart.class);
        when(chart.getApplicableInterestRate(any(), any(), any(), any())).thenReturn(new BigDecimal("5.00"));

        FixedDepositAccount account = createFixedDepositAccount(new BigDecimal("7.50"), false, null, chart);

        BigDecimal effectiveRate = account.getEffectiveInterestRateAsFraction(MathContext.DECIMAL64, DateUtils.getBusinessLocalDate(),
                false);
        assertEquals(0, new BigDecimal("0.05").compareTo(effectiveRate));
    }

    @Test
    void testFixedDepositNoChartNotOverriddenPrematureClosureDoesNotDriftRate() {
        // Reviewer point 1: no-chart + not-overridden + premature closure must not reduce or mutate rate
        DepositPreClosureDetail preClosureDetail = DepositPreClosureDetail.createFrom(true, new BigDecimal("1.00"),
                PreClosurePenalInterestOnType.WHOLE_TERM);
        FixedDepositAccount account = createFixedDepositAccount(new BigDecimal("7.50"), false, preClosureDetail, null);

        LocalDate prematureCloseDate = DateUtils.getBusinessLocalDate().minusMonths(1);

        // First call
        BigDecimal firstRate = account.getEffectiveInterestRateAsFraction(MathContext.DECIMAL64, prematureCloseDate, true);
        assertEquals(0, new BigDecimal("0.075").compareTo(firstRate));
        assertEquals(0, new BigDecimal("7.50").compareTo(account.getNominalAnnualInterestRate()));

        // Second call: must remain idempotent (no drift)
        BigDecimal secondRate = account.getEffectiveInterestRateAsFraction(MathContext.DECIMAL64, prematureCloseDate, true);
        assertEquals(0, new BigDecimal("0.075").compareTo(secondRate));
        assertEquals(0, new BigDecimal("7.50").compareTo(account.getNominalAnnualInterestRate()));
    }

    @Test
    void testFixedDepositToggleRateChartOverriddenBackToFalseRecalculatesFromChart() {
        // Reviewer point 3: toggling flag back to false recalculates rate from chart
        DepositAccountInterestRateChart chart = mock(DepositAccountInterestRateChart.class);
        when(chart.getApplicableInterestRate(any(), any(), any(), any())).thenReturn(new BigDecimal("5.00"));

        FixedDepositAccount account = createFixedDepositAccount(new BigDecimal("7.50"), true, null, chart);

        // While overridden, effective rate is custom (7.5%)
        BigDecimal effectiveRateOverridden = account.getEffectiveInterestRateAsFraction(MathContext.DECIMAL64,
                DateUtils.getBusinessLocalDate(), false);
        assertEquals(0, new BigDecimal("0.075").compareTo(effectiveRateOverridden));

        // Toggle isRateChartOverridden to false on the term
        DepositAccountTermAndPreClosure term = (DepositAccountTermAndPreClosure) ReflectionTestUtils.getField(account,
                "accountTermAndPreClosure");
        ReflectionTestUtils.setField(term, "isRateChartOverridden", false);

        // On next calculation, rate must be recalculated from chart (5.0%) and updated on entity
        BigDecimal effectiveRateFromChart = account.getEffectiveInterestRateAsFraction(MathContext.DECIMAL64,
                DateUtils.getBusinessLocalDate(), false);
        assertEquals(0, new BigDecimal("0.05").compareTo(effectiveRateFromChart));
        assertEquals(0, new BigDecimal("5.00").compareTo(account.getNominalAnnualInterestRate()));
    }

    @Test
    void testFixedDepositEffectiveInterestRateWithOverriddenRateAndPrematureClosurePenalty() {
        DepositPreClosureDetail preClosureDetail = DepositPreClosureDetail.createFrom(true, new BigDecimal("1.00"),
                PreClosurePenalInterestOnType.WHOLE_TERM);
        DepositAccountInterestRateChart chart = mock(DepositAccountInterestRateChart.class);

        FixedDepositAccount account = createFixedDepositAccount(new BigDecimal("7.50"), true, preClosureDetail, chart);

        // When overridden, premature closure penalty is applied only if defined within chart calculation
        // but overridden rates bypass chart and return the nominalAnnualInterestRate directly
        LocalDate prematureCloseDate = DateUtils.getBusinessLocalDate().minusMonths(1);
        BigDecimal effectiveRate = account.getEffectiveInterestRateAsFraction(MathContext.DECIMAL64, prematureCloseDate, true);
        assertEquals(0, new BigDecimal("0.075").compareTo(effectiveRate));
        assertEquals(0, new BigDecimal("7.50").compareTo(account.getNominalAnnualInterestRate()));
    }

    @Test
    void testRecurringDepositEffectiveInterestRateUsesOverriddenRateInsteadOfChart() {
        DepositAccountInterestRateChart chart = mock(DepositAccountInterestRateChart.class);
        when(chart.getApplicableInterestRate(any(), any(), any(), any())).thenReturn(new BigDecimal("6.00"));

        RecurringDepositAccount account = createRecurringDepositAccount(new BigDecimal("8.00"), true, null, chart);

        BigDecimal effectiveRate = account.getEffectiveInterestRateAsFraction(MathContext.DECIMAL64, DateUtils.getBusinessLocalDate(),
                false);
        assertEquals(0, new BigDecimal("0.08").compareTo(effectiveRate));
    }

    @Test
    void testRecurringDepositNoChartNotOverriddenPrematureClosureDoesNotDriftRate() {
        DepositPreClosureDetail preClosureDetail = DepositPreClosureDetail.createFrom(true, new BigDecimal("1.50"),
                PreClosurePenalInterestOnType.WHOLE_TERM);
        RecurringDepositAccount account = createRecurringDepositAccount(new BigDecimal("8.00"), false, preClosureDetail, null);

        LocalDate prematureCloseDate = DateUtils.getBusinessLocalDate().minusMonths(1);

        BigDecimal firstRate = account.getEffectiveInterestRateAsFraction(MathContext.DECIMAL64, prematureCloseDate, true);
        assertEquals(0, new BigDecimal("0.08").compareTo(firstRate));
        assertEquals(0, new BigDecimal("8.00").compareTo(account.getNominalAnnualInterestRate()));

        BigDecimal secondRate = account.getEffectiveInterestRateAsFraction(MathContext.DECIMAL64, prematureCloseDate, true);
        assertEquals(0, new BigDecimal("0.08").compareTo(secondRate));
        assertEquals(0, new BigDecimal("8.00").compareTo(account.getNominalAnnualInterestRate()));
    }

    @Test
    void testRecurringDepositToggleRateChartOverriddenBackToFalseRecalculatesFromChart() {
        DepositAccountInterestRateChart chart = mock(DepositAccountInterestRateChart.class);
        when(chart.getApplicableInterestRate(any(), any(), any(), any())).thenReturn(new BigDecimal("6.00"));

        RecurringDepositAccount account = createRecurringDepositAccount(new BigDecimal("8.00"), true, null, chart);

        BigDecimal effectiveRateOverridden = account.getEffectiveInterestRateAsFraction(MathContext.DECIMAL64,
                DateUtils.getBusinessLocalDate(), false);
        assertEquals(0, new BigDecimal("0.08").compareTo(effectiveRateOverridden));

        DepositAccountTermAndPreClosure term = (DepositAccountTermAndPreClosure) ReflectionTestUtils.getField(account,
                "accountTermAndPreClosure");
        ReflectionTestUtils.setField(term, "isRateChartOverridden", false);

        BigDecimal effectiveRateFromChart = account.getEffectiveInterestRateAsFraction(MathContext.DECIMAL64,
                DateUtils.getBusinessLocalDate(), false);
        assertEquals(0, new BigDecimal("0.06").compareTo(effectiveRateFromChart));
        assertEquals(0, new BigDecimal("6.00").compareTo(account.getNominalAnnualInterestRate()));
    }

    @Test
    void testValidationFailsWhenRateChartOverriddenTrueWithoutNominalAnnualInterestRateOnSubmit() {
        FromJsonHelper fromJsonHelper = new FromJsonHelper();
        DepositProductDataValidator productValidator = mock(DepositProductDataValidator.class);
        DepositAccountDataValidator validator = new DepositAccountDataValidator(fromJsonHelper, productValidator);

        String invalidJson = """
                {
                    "clientId": 1,
                    "productId": 1,
                    "submittedOnDate": "01 January 2024",
                    "dateFormat": "dd MMMM yyyy",
                    "locale": "en",
                    "depositAmount": 1000,
                    "depositPeriod": 6,
                    "depositPeriodFrequencyId": 2,
                    "isRateChartOverridden": true
                }
                """;

        assertThrows(PlatformApiDataValidationException.class, () -> validator.validateFixedDepositForSubmit(invalidJson));
    }

    @Test
    void testValidationPassesWhenRateChartOverriddenTrueWithNominalAnnualInterestRate() {
        FromJsonHelper fromJsonHelper = new FromJsonHelper();
        DepositProductDataValidator productValidator = mock(DepositProductDataValidator.class);
        DepositAccountDataValidator validator = new DepositAccountDataValidator(fromJsonHelper, productValidator);

        String validJson = """
                {
                    "clientId": 1,
                    "productId": 1,
                    "submittedOnDate": "01 January 2024",
                    "dateFormat": "dd MMMM yyyy",
                    "locale": "en",
                    "depositAmount": 1000,
                    "depositPeriod": 6,
                    "depositPeriodFrequencyId": 2,
                    "isRateChartOverridden": true,
                    "nominalAnnualInterestRate": 7.5
                }
                """;

        assertDoesNotThrow(() -> validator.validateFixedDepositForSubmit(validJson));
    }

    @Test
    void testValidationPassesWhenRateChartOverriddenToggledWithoutNominalAnnualInterestRateOnUpdate() {
        // Reviewer point 2: toggling isRateChartOverridden on update without passing nominalAnnualInterestRate must
        // pass validation
        FromJsonHelper fromJsonHelper = new FromJsonHelper();
        DepositProductDataValidator productValidator = mock(DepositProductDataValidator.class);
        DepositAccountDataValidator validator = new DepositAccountDataValidator(fromJsonHelper, productValidator);

        String updateJson = """
                {
                    "isRateChartOverridden": true
                }
                """;

        assertDoesNotThrow(() -> validator.validateFixedDepositForUpdate(updateJson));
    }

    @Test
    void testValidationFailsWhenZeroNominalAnnualInterestRateProvidedWithOverriddenRate() {
        // Reviewer point 2: zero rate should be rejected by validator via .positiveAmount()
        FromJsonHelper fromJsonHelper = new FromJsonHelper();
        DepositProductDataValidator productValidator = mock(DepositProductDataValidator.class);
        DepositAccountDataValidator validator = new DepositAccountDataValidator(fromJsonHelper, productValidator);

        String zeroRateJson = """
                {
                    "clientId": 1,
                    "productId": 1,
                    "submittedOnDate": "01 January 2024",
                    "dateFormat": "dd MMMM yyyy",
                    "locale": "en",
                    "depositAmount": 1000,
                    "depositPeriod": 6,
                    "depositPeriodFrequencyId": 2,
                    "isRateChartOverridden": true,
                    "nominalAnnualInterestRate": 0
                }
                """;

        assertThrows(PlatformApiDataValidationException.class, () -> validator.validateFixedDepositForSubmit(zeroRateJson));
    }

    @Test
    void testFixedDepositValidateDomainRulesSucceedsWhenRateChartOverriddenEvenIfNoMatchingSlab() {
        DepositAccountInterestRateChart chart = mock(DepositAccountInterestRateChart.class);
        when(chart.getFromDate()).thenReturn(LocalDate.of(2024, 1, 1));
        when(chart.getApplicableInterestRate(any(), any(), any(), any())).thenReturn(BigDecimal.ZERO);

        FixedDepositAccount account = createFixedDepositAccount(new BigDecimal("7.50"), true, null, chart);

        assertDoesNotThrow(account::validateDomainRules);
    }

    @Test
    void testFixedDepositValidateDomainRulesFailsWhenRateChartNotOverriddenAndNoMatchingSlab() {
        DepositAccountInterestRateChart chart = mock(DepositAccountInterestRateChart.class);
        when(chart.getFromDate()).thenReturn(LocalDate.of(2024, 1, 1));
        when(chart.getApplicableInterestRate(any(), any(), any(), any())).thenReturn(BigDecimal.ZERO);

        FixedDepositAccount account = createFixedDepositAccount(new BigDecimal("7.50"), false, null, chart);

        assertThrows(PlatformApiDataValidationException.class, account::validateDomainRules);
    }

    @Test
    void testRecurringDepositValidateApplicableInterestRateSucceedsWhenRateChartOverriddenEvenIfNoMatchingSlab() {
        DepositAccountInterestRateChart chart = mock(DepositAccountInterestRateChart.class);
        when(chart.getFromDate()).thenReturn(LocalDate.of(2024, 1, 1));
        when(chart.getApplicableInterestRate(any(), any(), any(), any())).thenReturn(BigDecimal.ZERO);

        RecurringDepositAccount account = createRecurringDepositAccount(new BigDecimal("8.00"), true, null, chart);

        assertDoesNotThrow(account::validateApplicableInterestRate);
    }

    @Test
    void testRecurringDepositValidateApplicableInterestRateFailsWhenRateChartNotOverriddenAndNoMatchingSlab() {
        DepositAccountInterestRateChart chart = mock(DepositAccountInterestRateChart.class);
        when(chart.getFromDate()).thenReturn(LocalDate.of(2024, 1, 1));
        when(chart.getApplicableInterestRate(any(), any(), any(), any())).thenReturn(BigDecimal.ZERO);

        RecurringDepositAccount account = createRecurringDepositAccount(new BigDecimal("8.00"), false, null, chart);

        assertThrows(PlatformApiDataValidationException.class, account::validateApplicableInterestRate);
    }
}
