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
 * Diagnostic probe for a CBR-aware undo on a breach-configured Working Capital loan.
 *
 * <p>
 * Mirrors the over-refund reopen of {@code FeignWorkingCapitalLoanCreditBalanceRefundReprocessingTest
 * #backdatedReversalAfterCbr_reInjectsFullCbrAsPrincipal}, but on a breach-configured product and with the reversal
 * landing in a breach period that carries no accumulated paid amount (period 2). The CBR-aware undo drives the schedule
 * reconciliation through {@code applyOutstandingPrincipalDeltaToSchedules} with a positive outstanding-principal delta
 * (the "Credit Principal" over-refund) that is larger than that period's paid amount.
 *
 * <p>
 * The breach schedule data exposed by the API does not surface {@code paidAmount}, so the discriminating check is on
 * {@code outstandingAmount}: since {@code outstandingAmount = minPaymentAmount - paidAmount}, a correctly floored
 * {@code paidAmount >= 0} keeps {@code outstandingAmount <= minPaymentAmount}. If the undo drives {@code paidAmount}
 * negative, {@code outstandingAmount} inflates above {@code minPaymentAmount}.
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
     * Over-refund reopen ("Credit Principal") on a breach-configured loan, with the reversal landing in a breach period
     * whose accumulated paid amount (0) is smaller than the re-injected outstanding-principal delta.
     *
     * <p>
     * Breach periods are only created at COB, and the inline WC COB only processes non-closed loans that are NOT
     * overpaid (its "behind date" query excludes the OVERPAID status). So all breach periods must be generated while
     * the loan is still ACTIVE. The repayments are then backdated into period 1 only, leaving period 2's
     * {@code paidAmount} at 0 — the exact condition the CBR-aware undo must handle (a positive delta larger than the
     * period's paid amount).
     *
     * <p>
     * Setup (principal 50, breach period 1 = 01-31 Jan, period 2 = 01-28 Feb, flat 30 minimum payment):
     * <ol>
     * <li>COB on 05 Feb while ACTIVE (no repayments yet) generates breach period 1 (01-31 Jan) and period 2 (01-28
     * Feb), both with paidAmount 0.</li>
     * <li>Backdated repayment 25 dated 05 Jan (ACTIVE, outstanding 25) — accrues into period 1.</li>
     * <li>Backdated repayment 75 dated 10 Jan (OVERPAID, overpaid 50) — accrues into period 1 (period 2 stays 0).</li>
     * <li>CBR 50 on 05 Feb (CLOSED, overpaid 0).</li>
     * <li>Reverse the first repayment (25) on 10 Feb. The active CBR forces a CBR-aware reprocess: outstanding
     * principal goes 0 -> 25 (the full CBR is re-injected as principal), so
     * {@code applyOutstandingPrincipalDeltaToSchedules} applies a +25 delta to breach period 2 (the period containing
     * the reversal business date) via {@code breachScheduleService.applyRepaymentUndo}, against a paidAmount of 0.</li>
     * </ol>
     *
     * <p>
     * Correct behaviour: period 2's paidAmount floors at 0, so its outstandingAmount stays at the 30 minimum. The bug
     * hypothesis: paidAmount goes to -25 and outstandingAmount inflates to 55 (30 + 25) — above the 30 minimum. Period
     * 2 is captured immediately before the reversal (outstanding = 30) and immediately after, so the reversal is the
     * only thing that can change it.
     */
    @Test
    @DisplayName("CBR-aware undo applying a positive outstanding-principal delta larger than a breach period's paid amount must not inflate it above the minimum payment")
    void cbrAwareUndo_overRefundDelta_largerThanBreachPaidAmount() {
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

            // Snapshot period 2 immediately before the reversal so any change is attributable solely to the undo.
            final WorkingCapitalLoanBreachScheduleData period2BeforeUndo = findBreachPeriod(wcLoanHelper.getBreachSchedule(loanId), 2);
            assertEqualBigDecimal(BREACH_MIN_PAYMENT_AMOUNT, period2BeforeUndo.getOutstandingAmount(),
                    "breach period 2 outstanding must still be the 30 minimum right before the reversal");

            // Reverse the first repayment (25) on 10 Feb -> CBR-aware reprocess re-injects the CBR as principal and
            // reconciles breach period 2 from the +25 outstanding-principal delta (against a paidAmount of 0).
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-02-10");
            wcLoanHelper.reverseTransaction(loanId, firstRepaymentId, WorkingCapitalLoanRequestBuilders.reversal());

            // The loan itself is cash-conserved: back to ACTIVE with outstanding 25, overpayment 0.
            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            final GetBalance balance = requireBalance(loan);
            assertEqualBigDecimal(BigDecimal.valueOf(25), balance.getPrincipalOutstanding(),
                    "loan outstanding principal after CBR-aware undo must be 25");
            assertEqualBigDecimal(BigDecimal.ZERO, balance.getOverpaymentAmount(), "overpayment cleared after CBR-aware undo");

            // Discriminator: breach period 2 must not be inflated above the 30 minimum by the +25 delta.
            final WorkingCapitalLoanBreachScheduleData period2 = findBreachPeriod(wcLoanHelper.getBreachSchedule(loanId), 2);
            assertNotNull(period2.getOutstandingAmount(), "breach period 2 outstanding must be present");
            assertTrue(period2.getOutstandingAmount().compareTo(period2.getMinPaymentAmount()) <= 0,
                    "breach period 2 outstanding (" + period2.getOutstandingAmount() + ") must not exceed its minimum payment ("
                            + period2.getMinPaymentAmount() + "); a value above the minimum means the undo drove paidAmount negative "
                            + "(spurious/oversized breach). Outstanding before the reversal was "
                            + period2BeforeUndo.getOutstandingAmount());
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
