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
import static org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper.errorCodesOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetJournalEntriesTransactionIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsRequest.AccountingRuleEnum;
import org.apache.fineract.client.models.ProjectedAmortizationScheduleData;
import org.apache.fineract.client.models.ProjectedAmortizationSchedulePaymentData;
import org.apache.fineract.client.models.PutWorkingCapitalLoanProductsProductIdRequest;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessDateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignJournalEntryHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalLoanRequestBuilders;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * FLAT discount fee amortization on a working capital loan.
 *
 * <p>
 * Under FLAT the discount fee is earned as a fixed ratio of every repayment:
 * {@code ratio = (discountFee - adjustment) / (netDisbursement + discountFee - adjustment)} and
 * {@code amortization = ratio x repayment}. The reference example is 9000 disbursed with a 1000 fee, so a 10% ratio,
 * and a 50 repayment earning exactly 5. Every expected value in this class is hand-derived from that formula; none is
 * read back from the model.
 *
 * <p>
 * The loan uses the standard test shape: 100000 payment volume at 18% over 360 days, so the plan bills 50 a day and a
 * 10000 gross runs 200 periods. Period N falls on the disbursement date plus N days.
 */
public class FeignWorkingCapitalLoanFlatAmortizationTest extends FeignIntegrationTest {

    private static final String DISCOUNT_FEE_CODE = "loanTransactionType.discountFee";
    private static final String DISCOUNT_FEE_AMORTIZATION_CODE = "loanTransactionType.discountFeeAmortization";
    private static final String DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT_CODE = "loanTransactionType.discountFeeAmortizationAdjustment";
    private static final String FLAT = "FLAT";

    private static final BigDecimal NET_DISBURSEMENT = new BigDecimal("9000");
    private static final BigDecimal DISCOUNT = new BigDecimal("1000");
    private static final BigDecimal PERIOD_PAYMENT_RATE = new BigDecimal("18");
    private static final BigDecimal RAISED_RATE = new BigDecimal("25");
    /** (100000 x 18%) / 360 */
    private static final BigDecimal DAILY_PAYMENT = new BigDecimal("50");
    /** (100000 x 25%) / 360 = 69.444... */
    private static final BigDecimal RAISED_DAILY_PAYMENT = new BigDecimal("69.44");
    /** 10000 gross at 50 a day. */
    private static final int TERM = 200;

    private static final String DISBURSEMENT_DATE = "01 January 2026";
    private static final LocalDate DAY_2 = LocalDate.of(2026, 1, 2);
    private static final LocalDate DAY_3 = LocalDate.of(2026, 1, 3);
    private static final LocalDate DAY_4 = LocalDate.of(2026, 1, 4);
    private static final LocalDate DAY_5 = LocalDate.of(2026, 1, 5);

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

    @BeforeAll
    void setupHelpers() {
        final var feignClient = fineractClient();
        wcLoanHelper = new FeignWorkingCapitalLoanHelper(feignClient);
        clientHelper = new FeignClientHelper(feignClient);
        businessDateHelper = new FeignBusinessDateHelper(feignClient);
        journalHelper = new FeignJournalEntryHelper(feignClient);
        productHelper = new WorkingCapitalLoanProductHelper();

        final FeignAccountHelper accountHelper = new FeignAccountHelper(feignClient);
        fundSourceAccount = accountHelper.createLiabilityAccount("wcFlatFundSrc");
        loanPortfolioAccount = accountHelper.createAssetAccount("wcFlatLoanPort");
        transfersSuspenseAccount = accountHelper.createAssetAccount("wcFlatXferSusp");
        incomeFromDiscountFeeAccount = accountHelper.createIncomeAccount("wcFlatIncDisc");
        feesReceivableAccount = accountHelper.createAssetAccount("wcFlatFeesRcv");
        penaltiesReceivableAccount = accountHelper.createAssetAccount("wcFlatPenRcv");
        incomeFromFeeAccount = accountHelper.createIncomeAccount("wcFlatIncFee");
        incomeFromPenaltyAccount = accountHelper.createIncomeAccount("wcFlatIncPen");
        incomeFromRecoveryAccount = accountHelper.createIncomeAccount("wcFlatIncRec");
        writeOffAccount = accountHelper.createExpenseAccount("wcFlatWrtOff");
        overpaymentAccount = accountHelper.createLiabilityAccount("wcFlatOverpay");
        deferredIncomeAccount = accountHelper.createLiabilityAccount("wcFlatDefInc");
    }

    @AfterAll
    void cleanupEntities() {
        createdLoanIds.forEach(wcLoanHelper::cleanupLoan);
        createdLoanIds.clear();
    }

    // -----------------------------------------------------------------------
    // FLAT is accepted on the product and the loan inherits it
    // -----------------------------------------------------------------------
    @Test
    void flatProductIsAccepted_andLoanInheritsFlatAmortizationType() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long productId = createFlatProductAssertingAccepted();

            final GetWorkingCapitalLoanProductsProductIdResponse product = productHelper.retrieveWorkingCapitalLoanProductById(productId);
            assertNotNull(product.getAmortizationType(), "product must expose its amortization type");
            assertEquals(FLAT, product.getAmortizationType().getId(), "product must report the FLAT amortization type it was created with");

