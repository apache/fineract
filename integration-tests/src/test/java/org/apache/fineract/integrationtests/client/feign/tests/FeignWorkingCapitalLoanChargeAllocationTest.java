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
import static org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper.findCharge;
import static org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper.findTransaction;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.client.models.GetBalance;
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.WorkingCapitalLoanChargeData;
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

public class FeignWorkingCapitalLoanChargeAllocationTest extends FeignIntegrationTest {

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
    @DisplayName("A single repayment splits across penalty, fee and principal in the product's configured allocation order")
    void testRepaymentSplitsAcrossPenaltyFeeAndPrincipalInConfiguredOrder() {
        businessDateHelper.runAt("2026-01-01", () -> {
            Long client = clientHelper.createClient("01 January 2026");
            Long loanId = createAndDisburseLoanOnDate(client, BigDecimal.valueOf(9000), "01 January 2026");

            // Charges due on the disbursement date (business date is 01 Jan, so the due date is not in the past).
            Long penaltyLoanChargeId = addCharge(loanId, true, 50, "01 January 2026");
            Long feeLoanChargeId = addCharge(loanId, false, 100, "01 January 2026");

            // A single 200 repayment must consume penalty (50) then fee (100) then principal (50) in that order.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-10");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(200), "10 January 2026"));

            // Balance buckets reflect the split.
            GetBalance balance = balanceOf(loanId);
            assertEqualBigDecimal(BigDecimal.valueOf(50), balance.getPenaltyPaid(), "Penalty paid should be the full 50 penalty charge");
            assertEqualBigDecimal(BigDecimal.valueOf(100), balance.getFeePaid(), "Fee paid should be the full 100 fee charge");
            assertEqualBigDecimal(BigDecimal.valueOf(50), balance.getPrincipalPaid(), "Principal paid should be the leftover 50");
            assertEqualBigDecimal(BigDecimal.valueOf(8950), balance.getPrincipalOutstanding(), "Principal outstanding should be 9000 - 50");
            assertEqualBigDecimal(BigDecimal.ZERO, balance.getOverpaymentAmount(), "No overpayment — everything was absorbed");

            // The transaction allocation records the same split.
            GetWorkingCapitalLoanTransactionIdResponse repayment = findTransaction(wcLoanHelper.getTransactions(loanId),
                    LocalDate.of(2026, 1, 10), BigDecimal.valueOf(200));
            assertEqualBigDecimal(BigDecimal.valueOf(50), repayment.getPenaltyChargesPortion(), "Penalty portion of the repayment");
            assertEqualBigDecimal(BigDecimal.valueOf(100), repayment.getFeeChargesPortion(), "Fee portion of the repayment");
            assertEqualBigDecimal(BigDecimal.valueOf(50), repayment.getPrincipalPortion(), "Principal portion of the repayment");

            // Both charges are fully settled.
            List<WorkingCapitalLoanChargeData> charges = wcLoanHelper.getCharges(loanId);
            WorkingCapitalLoanChargeData penaltyCharge = findCharge(charges, penaltyLoanChargeId);
            assertEqualBigDecimal(BigDecimal.valueOf(50), penaltyCharge.getAmountPaid(), "Penalty charge amount paid");
            assertTrue(penaltyCharge.getPaid(), "Penalty charge should be flagged paid");
            WorkingCapitalLoanChargeData feeCharge = findCharge(charges, feeLoanChargeId);
            assertEqualBigDecimal(BigDecimal.valueOf(100), feeCharge.getAmountPaid(), "Fee charge amount paid");
            assertTrue(feeCharge.getPaid(), "Fee charge should be flagged paid");
        });
    }

