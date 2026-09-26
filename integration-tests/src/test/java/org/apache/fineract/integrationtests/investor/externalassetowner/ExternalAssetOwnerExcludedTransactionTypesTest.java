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
package org.apache.fineract.integrationtests.investor.externalassetowner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.fineract.client.models.ExternalAssetOwnerRequest;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventResponse;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessStepHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignExternalAssetOwnerHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.AmortizationType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.InterestCalculationPeriodType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.InterestRateFrequencyType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.InterestType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.RepaymentFrequencyType;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Covers the EXCLUDED_TRANSACTION_TYPES loan product attribute end to end. Every case asserts BOTH enforcement points
 * for the same transaction in the same test (the presence of an external asset owner on the journal entries and the
 * presence of externalOwnerId on the published event). A divergence between the trial balance and the event stream is
 * exactly the failure this feature can introduce.
 */
public class ExternalAssetOwnerExcludedTransactionTypesTest extends FeignLoanTestBase {

    private static final String EXCLUDED_TRANSACTION_TYPES = "EXCLUDED_TRANSACTION_TYPES";
    private static final String BUY_DOWN_FEE_TYPES = "BUY_DOWN_FEE,BUY_DOWN_FEE_ADJUSTMENT,BUY_DOWN_FEE_AMORTIZATION,BUY_DOWN_FEE_AMORTIZATION_ADJUSTMENT";
    private static final List<String> EVENTS_UNDER_TEST = List.of("LoanBuyDownFeeTransactionCreatedBusinessEvent",
            "LoanBuyDownFeeAmortizationTransactionCreatedBusinessEvent", "LoanBuyDownFeeAdjustmentTransactionCreatedBusinessEvent",
            "LoanTransactionMakeRepaymentPostBusinessEvent", "LoanAdjustTransactionBusinessEvent");

    /** One account per type is enough: these tests assert who a journal entry belongs to, never what it is worth. */
    private static Account assetAccount;
    private static Account incomeAccount;
    private static Account expenseAccount;
    private static Account liabilityAccount;

    private final FeignExternalAssetOwnerHelper externalAssetOwnerHelper = new FeignExternalAssetOwnerHelper(
            FineractFeignClientHelper.getFineractFeignClient());

    @BeforeAll
    public static void createGlAccounts() {
        assetAccount = accountHelper.createAssetAccount(Utils.uniqueRandomStringGenerator("ASSET_", 5));
        incomeAccount = accountHelper.createIncomeAccount(Utils.uniqueRandomStringGenerator("INCOME_", 5));
        expenseAccount = accountHelper.createExpenseAccount(Utils.uniqueRandomStringGenerator("EXPENSE_", 5));
        liabilityAccount = accountHelper.createLiabilityAccount(Utils.uniqueRandomStringGenerator("LIABILITY_", 5));
    }

    /**
     * The external event configuration is global, so the events enabled here must be switched off again or unrelated
     * tests that assert on the exact set of published events start failing.
     */
    @AfterAll
    public static void teardown() {
        EVENTS_UNDER_TEST.forEach(externalEventHelper::disableBusinessEvent);
    }

    @BeforeEach
    public void beforeEach() {
        new FeignBusinessStepHelper(FineractFeignClientHelper.getFineractFeignClient()).updateSteps("LOAN_CLOSE_OF_BUSINESS",
                "APPLY_CHARGE_TO_OVERDUE_LOANS", "LOAN_DELINQUENCY_CLASSIFICATION", "CHECK_LOAN_REPAYMENT_DUE",
                "CHECK_LOAN_REPAYMENT_OVERDUE", "CHECK_DUE_INSTALLMENTS", "UPDATE_LOAN_ARREARS_AGING", "ADD_PERIODIC_ACCRUAL_ENTRIES",
                "ACCRUAL_ACTIVITY_POSTING", "CAPITALIZED_INCOME_AMORTIZATION", "BUY_DOWN_FEE_AMORTIZATION", "LOAN_INTEREST_RECALCULATION",
                "EXTERNAL_ASSET_OWNER_TRANSFER");
        EVENTS_UNDER_TEST.forEach(externalEventHelper::enableBusinessEvent);
        final Account transferAccount = accountHelper.createAssetAccount("transferInSuspense");
        externalAssetOwnerHelper.setProperFinancialActivity(transferAccount);
        // Start from an empty event store: a reused database can still hold events serialised with an older avro
        // schema, and reading those back fails before any assertion of ours runs.
        externalEventHelper.deleteAllExternalEvents();
    }

