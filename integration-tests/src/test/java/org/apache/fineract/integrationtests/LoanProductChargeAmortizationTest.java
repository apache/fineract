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
package org.apache.fineract.integrationtests;

import static org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper.errorCodesOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.ChargeUpdateRequest;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.LoanProductChargeData;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.ChargeRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.junit.jupiter.api.Test;

/**
 * {@code enableChargeAmortization} on the loan product and its accounting mapping validation (charge amortization FR-10
 * to FR-13).
 */
public class LoanProductChargeAmortizationTest extends FeignLoanTestBase {

    // USD is the organisation currency on every test database; charges in other currencies are not listed
    private static final String CURRENCY = "USD";
    private static final double FEE_AMOUNT = 50.0;
    private static final int CASH_BASED = 2;
    private static final int NONE = 1;

    @Test
    public void productWithoutTheFlagDoesNotAmortizeCharges() {
        final Long productId = loanHelper.createLoanProduct(progressiveProduct()).getResourceId();

        assertEquals(Boolean.FALSE, loanHelper.retrieveLoanProduct(productId).getEnableChargeAmortization());
    }

    @Test
    public void progressiveAccrualProductAcceptsAmortizedCharges() {
        final Long chargeId = createAmortizedCharge("INTEREST");

        final Long productId = loanHelper
                .createLoanProduct(progressiveProduct().enableChargeAmortization(true)
                        .deferredIncomeLiabilityAccountId(deferredIncomeLiabilityAccountId()).charges(List.of(productCharge(chargeId))))
                .getResourceId();

        final GetLoanProductsProductIdResponse product = loanHelper.retrieveLoanProduct(productId);
        assertEquals(Boolean.TRUE, product.getEnableChargeAmortization());
        assertEquals(deferredIncomeLiabilityAccountId(), product.getAccountingMappings().getDeferredIncomeLiabilityAccount().getId());
        assertTrue(product.getCharges().stream().anyMatch(charge -> chargeId.equals(charge.getId())));
    }

    @Test
    public void amortizedChargeNeedsTheFlag() {
        final Long chargeId = createAmortizedCharge("FEE");

        assertRejected(400, "validation.msg.loanproduct.charges.charge.amortization.not.enabled",
                loanHelper.createLoanProductExpectingError(progressiveProduct().charges(List.of(productCharge(chargeId)))));
    }

    @Test
    public void flagIsOnlySupportedForProgressiveLoans() {
        assertRejected(400, "validation.msg.loanproduct.enableChargeAmortization.supported.only.for.progressive.loan.charge.amortization",
                loanHelper.createLoanProductExpectingError(fourInstallmentsCumulativeTemplate().enableChargeAmortization(true)
                        .deferredIncomeLiabilityAccountId(deferredIncomeLiabilityAccountId())));
    }

    @Test
    public void flagIsRejectedWithCashBasedAccounting() {
        assertRejected(400, "validation.msg.loanproduct.enableChargeAmortization.not.supported.for.cash.based.accounting",
                loanHelper.createLoanProductExpectingError(
                        progressiveProduct().accountingRule(CASH_BASED).enableAccrualActivityPosting(null).enableChargeAmortization(true)));
    }

    @Test
    public void flagIsAcceptedWithoutAccounting() {
        final Long chargeId = createAmortizedCharge("FEE");

        final Long productId = loanHelper.createLoanProduct(progressiveProduct().accountingRule(NONE).enableAccrualActivityPosting(null)
                .enableChargeAmortization(true).charges(List.of(productCharge(chargeId)))).getResourceId();

        assertEquals(Boolean.TRUE, loanHelper.retrieveLoanProduct(productId).getEnableChargeAmortization());
    }

    @Test
    public void accrualAccountingNeedsDeferredIncomeLiabilityMapping() {
        final CallFailedRuntimeException failure = loanHelper
                .createLoanProductExpectingError(progressiveProduct().enableChargeAmortization(true));

        assertEquals(400, failure.getStatus());
        assertTrue(errorCodesOf(failure).contains("validation.msg.loanproduct.deferredIncomeLiabilityAccountId.cannot.be.blank"),
                () -> "unexpected errors " + errorCodesOf(failure));
    }

