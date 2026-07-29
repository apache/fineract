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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.client.models.GetBalance;
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.WorkingCapitalLoanBreachScheduleData;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessDateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalLoanRequestBuilders;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.workingcapitalloanbreach.WorkingCapitalBreachHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * How an over-refunding credit balance refund reaches the breach schedule on a breach-configured Working Capital loan.
 *
 * <p>
 * Mirrors the over-refund reopen of {@code FeignWorkingCapitalLoanCreditBalanceRefundReprocessingTest
 * #backdatedReversalAfterCbr_reInjectsFullCbrAsPrincipal}, but on a breach-configured product. A CBR pays nothing into
 * a period, so it never moves a period of its own; the principal it re-injects reaches the schedule only globally,
 * through the open period's minimum payment being capped at what the customer can still owe.
 *
 * <p>
 * That makes the cap the discriminating observation: while the loan is settled the open period demands nothing, and
 * once the undo re-injects the refunded principal the demand lifts again - never above the configured minimum.
 */
public class FeignWorkingCapitalLoanCreditBalanceRefundBreachScheduleTest extends FeignIntegrationTest {

    private static final String STATUS_ACTIVE = "loanStatusType.active";
    private static final String STATUS_OVERPAID = "loanStatusType.overpaid";
    private static final String STATUS_CLOSED_OBLIGATIONS_MET = "loanStatusType.closed.obligations.met";

    // Breach configuration: a one-month period with a flat, modest 30 minimum payment and no grace days (so period 1
    // exists from the disbursement date).
    private static final int BREACH_FREQUENCY = 1;
    private static final String BREACH_FREQUENCY_TYPE = "MONTHS";
    private static final String BREACH_AMOUNT_CALCULATION_TYPE = "FLAT";
    private static final BigDecimal BREACH_MIN_PAYMENT_AMOUNT = new BigDecimal("30");

    private FeignWorkingCapitalLoanHelper wcLoanHelper;
    private FeignClientHelper clientHelper;
    private FeignBusinessDateHelper businessDateHelper;
    private WorkingCapitalLoanProductHelper productHelper;
    private WorkingCapitalBreachHelper breachHelper;

    private final List<Long> createdLoanIds = new ArrayList<>();
    private final List<Long> createdProductIds = new ArrayList<>();

    @BeforeAll
    void setupHelpers() {
        final var feignClient = fineractClient();
        wcLoanHelper = new FeignWorkingCapitalLoanHelper(feignClient);
        clientHelper = new FeignClientHelper(feignClient);
        businessDateHelper = new FeignBusinessDateHelper(feignClient);
        productHelper = new WorkingCapitalLoanProductHelper();
        breachHelper = new WorkingCapitalBreachHelper();
    }

    @AfterAll
    void cleanupEntities() {
        createdLoanIds.forEach(wcLoanHelper::cleanupLoan);
        createdLoanIds.clear();
        createdProductIds.clear();
    }

    /**
     * Over-refund reopen ("Credit Principal") on a breach-configured loan: the refunded principal comes back as due
     * principal, and the open breach period's minimum payment has to follow it back up.
     *
     * <p>
     * Breach periods are only created at COB, and the inline WC COB only processes non-closed loans that are NOT
     * overpaid (its "behind date" query excludes the OVERPAID status). So all breach periods must be generated while
     * the loan is still ACTIVE.
     *
     * <p>
     * Setup (principal 50, breach period 1 = 01-31 Jan, period 2 = 01-28 Feb, flat 30 minimum payment):
     * <ol>
     * <li>COB on 05 Feb while ACTIVE (no repayments yet) generates breach period 1 (01-31 Jan) and period 2 (01-28
     * Feb). Outstanding principal is the full 50, so the cap does not bite and period 2 demands the full 30.</li>
     * <li>Backdated repayment 25 dated 05 Jan (ACTIVE, outstanding 25) — accrues into period 1.</li>
     * <li>Backdated repayment 75 dated 10 Jan (OVERPAID, overpaid 50) — accrues into period 1. Nothing is owed any
     * more, so period 2's minimum payment is capped to 0.</li>
     * <li>CBR 50 on 05 Feb (CLOSED, overpaid 0). A refund pays nothing into a period, so period 2 is untouched.</li>
     * <li>Reverse the first repayment (25) on 10 Feb. Later transactions exist, so the undo reprocesses: the 75
     * repayment settles the 50 principal leaving 25 overpayment, the CBR of 50 then finds only 25 of overpayment behind
     * it and the remaining 25 becomes a principal adjustment. Outstanding principal goes 0 -&gt; 25.</li>
     * </ol>
     *
     * <p>
     * The discriminating observation is period 2: capped to 0 while the loan was settled, and back to 25 - the newly
     * owed principal, still under the configured 30 - once the refund is re-recognised as principal. A period that
     * stayed at 0 would mean the principal adjustment never reached the breach schedule; a period above 30 would mean
     * the cap ran on a stale base.
     */
    @Test
    @DisplayName("An over-refund reopen lifts the open breach period's minimum payment back to the newly owed principal, capped at the configured minimum")
    void cbrAwareUndo_overRefundReopen_liftsCappedBreachMinimumPayment() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long clientId = clientHelper.createClient("01 January 2026");
            final Long loanId = createAndDisburseBreachLoan(clientId, BigDecimal.valueOf(50), "01 January 2026");

            // COB while ACTIVE (no repayments) generates period 1 (01-31 Jan) AND period 2 (01-28 Feb), both paidAmount
            // 0. This must happen before the loan is overpaid, because the inline WC COB skips overpaid loans.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-02-05");
            wcLoanHelper.executeInlineWCCOB(loanId);
            final WorkingCapitalLoanBreachScheduleData period2AfterCob = findBreachPeriod(wcLoanHelper.getBreachSchedule(loanId), 2);
            assertEqualBigDecimal(BREACH_MIN_PAYMENT_AMOUNT, period2AfterCob.getMinPaymentAmount(),
                    "breach period 2 minimum payment must be the flat 30");
            assertEqualBigDecimal(BREACH_MIN_PAYMENT_AMOUNT, period2AfterCob.getOutstandingAmount(),
                    "breach period 2 outstanding must start at the full 30 minimum (nothing paid into it)");

            // Backdated repayment 25 dated 05 Jan -> accrues into period 1; loan ACTIVE, outstanding 25. Resolve its
            // transaction id so it can be reversed later.
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(25), "05 January 2026"));
            assertStatus(loanId, STATUS_ACTIVE);
            final Long firstRepaymentId = findRepaymentTransactionId(loanId, BigDecimal.valueOf(25));

            // Backdated repayment 75 dated 10 Jan -> accrues into period 1 (principal 25); loan OVERPAID, overpaid 50.
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(75), "10 January 2026"));
            assertStatus(loanId, STATUS_OVERPAID);

            // CBR 50 (not backdated, dated on the business date) -> CLOSED, overpaid 0.
            wcLoanHelper.creditBalanceRefund(loanId,
                    WorkingCapitalLoanRequestBuilders.creditBalanceRefund(BigDecimal.valueOf(50), "05 February 2026"));
            assertStatus(loanId, STATUS_CLOSED_OBLIGATIONS_MET);

            // Snapshot period 2 immediately before the reversal so any change is attributable solely to the undo. The
            // loan owes nothing at this point, so the open period's demand is capped away entirely.
            final WorkingCapitalLoanBreachScheduleData period2BeforeUndo = findBreachPeriod(wcLoanHelper.getBreachSchedule(loanId), 2);
            assertEqualBigDecimal(BigDecimal.ZERO, period2BeforeUndo.getOutstandingAmount(),
                    "breach period 2 must demand nothing while the loan is settled");

            // Reverse the first repayment (25) on 10 Feb -> the reprocess re-derives the CBR split, turning the part of
            // the refund no longer backed by overpayment into a principal adjustment.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-02-10");
            wcLoanHelper.reverseTransaction(loanId, firstRepaymentId, WorkingCapitalLoanRequestBuilders.reversal());

            // The loan itself is cash-conserved: back to ACTIVE with outstanding 25, overpayment 0.
            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            final GetBalance balance = requireBalance(loan);
            assertEqualBigDecimal(BigDecimal.valueOf(25), balance.getPrincipalOutstanding(),
                    "loan outstanding principal after the over-refund reopen must be 25");
            assertEqualBigDecimal(BigDecimal.ZERO, balance.getOverpaymentAmount(), "overpayment cleared after the over-refund reopen");
            assertEqualBigDecimal(BigDecimal.valueOf(25), balance.getPrincipalAdjustment(),
                    "the part of the refund left without overpayment behind it must be recognized as a principal adjustment");

            // Discriminator: the open period's demand follows the newly owed principal back up, bounded by the
            // configured minimum.
            final WorkingCapitalLoanBreachScheduleData period2 = findBreachPeriod(wcLoanHelper.getBreachSchedule(loanId), 2);
            assertNotNull(period2.getOutstandingAmount(), "breach period 2 outstanding must be present");
            assertEqualBigDecimal(BigDecimal.valueOf(25), period2.getOutstandingAmount(),
                    "breach period 2 must demand exactly the re-injected principal (25), up from "
                            + period2BeforeUndo.getOutstandingAmount()
                            + "; staying at 0 would mean the principal adjustment never reached the breach schedule");
            assertTrue(period2.getMinPaymentAmount().compareTo(BREACH_MIN_PAYMENT_AMOUNT) <= 0, "breach period 2 minimum payment ("
                    + period2.getMinPaymentAmount() + ") must never exceed the configured " + BREACH_MIN_PAYMENT_AMOUNT);
        });
    }

    private Long createAndDisburseBreachLoan(final Long clientId, final BigDecimal principal, final String date) {
        final Long productId = createBreachProduct();
        final Long loanId = wcLoanHelper.submitApplication(
                WorkingCapitalLoanRequestBuilders.submitApplication(clientId, productId, principal, BigDecimal.valueOf(18), date, date));
        createdLoanIds.add(loanId);
        wcLoanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approve(date, principal, date));
        wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburse(date, principal));
        return loanId;
    }

    private Long createBreachProduct() {
        final Long breachId = breachHelper.create(breachHelper.createBreachRequest(Utils.randomStringGenerator("WC_BREACH_", 8),
                BREACH_FREQUENCY, BREACH_FREQUENCY_TYPE, BREACH_AMOUNT_CALCULATION_TYPE, BREACH_MIN_PAYMENT_AMOUNT));
        final Long productId = productHelper
                .createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder()
                        .withName("WCL CBR Breach " + Utils.uniqueRandomStringGenerator("", 8))
                        .withShortName(Utils.uniqueRandomStringGenerator("", 4)).withBreachId(breachId).withBreachGraceDays(0).build())
                .getResourceId();
        createdProductIds.add(productId);
        return productId;
    }

    private Long findRepaymentTransactionId(final Long loanId, final BigDecimal amount) {
        return wcLoanHelper.getTransactions(loanId).stream().filter(txn -> !Boolean.TRUE.equals(txn.getReversed()))
                .filter(txn -> txn.getTransactionAmount() != null && amount.compareTo(txn.getTransactionAmount()) == 0)
                .filter(txn -> txn.getType() != null && Boolean.TRUE.equals(txn.getType().getRepayment()))
                .map(GetWorkingCapitalLoanTransactionIdResponse::getId).findFirst()
                .orElseThrow(() -> new AssertionError("Repayment transaction of " + amount + " not found on loan " + loanId));
    }

    private void assertStatus(final Long loanId, final String expectedCode) {
        final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
        assertNotNull(loan.getStatus(), "loan status should be present");
        org.junit.jupiter.api.Assertions.assertEquals(expectedCode, loan.getStatus().getCode(), "loan status code");
    }

    private static GetBalance requireBalance(final GetWorkingCapitalLoansLoanIdResponse loan) {
        final GetBalance balance = loan.getBalance();
        assertNotNull(balance, "loan balance should be present");
        return balance;
    }

    private static WorkingCapitalLoanBreachScheduleData findBreachPeriod(final List<WorkingCapitalLoanBreachScheduleData> periods,
            final int periodNumber) {
        return periods.stream().filter(period -> period.getPeriodNumber() != null && periodNumber == period.getPeriodNumber()).findFirst()
                .orElseThrow(() -> new AssertionError("Breach schedule period " + periodNumber + " not found; periods present: "
                        + periods.stream().map(WorkingCapitalLoanBreachScheduleData::getPeriodNumber).toList()));
    }
}