    @Test
    @DisplayName("A backdated repayment triggers reprocessing that redistributes the later repayment's charge allocation onto principal")
    void testBackdatedRepaymentReprocessingRedistributesChargeAllocation() {
        businessDateHelper.runAt("2026-01-01", () -> {
            Long client = clientHelper.createClient("01 January 2026");
            Long loanId = createAndDisburseLoanOnDate(client, BigDecimal.valueOf(9000), "01 January 2026");
            Long feeLoanChargeId = addCharge(loanId, false, 100, "01 January 2026");

            // R1 on day 20: the fee (due 01 Jan) is the only thing to settle, so the 100 goes entirely to the fee.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-20");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(100), "20 January 2026"));

            GetWorkingCapitalLoanTransactionIdResponse r1BeforeReprocessing = findTransaction(wcLoanHelper.getTransactions(loanId),
                    LocalDate.of(2026, 1, 20), BigDecimal.valueOf(100));
            assertEqualBigDecimal(BigDecimal.valueOf(100), r1BeforeReprocessing.getFeeChargesPortion(),
                    "Before reprocessing R1 settles the fee in full");
            assertEqualBigDecimal(BigDecimal.ZERO, r1BeforeReprocessing.getPrincipalPortion(),
                    "Before reprocessing R1 allocates nothing to principal");

            // Backdated R2 on day 10 (before R1) settles the fee first when replayed in chronological order, so
            // reprocessing must redistribute R1 from fee to principal.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-25");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(100), "10 January 2026"));

            List<GetWorkingCapitalLoanTransactionIdResponse> transactions = wcLoanHelper.getTransactions(loanId);
            // R2 (earlier) now settles the fee.
            GetWorkingCapitalLoanTransactionIdResponse r2 = findTransaction(transactions, LocalDate.of(2026, 1, 10),
                    BigDecimal.valueOf(100));
            assertEqualBigDecimal(BigDecimal.valueOf(100), r2.getFeeChargesPortion(), "Backdated R2 settles the fee");
            assertEqualBigDecimal(BigDecimal.ZERO, r2.getPrincipalPortion(), "Backdated R2 allocates nothing to principal");
            // R1 (later) is redistributed onto principal — the fee is already gone.
            GetWorkingCapitalLoanTransactionIdResponse r1 = findTransaction(transactions, LocalDate.of(2026, 1, 20),
                    BigDecimal.valueOf(100));
            assertEqualBigDecimal(BigDecimal.ZERO, r1.getFeeChargesPortion(), "After reprocessing R1 no longer settles the fee");
            assertEqualBigDecimal(BigDecimal.valueOf(100), r1.getPrincipalPortion(), "After reprocessing R1 is redistributed to principal");

            // Balance reflects the recomputed totals: fee fully paid once, 100 to principal.
            GetBalance balance = balanceOf(loanId);
            assertEqualBigDecimal(BigDecimal.valueOf(100), balance.getFeePaid(), "Fee paid is 100 (settled once)");
            assertEqualBigDecimal(BigDecimal.valueOf(100), balance.getPrincipalPaid(), "Principal paid is 100 after redistribution");
            assertEqualBigDecimal(BigDecimal.valueOf(8900), balance.getPrincipalOutstanding(), "Principal outstanding is 9000 - 100");
            assertEqualBigDecimal(BigDecimal.ZERO, balance.getOverpaymentAmount(), "No overpayment");

            WorkingCapitalLoanChargeData feeCharge = findCharge(wcLoanHelper.getCharges(loanId), feeLoanChargeId);
            assertEqualBigDecimal(BigDecimal.valueOf(100), feeCharge.getAmountPaid(), "Fee charge amount paid is 100 (not double-counted)");
            assertTrue(feeCharge.getPaid(), "Fee charge should be flagged paid");
        });
    }

    @Test
    @DisplayName("A charge adjustment partially settles the charge and refreshes the fee balance bucket without touching principal")
    void testChargeAdjustmentPartiallySettlesChargeAndRefreshesBalanceBucket() {
        businessDateHelper.runAt("2026-01-01", () -> {
            Long client = clientHelper.createClient("01 January 2026");
            Long loanId = createAndDisburseLoanOnDate(client, BigDecimal.valueOf(9000), "01 January 2026");
            Long feeLoanChargeId = addCharge(loanId, false, 100, "01 January 2026");

            // A 40 charge adjustment partially settles the 100 fee charge and bumps the fee bucket by 40.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-05");
            wcLoanHelper.adjustCharge(loanId, feeLoanChargeId,
                    WorkingCapitalLoanRequestBuilders.chargeAdjustment(BigDecimal.valueOf(40), "05 January 2026"));

            WorkingCapitalLoanChargeData feeCharge = findCharge(wcLoanHelper.getCharges(loanId), feeLoanChargeId);
            assertEqualBigDecimal(BigDecimal.valueOf(40), feeCharge.getAmountPaid(), "Fee charge amount paid should be 40");
            assertEqualBigDecimal(BigDecimal.valueOf(60), feeCharge.getAmountOutstanding(), "Fee charge outstanding should be 60");
            assertFalse(feeCharge.getPaid(), "Fee charge is only partially settled, so not flagged paid");

            GetBalance balance = balanceOf(loanId);
            assertEqualBigDecimal(BigDecimal.valueOf(40), balance.getFeePaid(), "Fee paid bucket should reflect the 40 adjustment");
            assertEqualBigDecimal(BigDecimal.valueOf(60), balance.getFeeOutstanding(), "Fee outstanding should be 100 - 40");
            assertEqualBigDecimal(BigDecimal.ZERO, balance.getPrincipalPaid(), "A charge adjustment must not touch the principal bucket");
        });
    }

    private Long addCharge(Long loanId, boolean penalty, double amount, String dueDate) {
        Long chargeId = wcLoanHelper.createGlobalCharge(WorkingCapitalLoanRequestBuilders.specifiedDueDateCharge(penalty, amount));
        return wcLoanHelper.addCharge(loanId, WorkingCapitalLoanRequestBuilders.addCharge(chargeId, amount, dueDate));
    }

    private GetBalance balanceOf(Long loanId) {
        GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
        assertNotNull(loan.getBalance(), "Balance should exist");
        return loan.getBalance();
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
        String uniqueName = "WCL ChargeAlloc " + Utils.uniqueRandomStringGenerator("", 8);
        String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        Long productId = productHelper
                .createWorkingCapitalLoanProduct(
                        new WorkingCapitalLoanProductTestBuilder().withName(uniqueName).withShortName(uniqueShortName).build())
                .getResourceId();
        createdProductIds.add(productId);
        return productId;
    }
}
