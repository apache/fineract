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
package org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearCustomStrategyType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.domain.reaging.LoanReAgeParameter;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanApplicationTerms;
import org.apache.fineract.portfolio.loanaccount.service.schedule.LoanScheduleComponent;
import org.apache.fineract.portfolio.loanproduct.calc.EMICalculator;
import org.apache.fineract.portfolio.loanproduct.calc.data.ProgressiveLoanInterestScheduleModel;
import org.apache.fineract.portfolio.loanproduct.domain.ILoanConfigurationDetails;
import org.apache.fineract.portfolio.loanproduct.domain.InterestMethod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * The day count settings can be overridden per loan, so re-aging has to build its terms from the loan and not from the
 * loan product.
 */
class AdvancedPaymentScheduleReAgeLoanTermsTest {

    private static final MonetaryCurrency CURRENCY = new MonetaryCurrency("USD", 2, 1);
    private static final MockedStatic<MoneyHelper> MONEY_HELPER = mockStatic(MoneyHelper.class);

    private static final LocalDate DISBURSEMENT_DATE = LocalDate.of(2024, 1, 1);
    private static final LocalDate TRANSACTION_DATE = LocalDate.of(2024, 3, 15);
    private static final LocalDate RE_AGE_START_DATE = LocalDate.of(2024, 4, 1);

    private EMICalculator emiCalculator;
    private AdvancedPaymentScheduleTransactionProcessor underTest;

    @BeforeAll
    static void init() {
        MONEY_HELPER.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
        MONEY_HELPER.when(MoneyHelper::getMathContext).thenReturn(new MathContext(12, RoundingMode.HALF_EVEN));
    }

    @AfterAll
    static void destruct() {
        MONEY_HELPER.close();
    }

