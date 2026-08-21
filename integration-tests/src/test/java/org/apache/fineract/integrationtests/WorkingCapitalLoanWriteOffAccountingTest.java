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
import org.apache.fineract.client.models.PostWorkingCapitalLoansLoanIdRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansRequest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCodeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignJournalEntryHelper;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanDisbursementTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Integration tests verifying the accrual-with-deferred-revenue-amortization journal entries posted when a Working
 * Capital Loan is written off (and reversed on undo). A write-off is terminal: it debits Losses Written-off for the
 * whole outstanding and credits the portfolio/receivable accounts -- except on a loan that was already charged off,
 * where the receivables are already off the books and the write-off instead reclassifies the loss out of the charge-off
 * accounts into Losses Written-off.
 */
public class WorkingCapitalLoanWriteOffAccountingTest {

    private static final DateTimeFormatter BUSINESS_DATE = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    private static final PostWorkingCapitalLoansLoanIdRequest CLEANUP_EMPTY_COMMAND_REQUEST = WorkingCapitalLoanApplicationTestBuilder
            .buildUndoApproveRequest();

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

    @BeforeAll
    public static void setupAccounts() {
        createdClientId = createClient();
        final FeignAccountHelper accountHelper = new FeignAccountHelper(FineractFeignClientHelper.getFineractFeignClient());
        fundSourceAccount = accountHelper.createLiabilityAccount("wcWoFundSource");
        loanPortfolioAccount = accountHelper.createAssetAccount("wcWoLoanPortfolio");
        transfersSuspenseAccount = accountHelper.createAssetAccount("wcWoTransfersSuspense");
        incomeFromDiscountFeeAccount = accountHelper.createIncomeAccount("wcWoIncomeDiscountFee");
        feesReceivableAccount = accountHelper.createAssetAccount("wcWoFeesReceivable");
        penaltiesReceivableAccount = accountHelper.createAssetAccount("wcWoPenaltiesReceivable");
        incomeFromFeeAccount = accountHelper.createIncomeAccount("wcWoIncomeFee");
        incomeFromPenaltyAccount = accountHelper.createIncomeAccount("wcWoIncomePenalty");
        incomeFromRecoveryAccount = accountHelper.createIncomeAccount("wcWoIncomeRecovery");
        writeOffAccount = accountHelper.createExpenseAccount("wcWoWriteOff");
        overpaymentAccount = accountHelper.createLiabilityAccount("wcWoOverpayment");
        deferredIncomeAccount = accountHelper.createLiabilityAccount("wcWoDeferredIncome");
        chargeOffExpenseAccount = accountHelper.createExpenseAccount("wcWoChargeOffExpense");
        incomeFromChargeOffFeesAccount = accountHelper.createIncomeAccount("wcWoIncomeChargeOffFees");
        incomeFromChargeOffPenaltyAccount = accountHelper.createIncomeAccount("wcWoIncomeChargeOffPenalty");
    }

