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

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdStatus;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.junit.jupiter.api.Test;

/**
 * Covers the loan recalculation command ({@code POST /loans/{loanId}?command=recalculate}), which regenerates the
 * repayment schedule of a progressive loan from its terms, replays its transactions and rebuilds its interest schedule
 * model.
 */
public class FeignLoanRecalculationTest extends FeignLoanTestBase {

    private static final String RECALCULATE_COMMAND = "recalculate";
    private static final String ACCRUAL = "loanTransactionType.accrual";
    private static final String CONTRACT_TERMINATION = "loanTransactionType.contractTermination";

    /**
     * The loan of PS-3547: a down payment loan with a rescheduled installment that is then re-aged. Without its stored
     * model every request to the loan failed, because the model was rebuilt from the re-aged installments. The
     * recalculation rebuilds it from the loan terms and leaves the loan as it was before the model was lost.
     */
    @Test
    void recalculationRebuildsMissingModelOfRescheduledAndReAgedLoan() {
        runAt("2026-06-01", () -> {
            Long clientId = createClient("01 June 2026");
            Long productId = createLoanProduct(progressiveNoInterestWithDownPayment());
            Long loanId = createApproveAndDisburseProgressiveLoan(clientId, productId, "01 June 2026", 100.0, 2);

            updateBusinessDate("30 June 2026");
            createAndApproveReschedule(loanId, "30 June 2026", "01 July 2026", "01 August 2026");

            updateBusinessDate("29 July 2026");
            reAge(loanId, reAge("29 August 2026", LoanTestData.RepaymentFrequencyType.MONTHS_STRING, 1, 19));

            GetLoansLoanIdResponse before = getLoanDetails(loanId);
            Map<String, Object> modelBefore = fetchModel(loanId);
            assertNotNull(modelBefore);

            deleteModel(loanId);
            assertTrue(fetchModel(loanId).isEmpty());

            PostLoansLoanIdResponse result = recalculateLoan(loanId);
            assertEquals(loanId, result.getLoanId());

            assertEquals(modelBefore, fetchModel(loanId), "The rebuilt model should match the one that was lost");
            GetLoansLoanIdResponse after = getLoanDetails(loanId);
            verifyLoanStatus(after, GetLoansLoanIdStatus::getActive);
            assertSameSchedule(before, after);
            assertEquals(Utils.getDoubleValue(before.getSummary().getTotalOutstanding()),
                    Utils.getDoubleValue(after.getSummary().getTotalOutstanding()));

            // the loan takes new transactions again
            makeGoodwillCredit(loanId, repayment(10.0, "29 July 2026"));
            assertEquals(Utils.getDoubleValue(after.getSummary().getTotalOutstanding()) - 10.0,
                    Utils.getDoubleValue(getLoanDetails(loanId).getSummary().getTotalOutstanding()), 0.001);
        });
    }

    /**
     * On a healthy loan the recalculation changes nothing.
     */
    @Test
    void recalculationOfHealthyLoanKeepsScheduleAndModel() {
        runAt("2026-06-01", () -> {
            Long clientId = createClient("01 June 2026");
            Long productId = createLoanProduct(progressiveNoInterestWithDownPayment());
            Long loanId = createApproveAndDisburseProgressiveLoan(clientId, productId, "01 June 2026", 100.0, 2);

            updateBusinessDate("15 June 2026");
            addRepaymentForLoan(loanId, 20.0, "15 June 2026");

            GetLoansLoanIdResponse before = getLoanDetails(loanId);
            Map<String, Object> modelBefore = fetchModel(loanId);

            recalculateLoan(loanId);

            assertEquals(modelBefore, fetchModel(loanId));
            assertSameSchedule(before, getLoanDetails(loanId));
        });
    }

