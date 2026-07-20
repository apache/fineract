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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
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

public class FeignWorkingCapitalLoanChargeAdjustmentOverpaymentTest extends FeignIntegrationTest {

    private static final String STATUS_CLOSED_OBLIGATIONS_MET = "loanStatusType.closed.obligations.met";
    private static final String STATUS_OVERPAID = "loanStatusType.overpaid";
    private static final String STATUS_ACTIVE = "loanStatusType.active";

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
    @DisplayName("A charge adjustment on a closed (obligations met) loan is booked as overpayment and moves the loan to overpaid")
    void testChargeAdjustmentOnClosedLoanMovesToOverpaid() {
        businessDateHelper.runAt("2026-01-01", () -> {
            Long client = clientHelper.createClient("01 January 2026");
            Long loanId = createAndDisburseLoanOnDate(client, BigDecimal.valueOf(9000), "01 January 2026");
            Long feeLoanChargeId = addCharge(loanId, false, 100, "01 January 2026");

            // Repay principal (9000) + fee (100) to close the loan with the fee fully settled.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-12");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(9100), "12 January 2026"));

            assertEquals(STATUS_CLOSED_OBLIGATIONS_MET, statusCode(loanId), "Loan should be closed after repaying principal + fee");
            GetBalance closedBalance = balanceOf(loanId);
            assertEqualBigDecimal(BigDecimal.valueOf(100), closedBalance.getFeePaid(), "Fee paid should be 100 after the repayment");
            assertEqualBigDecimal(BigDecimal.ZERO, closedBalance.getOverpaymentAmount(), "No overpayment yet");

            // A 100 charge adjustment on the closed loan has nothing left to settle, so it is booked as overpayment.
            wcLoanHelper.adjustCharge(loanId, feeLoanChargeId, WorkingCapitalLoanRequestBuilders.chargeAdjustment(BigDecimal.valueOf(100)));

            assertEquals(STATUS_OVERPAID, statusCode(loanId), "Charge adjustment on the closed loan must move it to overpaid");
            GetBalance overpaidBalance = balanceOf(loanId);
            assertEqualBigDecimal(BigDecimal.valueOf(100), overpaidBalance.getOverpaymentAmount(),
                    "The full 100 adjustment must be booked as overpayment");
            assertEqualBigDecimal(BigDecimal.valueOf(100), overpaidBalance.getFeePaid(),
                    "Fee paid must stay 100 - the excess is overpayment, not a second charge payment");

            WorkingCapitalLoanChargeData feeCharge = findCharge(wcLoanHelper.getCharges(loanId), feeLoanChargeId);
            assertEqualBigDecimal(BigDecimal.valueOf(100), feeCharge.getAmountPaid(), "Charge amount paid must stay 100 (not over-paid)");
            assertEqualBigDecimal(BigDecimal.ZERO, feeCharge.getAmountOutstanding(), "Charge outstanding stays 0");
        });
    }

    @Test
    @DisplayName("Undoing a charge adjustment that overpaid a closed loan restores the closed state and clears the overpayment")
    void testUndoOfChargeAdjustmentOnClosedLoanRestoresClosedState() {
        businessDateHelper.runAt("2026-01-01", () -> {
            Long client = clientHelper.createClient("01 January 2026");
            Long loanId = createAndDisburseLoanOnDate(client, BigDecimal.valueOf(9000), "01 January 2026");
            Long feeLoanChargeId = addCharge(loanId, false, 100, "01 January 2026");

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-12");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(9100), "12 January 2026"));
            wcLoanHelper.adjustCharge(loanId, feeLoanChargeId, WorkingCapitalLoanRequestBuilders.chargeAdjustment(BigDecimal.valueOf(100)));
            assertEquals(STATUS_OVERPAID, statusCode(loanId), "Charge adjustment must move the closed loan to overpaid");

            // Undo the charge adjustment: the loan must return to exactly its pre-adjustment state (closed, no
            // overpayment, fee still settled) - not left overpaid with a stale overpayment and an unpaid charge.
            Long adjustmentTxnId = chargeAdjustmentTxnId(loanId);
            wcLoanHelper.undoTransaction(loanId, adjustmentTxnId, WorkingCapitalLoanRequestBuilders.undoTransaction());

            assertEquals(STATUS_CLOSED_OBLIGATIONS_MET, statusCode(loanId),
                    "Undoing the adjustment must return the loan to closed (obligations met)");
            GetBalance restoredBalance = balanceOf(loanId);
            assertEqualBigDecimal(BigDecimal.ZERO, restoredBalance.getOverpaymentAmount(),
                    "Overpayment must be cleared when the adjustment is undone");
            assertEqualBigDecimal(BigDecimal.valueOf(100), restoredBalance.getFeePaid(),
                    "Fee paid must stay 100 - the repayment still settled the fee");

            WorkingCapitalLoanChargeData feeCharge = findCharge(wcLoanHelper.getCharges(loanId), feeLoanChargeId);
            assertEqualBigDecimal(BigDecimal.valueOf(100), feeCharge.getAmountPaid(), "Charge must remain fully paid after the undo");
            assertEqualBigDecimal(BigDecimal.ZERO, feeCharge.getAmountOutstanding(), "Charge outstanding must stay 0 after the undo");
            assertTrue(feeCharge.getPaid(), "Charge must remain flagged paid after the undo");
        });
    }

