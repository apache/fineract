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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionIdResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoansChargeData;
import org.apache.fineract.client.models.PostWorkingCapitalLoansRequest;
import org.apache.fineract.client.models.PutWorkingCapitalLoansLoanIdRequest;
import org.apache.fineract.client.models.WorkingCapitalLoanChargeData;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessDateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignChargesHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ChargeRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalLoanRequestBuilders;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Charges sent with the Working Capital loan application (submit and modify). They follow the same rules as the account
 * charge endpoint for a loan pending approval - so only disbursement charges pass - and are exactly what the
 * disbursement settles: the product catalogue is a default for the client application, never inherited (term-loan
 * model).
 */
public class FeignWorkingCapitalLoanApplicationChargesTest extends FeignIntegrationTest {

    private static final String REPAYMENT_AT_DISBURSEMENT_CODE = "loanTransactionType.repaymentAtDisbursement";
    private static final String LOAN_SHOULD_BE_ACTIVE = "loan.should.be.active";
    private static final String DUE_DATE_NOT_ALLOWED = "dueDate.not.allowed.for.disbursement.charge";
    private static final String NOT_A_WC_CHARGE = "error.msg.charge.cannot.be.applied.toworking.capital.loan";
    private static final String LOAN_CHARGE_NOT_FOUND = "error.msg.wc.loan.charge.id.invalid";
    private static final int CALCULATION_PERCENT_OF_AMOUNT = 2;

    private static final String BUSINESS_DATE = "2026-01-01";
    private static final String LOAN_DATE = "01 January 2026";
    private static final BigDecimal PRINCIPAL = BigDecimal.valueOf(9000);
    private static final BigDecimal PERIOD_PAYMENT_RATE = BigDecimal.valueOf(18);

    private FeignChargesHelper chargesHelper;
    private FeignWorkingCapitalLoanHelper wcLoanHelper;
    private FeignClientHelper clientHelper;
    private FeignBusinessDateHelper businessDateHelper;
    private WorkingCapitalLoanProductHelper productHelper;

    @BeforeAll
    void setupHelpers() {
        final var feignClient = fineractClient();
        chargesHelper = new FeignChargesHelper(feignClient);
        wcLoanHelper = new FeignWorkingCapitalLoanHelper(feignClient);
        clientHelper = new FeignClientHelper(feignClient);
        businessDateHelper = new FeignBusinessDateHelper(feignClient);
        productHelper = new WorkingCapitalLoanProductHelper();
    }

    @Test
    void chargesSentWithTheApplicationAreSettledAtDisbursement() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final Long flatFeeId = createCharge(WorkingCapitalLoanRequestBuilders.disbursementCharge(false, 100.0));
            final Long percentFeeId = createCharge(
                    WorkingCapitalLoanRequestBuilders.disbursementCharge(CALCULATION_PERCENT_OF_AMOUNT, false, 5.0));
            final Long productId = createProduct(List.of());

            final Long loanId = wcLoanHelper.submitApplication(application(productId)
                    .charges(List.of(new PostWorkingCapitalLoansChargeData().chargeId(flatFeeId).amount(BigDecimal.valueOf(100)),
                            new PostWorkingCapitalLoansChargeData().chargeId(percentFeeId))));

            final List<WorkingCapitalLoanChargeData> pending = wcLoanHelper.getCharges(loanId);
            assertEquals(2, pending.size());
            pending.forEach(charge -> {
                assertEquals(Boolean.FALSE, charge.getPaid(), "not settled before disbursement");
                assertNull(charge.getDueDate(), "a disbursement charge has no due date of its own before disbursement");
            });

            wcLoanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approve(LOAN_DATE, PRINCIPAL, LOAN_DATE));
            wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburse(LOAN_DATE, PRINCIPAL));

            final List<WorkingCapitalLoanChargeData> settled = wcLoanHelper.getCharges(loanId);
            assertEquals(2, settled.size(), "no duplicate is created at disbursement");
            assertChargeSettled(settled, flatFeeId, "100");
            assertChargeSettled(settled, percentFeeId, "450");
            assertEquals(0, new BigDecimal("550").compareTo(singleSettlement(loanId).getTransactionAmount()));
            assertEquals(0, new BigDecimal("8450").compareTo(wcLoanHelper.getLoanDetails(loanId).getNetDisbursalAmount()));
        });
    }

    @Test
    void aChargeSentWithTheApplicationThatIsAlsoCataloguedIsNotDuplicated() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final Long feeId = createCharge(WorkingCapitalLoanRequestBuilders.disbursementCharge(false, 100.0));
            final Long productId = createProduct(List.of(feeId));

            final Long loanId = wcLoanHelper
                    .submitApplication(application(productId).charges(List.of(new PostWorkingCapitalLoansChargeData().chargeId(feeId))));
            wcLoanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approve(LOAN_DATE, PRINCIPAL, LOAN_DATE));
            wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburse(LOAN_DATE, PRINCIPAL));

            assertEquals(1, wcLoanHelper.getCharges(loanId).size(), "the application charge, once; the catalogue adds nothing");
            assertEquals(0, new BigDecimal("100").compareTo(singleSettlement(loanId).getTransactionAmount()));
        });
    }

    @Test
    void aSpecifiedDueDateChargeIsRejectedOnTheApplication() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final Long dueDateFeeId = createCharge(WorkingCapitalLoanRequestBuilders.specifiedDueDateCharge(false, 20.0));
            final Long productId = createProduct(List.of());

            final CallFailedRuntimeException failure = wcLoanHelper
                    .submitApplicationExpectingFailure(application(productId).charges(List.of(new PostWorkingCapitalLoansChargeData()
                            .chargeId(dueDateFeeId).amount(BigDecimal.valueOf(20)).dueDate("10 January 2026"))));

            assertEquals(400, failure.getStatus());
            assertEquals(List.of(LOAN_SHOULD_BE_ACTIVE), errorCodesOf(failure),
                    "a specified-due-date charge needs an active loan, so it cannot travel with the application");
        });
    }

    @Test
    void aDueDateOnADisbursementChargeIsRejected() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final Long feeId = createCharge(WorkingCapitalLoanRequestBuilders.disbursementCharge(false, 100.0));
            final Long productId = createProduct(List.of());

            final CallFailedRuntimeException failure = wcLoanHelper.submitApplicationExpectingFailure(application(productId)
                    .charges(List.of(new PostWorkingCapitalLoansChargeData().chargeId(feeId).dueDate("10 January 2026"))));

            assertEquals(400, failure.getStatus());
            assertEquals(List.of(DUE_DATE_NOT_ALLOWED), errorCodesOf(failure));
        });
    }

    @Test
    void aTermLoanChargeIsRejectedOnTheApplication() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final Long termLoanFeeId = createCharge(ChargeRequestBuilders.loanDisbursementFee(20.0));
            final Long productId = createProduct(List.of());

            final CallFailedRuntimeException failure = wcLoanHelper.submitApplicationExpectingFailure(
                    application(productId).charges(List.of(new PostWorkingCapitalLoansChargeData().chargeId(termLoanFeeId))));

            assertEquals(403, failure.getStatus(), "a domain-rule rejection is reported as 403");
            assertEquals(List.of(NOT_A_WC_CHARGE), errorCodesOf(failure),
                    "a disbursement-time charge of the wrong domain passes the time-type guard; the appliesTo guard must catch it");
        });
    }

    @Test
    void modifyingWithAnEmptyListRetiresTheChargesAndNothingIsSettledAtDisbursement() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final Long cataloguedFeeId = createCharge(WorkingCapitalLoanRequestBuilders.disbursementCharge(false, 100.0));
            final Long extraFeeId = createCharge(WorkingCapitalLoanRequestBuilders.disbursementCharge(false, 30.0));
            final Long productId = createProduct(List.of(cataloguedFeeId));
            final Long loanId = wcLoanHelper.submitApplication(
                    application(productId).charges(List.of(new PostWorkingCapitalLoansChargeData().chargeId(extraFeeId))));
            assertEquals(1, wcLoanHelper.getCharges(loanId).size());

            wcLoanHelper.modifyApplication(loanId, modification().charges(List.of()));

            assertTrue(wcLoanHelper.getCharges(loanId).isEmpty(), "a retired charge is no longer listed");

            wcLoanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approve(LOAN_DATE, PRINCIPAL, LOAN_DATE));
            wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburse(LOAN_DATE, PRINCIPAL));

            assertTrue(wcLoanHelper.getCharges(loanId).isEmpty(), "the catalogue is not inherited: nothing is settled");
            assertEquals(0, PRINCIPAL.compareTo(wcLoanHelper.getLoanDetails(loanId).getNetDisbursalAmount()));
        });
    }

    @Test
    void aCataloguedChargeRemovedFromTheApplicationDoesNotComeBackAtDisbursement() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final Long cataloguedFeeId = createCharge(WorkingCapitalLoanRequestBuilders.disbursementCharge(false, 100.0));
            final Long productId = createProduct(List.of(cataloguedFeeId));
            final Long loanId = wcLoanHelper.submitApplication(
                    application(productId).charges(List.of(new PostWorkingCapitalLoansChargeData().chargeId(cataloguedFeeId))));

            // The catalogue is only what the product offers: the application may drop a catalogued charge.
            wcLoanHelper.modifyApplication(loanId, modification().charges(List.of()));
            assertTrue(wcLoanHelper.getCharges(loanId).isEmpty());

            wcLoanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approve(LOAN_DATE, PRINCIPAL, LOAN_DATE));
            wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburse(LOAN_DATE, PRINCIPAL));

            assertTrue(wcLoanHelper.getCharges(loanId).isEmpty(), "a removed charge stays removed; the catalogue is never inherited");
            assertTrue(
                    wcLoanHelper.getTransactions(loanId).stream()
                            .noneMatch(txn -> txn.getType() != null && REPAYMENT_AT_DISBURSEMENT_CODE.equals(txn.getType().getCode())),
                    "nothing to settle");
            assertEquals(0, PRINCIPAL.compareTo(wcLoanHelper.getLoanDetails(loanId).getNetDisbursalAmount()));
        });
    }

    @Test
    void modifyingWithAnIdUpdatesTheAmountAndAForeignIdIsRejected() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final Long feeId = createCharge(WorkingCapitalLoanRequestBuilders.disbursementCharge(false, 100.0));
            final Long productId = createProduct(List.of());
            final Long loanId = wcLoanHelper
                    .submitApplication(application(productId).charges(List.of(new PostWorkingCapitalLoansChargeData().chargeId(feeId))));
            final Long loanChargeId = wcLoanHelper.getCharges(loanId).get(0).getId();

            wcLoanHelper.modifyApplication(loanId, modification().charges(
                    List.of(new PostWorkingCapitalLoansChargeData().id(loanChargeId).chargeId(feeId).amount(BigDecimal.valueOf(150)))));

            final List<WorkingCapitalLoanChargeData> charges = wcLoanHelper.getCharges(loanId);
            assertEquals(1, charges.size(), "an entry with id updates, it does not add");
            assertEquals(0, new BigDecimal("150").compareTo(charges.get(0).getAmount()));

            final CallFailedRuntimeException failure = wcLoanHelper.modifyApplicationExpectingFailure(loanId,
                    modification().charges(List.of(new PostWorkingCapitalLoansChargeData().id(loanChargeId + 100_000).chargeId(feeId))));
            assertEquals(404, failure.getStatus());
            assertEquals(List.of(LOAN_CHARGE_NOT_FOUND), errorCodesOf(failure));
        });
    }

    @Test
    void modifyingWithoutChargesKeepsThem() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final Long feeId = createCharge(WorkingCapitalLoanRequestBuilders.disbursementCharge(false, 100.0));
            final Long productId = createProduct(List.of());
            final Long loanId = wcLoanHelper
                    .submitApplication(application(productId).charges(List.of(new PostWorkingCapitalLoansChargeData().chargeId(feeId))));

            wcLoanHelper.modifyApplication(loanId, modification().principalAmount(BigDecimal.valueOf(9500)));

            assertEquals(1, wcLoanHelper.getCharges(loanId).size(), "omitting charges must not touch them");
        });
    }

    // -----------------------------------------------------------------------------------------------------------------

    private Long createCharge(final org.apache.fineract.client.models.ChargeRequest request) {
        return chargesHelper.createCharge(request).getResourceId();
    }

    private Long createProduct(final List<Long> cataloguedChargeIds) {
        return productHelper
                .createWorkingCapitalLoanProduct(
                        new WorkingCapitalLoanProductTestBuilder().withName("WCL AppChg " + Utils.uniqueRandomStringGenerator("", 8))
                                .withShortName(Utils.uniqueRandomStringGenerator("", 4)).withChargeIds(cataloguedChargeIds).build())
                .getResourceId();
    }

    private PostWorkingCapitalLoansRequest application(final Long productId) {
        final Long clientId = clientHelper.createClient(LOAN_DATE);
        return WorkingCapitalLoanRequestBuilders.submitApplication(clientId, productId, PRINCIPAL, PERIOD_PAYMENT_RATE, LOAN_DATE,
                LOAN_DATE);
    }

    private static PutWorkingCapitalLoansLoanIdRequest modification() {
        return new PutWorkingCapitalLoansLoanIdRequest().locale("en").dateFormat("dd MMMM yyyy");
    }

    private GetWorkingCapitalLoanTransactionIdResponse singleSettlement(final Long loanId) {
        final List<GetWorkingCapitalLoanTransactionIdResponse> settlements = wcLoanHelper
                .getTransactions(loanId).stream().filter(txn -> txn.getType() != null
                        && REPAYMENT_AT_DISBURSEMENT_CODE.equals(txn.getType().getCode()) && !Boolean.TRUE.equals(txn.getReversed()))
                .toList();
        assertEquals(1, settlements.size(), "exactly one live repayment-at-disbursement transaction");
        return settlements.get(0);
    }

    private static void assertChargeSettled(final List<WorkingCapitalLoanChargeData> charges, final Long chargeId, final String amount) {
        final WorkingCapitalLoanChargeData charge = charges.stream().filter(c -> chargeId.equals(c.getChargeId())).findFirst()
                .orElseThrow(() -> new AssertionError("charge " + chargeId + " not on the account"));
        assertEquals(0, new BigDecimal(amount).compareTo(charge.getAmount()), "amount of charge " + chargeId);
        assertEquals(Boolean.TRUE, charge.getPaid(), "charge " + chargeId + " is settled");
    }
}
