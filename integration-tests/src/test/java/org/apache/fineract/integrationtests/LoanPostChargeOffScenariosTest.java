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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.fineract.client.models.AllowAttributeOverrides;
import org.apache.fineract.client.models.GetJournalEntriesTransactionIdResponse;
import org.apache.fineract.client.models.GetLoanPaymentChannelToFundSourceMappings;
import org.apache.fineract.client.models.GetLoanTransactionRelation;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.LoanProductChargeData;
import org.apache.fineract.client.models.LoanProductChargeToGLAccountMapper;
import org.apache.fineract.client.models.PaymentTypeCreateRequest;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.PaymentTypeHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.funds.FundsResourceHandler;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(LoanTestLifecycleExtension.class)
public class LoanPostChargeOffScenariosTest extends FeignLoanTestBase {

    private static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder().appendPattern("dd MMMM yyyy").toFormatter();
    // asset
    private Account loansReceivable;
    private Account interestFeeReceivable;
    private Account suspenseAccount;
    private Account fundReceivables;
    // liability
    private Account suspenseClearingAccount;
    private Account overpaymentAccount;
    // income
    private Account interestIncome;
    private Account feeIncome;
    private Account feeChargeOff;
    private Account recoveries;
    private Account interestIncomeChargeOff;
    // expense
    private Account creditLossBadDebt;
    private Account creditLossBadDebtFraud;
    private Account writtenOff;
    private Account goodwillExpenseAccount;

    @BeforeEach
    public void setup() {
        this.loansReceivable = accountHelper.createAssetAccount("loansReceivable");
        this.interestFeeReceivable = accountHelper.createAssetAccount("interestFeeReceivable");
        this.suspenseAccount = accountHelper.createAssetAccount("suspenseAccount");
        this.fundReceivables = accountHelper.createAssetAccount("fundReceivables");
        this.suspenseClearingAccount = accountHelper.createLiabilityAccount("suspenseClearingAccount");
        this.overpaymentAccount = accountHelper.createLiabilityAccount("overpaymentAccount");
        this.interestIncome = accountHelper.createIncomeAccount("interestIncome");
        this.feeIncome = accountHelper.createIncomeAccount("feeIncome");
        this.feeChargeOff = accountHelper.createIncomeAccount("feeChargeOff");
        this.recoveries = accountHelper.createIncomeAccount("recoveries");
        this.interestIncomeChargeOff = accountHelper.createIncomeAccount("interestIncomeChargeOff");
        this.creditLossBadDebt = accountHelper.createExpenseAccount("creditLossBadDebt");
        this.creditLossBadDebtFraud = accountHelper.createExpenseAccount("creditLossBadDebtFraud");
        this.writtenOff = accountHelper.createExpenseAccount("writtenOff");
        this.goodwillExpenseAccount = accountHelper.createExpenseAccount("goodwillExpenseAccount");
    }

    @Test
    public void postChargeOffAddBackdatedTransactionTest() {
        runAt("14 September 2022", () -> {
            String loanExternalIdStr = UUID.randomUUID().toString();
            final Long loanProductId = createLoanProductWithPeriodicAccrualAccounting();
            final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            final Long loanId = createLoanAccount(clientId, loanProductId, loanExternalIdStr);

            // apply charges
            Long feeCharge = createLoanSpecifiedDueDateCharge(10.0);

            LocalDate targetDate = LocalDate.of(2022, 9, 5);
            final String feeCharge1AddedDate = DATE_FORMATTER.format(targetDate);
            addLoanCharge(loanId, feeCharge, feeCharge1AddedDate, 10.0);

            // set loan as chargeoff
            String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6)
                    + Utils.randomStringGenerator("is", 5);
            Long chargeOffReasonId = codeHelper.createChargeOffCodeValue(randomText, 1);
            String transactionExternalId = UUID.randomUUID().toString();
            PostLoansLoanIdTransactionsResponse chargeOffTransaction = chargeOffLoan(loanId,
                    new PostLoansLoanIdTransactionsRequest().transactionDate("14 September 2022").locale(LoanTestData.LOCALE)
                            .dateFormat(LoanTestData.DATETIME_PATTERN).externalId(transactionExternalId)
                            .chargeOffReasonId(chargeOffReasonId));

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getActive());
            assertTrue(loanDetails.getChargedOff());

            // verify Journal Entries For ChargeOff Transaction
            GetJournalEntriesTransactionIdResponse journalEntriesForChargeOff = getJournalEntries(
                    "L" + chargeOffTransaction.getResourceId().toString());

            assertNotNull(journalEntriesForChargeOff);
            List<JournalEntryTransactionItem> journalEntries = journalEntriesForChargeOff.getPageItems();
            assertEquals(4, journalEntries.size());

            assertEquals(1000, journalEntries.get(3).getAmount());
            assertEquals(LocalDate.of(2022, 9, 14), journalEntries.get(3).getTransactionDate());
            assertEquals(loansReceivable.getAccountID().longValue(), journalEntries.get(3).getGlAccountId().longValue());
            assertEquals("CREDIT", journalEntries.get(3).getEntryType().getValue());

            assertEquals(10, journalEntries.get(2).getAmount());
            assertEquals(LocalDate.of(2022, 9, 14), journalEntries.get(2).getTransactionDate());
            assertEquals(interestFeeReceivable.getAccountID().longValue(), journalEntries.get(2).getGlAccountId().longValue());
            assertEquals("CREDIT", journalEntries.get(2).getEntryType().getValue());

            assertEquals(1000, journalEntries.get(1).getAmount());
            assertEquals(LocalDate.of(2022, 9, 14), journalEntries.get(1).getTransactionDate());
            assertEquals(creditLossBadDebt.getAccountID().longValue(), journalEntries.get(1).getGlAccountId().longValue());
            assertEquals("DEBIT", journalEntries.get(1).getEntryType().getValue());

            assertEquals(10, journalEntries.get(0).getAmount());
            assertEquals(LocalDate.of(2022, 9, 14), journalEntries.get(0).getTransactionDate());
            assertEquals(feeChargeOff.getAccountID().longValue(), journalEntries.get(0).getGlAccountId().longValue());
            assertEquals("DEBIT", journalEntries.get(0).getEntryType().getValue());

            // make Repayment before chargeoff date
            final PostLoansLoanIdTransactionsResponse repaymentTransaction = makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate("7 September 2022")
                            .locale(LoanTestData.LOCALE).transactionAmount(100.0));

            loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getActive());
            assertTrue(loanDetails.getChargedOff());

            // verify Journal Entries for Repayment transaction

            GetJournalEntriesTransactionIdResponse journalEntriesForRepayment = getJournalEntries(
                    "L" + repaymentTransaction.getResourceId().toString());
            assertNotNull(journalEntriesForRepayment);

            journalEntries = journalEntriesForRepayment.getPageItems();
            assertEquals(3, journalEntries.size());

            assertEquals(90, journalEntries.get(2).getAmount());
            assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(2).getTransactionDate());
            assertEquals(loansReceivable.getAccountID().longValue(), journalEntries.get(2).getGlAccountId().longValue());
            assertEquals("CREDIT", journalEntries.get(2).getEntryType().getValue());

            assertEquals(10, journalEntries.get(1).getAmount());
            assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(1).getTransactionDate());
            assertEquals(interestFeeReceivable.getAccountID().longValue(), journalEntries.get(1).getGlAccountId().longValue());
            assertEquals("CREDIT", journalEntries.get(1).getEntryType().getValue());

            assertEquals(100, journalEntries.get(0).getAmount());
            assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(0).getTransactionDate());
            assertEquals(suspenseClearingAccount.getAccountID().longValue(), journalEntries.get(0).getGlAccountId().longValue());
            assertEquals("DEBIT", journalEntries.get(0).getEntryType().getValue());

            // Goodwill Credit before chargeoff date
            final PostLoansLoanIdTransactionsResponse goodwillCredit = makeGoodwillCredit(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate("10 September 2022")
                            .locale(LoanTestData.LOCALE).transactionAmount(100.0));

            loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getActive());
            assertTrue(loanDetails.getChargedOff());

            // verify Journal Entries for Goodwill Credit
            GetJournalEntriesTransactionIdResponse journalEntriesForGoodWillCredit = getJournalEntries(
                    "L" + goodwillCredit.getResourceId().toString());
            assertNotNull(journalEntriesForGoodWillCredit);

            journalEntries = journalEntriesForGoodWillCredit.getPageItems();
            assertEquals(2, journalEntries.size());

            assertEquals(100, journalEntries.get(1).getAmount());
            assertEquals(LocalDate.of(2022, 9, 10), journalEntries.get(1).getTransactionDate());
            assertEquals(loansReceivable.getAccountID().longValue(), journalEntries.get(1).getGlAccountId().longValue());
            assertEquals("CREDIT", journalEntries.get(1).getEntryType().getValue());

            assertEquals(100, journalEntries.get(0).getAmount());
            assertEquals(LocalDate.of(2022, 9, 10), journalEntries.get(0).getTransactionDate());
            assertEquals(goodwillExpenseAccount.getAccountID().longValue(), journalEntries.get(0).getGlAccountId().longValue());
            assertEquals("DEBIT", journalEntries.get(0).getEntryType().getValue());

            updateBusinessDate("16 September 2022");

            // make Repayment after chargeoff date
            final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate("15 September 2022")
                            .locale(LoanTestData.LOCALE).transactionAmount(100.0));

            loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getActive());
            assertTrue(loanDetails.getChargedOff());

            // verify Journal Entries for Repayment transaction
            journalEntriesForRepayment = getJournalEntries("L" + repaymentTransaction_1.getResourceId().toString());

            assertNotNull(journalEntriesForRepayment);

            journalEntries = journalEntriesForRepayment.getPageItems();
            assertEquals(2, journalEntries.size());

            assertEquals(100, journalEntries.get(1).getAmount());
            assertEquals(LocalDate.of(2022, 9, 15), journalEntries.get(1).getTransactionDate());
            assertEquals(recoveries.getAccountID().longValue(), journalEntries.get(1).getGlAccountId().longValue());
            assertEquals("CREDIT", journalEntries.get(1).getEntryType().getValue());

            assertEquals(100, journalEntries.get(0).getAmount());
            assertEquals(LocalDate.of(2022, 9, 15), journalEntries.get(0).getTransactionDate());
            assertEquals(suspenseClearingAccount.getAccountID().longValue(), journalEntries.get(0).getGlAccountId().longValue());
            assertEquals("DEBIT", journalEntries.get(0).getEntryType().getValue());

            // Goodwill Credit after chargeoff date
            final PostLoansLoanIdTransactionsResponse goodwillCredit_1 = makeGoodwillCredit(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate("16 September 2022")
                            .locale(LoanTestData.LOCALE).transactionAmount(100.0));

            loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getActive());
            assertTrue(loanDetails.getChargedOff());

            // verify Journal Entries for Goodwill Credit
            journalEntriesForGoodWillCredit = getJournalEntries("L" + goodwillCredit_1.getResourceId().toString());
            assertNotNull(journalEntriesForGoodWillCredit);

            journalEntries = journalEntriesForGoodWillCredit.getPageItems();
            assertEquals(2, journalEntries.size());

            assertEquals(100, journalEntries.get(1).getAmount());
            assertEquals(LocalDate.of(2022, 9, 16), journalEntries.get(1).getTransactionDate());
            assertEquals(recoveries.getAccountID().longValue(), journalEntries.get(1).getGlAccountId().longValue());
            assertEquals("CREDIT", journalEntries.get(1).getEntryType().getValue());

            assertEquals(100, journalEntries.get(0).getAmount());
            assertEquals(LocalDate.of(2022, 9, 16), journalEntries.get(0).getTransactionDate());
            assertEquals(goodwillExpenseAccount.getAccountID().longValue(), journalEntries.get(0).getGlAccountId().longValue());
            assertEquals("DEBIT", journalEntries.get(0).getEntryType().getValue());
        });
    }

    @Test
    public void postChargeOffBackdatedTransactionReverseTest() {
        String loanExternalIdStr = UUID.randomUUID().toString();
        final Long loanProductId = createLoanProductWithPeriodicAccrualAccounting();
        final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
        final Long loanId = createLoanAccount(clientId, loanProductId, loanExternalIdStr);

        // apply charges
        Long feeCharge = createLoanSpecifiedDueDateCharge(10.0);

        LocalDate targetDate = LocalDate.of(2022, 9, 5);
        final String feeCharge1AddedDate = DATE_FORMATTER.format(targetDate);
        addLoanCharge(loanId, feeCharge, feeCharge1AddedDate, 10.0);

        // make Repayment
        final PostLoansLoanIdTransactionsResponse repaymentTransaction = makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate("7 September 2022")
                        .locale(LoanTestData.LOCALE).transactionAmount(100.0));

        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        assertTrue(loanDetails.getStatus().getActive());

        // verify Journal Entries for Repayment transaction
        GetJournalEntriesTransactionIdResponse journalEntriesForRepayment = getJournalEntries(
                "L" + repaymentTransaction.getResourceId().toString());

        assertNotNull(journalEntriesForRepayment);

        List<JournalEntryTransactionItem> journalEntries = journalEntriesForRepayment.getPageItems();
        assertEquals(3, journalEntries.size());

        assertEquals(90, journalEntries.get(2).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(2).getTransactionDate());
        assertEquals(loansReceivable.getAccountID().longValue(), journalEntries.get(2).getGlAccountId().longValue());
        assertEquals("CREDIT", journalEntries.get(2).getEntryType().getValue());

        assertEquals(10, journalEntries.get(1).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(1).getTransactionDate());
        assertEquals(interestFeeReceivable.getAccountID().longValue(), journalEntries.get(1).getGlAccountId().longValue());
        assertEquals("CREDIT", journalEntries.get(1).getEntryType().getValue());

        assertEquals(100, journalEntries.get(0).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(0).getTransactionDate());
        assertEquals(suspenseClearingAccount.getAccountID().longValue(), journalEntries.get(0).getGlAccountId().longValue());
        assertEquals("DEBIT", journalEntries.get(0).getEntryType().getValue());

        // set loan as chargeoff
        String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6) + Utils.randomStringGenerator("is", 5);
        Long chargeOffReasonId = codeHelper.createChargeOffCodeValue(randomText, 1);
        String transactionExternalId = UUID.randomUUID().toString();
        PostLoansLoanIdTransactionsResponse chargeOffTransaction = chargeOffLoan(loanId,
                new PostLoansLoanIdTransactionsRequest().transactionDate("14 September 2022").locale(LoanTestData.LOCALE)
                        .dateFormat(LoanTestData.DATETIME_PATTERN).externalId(transactionExternalId).chargeOffReasonId(chargeOffReasonId));

        loanDetails = getLoanDetails(loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries For ChargeOff Transaction
        GetJournalEntriesTransactionIdResponse journalEntriesForChargeOff = getJournalEntries(
                "L" + chargeOffTransaction.getResourceId().toString());

        assertNotNull(journalEntriesForChargeOff);
        journalEntries = journalEntriesForChargeOff.getPageItems();
        assertEquals(2, journalEntries.size());

        assertEquals(910, journalEntries.get(1).getAmount());
        assertEquals(LocalDate.of(2022, 9, 14), journalEntries.get(1).getTransactionDate());
        assertEquals(loansReceivable.getAccountID().longValue(), journalEntries.get(1).getGlAccountId().longValue());
        assertEquals("CREDIT", journalEntries.get(1).getEntryType().getValue());

        assertEquals(910, journalEntries.get(0).getAmount());
        assertEquals(LocalDate.of(2022, 9, 14), journalEntries.get(0).getTransactionDate());
        assertEquals(creditLossBadDebt.getAccountID().longValue(), journalEntries.get(0).getGlAccountId().longValue());
        assertEquals("DEBIT", journalEntries.get(0).getEntryType().getValue());

        // reverse Repayment
        reverseRepayment(loanId, repaymentTransaction.getResourceId(), "7 September 2022");
        loanDetails = getLoanDetails(loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries for Reversed Repayment transaction
        journalEntriesForRepayment = getJournalEntries("L" + repaymentTransaction.getResourceId().toString());

        assertNotNull(journalEntriesForRepayment);

        journalEntries = journalEntriesForRepayment.getPageItems();
        assertEquals(6, journalEntries.size());

        assertEquals(90, journalEntries.get(5).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(5).getTransactionDate());
        assertEquals(loansReceivable.getAccountID().longValue(), journalEntries.get(5).getGlAccountId().longValue());
        assertEquals("CREDIT", journalEntries.get(5).getEntryType().getValue());

        assertEquals(10, journalEntries.get(4).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(4).getTransactionDate());
        assertEquals(interestFeeReceivable.getAccountID().longValue(), journalEntries.get(4).getGlAccountId().longValue());
        assertEquals("CREDIT", journalEntries.get(4).getEntryType().getValue());

        assertEquals(100, journalEntries.get(3).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(3).getTransactionDate());
        assertEquals(suspenseClearingAccount.getAccountID().longValue(), journalEntries.get(3).getGlAccountId().longValue());
        assertEquals("DEBIT", journalEntries.get(3).getEntryType().getValue());

        assertEquals(90, journalEntries.get(2).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(2).getTransactionDate());
        assertEquals(loansReceivable.getAccountID().longValue(), journalEntries.get(2).getGlAccountId().longValue());
        assertEquals("DEBIT", journalEntries.get(2).getEntryType().getValue());

        assertEquals(10, journalEntries.get(1).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(1).getTransactionDate());
        assertEquals(interestFeeReceivable.getAccountID().longValue(), journalEntries.get(1).getGlAccountId().longValue());
        assertEquals("DEBIT", journalEntries.get(1).getEntryType().getValue());

        assertEquals(100, journalEntries.get(0).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(0).getTransactionDate());
        assertEquals(suspenseClearingAccount.getAccountID().longValue(), journalEntries.get(0).getGlAccountId().longValue());
        assertEquals("CREDIT", journalEntries.get(0).getEntryType().getValue());

    }

    @Test
    public void postChargeOffBackdatedTransactionReverseReplayTest() {
        String loanExternalIdStr = UUID.randomUUID().toString();
        final Long loanProductId = createLoanProductWithPeriodicAccrualAccounting();
        final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
        final Long loanId = createLoanAccount(clientId, loanProductId, loanExternalIdStr);

        // apply charges
        Long feeCharge = createLoanSpecifiedDueDateCharge(10.0);

        LocalDate targetDate = LocalDate.of(2022, 9, 5);
        final String feeCharge1AddedDate = DATE_FORMATTER.format(targetDate);
        addLoanCharge(loanId, feeCharge, feeCharge1AddedDate, 10.0);

        // Set Loan transaction externalId for transaction getting reversed and replayed
        String loanTransactionExternalIdStr = UUID.randomUUID().toString();

        // make Repayment
        final PostLoansLoanIdTransactionsResponse repaymentTransaction = makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate("7 September 2022")
                        .locale(LoanTestData.LOCALE).transactionAmount(11.0).externalId(loanTransactionExternalIdStr));

        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        assertTrue(loanDetails.getStatus().getActive());

        // verify Journal Entries for Repayment transaction
        GetJournalEntriesTransactionIdResponse journalEntriesForRepayment = getJournalEntries(
                "L" + repaymentTransaction.getResourceId().toString());

        assertNotNull(journalEntriesForRepayment);

        List<JournalEntryTransactionItem> journalEntries = journalEntriesForRepayment.getPageItems();
        assertEquals(3, journalEntries.size());

        assertEquals(1, journalEntries.get(2).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(2).getTransactionDate());
        assertEquals(loansReceivable.getAccountID().longValue(), journalEntries.get(2).getGlAccountId().longValue());
        assertEquals("CREDIT", journalEntries.get(2).getEntryType().getValue());

        assertEquals(10, journalEntries.get(1).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(1).getTransactionDate());
        assertEquals(interestFeeReceivable.getAccountID().longValue(), journalEntries.get(1).getGlAccountId().longValue());
        assertEquals("CREDIT", journalEntries.get(1).getEntryType().getValue());

        assertEquals(11, journalEntries.get(0).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(0).getTransactionDate());
        assertEquals(suspenseClearingAccount.getAccountID().longValue(), journalEntries.get(0).getGlAccountId().longValue());
        assertEquals("DEBIT", journalEntries.get(0).getEntryType().getValue());

        // set loan as chargeoff
        String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6) + Utils.randomStringGenerator("is", 5);
        Long chargeOffReasonId = codeHelper.createChargeOffCodeValue(randomText, 1);
        String transactionExternalId = UUID.randomUUID().toString();
        PostLoansLoanIdTransactionsResponse chargeOffTransaction = chargeOffLoan(loanId,
                new PostLoansLoanIdTransactionsRequest().transactionDate("14 September 2022").locale(LoanTestData.LOCALE)
                        .dateFormat(LoanTestData.DATETIME_PATTERN).externalId(transactionExternalId).chargeOffReasonId(chargeOffReasonId));

        loanDetails = getLoanDetails(loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries For ChargeOff Transaction
        GetJournalEntriesTransactionIdResponse journalEntriesForChargeOff = getJournalEntries(
                "L" + chargeOffTransaction.getResourceId().toString());

        assertNotNull(journalEntriesForChargeOff);
        journalEntries = journalEntriesForChargeOff.getPageItems();
        assertEquals(2, journalEntries.size());

        assertEquals(999, journalEntries.get(1).getAmount());
        assertEquals(LocalDate.of(2022, 9, 14), journalEntries.get(1).getTransactionDate());
        assertEquals(loansReceivable.getAccountID().longValue(), journalEntries.get(1).getGlAccountId().longValue());
        assertEquals("CREDIT", journalEntries.get(1).getEntryType().getValue());

        assertEquals(999, journalEntries.get(0).getAmount());
        assertEquals(LocalDate.of(2022, 9, 14), journalEntries.get(0).getTransactionDate());
        assertEquals(creditLossBadDebt.getAccountID().longValue(), journalEntries.get(0).getGlAccountId().longValue());
        assertEquals("DEBIT", journalEntries.get(0).getEntryType().getValue());

        // make Repayment backdated for reverse replay
        final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate("5 September 2022")
                        .locale(LoanTestData.LOCALE).transactionAmount(5.0));

        loanDetails = getLoanDetails(loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries for Repayment transaction
        journalEntriesForRepayment = getJournalEntries("L" + repaymentTransaction_1.getResourceId().toString());

        assertNotNull(journalEntriesForRepayment);

        journalEntries = journalEntriesForRepayment.getPageItems();
        assertEquals(2, journalEntries.size());

        assertEquals(5, journalEntries.get(1).getAmount());
        assertEquals(LocalDate.of(2022, 9, 5), journalEntries.get(1).getTransactionDate());
        assertEquals(interestFeeReceivable.getAccountID().longValue(), journalEntries.get(1).getGlAccountId().longValue());
        assertEquals("CREDIT", journalEntries.get(1).getEntryType().getValue());

        assertEquals(5, journalEntries.get(0).getAmount());
        assertEquals(LocalDate.of(2022, 9, 5), journalEntries.get(0).getTransactionDate());
        assertEquals(suspenseClearingAccount.getAccountID().longValue(), journalEntries.get(0).getGlAccountId().longValue());
        assertEquals("DEBIT", journalEntries.get(0).getEntryType().getValue());

        // check reverse replay
        Long reversedAndReplayedTransactionId = repaymentTransaction.getResourceId();
        GetLoansLoanIdTransactionsTransactionIdResponse getLoansTransactionResponse = getLoanTransactionDetails(loanId,
                loanTransactionExternalIdStr);
        assertNotNull(getLoansTransactionResponse);
        assertNotNull(getLoansTransactionResponse.getTransactionRelations());

        // test replayed relationship
        GetLoanTransactionRelation transactionRelation = getLoansTransactionResponse.getTransactionRelations().iterator().next();
        assertEquals(reversedAndReplayedTransactionId, transactionRelation.getToLoanTransaction());
        assertEquals("REPLAYED", transactionRelation.getRelationType());

        // verify Journal Entries for new Transaction
        journalEntriesForRepayment = getJournalEntries("L" + getLoansTransactionResponse.getId().toString());

        assertNotNull(journalEntriesForRepayment);

        journalEntries = journalEntriesForRepayment.getPageItems();
        assertEquals(3, journalEntries.size());

        assertEquals(6, journalEntries.get(2).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(2).getTransactionDate());
        assertEquals(loansReceivable.getAccountID().longValue(), journalEntries.get(2).getGlAccountId().longValue());
        assertEquals("CREDIT", journalEntries.get(2).getEntryType().getValue());

        assertEquals(5, journalEntries.get(1).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(1).getTransactionDate());
        assertEquals(interestFeeReceivable.getAccountID().longValue(), journalEntries.get(1).getGlAccountId().longValue());
        assertEquals("CREDIT", journalEntries.get(1).getEntryType().getValue());

        assertEquals(11, journalEntries.get(0).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(0).getTransactionDate());
        assertEquals(suspenseClearingAccount.getAccountID().longValue(), journalEntries.get(0).getGlAccountId().longValue());
        assertEquals("DEBIT", journalEntries.get(0).getEntryType().getValue());

    }

    @Test
    @Disabled("Requires: FINERACT-1946")
    public void transactionOnChargeOffDatePreChargeOffReverseReplayTest() {
        String loanExternalIdStr = UUID.randomUUID().toString();
        final Long loanProductId = createLoanProductWithPeriodicAccrualAccounting();
        final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
        final Long loanId = createLoanAccount(clientId, loanProductId, loanExternalIdStr);

        // apply charges
        Long feeCharge = createLoanSpecifiedDueDateCharge(10.0);

        LocalDate targetDate = LocalDate.of(2022, 9, 5);
        final String feeCharge1AddedDate = DATE_FORMATTER.format(targetDate);
        addLoanCharge(loanId, feeCharge, feeCharge1AddedDate, 10.0);

        // Set Loan transaction externalId for transaction getting reversed and replayed
        String loanTransactionExternalIdStr = UUID.randomUUID().toString();

        // make Repayment on Chargeoff date before charge off
        final PostLoansLoanIdTransactionsResponse repaymentTransaction = makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate("7 September 2022")
                        .locale(LoanTestData.LOCALE).transactionAmount(11.0).externalId(loanTransactionExternalIdStr));

        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        assertTrue(loanDetails.getStatus().getActive());

        // verify Journal Entries for Repayment transaction
        GetJournalEntriesTransactionIdResponse journalEntriesForRepayment = getJournalEntries(
                "L" + repaymentTransaction.getResourceId().toString());

        List<JournalEntryTransactionItem> journalEntries = journalEntriesForRepayment.getPageItems();
        assertEquals(3, journalEntries.size());

        assertEquals(1, journalEntries.get(2).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(2).getTransactionDate());
        assertEquals(loansReceivable.getAccountID().longValue(), journalEntries.get(2).getGlAccountId().longValue());
        assertEquals("CREDIT", journalEntries.get(2).getEntryType().getValue());

        assertEquals(10, journalEntries.get(1).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(1).getTransactionDate());
        assertEquals(interestFeeReceivable.getAccountID().longValue(), journalEntries.get(1).getGlAccountId().longValue());
        assertEquals("CREDIT", journalEntries.get(1).getEntryType().getValue());

        assertEquals(11, journalEntries.get(0).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(0).getTransactionDate());
        assertEquals(suspenseClearingAccount.getAccountID().longValue(), journalEntries.get(0).getGlAccountId().longValue());
        assertEquals("DEBIT", journalEntries.get(0).getEntryType().getValue());

        // set loan as chargeoff
        String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6) + Utils.randomStringGenerator("is", 5);
        Long chargeOffReasonId = codeHelper.createChargeOffCodeValue(randomText, 1);
        String transactionExternalId = UUID.randomUUID().toString();
        PostLoansLoanIdTransactionsResponse chargeOffTransaction = chargeOffLoan(loanId,
                new PostLoansLoanIdTransactionsRequest().transactionDate("7 September 2022").locale(LoanTestData.LOCALE)
                        .dateFormat(LoanTestData.DATETIME_PATTERN).externalId(transactionExternalId).chargeOffReasonId(chargeOffReasonId));

        loanDetails = getLoanDetails(loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries For ChargeOff Transaction
        GetJournalEntriesTransactionIdResponse journalEntriesForChargeOff = getJournalEntries(
                "L" + chargeOffTransaction.getResourceId().toString());

        assertNotNull(journalEntriesForChargeOff);
        journalEntries = journalEntriesForChargeOff.getPageItems();
        assertEquals(2, journalEntries.size());

        assertEquals(999, journalEntries.get(1).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(1).getTransactionDate());
        assertEquals(loansReceivable.getAccountID().longValue(), journalEntries.get(1).getGlAccountId().longValue());
        assertEquals("CREDIT", journalEntries.get(1).getEntryType().getValue());

        assertEquals(999, journalEntries.get(0).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(0).getTransactionDate());
        assertEquals(creditLossBadDebt.getAccountID().longValue(), journalEntries.get(0).getGlAccountId().longValue());
        assertEquals("DEBIT", journalEntries.get(0).getEntryType().getValue());

        // make Repayment backdated for reverse replay
        final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate("5 September 2022")
                        .locale(LoanTestData.LOCALE).transactionAmount(5.0));

        loanDetails = getLoanDetails(loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries for Repayment transaction
        journalEntriesForRepayment = getJournalEntries("L" + repaymentTransaction_1.getResourceId().toString());

        assertNotNull(journalEntriesForRepayment);

        journalEntries = journalEntriesForRepayment.getPageItems();
        assertEquals(2, journalEntries.size());

        assertEquals(5, journalEntries.get(1).getAmount());
        assertEquals(LocalDate.of(2022, 9, 5), journalEntries.get(1).getTransactionDate());
        assertEquals(interestFeeReceivable.getAccountID().longValue(), journalEntries.get(1).getGlAccountId().longValue());
        assertEquals("CREDIT", journalEntries.get(1).getEntryType().getValue());

        assertEquals(5, journalEntries.get(0).getAmount());
        assertEquals(LocalDate.of(2022, 9, 5), journalEntries.get(0).getTransactionDate());
        assertEquals(suspenseClearingAccount.getAccountID().longValue(), journalEntries.get(0).getGlAccountId().longValue());
        assertEquals("DEBIT", journalEntries.get(0).getEntryType().getValue());

        // check reverse replay
        Long reversedAndReplayedTransactionId = repaymentTransaction.getResourceId();
        GetLoansLoanIdTransactionsTransactionIdResponse getLoansTransactionResponse = getLoanTransactionDetails(loanId,
                loanTransactionExternalIdStr);
        assertNotNull(getLoansTransactionResponse);
        assertNotNull(getLoansTransactionResponse.getTransactionRelations());

        // test replayed relationship
        GetLoanTransactionRelation transactionRelation = getLoansTransactionResponse.getTransactionRelations().iterator().next();
        assertEquals(reversedAndReplayedTransactionId, transactionRelation.getToLoanTransaction());
        assertEquals("REPLAYED", transactionRelation.getRelationType());

        // verify Journal Entries for new Transaction

        journalEntriesForRepayment = getJournalEntries("L" + getLoansTransactionResponse.getId().toString());

        assertNotNull(journalEntriesForRepayment);

        journalEntries = journalEntriesForRepayment.getPageItems();
        assertEquals(3, journalEntries.size());

        assertEquals(6, journalEntries.get(2).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(2).getTransactionDate());
        assertEquals(loansReceivable.getAccountID().longValue(), journalEntries.get(2).getGlAccountId().longValue());
        assertEquals("CREDIT", journalEntries.get(2).getEntryType().getValue());

        assertEquals(5, journalEntries.get(1).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(1).getTransactionDate());
        assertEquals(interestFeeReceivable.getAccountID().longValue(), journalEntries.get(1).getGlAccountId().longValue());
        assertEquals("CREDIT", journalEntries.get(1).getEntryType().getValue());

        assertEquals(11, journalEntries.get(0).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(0).getTransactionDate());
        assertEquals(suspenseClearingAccount.getAccountID().longValue(), journalEntries.get(0).getGlAccountId().longValue());
        assertEquals("DEBIT", journalEntries.get(0).getEntryType().getValue());

    }

    @Test
    @Disabled("Requires: FINERACT-1946")
    public void transactionOnChargeOffDatePostChargeOffReverseReplayTest() {
        String loanExternalIdStr = UUID.randomUUID().toString();
        final Long loanProductId = createLoanProductWithPeriodicAccrualAccounting();
        final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
        final Long loanId = createLoanAccount(clientId, loanProductId, loanExternalIdStr);

        // apply charges
        Long feeCharge = createLoanSpecifiedDueDateCharge(10.0);

        LocalDate targetDate = LocalDate.of(2022, 9, 5);
        final String feeCharge1AddedDate = DATE_FORMATTER.format(targetDate);
        addLoanCharge(loanId, feeCharge, feeCharge1AddedDate, 10.0);

        // set loan as chargeoff
        String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6) + Utils.randomStringGenerator("is", 5);
        Long chargeOffReasonId = codeHelper.createChargeOffCodeValue(randomText, 1);
        String transactionExternalId = UUID.randomUUID().toString();
        PostLoansLoanIdTransactionsResponse chargeOffTransaction = chargeOffLoan(loanId,
                new PostLoansLoanIdTransactionsRequest().transactionDate("7 September 2022").locale(LoanTestData.LOCALE)
                        .dateFormat(LoanTestData.DATETIME_PATTERN).externalId(transactionExternalId).chargeOffReasonId(chargeOffReasonId));

        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries For ChargeOff Transaction
        GetJournalEntriesTransactionIdResponse journalEntriesForChargeOff = getJournalEntries(
                "L" + chargeOffTransaction.getResourceId().toString());

        assertNotNull(journalEntriesForChargeOff);
        List<JournalEntryTransactionItem> journalEntries = journalEntriesForChargeOff.getPageItems();
        assertEquals(4, journalEntries.size());

        assertEquals(1000, journalEntries.get(3).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(3).getTransactionDate());
        assertEquals(loansReceivable.getAccountID().longValue(), journalEntries.get(3).getGlAccountId().longValue());
        assertEquals("CREDIT", journalEntries.get(3).getEntryType().getValue());

        assertEquals(10, journalEntries.get(2).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(2).getTransactionDate());
        assertEquals(interestFeeReceivable.getAccountID().longValue(), journalEntries.get(2).getGlAccountId().longValue());
        assertEquals("CREDIT", journalEntries.get(2).getEntryType().getValue());

        assertEquals(1000, journalEntries.get(1).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(1).getTransactionDate());
        assertEquals(creditLossBadDebt.getAccountID().longValue(), journalEntries.get(1).getGlAccountId().longValue());
        assertEquals("DEBIT", journalEntries.get(1).getEntryType().getValue());

        assertEquals(10, journalEntries.get(0).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(0).getTransactionDate());
        assertEquals(feeChargeOff.getAccountID().longValue(), journalEntries.get(0).getGlAccountId().longValue());
        assertEquals("DEBIT", journalEntries.get(0).getEntryType().getValue());

        // Set Loan transaction externalId for transaction getting reversed and replayed
        String loanTransactionExternalIdStr = UUID.randomUUID().toString();

        // make Repayment after charge-off on charge off date
        final PostLoansLoanIdTransactionsResponse repaymentTransaction = makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate("7 September 2022")
                        .locale(LoanTestData.LOCALE).transactionAmount(11.0).externalId(loanTransactionExternalIdStr));

        loanDetails = getLoanDetails(loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries for Repayment transaction
        GetJournalEntriesTransactionIdResponse journalEntriesForRepayment = getJournalEntries(
                "L" + repaymentTransaction.getResourceId().toString());

        assertNotNull(journalEntriesForRepayment);

        journalEntries = journalEntriesForRepayment.getPageItems();
        assertEquals(2, journalEntries.size());

        assertEquals(11, journalEntries.get(1).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(1).getTransactionDate());
        assertEquals(recoveries.getAccountID().longValue(), journalEntries.get(1).getGlAccountId().longValue());
        assertEquals("CREDIT", journalEntries.get(1).getEntryType().getValue());

        assertEquals(11, journalEntries.get(0).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(0).getTransactionDate());
        assertEquals(suspenseClearingAccount.getAccountID().longValue(), journalEntries.get(0).getGlAccountId().longValue());
        assertEquals("DEBIT", journalEntries.get(0).getEntryType().getValue());

        // make Repayment backdated for reverse replay
        final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate("5 September 2022")
                        .locale(LoanTestData.LOCALE).transactionAmount(5.0));

        loanDetails = getLoanDetails(loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries for Repayment transaction
        journalEntriesForRepayment = getJournalEntries("L" + repaymentTransaction_1.getResourceId().toString());
        assertNotNull(journalEntriesForRepayment);

        journalEntries = journalEntriesForRepayment.getPageItems();
        assertEquals(2, journalEntries.size());

        assertEquals(5, journalEntries.get(1).getAmount());
        assertEquals(LocalDate.of(2022, 9, 5), journalEntries.get(1).getTransactionDate());
        assertEquals(interestFeeReceivable.getAccountID().longValue(), journalEntries.get(1).getGlAccountId().longValue());
        assertEquals("CREDIT", journalEntries.get(1).getEntryType().getValue());

        assertEquals(5, journalEntries.get(0).getAmount());
        assertEquals(LocalDate.of(2022, 9, 5), journalEntries.get(0).getTransactionDate());
        assertEquals(suspenseClearingAccount.getAccountID().longValue(), journalEntries.get(0).getGlAccountId().longValue());
        assertEquals("DEBIT", journalEntries.get(0).getEntryType().getValue());

        // check reverse replay
        Long reversedAndReplayedTransactionId = repaymentTransaction.getResourceId();
        GetLoansLoanIdTransactionsTransactionIdResponse getLoansTransactionResponse = getLoanTransactionDetails(loanId,
                loanTransactionExternalIdStr);
        assertNotNull(getLoansTransactionResponse);
        assertNotNull(getLoansTransactionResponse.getTransactionRelations());

        // test replayed relationship
        GetLoanTransactionRelation transactionRelation = getLoansTransactionResponse.getTransactionRelations().iterator().next();
        assertEquals(reversedAndReplayedTransactionId, transactionRelation.getToLoanTransaction());
        assertEquals("REPLAYED", transactionRelation.getRelationType());

        // verify Journal Entries for new Transaction
        journalEntriesForRepayment = getJournalEntries("L" + getLoansTransactionResponse.getId().toString());

        assertNotNull(journalEntriesForRepayment);

        journalEntries = journalEntriesForRepayment.getPageItems();
        assertEquals(2, journalEntries.size());

        assertEquals(11, journalEntries.get(1).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(1).getTransactionDate());
        assertEquals(recoveries.getAccountID().longValue(), journalEntries.get(1).getGlAccountId().longValue());
        assertEquals("CREDIT", journalEntries.get(1).getEntryType().getValue());

        assertEquals(11, journalEntries.get(0).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(0).getTransactionDate());
        assertEquals(suspenseClearingAccount.getAccountID().longValue(), journalEntries.get(0).getGlAccountId().longValue());
        assertEquals("DEBIT", journalEntries.get(0).getEntryType().getValue());
    }

    @Test
    public void transactionsOnChargeOffDatePreAndPostChargeOffDateTest() {
        String loanExternalIdStr = UUID.randomUUID().toString();
        final Long loanProductId = createLoanProductWithPeriodicAccrualAccounting();
        final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
        final Long loanId = createLoanAccount(clientId, loanProductId, loanExternalIdStr);

        // apply charges
        Long feeCharge = createLoanSpecifiedDueDateCharge(10.0);

        LocalDate targetDate = LocalDate.of(2022, 9, 5);
        final String feeCharge1AddedDate = DATE_FORMATTER.format(targetDate);
        addLoanCharge(loanId, feeCharge, feeCharge1AddedDate, 10.0);

        // make Repayment before charge-off on charge off date
        final PostLoansLoanIdTransactionsResponse repaymentTransaction = makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate("7 September 2022")
                        .locale(LoanTestData.LOCALE).transactionAmount(100.0));

        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        assertTrue(loanDetails.getStatus().getActive());

        // verify Journal Entries for Repayment transaction
        GetJournalEntriesTransactionIdResponse journalEntriesForRepayment = getJournalEntries(
                "L" + repaymentTransaction.getResourceId().toString());

        assertNotNull(journalEntriesForRepayment);

        List<JournalEntryTransactionItem> journalEntries = journalEntriesForRepayment.getPageItems();
        assertEquals(3, journalEntries.size());

        assertEquals(90, journalEntries.get(2).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(2).getTransactionDate());
        assertEquals(loansReceivable.getAccountID().longValue(), journalEntries.get(2).getGlAccountId().longValue());
        assertEquals("CREDIT", journalEntries.get(2).getEntryType().getValue());

        assertEquals(10, journalEntries.get(1).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(1).getTransactionDate());
        assertEquals(interestFeeReceivable.getAccountID().longValue(), journalEntries.get(1).getGlAccountId().longValue());
        assertEquals("CREDIT", journalEntries.get(1).getEntryType().getValue());

        assertEquals(100, journalEntries.get(0).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(0).getTransactionDate());
        assertEquals(suspenseClearingAccount.getAccountID().longValue(), journalEntries.get(0).getGlAccountId().longValue());
        assertEquals("DEBIT", journalEntries.get(0).getEntryType().getValue());

        // set loan as chargeoff
        String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6) + Utils.randomStringGenerator("is", 5);
        Long chargeOffReasonId = codeHelper.createChargeOffCodeValue(randomText, 1);
        String transactionExternalId = UUID.randomUUID().toString();
        PostLoansLoanIdTransactionsResponse chargeOffTransaction = chargeOffLoan(loanId,
                new PostLoansLoanIdTransactionsRequest().transactionDate("7 September 2022").locale(LoanTestData.LOCALE)
                        .dateFormat(LoanTestData.DATETIME_PATTERN).externalId(transactionExternalId).chargeOffReasonId(chargeOffReasonId));

        loanDetails = getLoanDetails(loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries For ChargeOff Transaction
        GetJournalEntriesTransactionIdResponse journalEntriesForChargeOff = getJournalEntries(
                "L" + chargeOffTransaction.getResourceId().toString());

        assertNotNull(journalEntriesForChargeOff);
        journalEntries = journalEntriesForChargeOff.getPageItems();
        assertEquals(2, journalEntries.size());

        assertEquals(910, journalEntries.get(1).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(1).getTransactionDate());
        assertEquals(loansReceivable.getAccountID().longValue(), journalEntries.get(1).getGlAccountId().longValue());
        assertEquals("CREDIT", journalEntries.get(1).getEntryType().getValue());

        assertEquals(910, journalEntries.get(0).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(0).getTransactionDate());
        assertEquals(creditLossBadDebt.getAccountID().longValue(), journalEntries.get(0).getGlAccountId().longValue());
        assertEquals("DEBIT", journalEntries.get(0).getEntryType().getValue());

        // make Repayment after charge-off on charge off date
        final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate("7 September 2022")
                        .locale(LoanTestData.LOCALE).transactionAmount(90.0));

        loanDetails = getLoanDetails(loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries for Repayment transaction
        journalEntriesForRepayment = getJournalEntries("L" + repaymentTransaction_1.getResourceId().toString());

        assertNotNull(journalEntriesForRepayment);

        journalEntries = journalEntriesForRepayment.getPageItems();
        assertEquals(2, journalEntries.size());

        assertEquals(90, journalEntries.get(1).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(1).getTransactionDate());
        assertEquals(recoveries.getAccountID().longValue(), journalEntries.get(1).getGlAccountId().longValue());
        assertEquals("CREDIT", journalEntries.get(1).getEntryType().getValue());

        assertEquals(90, journalEntries.get(0).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(0).getTransactionDate());
        assertEquals(suspenseClearingAccount.getAccountID().longValue(), journalEntries.get(0).getGlAccountId().longValue());
        assertEquals("DEBIT", journalEntries.get(0).getEntryType().getValue());

    }

    @Test
    public void transactionOnChargeOffDateReverseTest() {
        String loanExternalIdStr = UUID.randomUUID().toString();
        final Long loanProductId = createLoanProductWithPeriodicAccrualAccounting();
        final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
        final Long loanId = createLoanAccount(clientId, loanProductId, loanExternalIdStr);

        // apply charges
        Long feeCharge = createLoanSpecifiedDueDateCharge(10.0);

        LocalDate targetDate = LocalDate.of(2022, 9, 5);
        final String feeCharge1AddedDate = DATE_FORMATTER.format(targetDate);
        addLoanCharge(loanId, feeCharge, feeCharge1AddedDate, 10.0);

        // make Repayment before charge-off on charge off date
        final PostLoansLoanIdTransactionsResponse repaymentTransaction = makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate("7 September 2022")
                        .locale(LoanTestData.LOCALE).transactionAmount(100.0));

        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        assertTrue(loanDetails.getStatus().getActive());

        // verify Journal Entries for Repayment transaction
        GetJournalEntriesTransactionIdResponse journalEntriesForRepayment = getJournalEntries(
                "L" + repaymentTransaction.getResourceId().toString());

        assertNotNull(journalEntriesForRepayment);

        List<JournalEntryTransactionItem> journalEntries = journalEntriesForRepayment.getPageItems();
        assertEquals(3, journalEntries.size());

        assertEquals(90, journalEntries.get(2).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(2).getTransactionDate());
        assertEquals(loansReceivable.getAccountID().longValue(), journalEntries.get(2).getGlAccountId().longValue());
        assertEquals("CREDIT", journalEntries.get(2).getEntryType().getValue());

        assertEquals(10, journalEntries.get(1).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(1).getTransactionDate());
        assertEquals(interestFeeReceivable.getAccountID().longValue(), journalEntries.get(1).getGlAccountId().longValue());
        assertEquals("CREDIT", journalEntries.get(1).getEntryType().getValue());

        assertEquals(100, journalEntries.get(0).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(0).getTransactionDate());
        assertEquals(suspenseClearingAccount.getAccountID().longValue(), journalEntries.get(0).getGlAccountId().longValue());
        assertEquals("DEBIT", journalEntries.get(0).getEntryType().getValue());

        // set loan as chargeoff
        String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6) + Utils.randomStringGenerator("is", 5);
        Long chargeOffReasonId = codeHelper.createChargeOffCodeValue(randomText, 1);
        String transactionExternalId = UUID.randomUUID().toString();
        PostLoansLoanIdTransactionsResponse chargeOffTransaction = chargeOffLoan(loanId,
                new PostLoansLoanIdTransactionsRequest().transactionDate("7 September 2022").locale(LoanTestData.LOCALE)
                        .dateFormat(LoanTestData.DATETIME_PATTERN).externalId(transactionExternalId).chargeOffReasonId(chargeOffReasonId));

        loanDetails = getLoanDetails(loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries For ChargeOff Transaction
        GetJournalEntriesTransactionIdResponse journalEntriesForChargeOff = getJournalEntries(
                "L" + chargeOffTransaction.getResourceId().toString());

        assertNotNull(journalEntriesForChargeOff);
        journalEntries = journalEntriesForChargeOff.getPageItems();
        assertEquals(2, journalEntries.size());

        assertEquals(910, journalEntries.get(1).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(1).getTransactionDate());
        assertEquals(loansReceivable.getAccountID().longValue(), journalEntries.get(1).getGlAccountId().longValue());
        assertEquals("CREDIT", journalEntries.get(1).getEntryType().getValue());

        assertEquals(910, journalEntries.get(0).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(0).getTransactionDate());
        assertEquals(creditLossBadDebt.getAccountID().longValue(), journalEntries.get(0).getGlAccountId().longValue());
        assertEquals("DEBIT", journalEntries.get(0).getEntryType().getValue());

        // reverse Repayment
        reverseRepayment(loanId, repaymentTransaction.getResourceId(), "7 September 2022");
        loanDetails = getLoanDetails(loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries for Reversed Repayment transaction
        journalEntriesForRepayment = getJournalEntries("L" + repaymentTransaction.getResourceId().toString());
        assertNotNull(journalEntriesForRepayment);

        journalEntries = journalEntriesForRepayment.getPageItems();
        assertEquals(6, journalEntries.size());

        assertEquals(90, journalEntries.get(5).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(5).getTransactionDate());
        assertEquals(loansReceivable.getAccountID().longValue(), journalEntries.get(5).getGlAccountId().longValue());
        assertEquals("CREDIT", journalEntries.get(5).getEntryType().getValue());

        assertEquals(10, journalEntries.get(4).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(4).getTransactionDate());
        assertEquals(interestFeeReceivable.getAccountID().longValue(), journalEntries.get(4).getGlAccountId().longValue());
        assertEquals("CREDIT", journalEntries.get(4).getEntryType().getValue());

        assertEquals(100, journalEntries.get(3).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(3).getTransactionDate());
        assertEquals(suspenseClearingAccount.getAccountID().longValue(), journalEntries.get(3).getGlAccountId().longValue());
        assertEquals("DEBIT", journalEntries.get(3).getEntryType().getValue());

        assertEquals(90, journalEntries.get(2).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(2).getTransactionDate());
        assertEquals(loansReceivable.getAccountID().longValue(), journalEntries.get(2).getGlAccountId().longValue());
        assertEquals("DEBIT", journalEntries.get(2).getEntryType().getValue());

        assertEquals(10, journalEntries.get(1).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(1).getTransactionDate());
        assertEquals(interestFeeReceivable.getAccountID().longValue(), journalEntries.get(1).getGlAccountId().longValue());
        assertEquals("DEBIT", journalEntries.get(1).getEntryType().getValue());

        assertEquals(100, journalEntries.get(0).getAmount());
        assertEquals(LocalDate.of(2022, 9, 7), journalEntries.get(0).getTransactionDate());
        assertEquals(suspenseClearingAccount.getAccountID().longValue(), journalEntries.get(0).getGlAccountId().longValue());
        assertEquals("CREDIT", journalEntries.get(0).getEntryType().getValue());

    }

    private Long createLoanAccount(final Long clientId, final Long loanProductId, final String externalId) {
        PostLoansRequest request = applyLoanRequest(clientId, loanProductId, "01 September 2022", 1000.0, 1,
                req -> req.expectedDisbursementDate("03 September 2022").externalId(externalId).interestRatePerPeriod(BigDecimal.ZERO)
                        .interestType(LoanTestData.InterestType.DECLINING_BALANCE)
                        .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)
                        .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)
                        .repaymentEvery(30).repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.DAYS).loanTermFrequency(30)
                        .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.DAYS).dateFormat(LoanTestData.DATETIME_PATTERN)
                        .locale(LoanTestData.LOCALE));
        Long loanId = applyForLoan(request);
        approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, "02 September 2022", "03 September 2022"));
        disburseLoanWithAmount(loanId, "03 September 2022", 1000.0);
        return loanId;
    }

    private Long createLoanProductWithPeriodicAccrualAccounting() {

        String name = Utils.uniqueRandomStringGenerator("LOAN_PRODUCT_", 6);
        String shortName = Utils.uniqueRandomStringGenerator("", 4);

        List<Integer> principalVariationsForBorrowerCycle = new ArrayList<>();
        List<Integer> numberOfRepaymentVariationsForBorrowerCycle = new ArrayList<>();
        List<Integer> interestRateVariationsForBorrowerCycle = new ArrayList<>();
        List<LoanProductChargeData> charges = new ArrayList<>();
        List<LoanProductChargeToGLAccountMapper> penaltyToIncomeAccountMappings = new ArrayList<>();
        List<LoanProductChargeToGLAccountMapper> feeToIncomeAccountMappings = new ArrayList<>();

        String paymentTypeName = PaymentTypeHelper.randomNameGenerator("P_T", 5);
        String description = PaymentTypeHelper.randomNameGenerator("PT_Desc", 15);
        Boolean isCashPayment = false;
        Long position = 1L;

        var paymentTypesResponse = PaymentTypeHelper.createPaymentType(new PaymentTypeCreateRequest().name(paymentTypeName)
                .description(description).isCashPayment(isCashPayment).position(position));
        Long paymentTypeIdOne = paymentTypesResponse.getResourceId();
        Assertions.assertNotNull(paymentTypeIdOne);

        List<GetLoanPaymentChannelToFundSourceMappings> paymentChannelToFundSourceMappings = new ArrayList<>();
        GetLoanPaymentChannelToFundSourceMappings loanPaymentChannelToFundSourceMappings = new GetLoanPaymentChannelToFundSourceMappings();
        loanPaymentChannelToFundSourceMappings.fundSourceAccountId(fundReceivables.getAccountID().longValue());
        loanPaymentChannelToFundSourceMappings.paymentTypeId(paymentTypeIdOne.longValue());
        paymentChannelToFundSourceMappings.add(loanPaymentChannelToFundSourceMappings);

        final Long fundId = FundsResourceHandler.createFund().getResourceId();
        Assertions.assertNotNull(fundId);

        final Long delinquencyBucketId = DelinquencyBucketsHelper.createDefaultBucket();

        PostLoanProductsRequest loanProductsRequest = new PostLoanProductsRequest().name(name)//
                .shortName(shortName)//
                .description("Loan Product Description")//
                .fundId(fundId)//
                .startDate(null)//
                .closeDate(null)//
                .includeInBorrowerCycle(false)//
                .currencyCode("USD")//
                .digitsAfterDecimal(2)//
                .inMultiplesOf(0)//
                .installmentAmountInMultiplesOf(1)//
                .useBorrowerCycle(false)//
                .minPrincipal(100.0)//
                .principal(1000.0)//
                .maxPrincipal(10000.0)//
                .minNumberOfRepayments(1)//
                .numberOfRepayments(1)//
                .maxNumberOfRepayments(30)//
                .isLinkedToFloatingInterestRates(false)//
                .minInterestRatePerPeriod((double) 0)//
                .interestRatePerPeriod((double) 0)//
                .maxInterestRatePerPeriod((double) 0)//
                .interestRateFrequencyType(2)//
                .repaymentEvery(30)//
                .repaymentFrequencyType(0L)//
                .principalVariationsForBorrowerCycle(principalVariationsForBorrowerCycle)//
                .numberOfRepaymentVariationsForBorrowerCycle(numberOfRepaymentVariationsForBorrowerCycle)//
                .interestRateVariationsForBorrowerCycle(interestRateVariationsForBorrowerCycle)//
                .amortizationType(1)//
                .interestType(0)//
                .isEqualAmortization(false)//
                .interestCalculationPeriodType(1)//
                .transactionProcessingStrategyCode("mifos-standard-strategy")//
                .loanScheduleType(LoanScheduleType.CUMULATIVE.toString())//
                .daysInYearType(1)//
                .daysInMonthType(1)//
                .canDefineInstallmentAmount(true)//
                .graceOnArrearsAgeing(3)//
                .overdueDaysForNPA(179)//
                .accountMovesOutOfNPAOnlyOnArrearsCompletion(false)//
                .principalThresholdForLastInstallment(50)//
                .allowVariableInstallments(false)//
                .canUseForTopup(false)//
                .isInterestRecalculationEnabled(false)//
                .holdGuaranteeFunds(false)//
                .multiDisburseLoan(true)//
                .allowAttributeOverrides(new AllowAttributeOverrides()//
                        .amortizationType(true)//
                        .interestType(true)//
                        .transactionProcessingStrategyCode(true)//
                        .interestCalculationPeriodType(true)//
                        .inArrearsTolerance(true)//
                        .repaymentEvery(true)//
                        .graceOnPrincipalAndInterestPayment(true)//
                        .graceOnArrearsAgeing(true))//
                .allowPartialPeriodInterestCalculation(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0)//
                .charges(charges)//
                .accountingRule(3)//
                .fundSourceAccountId(suspenseClearingAccount.getAccountID().longValue())//
                .loanPortfolioAccountId(loansReceivable.getAccountID().longValue())//
                .transfersInSuspenseAccountId(suspenseAccount.getAccountID().longValue())//
                .interestOnLoanAccountId(interestIncome.getAccountID().longValue())//
                .incomeFromFeeAccountId(feeIncome.getAccountID().longValue())//
                .incomeFromPenaltyAccountId(feeIncome.getAccountID().longValue())//
                .incomeFromRecoveryAccountId(recoveries.getAccountID().longValue())//
                .writeOffAccountId(writtenOff.getAccountID().longValue())//
                .overpaymentLiabilityAccountId(overpaymentAccount.getAccountID().longValue())//
                .receivableInterestAccountId(interestFeeReceivable.getAccountID().longValue())//
                .receivableFeeAccountId(interestFeeReceivable.getAccountID().longValue())//
                .receivablePenaltyAccountId(interestFeeReceivable.getAccountID().longValue())//
                .dateFormat("dd MMMM yyyy")//
                .locale("en_GB")//
                .disallowExpectedDisbursements(true)//
                .allowApprovedDisbursedAmountsOverApplied(true)//
                .overAppliedCalculationType("percentage")//
                .overAppliedNumber(50)//
                .delinquencyBucketId(delinquencyBucketId.longValue())//
                .goodwillCreditAccountId(goodwillExpenseAccount.getAccountID().longValue())//
                .incomeFromGoodwillCreditInterestAccountId(interestIncomeChargeOff.getAccountID().longValue())//
                .incomeFromGoodwillCreditFeesAccountId(feeChargeOff.getAccountID().longValue())//
                .incomeFromGoodwillCreditPenaltyAccountId(feeChargeOff.getAccountID().longValue())//
                .paymentChannelToFundSourceMappings(paymentChannelToFundSourceMappings)//
                .penaltyToIncomeAccountMappings(penaltyToIncomeAccountMappings)//
                .feeToIncomeAccountMappings(feeToIncomeAccountMappings)//
                .incomeFromChargeOffInterestAccountId(interestIncomeChargeOff.getAccountID().longValue())//
                .incomeFromChargeOffFeesAccountId(feeChargeOff.getAccountID().longValue())//
                .chargeOffExpenseAccountId(creditLossBadDebt.getAccountID().longValue())//
                .chargeOffFraudExpenseAccountId(creditLossBadDebtFraud.getAccountID().longValue())//
                .incomeFromChargeOffPenaltyAccountId(feeChargeOff.getAccountID().longValue());//

        return createLoanProduct(loanProductsRequest);
    }

}
