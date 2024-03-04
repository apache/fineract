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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.fineract.client.models.AllowAttributeOverrides;
import org.apache.fineract.client.models.ChargeData;
import org.apache.fineract.client.models.ChargeToGLAccountMapper;
import org.apache.fineract.client.models.GetJournalEntriesTransactionIdResponse;
import org.apache.fineract.client.models.GetLoanFeeToIncomeAccountMappings;
import org.apache.fineract.client.models.GetLoanPaymentChannelToFundSourceMappings;
import org.apache.fineract.client.models.GetLoanTransactionRelation;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostPaymentTypesRequest;
import org.apache.fineract.client.models.PostPaymentTypesResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.PaymentTypeHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.JournalEntryHelper;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.funds.FundsHelper;
import org.apache.fineract.integrationtests.common.funds.FundsResourceHandler;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductHelper;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.apache.fineract.integrationtests.common.system.CodeHelper;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(LoanTestLifecycleExtension.class)
public class LoanPostChargeOffScenariosTest {

    private static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder().appendPattern("dd MMMM yyyy").toFormatter();
    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private ClientHelper clientHelper;
    private LoanTransactionHelper loanTransactionHelper;
    private JournalEntryHelper journalEntryHelper;
    private AccountHelper accountHelper;
    private LoanProductHelper loanProductHelper;
    private PaymentTypeHelper paymentTypeHelper;
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
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.loanProductHelper = new LoanProductHelper();
        this.paymentTypeHelper = new PaymentTypeHelper();

        // Asset
        this.loansReceivable = this.accountHelper.createAssetAccount();
        this.interestFeeReceivable = this.accountHelper.createAssetAccount();
        this.suspenseAccount = this.accountHelper.createAssetAccount();
        this.fundReceivables = this.accountHelper.createAssetAccount();

        // Liability
        this.suspenseClearingAccount = this.accountHelper.createLiabilityAccount();
        this.overpaymentAccount = this.accountHelper.createLiabilityAccount();

        // income
        this.interestIncome = this.accountHelper.createIncomeAccount();
        this.feeIncome = this.accountHelper.createIncomeAccount();
        this.feeChargeOff = this.accountHelper.createIncomeAccount();
        this.recoveries = this.accountHelper.createIncomeAccount();
        this.interestIncomeChargeOff = this.accountHelper.createIncomeAccount();