    @AfterEach
    void cleanupEntities() {
        for (final Long loanId : createdLoanIds) {
            if (loanId == null) {
                continue;
            }
            try {
                loanHelper.undoWriteOffByLoanId(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildUndoWriteOffRequest());
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup (loan may not be written off)
            }
            try {
                loanHelper.undoDisbursalById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildUndoDisburseRequest());
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup (loan may not be disbursed / already removed)
            }
            try {
                loanHelper.undoApprovalById(loanId, CLEANUP_EMPTY_COMMAND_REQUEST);
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup (loan may not be approved / already removed)
            }
            try {
                loanHelper.deleteById(loanId);
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup (loan may be in non-deletable state / already removed)
            }
        }
        createdLoanIds.clear();
        for (final Long productId : createdProductIds) {
            if (productId == null) {
                continue;
            }
            try {
                productHelper.deleteWorkingCapitalLoanProductById(productId);
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup (product may be already removed)
            }
        }
        createdProductIds.clear();
    }

    @Test
    public void testWriteOffPostsLossesWrittenOffJournalEntries() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProduct();
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        final AtomicLong loanId = new AtomicLong(0L);
        BusinessDateHelper.runAt(currentDate.format(BUSINESS_DATE),
                () -> loanId.set(createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), currentDate)));

        final AtomicLong writeOffTxnId = new AtomicLong(0L);
        BusinessDateHelper.runAt(currentDate.format(BUSINESS_DATE), () -> writeOffTxnId.set(
                loanHelper.writeOffByLoanId(loanId.get(), WorkingCapitalLoanDisbursementTestBuilder.buildWriteOffRequest(currentDate))));

        // The loan is closed as written-off and the balance is zeroed.
        final GetWorkingCapitalLoansLoanIdResponse loanData = loanHelper.retrieveById(loanId.get());
        assert loanData.getStatus() != null;
        assertEquals("loanStatusType.closed.written.off", loanData.getStatus().getCode());

        // Principal-only outstanding: Dr Losses Written-off 5000, Cr Loan Portfolio 5000.
        final List<JournalEntryTransactionItem> entries = getJournalEntriesForWCTransaction(writeOffTxnId.get());
        assertEquals(2, entries.size(), "Expected 2 journal entries (1 debit + 1 credit)");
        assertJournalEntry(entries, "DEBIT", writeOffAccount, 5000.0);
        assertJournalEntry(entries, "CREDIT", loanPortfolioAccount, 5000.0);
    }

    @Test
    public void testUndoWriteOffReversesJournalEntriesAndReopensLoan() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProduct();
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        final AtomicLong loanId = new AtomicLong(0L);
        BusinessDateHelper.runAt(currentDate.format(BUSINESS_DATE),
                () -> loanId.set(createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), currentDate)));

        final AtomicLong writeOffTxnId = new AtomicLong(0L);
        BusinessDateHelper.runAt(currentDate.format(BUSINESS_DATE), () -> writeOffTxnId.set(
                loanHelper.writeOffByLoanId(loanId.get(), WorkingCapitalLoanDisbursementTestBuilder.buildWriteOffRequest(currentDate))));
        BusinessDateHelper.runAt(currentDate.format(BUSINESS_DATE),
                () -> loanHelper.undoWriteOffByLoanId(loanId.get(), WorkingCapitalLoanDisbursementTestBuilder.buildUndoWriteOffRequest()));

        // The loan is reopened to active.
        final GetWorkingCapitalLoansLoanIdResponse loanData = loanHelper.retrieveById(loanId.get());
        assert loanData.getStatus() != null;
        assertEquals("loanStatusType.active", loanData.getStatus().getCode());

        // Undo posts a mirror for each original entry (append-only), so the transaction now carries 4 entries and the
        // mirror flips the write-off: Dr Loan Portfolio 5000, Cr Losses Written-off 5000.
        final List<JournalEntryTransactionItem> entries = getJournalEntriesForWCTransaction(writeOffTxnId.get());
        assertEquals(4, entries.size(), "Expected 4 journal entries (original 2 + reversal 2)");
        assertJournalEntry(entries, "DEBIT", loanPortfolioAccount, 5000.0);
        assertJournalEntry(entries, "CREDIT", writeOffAccount, 5000.0);
    }

    @Test
    public void testWriteOffWithNoAccountingCreatesNoJournalEntries() {
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        final AtomicLong loanId = new AtomicLong(0L);
        final AtomicLong writeOffTxnId = new AtomicLong(0L);
        BusinessDateHelper.runAt(currentDate.format(BUSINESS_DATE), () -> {
            final String uniqueName = "WCL WoNoAcct " + UUID.randomUUID().toString().substring(0, 8);
            final String uniqueShortName = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
            final Long productId = productHelper
                    .createWorkingCapitalLoanProduct(
                            new WorkingCapitalLoanProductTestBuilder().withName(uniqueName).withShortName(uniqueShortName).build())
                    .getResourceId();
            createdProductIds.add(productId);
            loanId.set(createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), currentDate));
            writeOffTxnId.set(
                    loanHelper.writeOffByLoanId(loanId.get(), WorkingCapitalLoanDisbursementTestBuilder.buildWriteOffRequest(currentDate)));
        });

        final List<JournalEntryTransactionItem> entries = getJournalEntriesForWCTransaction(writeOffTxnId.get());
        assertTrue(entries.isEmpty(), "Expected no journal entries for NONE accounting rule");
    }

    @Test
    public void testWriteOffRecordsTheProvidedWriteOffReason() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProduct();
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        final Long writeOffReasonId = new FeignCodeHelper(FineractFeignClientHelper.getFineractFeignClient())
                .createWriteOffCodeValue("wcWoReason " + UUID.randomUUID().toString().substring(0, 8), 1);

        final AtomicLong loanId = new AtomicLong(0L);
        BusinessDateHelper.runAt(currentDate.format(BUSINESS_DATE), () -> {
            loanId.set(createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), currentDate));
            loanHelper.writeOffByLoanId(loanId.get(),
                    WorkingCapitalLoanDisbursementTestBuilder.buildWriteOffRequest(currentDate, writeOffReasonId));
        });

        // The reason travels as the lower-case "writeoffReasonId" the term/progressive loans use, and comes back on the
        // loan resource.
        final GetWorkingCapitalLoansLoanIdResponse loanData = loanHelper.retrieveById(loanId.get());
        assert loanData.getWriteOffReason() != null;
        assertEquals(writeOffReasonId, loanData.getWriteOffReason().getId());
    }

    @Test
    public void testWriteOffAfterChargeOffReclassifiesLossToLossesWrittenOff() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProduct();
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        final AtomicLong loanId = new AtomicLong(0L);
        BusinessDateHelper.runAt(currentDate.format(BUSINESS_DATE),
                () -> loanId.set(createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), currentDate)));

        // Charge-off leaves the loan ACTIVE and the balance untouched, so a write-off is still reachable afterwards.
        final LocalDate laterDate = currentDate.plusDays(1);
        final AtomicLong chargeOffTxnId = new AtomicLong(0L);
        BusinessDateHelper.runAt(laterDate.format(BUSINESS_DATE), () -> chargeOffTxnId.set(loanHelper.chargeOffByLoanId(loanId.get(),
                WorkingCapitalLoanDisbursementTestBuilder.buildChargeOffRequest(laterDate, null))));

        final AtomicLong writeOffTxnId = new AtomicLong(0L);
        BusinessDateHelper.runAt(laterDate.format(BUSINESS_DATE), () -> writeOffTxnId
                .set(loanHelper.writeOffByLoanId(loanId.get(), WorkingCapitalLoanDisbursementTestBuilder.buildWriteOffRequest(laterDate))));

        // The write-off closes the loan as usual.
        final GetWorkingCapitalLoansLoanIdResponse loanData = loanHelper.retrieveById(loanId.get());
        assert loanData.getStatus() != null;
        assertEquals("loanStatusType.closed.written.off", loanData.getStatus().getCode());

        // The charge-off already credited the portfolio out, so the write-off reclassifies instead of touching the
        // asset accounts: Dr Losses Written-off 5000, Cr Charge-off expense 5000 (P&L-neutral presentation move).
        final List<JournalEntryTransactionItem> writeOffEntries = getJournalEntriesForWCTransaction(writeOffTxnId.get());
        assertEquals(2, writeOffEntries.size(), "Expected 2 journal entries (1 debit + 1 credit)");
        assertJournalEntry(writeOffEntries, "DEBIT", writeOffAccount, 5000.0);
        assertJournalEntry(writeOffEntries, "CREDIT", chargeOffExpenseAccount, 5000.0);

        // The charge-off entries are left exactly as they were: Dr Charge-off expense 5000, Cr Loan portfolio 5000.
        final List<JournalEntryTransactionItem> chargeOffEntries = getJournalEntriesForWCTransaction(chargeOffTxnId.get());
        assertEquals(2, chargeOffEntries.size(), "Expected the charge-off entries to be untouched");
        assertJournalEntry(chargeOffEntries, "DEBIT", chargeOffExpenseAccount, 5000.0);
        assertJournalEntry(chargeOffEntries, "CREDIT", loanPortfolioAccount, 5000.0);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Long createAccrualWithDeferredRevenueAmortizationProduct() {
        final String uniqueName = "WCL WoAcct " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
        final Long productId = productHelper
                .createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder().withName(uniqueName)
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
                        .withIncomeFromChargeOffPenaltyAccountId(incomeFromChargeOffPenaltyAccount.getAccountID().longValue()).build())
                .getResourceId();
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

    private static Long createClient() {
        return ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
    }

    private Long submitAndTrack(final PostWorkingCapitalLoansRequest submitJson) {
        final Long loanId = loanHelper.submit(submitJson);
        createdLoanIds.add(loanId);
        return loanId;
    }
}
