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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetJournalEntriesTransactionIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsRequest.AccountingRuleEnum;
import org.apache.fineract.client.models.PostWorkingCapitalLoansRequest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignJournalEntryHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalLoanRequestBuilders;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanDisbursementTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Integration tests verifying the Working Capital Loan charge-off accounting treatment.
 * <p>
 * Charge-off is a pure accounting tag: it writes off the outstanding receivables against the charge-off expense (no
 * interest leg -- WC has no interest concept), keeps the loan ACTIVE, and is reversible via undo.
 */
public class WorkingCapitalLoanChargeOffAccountingTest {

    private static final DateTimeFormatter BUSINESS_DATE = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    private final WorkingCapitalLoanHelper loanHelper = new WorkingCapitalLoanHelper();
    private final WorkingCapitalLoanProductHelper productHelper = new WorkingCapitalLoanProductHelper();
    private final List<Long> createdLoanIds = new ArrayList<>();
    private final List<Long> createdProductIds = new ArrayList<>();
    private static Long createdClientId;

    // GL accounts for accrual with deferred revenue amortization accounting
    private static Account fundSourceAccount;
    private static Account loanPortfolioAccount;
    private static Account transfersSuspenseAccount;
    private static Account incomeFromDiscountFeeAccount;
    private static Account feesReceivableAccount;
    private static Account penaltiesReceivableAccount;
    private static Account incomeFromFeeAccount;
    private static Account incomeFromPenaltyAccount;
    private static Account incomeFromRecoveryAccount;
    private static Account writeOffAccount;
    private static Account overpaymentAccount;
    private static Account deferredIncomeAccount;
    private static Account chargeOffExpenseAccount;
    private static Account incomeFromChargeOffFeesAccount;
    private static Account incomeFromChargeOffPenaltyAccount;
    private static Account goodwillCreditAccount;

    @BeforeAll
    public static void setupAccounts() {
        createdClientId = ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
        final FeignAccountHelper accountHelper = new FeignAccountHelper(FineractFeignClientHelper.getFineractFeignClient());
        fundSourceAccount = accountHelper.createLiabilityAccount("wcCoFundSource");
        loanPortfolioAccount = accountHelper.createAssetAccount("wcCoLoanPortfolio");
        transfersSuspenseAccount = accountHelper.createAssetAccount("wcCoTransfersSuspense");
        incomeFromDiscountFeeAccount = accountHelper.createIncomeAccount("wcCoIncomeDiscountFee");
        feesReceivableAccount = accountHelper.createAssetAccount("wcCoFeesReceivable");
        penaltiesReceivableAccount = accountHelper.createAssetAccount("wcCoPenaltiesReceivable");
        incomeFromFeeAccount = accountHelper.createIncomeAccount("wcCoIncomeFee");
        incomeFromPenaltyAccount = accountHelper.createIncomeAccount("wcCoIncomePenalty");
        incomeFromRecoveryAccount = accountHelper.createIncomeAccount("wcCoIncomeRecovery");
        writeOffAccount = accountHelper.createExpenseAccount("wcCoWriteOff");
        overpaymentAccount = accountHelper.createLiabilityAccount("wcCoOverpayment");
        deferredIncomeAccount = accountHelper.createLiabilityAccount("wcCoDeferredIncome");
        chargeOffExpenseAccount = accountHelper.createExpenseAccount("wcCoChargeOffExpense");
        incomeFromChargeOffFeesAccount = accountHelper.createIncomeAccount("wcCoIncomeChargeOffFees");
        incomeFromChargeOffPenaltyAccount = accountHelper.createIncomeAccount("wcCoIncomeChargeOffPenalty");
        goodwillCreditAccount = accountHelper.createExpenseAccount("wcCoGoodwillCredit");
    }

