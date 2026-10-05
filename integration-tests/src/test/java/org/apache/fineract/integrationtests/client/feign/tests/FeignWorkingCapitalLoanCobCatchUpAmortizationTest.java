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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.fineract.client.models.GetJournalEntriesTransactionIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsRequest.AccountingRuleEnum;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessDateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignJournalEntryHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalLoanRequestBuilders;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The discount fee amortization COB step must never date an amortization before the payment that earns it.
 *
 * <p>
 * The step posts one delta transaction dated on the COB date it is running for, but derives the target from the whole
 * amortization model - every payment ever applied, regardless of date. So a payment dated after the COB date being
 * processed is already in the target, and its share of the income is booked on the earlier day. Two ways to get there:
 *
 * <ul>
 * <li><b>Case A</b> - the COB date trails the business date by a day, so a repayment made today is always ahead of the
 * COB date the nightly run processes.</li>
 * <li><b>Case B</b> - the COB is behind and catches up: the inline executor replays each missing day from
 * {@code lastClosedBusinessDate + 1}, and the earliest replayed day sees the whole delta, including payments dated days
 * later.</li>
 * </ul>
 *
 * <p>
 * Both cases put the income journal entry before the cash was received.
 */
public class FeignWorkingCapitalLoanCobCatchUpAmortizationTest extends FeignIntegrationTest {

    private static final String DISCOUNT_FEE_AMORTIZATION_CODE = "loanTransactionType.discountFeeAmortization";
    private static final String DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT_CODE = "loanTransactionType.discountFeeAmortizationAdjustment";

    private static final BigDecimal PRINCIPAL = BigDecimal.valueOf(9000);
    private static final BigDecimal DISCOUNT = BigDecimal.valueOf(1000);
    private static final BigDecimal REPAYMENT = BigDecimal.valueOf(50);

    /** Amortization earned by the first daily 50 payment on this 9000 / 100000 / 18 / 1000 loan. */
    private static final BigDecimal DAY_1_AMORTIZATION = new BigDecimal("9.61");
    /** Amortization earned by the second daily 50 payment. */
    private static final BigDecimal DAY_2_AMORTIZATION = new BigDecimal("9.57");

    private FeignWorkingCapitalLoanHelper wcLoanHelper;
    private FeignClientHelper clientHelper;
    private FeignBusinessDateHelper businessDateHelper;
    private FeignJournalEntryHelper journalHelper;
    private WorkingCapitalLoanProductHelper productHelper;

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

