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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.apache.fineract.client.models.DelinquencyRangeRequest;
import org.apache.fineract.client.models.GetBalance;
import org.apache.fineract.client.models.GetJournalEntriesTransactionIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.PostDelinquencyBucketResponse;
import org.apache.fineract.client.models.PostDelinquencyRangeResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsRequest.AccountingRuleEnum;
import org.apache.fineract.client.models.ProjectedAmortizationScheduleData;
import org.apache.fineract.client.models.ProjectedAmortizationSchedulePaymentData;
import org.apache.fineract.client.models.WorkingCapitalLoanBreachScheduleData;
import org.apache.fineract.client.models.WorkingCapitalLoanChargeData;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessDateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignJournalEntryHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalLoanRequestBuilders;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.products.DelinquencyRangesHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanDelinquencyRangeScheduleHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanbreach.WorkingCapitalBreachHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for Working Capital loan repayment: backdated repayment and undo.
 *
 * <p>
 * Covers, on a Working Capital loan: backdated repayment and undo (repayment) updating the loan balance and status
 * (closed / overpaid / re-opened); discount fee amortization and its adjustment reacting to repayment / undo at COB;
 * delinquency and breach schedules being re-evaluated; charge allocation being recomputed across transactions; and the
 * reversal GL entries produced on undo.
 *
 * <p>
 * Out of scope on this branch: repayment / undo on a charge-off loan, and rejection on a written-off loan — no WC
 * charge-off / write-off command exists to exercise these.
 */
public class FeignWorkingCapitalLoanRepaymentBackdatedUndoTest extends FeignIntegrationTest {

    private static final String DISCOUNT_FEE_AMORTIZATION_CODE = "loanTransactionType.discountFeeAmortization";
    private static final String DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT_CODE = "loanTransactionType.discountFeeAmortizationAdjustment";

    // Delinquency configuration: a 20-day period with a 3% minimum payment and a 3-day grace.
    private static final int DELINQUENCY_FREQUENCY_DAYS = 20;
    private static final int DELINQUENCY_GRACE_DAYS = 3;
    private static final BigDecimal DELINQUENCY_MIN_PAYMENT_PERCENT = new BigDecimal("3");

    // Breach configuration: a one-month period with a flat 500 minimum payment (mirrors the WC breach e2e product).
    private static final int BREACH_FREQUENCY = 1;
    private static final String BREACH_FREQUENCY_TYPE = "MONTHS";
    private static final String BREACH_AMOUNT_CALCULATION_TYPE = "FLAT";
    private static final BigDecimal BREACH_MIN_PAYMENT_AMOUNT = new BigDecimal("500");

    private static final BigDecimal NET_DISBURSEMENT = new BigDecimal("9000");
    private static final BigDecimal DISCOUNT = new BigDecimal("1000");
    private static final BigDecimal GROSS_OWED = new BigDecimal("10000");
    private static final BigDecimal TOTAL_PAYMENT_VOLUME = new BigDecimal("100000");
    private static final BigDecimal PERIOD_PAYMENT_RATE = new BigDecimal("18");
    private static final int NPV_DAY_COUNT = 360;
    private static final BigDecimal DAILY_PAYMENT = new BigDecimal("50");
    private static final int TERM_WITH_DISCOUNT = 200;
    private static final int TERM_WITHOUT_DISCOUNT = 180;
    private static final double SCHEDULE_CHARGE_AMOUNT = 100.0;
    private static final String SCHEDULE_DISBURSEMENT_DATE = "01 January 2026";

    private FeignWorkingCapitalLoanHelper wcLoanHelper;
    private FeignClientHelper clientHelper;
    private FeignBusinessDateHelper businessDateHelper;
    private FeignJournalEntryHelper journalHelper;
    private WorkingCapitalLoanProductHelper productHelper;
    private WorkingCapitalBreachHelper breachHelper;

    // GL accounts for cash-accounting products (tests 5, 6, 10, 11).
    private Account fundSourceAccount;
    private Account loanPortfolioAccount;
    private Account transfersSuspenseAccount;
    private Account incomeFromDiscountFeeAccount;
    private Account feesReceivableAccount;
    private Account penaltiesReceivableAccount;
    private Account incomeFromFeeAccount;
    private Account incomeFromPenaltyAccount;
    private Account incomeFromRecoveryAccount;
    private Account writeOffAccount;
    private Account overpaymentAccount;
    private Account deferredIncomeAccount;

    private final List<Long> createdLoanIds = new ArrayList<>();
    private final List<Long> createdProductIds = new ArrayList<>();

    @BeforeAll
    void setupHelpers() {
        final var feignClient = fineractClient();
        wcLoanHelper = new FeignWorkingCapitalLoanHelper(feignClient);
        clientHelper = new FeignClientHelper(feignClient);
        businessDateHelper = new FeignBusinessDateHelper(feignClient);
        journalHelper = new FeignJournalEntryHelper(feignClient);
        productHelper = new WorkingCapitalLoanProductHelper();
        breachHelper = new WorkingCapitalBreachHelper();

        final FeignAccountHelper accountHelper = new FeignAccountHelper(feignClient);
        fundSourceAccount = accountHelper.createLiabilityAccount("wcUndoFundSrc");
        loanPortfolioAccount = accountHelper.createAssetAccount("wcUndoLoanPort");
        transfersSuspenseAccount = accountHelper.createAssetAccount("wcUndoXferSusp");
        incomeFromDiscountFeeAccount = accountHelper.createIncomeAccount("wcUndoIncDisc");
        feesReceivableAccount = accountHelper.createAssetAccount("wcUndoFeesRcv");
        penaltiesReceivableAccount = accountHelper.createAssetAccount("wcUndoPenRcv");
        incomeFromFeeAccount = accountHelper.createIncomeAccount("wcUndoIncFee");
        incomeFromPenaltyAccount = accountHelper.createIncomeAccount("wcUndoIncPen");
        incomeFromRecoveryAccount = accountHelper.createIncomeAccount("wcUndoIncRec");
        writeOffAccount = accountHelper.createExpenseAccount("wcUndoWrtOff");
        overpaymentAccount = accountHelper.createLiabilityAccount("wcUndoOverpay");
        deferredIncomeAccount = accountHelper.createLiabilityAccount("wcUndoDefInc");
    }

    @AfterAll
    void cleanupEntities() {
        createdLoanIds.forEach(wcLoanHelper::cleanupLoan);
        createdLoanIds.clear();
        createdProductIds.clear();
    }

    @Test
    @DisplayName("a backdated repayment that brings the total repaid to the full principal closes the loan with zero outstanding")
    void backdatedRepayment_closesLoan_whenBalanceReachesZero() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long client = clientHelper.createClient("01 January 2026");
            final Long loanId = createAndDisburseLoanOnDate(client, BigDecimal.valueOf(9000), "01 January 2026");

            // Partial repayment on day 10 leaves 5000 outstanding.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-10");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(4000), "10 January 2026"));

