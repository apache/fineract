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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetLoansLoanIdLoanChargeData;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdSummary;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.params.provider.Arguments;

/**
 * Shared setup and assertions for the transaction adjustment tests.
 *
 * <p>
 * Most scenarios compare the adjusted loan with a "twin" loan on which the replacement transaction was posted directly.
 * Adjusting a transaction must leave the loan in the same state as if the new amount and date had been booked in the
 * first place, so the twin removes the need to hand-calculate interest and allocations for every loan variant.
 */
public abstract class FeignLoanTransactionAdjustmentTestBase extends FeignLoanTestBase {

    protected static final String DISBURSEMENT_DATE = "01 January 2024";
    protected static final String EARLIER_DATE = "20 January 2024";
    protected static final String LATER_DATE = "10 February 2024";
    protected static final String BUSINESS_DATE = "15 February 2024";
    protected static final double PRINCIPAL = 1000.0;

    protected static final String INTEREST_REFUND = "Interest Refund";
    protected static final String INTEREST_REFUND_ENABLED_ERROR = "error.msg.loan.transaction.adjust.not.allowed.when.interest.refund.enabled";

    @FunctionalInterface
    protected interface TransactionPoster {

        /**
         * Posts the transaction under test and returns its id.
         */
        Long post(Long loanId, String date, double amount);
    }

    @FunctionalInterface
    protected interface LoanPreparation {

        /**
         * Brings a freshly disbursed loan into the state the scenario needs, before the transaction under test is
         * posted.
         */
        void prepare(Long loanId);
    }

    protected enum LoanVariant {

        PROGRESSIVE(true, false), //
        PROGRESSIVE_INTEREST_RECALCULATION(true, true), //
        CUMULATIVE(false, false), //
        CUMULATIVE_INTEREST_RECALCULATION(false, true);

        private final boolean progressive;
        private final boolean interestRecalculation;

        LoanVariant(boolean progressive, boolean interestRecalculation) {
            this.progressive = progressive;
            this.interestRecalculation = interestRecalculation;
        }

        boolean isCumulativeWithInterestRecalculation() {
            return !progressive && interestRecalculation;
        }

        boolean isProgressiveWithInterestRecalculation() {
            return progressive && interestRecalculation;
        }
    }

    /**
     * Every loan variant combined with every amount/date change, for {@code @MethodSource}.
     */
    protected static Stream<Arguments> loanVariantsAndChanges() {
        return Stream.of(LoanVariant.values()).flatMap(variant -> Stream.of(//
                Arguments.of(variant, "amount up", 100.0, LATER_DATE, 150.0, LATER_DATE), //
                Arguments.of(variant, "amount down", 100.0, LATER_DATE, 40.0, LATER_DATE), //
                Arguments.of(variant, "date back", 100.0, LATER_DATE, 100.0, EARLIER_DATE), //
                Arguments.of(variant, "date forward", 100.0, EARLIER_DATE, 100.0, LATER_DATE), //
                Arguments.of(variant, "amount and date", 100.0, LATER_DATE, 70.0, EARLIER_DATE)));
    }

    protected Long createProduct(LoanVariant variant) {
        PostLoanProductsRequest product = switch (variant) {
            case PROGRESSIVE -> fourInstallmentsProgressiveWithAdvancedAllocation();
            case PROGRESSIVE_INTEREST_RECALCULATION -> create4IProgressive();
            case CUMULATIVE -> fourInstallmentsCumulative();
            case CUMULATIVE_INTEREST_RECALCULATION -> create4ICumulative();
        };
        return createLoanProduct(product);
    }

    protected Long createProduct(PostLoanProductsRequest request) {
        return createLoanProduct(request);
    }