    /**
     * On a healthy loan with interest recalculation, late payments and posted accruals the recalculation changes
     * nothing either: the schedule, the model and the transactions, accruals included, stay as they were.
     */
    @Test
    void recalculationOfHealthyInterestBearingLoanKeepsScheduleModelAndTransactions() {
        runAt("01 January 2026", () -> {
            Long clientId = createClient("01 January 2026");
            Long productId = createLoanProduct(progressiveWithInterestRecalculation());
            Long loanId = createApproveAndDisburseInterestBearingLoan(clientId, productId, "01 January 2026");

            updateBusinessDate("05 February 2026");
            executeInlineCOB(loanId);
            addRepaymentForLoan(loanId, 100.0, "05 February 2026");

            updateBusinessDate("15 March 2026");
            executeInlineCOB(loanId);
            addRepaymentForLoan(loanId, 150.0, "15 March 2026");

            GetLoansLoanIdResponse before = getLoanDetails(loanId);
            assertTrue(before.getTransactions().stream().anyMatch(t -> ACCRUAL.equals(t.getType().getCode())), "accruals were posted");
            Map<String, Object> modelBefore = fetchModel(loanId);

            recalculateLoan(loanId);

            assertEquals(modelBefore, fetchModel(loanId));
            GetLoansLoanIdResponse after = getLoanDetails(loanId);
            assertSameSchedule(before, after);
            assertSameTransactions(before, after);
            assertEquals(Utils.getDoubleValue(before.getSummary().getTotalOutstanding()),
                    Utils.getDoubleValue(after.getSummary().getTotalOutstanding()));
        });
    }

    /**
     * The interest recalculation path of the schedule regeneration: the deleted model of a re-aged loan with interest
     * recalculation is rebuilt to match the original, and the loan is left as it was.
     */
    @Test
    void recalculationRebuildsMissingModelOfReAgedInterestBearingLoan() {
        runAt("01 January 2026", () -> {
            Long clientId = createClient("01 January 2026");
            Long productId = createLoanProduct(progressiveWithInterestRecalculation());
            Long loanId = createApproveAndDisburseInterestBearingLoan(clientId, productId, "01 January 2026");

            updateBusinessDate("15 March 2026");
            reAge(loanId, reAge("01 April 2026", LoanTestData.RepaymentFrequencyType.MONTHS_STRING, 1, 6));

            assertRecalculationRebuildsDeletedModel(loanId, "15 March 2026");
        });
    }

    /**
     * Same as {@link #recalculationRebuildsMissingModelOfReAgedInterestBearingLoan()}, for a loan that is a non
     * performing asset when it is recalculated.
     */
    @Test
    void recalculationRebuildsMissingModelOfReAgedNonPerformingLoan() {
        runAt("01 January 2026", () -> {
            Long clientId = createClient("01 January 2026");
            Long productId = createLoanProduct(customizeProduct(progressiveWithInterestRecalculation(), p -> p.overdueDaysForNPA(1)));
            Long loanId = createApproveAndDisburseInterestBearingLoan(clientId, productId, "01 January 2026");

            updateBusinessDate("15 March 2026");
            schedulerHelper.executeAndAwaitJob("Update Loan Arrears Ageing");
            schedulerHelper.executeAndAwaitJob("Update Non Performing Assets");
            assertTrue(getLoanDetails(loanId).getIsNPA(), "the loan should be a non performing asset");

            reAge(loanId, reAge("01 April 2026", LoanTestData.RepaymentFrequencyType.MONTHS_STRING, 1, 6));
            assertTrue(getLoanDetails(loanId).getIsNPA(), "the loan should still be a non performing asset");

            assertRecalculationRebuildsDeletedModel(loanId, "15 March 2026");
        });
    }

