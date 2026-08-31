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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetJournalEntriesTransactionIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsRequest.AccountingRuleEnum;
import org.apache.fineract.client.models.PostWorkingCapitalLoansRequest;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventResponse;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessDateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignExternalEventHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignJournalEntryHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalLoanRequestBuilders;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.accounting.Account;
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
public class WorkingCapitalLoanChargeOffAccountingTest extends FeignIntegrationTest {

    private static final DateTimeFormatter API_DATE = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    private static final String DISCOUNT_FEE_CODE = "loanTransactionType.discountFee";
    private static final String DISCOUNT_FEE_AMORTIZATION_CODE = "loanTransactionType.discountFeeAmortization";
    private static final String TRANSACTION_REVERSED_EVENT = "WorkingCapitalLoanAdjustTransactionBusinessEvent";
    private static final String DISCOUNT_FEE_AMORTIZATION_EVENT = "WorkingCapitalLoanDiscountFeeAmortizationTransactionBusinessEvent";

    // Fixed simulated calendar used by every test in this class (never a computed "now"): loans are disbursed on
    // DAY_1, charged off on DAY_2, and later days are used for whatever the individual test needs next.
    private static final LocalDate DAY_1 = LocalDate.of(2026, 1, 1);
    private static final LocalDate DAY_2 = LocalDate.of(2026, 1, 2);
    private static final LocalDate DAY_3 = LocalDate.of(2026, 1, 3);
    private static final LocalDate DAY_4 = LocalDate.of(2026, 1, 4);
    private static final LocalDate DAY_5 = LocalDate.of(2026, 1, 5);
    private static final LocalDate DAY_10 = LocalDate.of(2026, 1, 10);

