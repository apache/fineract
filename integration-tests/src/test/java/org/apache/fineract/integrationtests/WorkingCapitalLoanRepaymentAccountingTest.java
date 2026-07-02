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
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionsResponse;
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
 * Integration tests verifying that accrual with deferred revenue amortization accounting journal entries are correctly
 * created during Working Capital Loan repayment transactions.
 */
public class WorkingCapitalLoanRepaymentAccountingTest {

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

    @BeforeAll
    public static void setupAccounts() {
        createdClientId = createClient();
        final FeignAccountHelper accountHelper = new FeignAccountHelper(FineractFeignClientHelper.getFineractFeignClient());
        fundSourceAccount = accountHelper.createLiabilityAccount("wcFundSource");
        loanPortfolioAccount = accountHelper.createAssetAccount("wcLoanPortfolio");
        transfersSuspenseAccount = accountHelper.createAssetAccount("wcTransfersSuspense");
        incomeFromDiscountFeeAccount = accountHelper.createIncomeAccount("wcIncomeDiscountFee");
        feesReceivableAccount = accountHelper.createAssetAccount("wcFeesReceivable");
        penaltiesReceivableAccount = accountHelper.createAssetAccount("wcPenaltiesReceivable");
        incomeFromFeeAccount = accountHelper.createIncomeAccount("wcIncomeFee");
        incomeFromPenaltyAccount = accountHelper.createIncomeAccount("wcIncomePenalty");
        incomeFromRecoveryAccount = accountHelper.createIncomeAccount("wcIncomeRecovery");
        writeOffAccount = accountHelper.createExpenseAccount("wcWriteOff");
        overpaymentAccount = accountHelper.createLiabilityAccount("wcOverpayment");
        deferredIncomeAccount = accountHelper.createLiabilityAccount("wcDeferredIncome");
    }

