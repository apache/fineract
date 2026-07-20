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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
import org.junit.jupiter.api.Test;

/**
 * Integration tests for WC Transaction Reprocessing (generic).
 *
 * Backdated repayments are applied through the regular incremental flow (balance math is order-independent and the
 * amortization model records payments on their actual day). The reprocessing engine only recalculates allocations of
 * subsequent transactions when payments compete for charge buckets — without charges it is a no-op, which these tests
 * verify by asserting that existing allocations stay untouched after a backdated repayment.
 *
 * <p>
 * The charge-based re-allocation path (fees/penalties competing across transactions) is covered at the E2E layer in
 * {@code WorkingCapitalTransactionReprocessing.feature} (C85212/C85216/C85218); this integration suite focuses on the
 * charge-free, order-independent path.
 */
public class FeignWorkingCapitalLoanTransactionReprocessingTest extends FeignIntegrationTest {

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

    @Test
    void testBackdatedRepayment_balanceReflectsBothPayments() {
        businessDateHelper.runAt("2026-01-01", () -> {
            Long clientForTest = clientHelper.createClient("01 January 2026");
            Long loanId = createAndDisburseLoanOnDate(clientForTest, BigDecimal.valueOf(9000), "01 January 2026");

            // First repayment on day 10
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-10");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(3000), "10 January 2026"));

            GetWorkingCapitalLoansLoanIdResponse afterFirstRepayment = wcLoanHelper.getLoanDetails(loanId);
            assertNotNull(afterFirstRepayment.getBalance(), "Balance should exist after repayment");
            assertEqualBigDecimal(BigDecimal.valueOf(6000), afterFirstRepayment.getBalance().getPrincipalOutstanding(),
                    "Outstanding should be 6000 after 3000 repayment on 9000 loan");
            assertEqualBigDecimal(BigDecimal.valueOf(3000), afterFirstRepayment.getBalance().getPrincipalPaid(),
                    "Principal paid should be 3000 after first repayment");

            // Backdated repayment on day 5 (before existing repayment on day 10)
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-15");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(2000), "05 January 2026"));

            // Both repayments should be reflected — balance math is order-independent
            GetWorkingCapitalLoansLoanIdResponse afterBackdated = wcLoanHelper.getLoanDetails(loanId);
            assertNotNull(afterBackdated.getBalance(), "Balance should exist after backdated repayment");
            assertEqualBigDecimal(BigDecimal.valueOf(4000), afterBackdated.getBalance().getPrincipalOutstanding(),
                    "Outstanding should be 4000 after total 5000 repaid on 9000 loan");
            assertEqualBigDecimal(BigDecimal.valueOf(5000), afterBackdated.getBalance().getPrincipalPaid(),
                    "Principal paid should be 5000 (2000 + 3000)");

            // Both repayments fit into principal: the backdated one allocates fully, the earlier one stays untouched
            List<GetWorkingCapitalLoanTransactionIdResponse> transactions = wcLoanHelper.getTransactions(loanId);
            assertAllocation(findTransaction(transactions, LocalDate.of(2026, 1, 5), BigDecimal.valueOf(2000)), BigDecimal.valueOf(2000));
            assertAllocation(findTransaction(transactions, LocalDate.of(2026, 1, 10), BigDecimal.valueOf(3000)), BigDecimal.valueOf(3000));
        });
    }