    private final FeignWorkingCapitalLoanHelper loanHelper = new FeignWorkingCapitalLoanHelper(fineractClient());
    private final FeignBusinessDateHelper businessDateHelper = new FeignBusinessDateHelper(fineractClient());
    private final FeignExternalEventHelper externalEventHelper = new FeignExternalEventHelper(fineractClient());
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
        final Long[] loanIdHolder = new Long[1];
        businessDateHelper.runAt(DAY_1.toString(),
                () -> loanIdHolder[0] = createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), DAY_1));

        final Long[] chargeOffTxnIdHolder = new Long[1];
        businessDateHelper.runAt(DAY_2.toString(), () -> chargeOffTxnIdHolder[0] = loanHelper.chargeOff(loanIdHolder[0],
                WorkingCapitalLoanRequestBuilders.chargeOff(DAY_2.format(API_DATE), "charge-off note")));

        // The loan stays ACTIVE and is flagged as charged off (pure accounting tag, no portfolio impact).
        final GetWorkingCapitalLoansLoanIdResponse loanData = loanHelper.getLoanDetails(loanIdHolder[0]);
        assertNotNull(loanData.getStatus());
        assertEquals("loanStatusType.active", loanData.getStatus().getCode());
        assertEquals(Boolean.TRUE, loanData.getChargedOff());

        // Only principal was outstanding (no fees/penalties): Dr Charge-off expense 5000, Cr Loan portfolio 5000.
        final List<JournalEntryTransactionItem> entries = getJournalEntriesForWCTransaction(chargeOffTxnIdHolder[0]);
        assertEquals(2, entries.size(), "Expected 2 journal entries (1 debit + 1 credit)");
        assertJournalEntry(entries, "DEBIT", chargeOffExpenseAccount, 5000.0);
        assertJournalEntry(entries, "CREDIT", loanPortfolioAccount, 5000.0);
    }

    @Test
    public void testUndoChargeOffReversesJournalEntriesAndClearsTag() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProduct();
        final Long[] loanIdHolder = new Long[1];
        businessDateHelper.runAt(DAY_1.toString(),
                () -> loanIdHolder[0] = createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), DAY_1));

        final Long[] chargeOffTxnIdHolder = new Long[1];
        businessDateHelper.runAt(DAY_2.toString(), () -> chargeOffTxnIdHolder[0] = loanHelper.chargeOff(loanIdHolder[0],
                WorkingCapitalLoanRequestBuilders.chargeOff(DAY_2.format(API_DATE), null)));

        businessDateHelper.runAt(DAY_3.toString(),
                () -> loanHelper.undoChargeOff(loanIdHolder[0], WorkingCapitalLoanRequestBuilders.undoChargeOff("undo note")));

        // The charge-off tag is cleared and the loan stays ACTIVE.
        final GetWorkingCapitalLoansLoanIdResponse loanData = loanHelper.getLoanDetails(loanIdHolder[0]);
        assertNotNull(loanData.getStatus());
        assertEquals("loanStatusType.active", loanData.getStatus().getCode());
        assertNotEquals(Boolean.TRUE, loanData.getChargedOff());

        // The original 2 entries are reversed: querying the charge-off transaction now returns the originals plus their
        // reversals (Dr Loan portfolio 5000, Cr Charge-off expense 5000).
        final List<JournalEntryTransactionItem> entries = getJournalEntriesForWCTransaction(chargeOffTxnIdHolder[0]);
        assertEquals(4, entries.size(), "Expected 4 journal entries (2 original + 2 reversal)");
        assertJournalEntry(entries, "DEBIT", loanPortfolioAccount, 5000.0);
        assertJournalEntry(entries, "CREDIT", chargeOffExpenseAccount, 5000.0);
    }

    @Test
    public void testSecondChargeOffFails() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProduct();
        final Long[] loanIdHolder = new Long[1];
        businessDateHelper.runAt(DAY_1.toString(),
                () -> loanIdHolder[0] = createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), DAY_1));

        businessDateHelper.runAt(DAY_2.toString(), () -> {
            loanHelper.chargeOff(loanIdHolder[0], WorkingCapitalLoanRequestBuilders.chargeOff(DAY_2.format(API_DATE), null));
            final CallFailedRuntimeException error = loanHelper.chargeOffExpectingFailure(loanIdHolder[0],
                    WorkingCapitalLoanRequestBuilders.chargeOff(DAY_2.format(API_DATE), null));
            assertTrue(error.getMessage() != null && error.getMessage().contains("already.charged.off"),
                    "Expected already-charged-off validation error, got: " + error.getMessage());
        });
    }

    @Test
    public void testChargeOffWithNoAccountingCreatesNoJournalEntries() {
        final Long[] loanIdHolder = new Long[1];
        businessDateHelper.runAt(DAY_1.toString(), () -> {
            final String uniqueName = "WCL CoNoAcct " + UUID.randomUUID().toString().substring(0, 8);
            final String uniqueShortName = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
            final Long productId = productHelper
                    .createWorkingCapitalLoanProduct(
                            new WorkingCapitalLoanProductTestBuilder().withName(uniqueName).withShortName(uniqueShortName).build())
                    .getResourceId();
            createdProductIds.add(productId);
            loanIdHolder[0] = createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), DAY_1);
        });

        final Long[] chargeOffTxnIdHolder = new Long[1];
        businessDateHelper.runAt(DAY_2.toString(), () -> chargeOffTxnIdHolder[0] = loanHelper.chargeOff(loanIdHolder[0],
                WorkingCapitalLoanRequestBuilders.chargeOff(DAY_2.format(API_DATE), null)));

        final List<JournalEntryTransactionItem> entries = getJournalEntriesForWCTransaction(chargeOffTxnIdHolder[0]);
        assertTrue(entries.isEmpty(), "Expected no journal entries for NONE accounting rule");
    }

    @Test
    public void testRepaymentAfterChargeOffPostsToRecoveryIncome() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProduct();
        final Long[] loanIdHolder = new Long[1];
        businessDateHelper.runAt(DAY_1.toString(),
                () -> loanIdHolder[0] = createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), DAY_1));

        businessDateHelper.runAt(DAY_2.toString(),
                () -> loanHelper.chargeOff(loanIdHolder[0], WorkingCapitalLoanRequestBuilders.chargeOff(DAY_2.format(API_DATE), null)));

        // Repayments stay allowed after charge-off (the balance can be cured); their credits are routed to recovery
        // income instead of the loan portfolio / receivables.
        final Long[] repaymentTxnIdHolder = new Long[1];
        businessDateHelper.runAt(DAY_3.toString(), () -> repaymentTxnIdHolder[0] = loanHelper.makeRepayment(loanIdHolder[0],
                WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(1000), DAY_3.format(API_DATE))));

        final List<JournalEntryTransactionItem> entries = getJournalEntriesForWCTransaction(repaymentTxnIdHolder[0]);
        assertEquals(2, entries.size(), "Expected 2 journal entries (1 debit + 1 credit)");
        assertJournalEntry(entries, "DEBIT", fundSourceAccount, 1000.0);
        assertJournalEntry(entries, "CREDIT", incomeFromRecoveryAccount, 1000.0);
    }

    @Test
    public void testGoodwillCreditAfterChargeOffPostsToRecoveryIncome() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProduct();
        final Long[] loanIdHolder = new Long[1];
        businessDateHelper.runAt(DAY_1.toString(),
                () -> loanIdHolder[0] = createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), DAY_1));

        businessDateHelper.runAt(DAY_2.toString(),
                () -> loanHelper.chargeOff(loanIdHolder[0], WorkingCapitalLoanRequestBuilders.chargeOff(DAY_2.format(API_DATE), null)));

        // Goodwill credit stays allowed after charge-off (parity with term and progressive loans). The debit side is
        // the goodwill expense as usual; the credit side recognizes recovery income instead of the written-off
        // portfolio.
        final Long[] goodwillTxnIdHolder = new Long[1];
        businessDateHelper.runAt(DAY_3.toString(), () -> goodwillTxnIdHolder[0] = loanHelper.makeGoodwillCredit(loanIdHolder[0],
                WorkingCapitalLoanRequestBuilders.goodwillCredit(BigDecimal.valueOf(1000), DAY_3.format(API_DATE))));

        final List<JournalEntryTransactionItem> entries = getJournalEntriesForWCTransaction(goodwillTxnIdHolder[0]);
        assertEquals(2, entries.size(), "Expected 2 journal entries (1 debit + 1 credit)");
        assertJournalEntry(entries, "DEBIT", goodwillCreditAccount, 1000.0);
        assertJournalEntry(entries, "CREDIT", incomeFromRecoveryAccount, 1000.0);
    }

    @Test
    public void testPayoutRefundAfterChargeOffReversesChargeOffExpense() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProduct();
        final Long[] loanIdHolder = new Long[1];
        businessDateHelper.runAt(DAY_1.toString(),
                () -> loanIdHolder[0] = createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), DAY_1));

        businessDateHelper.runAt(DAY_2.toString(),
                () -> loanHelper.chargeOff(loanIdHolder[0], WorkingCapitalLoanRequestBuilders.chargeOff(DAY_2.format(API_DATE), null)));

        // Payout refund stays allowed after charge-off; its principal credit reverses the charge-off expense that the
        // charge-off recognized, instead of reducing the already written-off portfolio.
        final Long[] refundTxnIdHolder = new Long[1];
        businessDateHelper.runAt(DAY_3.toString(), () -> refundTxnIdHolder[0] = loanHelper.payoutRefund(loanIdHolder[0],
                WorkingCapitalLoanRequestBuilders.payoutRefund(BigDecimal.valueOf(1000), DAY_3.format(API_DATE))));

        final List<JournalEntryTransactionItem> entries = getJournalEntriesForWCTransaction(refundTxnIdHolder[0]);
        assertEquals(2, entries.size(), "Expected 2 journal entries (1 debit + 1 credit)");
        assertJournalEntry(entries, "DEBIT", fundSourceAccount, 1000.0);
        assertJournalEntry(entries, "CREDIT", chargeOffExpenseAccount, 1000.0);
    }

    @Test
    public void testUndoOfRepaymentAfterChargeOffReversesRecoveryIncomeEntries() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProduct();
        final Long[] loanIdHolder = new Long[1];
        businessDateHelper.runAt(DAY_1.toString(),
                () -> loanIdHolder[0] = createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), DAY_1));

        businessDateHelper.runAt(DAY_2.toString(),
                () -> loanHelper.chargeOff(loanIdHolder[0], WorkingCapitalLoanRequestBuilders.chargeOff(DAY_2.format(API_DATE), null)));

        final Long[] repaymentTxnIdHolder = new Long[1];
        businessDateHelper.runAt(DAY_3.toString(), () -> repaymentTxnIdHolder[0] = loanHelper.makeRepayment(loanIdHolder[0],
                WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(1000), DAY_3.format(API_DATE))));

        // Undo the repayment that was posted after charge-off: the recovery-income entries it booked must be
        // reversed exactly like any other transaction's entries, not left dangling on the ledger.
        businessDateHelper.runAt(DAY_4.toString(), () -> loanHelper.undoTransaction(loanIdHolder[0], repaymentTxnIdHolder[0]));

        // The loan is still charged off - only the repayment was undone, not the charge-off itself.
        final GetWorkingCapitalLoansLoanIdResponse loanData = loanHelper.getLoanDetails(loanIdHolder[0]);
        assertEquals(Boolean.TRUE, loanData.getChargedOff());

        final List<JournalEntryTransactionItem> entries = getJournalEntriesForWCTransaction(repaymentTxnIdHolder[0]);
        assertEquals(4, entries.size(), "Expected 4 journal entries (2 original + 2 reversal)");
        // Originals: Dr Fund source 1000, Cr Income from recovery 1000. Reversal mirrors flip both sides.
        assertJournalEntry(entries, "DEBIT", fundSourceAccount, 1000.0);
        assertJournalEntry(entries, "CREDIT", incomeFromRecoveryAccount, 1000.0);
        assertJournalEntry(entries, "CREDIT", fundSourceAccount, 1000.0);
        assertJournalEntry(entries, "DEBIT", incomeFromRecoveryAccount, 1000.0);
    }

    @Test
    public void testChargeAdjustmentAfterChargeOffPostsToChargeOffFeeIncome() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProduct();
        final Long[] loanIdHolder = new Long[1];
        final Long[] feeChargeIdHolder = new Long[1];
        businessDateHelper.runAt(DAY_1.toString(), () -> {
            loanIdHolder[0] = createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), DAY_1);

            // The fee charge must be added before charge-off: new charges are rejected once the loan is charged off.
            final Long chargeId = loanHelper.createGlobalCharge(WorkingCapitalLoanRequestBuilders.specifiedDueDateCharge(false, 300));
            feeChargeIdHolder[0] = loanHelper.addCharge(loanIdHolder[0],
                    WorkingCapitalLoanRequestBuilders.addCharge(chargeId, 300, DAY_1.format(API_DATE)));
        });

        businessDateHelper.runAt(DAY_2.toString(),
                () -> loanHelper.chargeOff(loanIdHolder[0], WorkingCapitalLoanRequestBuilders.chargeOff(DAY_2.format(API_DATE), null)));

        // A charge adjustment on a charged-off loan still debits the fee's own income account, but the credit
        // recognizes charge-off fee income instead of the (already written-off) fees receivable.
        final Long[] adjustmentTxnIdHolder = new Long[1];
        businessDateHelper.runAt(DAY_3.toString(), () -> adjustmentTxnIdHolder[0] = loanHelper.adjustCharge(loanIdHolder[0],
                feeChargeIdHolder[0], WorkingCapitalLoanRequestBuilders.chargeAdjustment(BigDecimal.valueOf(100))));

        final List<JournalEntryTransactionItem> entries = getJournalEntriesForWCTransaction(adjustmentTxnIdHolder[0]);
        assertEquals(2, entries.size(), "Expected 2 journal entries (1 debit + 1 credit)");
        assertJournalEntry(entries, "DEBIT", incomeFromFeeAccount, 100.0);
        assertJournalEntry(entries, "CREDIT", incomeFromChargeOffFeesAccount, 100.0);
    }

    @Test
    public void testAddChargeAfterChargeOffIsRejected() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProduct();
        final Long[] loanIdHolder = new Long[1];
        businessDateHelper.runAt(DAY_1.toString(),
                () -> loanIdHolder[0] = createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), DAY_1));

        businessDateHelper.runAt(DAY_2.toString(), () -> {
            loanHelper.chargeOff(loanIdHolder[0], WorkingCapitalLoanRequestBuilders.chargeOff(DAY_2.format(API_DATE), null));

            // New charges cannot be added once the loan is charged off.
            final Long chargeId = loanHelper.createGlobalCharge(WorkingCapitalLoanRequestBuilders.specifiedDueDateCharge(false, 100));
            final CallFailedRuntimeException error = assertThrows(CallFailedRuntimeException.class, () -> loanHelper
                    .addCharge(loanIdHolder[0], WorkingCapitalLoanRequestBuilders.addCharge(chargeId, 100, DAY_2.format(API_DATE))));
            assertTrue(error.getMessage() != null && error.getMessage().contains("charged.off"),
                    "Expected charged-off rejection for addCharge, got: " + error.getMessage());
        });
    }

    @Test
    public void testChargeOffWithFutureDateFails() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProduct();
        businessDateHelper.runAt(DAY_1.toString(), () -> {
            final Long loanId = createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), DAY_1);

            // Business date stays at DAY_1, so a charge-off dated DAY_2 is a future date.
            final CallFailedRuntimeException error = loanHelper.chargeOffExpectingFailure(loanId,
                    WorkingCapitalLoanRequestBuilders.chargeOff(DAY_2.format(API_DATE), null));
            assertTrue(error.getMessage() != null && error.getMessage().contains("future.date"),
                    "Expected future-date validation error, got: " + error.getMessage());
        });
    }

    @Test
    public void testChargeOffDatedBeforeLastTransactionFails() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProduct();
        final Long[] loanIdHolder = new Long[1];
        businessDateHelper.runAt(DAY_1.toString(),
                () -> loanIdHolder[0] = createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), DAY_1));

        businessDateHelper.runAt(DAY_3.toString(), () -> {
            loanHelper.makeRepayment(loanIdHolder[0],
                    WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(1000), DAY_3.format(API_DATE)));

            // The charge-off can be backdated, but never behind the last (non-reversed) transaction: DAY_2 predates
            // the DAY_3 repayment above.
            final CallFailedRuntimeException error = loanHelper.chargeOffExpectingFailure(loanIdHolder[0],
                    WorkingCapitalLoanRequestBuilders.chargeOff(DAY_2.format(API_DATE), null));
            assertTrue(error.getMessage() != null && error.getMessage().contains("last.transaction"),
                    "Expected before-last-transaction validation error, got: " + error.getMessage());
        });
    }

    @Test
    public void testChargeOffAmortizesRemainingUnreleasedDiscountFeeToChargeOffExpense() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProductWithDiscountOverride();
        final BigDecimal principal = BigDecimal.valueOf(5000);
        final BigDecimal discount = BigDecimal.valueOf(500);
        final Long[] loanIdHolder = new Long[1];
        businessDateHelper.runAt(DAY_1.toString(),
                () -> loanIdHolder[0] = createApprovedAndDisbursedLoanWithDiscount(productId, principal, discount, DAY_1));

        businessDateHelper.runAt(DAY_2.toString(), () -> {
            loanHelper.executeInlineWCCOB(loanIdHolder[0]);
            assertTrue(filterByType(loanHelper.getTransactions(loanIdHolder[0]), DISCOUNT_FEE_AMORTIZATION_CODE).isEmpty(),
                    "No periodic amortization should have posted before any repayment");

            loanHelper.chargeOff(loanIdHolder[0], WorkingCapitalLoanRequestBuilders.chargeOff(DAY_2.format(API_DATE), null));
        });

        // The entire unreleased discount fee is recognized in one shot, dated on the charge-off date.
        final List<GetWorkingCapitalLoanTransactionIdResponse> amortizations = filterByType(loanHelper.getTransactions(loanIdHolder[0]),
                DISCOUNT_FEE_AMORTIZATION_CODE);
        assertEquals(1, amortizations.size(), "Expected exactly 1 discount fee amortization transaction");
        final GetWorkingCapitalLoanTransactionIdResponse amortTxn = amortizations.getFirst();
        assertEquals(DAY_2, amortTxn.getTransactionDate());
        assertEquals(0, discount.compareTo(amortTxn.getTransactionAmount()),
                "Expected the full discount to be recognized at charge-off, was: " + amortTxn.getTransactionAmount());

        // ...credited to charge-off expense, not discount-fee income.
        final List<JournalEntryTransactionItem> entries = getJournalEntriesForWCTransaction(amortTxn.getId());
        assertEquals(2, entries.size(), "Expected 2 journal entries (1 debit + 1 credit)");
        assertJournalEntry(entries, "DEBIT", deferredIncomeAccount, discount.doubleValue());
        assertJournalEntry(entries, "CREDIT", chargeOffExpenseAccount, discount.doubleValue());

        // No further amortization happens once charged off, even if COB keeps running: this run (business date DAY_3)
        // processes DAY_2, the charge-off date itself, and must not create a second amortization transaction.
        businessDateHelper.runAt(DAY_10.toString(), () -> loanHelper.executeInlineWCCOB(loanIdHolder[0]));
        assertEquals(1, filterByType(loanHelper.getTransactions(loanIdHolder[0]), DISCOUNT_FEE_AMORTIZATION_CODE).size(),
                "COB after charge-off must not create additional discount fee amortization transactions");
    }

    @Test
    public void testUndoChargeOffReversesDiscountFeeAmortizationAndResumesPeriodicAmortization() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProductWithDiscountOverride();
        final BigDecimal principal = BigDecimal.valueOf(5000);
        final BigDecimal discount = BigDecimal.valueOf(500);
        final Long[] loanIdHolder = new Long[1];
        businessDateHelper.runAt(DAY_1.toString(),
                () -> loanIdHolder[0] = createApprovedAndDisbursedLoanWithDiscount(productId, principal, discount, DAY_1));

        businessDateHelper.runAt(DAY_2.toString(),
                () -> loanHelper.chargeOff(loanIdHolder[0], WorkingCapitalLoanRequestBuilders.chargeOff(DAY_2.format(API_DATE), null)));

        final List<GetWorkingCapitalLoanTransactionIdResponse> amortizations = filterByType(loanHelper.getTransactions(loanIdHolder[0]),
                DISCOUNT_FEE_AMORTIZATION_CODE);
        assertEquals(1, amortizations.size(), "Expected exactly 1 discount fee amortization transaction");
        final Long amortTxnId = amortizations.getFirst().getId();

        businessDateHelper.runAt(DAY_3.toString(),
                () -> loanHelper.undoChargeOff(loanIdHolder[0], WorkingCapitalLoanRequestBuilders.undoChargeOff(null)));

        // The final amortization transaction created at charge-off is now reversed...
        final GetWorkingCapitalLoanTransactionIdResponse reversedAmortTxn = loanHelper.getTransaction(loanIdHolder[0], amortTxnId);
        assertEquals(Boolean.TRUE, reversedAmortTxn.getReversed(),
                "Expected the discount fee amortization transaction to be reversed after undo charge-off");

        // ...and its journal entries are reversed too (2 original + 2 reversal).
        final List<JournalEntryTransactionItem> entries = getJournalEntriesForWCTransaction(amortTxnId);
        assertEquals(4, entries.size(), "Expected 4 journal entries (2 original + 2 reversal)");
        assertJournalEntry(entries, "DEBIT", deferredIncomeAccount, discount.doubleValue());
        assertJournalEntry(entries, "CREDIT", chargeOffExpenseAccount, discount.doubleValue());
        assertJournalEntry(entries, "CREDIT", deferredIncomeAccount, discount.doubleValue());
        assertJournalEntry(entries, "DEBIT", chargeOffExpenseAccount, discount.doubleValue());

        final GetWorkingCapitalLoansLoanIdResponse loanData = loanHelper.getLoanDetails(loanIdHolder[0]);
        assertNotEquals(Boolean.TRUE, loanData.getChargedOff());

        businessDateHelper.runAt(DAY_4.toString(), () -> loanHelper.makeRepayment(loanIdHolder[0],
                WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(1000), DAY_4.format(API_DATE))));
        businessDateHelper.runAt(DAY_5.toString(), () -> loanHelper.executeInlineWCCOB(loanIdHolder[0]));

        final List<GetWorkingCapitalLoanTransactionIdResponse> resumedAmortizations = filterByType(
                loanHelper.getTransactions(loanIdHolder[0]), DISCOUNT_FEE_AMORTIZATION_CODE);
        assertEquals(1, resumedAmortizations.size(),
                "Expected exactly 1 non-reversed amortization transaction after undo + repayment + COB");
        final GetWorkingCapitalLoanTransactionIdResponse resumedAmortTxn = resumedAmortizations.getFirst();
        assertNotNull(resumedAmortTxn.getTransactionAmount());
        assertTrue(resumedAmortTxn.getTransactionAmount().compareTo(BigDecimal.ZERO) > 0, "Resumed amortization should be positive");

        final List<JournalEntryTransactionItem> resumedEntries = getJournalEntriesForWCTransaction(resumedAmortTxn.getId());
        assertJournalEntry(resumedEntries, "CREDIT", incomeFromDiscountFeeAccount, resumedAmortTxn.getTransactionAmount().doubleValue());
    }

    @Test
    public void testFinalDiscountFeeAmortizationReversalPublishesBusinessEvent() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProductWithDiscountOverride();
        final BigDecimal principal = BigDecimal.valueOf(5000);
        final BigDecimal discount = BigDecimal.valueOf(500);
        final Long[] loanIdHolder = new Long[1];
        final Long[] discountTxnIdHolder = new Long[1];
        businessDateHelper.runAt(DAY_1.toString(), () -> {
            loanIdHolder[0] = createApprovedAndDisbursedLoanWithDiscount(productId, principal, discount, DAY_1);
            discountTxnIdHolder[0] = filterByType(loanHelper.getTransactions(loanIdHolder[0]), DISCOUNT_FEE_CODE).getFirst().getId();
        });

        businessDateHelper.runAt(DAY_2.toString(),
                () -> loanHelper.chargeOff(loanIdHolder[0], WorkingCapitalLoanRequestBuilders.chargeOff(DAY_2.format(API_DATE), null)));

        final List<GetWorkingCapitalLoanTransactionIdResponse> amortizations = filterByType(loanHelper.getTransactions(loanIdHolder[0]),
                DISCOUNT_FEE_AMORTIZATION_CODE);
        assertEquals(1, amortizations.size(), "Expected exactly 1 discount fee amortization transaction");
        final Long amortTxnId = amortizations.getFirst().getId();

        externalEventHelper.enableBusinessEvent(TRANSACTION_REVERSED_EVENT);
        try {
            businessDateHelper.runAt(DAY_3.toString(), () -> {
                externalEventHelper.deleteAllExternalEvents();
                // A backdated (pre-charge-off) discount fee adjustment for the entire discount zeroes the pool, so
                // reprocessing must reverse the final amortization the charge-off created.
                loanHelper.makeDiscountFeeAdjustment(loanIdHolder[0],
                        WorkingCapitalLoanRequestBuilders.discountFeeAdjustment(discountTxnIdHolder[0], discount, DAY_1.format(API_DATE)));
            });

            final GetWorkingCapitalLoanTransactionIdResponse reversedAmortTxn = loanHelper.getTransaction(loanIdHolder[0], amortTxnId);
            assertEquals(Boolean.TRUE, reversedAmortTxn.getReversed(),
                    "Expected the final discount fee amortization to be reversed once the backdated adjustment zeroes the pool");

            final List<ExternalEventResponse> events = externalEventHelper.getExternalEventsByType(TRANSACTION_REVERSED_EVENT);
            final ExternalEventResponse event = events.stream()
                    .filter(e -> amortTxnId.equals(toLong(((HashMap) e.getPayLoad().get("transactionToAdjust")).get("id")))).findFirst()
                    .orElse(null);
            assertNotNull(event,
                    "Expected a " + TRANSACTION_REVERSED_EVENT + " for the reversed final amortization transaction " + amortTxnId);
            assertEquals(Boolean.TRUE, ((HashMap) event.getPayLoad().get("transactionToAdjust")).get("reversed"));
        } finally {
            externalEventHelper.disableBusinessEvent(TRANSACTION_REVERSED_EVENT);
        }
    }

    @Test
    public void testDiscountFeeAmortizationPlainRestatementPublishesBusinessEvent() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProductWithDiscountOverride();
        final BigDecimal principal = BigDecimal.valueOf(5000);
        final BigDecimal discount = BigDecimal.valueOf(500);
        final BigDecimal partialAdjustment = BigDecimal.valueOf(200);
        final BigDecimal expectedRestatedAmount = discount.subtract(partialAdjustment);
        final Long[] loanIdHolder = new Long[1];
        final Long[] discountTxnIdHolder = new Long[1];
        businessDateHelper.runAt(DAY_1.toString(), () -> {
            loanIdHolder[0] = createApprovedAndDisbursedLoanWithDiscount(productId, principal, discount, DAY_1);
            discountTxnIdHolder[0] = filterByType(loanHelper.getTransactions(loanIdHolder[0]), DISCOUNT_FEE_CODE).getFirst().getId();
        });

        businessDateHelper.runAt(DAY_2.toString(),
                () -> loanHelper.chargeOff(loanIdHolder[0], WorkingCapitalLoanRequestBuilders.chargeOff(DAY_2.format(API_DATE), null)));

        final List<GetWorkingCapitalLoanTransactionIdResponse> amortizations = filterByType(loanHelper.getTransactions(loanIdHolder[0]),
                DISCOUNT_FEE_AMORTIZATION_CODE);
        assertEquals(1, amortizations.size(), "Expected exactly 1 discount fee amortization transaction");
        final Long amortTxnId = amortizations.getFirst().getId();

        externalEventHelper.enableBusinessEvent(DISCOUNT_FEE_AMORTIZATION_EVENT);
        try {
            businessDateHelper.runAt(DAY_3.toString(), () -> {
                externalEventHelper.deleteAllExternalEvents();
                // A backdated (pre-charge-off) discount fee adjustment for PART of the discount shrinks the pool
                // without ever zeroing it out: the final amortization is restated in place, never reversed.
                loanHelper.makeDiscountFeeAdjustment(loanIdHolder[0], WorkingCapitalLoanRequestBuilders
                        .discountFeeAdjustment(discountTxnIdHolder[0], partialAdjustment, DAY_1.format(API_DATE)));
            });

            final GetWorkingCapitalLoanTransactionIdResponse restatedAmortTxn = loanHelper.getTransaction(loanIdHolder[0], amortTxnId);
            assertNotEquals(Boolean.TRUE, restatedAmortTxn.getReversed(), "Expected the final amortization to stay non-reversed");
            FeignWorkingCapitalLoanHelper.assertEqualBigDecimal(expectedRestatedAmount, restatedAmortTxn.getTransactionAmount(),
                    "Expected the final amortization amount to be restated down to the remaining discount");

            final List<ExternalEventResponse> events = externalEventHelper.getExternalEventsByType(DISCOUNT_FEE_AMORTIZATION_EVENT);
            final ExternalEventResponse event = events.stream().filter(e -> amortTxnId.equals(toLong(e.getPayLoad().get("id")))).findFirst()
                    .orElse(null);
            assertNotNull(event,
                    "Expected a " + DISCOUNT_FEE_AMORTIZATION_EVENT + " for the restated amortization transaction " + amortTxnId);
            assertEquals(Boolean.FALSE, event.getPayLoad().get("reversed"));
            FeignWorkingCapitalLoanHelper.assertEqualBigDecimal(expectedRestatedAmount,
                    toBigDecimal(event.getPayLoad().get("transactionAmount")), "Expected the event payload to carry the restated amount");
        } finally {
            externalEventHelper.disableBusinessEvent(DISCOUNT_FEE_AMORTIZATION_EVENT);
        }
    }

    @Test
    public void testDiscountFeeAmortizationRevivalPublishesBusinessEvent() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProductWithDiscountOverride();
        final BigDecimal principal = BigDecimal.valueOf(5000);
        final BigDecimal discount = BigDecimal.valueOf(500);
        final Long[] loanIdHolder = new Long[1];
        final Long[] discountTxnIdHolder = new Long[1];
        businessDateHelper.runAt(DAY_1.toString(), () -> {
            loanIdHolder[0] = createApprovedAndDisbursedLoanWithDiscount(productId, principal, discount, DAY_1);
            discountTxnIdHolder[0] = filterByType(loanHelper.getTransactions(loanIdHolder[0]), DISCOUNT_FEE_CODE).getFirst().getId();
        });

        businessDateHelper.runAt(DAY_2.toString(),
                () -> loanHelper.chargeOff(loanIdHolder[0], WorkingCapitalLoanRequestBuilders.chargeOff(DAY_2.format(API_DATE), null)));

        final List<GetWorkingCapitalLoanTransactionIdResponse> amortizations = filterByType(loanHelper.getTransactions(loanIdHolder[0]),
                DISCOUNT_FEE_AMORTIZATION_CODE);
        assertEquals(1, amortizations.size(), "Expected exactly 1 discount fee amortization transaction");
        final Long amortTxnId = amortizations.getFirst().getId();

        // A backdated (pre-charge-off) discount fee adjustment for the entire discount zeroes the pool and reverses
        // the final amortization (covered by testFinalDiscountFeeAmortizationReversalPublishesBusinessEvent).
        final Long[] adjustmentTxnIdHolder = new Long[1];
        businessDateHelper.runAt(DAY_3.toString(), () -> adjustmentTxnIdHolder[0] = loanHelper.makeDiscountFeeAdjustment(loanIdHolder[0],
                WorkingCapitalLoanRequestBuilders.discountFeeAdjustment(discountTxnIdHolder[0], discount, DAY_1.format(API_DATE))));
        assertEquals(Boolean.TRUE, loanHelper.getTransaction(loanIdHolder[0], amortTxnId).getReversed(),
                "Expected the final amortization to be reversed once the pool is fully depleted");

        externalEventHelper.enableBusinessEvent(DISCOUNT_FEE_AMORTIZATION_EVENT);
        try {
            businessDateHelper.runAt(DAY_4.toString(), () -> {
                externalEventHelper.deleteAllExternalEvents();
                // Undoing the depleting adjustment restores the pool: the same (previously reversed) final
                // amortization transaction must be revived rather than left behind for good.
                loanHelper.undoTransaction(loanIdHolder[0], adjustmentTxnIdHolder[0]);
            });

            final GetWorkingCapitalLoanTransactionIdResponse revivedAmortTxn = loanHelper.getTransaction(loanIdHolder[0], amortTxnId);
            assertNotEquals(Boolean.TRUE, revivedAmortTxn.getReversed(), "Expected the final amortization to be revived (non-reversed)");
            FeignWorkingCapitalLoanHelper.assertEqualBigDecimal(discount, revivedAmortTxn.getTransactionAmount(),
                    "Expected the revived amortization amount to be restored to the full discount");

            final List<ExternalEventResponse> events = externalEventHelper.getExternalEventsByType(DISCOUNT_FEE_AMORTIZATION_EVENT);
            final ExternalEventResponse event = events.stream().filter(e -> amortTxnId.equals(toLong(e.getPayLoad().get("id")))).findFirst()
                    .orElse(null);
            assertNotNull(event,
                    "Expected a " + DISCOUNT_FEE_AMORTIZATION_EVENT + " for the revived amortization transaction " + amortTxnId);
            assertEquals(Boolean.FALSE, event.getPayLoad().get("reversed"));
            FeignWorkingCapitalLoanHelper.assertEqualBigDecimal(discount, toBigDecimal(event.getPayLoad().get("transactionAmount")),
                    "Expected the event payload to carry the revived amount");
        } finally {
            externalEventHelper.disableBusinessEvent(DISCOUNT_FEE_AMORTIZATION_EVENT);
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Long createApprovedAndDisbursedLoanWithDiscount(final Long productId, final BigDecimal principal, final BigDecimal discount,
            final LocalDate approvedOnDate) {
        final String dateStr = approvedOnDate.format(API_DATE);
        final Long loanId = submitAndTrack(
                WorkingCapitalLoanRequestBuilders
                        .submitApplication(createdClientId, productId, principal,
                                WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT, dateStr, dateStr)
                        .discount(discount));
        loanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approveWithDiscount(dateStr, principal, dateStr, discount));
        loanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburseWithDiscount(dateStr, principal, discount));
        return loanId;
    }

    private List<GetWorkingCapitalLoanTransactionIdResponse> filterByType(
            final List<GetWorkingCapitalLoanTransactionIdResponse> transactions, final String typeCode) {
        return transactions.stream()
                .filter(txn -> txn.getType() != null && typeCode.equals(txn.getType().getCode()) && !Boolean.TRUE.equals(txn.getReversed()))
                .toList();
    }

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

    /**
     * Same GL setup as {@link #createAccrualWithDeferredRevenueAmortizationProduct()}, but also allows the discount
     * amount to be overridden per loan (required to submit/approve/disburse with a discount at all).
     */
    private Long createAccrualWithDeferredRevenueAmortizationProductWithDiscountOverride() {
        final String uniqueName = "WCL CoDiscAcct " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
        final Long productId = productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder().withName(uniqueName)
                .withShortName(uniqueShortName).withAllowAttributeOverrides(Map.of("discountDefault", Boolean.TRUE))
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
                .withDeferredIncomeLiabilityAccountId(deferredIncomeAccount.getAccountID().longValue())
                .withChargeOffExpenseAccountId(chargeOffExpenseAccount.getAccountID().longValue())
                .withIncomeFromChargeOffFeesAccountId(incomeFromChargeOffFeesAccount.getAccountID().longValue())
                .withIncomeFromChargeOffPenaltyAccountId(incomeFromChargeOffPenaltyAccount.getAccountID().longValue())
                .withGoodwillCreditAccountId(goodwillCreditAccount.getAccountID().longValue()).build()).getResourceId();
        createdProductIds.add(productId);
        return productId;
    }

    private Long createApprovedAndDisbursedLoan(final Long productId, final BigDecimal principal, final LocalDate approvedOnDate) {
        final String dateStr = approvedOnDate.format(API_DATE);
        final Long loanId = submitAndTrack(WorkingCapitalLoanRequestBuilders.submitApplication(createdClientId, productId, principal,
                WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT, dateStr, dateStr));
        loanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approve(dateStr, principal, dateStr));
        loanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburse(dateStr, principal));
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

    private static Long toLong(final Object value) {
        return value instanceof Number number ? number.longValue() : null;
    }

    /** Avro {@code bigdecimal} fields surface as either a JSON number or a string, depending on the codec. */
    private static BigDecimal toBigDecimal(final Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return new BigDecimal(number.toString());
        }
        return new BigDecimal(value.toString());
    }

    private Long submitAndTrack(final PostWorkingCapitalLoansRequest submitJson) {
        final Long loanId = loanHelper.submitApplication(submitJson);
        createdLoanIds.add(loanId);
        return loanId;
    }
}
