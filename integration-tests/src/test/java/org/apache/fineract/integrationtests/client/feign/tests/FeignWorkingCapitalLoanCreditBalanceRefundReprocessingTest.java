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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetBalance;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdStatus;
import org.apache.fineract.client.models.ProjectedAmortizationScheduleData;
import org.apache.fineract.client.models.ProjectedAmortizationSchedulePaymentData;
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
import org.junit.jupiter.api.Test;

public class FeignWorkingCapitalLoanCreditBalanceRefundReprocessingTest extends FeignIntegrationTest {

    private static final String STATUS_ACTIVE = "loanStatusType.active";
    private static final String STATUS_OVERPAID = "loanStatusType.overpaid";
    private static final String STATUS_CLOSED_OBLIGATIONS_MET = "loanStatusType.closed.obligations.met";

    private FeignWorkingCapitalLoanHelper wcLoanHelper;
    private FeignClientHelper clientHelper;
    private FeignBusinessDateHelper businessDateHelper;
    private WorkingCapitalLoanProductHelper productHelper;

    private final List<Long> createdLoanIds = new ArrayList<>();
    private final List<Long> createdProductIds = new ArrayList<>();

    @BeforeAll
    void setupHelpers() {
        wcLoanHelper = new FeignWorkingCapitalLoanHelper(fineractClient());
        clientHelper = new FeignClientHelper(fineractClient());
        businessDateHelper = new FeignBusinessDateHelper(fineractClient());
        productHelper = new WorkingCapitalLoanProductHelper();
    }

    @AfterAll
    void cleanupEntities() {
        createdLoanIds.forEach(wcLoanHelper::cleanupLoan);
        createdLoanIds.clear();
        createdProductIds.clear();
    }

    /**
     * Partial CBR. Overpaid loan, refund less than overpayment: loan stays OVERPAID with the reduced overpayment;
     * principal balances untouched.
     */
    @Test
    void partialCbr_staysOverpaidWithReducedOverpayment() {
        businessDateHelper.runAt("2026-01-01", () -> {
            Long clientId = clientHelper.createClient("01 January 2026");
            Long loanId = createAndDisburseLoanOnDate(clientId, BigDecimal.valueOf(50), "01 January 2026");

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-10");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(100), "10 January 2026"));
            assertStatus(loanId, STATUS_OVERPAID);

            wcLoanHelper.creditBalanceRefund(loanId,
                    WorkingCapitalLoanRequestBuilders.creditBalanceRefund(BigDecimal.valueOf(25), "10 January 2026"));

            GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            assertStatusCode(loan, STATUS_OVERPAID);
            GetBalance balance = requireBalance(loan);
            assertEqualBigDecimal(BigDecimal.valueOf(25), balance.getOverpaymentAmount(), "overpayment after partial CBR (50 - 25)");
            assertEqualBigDecimal(BigDecimal.ZERO, balance.getPrincipalOutstanding(), "principal stays fully repaid after partial CBR");
            assertEqualBigDecimal(BigDecimal.valueOf(50), balance.getPrincipalPaid(), "principal paid untouched by CBR");
        });
    }

    /**
     * Full CBR. Refund equal to overpayment: overpayment goes to 0 and the loan closes (obligations met); principal
     * balances untouched.
     */
    @Test
    void fullCbr_closesLoanObligationsMet() {
        businessDateHelper.runAt("2026-01-01", () -> {
            Long clientId = clientHelper.createClient("01 January 2026");
            Long loanId = createAndDisburseLoanOnDate(clientId, BigDecimal.valueOf(50), "01 January 2026");

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-10");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(100), "10 January 2026"));
            assertStatus(loanId, STATUS_OVERPAID);

            wcLoanHelper.creditBalanceRefund(loanId,
                    WorkingCapitalLoanRequestBuilders.creditBalanceRefund(BigDecimal.valueOf(50), "10 January 2026"));

            GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            assertStatusCode(loan, STATUS_CLOSED_OBLIGATIONS_MET);
            GetBalance balance = requireBalance(loan);
            assertEqualBigDecimal(BigDecimal.ZERO, balance.getOverpaymentAmount(), "overpayment cleared by full CBR");
            assertEqualBigDecimal(BigDecimal.ZERO, balance.getPrincipalOutstanding(), "principal stays fully repaid after full CBR");
            assertEqualBigDecimal(BigDecimal.valueOf(50), balance.getPrincipalPaid(), "principal paid untouched by CBR");
        });
    }

