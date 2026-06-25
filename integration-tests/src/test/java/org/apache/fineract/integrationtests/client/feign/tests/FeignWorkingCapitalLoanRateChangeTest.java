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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.WorkingCapitalLoanPeriodPaymentRateChangeData;
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

public class FeignWorkingCapitalLoanRateChangeTest extends FeignIntegrationTest {

    private FeignWorkingCapitalLoanHelper wcLoanHelper;
    private FeignClientHelper clientHelper;
    private WorkingCapitalLoanProductHelper productHelper;
    private FeignBusinessDateHelper businessDateHelper;

    private Long clientId;

    private final List<Long> createdLoanIds = new ArrayList<>();
    private final List<Long> createdProductIds = new ArrayList<>();

    @BeforeAll
    void setupHelpers() {
        wcLoanHelper = new FeignWorkingCapitalLoanHelper(fineractClient());
        clientHelper = new FeignClientHelper(fineractClient());
        productHelper = new WorkingCapitalLoanProductHelper();
        businessDateHelper = new FeignBusinessDateHelper(fineractClient());
        clientId = clientHelper.createClient();
    }

    @AfterAll
    void cleanupEntities() {
        createdLoanIds.forEach(wcLoanHelper::cleanupLoan);
        createdLoanIds.clear();
        createdProductIds.clear();
    }

    @Test
    void testUpdateRateOnActiveLoan() {
        Long loanId = createAndDisburseLoan(BigDecimal.valueOf(5000), BigDecimal.valueOf(18));

        wcLoanHelper.updateRate(loanId, WorkingCapitalLoanRequestBuilders.updateRate(BigDecimal.valueOf(17)));

        GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
        assertNotNull(loan);
        assertEquals(0, BigDecimal.valueOf(17).compareTo(loan.getPaymentRate()));
    }

    @Test
    void testRateChangeHistoryIsRecorded() {
        Long loanId = createAndDisburseLoan(BigDecimal.valueOf(5000), BigDecimal.valueOf(18));

        wcLoanHelper.updateRate(loanId, WorkingCapitalLoanRequestBuilders.updateRate(BigDecimal.valueOf(17)));

        List<WorkingCapitalLoanPeriodPaymentRateChangeData> history = wcLoanHelper.getRateChangeHistory(loanId);
        assertFalse(history.isEmpty(), "Rate change history should not be empty");

        WorkingCapitalLoanPeriodPaymentRateChangeData change = history.getFirst();
        assertEquals(0, BigDecimal.valueOf(18).compareTo(change.getPreviousRate()));
        assertEquals(0, BigDecimal.valueOf(17).compareTo(change.getNewRate()));
        assertFalse(change.getReversed());
    }

    @Test
    void testRateChangeNotAllowedOnNonActiveLoan() {
        Long productId = createProduct();
        String today = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());
        Long loanId = submitAndTrack(clientId, productId, BigDecimal.valueOf(5000), BigDecimal.valueOf(18), today);