    @BeforeEach
    void setUp() {
        emiCalculator = mock(EMICalculator.class);
        underTest = new AdvancedPaymentScheduleTransactionProcessor(emiCalculator, null, null, mock(LoanScheduleComponent.class), null,
                null, null, null, null);

        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, TRANSACTION_DATE)));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void reAgeTermsUseTheDayCountSettingsOfTheLoan() {
        LoanProductRelatedDetail loanDetail = relatedDetail(DaysInMonthType.ACTUAL, DaysInYearType.ACTUAL,
                DaysInYearCustomStrategyType.FEB_29_PERIOD_ONLY, true, true);
        LoanProductRelatedDetail productDetail = relatedDetail(DaysInMonthType.DAYS_30, DaysInYearType.DAYS_360, null, false, false);

        LoanApplicationTerms terms = reAgeTerms(loanDetail, productDetail);
        ILoanConfigurationDetails configuration = terms.toLoanConfigurationDetails();

        assertEquals(DaysInMonthType.ACTUAL.getValue(), configuration.getDaysInMonthType());
        assertEquals(DaysInYearType.ACTUAL.getValue(), configuration.getDaysInYearType());
        assertEquals(DaysInYearCustomStrategyType.FEB_29_PERIOD_ONLY, configuration.getDaysInYearCustomStrategy());
        assertTrue(configuration.isAllowPartialPeriodInterestCalculation());
        assertTrue(isInterestRecognitionOnDisbursementDate(terms));
    }

    @Test
    void reAgeTermsKeepAMissingStrategyOfTheLoan() {
        LoanProductRelatedDetail loanDetail = relatedDetail(DaysInMonthType.DAYS_30, DaysInYearType.DAYS_360, null, false, false);
        LoanProductRelatedDetail productDetail = relatedDetail(DaysInMonthType.ACTUAL, DaysInYearType.ACTUAL,
                DaysInYearCustomStrategyType.FEB_29_PERIOD_ONLY, true, true);

        LoanApplicationTerms terms = reAgeTerms(loanDetail, productDetail);
        ILoanConfigurationDetails configuration = terms.toLoanConfigurationDetails();

        assertEquals(DaysInMonthType.DAYS_30.getValue(), configuration.getDaysInMonthType());
        assertEquals(DaysInYearType.DAYS_360.getValue(), configuration.getDaysInYearType());
        assertNull(configuration.getDaysInYearCustomStrategy());
        assertFalse(configuration.isAllowPartialPeriodInterestCalculation());
        assertFalse(isInterestRecognitionOnDisbursementDate(terms));
    }

    private LoanApplicationTerms reAgeTerms(LoanProductRelatedDetail loanDetail, LoanProductRelatedDetail productDetail) {
        DaysInMonthType productDaysInMonthType = productDetail.fetchDaysInMonthType();
        DaysInYearType productDaysInYearType = productDetail.fetchDaysInYearType();
        LoanProduct loanProduct = mock(LoanProduct.class);
        when(loanProduct.getLoanProductRelatedDetail()).thenReturn(productDetail);
        when(loanProduct.fetchDaysInMonthType()).thenReturn(productDaysInMonthType);
        when(loanProduct.fetchDaysInYearType()).thenReturn(productDaysInYearType);

        Loan loan = mock(Loan.class);
        when(loan.getLoanProduct()).thenReturn(loanProduct);
        when(loan.getLoanProductRelatedDetail()).thenReturn(loanDetail);
        when(loan.getLoanRepaymentScheduleDetail()).thenReturn(loanDetail);
        when(loan.isInterestBearing()).thenReturn(true);
        when(loan.isInterestBearingAndInterestRecalculationEnabled()).thenReturn(true);
        when(loan.getDisbursementDate()).thenReturn(DISBURSEMENT_DATE);

        LoanReAgeParameter reAgeParameter = mock(LoanReAgeParameter.class);
        when(reAgeParameter.getStartDate()).thenReturn(RE_AGE_START_DATE);
        when(reAgeParameter.getFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        when(reAgeParameter.getFrequencyNumber()).thenReturn(1);
        when(reAgeParameter.getNumberOfInstallments()).thenReturn(6);

        LoanTransaction reAgeTransaction = mock(LoanTransaction.class);
        when(reAgeTransaction.getTypeOf()).thenReturn(LoanTransactionType.REAGE);
        when(reAgeTransaction.getLoan()).thenReturn(loan);
        when(reAgeTransaction.getLoanReAgeParameter()).thenReturn(reAgeParameter);
        when(reAgeTransaction.getTransactionDate()).thenReturn(TRANSACTION_DATE);
        when(reAgeTransaction.getAmount()).thenReturn(BigDecimal.ONE);

        Money outstandingPrincipal = Money.of(CURRENCY, BigDecimal.valueOf(1000));
        ProgressiveLoanInterestScheduleModel model = mock(ProgressiveLoanInterestScheduleModel.class);
        when(model.getTotalOutstandingPrincipal()).thenReturn(outstandingPrincipal);
        when(model.repaymentPeriods()).thenReturn(new ArrayList<>());

        underTest.processLatestTransaction(reAgeTransaction,
                new ProgressiveTransactionCtx(CURRENCY, new ArrayList<>(), Set.of(), null, null, model, List.of()));

        ArgumentCaptor<LoanApplicationTerms> termsCaptor = ArgumentCaptor.forClass(LoanApplicationTerms.class);
        verify(emiCalculator).updateModelRepaymentPeriodsDuringReAge(any(), any(), any(), any(), termsCaptor.capture(), any());
        return termsCaptor.getValue();
    }

    // LoanApplicationTerms has no getter for this flag
    private static boolean isInterestRecognitionOnDisbursementDate(LoanApplicationTerms terms) {
        return (boolean) ReflectionTestUtils.getField(terms, "interestRecognitionOnDisbursementDate");
    }

    private static LoanProductRelatedDetail relatedDetail(DaysInMonthType daysInMonthType, DaysInYearType daysInYearType,
            DaysInYearCustomStrategyType daysInYearCustomStrategy, boolean allowPartialPeriodInterestCalculation,
            boolean interestRecognitionOnDisbursementDate) {
        LoanProductRelatedDetail detail = mock(LoanProductRelatedDetail.class);
        when(detail.fetchDaysInMonthType()).thenReturn(daysInMonthType);
        when(detail.fetchDaysInYearType()).thenReturn(daysInYearType);
        when(detail.getDaysInYearCustomStrategy()).thenReturn(daysInYearCustomStrategy);
        when(detail.isAllowPartialPeriodInterestCalculation()).thenReturn(allowPartialPeriodInterestCalculation);
        when(detail.isInterestRecognitionOnDisbursementDate()).thenReturn(interestRecognitionOnDisbursementDate);
        when(detail.getInterestMethod()).thenReturn(InterestMethod.DECLINING_BALANCE);
        when(detail.getAnnualNominalInterestRate()).thenReturn(BigDecimal.valueOf(12));
        when(detail.getRepaymentPeriodFrequencyType()).thenReturn(PeriodFrequencyType.MONTHS);
        return detail;
    }
}