    @Test
    public void flagCanBeEnabledOnUpdateOnlyWithDeferredIncomeLiabilityMapping() {
        final Long productId = loanHelper.createLoanProduct(progressiveProduct()).getResourceId();

        assertRejected(400, "validation.msg.loanproduct.deferredIncomeLiabilityAccountId.required.when.charge.amortization.enabled",
                loanHelper.updateLoanProductExpectingError(productId, update().enableChargeAmortization(true)));
        assertEquals(Boolean.FALSE, loanHelper.retrieveLoanProduct(productId).getEnableChargeAmortization());

        loanHelper.updateLoanProduct(productId,
                update().enableChargeAmortization(true).deferredIncomeLiabilityAccountId(deferredIncomeLiabilityAccountId()));
        // an update that does not touch the mappings keeps the deferred income liability mapping
        loanHelper.updateLoanProduct(productId, update().description("charge amortization enabled"));

        final GetLoanProductsProductIdResponse product = loanHelper.retrieveLoanProduct(productId);
        assertEquals(Boolean.TRUE, product.getEnableChargeAmortization());
        assertNotNull(product.getAccountingMappings().getDeferredIncomeLiabilityAccount());
        assertEquals(deferredIncomeLiabilityAccountId(), product.getAccountingMappings().getDeferredIncomeLiabilityAccount().getId());
    }

    @Test
    public void flagCannotBeDisabledWhileAmortizedChargesAreAttached() {
        final Long chargeId = createAmortizedCharge("FEE");
        final Long productId = loanHelper
                .createLoanProduct(progressiveProduct().enableChargeAmortization(true)
                        .deferredIncomeLiabilityAccountId(deferredIncomeLiabilityAccountId()).charges(List.of(productCharge(chargeId))))
                .getResourceId();

        assertRejected(400, "validation.msg.loanproduct.charges.charge.amortization.not.enabled",
                loanHelper.updateLoanProductExpectingError(productId, update().enableChargeAmortization(false)));
        assertEquals(Boolean.TRUE, loanHelper.retrieveLoanProduct(productId).getEnableChargeAmortization());
    }

    @Test
    public void flagCannotBeTurnedToCashBasedAccountingOnUpdate() {
        final Long productId = loanHelper.createLoanProduct(
                progressiveProduct().enableChargeAmortization(true).deferredIncomeLiabilityAccountId(deferredIncomeLiabilityAccountId()))
                .getResourceId();

        assertRejected(400, "validation.msg.loanproduct.enableChargeAmortization.not.supported.for.cash.based.accounting", loanHelper
                .updateLoanProductExpectingError(productId, update().accountingRule(CASH_BASED).enableAccrualActivityPosting(false)));
    }

    @Test
    public void attachedChargeCannotBecomeAmortizedWithoutTheFlag() {
        final Long chargeId = chargesHelper.createCharge(ChargeRequestBuilders.loanDisbursementFee(FEE_AMOUNT, CURRENCY)).getResourceId();
        loanHelper.createLoanProduct(progressiveProduct().charges(List.of(productCharge(chargeId))));

        assertRejected(403, "error.msg.charge.amortization.requires.charge.amortization.loan.product",
                chargesHelper.updateChargeExpectingError(chargeId,
                        new ChargeUpdateRequest().locale(LoanTestData.LOCALE).amortizationPeriodType("FULL_LOAN_TERM")));
        assertEquals("NONE", chargesHelper.getCharge(chargeId).getAmortizationPeriodType().getId());
    }

    private PostLoanProductsRequest progressiveProduct() {
        return fourInstallmentsProgressiveWithAdvancedAllocation().currencyCode(CURRENCY);
    }

    private static PutLoanProductsProductIdRequest update() {
        return new PutLoanProductsProductIdRequest().locale(LoanTestData.LOCALE);
    }

    private Long createAmortizedCharge(final String incomeType) {
        return chargesHelper.createCharge(ChargeRequestBuilders.loanDisbursementFee(FEE_AMOUNT, CURRENCY)
                .amortizationPeriodType("FULL_LOAN_TERM").amortizationIncomeType(incomeType)).getResourceId();
    }

    private static LoanProductChargeData productCharge(final Long chargeId) {
        return new LoanProductChargeData().id(chargeId);
    }

    private Long deferredIncomeLiabilityAccountId() {
        return getLiabilityAccountId("deferredIncomeLiability");
    }

    private static void assertRejected(final int expectedStatus, final String expectedCode, final CallFailedRuntimeException failure) {
        assertEquals(expectedStatus, failure.getStatus(), () -> "unexpected response " + failure.getResponseBody());
        final List<String> codes = errorCodesOf(failure);
        assertFalse(codes.isEmpty(), () -> "no error code in " + failure.getResponseBody());
        assertEquals(List.of(expectedCode), codes);
    }
}