        CallFailedRuntimeException exception = wcLoanHelper.updateRateExpectingError(loanId,
                WorkingCapitalLoanRequestBuilders.updateRate(BigDecimal.valueOf(17)));
        assertTrue(exception.getStatus() >= 400,
                "Rate change on non-active loan should fail with 4xx status, got: " + exception.getStatus());
    }

    @Test
    void testMultipleRateChangesAutoReversesPrevious() {
        Long loanId = createAndDisburseLoan(BigDecimal.valueOf(5000), BigDecimal.valueOf(18));

        wcLoanHelper.updateRate(loanId, WorkingCapitalLoanRequestBuilders.updateRate(BigDecimal.valueOf(17)));
        wcLoanHelper.updateRate(loanId, WorkingCapitalLoanRequestBuilders.updateRate(BigDecimal.valueOf(15)));

        List<WorkingCapitalLoanPeriodPaymentRateChangeData> history = wcLoanHelper.getRateChangeHistory(loanId);
        assertEquals(2, history.size(), "Should have 2 rate change records");

        // Most recent (15%) should be active, previous (17%) should be auto-reversed
        WorkingCapitalLoanPeriodPaymentRateChangeData latestChange = history.get(0);
        WorkingCapitalLoanPeriodPaymentRateChangeData firstChange = history.get(1);
        assertFalse(latestChange.getReversed(), "Latest rate change should be active");
        assertTrue(firstChange.getReversed(), "Previous rate change should be auto-reversed");

        GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
        assertEquals(0, BigDecimal.valueOf(15).compareTo(loan.getPaymentRate()));
    }

    @Test
    void testMultipleRateChangesOnDifferentBusinessDates() {
        businessDateHelper.runAt("2026-01-01", () -> {
            Long clientForTest = clientHelper.createClient("01 January 2026");
            Long loanId = createAndDisburseLoanOnDate(clientForTest, BigDecimal.valueOf(50000), BigDecimal.valueOf(18), "01 January 2026");

            // First rate change: 18 → 15 on Jan 1
            wcLoanHelper.updateRate(loanId, WorkingCapitalLoanRequestBuilders.updateRate(BigDecimal.valueOf(15)));

            // Advance business date by 8 days
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-09");

            // Second rate change: 15 → 11 on Jan 9
            wcLoanHelper.updateRate(loanId, WorkingCapitalLoanRequestBuilders.updateRate(BigDecimal.valueOf(11)));

            GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            assertEquals(0, BigDecimal.valueOf(11).compareTo(loan.getPaymentRate()),
                    "Rate should be updated to 11 after second rate change");

            List<WorkingCapitalLoanPeriodPaymentRateChangeData> history = wcLoanHelper.getRateChangeHistory(loanId);
            assertEquals(2, history.size(), "Should have 2 rate change records");

            // Latest (11%) should be active, first (15%) should be auto-reversed
            assertFalse(history.get(0).getReversed(), "Latest rate change should be active");
            assertTrue(history.get(1).getReversed(), "Previous rate change should be auto-reversed");
        });
    }

    @Test
    void testRateChangePastEndOfTermSucceeds() {
        // Use a small principal with high rate to create a very short-term loan,
        // then advance past term. The rate change should succeed — the segment starts
        // at the base term end with the remaining principal as balance.
        businessDateHelper.runAt("2026-01-01", () -> {
            Long clientForTest = clientHelper.createClient("01 January 2026");
            Long loanId = createAndDisburseLoanOnDate(clientForTest, BigDecimal.valueOf(100), BigDecimal.valueOf(18), "01 January 2026");

            // Advance past the loan term — rate change at day 5 is past the schedule end
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-06");

            wcLoanHelper.updateRate(loanId, WorkingCapitalLoanRequestBuilders.updateRate(BigDecimal.valueOf(15)));

            GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            assertEquals(0, BigDecimal.valueOf(15).compareTo(loan.getPaymentRate()),
                    "Rate should be updated to 15 after past-term rate change");
        });
    }

    private Long createAndDisburseLoanOnDate(Long clientIdParam, BigDecimal principal, BigDecimal rate, String date) {
        Long productId = createProduct();
        Long loanId = submitAndTrack(clientIdParam, productId, principal, rate, date);
        wcLoanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approve(date, principal, date));
        wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburse(date, principal));
        return loanId;
    }

    private Long createAndDisburseLoan(BigDecimal principal, BigDecimal rate) {
        Long productId = createProduct();
        String today = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());
        Long loanId = submitAndTrack(clientId, productId, principal, rate, today);

        wcLoanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approve(today, principal, today));
        wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburse(today, principal));

        return loanId;
    }

    private Long submitAndTrack(Long clientIdParam, Long productId, BigDecimal principal, BigDecimal rate, String date) {
        Long loanId = wcLoanHelper.submitApplication(
                WorkingCapitalLoanRequestBuilders.submitApplication(clientIdParam, productId, principal, rate, date, date));
        createdLoanIds.add(loanId);
        return loanId;
    }

    private Long createProduct() {
        String uniqueName = "WCL Rate " + Utils.uniqueRandomStringGenerator("", 8);
        String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        Long productId = productHelper
                .createWorkingCapitalLoanProduct(
                        new WorkingCapitalLoanProductTestBuilder().withName(uniqueName).withShortName(uniqueShortName).build())
                .getResourceId();
        createdProductIds.add(productId);
        return productId;
    }
}