    protected Long createDisbursedLoan(Long clientId, Long productId, LoanVariant variant) {
        Long loanId = variant.progressive
                ? applyAndApproveProgressiveLoan(clientId, productId, DISBURSEMENT_DATE, PRINCIPAL, 10.0, 4,
                        request -> customizeLoanRequest(variant, request))
                : applyAndApproveCumulativeLoan(clientId, productId, DISBURSEMENT_DATE, PRINCIPAL, 10.0, 4,
                        request -> customizeLoanRequest(variant, request));
        disburseLoan(loanId, BigDecimal.valueOf(PRINCIPAL), DISBURSEMENT_DATE);
        return loanId;
    }

    private static void customizeLoanRequest(LoanVariant variant, PostLoansRequest request) {
        if (!variant.interestRecalculation) {
            request.interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD);
        }
    }

    /**
     * Adjusts a transaction and returns the id of the replacement transaction.
     */
    protected Long adjustTransaction(Long loanId, Long transactionId, String date, double amount) {
        PostLoansLoanIdTransactionsResponse response = transactionHelper.adjustLoanTransaction(loanId, transactionId, date, amount);
        assertNotNull(response.getResourceId());
        return response.getResourceId();
    }

    protected void assertAdjustRejected(Long loanId, Long transactionId, String date, double amount, String expectedErrorCode) {
        CallFailedRuntimeException exception = adjustLoanTransactionExpectingError(loanId, transactionId, date, amount);
        assertErrorGlobalisationCode(exception, expectedErrorCode);
        assertNotReversed(getTransaction(loanId, transactionId), "Rejected adjustment must not reverse the original");
    }

    protected static void assertNotReversed(GetLoansLoanIdTransactions transaction, String message) {
        assertTrue(transaction.getManuallyReversed() == null || !transaction.getManuallyReversed(), message);
    }

    /**
     * How the twin loan gets the replacement transaction.
     */
    protected enum TwinMode {
        /**
         * The twin takes the replacement transaction directly.
         */
        DIRECT,
        /**
         * The twin takes the replacement transaction directly and is then replayed as of the business date. For
         * transactions that are always booked on the business date (Charge Adjustment), so a twin transaction on an
         * earlier date is calculated as of that earlier date.
         */
        REPLAYED,
        /**
         * The twin takes a Repayment instead. On cumulative loans with interest recalculation a refund or charge
         * adjustment can only be posted as the latest transaction, and then skips the schedule regeneration that a
         * replay (and a Repayment) always does, so no direct posting of the same type is comparable. A Repayment is
         * allocated the same way; journal entries and per-type totals are not compared.
         */
        REPAYMENT
    }

    /**
     * Posts the original transaction on one loan and the replacement directly on a twin loan, adjusts the original, and
     * verifies that both loans end up in the same state.
     */
    protected Long verifyAdjustmentMatchesDirectPosting(LoanVariant variant, String expectedType, TransactionPoster poster,
            LoanPreparation preparation, double originalAmount, String originalDate, double newAmount, String newDate) {
        return verifyAdjustmentMatchesDirectPosting(variant, expectedType, poster, preparation, originalAmount, originalDate, newAmount,
                newDate, TwinMode.DIRECT);
    }

    protected Long verifyAdjustmentMatchesDirectPosting(LoanVariant variant, String expectedType, TransactionPoster poster,
            LoanPreparation preparation, double originalAmount, String originalDate, double newAmount, String newDate, TwinMode twinMode) {
        Long clientId = createClient(DISBURSEMENT_DATE);
        Long productId = createProduct(variant);

        Long loanId = createDisbursedLoan(clientId, productId, variant);
        Long twinLoanId = createDisbursedLoan(clientId, productId, variant);
        preparation.prepare(loanId);
        preparation.prepare(twinLoanId);

        Long originalTransactionId = poster.post(loanId, originalDate, originalAmount);
        Long twinTransactionId = twinMode == TwinMode.REPAYMENT ? addRepaymentForLoan(twinLoanId, newAmount, newDate)
                : poster.post(twinLoanId, newDate, newAmount);

        Long newTransactionId = adjustTransaction(loanId, originalTransactionId, newDate, newAmount);
        if (twinMode == TwinMode.REPLAYED) {
            twinTransactionId = replayLoan(twinLoanId, twinTransactionId);
        }

        assertReplacedWithSameType(loanId, originalTransactionId, newTransactionId, expectedType, newAmount, newDate);
        assertJournalEntriesReversed(originalTransactionId);
        if (twinMode != TwinMode.REPAYMENT) {
            assertSameJournalEntries(newTransactionId, twinTransactionId);
        }
        assertSameTransactionPortions(getTransaction(loanId, newTransactionId), getTransaction(twinLoanId, twinTransactionId));
        assertSameLoanState(loanId, twinLoanId, twinMode != TwinMode.REPAYMENT);
        return loanId;
    }

    /**
     * Adjusting a refund is rejected when the product calculates an Interest Refund for that refund type, while
     * reversing it keeps working and also reverses the linked Interest Refund.
     */
    protected void verifyAdjustRejectedWhenInterestRefundEnabled(String interestRefundType, String expectedType, TransactionPoster poster) {
        Long clientId = createClient(DISBURSEMENT_DATE);
        Long productId = createProduct(create4IProgressive().addSupportedInterestRefundTypesItem(interestRefundType));
        Long loanId = createDisbursedLoan(clientId, productId, LoanVariant.PROGRESSIVE_INTEREST_RECALCULATION);

        Long refundId = poster.post(loanId, LATER_DATE, 100.0);
        Long interestRefundId = findInterestRefund(loanId).getId();

        assertAdjustRejected(loanId, refundId, LATER_DATE, 150.0, INTEREST_REFUND_ENABLED_ERROR);

        adjustLoanTransaction(loanId, refundId, LATER_DATE);
        assertEquals(Boolean.TRUE, getTransaction(loanId, refundId).getManuallyReversed(), "Refund must be reversed");
        assertEquals(Boolean.TRUE, getTransaction(loanId, interestRefundId).getManuallyReversed(), "Interest Refund must be reversed");
        assertEquals(expectedType, getTransaction(loanId, refundId).getType().getValue());
    }

    /**
     * The product does not calculate an Interest Refund, but the refund requested one itself. The Interest Refund
     * calculation only takes the refund types enabled on the loan into account, so no Interest Refund is created and
     * the refund can be adjusted like any other.
     */
    protected void verifyAdjustAllowedWhenInterestRefundRequestedOnTransaction(String expectedType, TransactionPoster poster) {
        Long clientId = createClient(DISBURSEMENT_DATE);
        Long productId = createProduct(create4IProgressive());
        Long loanId = createDisbursedLoan(clientId, productId, LoanVariant.PROGRESSIVE_INTEREST_RECALCULATION);

        Long refundId = poster.post(loanId, LATER_DATE, 100.0);
        assertTrue(getLoanDetails(loanId).getTransactions().stream().noneMatch(tx -> INTEREST_REFUND.equals(tx.getType().getValue())),
                "No Interest Refund expected when the loan does not calculate one for " + expectedType);

        Long newId = adjustTransaction(loanId, refundId, EARLIER_DATE, 150.0);
        assertReplacedWithSameType(loanId, refundId, newId, expectedType, 150.0, EARLIER_DATE);
        assertTrue(getLoanDetails(loanId).getTransactions().stream().noneMatch(tx -> INTEREST_REFUND.equals(tx.getType().getValue())),
                "Adjustment must not create an Interest Refund");
    }

    /**
     * Enabling the Interest Refund for the other refund type does not block the adjustment, and the adjustment does not
     * create an Interest Refund.
     */
    protected void verifyAdjustAllowedWhenInterestRefundEnabledForOtherType(String otherInterestRefundType, String expectedType,
            TransactionPoster poster) {
        Long clientId = createClient(DISBURSEMENT_DATE);
        Long productId = createProduct(create4IProgressive().addSupportedInterestRefundTypesItem(otherInterestRefundType));
        Long loanId = createDisbursedLoan(clientId, productId, LoanVariant.PROGRESSIVE_INTEREST_RECALCULATION);

        Long refundId = poster.post(loanId, LATER_DATE, 100.0);
        Long newId = adjustTransaction(loanId, refundId, EARLIER_DATE, 150.0);

        assertReplacedWithSameType(loanId, refundId, newId, expectedType, 150.0, EARLIER_DATE);
        assertTrue(getLoanDetails(loanId).getTransactions().stream().noneMatch(tx -> INTEREST_REFUND.equals(tx.getType().getValue())),
                "No Interest Refund is expected");
    }

    private GetLoansLoanIdTransactions findInterestRefund(Long loanId) {
        return getLoanDetails(loanId).getTransactions().stream().filter(tx -> INTEREST_REFUND.equals(tx.getType().getValue())).findFirst()
                .orElseThrow(() -> new AssertionError("Interest Refund expected on loan " + loanId));
    }

    protected static LoanPreparation noPreparation() {
        return loanId -> {
            // nothing to prepare
        };
    }

    /**
     * Repays the loan down to the given outstanding amount, so the transaction under test can close or reopen it.
     */
    protected LoanPreparation repayDownTo(double remainingOutstanding) {
        return loanId -> {
            double outstanding = Utils.getDoubleValue(getLoanDetails(loanId).getSummary().getTotalOutstanding());
            addRepaymentForLoan(loanId, round(outstanding - remainingOutstanding), EARLIER_DATE);
        };
    }

    /**
     * Replays the whole loan as of the business date by posting a repayment and reversing it, and returns the id of the
     * transaction that took the place of the given one (the replay replaces transactions whose allocation changed).
     */
    private Long replayLoan(Long loanId, Long transactionId) {
        GetLoansLoanIdTransactions before = getTransaction(loanId, transactionId);
        Long repaymentId = addRepaymentForLoan(loanId, 1.0, BUSINESS_DATE);
        reverseLoanTransaction(loanId, repaymentId, BUSINESS_DATE);
        return getLoanDetails(loanId).getTransactions().stream()
                .filter(tx -> (tx.getManuallyReversed() == null || !tx.getManuallyReversed())
                        && Objects.equals(tx.getType().getValue(), before.getType().getValue())
                        && Objects.equals(tx.getDate(), before.getDate()) && tx.getAmount() != null
                        && tx.getAmount().compareTo(before.getAmount()) == 0)
                .map(GetLoansLoanIdTransactions::getId).findFirst()
                .orElseThrow(() -> new AssertionError("Replayed transaction not found for " + transactionId));
    }

    protected void assertReplacedWithSameType(Long loanId, Long originalTransactionId, Long newTransactionId, String expectedType,
            double expectedAmount, String expectedDate) {
        assertNotEquals(originalTransactionId, newTransactionId, "Adjustment must create a new transaction");

        GetLoansLoanIdTransactions original = getTransaction(loanId, originalTransactionId);
        assertEquals(Boolean.TRUE, original.getManuallyReversed(), "Original transaction must be reversed");
        assertEquals(expectedType, original.getType().getValue());

        GetLoansLoanIdTransactions replacement = getTransaction(loanId, newTransactionId);
        assertNotReversed(replacement, "Replacement must not be reversed");
        assertEquals(expectedType, replacement.getType().getValue(), "Replacement must keep the type of the original");
        assertEquals(expectedAmount, Utils.getDoubleValue(replacement.getAmount()));
        assertEquals(LocalDate.parse(expectedDate, dateTimeFormatter), replacement.getDate());
    }

    /**
     * Every journal entry posted for the transaction has an opposite entry, so the reversed transaction nets to zero on
     * every account.
     */
    protected void assertJournalEntriesReversed(Long transactionId) {
        List<JournalEntryTransactionItem> entries = journalEntries(transactionId);
        assertTrue(!entries.isEmpty(), "No journal entries found for transaction " + transactionId);
        Map<Long, Double> netByAccount = entries.stream()
                .collect(Collectors.groupingBy(JournalEntryTransactionItem::getGlAccountId, Collectors
                        .summingDouble(entry -> "DEBIT".equals(entry.getEntryType().getValue()) ? entry.getAmount() : -entry.getAmount())));
        netByAccount.forEach((accountId, net) -> assertEquals(0.0, net, 0.001,
                "Journal entries of reversed transaction " + transactionId + " do not net to zero on account " + accountId));
        assertEquals(0, entries.size() % 2, "Each journal entry of a reversed transaction must have a reversal entry");
    }

    protected void assertSameJournalEntries(Long transactionId, Long expectedTransactionId) {
        assertEquals(journalEntrySignatures(expectedTransactionId), journalEntrySignatures(transactionId), "Journal entries of transaction "
                + transactionId + " differ from those of directly posted transaction " + expectedTransactionId);
    }

    protected List<JournalEntryTransactionItem> journalEntries(Long transactionId) {
        return journalHelper.getJournalEntries("L" + transactionId).getPageItems();
    }

    private List<String> journalEntrySignatures(Long transactionId) {
        return journalEntries(transactionId).stream()
                .map(entry -> entry.getGlAccountId() + ":" + entry.getEntryType().getValue() + ":"
                        + BigDecimal.valueOf(entry.getAmount()).setScale(2, java.math.RoundingMode.HALF_UP).toPlainString())
                .sorted().toList();
    }

    protected void assertSameTransactionPortions(GetLoansLoanIdTransactions actual, GetLoansLoanIdTransactions expected) {
        assertAmount(expected.getPrincipalPortion(), actual.getPrincipalPortion(), "principal portion");
        assertAmount(expected.getInterestPortion(), actual.getInterestPortion(), "interest portion");
        assertAmount(expected.getFeeChargesPortion(), actual.getFeeChargesPortion(), "fee portion");
        assertAmount(expected.getPenaltyChargesPortion(), actual.getPenaltyChargesPortion(), "penalty portion");
        assertAmount(expected.getOverpaymentPortion(), actual.getOverpaymentPortion(), "overpayment portion");
        assertAmount(expected.getOutstandingLoanBalance(), actual.getOutstandingLoanBalance(), "outstanding loan balance");
    }

    protected void assertSameLoanState(Long loanId, Long expectedLoanId, boolean compareTotalsByType) {
        GetLoansLoanIdResponse actual = getLoanDetails(loanId);
        GetLoansLoanIdResponse expected = getLoanDetails(expectedLoanId);

        assertEquals(expected.getStatus().getCode(), actual.getStatus().getCode(), "loan status");
        assertAmount(expected.getTotalOverpaid(), actual.getTotalOverpaid(), "total overpaid");

        GetLoansLoanIdSummary expectedSummary = expected.getSummary();
        GetLoansLoanIdSummary actualSummary = actual.getSummary();
        assertAmount(expectedSummary.getTotalOutstanding(), actualSummary.getTotalOutstanding(), "total outstanding");
        assertAmount(expectedSummary.getPrincipalOutstanding(), actualSummary.getPrincipalOutstanding(), "principal outstanding");
        assertAmount(expectedSummary.getInterestOutstanding(), actualSummary.getInterestOutstanding(), "interest outstanding");
        assertAmount(expectedSummary.getFeeChargesOutstanding(), actualSummary.getFeeChargesOutstanding(), "fee outstanding");
        assertAmount(expectedSummary.getPenaltyChargesOutstanding(), actualSummary.getPenaltyChargesOutstanding(), "penalty outstanding");
        assertAmount(expectedSummary.getPrincipalPaid(), actualSummary.getPrincipalPaid(), "principal paid");
        assertAmount(expectedSummary.getInterestPaid(), actualSummary.getInterestPaid(), "interest paid");
        assertAmount(expectedSummary.getFeeChargesPaid(), actualSummary.getFeeChargesPaid(), "fee paid");
        if (compareTotalsByType) {
            assertAmount(expectedSummary.getTotalGoodwillCredit(), actualSummary.getTotalGoodwillCredit(), "total goodwill credit");
            assertAmount(expectedSummary.getTotalPayoutRefund(), actualSummary.getTotalPayoutRefund(), "total payout refund");
            assertAmount(expectedSummary.getTotalMerchantRefund(), actualSummary.getTotalMerchantRefund(), "total merchant refund");
            assertAmount(expectedSummary.getTotalChargeAdjustment(), actualSummary.getTotalChargeAdjustment(), "total charge adjustment");
        }

        List<GetLoansLoanIdRepaymentPeriod> expectedPeriods = expected.getRepaymentSchedule().getPeriods();
        List<GetLoansLoanIdRepaymentPeriod> actualPeriods = actual.getRepaymentSchedule().getPeriods();
        assertEquals(expectedPeriods.size(), actualPeriods.size(), "number of schedule periods");
        for (int i = 0; i < expectedPeriods.size(); i++) {
            GetLoansLoanIdRepaymentPeriod e = expectedPeriods.get(i);
            GetLoansLoanIdRepaymentPeriod a = actualPeriods.get(i);
            String period = "period " + e.getPeriod() + " ";
            assertEquals(e.getDueDate(), a.getDueDate(), period + "due date");
            assertAmount(e.getPrincipalDue(), a.getPrincipalDue(), period + "principal due");
            assertAmount(e.getPrincipalPaid(), a.getPrincipalPaid(), period + "principal paid");
            assertAmount(e.getInterestDue(), a.getInterestDue(), period + "interest due");
            assertAmount(e.getInterestPaid(), a.getInterestPaid(), period + "interest paid");
            assertAmount(e.getFeeChargesDue(), a.getFeeChargesDue(), period + "fee due");
            assertAmount(e.getFeeChargesPaid(), a.getFeeChargesPaid(), period + "fee paid");
            assertAmount(e.getTotalOutstandingForPeriod(), a.getTotalOutstandingForPeriod(), period + "total outstanding");
        }

        List<GetLoansLoanIdLoanChargeData> expectedCharges = sortedCharges(expected);
        List<GetLoansLoanIdLoanChargeData> actualCharges = sortedCharges(actual);
        assertEquals(expectedCharges.size(), actualCharges.size(), "number of loan charges");
        for (int i = 0; i < expectedCharges.size(); i++) {
            assertAmount(expectedCharges.get(i).getAmountPaid(), actualCharges.get(i).getAmountPaid(), "charge " + i + " paid");
            assertAmount(expectedCharges.get(i).getAmountOutstanding(), actualCharges.get(i).getAmountOutstanding(),
                    "charge " + i + " outstanding");
        }
    }

    private static List<GetLoansLoanIdLoanChargeData> sortedCharges(GetLoansLoanIdResponse loan) {
        return loan.getCharges() == null ? List.of()
                : loan.getCharges().stream().sorted(Comparator.comparing(GetLoansLoanIdLoanChargeData::getId)).toList();
    }

    protected GetLoansLoanIdTransactions getTransaction(Long loanId, Long transactionId) {
        return getLoanDetails(loanId).getTransactions().stream().filter(tx -> Objects.equals(tx.getId(), transactionId)).findFirst()
                .orElseThrow(() -> new AssertionError("Transaction " + transactionId + " not found on loan " + loanId));
    }

    private static void assertAmount(BigDecimal expected, BigDecimal actual, String what) {
        // the API leaves out zero amounts
        assertEquals(Objects.requireNonNullElse(expected, BigDecimal.ZERO).doubleValue(),
                Objects.requireNonNullElse(actual, BigDecimal.ZERO).doubleValue(), 0.001, what);
    }

    private static double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, java.math.RoundingMode.HALF_UP).doubleValue();
    }
}
