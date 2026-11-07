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
package org.apache.fineract.portfolio.loanproduct.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.apache.fineract.accounting.common.AccountingConstants.AccrualAccountsForLoan;
import org.apache.fineract.accounting.common.AccountingRuleType;
import org.apache.fineract.accounting.producttoaccountmapping.service.ProductToGLAccountMappingHelper;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.PortfolioProductType;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeAmortizationIncomeType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import org.apache.fineract.portfolio.loanproduct.domain.AdvancedPaymentAllocationsJsonParser;
import org.apache.fineract.portfolio.loanproduct.domain.AdvancedPaymentAllocationsValidator;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Rules of {@link LoanProductDataValidator#validateChargeAmortization(LoanProduct)} (charge amortization FR-10 and
 * FR-13).
 */
@ExtendWith(MockitoExtension.class)
public class LoanProductChargeAmortizationValidationTest {

    private static final Long PRODUCT_ID = 7L;

    @Mock
    private LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory;
    @Mock
    private AdvancedPaymentAllocationsJsonParser advancedPaymentAllocationsJsonParser;
    @Mock
    private AdvancedPaymentAllocationsValidator advancedPaymentAllocationsValidator;
    @Mock
    private ProductToGLAccountMappingHelper productToGLAccountMappingHelper;
    @Mock
    private LoanProduct loanProduct;
    @Mock
    private LoanProductRelatedDetail loanProductRelatedDetail;

    private LoanProductDataValidator underTest;

    @BeforeEach
    public void setUp() {
        underTest = new LoanProductDataValidator(new FromJsonHelper(), loanRepaymentScheduleTransactionProcessorFactory,
                advancedPaymentAllocationsJsonParser, advancedPaymentAllocationsValidator, productToGLAccountMappingHelper);
        when(loanProduct.getLoanProductRelatedDetail()).thenReturn(loanProductRelatedDetail);
        lenient().when(loanProduct.getId()).thenReturn(PRODUCT_ID);
    }

    @Test
    public void productWithoutAmortizedChargesNeedsNoFlag() {
        givenProduct(false, AccountingRuleType.ACCRUAL_PERIODIC, regularCharge());

        underTest.validateChargeAmortization(loanProduct);

        verify(productToGLAccountMappingHelper, never()).hasCoreProductToAccountMapping(anyLong(), eq(PortfolioProductType.LOAN), anyInt());
    }

    @Test
    public void amortizedChargeIsRejectedWhenTheFlagIsOff() {
        givenProduct(false, AccountingRuleType.NONE, regularCharge(), amortizedCharge(11L, ChargeAmortizationIncomeType.FEE));

        final ApiParameterError error = singleError(() -> underTest.validateChargeAmortization(loanProduct));

        assertEquals("charges", error.getParameterName());
        assertEquals("validation.msg.loanproduct.charges.charge.amortization.not.enabled", error.getUserMessageGlobalisationCode());
        assertTrue(error.getDefaultUserMessage().contains("[11]"), error::getDefaultUserMessage);
    }

    @Test
    public void noneAccountingNeedsNoMappings() {
        givenProduct(true, AccountingRuleType.NONE, amortizedCharge(11L, ChargeAmortizationIncomeType.INTEREST));

        underTest.validateChargeAmortization(loanProduct);

        verify(productToGLAccountMappingHelper, never()).hasCoreProductToAccountMapping(anyLong(), eq(PortfolioProductType.LOAN), anyInt());
    }

    @Test
    public void accrualWithFeeIncomeNeedsFeeMappingsOnly() {
        givenProduct(true, AccountingRuleType.ACCRUAL_UPFRONT, amortizedCharge(11L, ChargeAmortizationIncomeType.FEE));
        givenMappings(AccrualAccountsForLoan.FEES_RECEIVABLE, AccrualAccountsForLoan.DEFERRED_INCOME_LIABILITY,
                AccrualAccountsForLoan.INCOME_FROM_FEES);

        underTest.validateChargeAmortization(loanProduct);

        verify(productToGLAccountMappingHelper, never()).hasCoreProductToAccountMapping(PRODUCT_ID, PortfolioProductType.LOAN,
                AccrualAccountsForLoan.INTEREST_ON_LOANS.getValue());
    }

    @Test
    public void accrualWithoutDeferredIncomeLiabilityIsRejected() {
        givenProduct(true, AccountingRuleType.ACCRUAL_PERIODIC);
        givenMappings(AccrualAccountsForLoan.FEES_RECEIVABLE, AccrualAccountsForLoan.INCOME_FROM_FEES);

        final ApiParameterError error = singleError(() -> underTest.validateChargeAmortization(loanProduct));

        assertEquals("deferredIncomeLiabilityAccountId", error.getParameterName());
        assertEquals("validation.msg.loanproduct.deferredIncomeLiabilityAccountId.required.when.charge.amortization.enabled",
                error.getUserMessageGlobalisationCode());
    }

    @Test
    public void interestIncomeTypeNeedsInterestIncomeMapping() {
        givenProduct(true, AccountingRuleType.ACCRUAL_PERIODIC, amortizedCharge(11L, ChargeAmortizationIncomeType.FEE),
                amortizedCharge(12L, ChargeAmortizationIncomeType.INTEREST));
        givenMappings(AccrualAccountsForLoan.FEES_RECEIVABLE, AccrualAccountsForLoan.DEFERRED_INCOME_LIABILITY,
                AccrualAccountsForLoan.INCOME_FROM_FEES);

        final ApiParameterError error = singleError(() -> underTest.validateChargeAmortization(loanProduct));

        assertEquals("interestOnLoanAccountId", error.getParameterName());
        assertEquals("validation.msg.loanproduct.interestOnLoanAccountId.required.when.charge.amortization.enabled",
                error.getUserMessageGlobalisationCode());
    }

    private void givenProduct(final boolean enableChargeAmortization, final AccountingRuleType accountingRule, final Charge... charges) {
        when(loanProductRelatedDetail.isEnableChargeAmortization()).thenReturn(enableChargeAmortization);
        lenient().when(loanProduct.getAccountingRule()).thenReturn(accountingRule);
        when(loanProduct.getCharges()).thenReturn(List.of(charges));
    }

    private void givenMappings(final AccrualAccountsForLoan... mappedAccounts) {
        lenient().when(productToGLAccountMappingHelper.hasCoreProductToAccountMapping(anyLong(), eq(PortfolioProductType.LOAN), anyInt()))
                .thenReturn(false);
        for (AccrualAccountsForLoan account : mappedAccounts) {
            when(productToGLAccountMappingHelper.hasCoreProductToAccountMapping(PRODUCT_ID, PortfolioProductType.LOAN, account.getValue()))
                    .thenReturn(true);
        }
    }

    private static Charge regularCharge() {
        final Charge charge = mock(Charge.class);
        when(charge.isAmortized()).thenReturn(false);
        return charge;
    }

    private static Charge amortizedCharge(final Long id, final ChargeAmortizationIncomeType incomeType) {
        final Charge charge = mock(Charge.class);
        when(charge.isAmortized()).thenReturn(true);
        lenient().when(charge.getId()).thenReturn(id);
        lenient().when(charge.getAmortizationIncomeType()).thenReturn(incomeType);
        return charge;
    }

    private static ApiParameterError singleError(final org.junit.jupiter.api.function.Executable executable) {
        final PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class, executable);
        assertEquals(1, exception.getErrors().size(), () -> "expected one error, got " + exception.getErrors());
        return exception.getErrors().get(0);
    }
}