    /**
     * A contract termination cuts the schedule at the termination date. The recalculation regenerates the full schedule
     * from the loan terms, so the replay of the termination has to cut it again in the same way.
     */
    @Test
    void recalculationRebuildsMissingModelOfContractTerminatedLoan() {
        runAt("01 January 2026", () -> {
            Long clientId = createClient("01 January 2026");
            Long productId = createLoanProduct(progressiveWithInterestRecalculation());
            Long loanId = createApproveAndDisburseInterestBearingLoan(clientId, productId, "01 January 2026");

            updateBusinessDate("05 February 2026");
            executeInlineCOB(loanId);
            addRepaymentForLoan(loanId, 100.0, "05 February 2026");

            updateBusinessDate("15 March 2026");
            executeInlineCOB(loanId);
            applyContractTermination(loanId, "15 March 2026");

            GetLoansLoanIdResponse terminated = getLoanDetails(loanId);
            verifyLoanStatus(terminated, GetLoansLoanIdStatus::getActive);
            assertTrue(terminated.getTransactions().stream().anyMatch(t -> CONTRACT_TERMINATION.equals(t.getType().getCode())),
                    "the contract termination was posted");
            List<GetLoansLoanIdRepaymentPeriod> periods = terminated.getRepaymentSchedule().getPeriods();
            assertEquals(LocalDate.of(2026, 3, 15), periods.get(periods.size() - 1).getDueDate(),
                    "the schedule should be cut at the termination date");

            assertRecalculationRebuildsDeletedModel(loanId, "15 March 2026");
        });
    }

    /**
     * A loan repaid early is closed. The recalculation replays the transactions and then determines the status again,
     * which keeps the loan closed with the same schedule, transactions and model.
     */
    @Test
    void recalculationOfLoanRepaidEarlyKeepsItClosed() {
        runAt("01 January 2026", () -> {
            Long clientId = createClient("01 January 2026");
            Long productId = createLoanProduct(progressiveWithInterestRecalculation());
            Long loanId = createApproveAndDisburseInterestBearingLoan(clientId, productId, "01 January 2026");

            updateBusinessDate("15 February 2026");
            executeInlineCOB(loanId);
            verifyPrepayAmountByRepayment(loanId, "15 February 2026");

            GetLoansLoanIdResponse before = getLoanDetails(loanId);
            Map<String, Object> modelBefore = fetchModel(loanId);
            assertNotNull(modelBefore);

            deleteModel(loanId);
            assertTrue(fetchModel(loanId).isEmpty());

            PostLoansLoanIdResponse result = recalculateLoan(loanId);
            assertTrue(result.getChanges() == null || result.getChanges().getStatus() == null, "the status should not change");

            assertEquals(modelBefore, fetchModel(loanId), "The rebuilt model should match the one that was lost");
            GetLoansLoanIdResponse after = getLoanDetails(loanId);
            verifyLoanStatus(after, LoanStatus.CLOSED_OBLIGATIONS_MET);
            assertSameSchedule(before, after);
            assertSameTransactions(before, after);
            assertEquals(0.0, Utils.getDoubleValue(after.getSummary().getTotalOutstanding()));
        });
    }

    @Test
    void recalculationIsRejectedForCumulativeLoan() {
        runAt("2026-06-01", () -> {
            Long clientId = createClient("01 June 2026");
            Long productId = createLoanProduct(fourInstallmentsCumulative());
            Long loanId = createApproveAndDisburseLoan(clientId, productId, "01 June 2026", 100.0, 4);

            CallFailedRuntimeException failure = fail(() -> fineractClient().loans().handleCommandsLoan(loanId,
                    new PostLoansLoanIdRequest(), Map.of("command", RECALCULATE_COMMAND)));
            assertEquals(403, failure.getStatus(), failure.getResponseBody());
            assertTrue(failure.getResponseBody().contains("error.msg.loan.recalculation.not.progressive"), failure.getResponseBody());
        });
    }

    @Test
    void recalculationIsRejectedForLoanNotYetDisbursed() {
        runAt("2026-06-01", () -> {
            Long clientId = createClient("01 June 2026");
            Long productId = createLoanProduct(progressiveNoInterestWithDownPayment());
            Long loanId = applyForLoan(LoanRequestBuilders.applyProgressiveLoan(clientId, productId, "01 June 2026", 100.0, 2));
            approveLoan(loanId, LoanRequestBuilders.approveLoan(100.0, "01 June 2026"));

            CallFailedRuntimeException failure = fail(() -> fineractClient().loans().handleCommandsLoan(loanId,
                    new PostLoansLoanIdRequest(), Map.of("command", RECALCULATE_COMMAND)));
            assertEquals(403, failure.getStatus(), failure.getResponseBody());
            assertTrue(failure.getResponseBody().contains("error.msg.loan.recalculation.invalid.status"), failure.getResponseBody());
        });
    }