    @AfterEach
    void cleanupEntities() {
        for (final Long loanId : createdLoanIds) {
            if (loanId == null) {
                continue;
            }
            try {
                loanHelper.undoDisbursalById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildUndoDisburseRequest());
            } catch (final CallFailedRuntimeException ignored) {
                // best-effort cleanup (loan may not be disbursed / client inactive / loan already removed)
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
    public void testRepaymentCreatesJournalEntriesPrincipalOnly() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProduct();
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        AtomicLong loanId = new AtomicLong(0L);
        BusinessDateHelper.runAt(currentDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")), () -> {
            loanId.set(createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), currentDate));
        });
        final LocalDate repaymentDate = currentDate.plusDays(1);
        BusinessDateHelper
                .runAt(repaymentDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                        () -> loanHelper.makeRepaymentByLoanId(loanId.get(),
                                WorkingCapitalLoanDisbursementTestBuilder.buildRepaymentRequest(repaymentDate, BigDecimal.valueOf(3000),
                                        null, "partial repayment", 1, "repayment-account")));

        // Verify loan status is still active (partial repayment)
        final GetWorkingCapitalLoansLoanIdResponse loanData = loanHelper.retrieveById(loanId.get());
        assert loanData.getStatus() != null;
        assertEquals("loanStatusType.active", loanData.getStatus().getCode());

        // Verify journal entries: Dr Fund Source 3000, Cr Loan Portfolio 3000
        final Long repaymentTxnId = getRepaymentTransactionId(loanId.get());
        assertNotNull(repaymentTxnId, "Expected a repayment transaction to exist");
        final List<JournalEntryTransactionItem> entries = getJournalEntriesForWCTransaction(repaymentTxnId);
        assertEquals(2, entries.size(), "Expected 2 journal entries (1 debit + 1 credit)");

        assertJournalEntry(entries, "DEBIT", fundSourceAccount, 3000.0);
        assertJournalEntry(entries, "CREDIT", loanPortfolioAccount, 3000.0);
    }

    @Test
    public void testRepaymentWithOverpaymentCreatesJournalEntries() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProduct();
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        AtomicLong loanId = new AtomicLong(0L);
        BusinessDateHelper.runAt(currentDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")), () -> {
            loanId.set(createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), currentDate));
        });
        final LocalDate repaymentDate = currentDate.plusDays(1);
        BusinessDateHelper
                .runAt(repaymentDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                        () -> loanHelper.makeRepaymentByLoanId(loanId.get(),
                                WorkingCapitalLoanDisbursementTestBuilder.buildRepaymentRequest(repaymentDate, BigDecimal.valueOf(5200),
                                        null, "overpayment repayment", 1, "repayment-account")));

        // Verify loan status is overpaid
        final GetWorkingCapitalLoansLoanIdResponse loanData = loanHelper.retrieveById(loanId.get());
        assert loanData.getStatus() != null;
        assertEquals("loanStatusType.overpaid", loanData.getStatus().getCode());

        // Verify journal entries: Dr Fund Source 5200, Cr Loan Portfolio 5000, Cr Overpayment 200
        final Long repaymentTxnId = getRepaymentTransactionId(loanId.get());
        assertNotNull(repaymentTxnId, "Expected a repayment transaction to exist");
        final List<JournalEntryTransactionItem> entries = getJournalEntriesForWCTransaction(repaymentTxnId);
        assertEquals(3, entries.size(), "Expected 3 journal entries (1 debit + 2 credits)");

        assertJournalEntry(entries, "DEBIT", fundSourceAccount, 5200.0);
        assertJournalEntry(entries, "CREDIT", loanPortfolioAccount, 5000.0);
        assertJournalEntry(entries, "CREDIT", overpaymentAccount, 200.0);
    }

    @Test
    public void testFullRepaymentCreatesJournalEntries() {
        final Long productId = createAccrualWithDeferredRevenueAmortizationProduct();
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        AtomicLong loanId = new AtomicLong(0L);
        BusinessDateHelper.runAt(currentDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")), () -> {
            loanId.set(createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), currentDate));
        });
        final LocalDate repaymentDate = currentDate.plusDays(1);
        BusinessDateHelper.runAt(repaymentDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                () -> loanHelper.makeRepaymentByLoanId(loanId.get(), WorkingCapitalLoanDisbursementTestBuilder
                        .buildRepaymentRequest(repaymentDate, BigDecimal.valueOf(5000), null, "full payoff", 1, "repayment-account")));

        // Verify loan status is closed
        final GetWorkingCapitalLoansLoanIdResponse loanData = loanHelper.retrieveById(loanId.get());
        assert loanData.getStatus() != null;
        assertEquals("loanStatusType.closed.obligations.met", loanData.getStatus().getCode());

        // Verify journal entries: Dr Fund Source 5000, Cr Loan Portfolio 5000
        final Long repaymentTxnId = getRepaymentTransactionId(loanId.get());
        assertNotNull(repaymentTxnId, "Expected a repayment transaction to exist");
        final List<JournalEntryTransactionItem> entries = getJournalEntriesForWCTransaction(repaymentTxnId);
        assertEquals(2, entries.size(), "Expected 2 journal entries (1 debit + 1 credit)");

        assertJournalEntry(entries, "DEBIT", fundSourceAccount, 5000.0);
        assertJournalEntry(entries, "CREDIT", loanPortfolioAccount, 5000.0);
    }

    @Test
    public void testRepaymentWithNoAccountingCreatesNoJournalEntries() {
        // Create product with NONE accounting rule
        final LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());
        AtomicLong loanId = new AtomicLong(0L);
        BusinessDateHelper.runAt(currentDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")), () -> {
            final String uniqueName = "WCL NoAcct " + UUID.randomUUID().toString().substring(0, 8);
            final String uniqueShortName = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
            final Long productId = productHelper
                    .createWorkingCapitalLoanProduct(
                            new WorkingCapitalLoanProductTestBuilder().withName(uniqueName).withShortName(uniqueShortName).build())
                    .getResourceId();
            createdProductIds.add(productId);

            loanId.set(createApprovedAndDisbursedLoan(productId, BigDecimal.valueOf(5000), currentDate));
        });
        final LocalDate repaymentDate = currentDate.plusDays(1);
        BusinessDateHelper
                .runAt(repaymentDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                        () -> loanHelper.makeRepaymentByLoanId(loanId.get(),
                                WorkingCapitalLoanDisbursementTestBuilder.buildRepaymentRequest(repaymentDate, BigDecimal.valueOf(3000),
                                        null, "no accounting repayment", 1, "repayment-account")));

        // Verify no journal entries were created
        final Long repaymentTxnId = getRepaymentTransactionId(loanId.get());
        assertNotNull(repaymentTxnId, "Expected a repayment transaction to exist");
        final List<JournalEntryTransactionItem> entries = getJournalEntriesForWCTransaction(repaymentTxnId);
        assertTrue(entries.isEmpty(), "Expected no journal entries for NONE accounting rule");
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Long createAccrualWithDeferredRevenueAmortizationProduct() {
        final String uniqueName = "WCL Acct " + UUID.randomUUID().toString().substring(0, 8);
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
                .withDeferredIncomeLiabilityAccountId(deferredIncomeAccount.getAccountID().longValue()).build()).getResourceId();
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

    private Long getRepaymentTransactionId(final Long loanId) {
        final GetWorkingCapitalLoanTransactionsResponse transactions = loanHelper.retrieveTransactionsByLoanId(loanId);
        if (transactions.getContent() == null) {
            return null;
        }
        for (final GetWorkingCapitalLoanTransactionIdResponse txn : transactions.getContent()) {
            if (txn.getType() != null && "loanTransactionType.repayment".equals(txn.getType().getCode())) {
                return txn.getId();
            }
        }
        return null;
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
