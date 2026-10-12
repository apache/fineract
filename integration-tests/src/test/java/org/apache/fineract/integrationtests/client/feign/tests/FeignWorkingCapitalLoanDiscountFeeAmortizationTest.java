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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for the WC Discount Fee Amortization COB business step.
 *
 * Verifies:
 * <ul>
 * <li>Amortization transaction is created after repayment + COB</li>
 * <li>Correct journal entries: Dr Deferred Income (liability), Cr Income from Discount Fee (income)</li>
 * <li>No amortization when there is no repayment</li>
 * <li>No amortization when product has no discount</li>
 * <li>Idempotency: repeated COB without new payments creates no duplicate transactions</li>
 * <li>Incremental amortization after additional repayments</li>
 * </ul>
 */
public class FeignWorkingCapitalLoanDiscountFeeAmortizationTest extends FeignIntegrationTest {

    private static final String DISCOUNT_FEE_AMORTIZATION_CODE = "loanTransactionType.discountFeeAmortization";
    private static final String DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT_CODE = "loanTransactionType.discountFeeAmortizationAdjustment";

    private FeignWorkingCapitalLoanHelper wcLoanHelper;
    private FeignClientHelper clientHelper;
    private FeignBusinessDateHelper businessDateHelper;
    private FeignJournalEntryHelper journalHelper;
    private WorkingCapitalLoanProductHelper productHelper;