            final Long loanId = disburseLoan(productId, NET_DISBURSEMENT, DISCOUNT);
            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            assertNotNull(loan.getAmortizationType(), "loan must expose its amortization type");
            assertEquals(FLAT, loan.getAmortizationType().getId(), "loan must inherit the FLAT amortization type from its product");
        });
    }

    // -----------------------------------------------------------------------
    // projected schedule at disbursement follows the ratio
    // -----------------------------------------------------------------------
    @Test
    void projectedScheduleAtDisbursement_followsFlatRatio() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long productId = createFlatProductAssertingAccepted();
            final Long loanId = disburseLoan(productId, NET_DISBURSEMENT, DISCOUNT);

            final ProjectedAmortizationScheduleData schedule = wcLoanHelper.getAmortizationSchedule(loanId);
            assertEqualBigDecimal(DISCOUNT, schedule.getDiscountFeeAmount(), "schedule header discount fee");
            assertEqualBigDecimal(NET_DISBURSEMENT, schedule.getNetDisbursementAmount(), "schedule header net disbursement");
            assertEqualBigDecimal(DAILY_PAYMENT, schedule.getExpectedPaymentAmount(), "daily payment is (100000 x 18%) / 360");
            assertEquals(TERM, schedule.getOriginalPaymentNumber(), "10000 gross at 50 a day is a 200 payment term");
            assertNull(schedule.getEffectiveInterestRate(), "no EIR is solved for a FLAT loan" + render(schedule));

            final List<ProjectedAmortizationSchedulePaymentData> periods = periods(schedule);
            assertEquals(TERM, periods.size(), "the plan must have exactly 200 repayment periods" + render(schedule));
            for (final ProjectedAmortizationSchedulePaymentData period : periods) {
                assertEqualBigDecimal(DAILY_PAYMENT, period.getExpectedPaymentAmount(),
                        "period " + period.getPaymentNo() + " expected payment");
                // 10% x 50 = 5.00 on every period — the ratio does not depend on where in the term the payment falls
                assertEqualBigDecimal(new BigDecimal("5.00"), period.getExpectedAmortizationAmount(),
                        "period " + period.getPaymentNo() + " expected amortization is 10% of 50" + render(schedule));
                assertNull(period.getActualPaymentAmount(),
                        "nothing is known about period " + period.getPaymentNo() + " until its day has passed" + render(schedule));
            }
            // balance_n = 9000 - 50n + 5n = 9000 - 45n ; feeBalance_n = 1000 - 5n
            assertEqualBigDecimal(new BigDecimal("8955.00"), period(schedule, 1).getExpectedBalance(), "period 1 expected balance");
            assertEqualBigDecimal(new BigDecimal("995.00"), period(schedule, 1).getExpectedDiscountFeeBalance(),
                    "period 1 expected fee balance");
            assertEqualBigDecimal(new BigDecimal("4500.00"), period(schedule, 100).getExpectedBalance(), "period 100 expected balance");
            assertEqualBigDecimal(new BigDecimal("500.00"), period(schedule, 100).getExpectedDiscountFeeBalance(),
                    "period 100 expected fee balance");
            assertEqualBigDecimal(BigDecimal.ZERO, period(schedule, TERM).getExpectedBalance(), "period 200 expected balance");
            assertEqualBigDecimal(BigDecimal.ZERO, period(schedule, TERM).getExpectedDiscountFeeBalance(),
                    "period 200 expected fee balance");
            assertEqualBigDecimal(DISCOUNT, sumExpectedAmortization(schedule),
                    "expected amortization over the whole plan must sum to the 1000 fee");

            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            assertNull(loan.getDailyEir(), "no daily EIR for a FLAT loan");
            assertNull(loan.getCalculatedAnnualEir(), "no annual EIR for a FLAT loan");
        });
    }

    // -----------------------------------------------------------------------
    // a 50 repayment amortizes exactly 5
    // -----------------------------------------------------------------------
    @Test
    void repaymentOf50_amortizesExactly5() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long productId = createFlatProductAssertingAccepted();
            final Long loanId = disburseLoan(productId, NET_DISBURSEMENT, DISCOUNT);

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-02");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(DAILY_PAYMENT, "02 January 2026"));
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-03");
            wcLoanHelper.executeInlineWCCOB(loanId);

            final BigDecimal five = new BigDecimal("5.00");
            final ProjectedAmortizationScheduleData schedule = wcLoanHelper.getAmortizationSchedule(loanId);
            final ProjectedAmortizationSchedulePaymentData paid = periodOn(schedule, DAY_2);
            assertEqualBigDecimal(DAILY_PAYMENT, paid.getActualPaymentAmount(), "02 Jan actual payment" + render(schedule));
            assertEqualBigDecimal(five, paid.getActualAmortizationAmount(), "10% of 50 is 5" + render(schedule));
            assertEqualBigDecimal(new BigDecimal("8955.00"), paid.getActualBalance(), "9000 - 50 + 5" + render(schedule));
            assertEqualBigDecimal(new BigDecimal("995.00"), paid.getActualDiscountFeeBalance(), "1000 - 5" + render(schedule));

            final List<GetWorkingCapitalLoanTransactionIdResponse> amortizations = amortizationTransactions(loanId);
            assertEquals(1, amortizations.size(), "exactly one amortization transaction after one repayment + COB");
            assertEqualBigDecimal(five, amortizations.getFirst().getTransactionAmount(), "amortization transaction amount");
            assertEquals(DAY_2, amortizations.getFirst().getTransactionDate(),
                    "the COB run on the next business date posts the amortization on the repayment's day");

            final List<JournalEntryTransactionItem> entries = journalEntriesOf(amortizations.getFirst().getId());
            assertEquals(2, entries.size(), "one debit + one credit");
            assertJournalEntry(entries, "DEBIT", deferredIncomeAccount, five);
            assertJournalEntry(entries, "CREDIT", incomeFromDiscountFeeAccount, five);

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-04");
            wcLoanHelper.executeInlineWCCOB(loanId);
            final List<GetWorkingCapitalLoanTransactionIdResponse> afterIdleCob = amortizationTransactions(loanId);
            assertEquals(1, afterIdleCob.size(), "a COB with no new payment must not amortize anything more");
            assertEqualBigDecimal(five, afterIdleCob.getFirst().getTransactionAmount(), "the single amortization is unchanged");
        });
    }

    // -----------------------------------------------------------------------
    // non-round repayments, cumulative rounded once
    // -----------------------------------------------------------------------
    @Test
    void nonRoundRepayments_roundTheCumulativeAmortizationOnce() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long productId = createFlatProductAssertingAccepted();
            final Long loanId = disburseLoan(productId, NET_DISBURSEMENT, DISCOUNT);

            // 10% x 33.33 = 3.333 -> 3.33
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-02");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(new BigDecimal("33.33"), "02 January 2026"));
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-03");
            wcLoanHelper.executeInlineWCCOB(loanId);
            final List<GetWorkingCapitalLoanTransactionIdResponse> afterFirst = amortizationTransactions(loanId);
            assertEquals(1, afterFirst.size(), "one amortization after the first repayment");
            assertEqualBigDecimal(new BigDecimal("3.33"), afterFirst.getFirst().getTransactionAmount(), "10% of 33.33");

            // cumulative 10% x 99.99 = 9.999 -> 10.00 ; second transaction = 10.00 - 3.33
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(new BigDecimal("66.66"), "03 January 2026"));
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-04");
            wcLoanHelper.executeInlineWCCOB(loanId);
            final List<GetWorkingCapitalLoanTransactionIdResponse> afterSecond = amortizationTransactions(loanId);
            assertEquals(2, afterSecond.size(), "two amortizations after two repayments");
            assertEqualBigDecimal(new BigDecimal("6.67"), afterSecond.get(1).getTransactionAmount(),
                    "cumulative 9.999 rounds to 10.00, less the 3.33 already posted");
            assertEqualBigDecimal(new BigDecimal("10.00"), sumAmounts(afterSecond), "10% of 99.99 rounded once");
            assertTrue(adjustmentTransactions(loanId).isEmpty(), "rounding must never produce an adjustment transaction");

            final ProjectedAmortizationScheduleData schedule = wcLoanHelper.getAmortizationSchedule(loanId);
            assertEqualBigDecimal(new BigDecimal("3.33"), periodOn(schedule, DAY_2).getActualAmortizationAmount(),
                    "02 Jan row earns 3.33" + render(schedule));
            assertEqualBigDecimal(new BigDecimal("6.67"), periodOn(schedule, DAY_3).getActualAmortizationAmount(),
                    "03 Jan row earns 6.67" + render(schedule));
            assertEqualBigDecimal(new BigDecimal("990.00"), periodOn(schedule, DAY_3).getActualDiscountFeeBalance(),
                    "1000 - 10.00 unearned after the second repayment" + render(schedule));
        });
    }

    // -----------------------------------------------------------------------
    // full payoff in uneven instalments closes to exactly the fee
    // -----------------------------------------------------------------------
    @Test
    void fullPayoffInUnevenInstalments_amortizesExactlyTheFee() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long productId = createFlatProductAssertingAccepted();
            final Long loanId = disburseLoan(productId, NET_DISBURSEMENT, DISCOUNT);

            // 10% x 3333.33 = 333.333 -> 333.33
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-02");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(new BigDecimal("3333.33"), "02 January 2026"));
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-03");
            wcLoanHelper.executeInlineWCCOB(loanId);
            // cumulative 10% x 6666.66 = 666.666 -> 666.67 ; 666.67 - 333.33 = 333.34
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(new BigDecimal("3333.33"), "03 January 2026"));
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-04");
            wcLoanHelper.executeInlineWCCOB(loanId);
            // cumulative 10% x 10000 = 1000.00 ; 1000.00 - 666.67 = 333.33 ; the loan is paid off
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(new BigDecimal("3333.34"), "04 January 2026"));
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-05");
            wcLoanHelper.executeInlineWCCOB(loanId);

            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            assertEquals(Boolean.TRUE, loan.getStatus().getClosedObligationsMet(), "9000 + 1000 paid in full closes the loan");

            final List<GetWorkingCapitalLoanTransactionIdResponse> amortizations = amortizationTransactions(loanId);
            assertEquals(3, amortizations.size(), "one amortization per repayment");
            assertEqualBigDecimal(new BigDecimal("333.33"), amortizations.get(0).getTransactionAmount(), "first amortization");
            assertEqualBigDecimal(new BigDecimal("333.34"), amortizations.get(1).getTransactionAmount(), "second amortization");
            assertEqualBigDecimal(new BigDecimal("333.33"), amortizations.get(2).getTransactionAmount(), "closing amortization");
            assertEqualBigDecimal(DISCOUNT, sumAmounts(amortizations), "fully paid: aggregated amortization equals the discount fee");
            assertTrue(adjustmentTransactions(loanId).isEmpty(), "no adjustment is needed when the ratio sums exactly to the fee");

            final ProjectedAmortizationScheduleData schedule = wcLoanHelper.getAmortizationSchedule(loanId);
            assertEqualBigDecimal(DISCOUNT, sumActualAmortization(schedule),
                    "schedule actual amortization must sum to the 1000 fee" + render(schedule));
            final ProjectedAmortizationSchedulePaymentData closing = periodOn(schedule, DAY_4);
            assertEqualBigDecimal(BigDecimal.ZERO, closing.getActualDiscountFeeBalance(), "nothing left unearned" + render(schedule));
            assertEqualBigDecimal(BigDecimal.ZERO, closing.getActualBalance(), "nothing left owed" + render(schedule));
        });
    }

    // -----------------------------------------------------------------------
    // a discount fee adjustment moves the ratio and restates
    // -----------------------------------------------------------------------
    @Test
    void discountFeeAdjustment_movesTheRatioAndRestatesEarnedAmortization() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long productId = createFlatProductAssertingAccepted();
            final BigDecimal disbursed = new BigDecimal("9500");
            final Long loanId = disburseLoan(productId, disbursed, DISCOUNT);
            final Long discountFeeTransactionId = filterByType(wcLoanHelper.getTransactions(loanId), DISCOUNT_FEE_CODE).getFirst().getId();

            // 10500 gross at 50 a day is 210 periods; 1000/10500 x 50 = 4.7619 -> 4.76 a period does not divide evenly,
            // so the plan must still close to zero and earn exactly the fee, the residue settling on the last period
            final ProjectedAmortizationScheduleData plan = wcLoanHelper.getAmortizationSchedule(loanId);
            assertEquals(210, periods(plan).size(), "a non-terminating ratio must not grow catch-up periods" + render(plan));
            assertEqualBigDecimal(DISCOUNT, sumExpectedAmortization(plan), "expected amortization sums to the fee" + render(plan));
            assertEqualBigDecimal(BigDecimal.ZERO, period(plan, 210).getExpectedBalance(), "period 210 expected balance" + render(plan));
            assertEqualBigDecimal(BigDecimal.ZERO, period(plan, 210).getExpectedDiscountFeeBalance(),
                    "period 210 expected fee balance" + render(plan));

            // ratio 1000 / (9500 + 1000) = 0.095238... ; x 50 = 4.7619 -> 4.76
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-02");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(DAILY_PAYMENT, "02 January 2026"));
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-03");
            wcLoanHelper.executeInlineWCCOB(loanId);
            final List<GetWorkingCapitalLoanTransactionIdResponse> beforeAdjustment = amortizationTransactions(loanId);
            assertEquals(1, beforeAdjustment.size(), "one amortization before the adjustment");
            assertEqualBigDecimal(new BigDecimal("4.76"), beforeAdjustment.getFirst().getTransactionAmount(), "1000/10500 x 50");

            // adjustment 500: payable fee 500, initial balance 9500 + 500 = 10000, ratio 5% ; restated 2.50, posted
            // 4.76. The schedule is restated at once; the ledger catches up on the next COB, which processes the day
            // before the business date and so sees the adjustment only from 04 Jan (as the EIR scenario UC1 of
            // WorkingCapitalDiscountAmortizationAdjustment.feature pins).
            wcLoanHelper.makeDiscountFeeAdjustment(loanId, WorkingCapitalLoanRequestBuilders.discountFeeAdjustment(new BigDecimal("500"),
                    "03 January 2026", discountFeeTransactionId));

            final ProjectedAmortizationScheduleData restated = wcLoanHelper.getAmortizationSchedule(loanId);
            assertEqualBigDecimal(new BigDecimal("500.00"), restated.getDiscountFeeAmount(),
                    "schedule carries the payable fee net of the adjustment" + render(restated));
            assertEqualBigDecimal(new BigDecimal("2.50"), periodOn(restated, DAY_2).getActualAmortizationAmount(),
                    "the 02 Jan repayment is restated at 5% x 50" + render(restated));
            assertEqualBigDecimal(DAILY_PAYMENT, periodOn(restated, DAY_5).getExpectedPaymentAmount(),
                    "future periods still bill 50" + render(restated));
            assertEqualBigDecimal(new BigDecimal("2.50"), periodOn(restated, DAY_5).getExpectedAmortizationAmount(),
                    "future periods earn 5% x 50" + render(restated));

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-04");
            wcLoanHelper.executeInlineWCCOB(loanId);
            final List<GetWorkingCapitalLoanTransactionIdResponse> adjustments = adjustmentTransactions(loanId);
            assertEquals(1, adjustments.size(), "exactly one amortization adjustment once the COB reaches the adjustment date");
            assertEqualBigDecimal(new BigDecimal("2.26"), adjustments.getFirst().getTransactionAmount(), "4.76 posted - 2.50 restated");
            assertEquals(1, amortizationTransactions(loanId).size(), "the restatement posts an adjustment, not a new amortization");

            // 5% x 50 = 2.50 ; net earned 4.76 - 2.26 + 2.50 = 5.00
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(DAILY_PAYMENT, "04 January 2026"));
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-05");
            wcLoanHelper.executeInlineWCCOB(loanId);
            final List<GetWorkingCapitalLoanTransactionIdResponse> afterSecondRepayment = amortizationTransactions(loanId);
            assertEquals(2, afterSecondRepayment.size(), "two amortizations after two repayments");
            assertEqualBigDecimal(new BigDecimal("2.50"), afterSecondRepayment.get(1).getTransactionAmount(), "5% x 50");
            assertEqualBigDecimal(new BigDecimal("5.00"), netAmortization(loanId), "4.76 - 2.26 + 2.50");

            // gross owed 9500 + 500 = 10000, 100 paid, 9900 left ; 5% x 10000 = 500 total, 5.00 net so far -> 495.00
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(new BigDecimal("9900"), "05 January 2026"));
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-06");
            wcLoanHelper.executeInlineWCCOB(loanId);

            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            assertEquals(Boolean.TRUE, loan.getStatus().getClosedObligationsMet(), "9500 + 500 paid in full closes the loan");
            final List<GetWorkingCapitalLoanTransactionIdResponse> amortizations = amortizationTransactions(loanId);
            assertEquals(3, amortizations.size(), "one amortization per repayment");
            assertEqualBigDecimal(new BigDecimal("495.00"), amortizations.get(2).getTransactionAmount(), "closing amortization");
            assertEquals(1, adjustmentTransactions(loanId).size(), "the payoff must not need a second adjustment");
            assertEqualBigDecimal(new BigDecimal("500.00"), netAmortization(loanId),
                    "fully paid: net amortization equals the discount fee less the adjustment");
        });
    }

    // -----------------------------------------------------------------------
    // adjusting the whole fee away takes the ratio to zero and reverses what was earned
    // -----------------------------------------------------------------------
    @Test
    void discountFeeAdjustmentOfTheWholeFee_earnsNothingAndReversesWhatWasEarned() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long productId = createFlatProductAssertingAccepted();
            final Long loanId = disburseLoan(productId, NET_DISBURSEMENT, DISCOUNT);
            final Long discountFeeTransactionId = filterByType(wcLoanHelper.getTransactions(loanId), DISCOUNT_FEE_CODE).getFirst().getId();

            // 10% x 50 = 5.00
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-02");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(DAILY_PAYMENT, "02 January 2026"));
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-03");
            wcLoanHelper.executeInlineWCCOB(loanId);
            assertEqualBigDecimal(new BigDecimal("5.00"), netAmortization(loanId), "10% x 50 earned before the adjustment");

            // adjustment 1000: payable fee 0, gross 9000 at 50 a day is 180 periods, ratio 0 / 9000 = 0
            wcLoanHelper.makeDiscountFeeAdjustment(loanId,
                    WorkingCapitalLoanRequestBuilders.discountFeeAdjustment(DISCOUNT, "03 January 2026", discountFeeTransactionId));

            final ProjectedAmortizationScheduleData restated = wcLoanHelper.getAmortizationSchedule(loanId);
            assertEqualBigDecimal(new BigDecimal("0.00"), restated.getDiscountFeeAmount(), "no fee is left to earn" + render(restated));
            assertEquals(180, periods(restated).size(), "9000 gross at 50 a day" + render(restated));
            assertEqualBigDecimal(new BigDecimal("0.00"), periodOn(restated, DAY_2).getActualAmortizationAmount(),
                    "the 02 Jan repayment is restated at 0% x 50" + render(restated));
            assertEqualBigDecimal(DAILY_PAYMENT, periodOn(restated, DAY_5).getExpectedPaymentAmount(),
                    "future periods still bill 50" + render(restated));
            assertEqualBigDecimal(new BigDecimal("0.00"), periodOn(restated, DAY_5).getExpectedAmortizationAmount(),
                    "future periods earn nothing" + render(restated));
            assertEqualBigDecimal(BigDecimal.ZERO, sumExpectedAmortization(restated),
                    "nothing is left to earn anywhere" + render(restated));

            // the next COB reverses the 5.00 already posted
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-04");
            wcLoanHelper.executeInlineWCCOB(loanId);
            final List<GetWorkingCapitalLoanTransactionIdResponse> adjustments = adjustmentTransactions(loanId);
            assertEquals(1, adjustments.size(), "exactly one amortization adjustment");
            assertEqualBigDecimal(new BigDecimal("5.00"), adjustments.getFirst().getTransactionAmount(), "5.00 posted - 0.00 restated");
            assertEqualBigDecimal(BigDecimal.ZERO, netAmortization(loanId), "nothing stays earned");
            final List<JournalEntryTransactionItem> entries = journalEntriesOf(adjustments.getFirst().getId());
            assertJournalEntry(entries, "DEBIT", incomeFromDiscountFeeAccount, new BigDecimal("5.00"));
            assertJournalEntry(entries, "CREDIT", deferredIncomeAccount, new BigDecimal("5.00"));

            // 0% x 50 = 0: a zero amortization is not posted at all
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(DAILY_PAYMENT, "04 January 2026"));
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-05");
            wcLoanHelper.executeInlineWCCOB(loanId);
            assertEquals(1, amortizationTransactions(loanId).size(), "no amortization is posted for a repayment that earns nothing");
            assertEqualBigDecimal(BigDecimal.ZERO, netAmortization(loanId), "still nothing earned");

            // 9000 owed, 100 paid, 8900 closes the loan with the fee never earned
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(new BigDecimal("8900"), "05 January 2026"));
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-06");
            wcLoanHelper.executeInlineWCCOB(loanId);

            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            assertEquals(Boolean.TRUE, loan.getStatus().getClosedObligationsMet(), "9000 paid in full closes the loan");
            assertEquals(1, amortizationTransactions(loanId).size(), "the payoff earns nothing either");
            assertEquals(1, adjustmentTransactions(loanId).size(), "the payoff must not need a second adjustment");
            assertEqualBigDecimal(BigDecimal.ZERO, netAmortization(loanId), "fully paid: net amortization equals the fee, which is zero");
        });
    }

    // -----------------------------------------------------------------------
    // undoing a discount fee adjustment restores the ratio
    // -----------------------------------------------------------------------
    @Test
    void undoOfDiscountFeeAdjustment_restoresTheRatio() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long productId = createFlatProductAssertingAccepted();
            final Long loanId = disburseLoan(productId, new BigDecimal("9500"), DISCOUNT);
            final Long discountFeeTransactionId = filterByType(wcLoanHelper.getTransactions(loanId), DISCOUNT_FEE_CODE).getFirst().getId();

            // 1000 / 10500 x 50 = 4.7619 -> 4.76
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-02");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(DAILY_PAYMENT, "02 January 2026"));
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-03");
            wcLoanHelper.executeInlineWCCOB(loanId);
            assertEqualBigDecimal(new BigDecimal("4.76"), amortizationTransactions(loanId).getFirst().getTransactionAmount(),
                    "1000/10500 x 50");

            // adjustment 500 -> 5 % -> restated 2.50 ; the next-day COB posts the 2.26 difference back
            final Long adjustmentId = wcLoanHelper.makeDiscountFeeAdjustment(loanId, WorkingCapitalLoanRequestBuilders
                    .discountFeeAdjustment(new BigDecimal("500"), "03 January 2026", discountFeeTransactionId));
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-04");
            wcLoanHelper.executeInlineWCCOB(loanId);
            assertEqualBigDecimal(new BigDecimal("2.26"), adjustmentTransactions(loanId).getFirst().getTransactionAmount(), "4.76 - 2.50");
            assertEqualBigDecimal(new BigDecimal("2.50"), netAmortization(loanId), "net earned at 5 %");

            // the undo restores the 1000 fee and the 1000/10500 ratio; the amortization adjustment it triggered is
            // reversed with it, so the ledger is back at 4.76 at once
            wcLoanHelper.undoTransaction(loanId, adjustmentId, WorkingCapitalLoanRequestBuilders.undoTransaction());
            assertTrue(adjustmentTransactions(loanId).isEmpty(), "the linked amortization adjustment is reversed with the undo");
            assertEquals(1,
                    wcLoanHelper.getTransactions(loanId).stream()
                            .filter(txn -> DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT_CODE.equals(txn.getType().getCode())
                                    && Boolean.TRUE.equals(txn.getReversed()))
                            .count(),
                    "the reversed amortization adjustment stays on the books");
            assertEqualBigDecimal(new BigDecimal("4.76"), netAmortization(loanId), "net earned is back at 1000/10500 x 50");
            final ProjectedAmortizationScheduleData restored = wcLoanHelper.getAmortizationSchedule(loanId);
            assertEqualBigDecimal(DISCOUNT, restored.getDiscountFeeAmount(), "the schedule carries the full fee again" + render(restored));
            assertEqualBigDecimal(new BigDecimal("4.76"), periodOn(restored, DAY_2).getActualAmortizationAmount(),
                    "the 02 Jan repayment earns 1000/10500 x 50 again" + render(restored));
            assertEqualBigDecimal(new BigDecimal("4.76"), periodOn(restored, DAY_5).getExpectedAmortizationAmount(),
                    "future periods earn at the restored ratio" + render(restored));

            // the schedule and the ledger already agree, so the next COB has nothing to add
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-05");
            wcLoanHelper.executeInlineWCCOB(loanId);
            final List<GetWorkingCapitalLoanTransactionIdResponse> amortizations = amortizationTransactions(loanId);
            assertEquals(1, amortizations.size(), "no catch-up is needed once the adjustment and its reversal cancel out" + amortizations);
            assertTrue(adjustmentTransactions(loanId).isEmpty(), "and no new adjustment either");
            assertEqualBigDecimal(new BigDecimal("4.76"), netAmortization(loanId), "net earned stays at 1000/10500 x 50");
        });
    }

    // -----------------------------------------------------------------------
    // switching the product to FLAT leaves existing loans on EIR
    // -----------------------------------------------------------------------
    @Test
    void switchingTheProductToFlat_leavesExistingLoansOnEir() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long productId = createProductAssertingAccepted("EIR");
            final Long eirLoanId = disburseLoan(productId, NET_DISBURSEMENT, DISCOUNT);
            final ProjectedAmortizationScheduleData before = wcLoanHelper.getAmortizationSchedule(eirLoanId);
            assertNotNull(before.getEffectiveInterestRate(), "an EIR loan solves a rate" + render(before));

            productHelper.updateWorkingCapitalLoanProductById(productId, new PutWorkingCapitalLoanProductsProductIdRequest()
                    .amortizationType(PutWorkingCapitalLoanProductsProductIdRequest.AmortizationTypeEnum.FLAT).locale("en_US"));
            assertEquals(FLAT, productHelper.retrieveWorkingCapitalLoanProductById(productId).getAmortizationType().getId(),
                    "the product now creates FLAT loans");

            assertEquals("EIR", wcLoanHelper.getLoanDetails(eirLoanId).getAmortizationType().getId(),
                    "an existing loan keeps the type it was created with");
            final ProjectedAmortizationScheduleData after = wcLoanHelper.getAmortizationSchedule(eirLoanId);
            assertEqualBigDecimal(before.getEffectiveInterestRate(), after.getEffectiveInterestRate(), "the EIR loan keeps its rate");
            assertEqualBigDecimal(period(before, 1).getExpectedAmortizationAmount(), period(after, 1).getExpectedAmortizationAmount(),
                    "the EIR schedule is unchanged" + render(after));

            final Long flatLoanId = disburseLoan(productId, NET_DISBURSEMENT, DISCOUNT);
            assertEquals(FLAT, wcLoanHelper.getLoanDetails(flatLoanId).getAmortizationType().getId(), "a new loan takes the new type");
            final ProjectedAmortizationScheduleData flat = wcLoanHelper.getAmortizationSchedule(flatLoanId);
            assertNull(flat.getEffectiveInterestRate(), "no EIR is solved for the FLAT loan" + render(flat));
            assertEqualBigDecimal(new BigDecimal("5.00"), period(flat, 1).getExpectedAmortizationAmount(), "10% x 50" + render(flat));
        });
    }

    // -----------------------------------------------------------------------
    // undoing the repayment reverses the FLAT amortization
    // -----------------------------------------------------------------------
    @Test
    void undoRepayment_reversesFlatAmortization() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long productId = createFlatProductAssertingAccepted();
            final Long loanId = disburseLoan(productId, NET_DISBURSEMENT, DISCOUNT);

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-02");
            final Long repaymentId = wcLoanHelper.makeRepayment(loanId,
                    WorkingCapitalLoanRequestBuilders.repayment(DAILY_PAYMENT, "02 January 2026"));
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-03");
            wcLoanHelper.executeInlineWCCOB(loanId);
            final List<GetWorkingCapitalLoanTransactionIdResponse> amortizations = amortizationTransactions(loanId);
            assertEquals(1, amortizations.size(), "one amortization after the repayment");
            assertEqualBigDecimal(new BigDecimal("5.00"), amortizations.getFirst().getTransactionAmount(), "10% x 50");

            wcLoanHelper.undoTransaction(loanId, repaymentId, WorkingCapitalLoanRequestBuilders.undoTransaction());
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-04");
            wcLoanHelper.executeInlineWCCOB(loanId);

            final List<GetWorkingCapitalLoanTransactionIdResponse> adjustments = adjustmentTransactions(loanId);
            assertEquals(1, adjustments.size(), "exactly one adjustment reverses the amortization of the undone repayment");
            assertEqualBigDecimal(new BigDecimal("5.00"), adjustments.getFirst().getTransactionAmount(), "the whole 5.00 comes back");
            assertEqualBigDecimal(BigDecimal.ZERO, netAmortization(loanId), "nothing is earned once the only repayment is undone");

            final ProjectedAmortizationScheduleData schedule = wcLoanHelper.getAmortizationSchedule(loanId);
            final ProjectedAmortizationSchedulePaymentData undone = periodOn(schedule, DAY_2);
            assertEqualBigDecimal(BigDecimal.ZERO, undone.getActualPaymentAmount(),
                    "02 Jan elapsed with nothing collected once the repayment is undone" + render(schedule));
            assertEqualBigDecimal(BigDecimal.ZERO, undone.getActualAmortizationAmount(), "10% x 0 = 0" + render(schedule));
            assertEqualBigDecimal(DISCOUNT, undone.getActualDiscountFeeBalance(), "the whole fee is unearned again" + render(schedule));
        });
    }

    // -----------------------------------------------------------------------
    // a rate change resizes the payment but keeps the ratio
    // -----------------------------------------------------------------------
    @Test
    void rateChange_resizesPaymentsButKeepsFlatRatio() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long productId = createFlatProductAssertingAccepted();
            final Long loanId = disburseLoan(productId, NET_DISBURSEMENT, DISCOUNT);

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-02");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(DAILY_PAYMENT, "02 January 2026"));
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-03");
            wcLoanHelper.executeInlineWCCOB(loanId);
            assertEqualBigDecimal(new BigDecimal("5.00"), amortizationTransactions(loanId).getFirst().getTransactionAmount(), "10% x 50");

            // 25%: (100000 x 25%) / 360 = 69.44 a day ; 10% x 69.44 = 6.944 a day, reported as the movement of the
            // rounded
            // running total: 5.00 + 6.944 = 11.944 -> 11.94 ; 18.888 -> 18.89 ; 25.832 -> 25.83
            wcLoanHelper.updateRate(loanId, WorkingCapitalLoanRequestBuilders.updateRate(RAISED_RATE, "03 January 2026"));

            final ProjectedAmortizationScheduleData rerated = wcLoanHelper.getAmortizationSchedule(loanId);
            assertEqualBigDecimal(DAILY_PAYMENT, periodOn(rerated, DAY_2).getExpectedPaymentAmount(),
                    "the period before the change keeps the original payment" + render(rerated));
            assertEqualBigDecimal(new BigDecimal("5.00"), periodOn(rerated, DAY_2).getExpectedAmortizationAmount(),
                    "the period before the change keeps 10% x 50" + render(rerated));
            final Map<LocalDate, BigDecimal> reportedFee = Map.of(DAY_3, new BigDecimal("6.94"), DAY_4, new BigDecimal("6.95"), DAY_5,
                    new BigDecimal("6.94"));
            for (final LocalDate day : List.of(DAY_3, DAY_4, DAY_5)) {
                assertEqualBigDecimal(RAISED_DAILY_PAYMENT, periodOn(rerated, day).getExpectedPaymentAmount(),
                        day + " bills the raised daily payment" + render(rerated));
                assertEqualBigDecimal(reportedFee.get(day), periodOn(rerated, day).getExpectedAmortizationAmount(),
                        day + " earns 10% of 69.44 — the ratio does not move with the rate" + render(rerated));
            }
            assertEqualBigDecimal(new BigDecimal("974.17"), periodOn(rerated, DAY_5).getExpectedDiscountFeeBalance(),
                    "1000 - 25.83: the three raised days earn 10% of 208.32 between them" + render(rerated));

            // cumulative 10% x 119.44 = 11.944 -> 11.94 ; 11.94 - 5.00 = 6.94
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(RAISED_DAILY_PAYMENT, "03 January 2026"));
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-04");
            wcLoanHelper.executeInlineWCCOB(loanId);
            final List<GetWorkingCapitalLoanTransactionIdResponse> amortizations = amortizationTransactions(loanId);
            assertEquals(2, amortizations.size(), "two amortizations after two repayments");
            assertEqualBigDecimal(new BigDecimal("6.94"), amortizations.get(1).getTransactionAmount(), "10% x 69.44 at the raised rate");
        });
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * A rejected FLAT product is the behaviour under test, not a broken setup, so it is reported as an assertion
     * failure carrying the validator's error codes.
     */
    private Long createFlatProductAssertingAccepted() {
        return createProductAssertingAccepted(FLAT);
    }

    private Long createProductAssertingAccepted(final String amortizationType) {
        final PostWorkingCapitalLoanProductsRequest request = productBuilder(amortizationType).build();
        final Long productId;
        try {
            productId = productHelper.createWorkingCapitalLoanProduct(request).getResourceId();
        } catch (final CallFailedRuntimeException e) {
            return fail("a working capital product with amortizationType " + amortizationType
                    + " must be accepted, but creation was rejected with HTTP " + e.getStatus() + " and error codes " + errorCodesOf(e), e);
        }
        assertNotNull(productId, "product creation must return the new product id");
        return productId;
    }

    private WorkingCapitalLoanProductTestBuilder productBuilder(final String amortizationType) {
        return new WorkingCapitalLoanProductTestBuilder()
                .withName("WCL " + amortizationType + " " + UUID.randomUUID().toString().substring(0, 8))
                .withShortName(UUID.randomUUID().toString().replace("-", "").substring(0, 4)).withAmortizationType(amortizationType)
                .withAllowAttributeOverrides(Map.of("discountDefault", Boolean.TRUE)).withAccountingRule(AccountingRuleEnum.ACC_DEF_REV_AM)
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
                .withDeferredIncomeLiabilityAccountId(deferredIncomeAccount.getAccountID().longValue());
    }

    private Long disburseLoan(final Long productId, final BigDecimal principal, final BigDecimal discount) {
        final Long clientId = clientHelper.createClient(DISBURSEMENT_DATE);
        final Long loanId = wcLoanHelper.submitApplication(WorkingCapitalLoanRequestBuilders
                .submitApplication(clientId, productId, principal, PERIOD_PAYMENT_RATE, DISBURSEMENT_DATE, DISBURSEMENT_DATE)
                .discount(discount));
        createdLoanIds.add(loanId);
        wcLoanHelper.approve(loanId,
                WorkingCapitalLoanRequestBuilders.approveWithDiscount(DISBURSEMENT_DATE, principal, DISBURSEMENT_DATE, discount));
        wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburseWithDiscount(DISBURSEMENT_DATE, principal, discount));
        return loanId;
    }

    private List<GetWorkingCapitalLoanTransactionIdResponse> amortizationTransactions(final Long loanId) {
        return filterByType(wcLoanHelper.getTransactions(loanId), DISCOUNT_FEE_AMORTIZATION_CODE);
    }

    private List<GetWorkingCapitalLoanTransactionIdResponse> adjustmentTransactions(final Long loanId) {
        return filterByType(wcLoanHelper.getTransactions(loanId), DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT_CODE);
    }

    private BigDecimal netAmortization(final Long loanId) {
        return sumAmounts(amortizationTransactions(loanId)).subtract(sumAmounts(adjustmentTransactions(loanId)));
    }

    /** Non-reversed transactions of the given type, oldest first. */
    private static List<GetWorkingCapitalLoanTransactionIdResponse> filterByType(
            final List<GetWorkingCapitalLoanTransactionIdResponse> transactions, final String typeCode) {
        return transactions.stream()
                .filter(txn -> txn.getType() != null && typeCode.equals(txn.getType().getCode()) && !Boolean.TRUE.equals(txn.getReversed()))
                .sorted(Comparator.comparing(GetWorkingCapitalLoanTransactionIdResponse::getId)).toList();
    }

    private static BigDecimal sumAmounts(final List<GetWorkingCapitalLoanTransactionIdResponse> transactions) {
        return transactions.stream()
                .map(txn -> Objects.requireNonNull(txn.getTransactionAmount(),
                        () -> "transactionAmount must not be null for transaction " + txn.getId()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<JournalEntryTransactionItem> journalEntriesOf(final Long wcTransactionId) {
        final GetJournalEntriesTransactionIdResponse response = journalHelper.getJournalEntriesByTransactionId("WC" + wcTransactionId);
        return response != null && response.getPageItems() != null ? response.getPageItems() : List.of();
    }

    private static void assertJournalEntry(final List<JournalEntryTransactionItem> entries, final String expectedType,
            final Account expectedAccount, final BigDecimal expectedAmount) {
        final boolean found = entries.stream()
                .anyMatch(entry -> expectedType.equals(entry.getEntryType().getValue())
                        && expectedAccount.getAccountID().longValue() == entry.getGlAccountId()
                        && expectedAmount.compareTo(BigDecimal.valueOf(entry.getAmount())) == 0);
        assertTrue(found, "expected a " + expectedType + " of " + expectedAmount + " on GL account " + expectedAccount.getAccountID()
                + " but entries were: " + entries);
    }

    /** Repayment periods only — the disbursement row (#0) carries a negative amount and the opening balance. */
    private static List<ProjectedAmortizationSchedulePaymentData> periods(final ProjectedAmortizationScheduleData schedule) {
        return schedule.getPayments().stream().filter(payment -> payment.getPaymentNo() != null && payment.getPaymentNo() > 0).toList();
    }

    private static ProjectedAmortizationSchedulePaymentData period(final ProjectedAmortizationScheduleData schedule, final int periodNo) {
        return periods(schedule).stream().filter(payment -> payment.getPaymentNo() == periodNo).findFirst()
                .orElseThrow(() -> new AssertionError("no period " + periodNo + render(schedule)));
    }

    private static ProjectedAmortizationSchedulePaymentData periodOn(final ProjectedAmortizationScheduleData schedule,
            final LocalDate date) {
        return periods(schedule).stream().filter(payment -> date.equals(payment.getPaymentDate())).findFirst()
                .orElseThrow(() -> new AssertionError("no period on " + date + render(schedule)));
    }

    private static BigDecimal sumExpectedAmortization(final ProjectedAmortizationScheduleData schedule) {
        return periods(schedule).stream().map(ProjectedAmortizationSchedulePaymentData::getExpectedAmortizationAmount)
                .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal sumActualAmortization(final ProjectedAmortizationScheduleData schedule) {
        return periods(schedule).stream().map(ProjectedAmortizationSchedulePaymentData::getActualAmortizationAmount)
                .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static String render(final ProjectedAmortizationScheduleData schedule) {
        final StringBuilder out = new StringBuilder("\nschedule (fee=").append(schedule.getDiscountFeeAmount()).append(", eir=")
                .append(schedule.getEffectiveInterestRate()).append("):\n");
        for (final ProjectedAmortizationSchedulePaymentData payment : schedule.getPayments()) {
            out.append("  #").append(payment.getPaymentNo()).append(' ').append(payment.getPaymentDate()).append(" expPay=")
                    .append(payment.getExpectedPaymentAmount()).append(" expBal=").append(payment.getExpectedBalance()).append(" expAmort=")
                    .append(payment.getExpectedAmortizationAmount()).append(" expFeeBal=").append(payment.getExpectedDiscountFeeBalance())
                    .append(" actPay=").append(payment.getActualPaymentAmount()).append(" actBal=").append(payment.getActualBalance())
                    .append(" actAmort=").append(payment.getActualAmortizationAmount()).append(" actFeeBal=")
                    .append(payment.getActualDiscountFeeBalance()).append('\n');
        }
        return out.toString();
    }
}
