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
