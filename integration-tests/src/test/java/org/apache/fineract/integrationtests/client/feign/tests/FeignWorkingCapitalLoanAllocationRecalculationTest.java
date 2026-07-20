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
import static org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper.findTransaction;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
 * Verifies that after a backdated transaction triggers reprocessing, the recomputed transaction allocations are correct
 * and queryable through the transaction read endpoint - the reprocessing engine updates the separately stored
 * allocation of each affected transaction in place rather than reversing/replacing transactions.
 */
public class FeignWorkingCapitalLoanAllocationRecalculationTest extends FeignIntegrationTest {

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
    @DisplayName("A charge-free backdated repayment that overpays realigns the later repayment's principal split, queryable per transaction")
    void chargeFreeBackdatedOverpay_realignsLaterRepaymentAllocation() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long client = clientHelper.createClient("01 January 2026");
            final Long loanId = createAndDisburseLoanOnDate(client, BigDecimal.valueOf(9000), "01 January 2026");

            // R1 on day 20: allocates entirely to principal (5000 of 9000 outstanding).
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-20");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(5000), "20 January 2026"));

            // Backdated R2 on day 10 (before R1) for 6000. Chronologically R2 now covers principal first (6000 of
            // 9000),
            // leaving R1 to cover the remaining 3000 of principal and 2000 as overpayment - a charge-free suffix
            // reprocessing must realign R1 from 5000-principal to 3000-principal + 2000-overpayment.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-25");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(6000), "10 January 2026"));

            final List<GetWorkingCapitalLoanTransactionIdResponse> transactions = wcLoanHelper.getTransactions(loanId);

            final GetWorkingCapitalLoanTransactionIdResponse r2 = findTransaction(transactions, LocalDate.of(2026, 1, 10),
                    BigDecimal.valueOf(6000));
            assertEqualBigDecimal(BigDecimal.valueOf(6000), r2.getPrincipalPortion(), "Backdated R2 covers 6000 principal");
            assertAllocationWithinAmount(r2);

            final GetWorkingCapitalLoanTransactionIdResponse r1 = findTransaction(transactions, LocalDate.of(2026, 1, 20),
                    BigDecimal.valueOf(5000));
            assertEqualBigDecimal(BigDecimal.valueOf(3000), r1.getPrincipalPortion(),
                    "R1 principal is realigned to 3000 (the rest of its 5000 spilled to overpayment)");
            assertAllocationWithinAmount(r1);

            final GetBalance balance = balanceOf(loanId);
            assertEqualBigDecimal(BigDecimal.ZERO, balance.getPrincipalOutstanding(), "Principal is fully repaid");
            assertEqualBigDecimal(BigDecimal.valueOf(2000), balance.getOverpaymentAmount(), "Overpayment is 2000");
        });
    }

    @Test
    @DisplayName("A charged backdated repayment moves the fee settlement to the earlier transaction and reallocates the later one to principal")
    void chargedBackdatedRepayment_movesFeeSettlementToEarlierTransaction() {
        businessDateHelper.runAt("2026-02-01", () -> {
            final Long client = clientHelper.createClient("01 February 2026");
            final Long loanId = createAndDisburseLoanOnDate(client, BigDecimal.valueOf(9000), "01 February 2026");
            addCharge(loanId, false, 100, "01 February 2026");

            // R1 on day 20: the only outstanding charge is the 100 fee, so the 100 repayment settles it entirely.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-02-20");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(100), "20 February 2026"));

            final GetWorkingCapitalLoanTransactionIdResponse r1Before = findTransaction(wcLoanHelper.getTransactions(loanId),
                    LocalDate.of(2026, 2, 20), BigDecimal.valueOf(100));
            assertEqualBigDecimal(BigDecimal.valueOf(100), r1Before.getFeeChargesPortion(), "Before reprocessing R1 settles the fee");

            // Backdated R2 on day 10 settles the fee first when replayed chronologically, so the full reprocess must
            // move
            // the fee settlement onto R2 and reallocate R1 entirely to principal.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-02-25");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(100), "10 February 2026"));

            final List<GetWorkingCapitalLoanTransactionIdResponse> transactions = wcLoanHelper.getTransactions(loanId);

            final GetWorkingCapitalLoanTransactionIdResponse r2 = findTransaction(transactions, LocalDate.of(2026, 2, 10),
                    BigDecimal.valueOf(100));
            assertEqualBigDecimal(BigDecimal.valueOf(100), r2.getFeeChargesPortion(), "Backdated R2 now settles the fee");
            assertEqualBigDecimal(BigDecimal.ZERO, r2.getPrincipalPortion(), "Backdated R2 allocates nothing to principal");

            final GetWorkingCapitalLoanTransactionIdResponse r1 = findTransaction(transactions, LocalDate.of(2026, 2, 20),
                    BigDecimal.valueOf(100));
            assertEqualBigDecimal(BigDecimal.ZERO, r1.getFeeChargesPortion(), "R1 no longer settles the fee");
            assertEqualBigDecimal(BigDecimal.valueOf(100), r1.getPrincipalPortion(), "R1 is reallocated entirely to principal");
        });
    }

    private static void assertAllocationWithinAmount(final GetWorkingCapitalLoanTransactionIdResponse txn) {
        final BigDecimal allocated = nullToZero(txn.getPrincipalPortion()).add(nullToZero(txn.getFeeChargesPortion()))
                .add(nullToZero(txn.getPenaltyChargesPortion()));
        assertTrue(allocated.compareTo(txn.getTransactionAmount()) <= 0,
                "Allocated portions (" + allocated + ") must never exceed the transaction amount (" + txn.getTransactionAmount() + ")");
    }

    private static BigDecimal nullToZero(final BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private Long addCharge(final Long loanId, final boolean penalty, final double amount, final String dueDate) {
        final Long chargeId = wcLoanHelper.createGlobalCharge(WorkingCapitalLoanRequestBuilders.specifiedDueDateCharge(penalty, amount));
        return wcLoanHelper.addCharge(loanId, WorkingCapitalLoanRequestBuilders.addCharge(chargeId, amount, dueDate));
    }

    private GetBalance balanceOf(final Long loanId) {
        final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
        assertNotNull(loan.getBalance(), "Balance should exist");
        return loan.getBalance();
    }

    private Long createAndDisburseLoanOnDate(final Long clientId, final BigDecimal principal, final String date) {
        final Long productId = createProduct();
        final Long loanId = wcLoanHelper.submitApplication(
                WorkingCapitalLoanRequestBuilders.submitApplication(clientId, productId, principal, BigDecimal.valueOf(18), date, date));
        createdLoanIds.add(loanId);
        wcLoanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approve(date, principal, date));
        wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburse(date, principal));
        return loanId;
    }

    private Long createProduct() {
        final String uniqueName = "WCL AllocRecalc " + Utils.uniqueRandomStringGenerator("", 8);
        final String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        final Long productId = productHelper
                .createWorkingCapitalLoanProduct(
                        new WorkingCapitalLoanProductTestBuilder().withName(uniqueName).withShortName(uniqueShortName).build())
                .getResourceId();
        createdProductIds.add(productId);
        return productId;
    }
}