    @Test
    public void testExcludedTypeIsUntaggedWhileNonExcludedTypeStaysTaggedOnBothSurfaces() {
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();
        final AtomicReference<Long> buyDownFeeTxRef = new AtomicReference<>();
        final AtomicReference<Long> repaymentTxRef = new AtomicReference<>();
        final String ownerExternalId = UUID.randomUUID().toString();

        final Long clientId = createClient();
        final Long loanProductId = createLoanProduct(buyDownFeeProduct());
        // Configure BEFORE any transaction: the attribute cache is per node, so nothing in the test may depend on
        // mid-test cache propagation.
        externalAssetOwnerHelper.createLoanProductAttribute(loanProductId, EXCLUDED_TRANSACTION_TYPES, BUY_DOWN_FEE_TYPES);

        runAt("01 July 2026", () -> {
            final Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "01 July 2026", 1000.0, 10.0, 6, null);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(1000.0), "01 July 2026");
            sellLoanToInvestor(loanId, ownerExternalId, "2026-07-01");
        });

        runAt("02 July 2026", () -> {
            final Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            buyDownFeeTxRef.set(makeLoanBuyDownFee(loanId, "02 July 2026", 50.0).getResourceId());
            assertUntaggedOnBothSurfaces(buyDownFeeTxRef.get());

            final PostLoansLoanIdTransactionsResponse repayment = makeLoanRepayment(loanId, "repayment", "02 July 2026", 100.0);
            repaymentTxRef.set(repayment.getResourceId());
            assertTaggedOnBothSurfaces(repaymentTxRef.get(), ownerExternalId);
        });

        runAt("03 July 2026", () -> {
            final Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            final Long amortizationTxId = findTransactionId(loanId);
            assertUntaggedOnBothSurfaces(amortizationTxId);
        });
    }

    @Test
    public void testWithoutTheAttributeTheExcludableTypeIsStillTagged() {
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();
        final String ownerExternalId = UUID.randomUUID().toString();

        final Long clientId = createClient();
        final Long loanProductId = createLoanProduct(buyDownFeeProduct());

        runAt("01 July 2026", () -> {
            final Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "01 July 2026", 1000.0, 10.0, 6, null);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(1000.0), "01 July 2026");
            sellLoanToInvestor(loanId, ownerExternalId, "2026-07-01");
        });

        runAt("02 July 2026", () -> {
            final Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);
            final Long buyDownFeeTxId = makeLoanBuyDownFee(loanId, "02 July 2026", 50.0).getResourceId();
            assertTaggedOnBothSurfaces(buyDownFeeTxId, ownerExternalId);
        });
    }

    @Test
    public void testOnAnUnownedLoanNothingIsTaggedWhetherOrNotTheTypeIsExcluded() {
        final Long clientId = createClient();
        final Long loanProductId = createLoanProduct(buyDownFeeProduct());
        externalAssetOwnerHelper.createLoanProductAttribute(loanProductId, EXCLUDED_TRANSACTION_TYPES, BUY_DOWN_FEE_TYPES);

        runAt("01 July 2026", () -> {
            final Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "01 July 2026", 1000.0, 10.0, 6, null);
            disburseLoan(loanId, BigDecimal.valueOf(1000.0), "01 July 2026");

            assertUntaggedOnBothSurfaces(makeLoanBuyDownFee(loanId, "01 July 2026", 50.0).getResourceId());
            assertUntaggedOnBothSurfaces(makeLoanRepayment(loanId, "repayment", "01 July 2026", 100.0).getResourceId());
        });
    }

    @Test
    public void testAdjustmentAndReversalOfExcludedTransactionsAreAlsoUntagged() {
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();
        final AtomicReference<Long> buyDownFeeTxRef = new AtomicReference<>();
        final String ownerExternalId = UUID.randomUUID().toString();

        final Long clientId = createClient();
        final Long loanProductId = createLoanProduct(buyDownFeeProduct());
        externalAssetOwnerHelper.createLoanProductAttribute(loanProductId, EXCLUDED_TRANSACTION_TYPES, BUY_DOWN_FEE_TYPES);

        runAt("01 July 2026", () -> {
            final Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "01 July 2026", 1000.0, 10.0, 6, null);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(1000.0), "01 July 2026");
            sellLoanToInvestor(loanId, ownerExternalId, "2026-07-01");
        });

        runAt("02 July 2026", () -> {
            final Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);
            buyDownFeeTxRef.set(makeLoanBuyDownFee(loanId, "02 July 2026", 50.0).getResourceId());
            assertUntaggedOnBothSurfaces(buyDownFeeTxRef.get());
        });

        runAt("03 July 2026", () -> {
            final Long loanId = loanIdRef.get();
            final Long buyDownFeeTxId = buyDownFeeTxRef.get();
            final Long adjustmentTxId = buyDownFeeAdjustment(loanId, buyDownFeeTxId, "03 July 2026", 20.0).getResourceId();
            assertUntaggedOnBothSurfaces(adjustmentTxId);

            // A buy down fee cannot be reversed while a non-reversed adjustment exists, so the adjustment goes first.
            final int adjustmentEntryCount = journalEntriesOf(adjustmentTxId).size();
            final int buyDownFeeEntryCount = journalEntriesOf(buyDownFeeTxId).size();
            reverseLoanTransaction(loanId, adjustmentTxId, "03 July 2026");
            reverseLoanTransaction(loanId, buyDownFeeTxId, "03 July 2026");

            assertReversalUntaggedOnBothSurfaces(adjustmentTxId, adjustmentEntryCount);
            assertReversalUntaggedOnBothSurfaces(buyDownFeeTxId, buyDownFeeEntryCount);
        });
    }

    @Test
    public void testSaleTimeRecognitionIsNotAttributedToTheOutgoingInvestor() {
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();
        final String firstOwnerExternalId = UUID.randomUUID().toString();
        final String secondOwnerExternalId = UUID.randomUUID().toString();

        final Long clientId = createClient();
        final Long loanProductId = createLoanProduct(buyDownFeeProduct());
        externalAssetOwnerHelper.createLoanProductAttribute(loanProductId, EXCLUDED_TRANSACTION_TYPES, BUY_DOWN_FEE_TYPES);

        runAt("01 July 2026", () -> {
            final Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "01 July 2026", 1000.0, 10.0, 6, null);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(1000.0), "01 July 2026");
            makeLoanBuyDownFee(loanId, "01 July 2026", 50.0);
            sellLoanToInvestor(loanId, firstOwnerExternalId, "2026-07-01");
        });

        runAt("02 July 2026", () -> {
            executeInlineCOB(loanIdRef.get());
            sellLoanToInvestor(loanIdRef.get(), secondOwnerExternalId, "2026-07-02");
        });

        runAt("03 July 2026", () -> {
            final Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            assertUntaggedOnBothSurfaces(findTransactionId(loanId));

            assertNotNull(externalAssetOwnerHelper.retrieveJournalEntriesOfOwner(firstOwnerExternalId).getJournalEntryData(),
                    "asset transfer journal entries must stay owner tagged");
        });
    }

    // --- assertions on both surfaces -------------------------------------------------------------------------------

    private void assertUntaggedOnBothSurfaces(final Long loanTransactionId) {
        final List<JournalEntryTransactionItem> entries = journalEntriesOf(loanTransactionId);
        assertFalse(entries.isEmpty(), "expected journal entries for loan transaction " + loanTransactionId);
        entries.forEach(entry -> assertNull(entry.getExternalAssetOwner(),
                "journal entry of loan transaction " + loanTransactionId + " must not be attributed to any owner"));
        assertEventOwner(loanTransactionId, null);
    }

    private void assertTaggedOnBothSurfaces(final Long loanTransactionId, final String ownerExternalId) {
        final List<JournalEntryTransactionItem> entries = journalEntriesOf(loanTransactionId);
        assertFalse(entries.isEmpty(), "expected journal entries for loan transaction " + loanTransactionId);
        entries.forEach(entry -> assertEquals(ownerExternalId, entry.getExternalAssetOwner(),
                "journal entry of loan transaction " + loanTransactionId + " must be attributed to the owner"));
        assertEventOwner(loanTransactionId, ownerExternalId);
    }

    /**
     * Reversal books a mirrored entry for every original line under the same loan transaction, so the transaction must
     * now carry twice the entries, none of them owner tagged, and its adjust event must not name an owner either.
     */
    private void assertReversalUntaggedOnBothSurfaces(final Long loanTransactionId, final int originalEntryCount) {
        final List<JournalEntryTransactionItem> entries = journalEntriesOf(loanTransactionId);
        assertEquals(2 * originalEntryCount, entries.size(), "expected reversal journal entries for loan transaction " + loanTransactionId);
        entries.forEach(entry -> assertNull(entry.getExternalAssetOwner(),
                "journal entry of reversed loan transaction " + loanTransactionId + " must not be attributed to any owner"));
        assertAdjustEventUntagged(loanTransactionId);
    }

    private List<JournalEntryTransactionItem> journalEntriesOf(final Long loanTransactionId) {
        final List<JournalEntryTransactionItem> items = journalHelper.getJournalEntriesByTransactionId("L" + loanTransactionId)
                .getPageItems();
        return items == null ? List.of() : items;
    }

    private void assertEventOwner(final Long loanTransactionId, final String expectedOwnerExternalId) {
        // External events are flushed as the surrounding transaction commits, so the event may not be readable the
        // instant the API call returns. This mirrors verifyBusinessEvents in the test base.
        Awaitility.await().atMost(Duration.ofSeconds(30)).pollInterval(Duration.ofMillis(500)).untilAsserted(() -> {
            final List<Map<String, Object>> payloads = externalEventHelper.getAllExternalEvents().stream()
                    .map(ExternalEventResponse::getPayLoad).filter(payload -> isTransactionPayload(payload, loanTransactionId)).toList();
            assertFalse(payloads.isEmpty(), "expected an external event for loan transaction " + loanTransactionId);
            payloads.forEach(payload -> assertEquals(expectedOwnerExternalId, payload.get("externalOwnerId"),
                    "event of loan transaction " + loanTransactionId + " carries the wrong externalOwnerId"));
        });
    }

    private void assertAdjustEventUntagged(final Long loanTransactionId) {
        Awaitility.await().atMost(Duration.ofSeconds(30)).pollInterval(Duration.ofMillis(500)).untilAsserted(() -> {
            final List<Map<?, ?>> adjustedTransactions = externalEventHelper.getAllExternalEvents().stream()
                    .filter(event -> "LoanAdjustTransactionBusinessEvent".equals(event.getType()))
                    .map(event -> event.getPayLoad().get("transactionToAdjust")).filter(Map.class::isInstance)
                    .<Map<?, ?>>map(Map.class::cast)
                    .filter(transaction -> String.valueOf(loanTransactionId).equals(String.valueOf(transaction.get("id")))).toList();
            assertFalse(adjustedTransactions.isEmpty(), "expected an adjust event for loan transaction " + loanTransactionId);
            adjustedTransactions.forEach(transaction -> assertNull(transaction.get("externalOwnerId"),
                    "adjust event of loan transaction " + loanTransactionId + " must not carry an externalOwnerId"));
        });
    }

    /**
     * A loan transaction payload, identified by id. externalOwnerId is deliberately not required to be present: the
     * serialised record omits it when it is null, which is precisely the excluded case under test.
     */
    private boolean isTransactionPayload(final Map<String, Object> payload, final Long loanTransactionId) {
        if (payload == null || payload.get("id") == null || payload.get("loanId") == null) {
            return false;
        }
        return loanTransactionId.equals(Long.valueOf(String.valueOf(payload.get("id"))));
    }

    // --- fixtures --------------------------------------------------------------------------------------------------

    private void sellLoanToInvestor(final Long loanId, final String ownerExternalId, final String settlementDate) {
        externalAssetOwnerHelper.initiateTransferByLoanId(loanId, "sale",
                new ExternalAssetOwnerRequest().settlementDate(settlementDate).dateFormat("yyyy-MM-dd").locale("en")
                        .transferExternalId(UUID.randomUUID().toString()).ownerExternalId(ownerExternalId).purchasePriceRatio("1.0"));
    }

    private Long findTransactionId(final Long loanId) {
        final GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        assertNotNull(loanDetails.getTransactions());
        final List<GetLoansLoanIdTransactions> matching = loanDetails.getTransactions().stream().filter(
                transaction -> transaction.getType() != null && Boolean.TRUE.equals(transaction.getType().getBuyDownFeeAmortization()))
                .filter(transaction -> transaction.getType() != null
                        && Boolean.TRUE.equals(transaction.getType().getBuyDownFeeAmortization()))
                .toList();
        assertFalse(matching.isEmpty(), "expected a buy down fee amortization transaction on loan " + loanId);
        return matching.getLast().getId();
    }

    private PostLoanProductsRequest buyDownFeeProduct() {
        return withPeriodicAccrualAccounting(new PostLoanProductsRequest() //
                .name(Utils.uniqueRandomStringGenerator("BDF_EXCL_", 6)) //
                .shortName(Utils.uniqueRandomStringGenerator("", 4)) //
                .currencyCode("USD") //
                .digitsAfterDecimal(2) //
                .principal(1000.0) //
                .numberOfRepayments(6) //
                .repaymentEvery(1) //
                .repaymentFrequencyType(RepaymentFrequencyType.MONTHS_L) //
                .interestRatePerPeriod(10.0) //
                .interestRateFrequencyType(InterestRateFrequencyType.YEARS) //
                .amortizationType(AmortizationType.EQUAL_INSTALLMENTS) //
                .interestType(InterestType.DECLINING_BALANCE) //
                .interestCalculationPeriodType(InterestCalculationPeriodType.DAILY) //
                .daysInMonthType(30) //
                .daysInYearType(360) //
                .isInterestRecalculationEnabled(false) //
                .transactionProcessingStrategyCode("advanced-payment-allocation-strategy") //
                .paymentAllocation(List.of(createDefaultPaymentAllocation("NEXT_INSTALLMENT"))) //
                .loanScheduleType("PROGRESSIVE") //
                .loanScheduleProcessingType("HORIZONTAL") //
                .enableBuyDownFee(true) //
                .merchantBuyDownFee(true) //
                .buyDownFeeCalculationType(PostLoanProductsRequest.BuyDownFeeCalculationTypeEnum.FLAT) //
                .buyDownFeeStrategy(PostLoanProductsRequest.BuyDownFeeStrategyEnum.EQUAL_AMORTIZATION) //
                .buyDownFeeIncomeType(PostLoanProductsRequest.BuyDownFeeIncomeTypeEnum.FEE) //
                .locale("en") //
                .dateFormat("dd MMMM yyyy"), assetAccount, incomeAccount, expenseAccount, liabilityAccount) //
                .buyDownExpenseAccountId(expenseAccount.getAccountID().longValue()) //
                .incomeFromBuyDownAccountId(incomeAccount.getAccountID().longValue()) //
                .deferredIncomeLiabilityAccountId(liabilityAccount.getAccountID().longValue());
    }
}