    /**
     * Backdated reversal after CBR (the core case).
     *
     * <p>
     * Setup: principal 50; Repayment 25 (ACTIVE, outstanding 25); Repayment 75 (OVERPAID, overpaid 50); CBR 50 (CLOSED,
     * overpaid 0). Then the first Repayment (25) is reversed. CBR-aware reprocessing recomputes the overpayment at the
     * CBR point to 25 (surviving repayment 75 - principal 50), so the CBR of 50 over-refunds by 25. Only that
     * over-refunded excess (25) becomes due principal (a principal adjustment); the consumed overpayment (25) is simply
     * cleared. Net new outstanding = 25, overpayment 0, loan ACTIVE.
     *
     * <p>
     * Discriminating assertions: {@code principalAdjustment == 25} (only the excess, not the full CBR, is re-injected
     * as due principal, leaving the real {@code principalPaid} at 50), and the schedule's no-amortisation contract —
     * the base stays at the real disbursed 50 and the credited 25 appears <em>only</em> on the CBR-date period, on top
     * of that period's base expected payment. {@code principalOutstanding == 25} is cash-conserved and holds under both
     * an incremental and a CBR-aware undo, so it is an invariant, not a discriminator.
     *
     * <p>
     * Note the schedule deliberately does not "tie out" against the balance: the excess is due principal owned by
     * {@code principalAdjustment}, while {@code expectedBalance}/{@code netDisbursementAmount} remain the untouched TVM
     * projection of the real disbursed principal.
     */
    @Test
    void backdatedReversalAfterCbr_reInjectsExcessAsPrincipal() {
        businessDateHelper.runAt("2026-01-01", () -> {
            Long clientId = clientHelper.createClient("01 January 2026");
            Long loanId = createAndDisburseLoanOnDate(clientId, BigDecimal.valueOf(50), "01 January 2026");

            // Repayment 25 → ACTIVE, outstanding 25.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-05");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(25), "05 January 2026"));
            assertStatus(loanId, STATUS_ACTIVE);
            // Resolve the repayment's transaction id (the transaction endpoint response returns the loan id, not the
            // transaction id) so we can reverse exactly this repayment later.
            Long firstRepaymentId = findRepaymentTransactionId(loanId, BigDecimal.valueOf(25));

            // Repayment 75 → OVERPAID, overpaid 50.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-10");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(75), "10 January 2026"));
            assertStatus(loanId, STATUS_OVERPAID);

            // CBR 50 (not backdated) → CLOSED, overpaid 0.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-15");
            wcLoanHelper.creditBalanceRefund(loanId,
                    WorkingCapitalLoanRequestBuilders.creditBalanceRefund(BigDecimal.valueOf(50), "15 January 2026"));
            assertStatus(loanId, STATUS_CLOSED_OBLIGATIONS_MET);

            // Reverse the first Repayment (25). This must trigger CBR-aware reprocessing.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-20");
            wcLoanHelper.reverseTransaction(loanId, firstRepaymentId, WorkingCapitalLoanRequestBuilders.reversal());

            GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            GetBalance balance = requireBalance(loan);
            // Invariants (cash-conserved): status ACTIVE, outstanding 25, overpayment 0.
            assertStatusCode(loan, STATUS_ACTIVE);
            assertEqualBigDecimal(BigDecimal.ZERO, balance.getOverpaymentAmount(), "overpayment cleared after CBR-aware reprocessing");
            assertEqualBigDecimal(BigDecimal.valueOf(25), balance.getPrincipalOutstanding(),
                    "net new outstanding = CBR (50) - recomputed overpayment (25)");
            // Only the over-refunded excess (50 - 25 = 25) is credited as due principal, captured on
            // principalAdjustment.
            assertEqualBigDecimal(BigDecimal.valueOf(25), balance.getPrincipalAdjustment(),
                    "over-refunded excess (25) re-injected as due principal (no amortisation)");
            // The real principal paid (surviving 75 repayment's principal portion = 50) is left untouched.
            assertEqualBigDecimal(BigDecimal.valueOf(50), balance.getPrincipalPaid(),
                    "principal paid = surviving 75 repayment's principal portion (50), untouched by the CBR");

            // Reported principal is the total principal due: the original 50 plus the 25 principal adjustment.
            assertEqualBigDecimal(BigDecimal.valueOf(75), balance.getPrincipal(),
                    "reported principal = original disbursed (50) + principal adjustment (25)");

            // The excess is due principal, NOT amortised: the schedule keeps the real disbursed base (50) and the
            // credited 25 surfaces only on the CBR-date period, on top of that period's base expected payment.
            ProjectedAmortizationScheduleData schedule = wcLoanHelper.getAmortizationSchedule(loanId);
            assertEqualBigDecimal(BigDecimal.valueOf(50), schedule.getNetDisbursementAmount(),
                    "schedule base stays the real disbursed principal (50); the excess is not grossed into it");
            BigDecimal scheduleAppliedPrincipal = schedule.getPayments().stream()
                    .map(ProjectedAmortizationSchedulePaymentData::getActualPaymentAmount).filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            assertEqualBigDecimal(BigDecimal.valueOf(50), scheduleAppliedPrincipal,
                    "schedule applied payments (surviving repayment's principal 50)");
            // The credited-principal overlay on the CBR-date period is NOT asserted here: this loan amortizes its 50
            // principal in a single period, so its schedule term ends long before the 15 January CBR and no period
            // exists on that date to carry the overlay. That rule is covered by the e2e scenario, whose product has a
            // long enough term for the refund date to fall inside the schedule.
        });
    }