        final FeignAccountHelper accountHelper = new FeignAccountHelper(feignClient);
        fundSourceAccount = accountHelper.createLiabilityAccount("wcCobFundSrc");
        loanPortfolioAccount = accountHelper.createAssetAccount("wcCobLoanPort");
        transfersSuspenseAccount = accountHelper.createAssetAccount("wcCobXferSusp");
        incomeFromDiscountFeeAccount = accountHelper.createIncomeAccount("wcCobIncDisc");
        feesReceivableAccount = accountHelper.createAssetAccount("wcCobFeesRcv");
        penaltiesReceivableAccount = accountHelper.createAssetAccount("wcCobPenRcv");
        incomeFromFeeAccount = accountHelper.createIncomeAccount("wcCobIncFee");
        incomeFromPenaltyAccount = accountHelper.createIncomeAccount("wcCobIncPen");
        incomeFromRecoveryAccount = accountHelper.createIncomeAccount("wcCobIncRec");
        writeOffAccount = accountHelper.createExpenseAccount("wcCobWrtOff");
        overpaymentAccount = accountHelper.createLiabilityAccount("wcCobOverpay");
        deferredIncomeAccount = accountHelper.createLiabilityAccount("wcCobDefInc");
    }

    @AfterAll
    void cleanupEntities() {
        createdLoanIds.forEach(wcLoanHelper::cleanupLoan);
        createdLoanIds.clear();
        createdProductIds.clear();
    }

    /**
     * Repayments on 02 and 03 January; the COB run on business date 03 January processes COB date 02 January. Only the
     * 02 January payment has been earned by then, so exactly 9.61 may be recognized on 02 January. The 03 January
     * payment belongs to the next COB.
     */
    @Test
    @DisplayName("Case A: a COB run must not amortize a payment dated after its COB date")
    void normalCob_doesNotAmortizePaymentDatedAfterCobDate() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long loanId = createDisbursedLoanWithDiscount("01 January 2026");

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-02");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(REPAYMENT, "02 January 2026"));

            // Business date 03 January -> COB date 02 January. The 03 January repayment exists but is not yet earned.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-03");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(REPAYMENT, "03 January 2026"));
            wcLoanHelper.executeInlineWCCOB(loanId);

            final List<GetWorkingCapitalLoanTransactionIdResponse> afterFirstCob = amortizations(loanId);
            assertEquals(1, afterFirstCob.size(),
                    () -> "COB for 02 January must post exactly one amortization, got: " + describe(afterFirstCob));
            final GetWorkingCapitalLoanTransactionIdResponse firstAmortization = afterFirstCob.getFirst();
            assertEquals(LocalDate.of(2026, 1, 2), firstAmortization.getTransactionDate(),
                    "the amortization must be dated on the COB date being processed");
            assertAmount(DAY_1_AMORTIZATION, firstAmortization.getTransactionAmount(),
                    "COB for 02 January must recognize only the 02 January payment (9.61), not also the 03 January one (+9.57)");

            // The next COB (business date 04 January -> COB date 03 January) picks up the 03 January payment.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-04");
            wcLoanHelper.executeInlineWCCOB(loanId);

            final List<GetWorkingCapitalLoanTransactionIdResponse> afterSecondCob = amortizations(loanId);
            assertEquals(2, afterSecondCob.size(),
                    () -> "COB for 03 January must post the second day's amortization, got: " + describe(afterSecondCob));
            final GetWorkingCapitalLoanTransactionIdResponse secondAmortization = afterSecondCob.get(1);
            assertEquals(LocalDate.of(2026, 1, 3), secondAmortization.getTransactionDate(),
                    "the second amortization must be dated on the second COB date");
            assertAmount(DAY_2_AMORTIZATION, secondAmortization.getTransactionAmount(),
                    "the second COB must recognize the 03 January payment's share (9.57)");
        });
    }

    /**
     * One COB closes 01 January. The business date then jumps to 06 January, and a repayment is booked for 04 January.
     * The next COB run replays 02, 03, 04, and 05 January in turn. The amortization must land on 04 January (the
     * payment date) or later - never on 02 or 03 January, which precedes the cash.
     */
    @Test
    @DisplayName("Case B: catch-up COB must not date an amortization before the backdated payment it amortizes")
    void catchUpCob_doesNotDateAmortizationBeforeTheBackdatedPayment() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long loanId = createDisbursedLoanWithDiscount("01 January 2026");

            // Close 01 January so the catch-up below starts replaying from 02 January.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-02");
            wcLoanHelper.executeInlineWCCOB(loanId);
            assertTrue(amortizations(loanId).isEmpty(), "no payment has been made yet, so COB for 01 January must amortize nothing");

            // COB is now four days behind. The repayment is dated 04 January, inside the gap.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-06");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(REPAYMENT, "04 January 2026"));

            // Business date 06 January -> COB date 05 January: the run replays 02, 03, 04, 05 January.
            wcLoanHelper.executeInlineWCCOB(loanId);

            final List<GetWorkingCapitalLoanTransactionIdResponse> amortizations = amortizations(loanId);
            assertFalse(amortizations.isEmpty(), "the caught-up COB must recognize the 04 January payment");

            final LocalDate paymentDate = LocalDate.of(2026, 1, 4);
            for (final GetWorkingCapitalLoanTransactionIdResponse amortization : amortizations) {
                assertNotNull(amortization.getTransactionDate(), "amortization must carry a transaction date");
                assertFalse(amortization.getTransactionDate().isBefore(paymentDate),
                        () -> "an amortization must never be dated before the payment it amortizes - payment on " + paymentDate
                                + ", amortization on " + amortization.getTransactionDate() + "; all amortizations: "
                                + describe(amortizations));
            }

            // The total recognized is still exactly the one day's worth the single payment earned.
            assertAmount(DAY_1_AMORTIZATION, sum(amortizations),
                    "the caught-up COB must recognize exactly the 04 January payment's share of the discount");

            // The income journal entry inherits the transaction date, so it must not predate the cash either.
            for (final GetWorkingCapitalLoanTransactionIdResponse amortization : amortizations) {
                for (final JournalEntryTransactionItem entry : journalEntriesFor(amortization.getId())) {
                    assertNotNull(entry.getTransactionDate(), "journal entry must carry a transaction date");
                    assertFalse(entry.getTransactionDate().isBefore(paymentDate),
                            () -> "the discount fee income journal entry must not be dated before the cash was received - payment on "
                                    + paymentDate + ", entry on " + entry.getTransactionDate());
                }
            }
        });
    }

    /**
     * A loan is part paid, then a repayment lands on a later day, then a backdated repayment completes it. The
     * backdated one triggers the closure while carrying the earliest date - but by value date the loan was only settled
     * once the later payment was counted, so the closing top-up of the discount belongs on that later day. Dating it on
     * the trigger would recognize the last of the income before the cash that completed the settlement arrived.
     */
    @Test
    @DisplayName("a backdated repayment that closes the loan dates the closing amortization on the real settlement day")
    void backdatedClosingRepayment_datesClosingAmortizationOnTheSettlementDay() {
        businessDateHelper.runAt("2026-03-01", () -> {
            final Long loanId = createDisbursedLoanWithDiscount("01 March 2026");

            // 02 March: 9900 of the 10000 owed (9000 principal + 1000 discount), leaving 100.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-03-02");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(9900), "02 March 2026"));

            // COB recognizes most of the discount against that payment.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-03-04");
            wcLoanHelper.executeInlineWCCOB(loanId);

            // 10 March: 50 more, leaving 50.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-03-10");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(REPAYMENT, "10 March 2026"));

            // 05 March, booked on 12 March: the last 50. Cumulative by value date reaches 10000 only on 10 March, so
            // that - not 05 March - is the day the loan actually settled.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-03-12");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(REPAYMENT, "05 March 2026"));

            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            assertEquals("loanStatusType.closed.obligations.met", loan.getStatus().getCode(),
                    "the backdated repayment must close the loan");
            assertAmount(DISCOUNT, loan.getBalance().getRealizedIncomeFromDiscountFee(),
                    "closing the loan must recognize the whole discount");

            // The dates the API publishes must name the same day, not the transaction that happened to trigger the
            // closure. A client reading the timeline would otherwise be told the loan closed five days before the money
            // that closed it arrived.
            assertNotNull(loan.getTimeline(), "the loan must carry a timeline");
            assertEquals(LocalDate.of(2026, 3, 10), loan.getTimeline().getClosedOnDate(),
                    "timeline.closedOnDate must be the day the loan actually settled (10 March), not the backdated repayment "
                            + "that triggered the closure (05 March)");
            assertEquals(LocalDate.of(2026, 3, 10), loan.getTimeline().getActualMaturityDate(),
                    "timeline.actualMaturityDate must agree with the day the loan actually settled");

            final List<GetWorkingCapitalLoanTransactionIdResponse> amortizations = amortizations(loanId);
            final GetWorkingCapitalLoanTransactionIdResponse closing = amortizations.getLast();
            assertEquals(LocalDate.of(2026, 3, 10), closing.getTransactionDate(),
                    () -> "the closing amortization belongs on the day the loan actually settled (10 March), not on the backdated "
                            + "repayment that triggered it (05 March); all amortizations: " + describe(amortizations));

            // Nothing may be dated before the cash that earned it.
            for (final GetWorkingCapitalLoanTransactionIdResponse amortization : amortizations) {
                assertFalse(amortization.getTransactionDate().isAfter(LocalDate.of(2026, 3, 10)),
                        "no amortization may be dated after the settlement day");
            }
        });
    }

    @Test
    @DisplayName("undoing the overpaying repayment dates the closure on the settlement it falls back onto")
    void undoOfOverpayingRepayment_datesClosureOnTheEarlierSettlementDay() {
        businessDateHelper.runAt("2026-04-01", () -> {
            final Long loanId = createDisbursedLoanWithDiscount("01 April 2026");

            // 02 April: 9900 of the 10000 owed (9000 principal + 1000 discount), leaving 100.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-04-02");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(9900), "02 April 2026"));

            // 05 April: the last 100. The loan settles exactly, on this day.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-04-05");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(100), "05 April 2026"));

            // 08 April: 50 more on a loan that owes nothing, which tips it into overpayment.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-04-08");
            final Long overpayingTransactionId = wcLoanHelper.makeRepayment(loanId,
                    WorkingCapitalLoanRequestBuilders.repayment(REPAYMENT, "08 April 2026"));

            final GetWorkingCapitalLoansLoanIdResponse overpaid = wcLoanHelper.getLoanDetails(loanId);
            assertEquals("loanStatusType.overpaid", overpaid.getStatus().getCode(), "the extra payment must overpay the loan");
            assertEquals(LocalDate.of(2026, 4, 5), overpaid.getTimeline().getActualMaturityDate(),
                    "an extra payment does not move the day the loan met its obligations");

            // 12 April: the overpaying repayment is undone, which leaves the loan settled exactly again. The settlement
            // it falls back onto happened on 05 April - the undo is not what closed it.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-04-12");
            wcLoanHelper.undoTransaction(loanId, overpayingTransactionId);

            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            assertEquals("loanStatusType.closed.obligations.met", loan.getStatus().getCode(),
                    "undoing the excess must leave the loan settled exactly");
            assertEquals(LocalDate.of(2026, 4, 5), loan.getTimeline().getClosedOnDate(),
                    "timeline.closedOnDate must be the day the loan met its obligations (05 April), not the day the excess was "
                            + "undone (12 April)");
            assertEquals(LocalDate.of(2026, 4, 5), loan.getTimeline().getActualMaturityDate(),
                    "timeline.actualMaturityDate must not drift away from timeline.closedOnDate");
        });
    }

    @Test
    @DisplayName("a backdated repayment that overpays dates the maturity on the payment that finished the settlement")
    void backdatedOverpayingRepayment_datesMaturityOnTheSettlingPayment() {
        businessDateHelper.runAt("2026-05-01", () -> {
            final Long loanId = createDisbursedLoanWithDiscount("01 May 2026");

            // 02 May: 50 against the 10000 owed (9000 principal + 1000 discount).
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-05-02");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(REPAYMENT, "02 May 2026"));

            // 10 May: 9000 more, leaving 950 - the loan is still part-paid.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-05-10");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(9000), "10 May 2026"));

            // 05 May, booked on 12 May: 1000, which overpays the loan by 50. Allocated in date order the running total
            // reaches 10000 only at the 10 May payment, so that is the day the obligations were met - not 05 May, the
            // backdated payment that triggered the transition.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-05-12");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(1000), "05 May 2026"));

            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            assertEquals("loanStatusType.overpaid", loan.getStatus().getCode(), "the backdated repayment must overpay the loan");
            assertNotNull(loan.getTimeline(), "the loan must carry a timeline");
            assertEquals(LocalDate.of(2026, 5, 10), loan.getTimeline().getActualMaturityDate(),
                    "timeline.actualMaturityDate must be the day the obligations were met (10 May), not the backdated repayment "
                            + "that triggered the transition (05 May)");
        });
    }

    @Test
    @DisplayName("a backdated repayment that settles the loan earlier moves the maturity back")
    void backdatedRepaymentSettlingEarlier_movesTheMaturityBack() {
        businessDateHelper.runAt("2026-06-01", () -> {
            final Long loanId = createDisbursedLoanWithDiscount("01 June 2026");

            // 02 June: 9000 of the 10000 owed (9000 principal + 1000 discount), leaving 1000.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-06-02");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(9000), "02 June 2026"));

            // 10 June: 1500 more, which settles the last 1000 and overpays by 500. As it stands this is the day the
            // obligations were met.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-06-10");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(1500), "10 June 2026"));

            final GetWorkingCapitalLoansLoanIdResponse overpaid = wcLoanHelper.getLoanDetails(loanId);
            assertEquals("loanStatusType.overpaid", overpaid.getStatus().getCode(), "the second repayment must overpay the loan");
            assertEquals(LocalDate.of(2026, 6, 10), overpaid.getTimeline().getActualMaturityDate(),
                    "the 10 June payment is what met the obligations, so far");

            // 05 June, booked on 12 June: 1000. Allocated in date order the loan is now square at 05 June and the whole
            // 10 June payment is surplus - a payment the loan never needed, which cannot be what settled it. The
            // settlement day moves earlier, which is the direction a stored maturity is never able to follow on its
            // own.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-06-12");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(1000), "05 June 2026"));

            final GetWorkingCapitalLoansLoanIdResponse settledEarlier = wcLoanHelper.getLoanDetails(loanId);
            assertEquals("loanStatusType.overpaid", settledEarlier.getStatus().getCode(), "the backdated repayment leaves it overpaid");
            assertEquals(LocalDate.of(2026, 6, 5), settledEarlier.getTimeline().getActualMaturityDate(),
                    "timeline.actualMaturityDate must be the day the loan became square (05 June), not the payment it no longer "
                            + "needed (10 June)");

            // 15 June: the 1500 surplus is refunded, which closes the loan. A refund is a real money movement and the
            // account was not closed until it happened, so it carries its own date - matching core, which stamps both
            // dates from the refund on the same transition.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-06-15");
            wcLoanHelper.creditBalanceRefund(loanId,
                    WorkingCapitalLoanRequestBuilders.creditBalanceRefund(BigDecimal.valueOf(1500), "15 June 2026"));

            final GetWorkingCapitalLoansLoanIdResponse closed = wcLoanHelper.getLoanDetails(loanId);
            assertEquals("loanStatusType.closed.obligations.met", closed.getStatus().getCode(),
                    "refunding the whole surplus must close the loan");
            assertEquals(LocalDate.of(2026, 6, 15), closed.getTimeline().getClosedOnDate(),
                    "timeline.closedOnDate must be the day the refund closed the account (15 June)");
            assertEquals(LocalDate.of(2026, 6, 15), closed.getTimeline().getActualMaturityDate(),
                    "timeline.actualMaturityDate must not drift away from timeline.closedOnDate");
        });
    }

    /**
     * COB recognizes income against a payment that a later backdated repayment turns into surplus. The closing top-up
     * is dated on the earlier settlement day, but it must still count the income COB already posted after that day -
     * otherwise that income is recognized twice and the total exceeds the discount.
     */
    @Test
    @DisplayName("a backdated settlement before already-amortized income recognizes the discount exactly once")
    void backdatedSettlementBeforePostedIncome_recognizesTheDiscountExactlyOnce() {
        businessDateHelper.runAt("2026-07-01", () -> {
            final Long loanId = createDisbursedLoanWithDiscount("01 July 2026");

            // 02 July: 9000 of the 10000 owed (9000 principal + 1000 discount), leaving 1000.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-07-02");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(9000), "02 July 2026"));
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-07-03");
            wcLoanHelper.executeInlineWCCOB(loanId);

            // 10 July: 500 more, leaving 500. COB recognizes this payment's share on 10 July.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-07-10");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(500), "10 July 2026"));
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-07-11");
            wcLoanHelper.executeInlineWCCOB(loanId);

            final List<GetWorkingCapitalLoanTransactionIdResponse> beforeSettlement = amortizations(loanId);
            assertTrue(beforeSettlement.stream().anyMatch(txn -> LocalDate.of(2026, 7, 10).equals(txn.getTransactionDate())),
                    () -> "COB must have recognized the 10 July payment's share on 10 July; all amortizations: "
                            + describe(beforeSettlement));

            // 05 July, booked on 12 July: 1000. Allocated in date order the loan is square on 05 July and the whole
            // 10 July payment becomes surplus, so the loan is overpaid and settled on 05 July.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-07-12");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(1000), "05 July 2026"));

            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            assertEquals("loanStatusType.overpaid", loan.getStatus().getCode(), "the backdated repayment must overpay the loan");
            assertEquals(LocalDate.of(2026, 7, 5), loan.getTimeline().getActualMaturityDate(),
                    "the loan settled on 05 July, the 10 July payment is surplus");
            assertAmount(DISCOUNT, loan.getBalance().getRealizedIncomeFromDiscountFee(),
                    "the realized income must be the whole discount, not the discount plus the income COB posted on 10 July");

            final List<GetWorkingCapitalLoanTransactionIdResponse> amortizations = amortizations(loanId);
            assertAmount(DISCOUNT, sum(amortizations),
                    "the live amortizations must add up to the discount exactly; all amortizations: " + describe(amortizations));
        });
    }

    /**
     * A part-paid loan with income already recognized against its payments is overpaid by a backdated repayment that
     * carries the excess itself. The discount is recognized in full once, and nothing that follows - a further surplus
     * payment, the refund that closes the loan - recognizes any more of it.
     */
    @Test
    @DisplayName("a backdated overpaying repayment on a part-paid loan never recognizes more than the discount")
    void backdatedOverpayingRepaymentOnPartPaidLoan_neverRecognizesMoreThanTheDiscount() {
        businessDateHelper.runAt("2026-08-01", () -> {
            final Long loanId = createDisbursedLoanWithDiscount("01 August 2026");

            // 02 August: 9000 of the 10000 owed (9000 principal + 1000 discount), leaving 1000.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-08-02");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(9000), "02 August 2026"));
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-08-03");
            wcLoanHelper.executeInlineWCCOB(loanId);

            // 10 August: 500 more, leaving 500. COB recognizes this payment's share on 10 August.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-08-10");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(500), "10 August 2026"));
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-08-11");
            wcLoanHelper.executeInlineWCCOB(loanId);

            final GetWorkingCapitalLoansLoanIdResponse partPaid = wcLoanHelper.getLoanDetails(loanId);
            assertEquals("loanStatusType.active", partPaid.getStatus().getCode(), "the loan must still be part-paid");
            assertTrue(DISCOUNT.compareTo(netAmortized(loanId)) > 0, "a part-paid loan must not have recognized the whole discount yet");

            // 05 August, booked on 12 August: 1500. Allocated in date order it pays the last 1000 on 05 August and
            // carries 500 of excess itself, and the 10 August payment becomes surplus too: overpaid by 1000.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-08-12");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(1500), "05 August 2026"));

            final GetWorkingCapitalLoansLoanIdResponse overpaid = wcLoanHelper.getLoanDetails(loanId);
            assertEquals("loanStatusType.overpaid", overpaid.getStatus().getCode(), "the backdated repayment must overpay the loan");
            assertAmount(BigDecimal.valueOf(1000), overpaid.getBalance().getOverpaymentAmount(), "the loan must be overpaid by 1000");
            assertEquals(LocalDate.of(2026, 8, 5), overpaid.getTimeline().getActualMaturityDate(),
                    "the backdated repayment is what met the obligations");
            assertDiscountRecognizedExactlyOnce(loanId, overpaid, "after the backdated overpaying repayment");

            // 13 August: a further surplus payment. The loan is already fully paid, so it earns nothing.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-08-13");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(200), "13 August 2026"));
            assertDiscountRecognizedExactlyOnce(loanId, wcLoanHelper.getLoanDetails(loanId), "after a further surplus payment");

            // 14 August: the whole 1200 surplus is refunded, which closes the loan.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-08-14");
            wcLoanHelper.creditBalanceRefund(loanId,
                    WorkingCapitalLoanRequestBuilders.creditBalanceRefund(BigDecimal.valueOf(1200), "14 August 2026"));
            final GetWorkingCapitalLoansLoanIdResponse closed = wcLoanHelper.getLoanDetails(loanId);
            assertEquals("loanStatusType.closed.obligations.met", closed.getStatus().getCode(),
                    "refunding the whole surplus must close the loan");
            assertDiscountRecognizedExactlyOnce(loanId, closed, "after the refund closed the loan");
        });
    }

    /**
     * Undoing the settling repayment reopens the loan but leaves the closing top-up in place, so the discount is fully
     * recognized on a loan that is no longer paid. Repaying it in full again must top up only what is missing, whether
     * the loan is re-settled straight away on a day before the original settlement, or COB has first corrected the
     * income back down to the schedule.
     */
    @Test
    @DisplayName("reopening a settled loan by undo and repaying it again recognizes the discount exactly once")
    void undoReopenAndRepayAgain_recognizesTheDiscountExactlyOnce() {
        businessDateHelper.runAt("2026-09-01", () -> {
            final Long loanId = createDisbursedLoanWithDiscount("01 September 2026");

            // 02 September: 9900 of the 10000 owed (9000 principal + 1000 discount), leaving 100.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-09-02");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(9900), "02 September 2026"));
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-09-03");
            wcLoanHelper.executeInlineWCCOB(loanId);

            // 05 September: the last 100 closes the loan.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-09-05");
            final Long firstSettlingRepaymentId = wcLoanHelper.makeRepayment(loanId,
                    WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(100), "05 September 2026"));
            final GetWorkingCapitalLoansLoanIdResponse firstClosure = wcLoanHelper.getLoanDetails(loanId);
            assertEquals("loanStatusType.closed.obligations.met", firstClosure.getStatus().getCode(), "the last 100 must close the loan");
            assertDiscountRecognizedExactlyOnce(loanId, firstClosure, "after the first closure");

            // 07 September: the settling repayment is undone, which reopens the loan.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-09-07");
            wcLoanHelper.undoTransaction(loanId, firstSettlingRepaymentId);
            assertEquals("loanStatusType.active", wcLoanHelper.getLoanDetails(loanId).getStatus().getCode(),
                    "undoing the settling repayment must reopen the loan");

            // Same day, no COB in between: the missing 100 is booked for 04 September, before the original settlement
            // and so before the closing top-up it left behind. That top-up must still count against the new closure.
            final Long secondSettlingRepaymentId = wcLoanHelper.makeRepayment(loanId,
                    WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(100), "04 September 2026"));
            final GetWorkingCapitalLoansLoanIdResponse secondClosure = wcLoanHelper.getLoanDetails(loanId);
            assertEquals("loanStatusType.closed.obligations.met", secondClosure.getStatus().getCode(),
                    "repaying the missing 100 must close the loan again");
            assertEquals(LocalDate.of(2026, 9, 4), secondClosure.getTimeline().getClosedOnDate(), "the loan now settled on 04 September");
            assertDiscountRecognizedExactlyOnce(loanId, secondClosure, "after re-settling without a COB in between");

            // 08 September: undone again. This time COB runs while the loan is open and corrects the income down to
            // what the schedule has earned.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-09-08");
            wcLoanHelper.undoTransaction(loanId, secondSettlingRepaymentId);
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-09-09");
            wcLoanHelper.executeInlineWCCOB(loanId);
            final GetWorkingCapitalLoansLoanIdResponse reopened = wcLoanHelper.getLoanDetails(loanId);
            assertEquals("loanStatusType.active", reopened.getStatus().getCode(), "the second undo must reopen the loan");
            assertTrue(DISCOUNT.compareTo(netAmortized(loanId)) > 0,
                    "COB on the reopened loan must bring the recognized income back below the discount");

            // 10 September: repaid in full once more.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-09-10");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(100), "10 September 2026"));
            final GetWorkingCapitalLoansLoanIdResponse thirdClosure = wcLoanHelper.getLoanDetails(loanId);
            assertEquals("loanStatusType.closed.obligations.met", thirdClosure.getStatus().getCode(),
                    "repaying the missing 100 must close the loan a third time");
            assertEquals(LocalDate.of(2026, 9, 10), thirdClosure.getTimeline().getClosedOnDate(), "the loan now settled on 10 September");
            assertDiscountRecognizedExactlyOnce(loanId, thirdClosure, "after re-settling with a COB in between");
        });
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Long createDisbursedLoanWithDiscount(final String date) {
        final Long clientId = clientHelper.createClient(date);
        final Long productId = createAccrualWithDeferredRevenueAmortizationProduct();
        final Long loanId = wcLoanHelper.submitApplication(WorkingCapitalLoanRequestBuilders.submitApplication(clientId, productId,
                PRINCIPAL, WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT, date, date).discount(DISCOUNT));
        createdLoanIds.add(loanId);
        wcLoanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approveWithDiscount(date, PRINCIPAL, date, DISCOUNT));
        wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburseWithDiscount(date, PRINCIPAL, DISCOUNT));
        return loanId;
    }

    private Long createAccrualWithDeferredRevenueAmortizationProduct() {
        final String uniqueName = "WCL CobAmort " + UUID.randomUUID().toString().substring(0, 8);
        // Short names are four characters, so the space is small enough to collide on a database that accumulates
        // products across runs. Drawn from letters and digits rather than a UUID's hex, which is 26x the room.
        final String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        final Long productId = productHelper
                .createWorkingCapitalLoanProduct(
                        new WorkingCapitalLoanProductTestBuilder().withName(uniqueName).withShortName(uniqueShortName).withRepaymentEvery(1)
                                .withRepaymentFrequencyType("DAYS").withAllowAttributeOverrides(Map.of("discountDefault", Boolean.TRUE))
                                .withAccountingRule(AccountingRuleEnum.ACC_DEF_REV_AM)
                                .withFundSourceAccountId(fundSourceAccount.getAccountID().longValue())
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
                                .withDeferredIncomeLiabilityAccountId(deferredIncomeAccount.getAccountID().longValue()).build())
                .getResourceId();
        createdProductIds.add(productId);
        return productId;
    }

    /** Live discount fee amortizations, in posting order. */
    private List<GetWorkingCapitalLoanTransactionIdResponse> amortizations(final Long loanId) {
        return wcLoanHelper
                .getTransactions(loanId).stream().filter(txn -> txn.getType() != null
                        && DISCOUNT_FEE_AMORTIZATION_CODE.equals(txn.getType().getCode()) && !Boolean.TRUE.equals(txn.getReversed()))
                .toList();
    }

    /** Live discount fee amortizations net of the live amortization adjustments. */
    private BigDecimal netAmortized(final Long loanId) {
        return wcLoanHelper.getTransactions(loanId).stream()
                .filter(txn -> txn.getType() != null && !Boolean.TRUE.equals(txn.getReversed()) && txn.getTransactionAmount() != null)
                .map(txn -> switch (txn.getType().getCode()) {
                    case DISCOUNT_FEE_AMORTIZATION_CODE -> txn.getTransactionAmount();
                    case DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT_CODE -> txn.getTransactionAmount().negate();
                    default -> BigDecimal.ZERO;
                }).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void assertDiscountRecognizedExactlyOnce(final Long loanId, final GetWorkingCapitalLoansLoanIdResponse loan,
            final String when) {
        assertAmount(DISCOUNT, loan.getBalance().getRealizedIncomeFromDiscountFee(),
                "the realized income must be exactly the discount " + when);
        assertAmount(DISCOUNT, netAmortized(loanId), "the net live amortization must be exactly the discount " + when);
    }

    private List<JournalEntryTransactionItem> journalEntriesFor(final Long wcTransactionId) {
        final GetJournalEntriesTransactionIdResponse response = journalHelper.getJournalEntriesByTransactionId("WC" + wcTransactionId);
        return response == null || response.getPageItems() == null ? List.of() : response.getPageItems();
    }

    private static BigDecimal sum(final List<GetWorkingCapitalLoanTransactionIdResponse> transactions) {
        return transactions.stream().map(GetWorkingCapitalLoanTransactionIdResponse::getTransactionAmount)
                .filter(java.util.Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static String describe(final List<GetWorkingCapitalLoanTransactionIdResponse> transactions) {
        return transactions.stream().map(txn -> txn.getTransactionDate() + " " + txn.getTransactionAmount()).toList().toString();
    }

    private static void assertAmount(final BigDecimal expected, final BigDecimal actual, final String message) {
        assertNotNull(actual, message + " - amount was null");
        assertEquals(0, expected.compareTo(actual), message + " - expected: " + expected + " but was: " + actual);
    }
}
