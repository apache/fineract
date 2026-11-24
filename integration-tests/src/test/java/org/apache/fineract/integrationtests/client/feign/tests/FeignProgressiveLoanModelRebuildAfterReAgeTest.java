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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.NoteCreateRequest;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Test;

/**
 * Covers the rebuild of a lost or outdated interest schedule model, which runs before loan write requests and in close
 * of business, for a loan whose installments were re-aged (FINERACT-3327). The re-age had marked every regular
 * installment as re-aged, and the rebuild, which replayed the transactions over the stored installments without the
 * re-aged ones, was left without a regular installment and failed, so every write request to the loan failed with it.
 */
public class FeignProgressiveLoanModelRebuildAfterReAgeTest extends FeignLoanTestBase {

    @Test
    void modelOfReAgedLoanIsRebuiltBeforeWriteRequest() {
        runAt("2026-06-01", () -> {
            Long loanId = createRescheduledAndReAgedLoan();
            GetLoansLoanIdResponse before = getLoanDetails(loanId);
            Map<String, Object> modelBefore = fetchModel(loanId);
            assertFalse(modelBefore.isEmpty());

            deleteModel(loanId);
            assertTrue(fetchModel(loanId).isEmpty());

            // a write request that changes nothing on the loan itself, so the rebuilt model can be compared
            ok(() -> fineractClient().notes().addNewNote("loans", loanId, new NoteCreateRequest().note("model rebuild")));

            assertEquals(modelBefore, fetchModel(loanId), "The rebuilt model should match the one that was lost");
            GetLoansLoanIdResponse after = getLoanDetails(loanId);
            assertSameSchedule(before, after);

            makeGoodwillCredit(loanId, repayment(10.0, "29 July 2026"));
            assertEquals(Utils.getDoubleValue(after.getSummary().getTotalOutstanding()) - 10.0,
                    Utils.getDoubleValue(getLoanDetails(loanId).getSummary().getTotalOutstanding()), 0.001);
        });
    }

    @Test
    void modelOfReAgedLoanIsRebuiltInCloseOfBusiness() {
        runAt("2026-06-01", () -> {
            Long loanId = createRescheduledAndReAgedLoan();
            GetLoansLoanIdResponse before = getLoanDetails(loanId);

            deleteModel(loanId);

            updateBusinessDate("30 July 2026");
            executeInlineCOB(loanId);

            assertFalse(fetchModel(loanId).isEmpty());
            assertSameSchedule(before, getLoanDetails(loanId));
        });
    }

    private Long createRescheduledAndReAgedLoan() {
        Long clientId = createClient("01 June 2026");
        Long productId = createLoanProduct(progressiveNoInterestWithDownPayment());
        Long loanId = createApproveAndDisburseProgressiveLoan(clientId, productId, "01 June 2026", 100.0, 2);

        updateBusinessDate("30 June 2026");
        createAndApproveReschedule(loanId, "30 June 2026", "01 July 2026", "01 August 2026");

        updateBusinessDate("29 July 2026");
        reAge(loanId, reAge("29 August 2026", LoanTestData.RepaymentFrequencyType.MONTHS_STRING, 1, 19));
        return loanId;
    }

    private Map<String, Object> fetchModel(Long loanId) {
        return ok(() -> fineractClient().progressiveLoan().retrieveOneInternalProgressiveLoan(loanId));
    }

    private void deleteModel(Long loanId) {
        ok(() -> fineractClient().progressiveLoan().deleteInternalProgressiveLoan(loanId));
    }

    private static void assertSameSchedule(GetLoansLoanIdResponse expected, GetLoansLoanIdResponse actual) {
        List<GetLoansLoanIdRepaymentPeriod> expectedPeriods = expected.getRepaymentSchedule().getPeriods();
        List<GetLoansLoanIdRepaymentPeriod> actualPeriods = actual.getRepaymentSchedule().getPeriods();
        assertEquals(expectedPeriods.size(), actualPeriods.size(), "number of periods");
        for (int i = 0; i < expectedPeriods.size(); i++) {
            GetLoansLoanIdRepaymentPeriod e = expectedPeriods.get(i);
            GetLoansLoanIdRepaymentPeriod a = actualPeriods.get(i);
            String period = "period " + i;
            assertEquals(e.getPeriod(), a.getPeriod(), period);
            assertEquals(e.getFromDate(), a.getFromDate(), period);
            assertEquals(e.getDueDate(), a.getDueDate(), period);
            assertEquals(Utils.getDoubleValue(e.getPrincipalDue()), Utils.getDoubleValue(a.getPrincipalDue()), period);
            assertEquals(Utils.getDoubleValue(e.getInterestDue()), Utils.getDoubleValue(a.getInterestDue()), period);
            assertEquals(Utils.getDoubleValue(e.getTotalPaidForPeriod()), Utils.getDoubleValue(a.getTotalPaidForPeriod()), period);
            assertEquals(Utils.getDoubleValue(e.getTotalOutstandingForPeriod()), Utils.getDoubleValue(a.getTotalOutstandingForPeriod()),
                    period);
        }
    }

    private PostLoanProductsRequest progressiveNoInterestWithDownPayment() {
        return customizeProduct(fourInstallmentsProgressiveWithAdvancedAllocation(), p -> p//
                .numberOfRepayments(2)//
                .interestRatePerPeriod(0.0)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS_L)//
                .enableDownPayment(true)//
                .disbursedAmountPercentageForDownPayment(BigDecimal.valueOf(33.333333))//
                .enableAutoRepaymentForDownPayment(true));
    }
}