    @Test
    public void testChargeOffWritesOffPrincipalAndKeepsLoanActive() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProduct();
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        final AtomicLong loanId = new AtomicLong(0L);
        BusinessDateHelper.runAt(currentDate.format(BUSINESS_DATE),
                () -> loanId.set(createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), currentDate)));

        final LocalDate chargeOffDate = currentDate.plusDays(1);
        final AtomicLong chargeOffTxnId = new AtomicLong(0L);
        BusinessDateHelper.runAt(chargeOffDate.format(BUSINESS_DATE), () -> chargeOffTxnId.set(loanHelper.chargeOffByLoanId(loanId.get(),
                WorkingCapitalLoanDisbursementTestBuilder.buildChargeOffRequest(chargeOffDate, "charge-off note"))));

        // The loan stays ACTIVE and is flagged as charged off (pure accounting tag, no portfolio impact).
        final GetWorkingCapitalLoansLoanIdResponse loanData = loanHelper.retrieveById(loanId.get());
        assertNotNull(loanData.getStatus());
        assertEquals("loanStatusType.active", loanData.getStatus().getCode());
        assertEquals(Boolean.TRUE, loanData.getChargedOff());

        // Only principal was outstanding (no fees/penalties): Dr Charge-off expense 5000, Cr Loan portfolio 5000.
        final List<JournalEntryTransactionItem> entries = getJournalEntriesForWCTransaction(chargeOffTxnId.get());
        assertEquals(2, entries.size(), "Expected 2 journal entries (1 debit + 1 credit)");
        assertJournalEntry(entries, "DEBIT", chargeOffExpenseAccount, 5000.0);
        assertJournalEntry(entries, "CREDIT", loanPortfolioAccount, 5000.0);
    }

    @Test
    public void testUndoChargeOffReversesJournalEntriesAndClearsTag() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProduct();
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        final AtomicLong loanId = new AtomicLong(0L);
        BusinessDateHelper.runAt(currentDate.format(BUSINESS_DATE),
                () -> loanId.set(createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), currentDate)));

        final LocalDate chargeOffDate = currentDate.plusDays(1);
        final AtomicLong chargeOffTxnId = new AtomicLong(0L);
        BusinessDateHelper.runAt(chargeOffDate.format(BUSINESS_DATE), () -> chargeOffTxnId.set(loanHelper.chargeOffByLoanId(loanId.get(),
                WorkingCapitalLoanDisbursementTestBuilder.buildChargeOffRequest(chargeOffDate, null))));

        final LocalDate undoDate = chargeOffDate.plusDays(1);
        BusinessDateHelper.runAt(undoDate.format(BUSINESS_DATE), () -> loanHelper.undoChargeOffByLoanId(loanId.get(),
                WorkingCapitalLoanDisbursementTestBuilder.buildUndoChargeOffRequest("undo note")));

        // The charge-off tag is cleared and the loan stays ACTIVE.
        final GetWorkingCapitalLoansLoanIdResponse loanData = loanHelper.retrieveById(loanId.get());
        assertNotNull(loanData.getStatus());
        assertEquals("loanStatusType.active", loanData.getStatus().getCode());
        assertFalse(Boolean.TRUE.equals(loanData.getChargedOff()));

        // The original 2 entries are reversed: querying the charge-off transaction now returns the originals plus their
        // reversals (Dr Loan portfolio 5000, Cr Charge-off expense 5000).
        final List<JournalEntryTransactionItem> entries = getJournalEntriesForWCTransaction(chargeOffTxnId.get());
        assertEquals(4, entries.size(), "Expected 4 journal entries (2 original + 2 reversal)");
        assertJournalEntry(entries, "DEBIT", loanPortfolioAccount, 5000.0);
        assertJournalEntry(entries, "CREDIT", chargeOffExpenseAccount, 5000.0);
    }

    @Test
    public void testSecondChargeOffFails() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProduct();
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        final AtomicLong loanId = new AtomicLong(0L);
        BusinessDateHelper.runAt(currentDate.format(BUSINESS_DATE),
                () -> loanId.set(createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), currentDate)));

        final LocalDate chargeOffDate = currentDate.plusDays(1);
        BusinessDateHelper.runAt(chargeOffDate.format(BUSINESS_DATE), () -> {
            loanHelper.chargeOffByLoanId(loanId.get(),
                    WorkingCapitalLoanDisbursementTestBuilder.buildChargeOffRequest(chargeOffDate, null));
            final CallFailedRuntimeException error = loanHelper.runChargeOffByLoanIdExpectingFailure(loanId.get(),
                    WorkingCapitalLoanDisbursementTestBuilder.buildChargeOffRequest(chargeOffDate, null));
            assertTrue(error.getMessage() != null && error.getMessage().contains("already.charged.off"),
                    "Expected already-charged-off validation error, got: " + error.getMessage());
        });
    }

    @Test
    public void testChargeOffWithNoAccountingCreatesNoJournalEntries() {
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        final AtomicLong loanId = new AtomicLong(0L);
        BusinessDateHelper.runAt(currentDate.format(BUSINESS_DATE), () -> {
            final String uniqueName = "WCL CoNoAcct " + UUID.randomUUID().toString().substring(0, 8);
            final String uniqueShortName = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
            final Long productId = productHelper
                    .createWorkingCapitalLoanProduct(
                            new WorkingCapitalLoanProductTestBuilder().withName(uniqueName).withShortName(uniqueShortName).build())
                    .getResourceId();
            createdProductIds.add(productId);
            loanId.set(createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), currentDate));
        });

        final LocalDate chargeOffDate = currentDate.plusDays(1);
        final AtomicLong chargeOffTxnId = new AtomicLong(0L);
        BusinessDateHelper.runAt(chargeOffDate.format(BUSINESS_DATE), () -> chargeOffTxnId.set(loanHelper.chargeOffByLoanId(loanId.get(),
                WorkingCapitalLoanDisbursementTestBuilder.buildChargeOffRequest(chargeOffDate, null))));

        final List<JournalEntryTransactionItem> entries = getJournalEntriesForWCTransaction(chargeOffTxnId.get());
        assertTrue(entries.isEmpty(), "Expected no journal entries for NONE accounting rule");
    }

    @Test
    public void testRepaymentAfterChargeOffPostsToRecoveryIncome() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProduct();
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        final AtomicLong loanId = new AtomicLong(0L);
        BusinessDateHelper.runAt(currentDate.format(BUSINESS_DATE),
                () -> loanId.set(createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), currentDate)));

        final LocalDate chargeOffDate = currentDate.plusDays(1);
        BusinessDateHelper.runAt(chargeOffDate.format(BUSINESS_DATE), () -> loanHelper.chargeOffByLoanId(loanId.get(),
                WorkingCapitalLoanDisbursementTestBuilder.buildChargeOffRequest(chargeOffDate, null)));

        // Repayments stay allowed after charge-off (the balance can be cured); their credits are routed to recovery
        // income instead of the loan portfolio / receivables.
        final LocalDate repaymentDate = chargeOffDate.plusDays(1);
        final AtomicLong repaymentTxnId = new AtomicLong(0L);
        BusinessDateHelper.runAt(repaymentDate.format(BUSINESS_DATE),
                () -> repaymentTxnId.set(loanHelper.makeRepaymentByLoanId(loanId.get(), WorkingCapitalLoanDisbursementTestBuilder
                        .buildRepaymentRequest(repaymentDate, BigDecimal.valueOf(1000), null, null, null, null))));

        final List<JournalEntryTransactionItem> entries = getJournalEntriesForWCTransaction(repaymentTxnId.get());
        assertEquals(2, entries.size(), "Expected 2 journal entries (1 debit + 1 credit)");
        assertJournalEntry(entries, "DEBIT", fundSourceAccount, 1000.0);
        assertJournalEntry(entries, "CREDIT", incomeFromRecoveryAccount, 1000.0);
    }

    @Test
    public void testGoodwillCreditAfterChargeOffPostsToRecoveryIncome() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProduct();
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        final AtomicLong loanId = new AtomicLong(0L);
        BusinessDateHelper.runAt(currentDate.format(BUSINESS_DATE),
                () -> loanId.set(createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), currentDate)));

        final LocalDate chargeOffDate = currentDate.plusDays(1);
        BusinessDateHelper.runAt(chargeOffDate.format(BUSINESS_DATE), () -> loanHelper.chargeOffByLoanId(loanId.get(),
                WorkingCapitalLoanDisbursementTestBuilder.buildChargeOffRequest(chargeOffDate, null)));

        // Goodwill credit stays allowed after charge-off (parity with term and progressive loans). The debit side is
        // the
        // goodwill expense as usual; the credit side recognizes recovery income instead of the written-off portfolio.
        final LocalDate goodwillDate = chargeOffDate.plusDays(1);
        final AtomicLong goodwillTxnId = new AtomicLong(0L);
        BusinessDateHelper.runAt(goodwillDate.format(BUSINESS_DATE),
                () -> goodwillTxnId.set(loanHelper.makeGoodwillCreditByLoanId(loanId.get(), WorkingCapitalLoanDisbursementTestBuilder
                        .buildRepaymentRequest(goodwillDate, BigDecimal.valueOf(1000), null, null, null, null))));

        final List<JournalEntryTransactionItem> entries = getJournalEntriesForWCTransaction(goodwillTxnId.get());
        assertEquals(2, entries.size(), "Expected 2 journal entries (1 debit + 1 credit)");
        assertJournalEntry(entries, "DEBIT", goodwillCreditAccount, 1000.0);
        assertJournalEntry(entries, "CREDIT", incomeFromRecoveryAccount, 1000.0);
    }

    @Test
    public void testPayoutRefundAfterChargeOffReversesChargeOffExpense() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProduct();
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        final AtomicLong loanId = new AtomicLong(0L);
        BusinessDateHelper.runAt(currentDate.format(BUSINESS_DATE),
                () -> loanId.set(createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), currentDate)));

        final LocalDate chargeOffDate = currentDate.plusDays(1);
        BusinessDateHelper.runAt(chargeOffDate.format(BUSINESS_DATE), () -> loanHelper.chargeOffByLoanId(loanId.get(),
                WorkingCapitalLoanDisbursementTestBuilder.buildChargeOffRequest(chargeOffDate, null)));

        // Payout refund stays allowed after charge-off; its principal credit reverses the charge-off expense that the
        // charge-off recognized, instead of reducing the already written-off portfolio.
        final LocalDate refundDate = chargeOffDate.plusDays(1);
        final AtomicLong refundTxnId = new AtomicLong(0L);
        BusinessDateHelper.runAt(refundDate.format(BUSINESS_DATE),
                () -> refundTxnId.set(loanHelper.makePayoutRefundByLoanId(loanId.get(), WorkingCapitalLoanDisbursementTestBuilder
                        .buildRepaymentRequest(refundDate, BigDecimal.valueOf(1000), null, null, null, null))));

        final List<JournalEntryTransactionItem> entries = getJournalEntriesForWCTransaction(refundTxnId.get());
        assertEquals(2, entries.size(), "Expected 2 journal entries (1 debit + 1 credit)");
        assertJournalEntry(entries, "DEBIT", fundSourceAccount, 1000.0);
        assertJournalEntry(entries, "CREDIT", chargeOffExpenseAccount, 1000.0);
    }

    @Test
    public void testAddChargeAfterChargeOffIsRejected() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProduct();
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        final AtomicLong loanId = new AtomicLong(0L);
        BusinessDateHelper.runAt(currentDate.format(BUSINESS_DATE),
                () -> loanId.set(createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), currentDate)));

        final LocalDate chargeOffDate = currentDate.plusDays(1);
        BusinessDateHelper.runAt(chargeOffDate.format(BUSINESS_DATE), () -> {
            loanHelper.chargeOffByLoanId(loanId.get(),
                    WorkingCapitalLoanDisbursementTestBuilder.buildChargeOffRequest(chargeOffDate, null));

            // New charges cannot be added once the loan is charged off.
            final FeignWorkingCapitalLoanHelper feignLoanHelper = new FeignWorkingCapitalLoanHelper(
                    FineractFeignClientHelper.getFineractFeignClient());
            final Long chargeId = feignLoanHelper.createGlobalCharge(WorkingCapitalLoanRequestBuilders.specifiedDueDateCharge(false, 100));
            final CallFailedRuntimeException error = assertThrows(CallFailedRuntimeException.class,
                    () -> feignLoanHelper.addCharge(loanId.get(),
                            WorkingCapitalLoanRequestBuilders.addCharge(chargeId, 100, chargeOffDate.format(BUSINESS_DATE))));
            assertTrue(error.getMessage() != null && error.getMessage().contains("charged.off"),
                    "Expected charged-off rejection for addCharge, got: " + error.getMessage());
        });
    }

    @Test
    public void testChargeOffWithFutureDateFails() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProduct();
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        final AtomicLong loanId = new AtomicLong(0L);
        BusinessDateHelper.runAt(currentDate.format(BUSINESS_DATE), () -> {
            loanId.set(createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), currentDate));

            final CallFailedRuntimeException error = loanHelper.runChargeOffByLoanIdExpectingFailure(loanId.get(),
                    WorkingCapitalLoanDisbursementTestBuilder.buildChargeOffRequest(currentDate.plusDays(1), null));
            assertTrue(error.getMessage() != null && error.getMessage().contains("future.date"),
                    "Expected future-date validation error, got: " + error.getMessage());
        });
    }

    @Test
    public void testChargeOffDatedBeforeLastTransactionFails() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProduct();
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        final AtomicLong loanId = new AtomicLong(0L);
        BusinessDateHelper.runAt(currentDate.format(BUSINESS_DATE),
                () -> loanId.set(createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), currentDate)));

        final LocalDate repaymentDate = currentDate.plusDays(2);
        BusinessDateHelper.runAt(repaymentDate.format(BUSINESS_DATE), () -> {
            loanHelper.makeRepaymentByLoanId(loanId.get(), WorkingCapitalLoanDisbursementTestBuilder.buildRepaymentRequest(repaymentDate,
                    BigDecimal.valueOf(1000), null, null, null, null));

            // The charge-off can be backdated, but never behind the last (non-reversed) transaction.
            final CallFailedRuntimeException error = loanHelper.runChargeOffByLoanIdExpectingFailure(loanId.get(),
                    WorkingCapitalLoanDisbursementTestBuilder.buildChargeOffRequest(currentDate.plusDays(1), null));
            assertTrue(error.getMessage() != null && error.getMessage().contains("last.transaction"),
                    "Expected before-last-transaction validation error, got: " + error.getMessage());
        });
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Long createAccrualWithDeferredRevenueAmortizationProduct() {
        final String uniqueName = "WCL CoAcct " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
        final Long productId = productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder().withName(uniqueName)
                .withShortName(uniqueShortName).withAccountingRule(AccountingRuleEnum.ACC_DEF_REV_AM)
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
                .withDeferredIncomeLiabilityAccountId(deferredIncomeAccount.getAccountID().longValue())
                .withChargeOffExpenseAccountId(chargeOffExpenseAccount.getAccountID().longValue())
                .withIncomeFromChargeOffFeesAccountId(incomeFromChargeOffFeesAccount.getAccountID().longValue())
                .withIncomeFromChargeOffPenaltyAccountId(incomeFromChargeOffPenaltyAccount.getAccountID().longValue())
                .withGoodwillCreditAccountId(goodwillCreditAccount.getAccountID().longValue()).build()).getResourceId();
        createdProductIds.add(productId);
        return productId;
    }

    private Long createApprovedAndDisbursedLoan(final Long productId, final BigDecimal principal, final LocalDate approvedOnDate) {
        final Long loanId = submitAndTrack(new WorkingCapitalLoanApplicationTestBuilder().withClientId(createdClientId)
                .withProductId(productId).withPrincipal(principal)
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT).buildSubmitRequest());
        loanHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(approvedOnDate, principal, null));
        loanHelper.disburseById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(approvedOnDate, principal));
        return loanId;
    }

    private List<JournalEntryTransactionItem> getJournalEntriesForWCTransaction(final Long wcTransactionId) {
        final String transactionId = "WC" + wcTransactionId;
        final FeignJournalEntryHelper journalHelper = new FeignJournalEntryHelper(FineractFeignClientHelper.getFineractFeignClient());
        final GetJournalEntriesTransactionIdResponse response = journalHelper.getJournalEntriesByTransactionId(transactionId);
        if (response == null || response.getPageItems() == null) {
            return List.of();
        }
        return response.getPageItems();
    }

    private void assertJournalEntry(final List<JournalEntryTransactionItem> entries, final String expectedType,
            final Account expectedAccount, final double expectedAmount) {
        final boolean found = entries.stream().anyMatch(entry -> {
            assert entry != null;
            assert entry.getEntryType() != null;
            final boolean typeMatch = expectedType.equals(entry.getEntryType().getValue());
            final boolean accountMatch = expectedAccount.getAccountID().longValue() == entry.getGlAccountId();
            final boolean amountMatch = Double.compare(expectedAmount, entry.getAmount()) == 0;
            return typeMatch && accountMatch && amountMatch;
        });
        assertTrue(found, "Expected journal entry: " + expectedType + " " + expectedAccount.getAccountID() + " amount=" + expectedAmount
                + " not found in entries: " + entries.stream().map(e -> {
                    assert e.getEntryType() != null;
                    return e.getEntryType().getValue() + " acct=" + e.getGlAccountId() + " amt=" + e.getAmount();
                }).toList());
    }

    private Long submitAndTrack(final PostWorkingCapitalLoansRequest submitJson) {
        final Long loanId = loanHelper.submit(submitJson);
        createdLoanIds.add(loanId);
        return loanId;
    }
}