    @Test
    void testBackdatedRepayment_excessBecomesOverpayment() {
        businessDateHelper.runAt("2026-01-01", () -> {
            Long clientForTest = clientHelper.createClient("01 January 2026");
            Long loanId = createAndDisburseLoanOnDate(clientForTest, BigDecimal.valueOf(9000), "01 January 2026");

            // Partial repayment on day 10 (loan stays ACTIVE with 2000 outstanding)
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-10");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(7000), "10 January 2026"));

            GetWorkingCapitalLoansLoanIdResponse afterRepayment = wcLoanHelper.getLoanDetails(loanId);
            assertNotNull(afterRepayment.getBalance(), "Balance should exist after repayment");
            assertEqualBigDecimal(BigDecimal.valueOf(2000), afterRepayment.getBalance().getPrincipalOutstanding(),
                    "Outstanding should be 2000 after 7000 repayment on 9000 loan");

            // Backdated repayment on day 5 — total repaid (5000 + 7000 = 12000) exceeds principal (9000)
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-15");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(5000), "05 January 2026"));

            // Totals are order-independent: 9000 principal repaid, 3000 overpayment
            GetWorkingCapitalLoansLoanIdResponse afterBackdated = wcLoanHelper.getLoanDetails(loanId);
            assertNotNull(afterBackdated.getBalance(), "Balance should exist after backdated repayment");
            assertEqualBigDecimal(BigDecimal.ZERO, afterBackdated.getBalance().getPrincipalOutstanding(),
                    "Outstanding should be 0 — principal is fully repaid");
            assertEqualBigDecimal(BigDecimal.valueOf(9000), afterBackdated.getBalance().getPrincipalPaid(),
                    "Principal paid should be 9000 — capped at total principal");
            assertEqualBigDecimal(BigDecimal.valueOf(3000), afterBackdated.getBalance().getOverpaymentAmount(),
                    "Overpayment should be 3000 (5000 + 7000 - 9000 principal)");

            // Overpaying the loan triggers a chronological redistribution: the backdated day-5 repayment is now the
            // earliest, so it allocates first against the full 9000 outstanding (5000 to principal), leaving 4000 for
            // the day-10 repayment; the remaining 3000 of the day-10 repayment becomes overpayment.
            List<GetWorkingCapitalLoanTransactionIdResponse> transactions = wcLoanHelper.getTransactions(loanId);
            assertAllocation(findTransaction(transactions, LocalDate.of(2026, 1, 5), BigDecimal.valueOf(5000)), BigDecimal.valueOf(5000));
            assertAllocation(findTransaction(transactions, LocalDate.of(2026, 1, 10), BigDecimal.valueOf(7000)), BigDecimal.valueOf(4000));
        });
    }

