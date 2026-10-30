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
package org.apache.fineract.integrationtests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdStatus;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessStepHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Interest received on a non-performing loan is recognised as income on receipt: after a repayment or the closing
 * repayment of an NPA loan, Interest Receivable nets to zero and Interest Income equals the interest paid.
 */
public class LoanNpaInterestIncomeOnReceiptTest extends FeignLoanTestBase {

    private static final String DISBURSEMENT_DATE = "01 January 2024";
    private static final String NPA_REPAYMENT_DATE = "10 January 2024";
    private static final LocalDate JOURNAL_CUTOFF = LocalDate.of(2099, 12, 31);
    private static final double DELTA = 0.001;

    @BeforeAll
    public static void setupCobSteps() {
        new FeignBusinessStepHelper(FineractFeignClientHelper.getFineractFeignClient()).updateSteps("LOAN_CLOSE_OF_BUSINESS",
                "APPLY_CHARGE_TO_OVERDUE_LOANS", "LOAN_DELINQUENCY_CLASSIFICATION", "CHECK_LOAN_REPAYMENT_DUE",
                "CHECK_LOAN_REPAYMENT_OVERDUE", "UPDATE_LOAN_ARREARS_AGING", "ADD_PERIODIC_ACCRUAL_ENTRIES",
                "EXTERNAL_ASSET_OWNER_TRANSFER", "ACCRUAL_ACTIVITY_POSTING");
    }

    @Test
    public void repaymentAndClosureOfAnNpaLoanRecogniseTheInterestPaidAsIncome() {
        final Long loanId = createOverdueLoanThatBecomesNpa();

        runAt(NPA_REPAYMENT_DATE, () -> {
            repayEverythingDueBy(loanId, LocalDate.of(2024, 1, 10));

            final GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            assertTrue(Boolean.TRUE.equals(loan.getIsNPA()), "The loan is still NPA after the partial repayment");
            assertBalancesMatchInterestPaid(loanId, "after the NPA repayment");
        });

        runAt("12 January 2024", () -> {
            final double totalOutstanding = Utils.getDoubleValue(getLoanDetails(loanId).getSummary().getTotalOutstanding());
            addRepayment(loanId, repayment(totalOutstanding, "12 January 2024"));

            verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getClosedObligationsMet);
            assertBalancesMatchInterestPaid(loanId, "after the closing repayment");
        });
    }

    @Test
    public void undoingTheNpaRepaymentReversesTheIncomeRecognisedOnReceipt() {
        final Long loanId = createOverdueLoanThatBecomesNpa();
        final double incomeBeforeRepayment = interestIncome(loanId);
        final double receivableBeforeRepayment = interestReceivable(loanId);

        final AtomicReference<Long> repaymentId = new AtomicReference<>();
        runAt(NPA_REPAYMENT_DATE, () -> repaymentId.set(repayEverythingDueBy(loanId, LocalDate.of(2024, 1, 10))));

        runAt("11 January 2024", () -> {
            undoRepayment(loanId, repaymentId.get(), "11 January 2024");

            assertEquals(incomeBeforeRepayment, interestIncome(loanId), DELTA, "Interest Income after the repayment is undone");
            assertEquals(receivableBeforeRepayment, interestReceivable(loanId), DELTA, "Interest Receivable after the repayment is undone");
        });
    }

    /**
     * Disburses 1000 over 12 daily installments on a cumulative, periodic-accrual product that turns NPA after 5
     * overdue days, then runs the close of business and the NPA job daily without any repayment until the loan is NPA.
     */
    private Long createOverdueLoanThatBecomesNpa() {
        final Long clientId = createClient();
        final Long productId = createLoanProduct(fourInstallmentsCumulative()//
                .numberOfRepayments(12)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.DAYS_L)//
                .interestRatePerPeriod(120.0)//
                .overdueDaysForNPA(5));

        final AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt(DISBURSEMENT_DATE, () -> {
            final PostLoansRequest application = LoanRequestBuilders
                    .applyCumulativeLoan(clientId, productId, DISBURSEMENT_DATE, 1000.0, 12, 120.0)//
                    .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.DAYS)//
                    .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.DAYS);
            final Long id = applyForLoan(application);
            approveLoan(id, approveLoanRequest(1000.0, DISBURSEMENT_DATE));
            disburseLoan(id, LoanRequestBuilders.disburseLoan(1000.0, DISBURSEMENT_DATE));
            loanIdRef.set(id);
        });
        final Long loanId = loanIdRef.get();

        for (LocalDate date = LocalDate.of(2024, 1, 2); !date.isAfter(LocalDate.of(2024, 1, 10)); date = date.plusDays(1)) {
            runAt(date.format(dateTimeFormatter), () -> {
                executeInlineCOB(loanId);
                schedulerHelper.executeAndAwaitJob("Update Non Performing Assets");
            });
        }

        assertTrue(Boolean.TRUE.equals(getLoanDetails(loanId).getIsNPA()), "The loan is NPA after 5 overdue days");
        assertTrue(interestReceivable(loanId) > 0, "Interest was accrued before the loan turned NPA");
        return loanId;
    }

    private Long repayEverythingDueBy(Long loanId, LocalDate dueBy) {
        final double amount = getLoanDetails(loanId).getRepaymentSchedule().getPeriods().stream()
                .filter(period -> period.getPeriod() != null && !period.getDueDate().isAfter(dueBy))
                .mapToDouble(period -> Utils.getDoubleValue(period.getTotalOutstandingForPeriod())).sum();
        return addRepayment(loanId, repayment(amount, dueBy.format(dateTimeFormatter)));
    }

    private void assertBalancesMatchInterestPaid(Long loanId, String when) {
        final double interestPaid = Utils.getDoubleValue(getLoanDetails(loanId).getSummary().getInterestPaid());
        assertTrue(interestPaid > 0, "Interest was paid " + when);
        assertEquals(0.0, interestReceivable(loanId), DELTA, "Interest Receivable " + when);
        assertEquals(interestPaid, interestIncome(loanId), DELTA, "Interest Income " + when);
    }

    private double interestReceivable(Long loanId) {
        return interimJournalBalance(loanId, getAssetAccountId("interestReceivable"), JOURNAL_CUTOFF, true);
    }

    private double interestIncome(Long loanId) {
        return interimJournalBalance(loanId, getIncomeAccountId("interestIncome"), JOURNAL_CUTOFF, false);
    }
}