    private PostLoansLoanIdResponse recalculateLoan(Long loanId) {
        return moveLoanState(loanId, new PostLoansLoanIdRequest(), RECALCULATE_COMMAND);
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

    /**
     * Deletes the model of the loan, recalculates the loan and checks that the model is rebuilt to match the deleted
     * one and that the schedule, the transactions and the outstanding balance did not change. Then checks that the loan
     * takes new transactions again.
     */
    private void assertRecalculationRebuildsDeletedModel(Long loanId, String businessDate) {
        GetLoansLoanIdResponse before = getLoanDetails(loanId);
        Map<String, Object> modelBefore = fetchModel(loanId);
        assertNotNull(modelBefore);

        deleteModel(loanId);
        assertTrue(fetchModel(loanId).isEmpty());

        recalculateLoan(loanId);

        assertEquals(modelBefore, fetchModel(loanId), "The rebuilt model should match the one that was lost");
        GetLoansLoanIdResponse after = getLoanDetails(loanId);
        assertSameSchedule(before, after);
        assertSameTransactions(before, after);
        double outstandingAfter = Utils.getDoubleValue(after.getSummary().getTotalOutstanding());
        assertEquals(Utils.getDoubleValue(before.getSummary().getTotalOutstanding()), outstandingAfter);

        // with interest recalculation the credit also lowers the interest still to come, so the balance drops by at
        // least
        // the credited amount
        makeGoodwillCredit(loanId, repayment(10.0, businessDate));
        double outstandingAfterCredit = Utils.getDoubleValue(getLoanDetails(loanId).getSummary().getTotalOutstanding());
        assertTrue(outstandingAfterCredit <= outstandingAfter - 10.0 + 0.001,
                "outstanding " + outstandingAfterCredit + " after a credit of 10.00 on " + outstandingAfter);
    }

    private static void assertSameTransactions(GetLoansLoanIdResponse expected, GetLoansLoanIdResponse actual) {
        assertEquals(transactionRows(expected), transactionRows(actual));
    }

    private static List<String> transactionRows(GetLoansLoanIdResponse loan) {
        return loan.getTransactions().stream()
                .map(t -> String.join(" ", String.valueOf(t.getId()), t.getType().getCode(), String.valueOf(t.getDate()),
                        amount(t.getAmount()), amount(t.getPrincipalPortion()), amount(t.getInterestPortion()),
                        amount(t.getFeeChargesPortion()), amount(t.getPenaltyChargesPortion()), amount(t.getOverpaymentPortion()),
                        amount(t.getOutstandingLoanBalance()), String.valueOf(t.getManuallyReversed())))
                .toList();
    }

    private static String amount(BigDecimal value) {
        return value == null ? "null" : value.stripTrailingZeros().toPlainString();
    }

    private Long createApproveAndDisburseInterestBearingLoan(Long clientId, Long productId, String date) {
        Long loanId = applyForLoan(LoanRequestBuilders.applyProgressiveLoan(clientId, productId, date, 1000.0, 4, 12.0));
        approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, date));
        disburseLoan(loanId, LoanRequestBuilders.disburseLoan(1000.0, date));
        return loanId;
    }

    private PostLoanProductsRequest progressiveWithInterestRecalculation() {
        return customizeProduct(create4IProgressive(), p -> p//
                .numberOfRepayments(4)//
                .interestRatePerPeriod(12.0)//
                .multiDisburseLoan(false)//
                .disallowExpectedDisbursements(false)//
                .maxTrancheCount(null)//
                .outstandingLoanBalance(null));
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