            // Backdated repayment on day 5 brings the total repaid to exactly the 9000 principal.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-15");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(5000), "05 January 2026"));

            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            assertNotNull(loan.getBalance(), "Balance should exist");
            assertEqualBigDecimal(BigDecimal.ZERO, loan.getBalance().getPrincipalOutstanding(),
                    "Outstanding should be 0 after the backdated repayment brings the total to the 9000 principal");
            assertEqualBigDecimal(BigDecimal.valueOf(9000), loan.getBalance().getPrincipalPaid(), "Principal paid should be the full 9000");
            assertEqualBigDecimal(BigDecimal.ZERO, loan.getBalance().getOverpaymentAmount(), "There should be no overpayment");
            assertNotNull(loan.getStatus(), "Status should exist");
            assertEquals(Boolean.TRUE, loan.getStatus().getClosedObligationsMet(),
                    "Loan must be closed (obligations met) once the balance reaches exactly zero");
        });
    }

    @Test
    @DisplayName("a backdated repayment that exceeds the principal marks the loan overpaid with the exact excess as overpayment")
    void backdatedRepayment_overpaysLoan_whenBalanceNegative() {
        businessDateHelper.runAt("2026-02-01", () -> {
            final Long client = clientHelper.createClient("01 February 2026");
            final Long loanId = createAndDisburseLoanOnDate(client, BigDecimal.valueOf(9000), "01 February 2026");

            // Repayment on day 10 leaves 2000 outstanding.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-02-10");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(7000), "10 February 2026"));

            // Backdated repayment on day 5 pushes the total repaid to 12000 against a 9000 principal.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-02-15");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(5000), "05 February 2026"));

            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            assertNotNull(loan.getBalance(), "Balance should exist");
            assertEqualBigDecimal(BigDecimal.ZERO, loan.getBalance().getPrincipalOutstanding(), "Outstanding should be 0 once overpaid");
            assertEqualBigDecimal(BigDecimal.valueOf(9000), loan.getBalance().getPrincipalPaid(),
                    "Principal paid should be capped at the 9000 principal");
            assertEqualBigDecimal(BigDecimal.valueOf(3000), loan.getBalance().getOverpaymentAmount(),
                    "Overpayment should be the exact excess of 12000 - 9000 = 3000");
            assertNotNull(loan.getStatus(), "Status should exist");
            assertEquals(Boolean.TRUE, loan.getStatus().getOverpaid(), "Loan must be overpaid when the repaid total exceeds the principal");
        });
    }

    @Test
    @DisplayName("undoing a partial repayment on an active loan flags the transaction reversed and restores the outstanding balance")
    void undoRepayment_onActiveLoan_restoresBalance() {
        businessDateHelper.runAt("2026-03-01", () -> {
            final Long client = clientHelper.createClient("01 March 2026");
            final Long loanId = createAndDisburseLoanOnDate(client, BigDecimal.valueOf(9000), "01 March 2026");

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-03-05");
            final Long repaymentTxnId = wcLoanHelper.makeRepayment(loanId,
                    WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(3000), "05 March 2026"));

            final GetWorkingCapitalLoansLoanIdResponse afterRepayment = wcLoanHelper.getLoanDetails(loanId);
            assertEqualBigDecimal(BigDecimal.valueOf(6000), afterRepayment.getBalance().getPrincipalOutstanding(),
                    "Outstanding should be 6000 after the 3000 repayment");

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-03-10");
            wcLoanHelper.undoTransaction(loanId, repaymentTxnId, WorkingCapitalLoanRequestBuilders.undoTransaction());

            final GetWorkingCapitalLoanTransactionIdResponse reversed = findTransactionById(wcLoanHelper.getTransactions(loanId),
                    repaymentTxnId);
            assertEquals(Boolean.TRUE, reversed.getReversed(), "The undone repayment transaction must be flagged reversed");

            final GetWorkingCapitalLoansLoanIdResponse afterUndo = wcLoanHelper.getLoanDetails(loanId);
            assertNotNull(afterUndo.getBalance(), "Balance should exist after undo");
            assertEqualBigDecimal(BigDecimal.valueOf(9000), afterUndo.getBalance().getPrincipalOutstanding(),
                    "Outstanding should be restored to the full 9000 after undo");
            assertEqualBigDecimal(BigDecimal.ZERO, afterUndo.getBalance().getPrincipalPaid(), "Principal paid should be restored to 0");
            assertEqualBigDecimal(BigDecimal.ZERO, afterUndo.getBalance().getOverpaymentAmount(), "No overpayment after undo");
            assertEquals(Boolean.TRUE, afterUndo.getStatus().getActive(), "Loan must remain active after undoing a partial repayment");
        });
    }

    @Test
    @DisplayName("undoing the closing repayment re-opens a closed loan back to active with the full outstanding restored")
    void undoRepayment_reopensClosedLoan() {
        businessDateHelper.runAt("2026-04-01", () -> {
            final Long client = clientHelper.createClient("01 April 2026");
            final Long loanId = createAndDisburseLoanOnDate(client, BigDecimal.valueOf(9000), "01 April 2026");

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-04-05");
            final Long closingTxnId = wcLoanHelper.makeRepayment(loanId,
                    WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(9000), "05 April 2026"));

            final GetWorkingCapitalLoansLoanIdResponse closed = wcLoanHelper.getLoanDetails(loanId);
            assertEquals(Boolean.TRUE, closed.getStatus().getClosedObligationsMet(), "Loan should be closed after the full repayment");

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-04-10");
            wcLoanHelper.undoTransaction(loanId, closingTxnId, WorkingCapitalLoanRequestBuilders.undoTransaction());

            final GetWorkingCapitalLoansLoanIdResponse reopened = wcLoanHelper.getLoanDetails(loanId);
            assertEquals(Boolean.TRUE, reopened.getStatus().getActive(), "Loan must be active again after undoing the closing repayment");
            assertEqualBigDecimal(BigDecimal.valueOf(9000), reopened.getBalance().getPrincipalOutstanding(),
                    "Outstanding should return to the full 9000 after undo");
            assertEqualBigDecimal(BigDecimal.ZERO, reopened.getBalance().getPrincipalPaid(), "Principal paid should be restored to 0");
        });
    }

    @Test
    @DisplayName("a backdated repayment followed by COB posts exactly one discount fee amortization transaction capped by the discount")
    void backdatedRepayment_thenCOB_postsDiscountFeeAmortization() {
        businessDateHelper.runAt("2026-05-01", () -> {
            final Long client = clientHelper.createClient("01 May 2026");
            final BigDecimal principal = BigDecimal.valueOf(9000);
            final BigDecimal discount = BigDecimal.valueOf(1000);
            final Long productId = createAccrualProductWithDiscount();
            final Long loanId = submitApproveDisburseWithDiscount(client, productId, principal, discount, "01 May 2026");

            // Backdated repayment (booked on day 5 while the business date has advanced to day 10).
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-05-10");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(3000), "05 May 2026"));

            wcLoanHelper.executeInlineWCCOB(loanId);

            final List<GetWorkingCapitalLoanTransactionIdResponse> amortizations = filterByType(wcLoanHelper.getTransactions(loanId),
                    DISCOUNT_FEE_AMORTIZATION_CODE);
            assertEquals(1, amortizations.size(), "Exactly one discount fee amortization transaction must be posted after COB");
            final BigDecimal amortAmount = amortizations.get(0).getTransactionAmount();
            // The exact amortized figure is the output of the NPV/cursor amortization model (not analytically fixed for
            // a partial payment), so the strongest deterministic assertions are the single-transaction count and that
            // the amount is positive and capped by the discount.
            assertTrue(amortAmount.compareTo(BigDecimal.ZERO) > 0, "Amortization amount must be positive, was: " + amortAmount);
            assertTrue(amortAmount.compareTo(discount) <= 0, "Amortization amount must not exceed the discount, was: " + amortAmount);
        });
    }

    @Test
    @DisplayName("undoing the only repayment and running COB posts one amortization adjustment and returns net amortized income to zero")
    void undoRepayment_thenCOB_postsAmortizationAdjustment() {
        businessDateHelper.runAt("2026-06-01", () -> {
            final Long client = clientHelper.createClient("01 June 2026");
            final BigDecimal principal = BigDecimal.valueOf(9000);
            final BigDecimal discount = BigDecimal.valueOf(1000);
            final Long productId = createAccrualProductWithDiscount();
            final Long loanId = submitApproveDisburseWithDiscount(client, productId, principal, discount, "01 June 2026");

            // Repayment + COB recognizes some discount via an amortization transaction.
            final Long repaymentTxnId = wcLoanHelper.makeRepayment(loanId,
                    WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(3000), "01 June 2026"));
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-06-02");
            wcLoanHelper.executeInlineWCCOB(loanId);
            assertEquals(1, filterByType(wcLoanHelper.getTransactions(loanId), DISCOUNT_FEE_AMORTIZATION_CODE).size(),
                    "An amortization transaction must exist before the undo");
            assertTrue(netAmortization(loanId).compareTo(BigDecimal.ZERO) > 0, "Net amortized income must be positive before the undo");

            // Undo the repayment, then COB re-evaluates and posts an amortization adjustment.
            wcLoanHelper.undoTransaction(loanId, repaymentTxnId, WorkingCapitalLoanRequestBuilders.undoTransaction());
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-06-03");
            wcLoanHelper.executeInlineWCCOB(loanId);

            final List<GetWorkingCapitalLoanTransactionIdResponse> adjustments = filterByType(wcLoanHelper.getTransactions(loanId),
                    DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT_CODE);
            assertEquals(1, adjustments.size(),
                    "Exactly one amortization adjustment must be posted after undoing the only repayment, but found: "
                            + adjustments.size());
            assertEqualBigDecimal(BigDecimal.ZERO, netAmortization(loanId),
                    "Net amortized income (amortization - adjustment) must return to 0 after undoing the only repayment");
        });
    }

    @Test
    @DisplayName("undoing a discount fee adjustment that closed the loan gives the schedule its discount back")
    void undoDiscountFeeAdjustment_onClosedLoan_restoresScheduleDiscount() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final DiscountedLoan loan = disburseLoanAndAddDiscountFee();
            assertBaselineSummary(wcLoanHelper.getAmortizationSchedule(loan.loanId()), "when the discount is first added");

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-02");
            wcLoanHelper.makeRepayment(loan.loanId(), WorkingCapitalLoanRequestBuilders.repayment(NET_DISBURSEMENT, "02 January 2026"));
            assertEquals(Boolean.TRUE, wcLoanHelper.getLoanDetails(loan.loanId()).getStatus().getActive(),
                    "the loan still owes the 1000 discount fee, so it must stay active");

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-03");
            final Long adjustmentId = wcLoanHelper.makeDiscountFeeAdjustment(loan.loanId(),
                    WorkingCapitalLoanRequestBuilders.discountFeeAdjustment(DISCOUNT, "03 January 2026", loan.discountFeeTransactionId()));
            assertEquals(Boolean.TRUE, wcLoanHelper.getLoanDetails(loan.loanId()).getStatus().getClosedObligationsMet(),
                    "the adjustment settles the last 1000, so the loan must close as obligations met");

            wcLoanHelper.undoTransaction(loan.loanId(), adjustmentId, WorkingCapitalLoanRequestBuilders.undoTransaction());

            final GetWorkingCapitalLoansLoanIdResponse reopened = wcLoanHelper.getLoanDetails(loan.loanId());
            assertEquals(Boolean.TRUE, reopened.getStatus().getActive(), "undoing the adjustment must reopen the loan");
            assertEqualBigDecimal(DISCOUNT, reopened.getDiscountFee(), "the loan must carry its 1000 discount again");
            assertNotNull(reopened.getBalance(), "balance should exist after the undo");
            assertEqualBigDecimal(GROSS_OWED, reopened.getBalance().getPrincipal(), "the loan's principal must be the 10000 gross again");

            final ProjectedAmortizationScheduleData schedule = wcLoanHelper.getAmortizationSchedule(loan.loanId());
            assertEqualBigDecimal(DISCOUNT, schedule.getDiscountFeeAmount(),
                    "the schedule must carry the restored discount" + render(schedule));
            assertEqualBigDecimal(NET_DISBURSEMENT, schedule.getNetDisbursementAmount(),
                    "the net disbursement cannot move when an adjustment is undone" + render(schedule));
            assertEqualBigDecimal(DAILY_PAYMENT, schedule.getExpectedPaymentAmount(),
                    "the daily payment is (100000 x 18%) / 360 and never depends on the discount" + render(schedule));
            assertEquals(TERM_WITH_DISCOUNT, schedule.getOriginalPaymentNumber(),
                    "a 10000 gross at 50 a day is a 200 payment term" + render(schedule));

            final ProjectedAmortizationSchedulePaymentData disbursementRow = payment(schedule, 0);
            assertEqualBigDecimal(NET_DISBURSEMENT.negate(), disbursementRow.getExpectedPaymentAmount(),
                    "the disbursement row pays out the 9000" + render(schedule));
            assertEqualBigDecimal(NET_DISBURSEMENT, disbursementRow.getExpectedBalance(),
                    "the loan opens owing the 9000 handed over" + render(schedule));
            assertEqualBigDecimal(DISCOUNT, disbursementRow.getExpectedDiscountFeeBalance(),
                    "the whole discount fee is unearned at disbursement" + render(schedule));
            assertEqualBigDecimal(DISCOUNT, disbursementRow.getActualDiscountFeeBalance(),
                    "the actual fee column reads the same restored fee as the expected one" + render(schedule));

            assertAmortizationIsPopulatedPerPeriod(schedule, "after undoing the adjustment that closed the loan");

            final ProjectedAmortizationSchedulePaymentData coveredRow = payment(schedule, 1);
            assertNotNull(coveredRow.getActualAmortizationAmount(),
                    "the actual amortization column must be populated on a period the surviving repayment covers" + render(schedule));
            assertEquals(1, coveredRow.getActualAmortizationAmount().signum(),
                    "a period the surviving repayment covers must earn some of the fee, not 0.00" + render(schedule));
            assertNotNull(coveredRow.getActualDiscountFeeBalance(),
                    "the actual fee balance column must be populated on a covered period" + render(schedule));
            assertTrue(coveredRow.getActualDiscountFeeBalance().compareTo(DISCOUNT) < 0,
                    "the actual fee balance must have come down from 1000 once a period is covered, but was "
                            + coveredRow.getActualDiscountFeeBalance() + render(schedule));
            assertEqualBigDecimal(DISCOUNT, coveredRow.getActualAmortizationAmount().add(coveredRow.getActualDiscountFeeBalance()),
                    "what the covered period earned plus what it left unearned is the whole 1000 fee" + render(schedule));

            assertScheduleClosesCleanly(schedule, "after undoing the adjustment that closed the loan");
        });
    }

    @Test
    @DisplayName("undoing a repayment that closed a loan with an active charge gives the schedule its discount back")
    void undoRepayment_onClosedLoanWithCharge_restoresScheduleDiscount() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final DiscountedLoan loan = disburseLoanAndAddDiscountFee();
            assertBaselineSummary(wcLoanHelper.getAmortizationSchedule(loan.loanId()), "when the discount is first added");

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-05");
            addCharge(loan.loanId(), false, SCHEDULE_CHARGE_AMOUNT, "05 January 2026");

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-06");
            final Long repaymentId = wcLoanHelper.makeRepayment(loan.loanId(),
                    WorkingCapitalLoanRequestBuilders.repayment(new BigDecimal("10100"), "06 January 2026"));
            assertEquals(Boolean.TRUE, wcLoanHelper.getLoanDetails(loan.loanId()).getStatus().getClosedObligationsMet(),
                    "10100 settles the 10000 gross and the 100 fee, so the loan must close as obligations met");

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-07");
            wcLoanHelper.undoTransaction(loan.loanId(), repaymentId, WorkingCapitalLoanRequestBuilders.undoTransaction());

            final GetWorkingCapitalLoansLoanIdResponse reopened = wcLoanHelper.getLoanDetails(loan.loanId());
            assertEquals(Boolean.TRUE, reopened.getStatus().getActive(), "undoing the closing repayment must reopen the loan");
            assertEqualBigDecimal(DISCOUNT, reopened.getDiscountFee(), "the loan must still carry its 1000 discount");
            assertNotNull(reopened.getBalance(), "balance should exist after the undo");
            assertEqualBigDecimal(GROSS_OWED, reopened.getBalance().getPrincipal(), "the loan's principal must be the 10000 gross");
            assertEqualBigDecimal(BigDecimal.ZERO, reopened.getBalance().getPrincipalPaid(),
                    "the only repayment has been undone, so nothing is paid");
            assertEqualBigDecimal(DISCOUNT, reopened.getBalance().getTotalDiscountFee(), "the whole 1000 discount fee stands again");

            final ProjectedAmortizationScheduleData schedule = wcLoanHelper.getAmortizationSchedule(loan.loanId());
            assertBaselineSummary(schedule, "after undoing the repayment that closed the loan");

            final ProjectedAmortizationSchedulePaymentData disbursementRow = payment(schedule, 0);
            assertEqualBigDecimal(NET_DISBURSEMENT.negate(), disbursementRow.getExpectedPaymentAmount(),
                    "the disbursement row pays out the 9000" + render(schedule));
            assertEqualBigDecimal(NET_DISBURSEMENT, disbursementRow.getExpectedBalance(),
                    "the loan opens owing the 9000 handed over" + render(schedule));
            assertEqualBigDecimal(DISCOUNT, disbursementRow.getExpectedDiscountFeeBalance(),
                    "the whole discount fee is unearned again once the repayment is gone" + render(schedule));

            assertAmortizationIsPopulatedPerPeriod(schedule, "after undoing the repayment that closed the loan");
            assertAmortizationEarnsTheWholeDiscount(schedule, "after undoing the repayment that closed the loan");

            assertScheduleClosesCleanly(schedule, "after undoing the repayment that closed the loan");
        });
    }

    @Test
    @DisplayName("undoing a repayment that closed a charge-free loan leaves the schedule's discount untouched")
    void undoRepayment_onClosedChargeFreeLoan_keepsScheduleDiscount() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final DiscountedLoan loan = disburseLoanAndAddDiscountFee();
            assertBaselineSummary(wcLoanHelper.getAmortizationSchedule(loan.loanId()), "when the discount is first added");

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-06");
            final Long repaymentId = wcLoanHelper.makeRepayment(loan.loanId(),
                    WorkingCapitalLoanRequestBuilders.repayment(GROSS_OWED, "06 January 2026"));
            assertEquals(Boolean.TRUE, wcLoanHelper.getLoanDetails(loan.loanId()).getStatus().getClosedObligationsMet(),
                    "10000 settles the whole gross, so the loan must close as obligations met");

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-07");
            wcLoanHelper.undoTransaction(loan.loanId(), repaymentId, WorkingCapitalLoanRequestBuilders.undoTransaction());

            final GetWorkingCapitalLoansLoanIdResponse reopened = wcLoanHelper.getLoanDetails(loan.loanId());
            assertEquals(Boolean.TRUE, reopened.getStatus().getActive(), "undoing the closing repayment must reopen the loan");
            assertEqualBigDecimal(DISCOUNT, reopened.getDiscountFee(), "the loan must still carry its 1000 discount");

            final ProjectedAmortizationScheduleData schedule = wcLoanHelper.getAmortizationSchedule(loan.loanId());
            assertEqualBigDecimal(DISCOUNT, schedule.getDiscountFeeAmount(),
                    "an in-place undo must not touch the schedule's discount" + render(schedule));
            assertEquals(TERM_WITH_DISCOUNT, schedule.getOriginalPaymentNumber(),
                    "a 10000 gross at 50 a day is a 200 payment term" + render(schedule));
            assertEqualBigDecimal(DISCOUNT, payment(schedule, 0).getExpectedDiscountFeeBalance(),
                    "the whole discount fee is unearned again once the repayment is gone" + render(schedule));
        });
    }

    @Test
    @DisplayName("a discount fee adjustment that closes the loan really does leave it with no discount")
    void discountFeeAdjustment_closesLoan_leavesScheduleWithoutDiscount() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final DiscountedLoan loan = disburseLoanAndAddDiscountFee();
            assertBaselineSummary(wcLoanHelper.getAmortizationSchedule(loan.loanId()), "when the discount is first added");

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-02");
            wcLoanHelper.makeRepayment(loan.loanId(), WorkingCapitalLoanRequestBuilders.repayment(NET_DISBURSEMENT, "02 January 2026"));

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-03");
            wcLoanHelper.makeDiscountFeeAdjustment(loan.loanId(),
                    WorkingCapitalLoanRequestBuilders.discountFeeAdjustment(DISCOUNT, "03 January 2026", loan.discountFeeTransactionId()));

            final GetWorkingCapitalLoansLoanIdResponse closed = wcLoanHelper.getLoanDetails(loan.loanId());
            assertEquals(Boolean.TRUE, closed.getStatus().getClosedObligationsMet(), "the adjustment must close the loan");
            assertEqualBigDecimal(BigDecimal.ZERO, closed.getDiscountFee(), "the whole discount has been given back");

            final ProjectedAmortizationScheduleData schedule = wcLoanHelper.getAmortizationSchedule(loan.loanId());
            assertEqualBigDecimal(BigDecimal.ZERO, schedule.getDiscountFeeAmount(),
                    "the loan's own discount is zero, so the schedule's must be too" + render(schedule));
            assertEquals(TERM_WITHOUT_DISCOUNT, schedule.getOriginalPaymentNumber(),
                    "9000 at 50 a day is a 180 payment term once the discount is gone" + render(schedule));
        });
    }

    @Test
    @DisplayName("a backdated repayment covering the past-due minimum payment clears the loan's delinquency")
    void backdatedRepayment_reevaluatesDelinquency() {
        businessDateHelper.runAt("2026-07-01", () -> {
            final Long client = clientHelper.createClient("01 July 2026");
            final BigDecimal principal = BigDecimal.valueOf(9000);
            final Long productId = createDelinquencyProduct();
            final Long loanId = createAndDisburseLoanOnProduct(client, productId, principal, "01 July 2026");

            // Let the first delinquency period elapse unpaid, then COB classifies the loan as delinquent.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-07-26");
            wcLoanHelper.executeInlineWCCOB(loanId);
            final LocalDate expectedDelinquencyStart = LocalDate.of(2026, 7, 1);
            assertEquals(expectedDelinquencyStart, wcLoanHelper.getLoanDetails(loanId).getDelinquencyStartDate(),
                    "Loan must become delinquent (start date = disbursement date) once the first period elapses unpaid");

            // A backdated repayment that covers the period's minimum payment (3% of 9000 = 270) clears the delinquency.
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(300), "05 July 2026"));

            assertNull(wcLoanHelper.getLoanDetails(loanId).getDelinquencyStartDate(),
                    "Delinquency must be cleared after a backdated repayment that covers the past-due minimum payment");
        });
    }

    @Test
    @DisplayName("undoing a delinquency-clearing repayment restores the loan to delinquent after COB re-evaluation")
    void undoRepayment_reevaluatesDelinquency() {
        businessDateHelper.runAt("2026-08-01", () -> {
            final Long client = clientHelper.createClient("01 August 2026");
            final BigDecimal principal = BigDecimal.valueOf(9000);
            final Long productId = createDelinquencyProduct();
            final Long loanId = createAndDisburseLoanOnProduct(client, productId, principal, "01 August 2026");

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-08-26");
            wcLoanHelper.executeInlineWCCOB(loanId);
            final LocalDate expectedDelinquencyStart = LocalDate.of(2026, 8, 1);
            assertEquals(expectedDelinquencyStart, wcLoanHelper.getLoanDetails(loanId).getDelinquencyStartDate(),
                    "Loan must be delinquent before the repayment");

            // A repayment covering the minimum payment clears the delinquency.
            final Long repaymentTxnId = wcLoanHelper.makeRepayment(loanId,
                    WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(300), "05 August 2026"));
            assertNull(wcLoanHelper.getLoanDetails(loanId).getDelinquencyStartDate(),
                    "Delinquency must be cleared once the minimum payment is covered");

            // Undoing the clearing repayment re-opens the past-due period; COB re-evaluates it back to delinquent.
            wcLoanHelper.undoTransaction(loanId, repaymentTxnId, WorkingCapitalLoanRequestBuilders.undoTransaction());
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-08-27");
            wcLoanHelper.executeInlineWCCOB(loanId);

            assertEquals(expectedDelinquencyStart, wcLoanHelper.getLoanDetails(loanId).getDelinquencyStartDate(),
                    "Delinquency must be restored after the clearing repayment is undone and re-evaluated");
        });
    }

    @Test
    @DisplayName("a backdated repayment covering the past-due minimum clears the breached period's outstanding and breach flag")
    void backdatedRepayment_reevaluatesBreach() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long client = clientHelper.createClient("01 January 2026");
            final Long productId = createBreachProduct();
            final Long loanId = createAndDisburseLoanOnProduct(client, productId, BigDecimal.valueOf(9000), "01 January 2026");

            // Period 1 spans 01-31 Jan. The COB step runs one day behind the business date, so COB on 01 Feb evaluates
            // 31 Jan (the period's last day) and flags it breached while the 500 minimum is unpaid.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-02-01");
            wcLoanHelper.executeInlineWCCOB(loanId);
            final WorkingCapitalLoanBreachScheduleData breached = findBreachPeriod(wcLoanHelper.getBreachSchedule(loanId), 1);
            assertEqualBigDecimal(BREACH_MIN_PAYMENT_AMOUNT, breached.getMinPaymentAmount(),
                    "Period 1 minimum payment must be the flat 500 breach amount");
            assertEqualBigDecimal(BREACH_MIN_PAYMENT_AMOUNT, breached.getOutstandingAmount(),
                    "Period 1 outstanding must be the full 500 while the minimum payment is unpaid");
            assertEquals(Boolean.TRUE, breached.getBreach(), "Period 1 must be flagged breached once it elapses unpaid");

            // A backdated repayment dated inside period 1 that covers the 500 minimum re-evaluates and clears the
            // breach.
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(500), "15 January 2026"));

            final WorkingCapitalLoanBreachScheduleData cleared = findBreachPeriod(wcLoanHelper.getBreachSchedule(loanId), 1);
            assertEqualBigDecimal(BigDecimal.ZERO, cleared.getOutstandingAmount(),
                    "Period 1 outstanding must drop to 0 after the backdated repayment covers the minimum payment");
            assertEquals(Boolean.FALSE, cleared.getBreach(),
                    "Period 1 breach flag must be cleared (false) after the backdated repayment covers the minimum payment");
        });
    }

    @Test
    @DisplayName("undoing a breach-clearing repayment restores the ended period's outstanding and breach flag")
    void undoRepayment_reevaluatesBreach() {
        businessDateHelper.runAt("2026-03-01", () -> {
            final Long client = clientHelper.createClient("01 March 2026");
            final Long productId = createBreachProduct();
            final Long loanId = createAndDisburseLoanOnProduct(client, productId, BigDecimal.valueOf(9000), "01 March 2026");

            // Period 1 spans 01-31 Mar. The COB step runs one day behind the business date, so COB on 01 Apr evaluates
            // 31 Mar (the period's last day) and flags it breached (500 minimum outstanding).
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-04-01");
            wcLoanHelper.executeInlineWCCOB(loanId);
            assertEquals(Boolean.TRUE, findBreachPeriod(wcLoanHelper.getBreachSchedule(loanId), 1).getBreach(),
                    "Period 1 must be breached before the clearing repayment");

            // A repayment dated inside period 1 that covers the 500 minimum clears the breach.
            final Long repaymentTxnId = wcLoanHelper.makeRepayment(loanId,
                    WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(500), "15 March 2026"));
            final WorkingCapitalLoanBreachScheduleData cleared = findBreachPeriod(wcLoanHelper.getBreachSchedule(loanId), 1);
            assertEqualBigDecimal(BigDecimal.ZERO, cleared.getOutstandingAmount(), "Period 1 outstanding cleared to 0 by the repayment");
            assertEquals(Boolean.FALSE, cleared.getBreach(), "Period 1 breach cleared (false) by the repayment");

            // Undoing the clearing repayment (business date 01 Apr is already past the 31 Mar period end) re-evaluates
            // and restores the breach.
            wcLoanHelper.undoTransaction(loanId, repaymentTxnId, WorkingCapitalLoanRequestBuilders.undoTransaction());

            final WorkingCapitalLoanBreachScheduleData reverted = findBreachPeriod(wcLoanHelper.getBreachSchedule(loanId), 1);
            assertEqualBigDecimal(BREACH_MIN_PAYMENT_AMOUNT, reverted.getOutstandingAmount(),
                    "Period 1 outstanding must be restored to the full 500 after undoing the clearing repayment");
            assertEquals(Boolean.TRUE, reverted.getBreach(),
                    "Period 1 breach flag must be restored (true) after undoing the clearing repayment, since the period has already ended");
        });
    }

    @Test
    @DisplayName("a backdated repayment triggers reprocessing that redistributes the later repayment's charge allocation onto principal")
    void backdatedRepayment_withCharges_reallocatesTransactions() {
        businessDateHelper.runAt("2026-09-01", () -> {
            final Long client = clientHelper.createClient("01 September 2026");
            final Long loanId = createAndDisburseLoanOnDate(client, BigDecimal.valueOf(9000), "01 September 2026");
            final Long feeLoanChargeId = addCharge(loanId, false, 100, "01 September 2026");

            // R1 on day 20 is the only repayment so far, so the 100 settles the fee in full.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-09-20");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(100), "20 September 2026"));
            final GetWorkingCapitalLoanTransactionIdResponse r1Before = findTransaction(wcLoanHelper.getTransactions(loanId),
                    LocalDate.of(2026, 9, 20), BigDecimal.valueOf(100));
            assertEqualBigDecimal(BigDecimal.valueOf(100), r1Before.getFeeChargesPortion(), "Before reprocessing R1 settles the whole fee");
            assertEqualBigDecimal(BigDecimal.ZERO, r1Before.getPrincipalPortion(), "Before reprocessing R1 allocates nothing to principal");

            // A backdated R2 on day 10 settles the fee first when replayed chronologically, so reprocessing must move
            // R1's allocation from the fee onto principal.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-09-25");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(100), "10 September 2026"));

            final List<GetWorkingCapitalLoanTransactionIdResponse> transactions = wcLoanHelper.getTransactions(loanId);
            final GetWorkingCapitalLoanTransactionIdResponse r2 = findTransaction(transactions, LocalDate.of(2026, 9, 10),
                    BigDecimal.valueOf(100));
            assertEqualBigDecimal(BigDecimal.valueOf(100), r2.getFeeChargesPortion(), "The backdated R2 settles the fee");
            assertEqualBigDecimal(BigDecimal.ZERO, r2.getPrincipalPortion(), "The backdated R2 allocates nothing to principal");
            final GetWorkingCapitalLoanTransactionIdResponse r1After = findTransaction(transactions, LocalDate.of(2026, 9, 20),
                    BigDecimal.valueOf(100));
            assertEqualBigDecimal(BigDecimal.ZERO, r1After.getFeeChargesPortion(), "After reprocessing R1 no longer settles the fee");
            assertEqualBigDecimal(BigDecimal.valueOf(100), r1After.getPrincipalPortion(),
                    "After reprocessing R1 is redistributed to principal");

            final GetBalance balance = wcLoanHelper.getLoanDetails(loanId).getBalance();
            assertNotNull(balance, "Balance should exist");
            assertEqualBigDecimal(BigDecimal.valueOf(100), balance.getFeePaid(), "Fee paid is 100 (settled exactly once)");
            assertEqualBigDecimal(BigDecimal.valueOf(100), balance.getPrincipalPaid(), "Principal paid is 100 after redistribution");
            assertEqualBigDecimal(BigDecimal.valueOf(8900), balance.getPrincipalOutstanding(), "Principal outstanding is 9000 - 100");

            final WorkingCapitalLoanChargeData feeCharge = findCharge(wcLoanHelper.getCharges(loanId), feeLoanChargeId);
            assertEqualBigDecimal(BigDecimal.valueOf(100), feeCharge.getAmountPaid(), "The fee charge is paid 100 (not double-counted)");
            assertTrue(feeCharge.getPaid(), "The fee charge should be flagged paid");
        });
    }

    @Test
    @DisplayName("undoing a fee-settling repayment flags the original GL legs reversed and posts mirrored reversal legs")
    void undoRepayment_withFees_reversesGLEntries() {
        businessDateHelper.runAt("2026-10-01", () -> {
            final Long client = clientHelper.createClient("01 October 2026");
            final Long productId = createAccrualProductWithoutDiscount();
            final Long loanId = createAndDisburseLoanOnProduct(client, productId, BigDecimal.valueOf(9000), "01 October 2026");
            addCharge(loanId, false, 100, "01 October 2026");

            // A 100 repayment settles the fee in full.
            final Long repaymentTxnId = wcLoanHelper.makeRepayment(loanId,
                    WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(100), "01 October 2026"));
            final List<JournalEntryTransactionItem> original = journalEntriesFor(repaymentTxnId);
            assertJournalEntry(original, "DEBIT", fundSourceAccount, BigDecimal.valueOf(100), false);
            assertJournalEntry(original, "CREDIT", feesReceivableAccount, BigDecimal.valueOf(100), false);

            // Undo must reverse both legs.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-10-02");
            wcLoanHelper.undoTransaction(loanId, repaymentTxnId, WorkingCapitalLoanRequestBuilders.undoTransaction());

            final List<JournalEntryTransactionItem> afterUndo = journalEntriesFor(repaymentTxnId);
            // Original legs are now flagged reversed ...
            assertJournalEntry(afterUndo, "DEBIT", fundSourceAccount, BigDecimal.valueOf(100), true);
            assertJournalEntry(afterUndo, "CREDIT", feesReceivableAccount, BigDecimal.valueOf(100), true);
            // ... and mirrored reversal legs are posted (opposite entry type, same accounts and amounts).
            assertJournalEntry(afterUndo, "CREDIT", fundSourceAccount, BigDecimal.valueOf(100), false);
            assertJournalEntry(afterUndo, "DEBIT", feesReceivableAccount, BigDecimal.valueOf(100), false);
        });
    }

    @Test
    @DisplayName("undoing a penalty-settling repayment flags the original GL legs reversed and posts mirrored reversal legs")
    void undoRepayment_withPenalties_reversesGLEntries() {
        businessDateHelper.runAt("2026-11-01", () -> {
            final Long client = clientHelper.createClient("01 November 2026");
            final Long productId = createAccrualProductWithoutDiscount();
            final Long loanId = createAndDisburseLoanOnProduct(client, productId, BigDecimal.valueOf(9000), "01 November 2026");
            addCharge(loanId, true, 100, "01 November 2026");

            // A 100 repayment settles the penalty in full.
            final Long repaymentTxnId = wcLoanHelper.makeRepayment(loanId,
                    WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(100), "01 November 2026"));
            final List<JournalEntryTransactionItem> original = journalEntriesFor(repaymentTxnId);
            assertJournalEntry(original, "DEBIT", fundSourceAccount, BigDecimal.valueOf(100), false);
            assertJournalEntry(original, "CREDIT", penaltiesReceivableAccount, BigDecimal.valueOf(100), false);

            // Undo must reverse both legs.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-11-02");
            wcLoanHelper.undoTransaction(loanId, repaymentTxnId, WorkingCapitalLoanRequestBuilders.undoTransaction());

            final List<JournalEntryTransactionItem> afterUndo = journalEntriesFor(repaymentTxnId);
            assertJournalEntry(afterUndo, "DEBIT", fundSourceAccount, BigDecimal.valueOf(100), true);
            assertJournalEntry(afterUndo, "CREDIT", penaltiesReceivableAccount, BigDecimal.valueOf(100), true);
            assertJournalEntry(afterUndo, "CREDIT", fundSourceAccount, BigDecimal.valueOf(100), false);
            assertJournalEntry(afterUndo, "DEBIT", penaltiesReceivableAccount, BigDecimal.valueOf(100), false);
        });
    }

    private Long createAndDisburseLoanOnDate(final Long clientId, final BigDecimal principal, final String date) {
        return createAndDisburseLoanOnProduct(clientId, createPlainProduct(), principal, date);
    }

    private Long createAndDisburseLoanOnProduct(final Long clientId, final Long productId, final BigDecimal principal, final String date) {
        final Long loanId = wcLoanHelper.submitApplication(
                WorkingCapitalLoanRequestBuilders.submitApplication(clientId, productId, principal, BigDecimal.valueOf(18), date, date));
        createdLoanIds.add(loanId);
        wcLoanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approve(date, principal, date));
        wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburse(date, principal));
        return loanId;
    }

    private Long submitApproveDisburseWithDiscount(final Long clientId, final Long productId, final BigDecimal principal,
            final BigDecimal discount, final String date) {
        final Long loanId = wcLoanHelper.submitApplication(WorkingCapitalLoanRequestBuilders
                .submitApplication(clientId, productId, principal, BigDecimal.valueOf(0.18), date, date).discount(discount));
        createdLoanIds.add(loanId);
        wcLoanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approveWithDiscount(date, principal, date, discount));
        wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburseWithDiscount(date, principal, discount));
        return loanId;
    }

    private record DiscountedLoan(Long loanId, Long discountFeeTransactionId) {
    }

    private DiscountedLoan disburseLoanAndAddDiscountFee() {
        final Long clientId = clientHelper.createClient(SCHEDULE_DISBURSEMENT_DATE);
        final Long productId = createAccrualProductWithDiscount();
        final Long loanId = wcLoanHelper.submitApplication(WorkingCapitalLoanRequestBuilders.submitApplication(clientId, productId,
                NET_DISBURSEMENT, PERIOD_PAYMENT_RATE, SCHEDULE_DISBURSEMENT_DATE, SCHEDULE_DISBURSEMENT_DATE));
        createdLoanIds.add(loanId);
        wcLoanHelper.approve(loanId,
                WorkingCapitalLoanRequestBuilders.approve(SCHEDULE_DISBURSEMENT_DATE, NET_DISBURSEMENT, SCHEDULE_DISBURSEMENT_DATE));
        final Long disbursementTransactionId = wcLoanHelper.disburse(loanId,
                WorkingCapitalLoanRequestBuilders.disburse(SCHEDULE_DISBURSEMENT_DATE, NET_DISBURSEMENT));
        final Long discountFeeTransactionId = wcLoanHelper.makeDiscountFee(loanId,
                WorkingCapitalLoanRequestBuilders.discountFee(DISCOUNT, disbursementTransactionId));
        return new DiscountedLoan(loanId, discountFeeTransactionId);
    }

    private Long createPlainProduct() {
        final Long productId = productHelper.createWorkingCapitalLoanProduct(
                new WorkingCapitalLoanProductTestBuilder().withName("WCL UndoBackdate " + Utils.uniqueRandomStringGenerator("", 8))
                        .withShortName(Utils.uniqueRandomStringGenerator("", 4)).build())
                .getResourceId();
        createdProductIds.add(productId);
        return productId;
    }

    private Long createDelinquencyProduct() {
        final PostDelinquencyRangeResponse range = DelinquencyRangesHelper.createRange(new DelinquencyRangeRequest()
                .classification(Utils.randomStringGenerator("DLQ_R_", 10)).minimumAgeDays(1).maximumAgeDays(60).locale("en"));
        final PostDelinquencyBucketResponse bucket = WorkingCapitalLoanDelinquencyRangeScheduleHelper
                .createWorkingCapitalLoanDelinquencyBucket(List.of(range.getResourceId()), DELINQUENCY_FREQUENCY_DAYS, 0,
                        DELINQUENCY_MIN_PAYMENT_PERCENT, 1);
        final Long productId = productHelper.createWorkingCapitalLoanProduct(
                new WorkingCapitalLoanProductTestBuilder().withName("WCL UndoDelinq " + Utils.uniqueRandomStringGenerator("", 8))
                        .withShortName(Utils.uniqueRandomStringGenerator("", 4)).withDelinquencyBucketId(bucket.getResourceId())
                        .withDelinquencyGraceDays(DELINQUENCY_GRACE_DAYS).build())
                .getResourceId();
        createdProductIds.add(productId);
        return productId;
    }

    private Long createBreachProduct() {
        final Long breachId = breachHelper.create(breachHelper.createBreachRequest(Utils.randomStringGenerator("WC_BREACH_", 8),
                BREACH_FREQUENCY, BREACH_FREQUENCY_TYPE, BREACH_AMOUNT_CALCULATION_TYPE, BREACH_MIN_PAYMENT_AMOUNT));
        final Long productId = productHelper.createWorkingCapitalLoanProduct(
                new WorkingCapitalLoanProductTestBuilder().withName("WCL UndoBreach " + Utils.uniqueRandomStringGenerator("", 8))
                        .withShortName(Utils.uniqueRandomStringGenerator("", 4)).withBreachId(breachId).build())
                .getResourceId();
        createdProductIds.add(productId);
        return productId;
    }

    private Long createAccrualProductWithDiscount() {
        return createAccrualProduct(true);
    }

    private Long createAccrualProductWithoutDiscount() {
        return createAccrualProduct(false);
    }

    private Long createAccrualProduct(final boolean withDiscount) {
        WorkingCapitalLoanProductTestBuilder builder = new WorkingCapitalLoanProductTestBuilder()
                .withName("WCL UndoCash " + UUID.randomUUID().toString().substring(0, 8))
                .withShortName(UUID.randomUUID().toString().replace("-", "").substring(0, 4))
                .withAccountingRule(AccountingRuleEnum.ACC_DEF_REV_AM).withFundSourceAccountId(fundSourceAccount.getAccountID().longValue())
                .withLoanPortfolioAccountId(loanPortfolioAccount.getAccountID().longValue())
                .withTransfersInSuspenseAccountId(transfersSuspenseAccount.getAccountID().longValue())
                .withIncomeFromDiscountFeeAccountId(incomeFromDiscountFeeAccount.getAccountID().longValue())
                .withReceivableFeeAccountId(feesReceivableAccount.getAccountID().longValue())
                .withReceivablePenaltyAccountId(penaltiesReceivableAccount.getAccountID().longValue())
                .withIncomeFromFeeAccountId(incomeFromFeeAccount.getAccountID().longValue())
                .withIncomeFromPenaltyAccountId(incomeFromPenaltyAccount.getAccountID().longValue())
                .withIncomeFromRecoveryAccountId(incomeFromRecoveryAccount.getAccountID().longValue())
                .withWriteOffAccountId(writeOffAccount.getAccountID().longValue())
                .withOverpaymentLiabilityAccountId(overpaymentAccount.getAccountID().longValue())
                .withDeferredIncomeLiabilityAccountId(deferredIncomeAccount.getAccountID().longValue());
        if (withDiscount) {
            builder = builder.withAllowAttributeOverrides(Map.of("discountDefault", Boolean.TRUE));
        }
        final Long productId = productHelper.createWorkingCapitalLoanProduct(builder.build()).getResourceId();
        createdProductIds.add(productId);
        return productId;
    }

    private Long addCharge(final Long loanId, final boolean penalty, final double amount, final String dueDate) {
        final Long chargeId = wcLoanHelper.createGlobalCharge(WorkingCapitalLoanRequestBuilders.specifiedDueDateCharge(penalty, amount));
        return wcLoanHelper.addCharge(loanId, WorkingCapitalLoanRequestBuilders.addCharge(chargeId, amount, dueDate));
    }

    private List<GetWorkingCapitalLoanTransactionIdResponse> filterByType(
            final List<GetWorkingCapitalLoanTransactionIdResponse> transactions, final String typeCode) {
        return transactions.stream()
                .filter(txn -> txn.getType() != null && typeCode.equals(txn.getType().getCode()) && !Boolean.TRUE.equals(txn.getReversed()))
                .toList();
    }

    private BigDecimal netAmortization(final Long loanId) {
        final List<GetWorkingCapitalLoanTransactionIdResponse> txns = wcLoanHelper.getTransactions(loanId);
        return sumAmounts(filterByType(txns, DISCOUNT_FEE_AMORTIZATION_CODE))
                .subtract(sumAmounts(filterByType(txns, DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT_CODE)));
    }

    private static BigDecimal sumAmounts(final List<GetWorkingCapitalLoanTransactionIdResponse> transactions) {
        return transactions.stream().map(GetWorkingCapitalLoanTransactionIdResponse::getTransactionAmount).filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<JournalEntryTransactionItem> journalEntriesFor(final Long wcTransactionId) {
        final GetJournalEntriesTransactionIdResponse response = journalHelper.getJournalEntriesByTransactionId("WC" + wcTransactionId);
        if (response == null || response.getPageItems() == null) {
            return List.of();
        }
        return response.getPageItems();
    }

    private static GetWorkingCapitalLoanTransactionIdResponse findTransactionById(
            final List<GetWorkingCapitalLoanTransactionIdResponse> transactions, final Long transactionId) {
        return transactions.stream().filter(txn -> transactionId.equals(txn.getId())).findFirst()
                .orElseThrow(() -> new AssertionError("Transaction not found with id " + transactionId));
    }

    private static WorkingCapitalLoanBreachScheduleData findBreachPeriod(final List<WorkingCapitalLoanBreachScheduleData> periods,
            final int periodNumber) {
        return periods.stream().filter(period -> period.getPeriodNumber() != null && periodNumber == period.getPeriodNumber()).findFirst()
                .orElseThrow(() -> new AssertionError("Breach schedule period " + periodNumber + " not found"));
    }

    private static void assertJournalEntry(final List<JournalEntryTransactionItem> entries, final String expectedType,
            final Account expectedAccount, final BigDecimal expectedAmount, final boolean expectedReversed) {
        final boolean found = entries.stream()
                .anyMatch(entry -> expectedType.equals(entry.getEntryType().getValue())
                        && expectedAccount.getAccountID().longValue() == entry.getGlAccountId()
                        && expectedAmount.compareTo(BigDecimal.valueOf(entry.getAmount())) == 0
                        && expectedReversed == Boolean.TRUE.equals(entry.getReversed()));
        assertTrue(found,
                "Expected journal entry " + expectedType + " account=" + expectedAccount.getAccountID() + " amount=" + expectedAmount
                        + " reversed=" + expectedReversed + " not found in: " + entries.stream().map(e -> e.getEntryType().getValue()
                                + " acct=" + e.getGlAccountId() + " amt=" + e.getAmount() + " reversed=" + e.getReversed()).toList());
    }

    private static void assertBaselineSummary(final ProjectedAmortizationScheduleData schedule, final String context) {
        assertEqualBigDecimal(DISCOUNT, schedule.getDiscountFeeAmount(), "discountFeeAmount " + context + render(schedule));
        assertEqualBigDecimal(NET_DISBURSEMENT, schedule.getNetDisbursementAmount(), "netDisbursementAmount " + context + render(schedule));
        assertEqualBigDecimal(TOTAL_PAYMENT_VOLUME, schedule.getTotalPaymentVolume(), "totalPaymentVolume " + context + render(schedule));
        assertEqualBigDecimal(PERIOD_PAYMENT_RATE, schedule.getPeriodPaymentRate(), "periodPaymentRate " + context + render(schedule));
        assertEquals(NPV_DAY_COUNT, schedule.getNpvDayCount(), "npvDayCount " + context + render(schedule));
        assertEqualBigDecimal(DAILY_PAYMENT, schedule.getExpectedPaymentAmount(), "expectedPaymentAmount " + context + render(schedule));
        assertEquals(TERM_WITH_DISCOUNT, schedule.getOriginalPaymentNumber(), "originalPaymentNumber " + context + render(schedule));
    }

    private static void assertAmortizationIsPopulatedPerPeriod(final ProjectedAmortizationScheduleData schedule, final String context) {
        BigDecimal total = BigDecimal.ZERO;
        for (final ProjectedAmortizationSchedulePaymentData row : schedule.getPayments()) {
            if (row.getPaymentNo() == null || row.getPaymentNo() == 0) {
                continue;
            }
            assertNotNull(row.getExpectedAmortizationAmount(),
                    "period " + row.getPaymentNo() + " states no amortization amount " + context + render(schedule));
            total = total.add(row.getExpectedAmortizationAmount());
        }
        assertEquals(1, total.signum(),
                "the amortization column must earn some of the fee, not read 0.00 in every row " + context + render(schedule));
    }

    private static void assertAmortizationEarnsTheWholeDiscount(final ProjectedAmortizationScheduleData schedule, final String context) {
        final BigDecimal amortized = schedule.getPayments().stream().filter(row -> row.getPaymentNo() != null && row.getPaymentNo() > 0)
                .map(ProjectedAmortizationSchedulePaymentData::getExpectedAmortizationAmount).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEqualBigDecimal(DISCOUNT, amortized,
                "the amortization column must earn the whole 1000 discount across the term " + context + render(schedule));
    }

    private static void assertScheduleClosesCleanly(final ProjectedAmortizationScheduleData schedule, final String context) {
        final ProjectedAmortizationSchedulePaymentData last = schedule.getPayments().getLast();
        assertNotNull(last.getExpectedBalance(), "the last period must state a balance " + context + render(schedule));
        assertEquals(0, last.getExpectedBalance().signum(), "the schedule must end on a cleared balance " + context + render(schedule));
        assertNotNull(last.getExpectedDiscountFeeBalance(), "the last period must state a fee balance " + context + render(schedule));
        assertEquals(0, last.getExpectedDiscountFeeBalance().signum(),
                "the fee must be fully earned by the time the balance clears " + context + render(schedule));
    }

    private static ProjectedAmortizationSchedulePaymentData payment(final ProjectedAmortizationScheduleData schedule, final int paymentNo) {
        return schedule.getPayments().stream().filter(row -> row.getPaymentNo() != null && row.getPaymentNo() == paymentNo).findFirst()
                .orElseThrow(() -> new AssertionError("no payment " + paymentNo + " in" + render(schedule)));
    }

    private static String render(final ProjectedAmortizationScheduleData schedule) {
        final List<ProjectedAmortizationSchedulePaymentData> payments = schedule.getPayments();
        final StringBuilder out = new StringBuilder("\nschedule: discountFeeAmount=").append(schedule.getDiscountFeeAmount())
                .append(" netDisbursementAmount=").append(schedule.getNetDisbursementAmount()).append(" expectedPaymentAmount=")
                .append(schedule.getExpectedPaymentAmount()).append(" originalPaymentNumber=").append(schedule.getOriginalPaymentNumber())
                .append(" periods=").append(payments.size()).append('\n');
        for (final ProjectedAmortizationSchedulePaymentData row : payments.subList(0, Math.min(4, payments.size()))) {
            out.append(renderRow(row));
        }
        if (payments.size() > 4) {
            out.append("  ...\n").append(renderRow(payments.getLast()));
        }
        return out.toString();
    }

    private static String renderRow(final ProjectedAmortizationSchedulePaymentData row) {
        return new StringBuilder("  #").append(row.getPaymentNo()).append(' ').append(row.getPaymentDate()).append(" payment=")
                .append(row.getExpectedPaymentAmount()).append(" balance=").append(row.getExpectedBalance()).append(" amort=")
                .append(row.getExpectedAmortizationAmount()).append(" feeBalance=").append(row.getExpectedDiscountFeeBalance())
                .append(" actualAmort=").append(row.getActualAmortizationAmount()).append(" actualFeeBalance=")
                .append(row.getActualDiscountFeeBalance()).append('\n').toString();
    }
}
