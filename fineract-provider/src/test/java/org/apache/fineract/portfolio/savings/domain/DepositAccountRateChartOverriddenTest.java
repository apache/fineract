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
        FixedDepositAccount account = new FixedDepositAccount();
        ReflectionTestUtils.setField(account, "nominalAnnualInterestRate", new BigDecimal("7.50"));
        ReflectionTestUtils.setField(account, "submittedOnDate", LocalDate.of(2024, 1, 1));

        DepositTermDetail termDetails = DepositTermDetail.createFrom(1, 12, SavingsPeriodFrequencyType.MONTHS,
                SavingsPeriodFrequencyType.MONTHS, 1, SavingsPeriodFrequencyType.MONTHS);
        DepositPreClosureDetail preClosureDetail = DepositPreClosureDetail.createFrom(false, null, null);

        DepositAccountTermAndPreClosure term = DepositAccountTermAndPreClosure.createNew(preClosureDetail, termDetails, account,
                BigDecimal.valueOf(10000), BigDecimal.valueOf(10000), null, 6, SavingsPeriodFrequencyType.MONTHS, null, null, false, null,
                true);
        ReflectionTestUtils.setField(account, "accountTermAndPreClosure", term);

        DepositAccountInterestRateChart chart = mock(DepositAccountInterestRateChart.class);
        when(chart.getApplicableInterestRate(any(), any(), any(), any())).thenReturn(new BigDecimal("5.00"));
        ReflectionTestUtils.setField(account, "chart", chart);

        // When isRateChartOverridden is true, effective rate should be customNominalRate / 100 = 0.075
        BigDecimal effectiveRate = account.getEffectiveInterestRateAsFraction(MathContext.DECIMAL64, DateUtils.getBusinessLocalDate(),
                false);
        assertEquals(0, new BigDecimal("0.075").compareTo(effectiveRate));
    }

    @Test
    void testFixedDepositEffectiveInterestRateUsesChartWhenNotOverridden() {
        FixedDepositAccount account = new FixedDepositAccount();
        ReflectionTestUtils.setField(account, "nominalAnnualInterestRate", new BigDecimal("7.50"));
        ReflectionTestUtils.setField(account, "submittedOnDate", LocalDate.of(2024, 1, 1));

        DepositTermDetail termDetails = DepositTermDetail.createFrom(1, 12, SavingsPeriodFrequencyType.MONTHS,
                SavingsPeriodFrequencyType.MONTHS, 1, SavingsPeriodFrequencyType.MONTHS);
        DepositPreClosureDetail preClosureDetail = DepositPreClosureDetail.createFrom(false, null, null);

        // isRateChartOverridden = false
        DepositAccountTermAndPreClosure term = DepositAccountTermAndPreClosure.createNew(preClosureDetail, termDetails, account,
                BigDecimal.valueOf(10000), BigDecimal.valueOf(10000), null, 6, SavingsPeriodFrequencyType.MONTHS, null, null, false, null,
                false);
        ReflectionTestUtils.setField(account, "accountTermAndPreClosure", term);

        DepositAccountInterestRateChart chart = mock(DepositAccountInterestRateChart.class);
        when(chart.getApplicableInterestRate(any(), any(), any(), any())).thenReturn(new BigDecimal("5.00"));
        ReflectionTestUtils.setField(account, "chart", chart);

        // When isRateChartOverridden is false, effective rate comes from chart (5.00 / 100 = 0.05)
        BigDecimal effectiveRate = account.getEffectiveInterestRateAsFraction(MathContext.DECIMAL64, DateUtils.getBusinessLocalDate(),
                false);
        assertEquals(0, new BigDecimal("0.05").compareTo(effectiveRate));
    }

    @Test
    void testFixedDepositEffectiveInterestRateWithOverriddenRateAndPrematureClosurePenalty() {
        FixedDepositAccount account = new FixedDepositAccount();
        ReflectionTestUtils.setField(account, "nominalAnnualInterestRate", new BigDecimal("7.50"));
        ReflectionTestUtils.setField(account, "submittedOnDate", LocalDate.of(2024, 1, 1));

        DepositTermDetail termDetails = DepositTermDetail.createFrom(1, 12, SavingsPeriodFrequencyType.MONTHS,
                SavingsPeriodFrequencyType.MONTHS, 1, SavingsPeriodFrequencyType.MONTHS);
        // Pre-closure penalty of 1.0%
        DepositPreClosureDetail preClosureDetail = DepositPreClosureDetail.createFrom(true, new BigDecimal("1.00"),
                PreClosurePenalInterestOnType.WHOLE_TERM);

        DepositAccountTermAndPreClosure term = DepositAccountTermAndPreClosure.createNew(preClosureDetail, termDetails, account,
                BigDecimal.valueOf(10000), BigDecimal.valueOf(10000), null, 6, SavingsPeriodFrequencyType.MONTHS, null, null, false, null,
                true);
        ReflectionTestUtils.setField(account, "accountTermAndPreClosure", term);

        DepositAccountInterestRateChart chart = mock(DepositAccountInterestRateChart.class);
        ReflectionTestUtils.setField(account, "chart", chart);

        // Premature close: effective rate is (7.50 - 1.00) / 100 = 0.065
        LocalDate prematureCloseDate = DateUtils.getBusinessLocalDate().minusMonths(1);
        BigDecimal effectiveRate = account.getEffectiveInterestRateAsFraction(MathContext.DECIMAL64, prematureCloseDate, true);
        assertEquals(0, new BigDecimal("0.065").compareTo(effectiveRate));
    }

    @Test
    void testRecurringDepositEffectiveInterestRateUsesOverriddenRateInsteadOfChart() {
        RecurringDepositAccount account = new RecurringDepositAccount();
        ReflectionTestUtils.setField(account, "nominalAnnualInterestRate", new BigDecimal("8.00"));
        ReflectionTestUtils.setField(account, "submittedOnDate", LocalDate.of(2024, 1, 1));

        DepositTermDetail termDetails = DepositTermDetail.createFrom(1, 12, SavingsPeriodFrequencyType.MONTHS,
                SavingsPeriodFrequencyType.MONTHS, 1, SavingsPeriodFrequencyType.MONTHS);
        DepositPreClosureDetail preClosureDetail = DepositPreClosureDetail.createFrom(false, null, null);

        DepositAccountTermAndPreClosure term = DepositAccountTermAndPreClosure.createNew(preClosureDetail, termDetails, account,
                BigDecimal.valueOf(10000), BigDecimal.valueOf(10000), null, 6, SavingsPeriodFrequencyType.MONTHS, null, null, false, null,
                true);
        ReflectionTestUtils.setField(account, "accountTermAndPreClosure", term);

        DepositAccountInterestRateChart chart = mock(DepositAccountInterestRateChart.class);
        when(chart.getApplicableInterestRate(any(), any(), any(), any())).thenReturn(new BigDecimal("6.00"));
        ReflectionTestUtils.setField(account, "chart", chart);

        // When isRateChartOverridden is true, effective rate should be customNominalRate / 100 = 0.08
        BigDecimal effectiveRate = account.getEffectiveInterestRateAsFraction(MathContext.DECIMAL64, DateUtils.getBusinessLocalDate(),
                false);
        assertEquals(0, new BigDecimal("0.08").compareTo(effectiveRate));
    }

    @Test
    void testRecurringDepositEffectiveInterestRateWithOverriddenRateAndPrematureClosurePenalty() {
        RecurringDepositAccount account = new RecurringDepositAccount();
        ReflectionTestUtils.setField(account, "nominalAnnualInterestRate", new BigDecimal("8.00"));
        ReflectionTestUtils.setField(account, "submittedOnDate", LocalDate.of(2024, 1, 1));

        DepositTermDetail termDetails = DepositTermDetail.createFrom(1, 12, SavingsPeriodFrequencyType.MONTHS,
                SavingsPeriodFrequencyType.MONTHS, 1, SavingsPeriodFrequencyType.MONTHS);
        // Pre-closure penalty of 1.5%
        DepositPreClosureDetail preClosureDetail = DepositPreClosureDetail.createFrom(true, new BigDecimal("1.50"),
                PreClosurePenalInterestOnType.WHOLE_TERM);

        DepositAccountTermAndPreClosure term = DepositAccountTermAndPreClosure.createNew(preClosureDetail, termDetails, account,
                BigDecimal.valueOf(10000), BigDecimal.valueOf(10000), null, 6, SavingsPeriodFrequencyType.MONTHS, null, null, false, null,
                true);
        ReflectionTestUtils.setField(account, "accountTermAndPreClosure", term);

        DepositAccountInterestRateChart chart = mock(DepositAccountInterestRateChart.class);
        ReflectionTestUtils.setField(account, "chart", chart);

        // Premature close: effective rate is (8.00 - 1.50) / 100 = 0.065
        LocalDate prematureCloseDate = DateUtils.getBusinessLocalDate().minusMonths(1);
        BigDecimal effectiveRate = account.getEffectiveInterestRateAsFraction(MathContext.DECIMAL64, prematureCloseDate, true);
        assertEquals(0, new BigDecimal("0.065").compareTo(effectiveRate));
    }

    @Test
    void testValidationFailsWhenRateChartOverriddenTrueWithoutNominalAnnualInterestRate() {
        FromJsonHelper fromJsonHelper = new FromJsonHelper();
        DepositProductDataValidator productValidator = mock(DepositProductDataValidator.class);
        DepositAccountDataValidator validator = new DepositAccountDataValidator(fromJsonHelper, productValidator);

        // JSON payload with isRateChartOverridden = true but no nominalAnnualInterestRate
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

        // Should not throw validation exception
        validator.validateFixedDepositForSubmit(validJson);
    }

    @Test
    void testValidationFailsWhenRateChartOverriddenTrueWithoutNominalAnnualInterestRateOnUpdate() {
        FromJsonHelper fromJsonHelper = new FromJsonHelper();
        DepositProductDataValidator productValidator = mock(DepositProductDataValidator.class);
        DepositAccountDataValidator validator = new DepositAccountDataValidator(fromJsonHelper, productValidator);

        String invalidUpdateJson = """
                {
                    "isRateChartOverridden": true
                }
                """;

        assertThrows(PlatformApiDataValidationException.class, () -> validator.validateFixedDepositForUpdate(invalidUpdateJson));
    }

    @Test
    void testFixedDepositValidateDomainRulesSucceedsWhenRateChartOverriddenEvenIfNoMatchingSlab() {
        FixedDepositAccount account = new FixedDepositAccount();
        ReflectionTestUtils.setField(account, "nominalAnnualInterestRate", new BigDecimal("7.50"));
        ReflectionTestUtils.setField(account, "submittedOnDate", LocalDate.of(2024, 1, 1));
        ReflectionTestUtils.setField(account, "interestPostingPeriodType", SavingsPostingInterestPeriodType.MONTHLY.getValue());
        ReflectionTestUtils.setField(account, "interestCompoundingPeriodType", SavingsCompoundingInterestPeriodType.MONTHLY.getValue());

        DepositTermDetail termDetails = DepositTermDetail.createFrom(1, 12, SavingsPeriodFrequencyType.MONTHS,
                SavingsPeriodFrequencyType.MONTHS, 1, SavingsPeriodFrequencyType.MONTHS);
        DepositPreClosureDetail preClosureDetail = DepositPreClosureDetail.createFrom(false, null, null);

        DepositAccountTermAndPreClosure term = DepositAccountTermAndPreClosure.createNew(preClosureDetail, termDetails, account,
                BigDecimal.valueOf(10000), BigDecimal.valueOf(10000), null, 6, SavingsPeriodFrequencyType.MONTHS, null, null, false, null,
                true);
        ReflectionTestUtils.setField(account, "accountTermAndPreClosure", term);

        DepositAccountInterestRateChart chart = mock(DepositAccountInterestRateChart.class);
        when(chart.getFromDate()).thenReturn(LocalDate.of(2024, 1, 1));
        when(chart.getApplicableInterestRate(any(), any(), any(), any())).thenReturn(BigDecimal.ZERO);
        ReflectionTestUtils.setField(account, "chart", chart);

        // Even though chart returns ZERO (no slab matched), domain rules should succeed because chart is overridden
        assertDoesNotThrow(account::validateDomainRules);
    }

    @Test
    void testFixedDepositValidateDomainRulesFailsWhenRateChartNotOverriddenAndNoMatchingSlab() {
        FixedDepositAccount account = new FixedDepositAccount();
        ReflectionTestUtils.setField(account, "nominalAnnualInterestRate", new BigDecimal("7.50"));
        ReflectionTestUtils.setField(account, "submittedOnDate", LocalDate.of(2024, 1, 1));
        ReflectionTestUtils.setField(account, "interestPostingPeriodType", SavingsPostingInterestPeriodType.MONTHLY.getValue());
        ReflectionTestUtils.setField(account, "interestCompoundingPeriodType", SavingsCompoundingInterestPeriodType.MONTHLY.getValue());

        DepositTermDetail termDetails = DepositTermDetail.createFrom(1, 12, SavingsPeriodFrequencyType.MONTHS,
                SavingsPeriodFrequencyType.MONTHS, 1, SavingsPeriodFrequencyType.MONTHS);
        DepositPreClosureDetail preClosureDetail = DepositPreClosureDetail.createFrom(false, null, null);

        // isRateChartOverridden = false
        DepositAccountTermAndPreClosure term = DepositAccountTermAndPreClosure.createNew(preClosureDetail, termDetails, account,
                BigDecimal.valueOf(10000), BigDecimal.valueOf(10000), null, 6, SavingsPeriodFrequencyType.MONTHS, null, null, false, null,
                false);
        ReflectionTestUtils.setField(account, "accountTermAndPreClosure", term);

        DepositAccountInterestRateChart chart = mock(DepositAccountInterestRateChart.class);
        when(chart.getFromDate()).thenReturn(LocalDate.of(2024, 1, 1));
        when(chart.getApplicableInterestRate(any(), any(), any(), any())).thenReturn(BigDecimal.ZERO);
        ReflectionTestUtils.setField(account, "chart", chart);

        // When not overridden and no slab matches (returns ZERO), validation must fail
        assertThrows(PlatformApiDataValidationException.class, account::validateDomainRules);
    }

    @Test
    void testRecurringDepositValidateApplicableInterestRateSucceedsWhenRateChartOverriddenEvenIfNoMatchingSlab() {
        RecurringDepositAccount account = new RecurringDepositAccount();
        ReflectionTestUtils.setField(account, "nominalAnnualInterestRate", new BigDecimal("8.00"));
        ReflectionTestUtils.setField(account, "submittedOnDate", LocalDate.of(2024, 1, 1));

        DepositTermDetail termDetails = DepositTermDetail.createFrom(1, 12, SavingsPeriodFrequencyType.MONTHS,
                SavingsPeriodFrequencyType.MONTHS, 1, SavingsPeriodFrequencyType.MONTHS);
        DepositPreClosureDetail preClosureDetail = DepositPreClosureDetail.createFrom(false, null, null);

        DepositAccountTermAndPreClosure term = DepositAccountTermAndPreClosure.createNew(preClosureDetail, termDetails, account,
                BigDecimal.valueOf(10000), BigDecimal.valueOf(10000), null, 6, SavingsPeriodFrequencyType.MONTHS, null, null, false, null,
                true);
        ReflectionTestUtils.setField(account, "accountTermAndPreClosure", term);

        DepositAccountInterestRateChart chart = mock(DepositAccountInterestRateChart.class);
        when(chart.getFromDate()).thenReturn(LocalDate.of(2024, 1, 1));
        when(chart.getApplicableInterestRate(any(), any(), any(), any())).thenReturn(BigDecimal.ZERO);
        ReflectionTestUtils.setField(account, "chart", chart);

        // Even though chart returns ZERO, validation should succeed because chart is overridden
        assertDoesNotThrow(account::validateApplicableInterestRate);
    }

    @Test
    void testRecurringDepositValidateApplicableInterestRateFailsWhenRateChartNotOverriddenAndNoMatchingSlab() {
        RecurringDepositAccount account = new RecurringDepositAccount();
        ReflectionTestUtils.setField(account, "nominalAnnualInterestRate", new BigDecimal("8.00"));
        ReflectionTestUtils.setField(account, "submittedOnDate", LocalDate.of(2024, 1, 1));

        DepositTermDetail termDetails = DepositTermDetail.createFrom(1, 12, SavingsPeriodFrequencyType.MONTHS,
                SavingsPeriodFrequencyType.MONTHS, 1, SavingsPeriodFrequencyType.MONTHS);
        DepositPreClosureDetail preClosureDetail = DepositPreClosureDetail.createFrom(false, null, null);

        // isRateChartOverridden = false
        DepositAccountTermAndPreClosure term = DepositAccountTermAndPreClosure.createNew(preClosureDetail, termDetails, account,
                BigDecimal.valueOf(10000), BigDecimal.valueOf(10000), null, 6, SavingsPeriodFrequencyType.MONTHS, null, null, false, null,
                false);
        ReflectionTestUtils.setField(account, "accountTermAndPreClosure", term);

        DepositAccountInterestRateChart chart = mock(DepositAccountInterestRateChart.class);
        when(chart.getFromDate()).thenReturn(LocalDate.of(2024, 1, 1));
        when(chart.getApplicableInterestRate(any(), any(), any(), any())).thenReturn(BigDecimal.ZERO);
        ReflectionTestUtils.setField(account, "chart", chart);

        // When not overridden and no slab matches (returns ZERO), validation must fail
        assertThrows(PlatformApiDataValidationException.class, account::validateApplicableInterestRate);
    }
}
