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

import static org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper.assertEqualBigDecimal;
import static org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper.errorCodesOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetBalance;
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessDateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalLoanRequestBuilders;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for what {@code POST working-capital-loans/{id}/transactions?command=discountFee} accepts as its
 * {@code relatedResourceId}, and for the loan carrying at most one discount fee.
 *
 * <p>
 * A discount fee is not a free-standing transaction: it is the fee withheld from one disbursement, it is what the
 * loan's {@code discountFee}, its amortization schedule and its unrealized income are all derived from, and there is
 * exactly one of those per loan. So {@code relatedResourceId} must name an active disbursement <em>of that same
 * loan</em> — a repayment, another loan's disbursement, a reversed disbursement and an unknown id are all rejected —
 * and a loan that already carries an active discount fee rejects a second one whatever id is passed. Everything the fee
 * feeds (loan {@code discountFee}, {@code balance.totalDiscountFee}, the schedule's discount) is asserted after each
 * rejection, because the failure mode being guarded is not the 200 itself but the balances silently disagreeing from
 * then on.
 *
 * <p>
 * The mirror-image validation on {@code command=discountFeeAdjustment} ({@code discount.transaction.not.found} /
 * {@code discount.transaction.invalid}) is what this path is modelled on.
 */
public class FeignWorkingCapitalLoanDiscountFeeValidationTest extends FeignIntegrationTest {

    private static final String DISCOUNT_FEE_CODE = "loanTransactionType.discountFee";

    private static final String DISBURSE_DATE = "01 January 2026";
    private static final String DISBURSE_DATE_ISO = "2026-01-01";
    private static final BigDecimal NET_DISBURSEMENT = new BigDecimal("9000");
    private static final BigDecimal DISCOUNT = new BigDecimal("1000");
    private static final BigDecimal PERIOD_PAYMENT_RATE = new BigDecimal("18");
    private static final BigDecimal DAILY_PAYMENT = new BigDecimal("50");

    private static final String CODE_NOT_FOUND = "validation.msg.wc.loan.disbursement.transaction.not.found";
    private static final String CODE_INVALID = "validation.msg.wc.loan.disbursement.transaction.invalid";
    private static final String CODE_ALREADY_SET = "validation.msg.wc.loan.discount.already.set.before.disbursement";

    private FeignWorkingCapitalLoanHelper wcLoanHelper;
    private FeignClientHelper clientHelper;
    private FeignBusinessDateHelper businessDateHelper;
    private WorkingCapitalLoanProductHelper productHelper;

    private final List<Long> createdLoanIds = new ArrayList<>();

    @BeforeAll
    void setupHelpers() {
        final var feignClient = fineractClient();
        wcLoanHelper = new FeignWorkingCapitalLoanHelper(feignClient);
        clientHelper = new FeignClientHelper(feignClient);
        businessDateHelper = new FeignBusinessDateHelper(feignClient);
        productHelper = new WorkingCapitalLoanProductHelper();
    }

    @AfterAll
    void cleanupEntities() {
        createdLoanIds.forEach(wcLoanHelper::cleanupLoan);
        createdLoanIds.clear();
    }

    @Test
    @DisplayName("a valid disbursement id is still accepted and books the discount fee")
    void discountFee_withOwnActiveDisbursement_isAccepted() {
        businessDateHelper.runAt(DISBURSE_DATE_ISO, () -> {
            final DisbursedLoan loan = disburseLoan();

            final Long discountFeeId = wcLoanHelper.makeDiscountFee(loan.loanId(),
                    WorkingCapitalLoanRequestBuilders.discountFee(DISCOUNT, loan.disbursementId()));

            assertNotNull(discountFeeId, "the accepted discount fee must come back with a transaction id");
            assertDiscountFeeBooked(loan.loanId());
        });
    }

    @Test
    @DisplayName("a repayment id of the same loan is rejected as not a disbursement")
    void discountFee_withOwnRepaymentId_isRejected() {
        businessDateHelper.runAt(DISBURSE_DATE_ISO, () -> {
            final DisbursedLoan loan = disburseLoan();
            final Long repaymentId = wcLoanHelper.makeRepayment(loan.loanId(),
                    WorkingCapitalLoanRequestBuilders.repayment(DAILY_PAYMENT, DISBURSE_DATE));

            final CallFailedRuntimeException failure = wcLoanHelper.makeDiscountFeeExpectingFailure(loan.loanId(),
                    WorkingCapitalLoanRequestBuilders.discountFee(DISCOUNT, repaymentId));

            assertEquals(400, failure.getStatus(), "a rejected relatedResourceId is reported as 400 by Fineract");
            assertEquals(List.of(CODE_INVALID), errorCodesOf(failure),
                    "a transaction of the loan that is not a disbursement must be rejected as invalid, not as missing");
            assertNoDiscountFeeBooked(loan.loanId());
        });
    }

    @Test
    @DisplayName("another loan's disbursement id is rejected as not found, and leaves that other loan alone")
    void discountFee_withForeignDisbursementId_isRejected() {
        businessDateHelper.runAt(DISBURSE_DATE_ISO, () -> {
            final DisbursedLoan target = disburseLoan();
            final DisbursedLoan other = disburseLoan();

            final CallFailedRuntimeException failure = wcLoanHelper.makeDiscountFeeExpectingFailure(target.loanId(),
                    WorkingCapitalLoanRequestBuilders.discountFee(DISCOUNT, other.disbursementId()));

            assertEquals(400, failure.getStatus(), "a rejected relatedResourceId is reported as 400 by Fineract");
            assertEquals(List.of(CODE_NOT_FOUND), errorCodesOf(failure),
                    "a transaction that exists but belongs to a different loan must not be visible to this loan at all");
            assertNoDiscountFeeBooked(target.loanId());
            assertNoDiscountFeeBooked(other.loanId());
        });
    }

    @Test
    @DisplayName("a reversed disbursement id is rejected even though a new disbursement is active")
    void discountFee_withReversedDisbursementId_isRejected() {
        businessDateHelper.runAt(DISBURSE_DATE_ISO, () -> {
            final DisbursedLoan loan = disburseLoan();
            wcLoanHelper.undoDisbursal(loan.loanId(), WorkingCapitalLoanRequestBuilders.undoDisbursal());
            final Long reDisbursementId = wcLoanHelper.disburse(loan.loanId(),
                    WorkingCapitalLoanRequestBuilders.disburse(DISBURSE_DATE, NET_DISBURSEMENT));

            final CallFailedRuntimeException failure = wcLoanHelper.makeDiscountFeeExpectingFailure(loan.loanId(),
                    WorkingCapitalLoanRequestBuilders.discountFee(DISCOUNT, loan.disbursementId()));

            assertEquals(400, failure.getStatus(), "a rejected relatedResourceId is reported as 400 by Fineract");
            assertEquals(List.of(CODE_INVALID), errorCodesOf(failure),
                    "the undone disbursement is reversed, so it can no longer carry a discount fee");
            assertNoDiscountFeeBooked(loan.loanId());

            // The rejection is about the id passed, not about the loan: the live disbursement still takes the fee.
            wcLoanHelper.makeDiscountFee(loan.loanId(), WorkingCapitalLoanRequestBuilders.discountFee(DISCOUNT, reDisbursementId));
            assertDiscountFeeBooked(loan.loanId());
        });
    }

    @Test
    @DisplayName("an unknown transaction id is rejected as not found")
    void discountFee_withUnknownTransactionId_isRejected() {
        businessDateHelper.runAt(DISBURSE_DATE_ISO, () -> {
            final DisbursedLoan loan = disburseLoan();
            final Long unknownTransactionId = loan.disbursementId() + 1_000_000L;

            final CallFailedRuntimeException failure = wcLoanHelper.makeDiscountFeeExpectingFailure(loan.loanId(),
                    WorkingCapitalLoanRequestBuilders.discountFee(DISCOUNT, unknownTransactionId));

            assertEquals(400, failure.getStatus(), "a rejected relatedResourceId is reported as 400 by Fineract");
            assertEquals(List.of(CODE_NOT_FOUND), errorCodesOf(failure), "an id that names no transaction must be reported as not found");
            assertNoDiscountFeeBooked(loan.loanId());
        });
    }

    @Test
    @DisplayName("a second discount fee is rejected and the first one's balances stay consistent")
    void secondDiscountFee_isRejectedAndLeavesBalancesConsistent() {
        businessDateHelper.runAt(DISBURSE_DATE_ISO, () -> {
            final DisbursedLoan loan = disburseLoan();
            wcLoanHelper.makeDiscountFee(loan.loanId(), WorkingCapitalLoanRequestBuilders.discountFee(DISCOUNT, loan.disbursementId()));

            final CallFailedRuntimeException failure = wcLoanHelper.makeDiscountFeeExpectingFailure(loan.loanId(),
                    WorkingCapitalLoanRequestBuilders.discountFee(DISCOUNT, loan.disbursementId()));

            assertEquals(400, failure.getStatus(), "a second discount fee is reported as 400 by Fineract");
            assertEquals(List.of(CODE_ALREADY_SET), errorCodesOf(failure), "the loan already carries its one discount fee");
            assertDiscountFeeBooked(loan.loanId());
        });
    }

    // -----------------------------------------------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------------------------------------------

    /** Asserts the loan carries exactly one active discount fee and every derived total agrees with it. */
    private void assertDiscountFeeBooked(final Long loanId) {
        final GetWorkingCapitalLoansLoanIdResponse details = wcLoanHelper.getLoanDetails(loanId);
        assertEqualBigDecimal(DISCOUNT, details.getDiscountFee(), "loan.discountFee");
        final GetBalance balance = details.getBalance();
        assertNotNull(balance, "loan.balance must be present");
        assertEqualBigDecimal(DISCOUNT, balance.getTotalDiscountFee(), "balance.totalDiscountFee must equal the one booked fee");
        assertEqualBigDecimal(NET_DISBURSEMENT.add(DISCOUNT), balance.getPrincipal(),
                "balance.principal is the total repayable, i.e. disbursed plus the one discount");
        assertEqualBigDecimal(DISCOUNT, wcLoanHelper.getAmortizationSchedule(loanId).getDiscountFeeAmount(),
                "the schedule must be built on the same single discount");
        assertEquals(1, activeDiscountFees(loanId).size(), "the loan must carry exactly one active discount fee transaction");
    }

    /** Asserts a rejected call left no trace: no discount fee transaction and none of the derived totals moved. */
    private void assertNoDiscountFeeBooked(final Long loanId) {
        final GetWorkingCapitalLoansLoanIdResponse details = wcLoanHelper.getLoanDetails(loanId);
        assertNull(details.getDiscountFee(), "a rejected discount fee must leave loan.discountFee unset");
        final GetBalance balance = details.getBalance();
        assertNotNull(balance, "loan.balance must be present");
        assertEqualBigDecimal(BigDecimal.ZERO, balance.getTotalDiscountFee(), "balance.totalDiscountFee after a rejected discount fee");
        assertEqualBigDecimal(NET_DISBURSEMENT, balance.getPrincipal(),
                "balance.principal must stay the bare disbursed amount when no discount was booked");
        assertEquals(List.of(), activeDiscountFees(loanId), "a rejected discount fee must post no transaction");
    }

    private List<GetWorkingCapitalLoanTransactionIdResponse> activeDiscountFees(final Long loanId) {
        return wcLoanHelper.getTransactions(loanId).stream().filter(txn -> txn.getType() != null)
                .filter(txn -> DISCOUNT_FEE_CODE.equals(txn.getType().getCode())).filter(txn -> !Boolean.TRUE.equals(txn.getReversed()))
                .toList();
    }

    /** A plain approved-and-disbursed loan with no discount of its own, ready to take a discount fee transaction. */
    private DisbursedLoan disburseLoan() {
        final Long clientId = clientHelper.createClient(DISBURSE_DATE);
        final Long productId = createProduct();
        final Long loanId = wcLoanHelper.submitApplication(WorkingCapitalLoanRequestBuilders.submitApplication(clientId, productId,
                NET_DISBURSEMENT, PERIOD_PAYMENT_RATE, DISBURSE_DATE, DISBURSE_DATE));
        createdLoanIds.add(loanId);
        wcLoanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approve(DISBURSE_DATE, NET_DISBURSEMENT, DISBURSE_DATE));
        final Long disbursementId = wcLoanHelper.disburse(loanId,
                WorkingCapitalLoanRequestBuilders.disburse(DISBURSE_DATE, NET_DISBURSEMENT));
        return new DisbursedLoan(loanId, disbursementId);
    }

    private Long createProduct() {
        return productHelper.createWorkingCapitalLoanProduct(
                new WorkingCapitalLoanProductTestBuilder().withName("WCL DiscFeeVal " + Utils.uniqueRandomStringGenerator("", 8))
                        .withShortName(Utils.uniqueRandomStringGenerator("", 4))
                        .withAllowAttributeOverrides(Map.of("discountDefault", Boolean.TRUE)).build())
                .getResourceId();
    }

    private record DisbursedLoan(Long loanId, Long disbursementId) {
    }
}