    @Test
    void testMultipleBackdatedRepaymentsAccumulateCorrectly() {
        businessDateHelper.runAt("2026-01-01", () -> {
            Long clientForTest = clientHelper.createClient("01 January 2026");
            Long loanId = createAndDisburseLoanOnDate(clientForTest, BigDecimal.valueOf(9000), "01 January 2026");

            // First repayment on day 15
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-15");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(3000), "15 January 2026"));

            GetWorkingCapitalLoansLoanIdResponse afterFirst = wcLoanHelper.getLoanDetails(loanId);
            assertNotNull(afterFirst.getBalance());
            assertEqualBigDecimal(BigDecimal.valueOf(6000), afterFirst.getBalance().getPrincipalOutstanding(),
                    "Outstanding should be 6000 after first repayment");

            // Backdated repayment on day 5
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-20");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(1000), "05 January 2026"));

            GetWorkingCapitalLoansLoanIdResponse afterSecond = wcLoanHelper.getLoanDetails(loanId);
            assertNotNull(afterSecond.getBalance());
            assertEqualBigDecimal(BigDecimal.valueOf(5000), afterSecond.getBalance().getPrincipalOutstanding(),
                    "Outstanding should be 5000 after 4000 total repaid");
            assertEqualBigDecimal(BigDecimal.valueOf(4000), afterSecond.getBalance().getPrincipalPaid(),
                    "Principal paid should be 4000 (1000 + 3000)");

            // Another backdated repayment on day 10 (between existing ones)
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(2000), "10 January 2026"));

            GetWorkingCapitalLoansLoanIdResponse afterThird = wcLoanHelper.getLoanDetails(loanId);
            assertNotNull(afterThird.getBalance());
            assertEqualBigDecimal(BigDecimal.valueOf(3000), afterThird.getBalance().getPrincipalOutstanding(),
                    "Outstanding should be 3000 after 6000 total repaid");
            assertEqualBigDecimal(BigDecimal.valueOf(6000), afterThird.getBalance().getPrincipalPaid(),
                    "Principal paid should be 6000 (1000 + 2000 + 3000)");
        });
    }

    @Test
    void testUndoRepayment_onOverpaidChargeFreeLoan_reallocatesRemainingRepayment() {
        businessDateHelper.runAt("2026-01-01", () -> {
            Long clientForTest = clientHelper.createClient("01 January 2026");
            Long loanId = createAndDisburseLoanOnDate(clientForTest, BigDecimal.valueOf(9000), "01 January 2026");

            // Repayment on day 5 (3000) leaves 6000 outstanding.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-05");
            final Long day5TxnId = wcLoanHelper.makeRepayment(loanId,
                    WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(3000), "05 January 2026"));

            // Repayment on day 10 (6500) overpays: total repaid 9500 on a 9000 loan, so the day-10 repayment stores
            // 6000 principal + 500 overpayment.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-10");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(6500), "10 January 2026"));

            GetWorkingCapitalLoansLoanIdResponse afterOverpay = wcLoanHelper.getLoanDetails(loanId);
            assertEqualBigDecimal(BigDecimal.valueOf(9000), afterOverpay.getBalance().getPrincipalPaid(),
                    "Principal paid should be capped at 9000 after the overpaying day-10 repayment");
            assertEqualBigDecimal(BigDecimal.valueOf(500), afterOverpay.getBalance().getOverpaymentAmount(),
                    "Overpayment should be 500 (9500 - 9000)");

            // Undo the day-5 repayment. Total remaining repaid is now 6500 on a 9000 loan → no overpayment, and the
            // remaining day-10 repayment must re-allocate its full 6500 to principal (its former 500 overpayment folds
            // back into principal).
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-15");
            wcLoanHelper.undoTransaction(loanId, day5TxnId, WorkingCapitalLoanRequestBuilders.undoTransaction());

            GetWorkingCapitalLoansLoanIdResponse afterUndo = wcLoanHelper.getLoanDetails(loanId);
            assertNotNull(afterUndo.getBalance(), "Balance should exist after undo");
            assertEqualBigDecimal(BigDecimal.valueOf(6500), afterUndo.getBalance().getPrincipalPaid(),
                    "Principal paid should be 6500 after undoing the day-5 repayment");
            assertEqualBigDecimal(BigDecimal.ZERO, afterUndo.getBalance().getOverpaymentAmount(),
                    "Overpayment should be 0 — the remaining 6500 is under the 9000 principal");
            assertEqualBigDecimal(BigDecimal.valueOf(2500), afterUndo.getBalance().getPrincipalOutstanding(),
                    "Principal outstanding should be 2500 (9000 - 6500)");
            assertEquals(Boolean.TRUE, afterUndo.getStatus().getActive(), "Loan must be active after the undo");

            // The remaining day-10 repayment must re-allocate its full 6500 to principal (was 6000 + 500 overpayment).
            List<GetWorkingCapitalLoanTransactionIdResponse> transactions = wcLoanHelper.getTransactions(loanId);
            GetWorkingCapitalLoanTransactionIdResponse day10 = findTransaction(transactions, LocalDate.of(2026, 1, 10),
                    BigDecimal.valueOf(6500));
            assertEqualBigDecimal(BigDecimal.valueOf(6500), day10.getPrincipalPortion(),
                    "The remaining day-10 repayment must re-allocate its full 6500 to principal after the undo");
        });
    }

    @Test
    void testNonBackdatedRepaymentDoesNotTriggerReprocessing() {
        businessDateHelper.runAt("2026-01-01", () -> {
            Long clientForTest = clientHelper.createClient("01 January 2026");
            Long loanId = createAndDisburseLoanOnDate(clientForTest, BigDecimal.valueOf(9000), "01 January 2026");

            // Sequential repayments (not backdated — each on or after the business date)
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-05");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(2000), "05 January 2026"));

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-10");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(3000), "10 January 2026"));

            // Verify balance is the simple sum — no reprocessing side effects
            GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            assertNotNull(loan.getBalance());
            assertEqualBigDecimal(BigDecimal.valueOf(4000), loan.getBalance().getPrincipalOutstanding(),
                    "Outstanding should be 4000 after sequential 2000 + 3000 repayments");
            assertEqualBigDecimal(BigDecimal.valueOf(5000), loan.getBalance().getPrincipalPaid(),
                    "Principal paid should be 5000 after sequential repayments");
            assertEqualBigDecimal(BigDecimal.ZERO, loan.getBalance().getOverpaymentAmount(),
                    "No overpayment expected for sequential repayments under principal");
        });
    }

    @Test
    void testBackdatedRepayment_priorReversedTransactionInSuffix_isNotDoubleCounted() {
        businessDateHelper.runAt("2026-01-01", () -> {
            Long clientForTest = clientHelper.createClient("01 January 2026");
            Long loanId = createAndDisburseLoanOnDate(clientForTest, BigDecimal.valueOf(9000), "01 January 2026");

            // Day 10: repayment 3000, then undo it (charge-free, not overpaid → simple undo). It is now reversed but
            // sits on day 10, inside the suffix of the later backdated day-5 repayment.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-10");
            final Long reversedDay10 = wcLoanHelper.makeRepayment(loanId,
                    WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(3000), "10 January 2026"));
            wcLoanHelper.undoTransaction(loanId, reversedDay10, WorkingCapitalLoanRequestBuilders.undoTransaction());

            // Day 20: repayment 8000 (loan back to 9000 outstanding after the undo, so 1000 remains).
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-20");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(8000), "20 January 2026"));

            // Backdated day 5: repayment 5000. Non-reversed repaid is now 8000 + 5000 = 13000 on a 9000 loan → overpaid
            // by 4000, which triggers the suffix reprocess with boundary = day 5. The suffix contains the reversed
            // day-10 transaction, which must be ignored.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-25");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(5000), "05 January 2026"));

            GetWorkingCapitalLoansLoanIdResponse afterBackdated = wcLoanHelper.getLoanDetails(loanId);
            assertNotNull(afterBackdated.getBalance(), "Balance should exist after the backdated repayment");
            assertEqualBigDecimal(BigDecimal.valueOf(9000), afterBackdated.getBalance().getPrincipalPaid(),
                    "Principal paid should be capped at 9000 (reversed day-10 must not be counted)");
            assertEqualBigDecimal(BigDecimal.valueOf(4000), afterBackdated.getBalance().getOverpaymentAmount(),
                    "Overpayment should be exactly 4000 (13000 - 9000); a double-counted reversal would skew this");
            assertEqualBigDecimal(BigDecimal.ZERO, afterBackdated.getBalance().getPrincipalOutstanding(),
                    "Principal should be fully repaid");

            // Chronological redistribution over the two live repayments: day-5 (5000) allocates first against the full
            // 9000, then day-20 (8000) covers the remaining 4000 principal and books 4000 overpayment.
            List<GetWorkingCapitalLoanTransactionIdResponse> transactions = wcLoanHelper.getTransactions(loanId);
            assertAllocation(findTransaction(transactions, LocalDate.of(2026, 1, 5), BigDecimal.valueOf(5000)), BigDecimal.valueOf(5000));
            assertAllocation(findTransaction(transactions, LocalDate.of(2026, 1, 20), BigDecimal.valueOf(8000)), BigDecimal.valueOf(4000));
        });
    }

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
        String uniqueName = "WCL Reprocess " + Utils.uniqueRandomStringGenerator("", 8);
        String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        Long productId = productHelper
                .createWorkingCapitalLoanProduct(
                        new WorkingCapitalLoanProductTestBuilder().withName(uniqueName).withShortName(uniqueShortName).build())
                .getResourceId();
        createdProductIds.add(productId);
        return productId;
    }

    private static GetWorkingCapitalLoanTransactionIdResponse findTransaction(List<GetWorkingCapitalLoanTransactionIdResponse> transactions,
            LocalDate transactionDate, BigDecimal amount) {
        return transactions.stream().filter(txn -> transactionDate.equals(txn.getTransactionDate()))
                .filter(txn -> txn.getTransactionAmount() != null && amount.compareTo(txn.getTransactionAmount()) == 0).findFirst()
                .orElseThrow(() -> new AssertionError("Transaction not found on " + transactionDate + " with amount " + amount));
    }

    private static void assertAllocation(GetWorkingCapitalLoanTransactionIdResponse transaction, BigDecimal expectedPrincipalPortion) {
        String context = "Transaction on " + transaction.getTransactionDate() + " amount " + transaction.getTransactionAmount();
        assertEqualBigDecimal(expectedPrincipalPortion, transaction.getPrincipalPortion(), context + " — principal portion");
        assertEqualBigDecimal(BigDecimal.ZERO, transaction.getFeeChargesPortion(), context + " — fee charges portion");
        assertEqualBigDecimal(BigDecimal.ZERO, transaction.getPenaltyChargesPortion(), context + " — penalty charges portion");
    }

    private static void assertEqualBigDecimal(BigDecimal expected, BigDecimal actual, String message) {
        assertNotNull(actual, message + " — value was null");
        assertEquals(0, expected.compareTo(actual), message + " — expected: " + expected + " but was: " + actual);
    }
}
