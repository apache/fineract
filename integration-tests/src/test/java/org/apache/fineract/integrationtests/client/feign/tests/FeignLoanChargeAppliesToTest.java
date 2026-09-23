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
package org.apache.fineract.integrationtests.client.feign.tests;

import static org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper.errorCodesOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.LoanProductChargeData;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansRequestChargeData;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignChargesHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignLoanHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ChargeRequestBuilders;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * A Term Loan product and a Term Loan account only accept charge definitions whose {@code chargeAppliesTo} is Loan.
 *
 * <p>
 * Every rejected case below uses a Client charge that is a Specified-due-date, Flat charge in the product currency, so
 * neither the currency rule nor the charge time type can account for the rejection: only the {@code chargeAppliesTo}
 * guard can.
 */
public class FeignLoanChargeAppliesToTest extends FeignIntegrationTest {

    private static final String APPLIES_TO_LOAN_PRODUCT_ERROR = "error.msg.charge.cannot.be.applied.toloan.product";
    private static final String APPLIES_TO_LOAN_ACCOUNT_ERROR = "error.msg.charge.cannot.be.applied.toloan";

    private static final double CHARGE_AMOUNT = 20.0;

    private FeignChargesHelper chargesHelper;
    private FeignLoanHelper loanHelper;
    private FeignClientHelper clientHelper;

    @BeforeAll
    void setupHelpers() {
        chargesHelper = new FeignChargesHelper(fineractClient());
        loanHelper = new FeignLoanHelper(fineractClient());
        clientHelper = new FeignClientHelper(fineractClient());
    }

    @Test
    void loanProductWithAClientCharge_isRejected() {
        final Long clientChargeId = createClientCharge();

        final CallFailedRuntimeException failure = loanHelper.createLoanProductExpectingError(
                simpleLoanProductRequest().charges(List.of(new LoanProductChargeData().id(clientChargeId))));

        assertEquals(403, failure.getStatus(), "a domain-rule rejection is reported as 403");
        assertEquals(List.of(APPLIES_TO_LOAN_PRODUCT_ERROR), errorCodesOf(failure),
                "the product catalogue must reject a charge that is not defined for loans");
    }

    @Test
    void loanProductWithALoanCharge_isAccepted() {
        final Long loanChargeId = createLoanCharge();

        final Long productId = loanHelper
                .createLoanProduct(simpleLoanProductRequest().charges(List.of(new LoanProductChargeData().id(loanChargeId))))
                .getResourceId();

        assertNotNull(productId, "a Loan charge must still be catalogued without complaint");
    }

    @Test
    void loanApplicationWithAClientCharge_isRejected() {
        final Long clientChargeId = createClientCharge();
        final Long productId = loanHelper.createSimpleLoanProduct().getResourceId();
        final Long clientId = clientHelper.createClient();
        final String today = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());

        final PostLoansRequest request = simpleLoanApplication(clientId, productId, today).charges(List
                .of(new PostLoansRequestChargeData().chargeId(clientChargeId).amount(BigDecimal.valueOf(CHARGE_AMOUNT)).dueDate(today)));

        final CallFailedRuntimeException failure = loanHelper.applyForLoanExpectingError(request);

        assertEquals(403, failure.getStatus(), "a domain-rule rejection is reported as 403");
        assertEquals(List.of(APPLIES_TO_LOAN_ACCOUNT_ERROR), errorCodesOf(failure),
                "submitting a loan application must reject a charge that is not defined for loans");
    }

    @Test
    void addingAClientChargeToAnActiveLoan_isRejected() {
        final Long clientChargeId = createClientCharge();
        final Long productId = loanHelper.createSimpleLoanProduct().getResourceId();
        final Long clientId = clientHelper.createClient();
        final String today = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());
        final Long loanId = loanHelper.applyAndApproveLoan(clientId, productId, today, 10000.0, 12).getLoanId();
        loanHelper.disburseLoan(today, loanId, "10000");

        final CallFailedRuntimeException failure = loanHelper.addLoanChargeExpectingError(loanId, new PostLoansLoanIdChargesRequest()
                .chargeId(clientChargeId).amount(CHARGE_AMOUNT).dueDate(today).locale("en").dateFormat("dd MMMM yyyy"));

        assertEquals(403, failure.getStatus(), "a domain-rule rejection is reported as 403");
        assertEquals(List.of(APPLIES_TO_LOAN_ACCOUNT_ERROR), errorCodesOf(failure),
                "an active loan must reject a charge that is not defined for loans");
        assertTrue(loanHelper.getLoanCharges(loanId).isEmpty(), "no charge may be persisted on the account");
    }

    private Long createClientCharge() {
        return chargesHelper.createCharge(ChargeRequestBuilders.clientSpecifiedDueDateFee(CHARGE_AMOUNT)).getResourceId();
    }

    private Long createLoanCharge() {
        return chargesHelper.createCharge(ChargeRequestBuilders.loanSpecifiedDueDateFee(CHARGE_AMOUNT)).getResourceId();
    }

    private PostLoanProductsRequest simpleLoanProductRequest() {
        return new PostLoanProductsRequest()//
                .name(Utils.uniqueRandomStringGenerator("Loan_Charge_Applies_To_", 6))//
                .shortName(Utils.uniqueRandomStringGenerator("", 4))//
                .currencyCode("USD")//
                .digitsAfterDecimal(2)//
                .inMultiplesOf(1)//
                .principal(10000.0)//
                .numberOfRepayments(12)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(2L)//
                .interestRatePerPeriod(1.0)//
                .interestRateFrequencyType(2)//
                .amortizationType(1)//
                .interestType(0)//
                .interestCalculationPeriodType(1)//
                .transactionProcessingStrategyCode("mifos-standard-strategy")//
                .daysInYearType(365)//
                .daysInMonthType(30)//
                .isInterestRecalculationEnabled(false)//
                .accountingRule(1)//
                .locale("en")//
                .dateFormat("dd MMMM yyyy");
    }

    private PostLoansRequest simpleLoanApplication(final Long clientId, final Long productId, final String submittedOnDate) {
        return new PostLoansRequest()//
                .clientId(clientId)//
                .productId(productId)//
                .loanType("individual")//
                .submittedOnDate(submittedOnDate)//
                .expectedDisbursementDate(submittedOnDate)//
                .principal(BigDecimal.valueOf(10000.0))//
                .loanTermFrequency(12)//
                .loanTermFrequencyType(2)//
                .numberOfRepayments(12)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(2)//
                .interestRatePerPeriod(BigDecimal.ZERO)//
                .amortizationType(1)//
                .interestType(0)//
                .interestCalculationPeriodType(1)//
                .transactionProcessingStrategyCode("mifos-standard-strategy")//
                .locale("en")//
                .dateFormat("dd MMMM yyyy");
    }
}