    @Test
    @DisplayName("Undoing a charge adjustment on an active loan restores the charge outstanding and keeps the loan active")
    void testUndoOfChargeAdjustmentOnActiveLoanRestoresCharge() {
        businessDateHelper.runAt("2026-01-01", () -> {
            Long client = clientHelper.createClient("01 January 2026");
            Long loanId = createAndDisburseLoanOnDate(client, BigDecimal.valueOf(9000), "01 January 2026");
            Long feeLoanChargeId = addCharge(loanId, false, 100, "01 January 2026");

            // On an active loan a 100 adjustment settles the fee (existing behaviour); the loan stays active.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-05");
            wcLoanHelper.adjustCharge(loanId, feeLoanChargeId, WorkingCapitalLoanRequestBuilders.chargeAdjustment(BigDecimal.valueOf(100)));
            assertEquals(STATUS_ACTIVE, statusCode(loanId), "Adjustment on the active loan must leave it active");
            assertEqualBigDecimal(BigDecimal.valueOf(100), balanceOf(loanId).getFeePaid(), "Fee paid should be 100 after the adjustment");

            // Undo must restore the charge to outstanding and keep the loan active - no overpayment introduced.
            Long adjustmentTxnId = chargeAdjustmentTxnId(loanId);
            wcLoanHelper.undoTransaction(loanId, adjustmentTxnId, WorkingCapitalLoanRequestBuilders.undoTransaction());

            assertEquals(STATUS_ACTIVE, statusCode(loanId), "Loan must remain active after undoing the adjustment");
            GetBalance restored = balanceOf(loanId);
            assertEqualBigDecimal(BigDecimal.ZERO, restored.getFeePaid(), "Fee paid must be reverted to 0");
            assertEqualBigDecimal(BigDecimal.ZERO, restored.getOverpaymentAmount(), "No overpayment on an active-loan adjustment undo");

            WorkingCapitalLoanChargeData feeCharge = findCharge(wcLoanHelper.getCharges(loanId), feeLoanChargeId);
            assertEqualBigDecimal(BigDecimal.ZERO, feeCharge.getAmountPaid(), "Charge amount paid must be reverted to 0");
            assertEqualBigDecimal(BigDecimal.valueOf(100), feeCharge.getAmountOutstanding(), "Charge must be fully outstanding again");
        });
    }

