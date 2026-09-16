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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
 * Integration tests for money collected on a Working Capital Loan after it was written off.
 * <p>
 * A recovery payment is recognized as income, not as a repayment: the portfolio and receivables were already relieved
 * by the write-off, so the whole amount books as Dr Fund Source / Cr Income from Recovery with no split by principal,
 * fee or penalty. Reversing it posts the mirror. The loan keeps its CLOSED_WRITTEN_OFF status throughout.
 * </p>
 * <p>
 * The amount is capped by what is still recoverable rather than by the gross amount written off, so successive
 * recoveries cannot collect more than the loss that was booked -- and while any recovery stands, the write-off cannot
 * be undone.
 * </p>
 */
public class WorkingCapitalLoanRecoveryPaymentAccountingTest {

    private static final DateTimeFormatter BUSINESS_DATE = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    private static final PostWorkingCapitalLoansLoanIdRequest CLEANUP_EMPTY_COMMAND_REQUEST = WorkingCapitalLoanApplicationTestBuilder
            .buildUndoApproveRequest();

    private final WorkingCapitalLoanHelper loanHelper = new WorkingCapitalLoanHelper();
    private final WorkingCapitalLoanProductHelper productHelper = new WorkingCapitalLoanProductHelper();
    private final List<Long> createdLoanIds = new ArrayList<>();
    private final Map<Long, List<Long>> recoveryTxnIdsByLoanId = new LinkedHashMap<>();
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
        fundSourceAccount = accountHelper.createLiabilityAccount("wcRpFundSource");
        loanPortfolioAccount = accountHelper.createAssetAccount("wcRpLoanPortfolio");
        transfersSuspenseAccount = accountHelper.createAssetAccount("wcRpTransfersSuspense");
        incomeFromDiscountFeeAccount = accountHelper.createIncomeAccount("wcRpIncomeDiscountFee");
        feesReceivableAccount = accountHelper.createAssetAccount("wcRpFeesReceivable");
        penaltiesReceivableAccount = accountHelper.createAssetAccount("wcRpPenaltiesReceivable");
        incomeFromFeeAccount = accountHelper.createIncomeAccount("wcRpIncomeFee");
        incomeFromPenaltyAccount = accountHelper.createIncomeAccount("wcRpIncomePenalty");
        incomeFromRecoveryAccount = accountHelper.createIncomeAccount("wcRpIncomeRecovery");
        writeOffAccount = accountHelper.createExpenseAccount("wcRpWriteOff");
        overpaymentAccount = accountHelper.createLiabilityAccount("wcRpOverpayment");
        deferredIncomeAccount = accountHelper.createLiabilityAccount("wcRpDeferredIncome");
        chargeOffExpenseAccount = accountHelper.createExpenseAccount("wcRpChargeOffExpense");
        incomeFromChargeOffFeesAccount = accountHelper.createIncomeAccount("wcRpIncomeChargeOffFees");
        incomeFromChargeOffPenaltyAccount = accountHelper.createIncomeAccount("wcRpIncomeChargeOffPenalty");
    }

    @AfterEach
    void cleanupEntities() {
        for (final Long loanId : createdLoanIds) {
            if (loanId == null) {
                continue;
            }
            // A standing recovery blocks the undo write-off, so give the money back first.
            for (final Long recoveryTxnId : recoveryTxnIdsByLoanId.getOrDefault(loanId, List.of())) {
                try {
                    loanHelper.undoTransactionByLoanId(loanId, recoveryTxnId);
                } catch (final CallFailedRuntimeException ignored) {
                    // best-effort cleanup (recovery may already be reversed)
                }
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
        recoveryTxnIdsByLoanId.clear();
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
    public void testRecoveryPaymentPostsIncomeFromRecoveryJournalEntries() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProduct();
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        final AtomicLong loanId = new AtomicLong(0L);
        final AtomicLong recoveryTxnId = new AtomicLong(0L);
        BusinessDateHelper.runAt(currentDate.format(BUSINESS_DATE), () -> {
            loanId.set(createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), currentDate));
            loanHelper.writeOffByLoanId(loanId.get(), WorkingCapitalLoanDisbursementTestBuilder.buildWriteOffRequest(currentDate));
            recoveryTxnId.set(trackRecovery(loanId.get(), loanHelper.recoveryPaymentByLoanId(loanId.get(),
                    WorkingCapitalLoanDisbursementTestBuilder.buildRecoveryPaymentRequest(currentDate, BigDecimal.valueOf(2000)))));
        });

        // The recovery does not reopen the loan: it stays closed as written off.
        final GetWorkingCapitalLoansLoanIdResponse loanData = loanHelper.retrieveById(loanId.get());
        assert loanData.getStatus() != null;
        assertEquals("loanStatusType.closed.written.off", loanData.getStatus().getCode());

        // Dr Fund Source 2000, Cr Income from Recovery 2000 -- one line each, no split by portion.
        final List<JournalEntryTransactionItem> entries = getJournalEntriesForWCTransaction(recoveryTxnId.get());
        assertEquals(2, entries.size(), "Expected 2 journal entries (1 debit + 1 credit)");
        assertJournalEntry(entries, "DEBIT", fundSourceAccount, 2000.0);
        assertJournalEntry(entries, "CREDIT", incomeFromRecoveryAccount, 2000.0);
    }

    @Test
    public void testRecoveryPaymentUpdatesTheRecoverableBalance() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProduct();
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        final AtomicLong loanId = new AtomicLong(0L);
        BusinessDateHelper.runAt(currentDate.format(BUSINESS_DATE), () -> {
            loanId.set(createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), currentDate));
            loanHelper.writeOffByLoanId(loanId.get(), WorkingCapitalLoanDisbursementTestBuilder.buildWriteOffRequest(currentDate));
            trackRecovery(loanId.get(), loanHelper.recoveryPaymentByLoanId(loanId.get(),
                    WorkingCapitalLoanDisbursementTestBuilder.buildRecoveryPaymentRequest(currentDate, BigDecimal.valueOf(2000))));
        });

        // The read API has to explain the zeroed outstanding and what is left to recover, or a client cannot tell a
        // written-off loan from a paid one, nor why a further recovery gets rejected.
        final GetWorkingCapitalLoansLoanIdResponse loanData = loanHelper.retrieveById(loanId.get());
        assert loanData.getBalance() != null;
        assertEquals(0, BigDecimal.valueOf(5000).compareTo(loanData.getBalance().getTotalWrittenOff()));
        assertEquals(0, BigDecimal.valueOf(2000).compareTo(loanData.getBalance().getTotalRecovered()));
        assertEquals(0, BigDecimal.valueOf(3000).compareTo(loanData.getBalance().getWrittenOffOutstanding()));
        assertEquals(0, BigDecimal.ZERO.compareTo(loanData.getBalance().getTotalOutstanding()));
    }

    @Test
    public void testUndoRecoveryPaymentPostsMirrorJournalEntries() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProduct();
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        final AtomicLong loanId = new AtomicLong(0L);
        final AtomicLong recoveryTxnId = new AtomicLong(0L);
        BusinessDateHelper.runAt(currentDate.format(BUSINESS_DATE), () -> {
            loanId.set(createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), currentDate));
            loanHelper.writeOffByLoanId(loanId.get(), WorkingCapitalLoanDisbursementTestBuilder.buildWriteOffRequest(currentDate));
            recoveryTxnId.set(loanHelper.recoveryPaymentByLoanId(loanId.get(),
                    WorkingCapitalLoanDisbursementTestBuilder.buildRecoveryPaymentRequest(currentDate, BigDecimal.valueOf(2000))));
            loanHelper.undoTransactionByLoanId(loanId.get(), recoveryTxnId.get());
        });

        // Reversal is a mirror appended to the same transaction: Dr Income from Recovery, Cr Fund Source.
        final List<JournalEntryTransactionItem> entries = getJournalEntriesForWCTransaction(recoveryTxnId.get());
        assertEquals(4, entries.size(), "Expected 4 journal entries (original 2 + reversal 2)");
        assertJournalEntry(entries, "DEBIT", incomeFromRecoveryAccount, 2000.0);
        assertJournalEntry(entries, "CREDIT", fundSourceAccount, 2000.0);

        // Giving the money back makes it recoverable again, and the loan is still written off.
        final GetWorkingCapitalLoansLoanIdResponse loanData = loanHelper.retrieveById(loanId.get());
        assert loanData.getStatus() != null;
        assertEquals("loanStatusType.closed.written.off", loanData.getStatus().getCode());
        assert loanData.getBalance() != null;
        assertEquals(0, BigDecimal.ZERO.compareTo(loanData.getBalance().getTotalRecovered()));
        assertEquals(0, BigDecimal.valueOf(5000).compareTo(loanData.getBalance().getWrittenOffOutstanding()));
    }

    @Test
    public void testRecoveryPaymentsCannotAddUpPastTheAmountWrittenOff() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProduct();
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        final AtomicLong loanId = new AtomicLong(0L);
        BusinessDateHelper.runAt(currentDate.format(BUSINESS_DATE), () -> {
            loanId.set(createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), currentDate));
            loanHelper.writeOffByLoanId(loanId.get(), WorkingCapitalLoanDisbursementTestBuilder.buildWriteOffRequest(currentDate));
            trackRecovery(loanId.get(), loanHelper.recoveryPaymentByLoanId(loanId.get(),
                    WorkingCapitalLoanDisbursementTestBuilder.buildRecoveryPaymentRequest(currentDate, BigDecimal.valueOf(3000))));

            // 5000 was written off and 3000 already recovered, so only 2000 is left. Term loan compares each recovery
            // against the gross 5000 and would let this through, collecting more than the loss that was booked.
            final CallFailedRuntimeException error = loanHelper.runRecoveryPaymentByLoanIdExpectingFailure(loanId.get(),
                    WorkingCapitalLoanDisbursementTestBuilder.buildRecoveryPaymentRequest(currentDate, BigDecimal.valueOf(2500)));
            assertTrue(error.getMessage() != null && error.getMessage().contains("cannot.be.greater.than.remaining.written.off.amount"),
                    "Expected remaining-written-off validation error, got: " + error.getMessage());

            // Exactly the remainder is still accepted.
            trackRecovery(loanId.get(), loanHelper.recoveryPaymentByLoanId(loanId.get(),
                    WorkingCapitalLoanDisbursementTestBuilder.buildRecoveryPaymentRequest(currentDate, BigDecimal.valueOf(2000))));
        });

        final GetWorkingCapitalLoansLoanIdResponse loanData = loanHelper.retrieveById(loanId.get());
        assert loanData.getBalance() != null;
        assertEquals(0, BigDecimal.valueOf(5000).compareTo(loanData.getBalance().getTotalRecovered()));
        assertEquals(0, BigDecimal.ZERO.compareTo(loanData.getBalance().getWrittenOffOutstanding()));
    }

    @Test
    public void testRecoveryPaymentIsRejectedOnALoanThatIsNotWrittenOff() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProduct();
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        BusinessDateHelper.runAt(currentDate.format(BUSINESS_DATE), () -> {
            final Long loanId = createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), currentDate);
            final CallFailedRuntimeException error = loanHelper.runRecoveryPaymentByLoanIdExpectingFailure(loanId,
                    WorkingCapitalLoanDisbursementTestBuilder.buildRecoveryPaymentRequest(currentDate, BigDecimal.valueOf(100)));
            assertTrue(error.getMessage() != null && error.getMessage().contains("is.not.written.off"),
                    "Expected not-written-off validation error, got: " + error.getMessage());
        });
    }

    @Test
    public void testUndoWriteOffIsRejectedWhileARecoveryStands() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProduct();
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        final AtomicLong loanId = new AtomicLong(0L);
        final AtomicLong recoveryTxnId = new AtomicLong(0L);
        BusinessDateHelper.runAt(currentDate.format(BUSINESS_DATE), () -> {
            loanId.set(createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), currentDate));
            loanHelper.writeOffByLoanId(loanId.get(), WorkingCapitalLoanDisbursementTestBuilder.buildWriteOffRequest(currentDate));
            recoveryTxnId.set(trackRecovery(loanId.get(), loanHelper.recoveryPaymentByLoanId(loanId.get(),
                    WorkingCapitalLoanDisbursementTestBuilder.buildRecoveryPaymentRequest(currentDate, BigDecimal.valueOf(2000)))));

            // Undoing the write-off would restore the full outstanding while the recovered cash stays booked as
            // income, so the same money would be counted twice.
            final CallFailedRuntimeException error = loanHelper.runUndoWriteOffByLoanIdExpectingFailure(loanId.get(),
                    WorkingCapitalLoanDisbursementTestBuilder.buildUndoWriteOffRequest());
            assertTrue(error.getMessage() != null && error.getMessage().contains("cannot.undo.write.off.with.recovery.payments"),
                    "Expected recovery-payments validation error, got: " + error.getMessage());

            // Reversing the recovery clears the way.
            loanHelper.undoTransactionByLoanId(loanId.get(), recoveryTxnId.get());
            loanHelper.undoWriteOffByLoanId(loanId.get(), WorkingCapitalLoanDisbursementTestBuilder.buildUndoWriteOffRequest());
        });

        final GetWorkingCapitalLoansLoanIdResponse loanData = loanHelper.retrieveById(loanId.get());
        assert loanData.getStatus() != null;
        assertEquals("loanStatusType.active", loanData.getStatus().getCode());
    }

    @Test
    public void testRecoveryPaymentWithNoAccountingCreatesNoJournalEntries() {
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        final AtomicLong recoveryTxnId = new AtomicLong(0L);
        BusinessDateHelper.runAt(currentDate.format(BUSINESS_DATE), () -> {
            final String uniqueName = "WCL RpNoAcct " + UUID.randomUUID().toString().substring(0, 8);
            final String uniqueShortName = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
            final Long productId = productHelper
                    .createWorkingCapitalLoanProduct(
                            new WorkingCapitalLoanProductTestBuilder().withName(uniqueName).withShortName(uniqueShortName).build())
                    .getResourceId();
            createdProductIds.add(productId);
            final Long loanId = createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), currentDate);
            loanHelper.writeOffByLoanId(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildWriteOffRequest(currentDate));
            recoveryTxnId.set(trackRecovery(loanId, loanHelper.recoveryPaymentByLoanId(loanId,
                    WorkingCapitalLoanDisbursementTestBuilder.buildRecoveryPaymentRequest(currentDate, BigDecimal.valueOf(2000)))));
        });

        final List<JournalEntryTransactionItem> entries = getJournalEntriesForWCTransaction(recoveryTxnId.get());
        assertTrue(entries.isEmpty(), "Expected no journal entries for NONE accounting rule");
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

    private Long trackRecovery(final Long loanId, final Long recoveryTxnId) {
        recoveryTxnIdsByLoanId.computeIfAbsent(loanId, key -> new ArrayList<>()).add(recoveryTxnId);
        return recoveryTxnId;
    }

    private Long submitAndTrack(final PostWorkingCapitalLoansRequest submitJson) {
        final Long loanId = loanHelper.submit(submitJson);
        createdLoanIds.add(loanId);
        return loanId;
    }
}
