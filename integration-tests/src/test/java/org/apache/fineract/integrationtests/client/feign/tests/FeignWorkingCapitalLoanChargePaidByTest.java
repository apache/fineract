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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.client.models.GetWorkingCapitalLoanChargePaidByData;
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionIdResponse;
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
 * Verifies the charge-paid-by cross-tabulation exposed on each transaction: which charge a repayment settled and for
 * how much. The rows are output rebuilt from the recomputed allocations, so a backdated repayment that reshuffles
 * charge settlement must re-attribute them to the correct transaction.
 */
public class FeignWorkingCapitalLoanChargePaidByTest extends FeignIntegrationTest {

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
    @DisplayName("A repayment settling a fee and a penalty produces one paid-by row per charge, attributed to the transaction")
    void repaymentSettlingCharges_producesPaidByRowPerCharge() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long client = clientHelper.createClient("01 January 2026");
            final Long loanId = createAndDisburseLoanOnDate(client, BigDecimal.valueOf(9000), "01 January 2026");
            final Long penaltyChargeId = addCharge(loanId, true, 50, "01 January 2026");
            final Long feeChargeId = addCharge(loanId, false, 100, "01 January 2026");

            // A single 200 repayment settles penalty (50) then fee (100) then 50 principal.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-10");
            final Long repaymentTxnId = wcLoanHelper.makeRepayment(loanId,
                    WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(200), "10 January 2026"));

            final GetWorkingCapitalLoanTransactionIdResponse repayment = findTransaction(wcLoanHelper.getTransactions(loanId),
                    LocalDate.of(2026, 1, 10), BigDecimal.valueOf(200));

            final List<GetWorkingCapitalLoanChargePaidByData> paidBy = paidByOf(repayment);
            assertEquals(2, paidBy.size(), "One paid-by row per settled charge (fee and penalty)");
            paidBy.forEach(row -> assertEquals(repaymentTxnId, row.getTransactionId(), "Each paid-by row points at the repayment"));

            final GetWorkingCapitalLoanChargePaidByData feePaidBy = paidByForCharge(paidBy, feeChargeId);
            assertEqualBigDecimal(BigDecimal.valueOf(100), feePaidBy.getAmount(), "Fee paid-by amount is 100");
            assertNotNull(feePaidBy.getName(), "The fee charge name is resolved so a client need not look it up separately");

            final GetWorkingCapitalLoanChargePaidByData penaltyPaidBy = paidByForCharge(paidBy, penaltyChargeId);
            assertEqualBigDecimal(BigDecimal.valueOf(50), penaltyPaidBy.getAmount(), "Penalty paid-by amount is 50");
        });
    }

    @Test
    @DisplayName("A principal-only repayment produces no paid-by rows")
    void principalOnlyRepayment_producesNoPaidByRows() {
        businessDateHelper.runAt("2026-02-01", () -> {
            final Long client = clientHelper.createClient("01 February 2026");
            final Long loanId = createAndDisburseLoanOnDate(client, BigDecimal.valueOf(9000), "01 February 2026");

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-02-10");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(3000), "10 February 2026"));

            final GetWorkingCapitalLoanTransactionIdResponse repayment = findTransaction(wcLoanHelper.getTransactions(loanId),
                    LocalDate.of(2026, 2, 10), BigDecimal.valueOf(3000));
            assertTrue(paidByOf(repayment).isEmpty(), "A repayment that touches no charge has no paid-by rows");
        });
    }

    @Test
    @DisplayName("A backdated repayment re-attributes the fee's paid-by row from the later transaction to the earlier one")
    void backdatedRepayment_reAttributesPaidByRows() {
        businessDateHelper.runAt("2026-03-01", () -> {
            final Long client = clientHelper.createClient("01 March 2026");
            final Long loanId = createAndDisburseLoanOnDate(client, BigDecimal.valueOf(9000), "01 March 2026");
            final Long feeChargeId = addCharge(loanId, false, 100, "01 March 2026");

            // R1 on day 20 settles the fee - so its paid-by row initially carries the fee.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-03-20");
            final Long r1Id = wcLoanHelper.makeRepayment(loanId,
                    WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(100), "20 March 2026"));

            // Backdated R2 on day 10 settles the fee first on replay; the paid-by row must move to R2.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-03-25");
            final Long r2Id = wcLoanHelper.makeRepayment(loanId,
                    WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(100), "10 March 2026"));

            final List<GetWorkingCapitalLoanTransactionIdResponse> transactions = wcLoanHelper.getTransactions(loanId);

            final GetWorkingCapitalLoanTransactionIdResponse r2 = findTransactionById(transactions, r2Id);
            final List<GetWorkingCapitalLoanChargePaidByData> r2PaidBy = paidByOf(r2);
            assertEquals(1, r2PaidBy.size(), "The earlier (backdated) transaction now carries the fee's paid-by row");
            assertEquals(feeChargeId, r2PaidBy.getFirst().getChargeId(), "R2 paid-by references the fee charge");
            assertEqualBigDecimal(BigDecimal.valueOf(100), r2PaidBy.getFirst().getAmount(), "R2 settled the full 100 fee");
            assertEquals(r2Id, r2PaidBy.getFirst().getTransactionId(), "R2 paid-by points at R2");

            final GetWorkingCapitalLoanTransactionIdResponse r1 = findTransactionById(transactions, r1Id);
            assertTrue(paidByOf(r1).isEmpty(), "The later transaction no longer settles the fee, so it has no paid-by rows");
        });
    }

    private static List<GetWorkingCapitalLoanChargePaidByData> paidByOf(final GetWorkingCapitalLoanTransactionIdResponse txn) {
        return txn.getChargePaidByList() != null ? txn.getChargePaidByList() : List.of();
    }

    private static GetWorkingCapitalLoanChargePaidByData paidByForCharge(final List<GetWorkingCapitalLoanChargePaidByData> rows,
            final Long chargeId) {
        return rows.stream().filter(row -> chargeId.equals(row.getChargeId())).findFirst()
                .orElseThrow(() -> new AssertionError("No paid-by row for charge " + chargeId + " in " + rows));
    }

    private static GetWorkingCapitalLoanTransactionIdResponse findTransactionById(
            final List<GetWorkingCapitalLoanTransactionIdResponse> transactions, final Long transactionId) {
        return transactions.stream().filter(txn -> transactionId.equals(txn.getId())).findFirst()
                .orElseThrow(() -> new AssertionError("Transaction not found with id " + transactionId));
    }

    private Long addCharge(final Long loanId, final boolean penalty, final double amount, final String dueDate) {
        final Long chargeId = wcLoanHelper.createGlobalCharge(WorkingCapitalLoanRequestBuilders.specifiedDueDateCharge(penalty, amount));
        return wcLoanHelper.addCharge(loanId, WorkingCapitalLoanRequestBuilders.addCharge(chargeId, amount, dueDate));
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
        final String uniqueName = "WCL PaidBy " + Utils.uniqueRandomStringGenerator("", 8);
        final String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        final Long productId = productHelper
                .createWorkingCapitalLoanProduct(
                        new WorkingCapitalLoanProductTestBuilder().withName(uniqueName).withShortName(uniqueShortName).build())
                .getResourceId();
        createdProductIds.add(productId);
        return productId;
    }
}
