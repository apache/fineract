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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.fineract.client.models.GetJournalEntriesTransactionIdResponse;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsRequest.AccountingRuleEnum;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Verifies that when a backdated repayment triggers reprocessing on an accrual-with-deferred-revenue product, the
 * journal entries for the newly created (reprocessing-triggering) transaction are posted exactly once. The
 * transaction's entries are posted by the write flow after reprocessing; the reprocessing replay itself only
 * reverses+reposts entries for the *pre-existing* transactions whose allocation changed - so the new one must never be
 * double booked.
 */
public class FeignWorkingCapitalLoanReprocessJournalEntryTest extends FeignIntegrationTest {

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
        fundSourceAccount = accountHelper.createLiabilityAccount("wcReprocJeFundSrc");
        loanPortfolioAccount = accountHelper.createAssetAccount("wcReprocJeLoanPort");
        transfersSuspenseAccount = accountHelper.createAssetAccount("wcReprocJeXferSusp");
        incomeFromDiscountFeeAccount = accountHelper.createIncomeAccount("wcReprocJeIncDisc");
        feesReceivableAccount = accountHelper.createAssetAccount("wcReprocJeFeesRcv");
        penaltiesReceivableAccount = accountHelper.createAssetAccount("wcReprocJePenRcv");
        incomeFromFeeAccount = accountHelper.createIncomeAccount("wcReprocJeIncFee");
        incomeFromPenaltyAccount = accountHelper.createIncomeAccount("wcReprocJeIncPen");
        incomeFromRecoveryAccount = accountHelper.createIncomeAccount("wcReprocJeIncRec");
        writeOffAccount = accountHelper.createExpenseAccount("wcReprocJeWrtOff");
        overpaymentAccount = accountHelper.createLiabilityAccount("wcReprocJeOverpay");
        deferredIncomeAccount = accountHelper.createLiabilityAccount("wcReprocJeDefInc");
    }

    @AfterAll
    void cleanupEntities() {
        createdLoanIds.forEach(wcLoanHelper::cleanupLoan);
        createdLoanIds.clear();
        createdProductIds.clear();
    }

    @Test
    @DisplayName("A backdated repayment that triggers reprocessing books exactly one set of journal entries for the new transaction")
    void backdatedRepaymentReprocess_booksSingleJournalEntrySetForNewTransaction() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long client = clientHelper.createClient("01 January 2026");
            final Long productId = createAccrualProduct();
            final Long loanId = createAndDisburseLoanOnProduct(client, productId, BigDecimal.valueOf(9000), "01 January 2026");
            addCharge(loanId, false, 100, "01 January 2026");

            // R1 on day 20 settles the 100 fee: DR fund source 100, CR fees receivable 100.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-20");
            final Long r1Id = wcLoanHelper.makeRepayment(loanId,
                    WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(100), "20 January 2026"));

            // Backdated R2 on day 10 settles the fee first on replay, triggering the full reprocess. R2 is the newly
            // created transaction whose entries must be booked exactly once (not once by the write flow AND again by
            // the
            // replay).
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-25");
            final Long r2Id = wcLoanHelper.makeRepayment(loanId,
                    WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(100), "10 January 2026"));

            final List<JournalEntryTransactionItem> r2Entries = nonReversed(journalEntriesFor(r2Id));
            assertEquals(2, r2Entries.size(),
                    "The newly created transaction must have exactly one debit/credit pair - not double booked: " + describe(r2Entries));
            assertSingleEntry(r2Entries, "DEBIT", fundSourceAccount, BigDecimal.valueOf(100));
            assertSingleEntry(r2Entries, "CREDIT", feesReceivableAccount, BigDecimal.valueOf(100));

            // The whole loan's live ledger stays balanced (debits equal credits), which a double booking would break.
            final List<JournalEntryTransactionItem> allLive = nonReversed(allJournalEntriesForLoan(loanId));
            assertEqualBigDecimal(sum(allLive, "DEBIT"), sum(allLive, "CREDIT"),
                    "Total non-reversed debits must equal total non-reversed credits across the loan");
        });
    }

    private static void assertSingleEntry(final List<JournalEntryTransactionItem> entries, final String type, final Account account,
            final BigDecimal amount) {
        final long matches = entries.stream()
                .filter(entry -> type.equals(entry.getEntryType().getValue())
                        && account.getAccountID().longValue() == entry.getGlAccountId()
                        && amount.compareTo(BigDecimal.valueOf(entry.getAmount())) == 0)
                .count();
        assertEquals(1, matches, "Expected exactly one " + type + " of " + amount + " to account " + account.getAccountID() + " but found "
                + matches + " in: " + describe(entries));
    }

    private static BigDecimal sum(final List<JournalEntryTransactionItem> entries, final String type) {
        return entries.stream().filter(entry -> type.equals(entry.getEntryType().getValue()))
                .map(entry -> BigDecimal.valueOf(entry.getAmount())).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static void assertEqualBigDecimal(final BigDecimal expected, final BigDecimal actual, final String message) {
        assertTrue(expected.compareTo(actual) == 0, message + " - expected " + expected + " but was " + actual);
    }

    private static List<JournalEntryTransactionItem> nonReversed(final List<JournalEntryTransactionItem> entries) {
        return entries.stream().filter(entry -> !Boolean.TRUE.equals(entry.getReversed())).toList();
    }

    private static List<String> describe(final List<JournalEntryTransactionItem> entries) {
        return entries.stream().map(
                e -> e.getEntryType().getValue() + " acct=" + e.getGlAccountId() + " amt=" + e.getAmount() + " reversed=" + e.getReversed())
                .toList();
    }

    private List<JournalEntryTransactionItem> journalEntriesFor(final Long wcTransactionId) {
        final GetJournalEntriesTransactionIdResponse response = journalHelper.getJournalEntriesByTransactionId("WC" + wcTransactionId);
        return response == null || response.getPageItems() == null ? List.of() : response.getPageItems();
    }

    private List<JournalEntryTransactionItem> allJournalEntriesForLoan(final Long loanId) {
        final GetJournalEntriesTransactionIdResponse response = journalHelper.getJournalEntriesForLoan(loanId);
        return response == null || response.getPageItems() == null ? List.of() : response.getPageItems();
    }

    private Long addCharge(final Long loanId, final boolean penalty, final double amount, final String dueDate) {
        final Long chargeId = wcLoanHelper.createGlobalCharge(WorkingCapitalLoanRequestBuilders.specifiedDueDateCharge(penalty, amount));
        return wcLoanHelper.addCharge(loanId, WorkingCapitalLoanRequestBuilders.addCharge(chargeId, amount, dueDate));
    }

    private Long createAndDisburseLoanOnProduct(final Long clientId, final Long productId, final BigDecimal principal, final String date) {
        final Long loanId = wcLoanHelper.submitApplication(
                WorkingCapitalLoanRequestBuilders.submitApplication(clientId, productId, principal, BigDecimal.valueOf(18), date, date));
        createdLoanIds.add(loanId);
        wcLoanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approve(date, principal, date));
        wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburse(date, principal));
        return loanId;
    }

    private Long createAccrualProduct() {
        final Long productId = productHelper
                .createWorkingCapitalLoanProduct(
                        new WorkingCapitalLoanProductTestBuilder().withName("WCL ReprocJe " + UUID.randomUUID().toString().substring(0, 8))
                                .withShortName(UUID.randomUUID().toString().replace("-", "").substring(0, 4))
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
}