    // GL accounts
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
        fundSourceAccount = accountHelper.createLiabilityAccount("wcAmortFundSrc");
        loanPortfolioAccount = accountHelper.createAssetAccount("wcAmortLoanPort");
        transfersSuspenseAccount = accountHelper.createAssetAccount("wcAmortXferSusp");
        incomeFromDiscountFeeAccount = accountHelper.createIncomeAccount("wcAmortIncDisc");
        feesReceivableAccount = accountHelper.createAssetAccount("wcAmortFeesRcv");
        penaltiesReceivableAccount = accountHelper.createAssetAccount("wcAmortPenRcv");
        incomeFromFeeAccount = accountHelper.createIncomeAccount("wcAmortIncFee");
        incomeFromPenaltyAccount = accountHelper.createIncomeAccount("wcAmortIncPen");
        incomeFromRecoveryAccount = accountHelper.createIncomeAccount("wcAmortIncRec");
        writeOffAccount = accountHelper.createExpenseAccount("wcAmortWrtOff");
        overpaymentAccount = accountHelper.createLiabilityAccount("wcAmortOverpay");
        deferredIncomeAccount = accountHelper.createLiabilityAccount("wcAmortDefInc");
    }

    @AfterAll
    void cleanupEntities() {
        createdLoanIds.forEach(wcLoanHelper::cleanupLoan);
        createdLoanIds.clear();
        createdProductIds.clear();
    }

    // -----------------------------------------------------------------------
    // Test 1: Happy path — repayment triggers discount fee amortization on COB
    // -----------------------------------------------------------------------
    @Test
    @Order(1)
    void testDiscountFeeAmortizationCreatedAfterRepaymentAndCOB() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long testClientId = clientHelper.createClient("01 January 2026");
            final Long productId = createAccrualWithDeferredRevenueAmortizationProductWithDiscount();
            final BigDecimal principal = BigDecimal.valueOf(9000);
            final BigDecimal discount = BigDecimal.valueOf(1000);

            final Long loanId = submitLoanWithDiscount(testClientId, productId, principal, discount, "01 January 2026");
            wcLoanHelper.approve(loanId,
                    WorkingCapitalLoanRequestBuilders.approveWithDiscount("01 January 2026", principal, "01 January 2026", discount));
            wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburseWithDiscount("01 January 2026", principal, discount));

            // Make a repayment
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(3000), "01 January 2026"));

            // Run WC COB — this triggers the discount fee amortization business step
            wcLoanHelper.executeInlineWCCOB(loanId);

            // Verify amortization transaction was created
            final List<GetWorkingCapitalLoanTransactionIdResponse> transactions = wcLoanHelper.getTransactions(loanId);
            final List<GetWorkingCapitalLoanTransactionIdResponse> amortizationTxns = filterByType(transactions,
                    DISCOUNT_FEE_AMORTIZATION_CODE);
            assertEquals(1, amortizationTxns.size(), "Expected exactly 1 discount fee amortization transaction");

            final GetWorkingCapitalLoanTransactionIdResponse amortTxn = amortizationTxns.get(0);
            final BigDecimal amortAmount = amortTxn.getTransactionAmount();
            assertTrue(amortAmount.compareTo(BigDecimal.ZERO) > 0, "Amortization amount should be positive, was: " + amortAmount);

            // Amortization is cursor-based (proportional to payment), so it should be less than discount
            assertTrue(amortAmount.compareTo(discount) <= 0,
                    "Amortization amount should not exceed discount — amort: " + amortAmount + ", discount: " + discount);

            // Verify journal entries: Dr Deferred Income (liability), Cr Income from Discount Fee (income)
            final List<JournalEntryTransactionItem> entries = getJournalEntriesForWCTransaction(amortTxn.getId());
            assertEquals(2, entries.size(), "Expected 2 journal entries (1 debit + 1 credit)");
            assertJournalEntry(entries, "DEBIT", deferredIncomeAccount, amortAmount);
            assertJournalEntry(entries, "CREDIT", incomeFromDiscountFeeAccount, amortAmount);
        });
    }

    // -----------------------------------------------------------------------
    // Test 2: No payment → no discount fee amortization
    // -----------------------------------------------------------------------
    @Test
    @Order(2)
    void testNoAmortizationWithoutRepayment() {
        businessDateHelper.runAt("2026-02-01", () -> {
            final Long testClientId = clientHelper.createClient("01 February 2026");
            final Long productId = createAccrualWithDeferredRevenueAmortizationProductWithDiscount();
            final BigDecimal principal = BigDecimal.valueOf(9000);
            final BigDecimal discount = BigDecimal.valueOf(1000);

            final Long loanId = submitLoanWithDiscount(testClientId, productId, principal, discount, "01 February 2026");
            wcLoanHelper.approve(loanId,
                    WorkingCapitalLoanRequestBuilders.approveWithDiscount("01 February 2026", principal, "01 February 2026", discount));
            wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburseWithDiscount("01 February 2026", principal, discount));

            // Run COB without making any repayment
            wcLoanHelper.executeInlineWCCOB(loanId);

            // Verify no amortization transaction
            final List<GetWorkingCapitalLoanTransactionIdResponse> transactions = wcLoanHelper.getTransactions(loanId);
            final List<GetWorkingCapitalLoanTransactionIdResponse> amortizationTxns = filterByType(transactions,
                    DISCOUNT_FEE_AMORTIZATION_CODE);
            assertTrue(amortizationTxns.isEmpty(), "No amortization transaction should exist when no repayment was made");
        });
    }

    // -----------------------------------------------------------------------
    // Test 3: No discount fee on product → no amortization
    // -----------------------------------------------------------------------
    @Test
    @Order(3)
    void testNoAmortizationWithoutDiscountFee() {
        businessDateHelper.runAt("2026-03-01", () -> {
            final Long testClientId = clientHelper.createClient("01 March 2026");
            final Long productId = createAccrualWithDeferredRevenueAmortizationProductWithoutDiscount();
            final BigDecimal principal = BigDecimal.valueOf(9000);

            final Long loanId = submitLoan(testClientId, productId, principal, "01 March 2026");
            wcLoanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approve("01 March 2026", principal, "01 March 2026"));
            wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburse("01 March 2026", principal));

            // Make a repayment
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(3000), "01 March 2026"));

            // Run COB
            wcLoanHelper.executeInlineWCCOB(loanId);

            // Verify no amortization transaction (product has no discount)
            final List<GetWorkingCapitalLoanTransactionIdResponse> transactions = wcLoanHelper.getTransactions(loanId);
            final List<GetWorkingCapitalLoanTransactionIdResponse> amortizationTxns = filterByType(transactions,
                    DISCOUNT_FEE_AMORTIZATION_CODE);
            assertTrue(amortizationTxns.isEmpty(), "No amortization transaction should exist when product has no discount fee");
        });
    }

    // -----------------------------------------------------------------------
    // Test 4: Idempotency — running COB again without new payments creates no duplicate
    // -----------------------------------------------------------------------
    @Test
    @Order(4)
    void testCOBIdempotencyNoNewPayment() {
        businessDateHelper.runAt("2026-04-01", () -> {
            final Long testClientId = clientHelper.createClient("01 April 2026");
            final Long productId = createAccrualWithDeferredRevenueAmortizationProductWithDiscount();
            final BigDecimal principal = BigDecimal.valueOf(9000);
            final BigDecimal discount = BigDecimal.valueOf(1000);

            final Long loanId = submitLoanWithDiscount(testClientId, productId, principal, discount, "01 April 2026");
            wcLoanHelper.approve(loanId,
                    WorkingCapitalLoanRequestBuilders.approveWithDiscount("01 April 2026", principal, "01 April 2026", discount));
            wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburseWithDiscount("01 April 2026", principal, discount));

            // Make a repayment and run COB
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(3000), "01 April 2026"));
            wcLoanHelper.executeInlineWCCOB(loanId);

            final List<GetWorkingCapitalLoanTransactionIdResponse> firstRunTxns = filterByType(wcLoanHelper.getTransactions(loanId),
                    DISCOUNT_FEE_AMORTIZATION_CODE);
            assertEquals(1, firstRunTxns.size(), "First COB should create 1 amortization transaction");
            final BigDecimal firstAmount = firstRunTxns.get(0).getTransactionAmount();

            // Advance business date and run COB again — no new payments
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-04-02");
            wcLoanHelper.executeInlineWCCOB(loanId);

            final List<GetWorkingCapitalLoanTransactionIdResponse> secondRunTxns = filterByType(wcLoanHelper.getTransactions(loanId),
                    DISCOUNT_FEE_AMORTIZATION_CODE);
            assertEquals(1, secondRunTxns.size(), "Second COB without new payment should NOT create another amortization transaction");
            assertEquals(0, firstAmount.compareTo(secondRunTxns.get(0).getTransactionAmount()),
                    "Amortization amount should remain unchanged");
        });
    }

    // -----------------------------------------------------------------------
    // Test 5: Incremental amortization — two repayments produce two amortization txns
    // -----------------------------------------------------------------------
    @Test
    @Order(5)
    void testIncrementalAmortizationAcrossMultipleRepayments() {
        businessDateHelper.runAt("2026-05-01", () -> {
            final Long testClientId = clientHelper.createClient("01 May 2026");
            final Long productId = createAccrualWithDeferredRevenueAmortizationProductWithDiscount();
            final BigDecimal principal = BigDecimal.valueOf(9000);
            final BigDecimal discount = BigDecimal.valueOf(1000);

            final Long loanId = submitLoanWithDiscount(testClientId, productId, principal, discount, "01 May 2026");
            wcLoanHelper.approve(loanId,
                    WorkingCapitalLoanRequestBuilders.approveWithDiscount("01 May 2026", principal, "01 May 2026", discount));
            wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburseWithDiscount("01 May 2026", principal, discount));

            // First repayment + COB → first partial amortization
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(50), "01 May 2026"));
            wcLoanHelper.executeInlineWCCOB(loanId);

            final List<GetWorkingCapitalLoanTransactionIdResponse> firstRun = filterByType(wcLoanHelper.getTransactions(loanId),
                    DISCOUNT_FEE_AMORTIZATION_CODE);
            assertEquals(1, firstRun.size(), "Expected 1 amortization transaction after first repayment");
            final BigDecimal firstAmount = firstRun.get(0).getTransactionAmount();
            assertTrue(firstAmount.compareTo(BigDecimal.ZERO) > 0, "First amortization should be positive");
            assertTrue(firstAmount.compareTo(discount) < 0, "First amortization should be less than full discount — was: " + firstAmount);

            // Second repayment + COB → second incremental amortization
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-05-02");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(50), "02 May 2026"));
            wcLoanHelper.executeInlineWCCOB(loanId);

            final List<GetWorkingCapitalLoanTransactionIdResponse> secondRun = filterByType(wcLoanHelper.getTransactions(loanId),
                    DISCOUNT_FEE_AMORTIZATION_CODE);
            assertEquals(2, secondRun.size(), "Expected 2 amortization transactions after second repayment");
            final BigDecimal secondAmount = secondRun.get(1).getTransactionAmount();
            assertTrue(secondAmount.compareTo(BigDecimal.ZERO) > 0, "Second amortization should be positive");

            // Total amortization should not exceed discount
            final BigDecimal totalAmortized = firstAmount.add(secondAmount);
            assertTrue(totalAmortized.compareTo(discount) <= 0,
                    "Total amortization should not exceed discount — total: " + totalAmortized + ", discount: " + discount);
        });
    }

    // -----------------------------------------------------------------------
    // Test 6: Amortization transaction uses COB date as transaction date
    // -----------------------------------------------------------------------
    @Test
    @Order(6)
    void testAmortizationUsesCOBDateAsTransactionDate() {
        businessDateHelper.runAt("2026-06-14", () -> {
            final Long testClientId = clientHelper.createClient("14 June 2026");
            final Long productId = createAccrualWithDeferredRevenueAmortizationProductWithDiscount();
            final BigDecimal principal = BigDecimal.valueOf(9000);
            final BigDecimal discount = BigDecimal.valueOf(1000);

            final Long loanId = submitLoanWithDiscount(testClientId, productId, principal, discount, "14 June 2026");
            wcLoanHelper.approve(loanId,
                    WorkingCapitalLoanRequestBuilders.approveWithDiscount("14 June 2026", principal, "14 June 2026", discount));
            wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburseWithDiscount("14 June 2026", principal, discount));

            // Repay on June 14
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(3000), "14 June 2026"));

            // Advance business date to June 16 → COB_DATE becomes June 15
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-06-16");
            wcLoanHelper.executeInlineWCCOB(loanId);

            final List<GetWorkingCapitalLoanTransactionIdResponse> amortTxns = filterByType(wcLoanHelper.getTransactions(loanId),
                    DISCOUNT_FEE_AMORTIZATION_CODE);
            assertEquals(1, amortTxns.size(), "Expected 1 amortization transaction");

            // COB sets ActionContext.COB, so getBusinessDate() returns COB_DATE (June 15), not BUSINESS_DATE (June 16)
            assertEquals(LocalDate.of(2026, 6, 15), amortTxns.get(0).getTransactionDate(),
                    "Amortization transaction date should be the COB date (June 15), not the repayment date (June 14) or business date (June 16)");
        });
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Long createAccrualWithDeferredRevenueAmortizationProductWithDiscount() {
        final String uniqueName = "WCL DiscAmort " + UUID.randomUUID().toString().substring(0, 8);
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
                .withDeferredIncomeLiabilityAccountId(deferredIncomeAccount.getAccountID().longValue()).build()).getResourceId();
        createdProductIds.add(productId);
        return productId;
    }

    private Long createAccrualWithDeferredRevenueAmortizationProductWithoutDiscount() {
        final String uniqueName = "WCL NoDisc " + UUID.randomUUID().toString().substring(0, 8);
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

    private Long submitLoanWithDiscount(Long clientIdParam, Long productId, BigDecimal principal, BigDecimal discount, String date) {
        final Long loanId = wcLoanHelper.submitApplication(WorkingCapitalLoanRequestBuilders
                .submitApplication(clientIdParam, productId, principal, BigDecimal.valueOf(0.18), date, date).discount(discount));
        createdLoanIds.add(loanId);
        return loanId;
    }

    private Long submitLoan(Long clientIdParam, Long productId, BigDecimal principal, String date) {
        final Long loanId = wcLoanHelper.submitApplication(WorkingCapitalLoanRequestBuilders.submitApplication(clientIdParam, productId,
                principal, BigDecimal.valueOf(0.18), date, date));
        createdLoanIds.add(loanId);
        return loanId;
    }

    private List<GetWorkingCapitalLoanTransactionIdResponse> filterByType(List<GetWorkingCapitalLoanTransactionIdResponse> transactions,
            String typeCode) {
        return transactions.stream()
                .filter(txn -> txn.getType() != null && typeCode.equals(txn.getType().getCode()) && !Boolean.TRUE.equals(txn.getReversed()))
                .toList();
    }

    private List<JournalEntryTransactionItem> getJournalEntriesForWCTransaction(Long wcTransactionId) {
        final String transactionId = "WC" + wcTransactionId;
        final GetJournalEntriesTransactionIdResponse response = journalHelper.getJournalEntriesByTransactionId(transactionId);
        if (response == null || response.getPageItems() == null) {
            return List.of();
        }
        return response.getPageItems();
    }

    // -----------------------------------------------------------------------
    // Test 7: Amortization created inline on loan close (no COB needed)
    // -----------------------------------------------------------------------
    @Test
    @Order(7)
    void testAmortizationCreatedInlineOnLoanClose() {
        businessDateHelper.runAt("2026-07-01", () -> {
            final Long testClientId = clientHelper.createClient("01 July 2026");
            final Long productId = createAccrualWithDeferredRevenueAmortizationProductWithDiscount();
            final BigDecimal principal = BigDecimal.valueOf(9000);
            final BigDecimal discount = BigDecimal.valueOf(1000);

            final Long loanId = submitLoanWithDiscount(testClientId, productId, principal, discount, "01 July 2026");
            wcLoanHelper.approve(loanId,
                    WorkingCapitalLoanRequestBuilders.approveWithDiscount("01 July 2026", principal, "01 July 2026", discount));
            wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburseWithDiscount("01 July 2026", principal, discount));

            // Make a repayment that fully pays off the loan (principal + discount = 10000)
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(10000), "01 July 2026"));

            // Do NOT run COB — amortization should be created inline during the repayment
            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            assertEquals("loanStatusType.closed.obligations.met", loan.getStatus().getCode(), "Loan should be closed after full repayment");

            final List<GetWorkingCapitalLoanTransactionIdResponse> amortTxns = filterByType(wcLoanHelper.getTransactions(loanId),
                    DISCOUNT_FEE_AMORTIZATION_CODE);
            assertEquals(1, amortTxns.size(), "Amortization should be created inline on loan close without COB");
            final BigDecimal amortAmount = amortTxns.get(0).getTransactionAmount();
            assertEquals(0, amortAmount.compareTo(discount),
                    "Closing the loan must recognize the full discount — amort: " + amortAmount + ", discount: " + discount);

            // Verify journal entries
            final List<JournalEntryTransactionItem> entries = getJournalEntriesForWCTransaction(amortTxns.get(0).getId());
            assertEquals(2, entries.size(), "Expected 2 journal entries (1 debit + 1 credit)");
            assertJournalEntry(entries, "DEBIT", deferredIncomeAccount, amortAmount);
            assertJournalEntry(entries, "CREDIT", incomeFromDiscountFeeAccount, amortAmount);
        });
    }

    // -----------------------------------------------------------------------
    // Test 8: Amortization created inline on loan overpay (no COB needed)
    // -----------------------------------------------------------------------
    @Test
    @Order(8)
    void testAmortizationCreatedInlineOnLoanOverpay() {
        businessDateHelper.runAt("2026-08-01", () -> {
            final Long testClientId = clientHelper.createClient("01 August 2026");
            final Long productId = createAccrualWithDeferredRevenueAmortizationProductWithDiscount();
            final BigDecimal principal = BigDecimal.valueOf(9000);
            final BigDecimal discount = BigDecimal.valueOf(1000);

            final Long loanId = submitLoanWithDiscount(testClientId, productId, principal, discount, "01 August 2026");
            wcLoanHelper.approve(loanId,
                    WorkingCapitalLoanRequestBuilders.approveWithDiscount("01 August 2026", principal, "01 August 2026", discount));
            wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburseWithDiscount("01 August 2026", principal, discount));

            // Make an overpayment (more than principal + discount)
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(11000), "01 August 2026"));

            // Do NOT run COB — amortization should be created inline
            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            assertEquals("loanStatusType.overpaid", loan.getStatus().getCode(), "Loan should be overpaid");

            final List<GetWorkingCapitalLoanTransactionIdResponse> amortTxns = filterByType(wcLoanHelper.getTransactions(loanId),
                    DISCOUNT_FEE_AMORTIZATION_CODE);
            assertEquals(1, amortTxns.size(), "Amortization should be created inline on loan overpay without COB");
            final BigDecimal amortAmount = amortTxns.get(0).getTransactionAmount();
            assertEquals(0, amortAmount.compareTo(discount),
                    "Overpaying the loan must recognize the full discount — amort: " + amortAmount + ", discount: " + discount);
        });
    }

    // Test 9: a backdated repayment must keep net amortization consistent (recognition is time-capped), and overpay
    // must recognize the full discount exactly.
    @Test
    @Order(9)
    void testBackdatedRepaymentKeepsAmortizationConsistent() {
        businessDateHelper.runAt("2026-09-01", () -> {
            final Long testClientId = clientHelper.createClient("01 September 2026");
            final Long productId = createAccrualWithDeferredRevenueAmortizationProductWithDiscount();
            final BigDecimal principal = BigDecimal.valueOf(9000);
            final BigDecimal discount = BigDecimal.valueOf(1000);

            final Long loanId = submitLoanWithDiscount(testClientId, productId, principal, discount, "01 September 2026");
            wcLoanHelper.approve(loanId,
                    WorkingCapitalLoanRequestBuilders.approveWithDiscount("01 September 2026", principal, "01 September 2026", discount));
            wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburseWithDiscount("01 September 2026", principal, discount));

            // Repayment on Sep 5, then COB → first partial amortization
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-09-05");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(3000), "05 September 2026"));
            wcLoanHelper.executeInlineWCCOB(loanId);

            final List<GetWorkingCapitalLoanTransactionIdResponse> afterFirst = filterByType(wcLoanHelper.getTransactions(loanId),
                    DISCOUNT_FEE_AMORTIZATION_CODE);
            assertEquals(1, afterFirst.size(), "Expected 1 amortization transaction after the first repayment");
            final BigDecimal totalAfterFirst = sumAmounts(afterFirst);
            assertTrue(totalAfterFirst.compareTo(BigDecimal.ZERO) > 0, "First amortization should be positive");

            // Backdated repayment on Sep 2, then COB: net amortization must stay non-decreasing and bounded by the
            // discount.
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(2000), "02 September 2026"));
            wcLoanHelper.executeInlineWCCOB(loanId);

            final BigDecimal netAfterBackdated = netAmortization(loanId);
            assertTrue(netAfterBackdated.compareTo(totalAfterFirst) >= 0,
                    "Net amortization must not decrease after a backdated repayment — before: " + totalAfterFirst + ", after: "
                            + netAfterBackdated);
            assertTrue(netAfterBackdated.compareTo(discount) <= 0,
                    "Net amortization must never exceed the discount — net: " + netAfterBackdated + ", discount: " + discount);

            // Overpay (cumulative 11000 > principal + discount = 10000) → inline full recognition.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-09-10");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(6000), "10 September 2026"));

            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            assertEquals("loanStatusType.overpaid", loan.getStatus().getCode(), "Loan should be overpaid after the final repayment");

            final BigDecimal netAfterOverpay = netAmortization(loanId);
            assertEquals(0, netAfterOverpay.compareTo(discount),
                    "After overpay, net amortization (amortization - adjustment) must equal the discount exactly — net: " + netAfterOverpay
                            + ", discount: " + discount);
        });
    }

    /** Net recognized discount fee income from transactions: sum(amortization) - sum(amortization adjustment). */
    private BigDecimal netAmortization(final Long loanId) {
        final List<GetWorkingCapitalLoanTransactionIdResponse> txns = wcLoanHelper.getTransactions(loanId);
        return sumAmounts(filterByType(txns, DISCOUNT_FEE_AMORTIZATION_CODE))
                .subtract(sumAmounts(filterByType(txns, DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT_CODE)));
    }

    private static BigDecimal sumAmounts(List<GetWorkingCapitalLoanTransactionIdResponse> transactions) {
        return transactions.stream()
                .map(txn -> Objects.requireNonNull(txn.getTransactionAmount(),
                        () -> "transactionAmount must not be null for transaction " + txn.getId()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void assertJournalEntry(List<JournalEntryTransactionItem> entries, String expectedType, Account expectedAccount,
            BigDecimal expectedAmount) {
        final boolean found = entries.stream().anyMatch(entry -> {
            final boolean typeMatch = expectedType.equals(entry.getEntryType().getValue());
            final boolean accountMatch = expectedAccount.getAccountID().longValue() == entry.getGlAccountId();
            final boolean amountMatch = expectedAmount.compareTo(BigDecimal.valueOf(entry.getAmount())) == 0;
            return typeMatch && accountMatch && amountMatch;
        });
        assertTrue(found,
                "Expected journal entry: " + expectedType + " account=" + expectedAccount.getAccountID() + " amount=" + expectedAmount
                        + " not found in entries: " + entries.stream()
                                .map(e -> e.getEntryType().getValue() + " acct=" + e.getGlAccountId() + " amt=" + e.getAmount()).toList());
    }
}