    @Test
    @DisplayName("A charge adjustment on an already overpaid loan keeps it overpaid and increases the overpayment")
    void testChargeAdjustmentOnOverpaidLoanIncreasesOverpayment() {
        businessDateHelper.runAt("2026-01-01", () -> {
            Long client = clientHelper.createClient("01 January 2026");
            Long loanId = createAndDisburseLoanOnDate(client, BigDecimal.valueOf(9000), "01 January 2026");
            Long feeLoanChargeId = addCharge(loanId, false, 100, "01 January 2026");

            // Repay principal (9000) + fee (100) + 200 extra to drive the loan overpaid by 200.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-12");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(9300), "12 January 2026"));

            assertEquals(STATUS_OVERPAID, statusCode(loanId), "Loan should be overpaid after repaying more than principal + fee");
            assertEqualBigDecimal(BigDecimal.valueOf(200), balanceOf(loanId).getOverpaymentAmount(), "Overpayment should be 200");

            // A 100 charge adjustment on the overpaid loan keeps it overpaid and adds to the overpayment.
            wcLoanHelper.adjustCharge(loanId, feeLoanChargeId, WorkingCapitalLoanRequestBuilders.chargeAdjustment(BigDecimal.valueOf(100)));

            assertEquals(STATUS_OVERPAID, statusCode(loanId), "Loan should stay overpaid after the charge adjustment");
            GetBalance overpaidBalance = balanceOf(loanId);
            assertEqualBigDecimal(BigDecimal.valueOf(300), overpaidBalance.getOverpaymentAmount(),
                    "Overpayment should grow from 200 to 300");
            assertEqualBigDecimal(BigDecimal.valueOf(100), overpaidBalance.getFeePaid(), "Fee paid must stay 100");
        });
    }

    @Test
    @DisplayName("A partial charge adjustment on a closed loan is still fully booked as overpayment")
    void testPartialChargeAdjustmentOnClosedLoanMovesToOverpaid() {
        businessDateHelper.runAt("2026-01-01", () -> {
            Long client = clientHelper.createClient("01 January 2026");
            Long loanId = createAndDisburseLoanOnDate(client, BigDecimal.valueOf(9000), "01 January 2026");
            Long feeLoanChargeId = addCharge(loanId, false, 100, "01 January 2026");

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-12");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(9100), "12 January 2026"));
            assertEquals(STATUS_CLOSED_OBLIGATIONS_MET, statusCode(loanId), "Loan should be closed after repaying principal + fee");

            // A partial (40 of 100) adjustment on the closed loan still has nothing to settle, so all 40 is
            // overpayment.
            wcLoanHelper.adjustCharge(loanId, feeLoanChargeId, WorkingCapitalLoanRequestBuilders.chargeAdjustment(BigDecimal.valueOf(40)));

            assertEquals(STATUS_OVERPAID, statusCode(loanId), "A partial charge adjustment on the closed loan must move it to overpaid");
            GetBalance overpaidBalance = balanceOf(loanId);
            assertEqualBigDecimal(BigDecimal.valueOf(40), overpaidBalance.getOverpaymentAmount(),
                    "The full 40 adjustment must be booked as overpayment");
            assertEqualBigDecimal(BigDecimal.valueOf(100), overpaidBalance.getFeePaid(), "Fee paid must stay 100");

            WorkingCapitalLoanChargeData feeCharge = findCharge(wcLoanHelper.getCharges(loanId), feeLoanChargeId);
            assertEqualBigDecimal(BigDecimal.valueOf(100), feeCharge.getAmountPaid(), "Charge amount paid must stay 100 (not over-paid)");
            assertTrue(feeCharge.getPaid(), "Charge stays fully settled");
        });
    }

    private String statusCode(Long loanId) {
        GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
        assertNotNull(loan.getStatus(), "Loan status should exist");
        return loan.getStatus().getCode();
    }

    private Long chargeAdjustmentTxnId(Long loanId) {
        return wcLoanHelper.getTransactions(loanId).stream()
                .filter(txn -> txn.getType() != null && "loanTransactionType.chargeAdjustment".equals(txn.getType().getCode()))
                .filter(txn -> !Boolean.TRUE.equals(txn.getReversed())).map(GetWorkingCapitalLoanTransactionIdResponse::getId).findFirst()
                .orElseThrow(() -> new IllegalStateException("No charge adjustment transaction found for loan " + loanId));
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
        String uniqueName = "WCL ChargeAdjOverpay " + Utils.uniqueRandomStringGenerator("", 8);
        String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        Long productId = productHelper
                .createWorkingCapitalLoanProduct(
                        new WorkingCapitalLoanProductTestBuilder().withName(uniqueName).withShortName(uniqueShortName).build())
                .getResourceId();
        createdProductIds.add(productId);
        return productId;
    }
}
