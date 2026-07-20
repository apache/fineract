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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.client.models.GetBalance;
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
 * Verifies that when a backdated transaction forces the full reset+replay reprocessing, a charge adjustment is replayed
 * to the same split the live processor produced.
 *
 * <p>
 * A charge adjustment is a repayment-type transaction, so reprocessing includes it in the replay set and re-derives its
 * allocation from scratch. It carries no privilege towards the charge it adjusts: both the live path and the replay run
 * it through the loan's configured payment allocation order for its type (falling back to DEFAULT). What this test pins
 * is that the two agree - the replay must land on the same buckets as the original booking, rather than shifting the
 * amount between the charge and principal because it was re-allocated at a different point in the sequence.
 */
public class FeignWorkingCapitalLoanChargeAdjustmentReprocessTest extends FeignIntegrationTest {

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
    @DisplayName("A backdated repayment that triggers a full reprocess re-allocates a charge adjustment to the same split the live path produced")
    void backdatedRepaymentReprocess_replaysChargeAdjustmentToTheSameSplit() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long client = clientHelper.createClient("01 January 2026");
            final Long loanId = createAndDisburseLoanOnDate(client, BigDecimal.valueOf(9000), "01 January 2026");

            // The fee falls due on the adjustment date, so the adjustment meets it as a DUE_FEE - which the default
            // allocation order ranks ahead of DUE_PRINCIPAL - and settles it.
            final Long feeLoanChargeId = addCharge(loanId, false, 100, "05 January 2026");

            // Adjust the fee by its full amount on its due date.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-05");
            wcLoanHelper.adjustCharge(loanId, feeLoanChargeId, WorkingCapitalLoanRequestBuilders.chargeAdjustment(BigDecimal.valueOf(100)));

            final GetBalance beforeReprocess = balanceOf(loanId);
            assertEqualBigDecimal(BigDecimal.valueOf(100), beforeReprocess.getFeePaid(),
                    "The adjustment settles the fee before reprocessing");
            assertEqualBigDecimal(BigDecimal.ZERO, beforeReprocess.getPrincipalPaid(), "No principal is paid yet");

            // A repayment dated day 1 - before the adjustment on day 5 - is backdated, and with a charge present it
            // routes through the full reset+replay that re-allocates every replayable transaction, the adjustment
            // included. On day 1 the fee is still in advance, so the 500 lands entirely on principal.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-15");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(500), "01 January 2026"));

            // The replay must reproduce the live split exactly: the adjustment still settles the fee it met as due,
            // and the repayment is the only principal.
            final GetBalance afterReprocess = balanceOf(loanId);
            assertEqualBigDecimal(BigDecimal.valueOf(100), afterReprocess.getFeePaid(),
                    "The charge adjustment must still settle the fee after the replay re-allocates it");
            assertEqualBigDecimal(BigDecimal.valueOf(500), afterReprocess.getPrincipalPaid(),
                    "Only the 500 repayment is principal - the adjustment must not inflate principal-paid");

            final WorkingCapitalLoanChargeData feeCharge = findCharge(wcLoanHelper.getCharges(loanId), feeLoanChargeId);
            assertEqualBigDecimal(BigDecimal.valueOf(100), feeCharge.getAmountPaid(), "The fee must remain fully paid after reprocessing");
            assertEqualBigDecimal(BigDecimal.ZERO, feeCharge.getAmountOutstanding(), "The fee must have no outstanding after reprocessing");
            assertTrue(feeCharge.getPaid(), "The fee must remain flagged paid after reprocessing");
        });
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
        final String uniqueName = "WCL ChargeAdjReproc " + Utils.uniqueRandomStringGenerator("", 8);
        final String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        final Long productId = productHelper
                .createWorkingCapitalLoanProduct(
                        new WorkingCapitalLoanProductTestBuilder().withName(uniqueName).withShortName(uniqueShortName).build())
                .getResourceId();
        createdProductIds.add(productId);
        return productId;
    }
}