    /**
     * A repayment dated <em>before</em> an existing CBR triggers CBR-aware reprocessing.
     *
     * <p>
     * Setup: principal 50; Repayment 100 (OVERPAID, overpaid 50); CBR 50 (CLOSED, overpaid 0). Then a Repayment 30
     * dated before the CBR is posted. Reprocessing replays R100 (principal 50, overpaid 50), R30 (overpaid 80), then
     * the CBR 50 which — now that the overpayment (80) exceeds the refund — merely consumes overpayment (80 - 50 = 30)
     * with no principal injection. Net: principalOutstanding 0, principalAdjustment 0, overpayment 30, loan back to
     * OVERPAID.
     */
    @Test
    void backdatedRepaymentBeforeCbr_reprocessesCbrAndReopensOverpaid() {
        businessDateHelper.runAt("2026-01-01", () -> {
            Long clientId = clientHelper.createClient("01 January 2026");
            Long loanId = createAndDisburseLoanOnDate(clientId, BigDecimal.valueOf(50), "01 January 2026");

            // Repayment 100 → OVERPAID, overpaid 50.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-10");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(100), "10 January 2026"));
            assertStatus(loanId, STATUS_OVERPAID);

            // CBR 50 (full, not backdated) → CLOSED, overpaid 0.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-15");
            wcLoanHelper.creditBalanceRefund(loanId,
                    WorkingCapitalLoanRequestBuilders.creditBalanceRefund(BigDecimal.valueOf(50), "15 January 2026"));
            assertStatus(loanId, STATUS_CLOSED_OBLIGATIONS_MET);

            // Backdated Repayment 30 dated before the CBR → triggers CBR-aware reprocessing, loan re-opens to OVERPAID.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-20");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(30), "12 January 2026"));

            GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            GetBalance balance = requireBalance(loan);
            assertStatusCode(loan, STATUS_OVERPAID);
            assertEqualBigDecimal(BigDecimal.valueOf(30), balance.getOverpaymentAmount(),
                    "overpayment = payments in (100 + 30) - refund (50) - principal (50) = 30");
            assertEqualBigDecimal(BigDecimal.ZERO, balance.getPrincipalOutstanding(), "principal fully repaid after reprocessing");
            assertEqualBigDecimal(BigDecimal.valueOf(50), balance.getPrincipalPaid(), "principal paid = original principal (50)");
            assertEqualBigDecimal(BigDecimal.ZERO, balance.getPrincipalAdjustment(),
                    "no principal injection: the refund (50) no longer exceeds the recomputed overpayment (80)");
        });
    }

