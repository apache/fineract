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

/**
 * Correctness tests for the WC transaction reprocessing engine when a charge adjustment has already settled part of a
 * charge before the reprocessing-triggering (backdated) repayment.
 */
public class FeignWorkingCapitalLoanReprocessingCorrectnessTest extends FeignIntegrationTest {

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
    @DisplayName("Reprocessing after a backdated repayment preserves a prior charge adjustment (no double-settlement of the fee)")
    void backdatedRepayment_preservesChargeAdjustment() {
        businessDateHelper.runAt("2026-01-01", () -> {
            Long client = clientHelper.createClient("01 January 2026");
            Long loanId = createAndDisburseLoanOnDate(client, BigDecimal.valueOf(9000), "01 January 2026");
            Long feeLoanChargeId = addCharge(loanId, false, 100, "01 January 2026");

            // A 40 charge adjustment partially settles the 100 fee: amountPaid 40, outstanding 60.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-05");
            wcLoanHelper.adjustCharge(loanId, feeLoanChargeId,
                    WorkingCapitalLoanRequestBuilders.chargeAdjustment(BigDecimal.valueOf(40), "05 January 2026"));

            // R1 on day 10: settles the remaining 60 of the fee, then the leftover would be nothing (60 == remaining).
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-20");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(60), "10 January 2026"));

            // Backdated R2 on day 8 (before R1) triggers reprocessing. The prior 40 adjustment must survive: the fee's
            // remaining outstanding is only 60, so the replayed repayments together settle only the 60 remainder while
            // the rest goes to principal.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-25");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(50), "08 January 2026"));

            // Balance: fee fully paid (40 adjustment + 60 repayment), 50 to principal.
            GetBalance balance = balanceOf(loanId);
            assertEqualBigDecimal(BigDecimal.valueOf(100), balance.getFeePaid(),
                    "Fee paid should be 100 (40 adjustment + 60 repayment), not double-settled");
            assertEqualBigDecimal(BigDecimal.valueOf(50), balance.getPrincipalPaid(),
                    "Principal paid should be 50 — the 100 repaid minus the 50 that settled the remaining fee");
            assertEqualBigDecimal(BigDecimal.valueOf(8950), balance.getPrincipalOutstanding(), "Principal outstanding should be 9000 - 50");

            // The fee charge is settled exactly once to 100.
            WorkingCapitalLoanChargeData feeCharge = findCharge(wcLoanHelper.getCharges(loanId), feeLoanChargeId);
            assertEqualBigDecimal(BigDecimal.valueOf(100), feeCharge.getAmountPaid(), "Fee charge amount paid should be 100 (not > 100)");
            assertTrue(feeCharge.getPaid(), "Fee charge should be flagged paid");

            // Allocation: the earlier day-8 repayment settles the remaining 50 of the fee; the day-10 repayment settles
            // the last 10 of the fee and 50 to principal.
            List<GetWorkingCapitalLoanTransactionIdResponse> transactions = wcLoanHelper.getTransactions(loanId);
            GetWorkingCapitalLoanTransactionIdResponse r2 = findTransaction(transactions, LocalDate.of(2026, 1, 8), BigDecimal.valueOf(50));
            assertEqualBigDecimal(BigDecimal.valueOf(50), r2.getFeeChargesPortion(), "Day-8 repayment settles 50 of the remaining fee");
            assertEqualBigDecimal(BigDecimal.ZERO, r2.getPrincipalPortion(), "Day-8 repayment allocates nothing to principal");
            GetWorkingCapitalLoanTransactionIdResponse r1 = findTransaction(transactions, LocalDate.of(2026, 1, 10),
                    BigDecimal.valueOf(60));
            assertEqualBigDecimal(BigDecimal.valueOf(10), r1.getFeeChargesPortion(), "Day-10 repayment settles the last 10 of the fee");
            assertEqualBigDecimal(BigDecimal.valueOf(50), r1.getPrincipalPortion(), "Day-10 repayment allocates 50 to principal");
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
        String uniqueName = "WCL ReprocessCorrect " + Utils.uniqueRandomStringGenerator("", 8);
        String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        Long productId = productHelper
                .createWorkingCapitalLoanProduct(
                        new WorkingCapitalLoanProductTestBuilder().withName(uniqueName).withShortName(uniqueShortName).build())
                .getResourceId();
        createdProductIds.add(productId);
        return productId;
    }
}
