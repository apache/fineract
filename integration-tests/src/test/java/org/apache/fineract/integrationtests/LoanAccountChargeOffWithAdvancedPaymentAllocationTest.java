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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.AllowAttributeOverrides;
import org.apache.fineract.client.models.GetJournalEntriesTransactionIdResponse;
import org.apache.fineract.client.models.GetLoanPaymentChannelToFundSourceMappings;
import org.apache.fineract.client.models.GetLoanTransactionRelation;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
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
import org.apache.fineract.integrationtests.common.accounting.JournalEntry;
import org.apache.fineract.integrationtests.common.funds.FundsResourceHandler;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LoanAccountChargeOffWithAdvancedPaymentAllocationTest extends FeignLoanTestBase {

    private static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder().appendPattern("dd MMMM yyyy").toFormatter();

    // Charge-off accounting and balances
    @Test
    public void loanChargeOffWithAdvancedPaymentStrategyTest() {
        runAt("10 September 2022", () -> {
            String loanExternalIdStr = UUID.randomUUID().toString();
            final Long loanProductId = createLoanProductWithPeriodicAccrualAccountingAndAdvancedPaymentAllocationStrategy();
            final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            final Long loanId = createLoanAccount(clientId, loanProductId, loanExternalIdStr);

            // apply charges
            Long feeCharge = createLoanSpecifiedDueDateCharge(10.0);

            LocalDate targetDate = LocalDate.of(2022, 9, 5);
            final String feeCharge1AddedDate = DATE_FORMATTER.format(targetDate);
            addLoanCharge(loanId, feeCharge, feeCharge1AddedDate, 10.0);

            // apply penalty
            final String penaltyCharge1AddedDate = DATE_FORMATTER.format(targetDate);
            addCharge(loanId, true, 10.0, penaltyCharge1AddedDate);

            // make Repayment
            makeLoanRepayment(loanExternalIdStr, new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN)
                    .transactionDate("9 September 2022").locale(LoanTestData.LOCALE).transactionAmount(10.0));

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getActive());

            // set loan as chargeoff
            String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6)
                    + Utils.randomStringGenerator("is", 5);
            Long chargeOffReasonId = codeHelper.createChargeOffCodeValue(randomText, 1);
            String transactionExternalId = UUID.randomUUID().toString();
            PostLoansLoanIdTransactionsResponse chargeOffTransaction = chargeOffLoan(loanId,
                    new PostLoansLoanIdTransactionsRequest().transactionDate("10 September 2022").locale(LoanTestData.LOCALE)
                            .dateFormat(LoanTestData.DATETIME_PATTERN).externalId(transactionExternalId)
                            .chargeOffReasonId(chargeOffReasonId));

            loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getActive());
            assertTrue(loanDetails.getChargedOff());

            // verify amounts for charge-off transaction
            verifyTransaction(LocalDate.of(2022, 9, 10), 1010.0f, 1000.0f, 0.0f, 10.0f, 0.0f, loanId, "chargeoff");
            // verify journal entries
            GetJournalEntriesTransactionIdResponse journalEntriesForChargeOff = getJournalEntries(
                    "L" + chargeOffTransaction.getResourceId().toString());

            assertNotNull(journalEntriesForChargeOff);

            List<JournalEntryTransactionItem> journalEntries = journalEntriesForChargeOff.getPageItems();
            assertEquals(4, journalEntries.size());
            verifyJournalEntry(journalEntries.get(3), 1000.0, LocalDate.of(2022, 9, 10), getAccounts().getLoansReceivableAccount(),
                    JournalEntry.TransactionType.CREDIT.name());
            verifyJournalEntry(journalEntries.get(2), 10.0, LocalDate.of(2022, 9, 10), getAccounts().getFeeReceivableAccount(),
                    JournalEntry.TransactionType.CREDIT.name());
            verifyJournalEntry(journalEntries.get(1), 1000.0, LocalDate.of(2022, 9, 10), getAccounts().getChargeOffExpenseAccount(),
                    JournalEntry.TransactionType.DEBIT.name());
            verifyJournalEntry(journalEntries.get(0), 10.0, LocalDate.of(2022, 9, 10), getAccounts().getFeeChargeOffAccount(),
                    JournalEntry.TransactionType.DEBIT.name());
        });
    }

    // Reverse Replay of Charge-Off
    @Test
    public void loanChargeOffReverseReplayWithAdvancedPaymentStrategyTest() {
        runAt("9 September 2022", () -> {
            String loanExternalIdStr = UUID.randomUUID().toString();
            final Long loanProductId = createLoanProductWithPeriodicAccrualAccountingAndAdvancedPaymentAllocationStrategy();
            final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            final Long loanId = createLoanAccount(clientId, loanProductId, loanExternalIdStr);

            // apply charges
            Long feeCharge = createLoanSpecifiedDueDateCharge(10.0);

            LocalDate targetDate = LocalDate.of(2022, 9, 5);
            final String feeCharge1AddedDate = DATE_FORMATTER.format(targetDate);
            addLoanCharge(loanId, feeCharge, feeCharge1AddedDate, 10.0);

            // apply penalty
            final String penaltyCharge1AddedDate = DATE_FORMATTER.format(targetDate);
            addCharge(loanId, true, 10.0, penaltyCharge1AddedDate);

            // make Repayment
            final PostLoansLoanIdTransactionsResponse repaymentTransaction = makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate("9 September 2022")
                            .locale(LoanTestData.LOCALE).transactionAmount(10.0));

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getActive());

            // set loan as chargeoff
            updateBusinessDate("10 September 2022");
            String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6)
                    + Utils.randomStringGenerator("is", 5);
            Long chargeOffReasonId = codeHelper.createChargeOffCodeValue(randomText, 1);
            String transactionExternalId = UUID.randomUUID().toString();
            PostLoansLoanIdTransactionsResponse chargeOffTransaction = chargeOffLoan(loanId,
                    new PostLoansLoanIdTransactionsRequest().transactionDate("10 September 2022").locale(LoanTestData.LOCALE)
                            .dateFormat(LoanTestData.DATETIME_PATTERN).externalId(transactionExternalId)
                            .chargeOffReasonId(chargeOffReasonId));

            loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getActive());
            assertTrue(loanDetails.getChargedOff());

            // verify amounts for charge-off transaction
            verifyTransaction(LocalDate.of(2022, 9, 10), 1010.0f, 1000.0f, 0.0f, 10.0f, 0.0f, loanId, "chargeoff");

            Long reversedAndReplayedTransactionId = chargeOffTransaction.getResourceId();

            // reverse Repayment
            updateBusinessDate("11 September 2022");
            reverseRepayment(loanId, repaymentTransaction.getResourceId(), "11 September 2022");

            // verify chargeOffTransaction gets reverse replayed

            GetLoansLoanIdTransactionsTransactionIdResponse getLoansTransactionResponse = getLoanTransactionDetails(loanId,
                    transactionExternalId);
            assertNotNull(getLoansTransactionResponse);
            assertNotNull(getLoansTransactionResponse.getTransactionRelations());

            // test replayed relationship
            GetLoanTransactionRelation transactionRelation = getLoansTransactionResponse.getTransactionRelations().iterator().next();
            assertEquals(reversedAndReplayedTransactionId, transactionRelation.getToLoanTransaction());
            assertEquals("REPLAYED", transactionRelation.getRelationType());

            // verify amounts for charge-off transaction
            verifyTransaction(LocalDate.of(2022, 9, 10), 1020.0f, 1000.0f, 0.0f, 10.0f, 10.0f, loanId, "chargeoff");
        });
    }

    // undo Charge-Off
    @Test
    public void loanUndoChargeOffTest() {
        // Loan ExternalId
        String loanExternalIdStr = UUID.randomUUID().toString();

        final Long loanProductId = createLoanProductWithPeriodicAccrualAccountingAndAdvancedPaymentAllocationStrategy();
        final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
        final Long loanId = createLoanAccount(clientId, loanProductId, loanExternalIdStr);

        // make Repayment
        makeLoanRepayment(loanExternalIdStr, new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN)
                .transactionDate("6 September 2022").locale(LoanTestData.LOCALE).transactionAmount(100.0));

        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        assertTrue(loanDetails.getStatus().getActive());

        // set loan as chargeoff
        String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6) + Utils.randomStringGenerator("is", 5);
        Long chargeOffReasonId = codeHelper.createChargeOffCodeValue(randomText, 1);
        String transactionExternalId = UUID.randomUUID().toString();
        chargeOffLoan(loanId, new PostLoansLoanIdTransactionsRequest().transactionDate("7 September 2022").locale(LoanTestData.LOCALE)
                .dateFormat(LoanTestData.DATETIME_PATTERN).externalId(transactionExternalId).chargeOffReasonId(chargeOffReasonId));

        loanDetails = getLoanDetails(loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // undo charge-off
        String reverseTransactionExternalId = UUID.randomUUID().toString();
        PostLoansLoanIdTransactionsResponse undoChargeOffTxResponse = transactionHelper.undoChargeOff(loanId,
                new PostLoansLoanIdTransactionsRequest().reversalExternalId(reverseTransactionExternalId));
        assertNotNull(undoChargeOffTxResponse);

        loanDetails = getLoanDetails(loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertFalse(loanDetails.getChargedOff());

        GetLoansLoanIdTransactionsTransactionIdResponse chargeOffTransactionDetails = getLoanTransactionDetails(loanId,
                transactionExternalId);
        assertNotNull(chargeOffTransactionDetails);
        assertTrue(chargeOffTransactionDetails.getManuallyReversed());
        assertEquals(reverseTransactionExternalId, chargeOffTransactionDetails.getReversalExternalId());
    }

    // Backdated repayment transaction, Reverse replay of charge off
    @Test
    public void postChargeOffAddBackdatedTransactionAndReverseReplayTest() {
        runAt("3 September 2022", () -> {
            String loanExternalIdStr = UUID.randomUUID().toString();
            final Long loanProductId = createLoanProductWithPeriodicAccrualAccountingAndAdvancedPaymentAllocationStrategy();
            final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            final Long loanId = createLoanAccount(clientId, loanProductId, loanExternalIdStr);

            // apply charges
            updateBusinessDate("5 September 2022");
            Long feeCharge = createLoanSpecifiedDueDateCharge(10.0);

            LocalDate targetDate = LocalDate.of(2022, 9, 5);
            final String feeCharge1AddedDate = DATE_FORMATTER.format(targetDate);
            addLoanCharge(loanId, feeCharge, feeCharge1AddedDate, 10.0);

            // set loan as chargeoff
            updateBusinessDate("14 September 2022");
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

            Long reversedAndReplayedTransactionId = chargeOffTransaction.getResourceId();

            // verify Journal Entries For ChargeOff Transaction
            GetJournalEntriesTransactionIdResponse journalEntriesForChargeOff = getJournalEntries(
                    "L" + chargeOffTransaction.getResourceId().toString());

            assertNotNull(journalEntriesForChargeOff);
            List<JournalEntryTransactionItem> journalEntries = journalEntriesForChargeOff.getPageItems();
            assertEquals(4, journalEntries.size());

            verifyJournalEntry(journalEntries.get(3), 1000.0, LocalDate.of(2022, 9, 14), getAccounts().getLoansReceivableAccount(),
                    JournalEntry.TransactionType.CREDIT.name());
            verifyJournalEntry(journalEntries.get(2), 10.0, LocalDate.of(2022, 9, 14), getAccounts().getFeeReceivableAccount(),
                    JournalEntry.TransactionType.CREDIT.name());
            verifyJournalEntry(journalEntries.get(1), 1000.0, LocalDate.of(2022, 9, 14), getAccounts().getChargeOffExpenseAccount(),
                    JournalEntry.TransactionType.DEBIT.name());
            verifyJournalEntry(journalEntries.get(0), 10.0, LocalDate.of(2022, 9, 14), getAccounts().getFeeChargeOffAccount(),
                    JournalEntry.TransactionType.DEBIT.name());

            // make Repayment before chargeoff date - business date is still on 14 September 2022
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

            verifyJournalEntry(journalEntries.get(2), 90.0, LocalDate.of(2022, 9, 7), getAccounts().getLoansReceivableAccount(),
                    JournalEntry.TransactionType.CREDIT.name());
            verifyJournalEntry(journalEntries.get(1), 10.0, LocalDate.of(2022, 9, 7), getAccounts().getFeeReceivableAccount(),
                    JournalEntry.TransactionType.CREDIT.name());
            verifyJournalEntry(journalEntries.get(0), 100.0, LocalDate.of(2022, 9, 7), getAccounts().getFundSource(),
                    JournalEntry.TransactionType.DEBIT.name());

            // verify reverse replay of Charge-Off

            GetLoansLoanIdTransactionsTransactionIdResponse getLoansTransactionResponse = getLoanTransactionDetails(loanId,
                    transactionExternalId);
            assertNotNull(getLoansTransactionResponse);
            assertNotNull(getLoansTransactionResponse.getTransactionRelations());

            // test replayed relationship
            GetLoanTransactionRelation transactionRelation = getLoansTransactionResponse.getTransactionRelations().iterator().next();
            assertEquals(reversedAndReplayedTransactionId, transactionRelation.getToLoanTransaction());
            assertEquals("REPLAYED", transactionRelation.getRelationType());

            // verify amounts for charge-off transaction
            verifyTransaction(LocalDate.of(2022, 9, 14), 910.0f, 910.0f, 0.0f, 0.0f, 0.0f, loanId, "chargeoff");

            // make Repayment after chargeoff date
            updateBusinessDate("15 September 2022");
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

            verifyJournalEntry(journalEntries.get(1), 100.0, LocalDate.of(2022, 9, 15), getAccounts().getRecoveriesAccount(),
                    JournalEntry.TransactionType.CREDIT.name());
            verifyJournalEntry(journalEntries.get(0), 100.0, LocalDate.of(2022, 9, 15), getAccounts().getFundSource(),
                    JournalEntry.TransactionType.DEBIT.name());
        });
    }

    // Repayment before charge off on charge off date, reverse replay of charge off
    @Test
    public void transactionOnChargeOffDateReverseTest() {
        runAt("7 September 2022", () -> {
            String loanExternalIdStr = UUID.randomUUID().toString();
            final Long loanProductId = createLoanProductWithPeriodicAccrualAccountingAndAdvancedPaymentAllocationStrategy();
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

            verifyJournalEntry(journalEntries.get(2), 90.0, LocalDate.of(2022, 9, 7), getAccounts().getLoansReceivableAccount(),
                    JournalEntry.TransactionType.CREDIT.name());
            verifyJournalEntry(journalEntries.get(1), 10.0, LocalDate.of(2022, 9, 7), getAccounts().getFeeReceivableAccount(),
                    JournalEntry.TransactionType.CREDIT.name());
            verifyJournalEntry(journalEntries.get(0), 100.0, LocalDate.of(2022, 9, 7), getAccounts().getFundSource(),
                    JournalEntry.TransactionType.DEBIT.name());

            // set loan as chargeoff
            String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6)
                    + Utils.randomStringGenerator("is", 5);
            Long chargeOffReasonId = codeHelper.createChargeOffCodeValue(randomText, 1);
            String transactionExternalId = UUID.randomUUID().toString();
            PostLoansLoanIdTransactionsResponse chargeOffTransaction = chargeOffLoan(loanId,
                    new PostLoansLoanIdTransactionsRequest().transactionDate("7 September 2022").locale(LoanTestData.LOCALE)
                            .dateFormat(LoanTestData.DATETIME_PATTERN).externalId(transactionExternalId)
                            .chargeOffReasonId(chargeOffReasonId));

            loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getActive());
            assertTrue(loanDetails.getChargedOff());

            Long reversedAndReplayedTransactionId = chargeOffTransaction.getResourceId();

            // verify Journal Entries For ChargeOff Transaction
            GetJournalEntriesTransactionIdResponse journalEntriesForChargeOff = getJournalEntries(
                    "L" + chargeOffTransaction.getResourceId().toString());

            assertNotNull(journalEntriesForChargeOff);
            journalEntries = journalEntriesForChargeOff.getPageItems();
            assertEquals(2, journalEntries.size());

            verifyJournalEntry(journalEntries.get(1), 910.0, LocalDate.of(2022, 9, 7), getAccounts().getLoansReceivableAccount(),
                    JournalEntry.TransactionType.CREDIT.name());
            verifyJournalEntry(journalEntries.get(0), 910.0, LocalDate.of(2022, 9, 7), getAccounts().getChargeOffExpenseAccount(),
                    JournalEntry.TransactionType.DEBIT.name());

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

            verifyJournalEntry(journalEntries.get(5), 90.0, LocalDate.of(2022, 9, 7), getAccounts().getLoansReceivableAccount(),
                    JournalEntry.TransactionType.CREDIT.name());
            verifyJournalEntry(journalEntries.get(4), 10.0, LocalDate.of(2022, 9, 7), getAccounts().getFeeReceivableAccount(),
                    JournalEntry.TransactionType.CREDIT.name());
            verifyJournalEntry(journalEntries.get(3), 100.0, LocalDate.of(2022, 9, 7), getAccounts().getFundSource(),
                    JournalEntry.TransactionType.DEBIT.name());
            verifyJournalEntry(journalEntries.get(2), 90.0, LocalDate.of(2022, 9, 7), getAccounts().getLoansReceivableAccount(),
                    JournalEntry.TransactionType.DEBIT.name());
            verifyJournalEntry(journalEntries.get(1), 10.0, LocalDate.of(2022, 9, 7), getAccounts().getFeeReceivableAccount(),
                    JournalEntry.TransactionType.DEBIT.name());
            verifyJournalEntry(journalEntries.get(0), 100.0, LocalDate.of(2022, 9, 7), getAccounts().getFundSource(),
                    JournalEntry.TransactionType.CREDIT.name());

            // verify reverse replay of Charge-Off

            GetLoansLoanIdTransactionsTransactionIdResponse getLoansTransactionResponse = getLoanTransactionDetails(loanId,
                    transactionExternalId);
            assertNotNull(getLoansTransactionResponse);
            assertNotNull(getLoansTransactionResponse.getTransactionRelations());

            // test replayed relationship
            GetLoanTransactionRelation transactionRelation = getLoansTransactionResponse.getTransactionRelations().iterator().next();
            assertEquals(reversedAndReplayedTransactionId, transactionRelation.getToLoanTransaction());
            assertEquals("REPLAYED", transactionRelation.getRelationType());

            // verify amounts for charge-off transaction
            verifyTransaction(LocalDate.of(2022, 9, 7), 1010.0f, 1000.0f, 0.0f, 10.0f, 0.0f, loanId, "chargeoff");
        });

    }

    @Test
    public void testProgressiveChargeOffWithEarlyRepaymentZeroInterest() {
        final Long clientId = createClient();
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        final Long loanProductId = createLoanProduct(create4IProgressive().chargeOffBehaviour("ZERO_INTEREST"));

        runAt("01 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "01 January 2024", 1000.0, 7.0, 6, null);

            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(1000), "01 January 2024");

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "01 February 2024", 164.26, 0, 0, 5.83);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2024", 165.21, 0, 0, 4.88);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2024", 166.18, 0, 0, 3.91);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2024", 167.15, 0, 0, 2.94);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 5, "01 June 2024", 168.12, 0, 0, 1.97);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 6, "01 July 2024", 169.08, 0, 0, 0.99);

            verifyTransactions(loanId, transaction(1000.0d, "Disbursement", "01 January 2024"));
            executeInlineCOB(loanId);
        });
        runAt("10 February 2024", () -> {
            Long loanId = loanIdRef.get();

            makeLoanRepayment(loanId, new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN)
                    .transactionDate("15 January 2024").locale(LoanTestData.LOCALE).transactionAmount(170.09));

            chargeOffLoan(loanId, new PostLoansLoanIdTransactionsRequest().transactionDate("31 January 2024").locale(LoanTestData.LOCALE)
                    .dateFormat(LoanTestData.DATETIME_PATTERN));

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 2, 1), 164.95, 167.46, -2.51, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 5.14,
                    2.63, 2.51, 170.09, 0.0);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2024", 170.09, 0, 0, 0.0);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2024", 170.09, 0, 0, 0.0);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2024", 170.09, 0, 0, 0.0);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 5, "01 June 2024", 170.09, 0, 0, 0.0);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 6, "01 July 2024", 154.69, 0, 0, 0.0);

            verifyTransactions(loanId, //
                    transaction(1000.0d, "Disbursement", "01 January 2024", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(170.09d, "Repayment", "15 January 2024", 832.54, 167.46, 2.63, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(5.14d, "Accrual", "31 January 2024", 0.0, 0.0, 5.14, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(835.05d, "Charge-off", "31 January 2024", 0.0, 832.54, 2.51, 0.0, 0.0, 0.0, 0.0, false) //
            );
        });
    }

    private void verifyJournalEntry(JournalEntryTransactionItem journalEntryTransactionItem, Double amount, LocalDate entryDate,
            Account account, String type) {
        assertEquals(amount, journalEntryTransactionItem.getAmount());
        assertEquals(entryDate, journalEntryTransactionItem.getTransactionDate());
        assertEquals(account.getAccountID().longValue(), journalEntryTransactionItem.getGlAccountId().longValue());
        assertEquals(type, journalEntryTransactionItem.getEntryType().getValue());
    }

    private void verifyTransaction(final LocalDate transactionDate, final Float transactionAmount, final Float principalPortion,
            final Float interestPortion, final Float feePortion, final Float penaltyPortion, final Long loanId,
            final String transactionOfType) {
        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        boolean isTransactionFound = false;
        for (GetLoansLoanIdTransactions transaction : loanDetails.getTransactions()) {
            boolean isTransaction = switch (transactionOfType) {
                case "chargeoff" -> Boolean.TRUE.equals(transaction.getType().getChargeoff());
                default -> false;
            };

            if (isTransaction && transactionDate.equals(transaction.getDate())) {
                isTransactionFound = true;
                assertEquals(transactionAmount, Utils.getDoubleValue(transaction.getAmount()).floatValue(),
                        "Mismatch in transaction amounts");
                assertEquals(principalPortion, Utils.getDoubleValue(transaction.getPrincipalPortion()).floatValue(),
                        "Mismatch in transaction amounts");
                assertEquals(interestPortion, Utils.getDoubleValue(transaction.getInterestPortion()).floatValue(),
                        "Mismatch in transaction amounts");
                assertEquals(feePortion, Utils.getDoubleValue(transaction.getFeeChargesPortion()).floatValue(),
                        "Mismatch in transaction amounts");
                assertEquals(penaltyPortion, Utils.getDoubleValue(transaction.getPenaltyChargesPortion()).floatValue(),
                        "Mismatch in transaction amounts");
                break;
            }
        }
        assertTrue(isTransactionFound, "No Transaction entries are posted");
    }

    private Long createLoanAccount(final Long clientId, final Long loanProductId, final String externalId) {

        PostLoansRequest loanApplication = LoanRequestBuilders
                .legacyDaysBasedApplication(clientId, loanProductId, "1000", 30, 1, 30, "03 September 2022", "01 September 2022")
                .externalId(externalId)//
                .transactionProcessingStrategyCode("advanced-payment-allocation-strategy");

        Long loanId = applyForLoan(loanApplication);
        approveLoan(loanId, approveLoanRequest(1000.0, "02 September 2022"));
        disburseLoanWithAmount(loanId, "03 September 2022", 1000.0);
        return loanId;
    }

    private Long createLoanProductWithPeriodicAccrualAccountingAndAdvancedPaymentAllocationStrategy() {

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
        loanPaymentChannelToFundSourceMappings.fundSourceAccountId(getAccounts().getFundSource().getAccountID().longValue());
        loanPaymentChannelToFundSourceMappings.paymentTypeId(paymentTypeIdOne.longValue());
        paymentChannelToFundSourceMappings.add(loanPaymentChannelToFundSourceMappings);

        final Long fundId = FundsResourceHandler.createFund().getResourceId();
        Assertions.assertNotNull(fundId);

        // Delinquency Bucket
        final Long delinquencyBucketId = DelinquencyBucketsHelper.createDefaultBucket();

        String futureInstallmentAllocationRule = "NEXT_INSTALLMENT";
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation(futureInstallmentAllocationRule);

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
                .transactionProcessingStrategyCode("advanced-payment-allocation-strategy")//
                .loanScheduleType(LoanScheduleType.PROGRESSIVE.toString())//
                .loanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString())//
                .addPaymentAllocationItem(defaultAllocation)//
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
                .fundSourceAccountId(getAccounts().getFundSource().getAccountID().longValue())//
                .loanPortfolioAccountId(getAccounts().getLoansReceivableAccount().getAccountID().longValue())//
                .transfersInSuspenseAccountId(getAccounts().getSuspenseAccount().getAccountID().longValue())//
                .interestOnLoanAccountId(getAccounts().getInterestIncomeAccount().getAccountID().longValue())//
                .incomeFromFeeAccountId(feeIncomeAccount().getAccountID().longValue())//
                .incomeFromPenaltyAccountId(feeIncomeAccount().getAccountID().longValue())//
                .incomeFromRecoveryAccountId(getAccounts().getRecoveriesAccount().getAccountID().longValue())//
                .writeOffAccountId(getAccounts().getWrittenOffAccount().getAccountID().longValue())//
                .overpaymentLiabilityAccountId(getAccounts().getOverpaymentAccount().getAccountID().longValue())//
                .receivableInterestAccountId(getAccounts().getFeeReceivableAccount().getAccountID().longValue())//
                .receivableFeeAccountId(getAccounts().getFeeReceivableAccount().getAccountID().longValue())//
                .receivablePenaltyAccountId(getAccounts().getFeeReceivableAccount().getAccountID().longValue())//
                .dateFormat("dd MMMM yyyy")//
                .locale("en_GB")//
                .disallowExpectedDisbursements(true)//
                .allowApprovedDisbursedAmountsOverApplied(true)//
                .overAppliedCalculationType("percentage")//
                .overAppliedNumber(50)//
                .delinquencyBucketId(delinquencyBucketId.longValue())//
                .goodwillCreditAccountId(getAccounts().getGoodwillExpenseAccount().getAccountID().longValue())//
                .incomeFromGoodwillCreditInterestAccountId(getAccounts().getInterestIncomeChargeOffAccount().getAccountID().longValue())//
                .incomeFromGoodwillCreditFeesAccountId(getAccounts().getFeeChargeOffAccount().getAccountID().longValue())//
                .incomeFromGoodwillCreditPenaltyAccountId(getAccounts().getFeeChargeOffAccount().getAccountID().longValue())//
                .paymentChannelToFundSourceMappings(paymentChannelToFundSourceMappings)//
                .penaltyToIncomeAccountMappings(penaltyToIncomeAccountMappings)//
                .feeToIncomeAccountMappings(feeToIncomeAccountMappings)//
                .incomeFromChargeOffInterestAccountId(getAccounts().getInterestIncomeChargeOffAccount().getAccountID().longValue())//
                .incomeFromChargeOffFeesAccountId(getAccounts().getFeeChargeOffAccount().getAccountID().longValue())//
                .chargeOffExpenseAccountId(getAccounts().getChargeOffExpenseAccount().getAccountID().longValue())//
                .chargeOffFraudExpenseAccountId(getAccounts().getChargeOffFraudExpenseAccount().getAccountID().longValue())//
                .incomeFromChargeOffPenaltyAccountId(getAccounts().getFeeChargeOffAccount().getAccountID().longValue());//

        return createLoanProduct(loanProductsRequest);
    }
}