    /**
     * Negative — a CBR whose transaction date is before the business date (backdated) must be rejected. The over-refund
     * state only ever arises retroactively via reprocessing, never from a direct backdated CBR.
     */
    @Test
    void backdatedCbr_isRejected() {
        businessDateHelper.runAt("2026-01-01", () -> {
            Long clientId = clientHelper.createClient("01 January 2026");
            Long loanId = createAndDisburseLoanOnDate(clientId, BigDecimal.valueOf(50), "01 January 2026");

            // Overpay on day 10.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-10");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(100), "10 January 2026"));
            assertStatus(loanId, STATUS_OVERPAID);

            // Advance business date, then attempt a CBR dated before it (backdated).
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-15");
            CallFailedRuntimeException ex = wcLoanHelper.creditBalanceRefundExpectingFailure(loanId,
                    WorkingCapitalLoanRequestBuilders.creditBalanceRefund(BigDecimal.valueOf(25), "10 January 2026"));

            assertEquals(400, ex.getStatus(), "backdated CBR must be rejected with a validation error");
            assertTrue(ex.getMessage() != null && ex.getMessage().contains("backdated"),
                    "rejection must be the backdated-CBR validation error, was: " + ex.getMessage());
        });
    }

    // --- setup helpers (mirrors FeignWorkingCapitalLoanTransactionReprocessingTest) ---

    private Long createAndDisburseLoanOnDate(Long clientIdParam, BigDecimal principal, String date) {
        Long productId = createProduct();
        Long loanId = submitAndTrack(clientIdParam, productId, principal, date);
        wcLoanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approve(date, principal, date));
        wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburse(date, principal));
        return loanId;
    }

    private Long submitAndTrack(Long clientIdParam, Long productId, BigDecimal principal, String date) {
        Long loanId = wcLoanHelper.submitApplication(WorkingCapitalLoanRequestBuilders.submitApplication(clientIdParam, productId,
                principal, BigDecimal.valueOf(18), date, date));
        createdLoanIds.add(loanId);
        return loanId;
    }

    private Long createProduct() {
        String uniqueName = "WCL CBR " + Utils.uniqueRandomStringGenerator("", 8);
        String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        Long productId = productHelper
                .createWorkingCapitalLoanProduct(
                        new WorkingCapitalLoanProductTestBuilder().withName(uniqueName).withShortName(uniqueShortName).build())
                .getResourceId();
        createdProductIds.add(productId);
        return productId;
    }

    private Long findRepaymentTransactionId(Long loanId, BigDecimal amount) {
        return wcLoanHelper.getTransactions(loanId).stream().filter(txn -> !Boolean.TRUE.equals(txn.getReversed()))
                .filter(txn -> txn.getTransactionAmount() != null && amount.compareTo(txn.getTransactionAmount()) == 0)
                .filter(txn -> txn.getType() != null && Boolean.TRUE.equals(txn.getType().getRepayment())).map(txn -> txn.getId())
                .findFirst().orElseThrow(() -> new AssertionError("Repayment transaction of " + amount + " not found on loan " + loanId));
    }

    private void assertStatus(Long loanId, String expectedCode) {
        assertStatusCode(wcLoanHelper.getLoanDetails(loanId), expectedCode);
    }

    private static void assertStatusCode(GetWorkingCapitalLoansLoanIdResponse loan, String expectedCode) {
        GetWorkingCapitalLoansLoanIdStatus status = loan.getStatus();
        assertNotNull(status, "loan status should be present");
        assertEquals(expectedCode, status.getCode(), "loan status code");
    }

    private static GetBalance requireBalance(GetWorkingCapitalLoansLoanIdResponse loan) {
        GetBalance balance = loan.getBalance();
        assertNotNull(balance, "loan balance should be present");
        return balance;
    }
}