        // expense
        this.creditLossBadDebt = this.accountHelper.createExpenseAccount();
        this.creditLossBadDebtFraud = this.accountHelper.createExpenseAccount();
        this.writtenOff = this.accountHelper.createExpenseAccount();
        this.goodwillExpenseAccount = this.accountHelper.createExpenseAccount();

        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);
        this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void postChargeOffAddBackdatedTransactionTest() {
        String loanExternalIdStr = UUID.randomUUID().toString();
        final Integer loanProductID = createLoanProductWithPeriodicAccrualAccounting();
        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
        final Integer loanId = createLoanAccount(clientId, loanProductID, loanExternalIdStr);

        // apply charges
        Integer feeCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", false));

        LocalDate targetDate = LocalDate.of(2022, 9, 5);
        final String feeCharge1AddedDate = DATE_FORMATTER.format(targetDate);
        Integer feeLoanChargeId = loanTransactionHelper.addChargesForLoan(loanId,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(feeCharge), feeCharge1AddedDate, "10"));

        // set loan as chargeoff
        String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6) + Utils.randomStringGenerator("is", 5);
        Integer chargeOffReasonId = CodeHelper.createChargeOffCodeValue(requestSpec, responseSpec, randomText, 1);
        String transactionExternalId = UUID.randomUUID().toString();
        PostLoansLoanIdTransactionsResponse chargeOffTransaction = loanTransactionHelper.chargeOffLoan((long) loanId,
                new PostLoansLoanIdTransactionsRequest().transactionDate("14 September 2022").locale("en").dateFormat("dd MMMM yyyy")
                        .externalId(transactionExternalId).chargeOffReasonId((long) chargeOffReasonId));

        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries For ChargeOff Transaction
        GetJournalEntriesTransactionIdResponse journalEntriesForChargeOff = journalEntryHelper
                .getJournalEntries("L" + chargeOffTransaction.getResourceId().toString());

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
        final PostLoansLoanIdTransactionsResponse repaymentTransaction = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("7 September 2022").locale("en")
                        .transactionAmount(100.0));

        loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries for Repayment transaction

        GetJournalEntriesTransactionIdResponse journalEntriesForRepayment = journalEntryHelper
                .getJournalEntries("L" + repaymentTransaction.getResourceId().toString());
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
        final PostLoansLoanIdTransactionsResponse goodwillCredit = loanTransactionHelper.makeGoodwillCredit((long) loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("10 September 2022").locale("en")
                        .transactionAmount(100.0));

        loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries for Goodwill Credit
        GetJournalEntriesTransactionIdResponse journalEntriesForGoodWillCredit = journalEntryHelper
                .getJournalEntries("L" + goodwillCredit.getResourceId().toString());
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

        // make Repayment after chargeoff date
        final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("15 September 2022").locale("en")
                        .transactionAmount(100.0));

        loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries for Repayment transaction
        journalEntriesForRepayment = journalEntryHelper.getJournalEntries("L" + repaymentTransaction_1.getResourceId().toString());

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
        final PostLoansLoanIdTransactionsResponse goodwillCredit_1 = loanTransactionHelper.makeGoodwillCredit((long) loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("16 September 2022").locale("en")
                        .transactionAmount(100.0));

        loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries for Goodwill Credit
        journalEntriesForGoodWillCredit = journalEntryHelper.getJournalEntries("L" + goodwillCredit_1.getResourceId().toString());
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
    }

    @Test
    public void postChargeOffBackdatedTransactionReverseTest() {
        String loanExternalIdStr = UUID.randomUUID().toString();
        final Integer loanProductID = createLoanProductWithPeriodicAccrualAccounting();
        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
        final Integer loanId = createLoanAccount(clientId, loanProductID, loanExternalIdStr);

        // apply charges
        Integer feeCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", false));

        LocalDate targetDate = LocalDate.of(2022, 9, 5);
        final String feeCharge1AddedDate = DATE_FORMATTER.format(targetDate);
        Integer feeLoanChargeId = loanTransactionHelper.addChargesForLoan(loanId,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(feeCharge), feeCharge1AddedDate, "10"));

        // make Repayment
        final PostLoansLoanIdTransactionsResponse repaymentTransaction = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("7 September 2022").locale("en")
                        .transactionAmount(100.0));

        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());

        // verify Journal Entries for Repayment transaction
        GetJournalEntriesTransactionIdResponse journalEntriesForRepayment = journalEntryHelper
                .getJournalEntries("L" + repaymentTransaction.getResourceId().toString());

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
        Integer chargeOffReasonId = CodeHelper.createChargeOffCodeValue(requestSpec, responseSpec, randomText, 1);
        String transactionExternalId = UUID.randomUUID().toString();
        PostLoansLoanIdTransactionsResponse chargeOffTransaction = loanTransactionHelper.chargeOffLoan((long) loanId,
                new PostLoansLoanIdTransactionsRequest().transactionDate("14 September 2022").locale("en").dateFormat("dd MMMM yyyy")
                        .externalId(transactionExternalId).chargeOffReasonId((long) chargeOffReasonId));

        loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries For ChargeOff Transaction
        GetJournalEntriesTransactionIdResponse journalEntriesForChargeOff = journalEntryHelper
                .getJournalEntries("L" + chargeOffTransaction.getResourceId().toString());

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
        loanTransactionHelper.reverseRepayment(loanId, repaymentTransaction.getResourceId().intValue(), "7 September 2022");
        loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries for Reversed Repayment transaction
        journalEntriesForRepayment = journalEntryHelper.getJournalEntries("L" + repaymentTransaction.getResourceId().toString());

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
        final Integer loanProductID = createLoanProductWithPeriodicAccrualAccounting();
        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
        final Integer loanId = createLoanAccount(clientId, loanProductID, loanExternalIdStr);

        // apply charges
        Integer feeCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", false));

        LocalDate targetDate = LocalDate.of(2022, 9, 5);
        final String feeCharge1AddedDate = DATE_FORMATTER.format(targetDate);
        Integer feeLoanChargeId = loanTransactionHelper.addChargesForLoan(loanId,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(feeCharge), feeCharge1AddedDate, "10"));

        // Set Loan transaction externalId for transaction getting reversed and replayed
        String loanTransactionExternalIdStr = UUID.randomUUID().toString();

        // make Repayment
        final PostLoansLoanIdTransactionsResponse repaymentTransaction = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("7 September 2022").locale("en")
                        .transactionAmount(11.0).externalId(loanTransactionExternalIdStr));

        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());

        // verify Journal Entries for Repayment transaction
        GetJournalEntriesTransactionIdResponse journalEntriesForRepayment = journalEntryHelper
                .getJournalEntries("L" + repaymentTransaction.getResourceId().toString());

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
        Integer chargeOffReasonId = CodeHelper.createChargeOffCodeValue(requestSpec, responseSpec, randomText, 1);
        String transactionExternalId = UUID.randomUUID().toString();
        PostLoansLoanIdTransactionsResponse chargeOffTransaction = loanTransactionHelper.chargeOffLoan((long) loanId,
                new PostLoansLoanIdTransactionsRequest().transactionDate("14 September 2022").locale("en").dateFormat("dd MMMM yyyy")
                        .externalId(transactionExternalId).chargeOffReasonId((long) chargeOffReasonId));

        loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries For ChargeOff Transaction
        GetJournalEntriesTransactionIdResponse journalEntriesForChargeOff = journalEntryHelper
                .getJournalEntries("L" + chargeOffTransaction.getResourceId().toString());

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
        final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("5 September 2022").locale("en")
                        .transactionAmount(5.0));

        loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries for Repayment transaction
        journalEntriesForRepayment = journalEntryHelper.getJournalEntries("L" + repaymentTransaction_1.getResourceId().toString());

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
        GetLoansLoanIdTransactionsTransactionIdResponse getLoansTransactionResponse = loanTransactionHelper
                .getLoanTransactionDetails((long) loanId, loanTransactionExternalIdStr);
        assertNotNull(getLoansTransactionResponse);
        assertNotNull(getLoansTransactionResponse.getTransactionRelations());

        // test replayed relationship
        GetLoanTransactionRelation transactionRelation = getLoansTransactionResponse.getTransactionRelations().iterator().next();
        assertEquals(reversedAndReplayedTransactionId, transactionRelation.getToLoanTransaction());
        assertEquals("REPLAYED", transactionRelation.getRelationType());

        // verify Journal Entries for new Transaction
        journalEntriesForRepayment = journalEntryHelper.getJournalEntries("L" + getLoansTransactionResponse.getId().toString());

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
        final Integer loanProductID = createLoanProductWithPeriodicAccrualAccounting();
        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
        final Integer loanId = createLoanAccount(clientId, loanProductID, loanExternalIdStr);

        // apply charges
        Integer feeCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", false));

        LocalDate targetDate = LocalDate.of(2022, 9, 5);
        final String feeCharge1AddedDate = DATE_FORMATTER.format(targetDate);
        Integer feeLoanChargeId = loanTransactionHelper.addChargesForLoan(loanId,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(feeCharge), feeCharge1AddedDate, "10"));

        // Set Loan transaction externalId for transaction getting reversed and replayed
        String loanTransactionExternalIdStr = UUID.randomUUID().toString();

        // make Repayment on Chargeoff date before charge off
        final PostLoansLoanIdTransactionsResponse repaymentTransaction = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("7 September 2022").locale("en")
                        .transactionAmount(11.0).externalId(loanTransactionExternalIdStr));

        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());

        // verify Journal Entries for Repayment transaction
        GetJournalEntriesTransactionIdResponse journalEntriesForRepayment = journalEntryHelper
                .getJournalEntries("L" + repaymentTransaction.getResourceId().toString());

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
        Integer chargeOffReasonId = CodeHelper.createChargeOffCodeValue(requestSpec, responseSpec, randomText, 1);
        String transactionExternalId = UUID.randomUUID().toString();
        PostLoansLoanIdTransactionsResponse chargeOffTransaction = loanTransactionHelper.chargeOffLoan((long) loanId,
                new PostLoansLoanIdTransactionsRequest().transactionDate("7 September 2022").locale("en").dateFormat("dd MMMM yyyy")
                        .externalId(transactionExternalId).chargeOffReasonId((long) chargeOffReasonId));

        loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries For ChargeOff Transaction
        GetJournalEntriesTransactionIdResponse journalEntriesForChargeOff = journalEntryHelper
                .getJournalEntries("L" + chargeOffTransaction.getResourceId().toString());

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
        final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("5 September 2022").locale("en")
                        .transactionAmount(5.0));

        loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries for Repayment transaction
        journalEntriesForRepayment = journalEntryHelper.getJournalEntries("L" + repaymentTransaction_1.getResourceId().toString());

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
        GetLoansLoanIdTransactionsTransactionIdResponse getLoansTransactionResponse = loanTransactionHelper
                .getLoanTransactionDetails((long) loanId, loanTransactionExternalIdStr);
        assertNotNull(getLoansTransactionResponse);
        assertNotNull(getLoansTransactionResponse.getTransactionRelations());

        // test replayed relationship
        GetLoanTransactionRelation transactionRelation = getLoansTransactionResponse.getTransactionRelations().iterator().next();
        assertEquals(reversedAndReplayedTransactionId, transactionRelation.getToLoanTransaction());
        assertEquals("REPLAYED", transactionRelation.getRelationType());

        // verify Journal Entries for new Transaction

        journalEntriesForRepayment = journalEntryHelper.getJournalEntries("L" + getLoansTransactionResponse.getId().toString());

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
        final Integer loanProductID = createLoanProductWithPeriodicAccrualAccounting();
        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
        final Integer loanId = createLoanAccount(clientId, loanProductID, loanExternalIdStr);

        // apply charges
        Integer feeCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", false));

        LocalDate targetDate = LocalDate.of(2022, 9, 5);
        final String feeCharge1AddedDate = DATE_FORMATTER.format(targetDate);
        Integer feeLoanChargeId = loanTransactionHelper.addChargesForLoan(loanId,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(feeCharge), feeCharge1AddedDate, "10"));

        // set loan as chargeoff
        String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6) + Utils.randomStringGenerator("is", 5);
        Integer chargeOffReasonId = CodeHelper.createChargeOffCodeValue(requestSpec, responseSpec, randomText, 1);
        String transactionExternalId = UUID.randomUUID().toString();
        PostLoansLoanIdTransactionsResponse chargeOffTransaction = loanTransactionHelper.chargeOffLoan((long) loanId,
                new PostLoansLoanIdTransactionsRequest().transactionDate("7 September 2022").locale("en").dateFormat("dd MMMM yyyy")
                        .externalId(transactionExternalId).chargeOffReasonId((long) chargeOffReasonId));

        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries For ChargeOff Transaction
        GetJournalEntriesTransactionIdResponse journalEntriesForChargeOff = journalEntryHelper
                .getJournalEntries("L" + chargeOffTransaction.getResourceId().toString());

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
        final PostLoansLoanIdTransactionsResponse repaymentTransaction = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("7 September 2022").locale("en")
                        .transactionAmount(11.0).externalId(loanTransactionExternalIdStr));

        loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries for Repayment transaction
        GetJournalEntriesTransactionIdResponse journalEntriesForRepayment = journalEntryHelper
                .getJournalEntries("L" + repaymentTransaction.getResourceId().toString());

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
        final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("5 September 2022").locale("en")
                        .transactionAmount(5.0));

        loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries for Repayment transaction
        journalEntriesForRepayment = journalEntryHelper.getJournalEntries("L" + repaymentTransaction_1.getResourceId().toString());
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
        GetLoansLoanIdTransactionsTransactionIdResponse getLoansTransactionResponse = loanTransactionHelper
                .getLoanTransactionDetails((long) loanId, loanTransactionExternalIdStr);
        assertNotNull(getLoansTransactionResponse);
        assertNotNull(getLoansTransactionResponse.getTransactionRelations());

        // test replayed relationship
        GetLoanTransactionRelation transactionRelation = getLoansTransactionResponse.getTransactionRelations().iterator().next();
        assertEquals(reversedAndReplayedTransactionId, transactionRelation.getToLoanTransaction());
        assertEquals("REPLAYED", transactionRelation.getRelationType());

        // verify Journal Entries for new Transaction
        journalEntriesForRepayment = journalEntryHelper.getJournalEntries("L" + getLoansTransactionResponse.getId().toString());

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
        final Integer loanProductID = createLoanProductWithPeriodicAccrualAccounting();
        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
        final Integer loanId = createLoanAccount(clientId, loanProductID, loanExternalIdStr);

        // apply charges
        Integer feeCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", false));

        LocalDate targetDate = LocalDate.of(2022, 9, 5);
        final String feeCharge1AddedDate = DATE_FORMATTER.format(targetDate);
        Integer feeLoanChargeId = loanTransactionHelper.addChargesForLoan(loanId,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(feeCharge), feeCharge1AddedDate, "10"));

        // make Repayment before charge-off on charge off date
        final PostLoansLoanIdTransactionsResponse repaymentTransaction = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("7 September 2022").locale("en")
                        .transactionAmount(100.0));

        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());

        // verify Journal Entries for Repayment transaction
        GetJournalEntriesTransactionIdResponse journalEntriesForRepayment = journalEntryHelper
                .getJournalEntries("L" + repaymentTransaction.getResourceId().toString());

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
        Integer chargeOffReasonId = CodeHelper.createChargeOffCodeValue(requestSpec, responseSpec, randomText, 1);
        String transactionExternalId = UUID.randomUUID().toString();
        PostLoansLoanIdTransactionsResponse chargeOffTransaction = loanTransactionHelper.chargeOffLoan((long) loanId,
                new PostLoansLoanIdTransactionsRequest().transactionDate("7 September 2022").locale("en").dateFormat("dd MMMM yyyy")
                        .externalId(transactionExternalId).chargeOffReasonId((long) chargeOffReasonId));

        loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries For ChargeOff Transaction
        GetJournalEntriesTransactionIdResponse journalEntriesForChargeOff = journalEntryHelper
                .getJournalEntries("L" + chargeOffTransaction.getResourceId().toString());

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
        final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("7 September 2022").locale("en")
                        .transactionAmount(90.0));

        loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries for Repayment transaction
        journalEntriesForRepayment = journalEntryHelper.getJournalEntries("L" + repaymentTransaction_1.getResourceId().toString());

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
        final Integer loanProductID = createLoanProductWithPeriodicAccrualAccounting();
        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
        final Integer loanId = createLoanAccount(clientId, loanProductID, loanExternalIdStr);

        // apply charges
        Integer feeCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", false));

        LocalDate targetDate = LocalDate.of(2022, 9, 5);
        final String feeCharge1AddedDate = DATE_FORMATTER.format(targetDate);
        Integer feeLoanChargeId = loanTransactionHelper.addChargesForLoan(loanId,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(feeCharge), feeCharge1AddedDate, "10"));

        // make Repayment before charge-off on charge off date
        final PostLoansLoanIdTransactionsResponse repaymentTransaction = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("7 September 2022").locale("en")
                        .transactionAmount(100.0));

        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());

        // verify Journal Entries for Repayment transaction
        GetJournalEntriesTransactionIdResponse journalEntriesForRepayment = journalEntryHelper
                .getJournalEntries("L" + repaymentTransaction.getResourceId().toString());

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
        Integer chargeOffReasonId = CodeHelper.createChargeOffCodeValue(requestSpec, responseSpec, randomText, 1);
        String transactionExternalId = UUID.randomUUID().toString();
        PostLoansLoanIdTransactionsResponse chargeOffTransaction = loanTransactionHelper.chargeOffLoan((long) loanId,
                new PostLoansLoanIdTransactionsRequest().transactionDate("7 September 2022").locale("en").dateFormat("dd MMMM yyyy")
                        .externalId(transactionExternalId).chargeOffReasonId((long) chargeOffReasonId));

        loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries For ChargeOff Transaction
        GetJournalEntriesTransactionIdResponse journalEntriesForChargeOff = journalEntryHelper
                .getJournalEntries("L" + chargeOffTransaction.getResourceId().toString());

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
        loanTransactionHelper.reverseRepayment(loanId, repaymentTransaction.getResourceId().intValue(), "7 September 2022");
        loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries for Reversed Repayment transaction
        journalEntriesForRepayment = journalEntryHelper.getJournalEntries("L" + repaymentTransaction.getResourceId().toString());
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

    private Integer createLoanAccount(final Integer clientID, final Integer loanProductID, final String externalId) {

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("30")
                .withLoanTermFrequencyAsDays().withNumberOfRepayments("1").withRepaymentEveryAfter("30").withRepaymentFrequencyTypeAsDays()
                .withInterestRatePerPeriod("0").withInterestTypeAsFlatBalance().withAmortizationTypeAsEqualPrincipalPayments()
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod().withExpectedDisbursementDate("03 September 2022")
                .withSubmittedOnDate("01 September 2022").withLoanType("individual").withExternalId(externalId)
                .build(clientID.toString(), loanProductID.toString(), null);

        final Integer loanId = loanTransactionHelper.getLoanId(loanApplicationJSON);
        loanTransactionHelper.approveLoan("02 September 2022", "1000", loanId, null);
        loanTransactionHelper.disburseLoanWithTransactionAmount("03 September 2022", loanId, "1000");
        return loanId;
    }

    private Integer createLoanProductWithPeriodicAccrualAccounting() {

        String name = Utils.uniqueRandomStringGenerator("LOAN_PRODUCT_", 6);
        String shortName = Utils.uniqueRandomStringGenerator("", 4);

        List<Integer> principalVariationsForBorrowerCycle = new ArrayList<>();
        List<Integer> numberOfRepaymentVariationsForBorrowerCycle = new ArrayList<>();
        List<Integer> interestRateVariationsForBorrowerCycle = new ArrayList<>();
        List<ChargeData> charges = new ArrayList<>();
        List<ChargeToGLAccountMapper> penaltyToIncomeAccountMappings = new ArrayList<>();
        List<GetLoanFeeToIncomeAccountMappings> feeToIncomeAccountMappings = new ArrayList<>();

        String paymentTypeName = PaymentTypeHelper.randomNameGenerator("P_T", 5);
        String description = PaymentTypeHelper.randomNameGenerator("PT_Desc", 15);
        Boolean isCashPayment = false;
        Integer position = 1;

        PostPaymentTypesResponse paymentTypesResponse = paymentTypeHelper.createPaymentType(new PostPaymentTypesRequest()
                .name(paymentTypeName).description(description).isCashPayment(isCashPayment).position(position));
        Long paymentTypeIdOne = paymentTypesResponse.getResourceId();
        Assertions.assertNotNull(paymentTypeIdOne);

        List<GetLoanPaymentChannelToFundSourceMappings> paymentChannelToFundSourceMappings = new ArrayList<>();
        GetLoanPaymentChannelToFundSourceMappings loanPaymentChannelToFundSourceMappings = new GetLoanPaymentChannelToFundSourceMappings();
        loanPaymentChannelToFundSourceMappings.fundSourceAccountId(fundReceivables.getAccountID().longValue());
        loanPaymentChannelToFundSourceMappings.paymentTypeId(paymentTypeIdOne.longValue());
        paymentChannelToFundSourceMappings.add(loanPaymentChannelToFundSourceMappings);

        // fund
        FundsHelper fh = FundsHelper.create(Utils.uniqueRandomStringGenerator("", 10)).externalId(UUID.randomUUID().toString()).build();
        String jsonData = fh.toJSON();

        final Long fundID = createFund(jsonData, this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(fundID);

        // Delinquency Bucket
        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);

        PostLoanProductsRequest loanProductsRequest = new PostLoanProductsRequest().name(name)//
                .shortName(shortName)//
                .description("Loan Product Description")//
                .fundId(fundID)//
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
                .allowPartialPeriodInterestCalcualtion(true)//
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

        PostLoanProductsResponse loanProductCreateResponse = loanProductHelper.createLoanProduct(loanProductsRequest);
        return loanProductCreateResponse.getResourceId().intValue();
    }

    private Long createFund(final String fundJSON, final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        String fundId = String.valueOf(FundsResourceHandler.createFund(fundJSON, requestSpec, responseSpec));
        if (fundId.equals("null")) {
            // Invalid JSON data parameters
            return null;
        }

        return Long.valueOf(fundId);
    }

}
