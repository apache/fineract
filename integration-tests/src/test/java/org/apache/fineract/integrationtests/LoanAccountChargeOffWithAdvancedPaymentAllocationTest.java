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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.apache.fineract.client.models.AdvancedPaymentData;
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
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
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
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(LoanTestLifecycleExtension.class)
public class LoanAccountChargeOffWithAdvancedPaymentAllocationTest extends BaseLoanIntegrationTest {

    private static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder().appendPattern("dd MMMM yyyy").toFormatter();
    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private ClientHelper clientHelper;
    private LoanTransactionHelper loanTransactionHelper;
    private JournalEntryHelper journalEntryHelper;
    private AccountHelper accountHelper;
    private LoanProductHelper loanProductHelper;
    private PaymentTypeHelper paymentTypeHelper;
    private final BusinessDateHelper businessDateHelper = new BusinessDateHelper();
    private static final String DATETIME_PATTERN = "dd MMMM yyyy";
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

    // Charge-off accounting and balances
    @Test
    public void loanChargeOffWithAdvancedPaymentStrategyTest() {
        String loanExternalIdStr = UUID.randomUUID().toString();
        final Integer loanProductID = createLoanProductWithPeriodicAccrualAccountingAndAdvancedPaymentAllocationStrategy();
        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
        final Integer loanId = createLoanAccount(clientId, loanProductID, loanExternalIdStr);

        // apply charges
        Integer feeCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", false));

        LocalDate targetDate = LocalDate.of(2022, 9, 5);
        final String feeCharge1AddedDate = DATE_FORMATTER.format(targetDate);
        Integer feeLoanChargeId = loanTransactionHelper.addChargesForLoan(loanId,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(feeCharge), feeCharge1AddedDate, "10"));

        // apply penalty
        Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", true));

        final String penaltyCharge1AddedDate = DATE_FORMATTER.format(targetDate);

        Integer penalty1LoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), penaltyCharge1AddedDate, "10"));

        // make Repayment
        final PostLoansLoanIdTransactionsResponse repaymentTransaction = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("9 September 2022").locale("en")
                        .transactionAmount(10.0));

        GetLoansLoanIdResponse loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());

        // set loan as chargeoff
        String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6) + Utils.randomStringGenerator("is", 5);
        Integer chargeOffReasonId = CodeHelper.createChargeOffCodeValue(requestSpec, responseSpec, randomText, 1);
        String transactionExternalId = UUID.randomUUID().toString();
        PostLoansLoanIdTransactionsResponse chargeOffTransaction = this.loanTransactionHelper.chargeOffLoan((long) loanId,
                new PostLoansLoanIdTransactionsRequest().transactionDate("10 September 2022").locale("en").dateFormat("dd MMMM yyyy")
                        .externalId(transactionExternalId).chargeOffReasonId((long) chargeOffReasonId));

        loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify amounts for charge-off transaction
        verifyTransaction(LocalDate.of(2022, 9, 10), 1010.0f, 1000.0f, 0.0f, 10.0f, 0.0f, loanId, "chargeoff");
        // verify journal entries
        GetJournalEntriesTransactionIdResponse journalEntriesForChargeOff = journalEntryHelper
                .getJournalEntries("L" + chargeOffTransaction.getResourceId().toString());

        assertNotNull(journalEntriesForChargeOff);

        List<JournalEntryTransactionItem> journalEntries = journalEntriesForChargeOff.getPageItems();
        assertEquals(4, journalEntries.size());
        verifyJournalEntry(journalEntries.get(3), 1000.0, LocalDate.of(2022, 9, 10), loansReceivable, "CREDIT");
        verifyJournalEntry(journalEntries.get(2), 10.0, LocalDate.of(2022, 9, 10), interestFeeReceivable, "CREDIT");
        verifyJournalEntry(journalEntries.get(1), 1000.0, LocalDate.of(2022, 9, 10), creditLossBadDebt, "DEBIT");
        verifyJournalEntry(journalEntries.get(0), 10.0, LocalDate.of(2022, 9, 10), feeChargeOff, "DEBIT");

    }

    // Reverse Replay of Charge-Off
    @Test
    public void loanChargeOffReverseReplayWithAdvancedPaymentStrategyTest() {
        runAt("9 September 2022", () -> {
            String loanExternalIdStr = UUID.randomUUID().toString();
            final Integer loanProductID = createLoanProductWithPeriodicAccrualAccountingAndAdvancedPaymentAllocationStrategy();
            final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
            final Integer loanId = createLoanAccount(clientId, loanProductID, loanExternalIdStr);

            // apply charges
            Integer feeCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", false));

            LocalDate targetDate = LocalDate.of(2022, 9, 5);
            final String feeCharge1AddedDate = DATE_FORMATTER.format(targetDate);
            Integer feeLoanChargeId = loanTransactionHelper.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(feeCharge), feeCharge1AddedDate, "10"));

            // apply penalty
            Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", true));

            final String penaltyCharge1AddedDate = DATE_FORMATTER.format(targetDate);

            Integer penalty1LoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), penaltyCharge1AddedDate, "10"));

            // make Repayment
            final PostLoansLoanIdTransactionsResponse repaymentTransaction = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("9 September 2022").locale("en")
                            .transactionAmount(10.0));

            GetLoansLoanIdResponse loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
            assertTrue(loanDetails.getStatus().getActive());

            // set loan as chargeoff
            updateBusinessDate("10 September 2022");
            String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6)
                    + Utils.randomStringGenerator("is", 5);
            Integer chargeOffReasonId = CodeHelper.createChargeOffCodeValue(requestSpec, responseSpec, randomText, 1);
            String transactionExternalId = UUID.randomUUID().toString();
            PostLoansLoanIdTransactionsResponse chargeOffTransaction = this.loanTransactionHelper.chargeOffLoan((long) loanId,
                    new PostLoansLoanIdTransactionsRequest().transactionDate("10 September 2022").locale("en").dateFormat("dd MMMM yyyy")
                            .externalId(transactionExternalId).chargeOffReasonId((long) chargeOffReasonId));

            loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
            assertTrue(loanDetails.getStatus().getActive());
            assertTrue(loanDetails.getChargedOff());

            // verify amounts for charge-off transaction
            verifyTransaction(LocalDate.of(2022, 9, 10), 1010.0f, 1000.0f, 0.0f, 10.0f, 0.0f, loanId, "chargeoff");

            Long reversedAndReplayedTransactionId = chargeOffTransaction.getResourceId();

            // reverse Repayment
            updateBusinessDate("11 September 2022");
            loanTransactionHelper.reverseRepayment(loanId, repaymentTransaction.getResourceId().intValue(), "11 September 2022");

            // verify chargeOffTransaction gets reverse replayed

            GetLoansLoanIdTransactionsTransactionIdResponse getLoansTransactionResponse = loanTransactionHelper
                    .getLoanTransactionDetails((long) loanId, transactionExternalId);
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

        final Integer loanProductID = createLoanProductWithPeriodicAccrualAccountingAndAdvancedPaymentAllocationStrategy();
        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
        final Integer loanId = createLoanAccount(clientId, loanProductID, loanExternalIdStr);

        // make Repayment
        final PostLoansLoanIdTransactionsResponse repaymentTransaction = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("6 September 2022").locale("en")
                        .transactionAmount(100.0));

        GetLoansLoanIdResponse loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());

        // set loan as chargeoff
        String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6) + Utils.randomStringGenerator("is", 5);
        Integer chargeOffReasonId = CodeHelper.createChargeOffCodeValue(requestSpec, responseSpec, randomText, 1);
        String transactionExternalId = UUID.randomUUID().toString();
        loanTransactionHelper.chargeOffLoan((long) loanId, new PostLoansLoanIdTransactionsRequest().transactionDate("7 September 2022")
                .locale("en").dateFormat("dd MMMM yyyy").externalId(transactionExternalId).chargeOffReasonId((long) chargeOffReasonId));

        loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // undo charge-off
        String reverseTransactionExternalId = UUID.randomUUID().toString();
        PostLoansLoanIdTransactionsResponse undoChargeOffTxResponse = loanTransactionHelper.undoChargeOffLoan((long) loanId,
                new PostLoansLoanIdTransactionsRequest().reversalExternalId(reverseTransactionExternalId));
        assertNotNull(undoChargeOffTxResponse);

        loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertFalse(loanDetails.getChargedOff());

        GetLoansLoanIdTransactionsTransactionIdResponse chargeOffTransactionDetails = loanTransactionHelper
                .getLoanTransactionDetails((long) loanId, transactionExternalId);
        assertNotNull(chargeOffTransactionDetails);
        assertTrue(chargeOffTransactionDetails.getManuallyReversed());
        assertEquals(reverseTransactionExternalId, chargeOffTransactionDetails.getReversalExternalId());
    }

    // Backdated repayment transaction, Reverse replay of charge off
    @Test
    public void postChargeOffAddBackdatedTransactionAndReverseReplayTest() {
        runAt("3 September 2022", () -> {
            String loanExternalIdStr = UUID.randomUUID().toString();
            final Integer loanProductID = createLoanProductWithPeriodicAccrualAccountingAndAdvancedPaymentAllocationStrategy();
            final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
            final Integer loanId = createLoanAccount(clientId, loanProductID, loanExternalIdStr);

            // apply charges
            updateBusinessDate("5 September 2022");
            Integer feeCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", false));

            LocalDate targetDate = LocalDate.of(2022, 9, 5);
            final String feeCharge1AddedDate = DATE_FORMATTER.format(targetDate);
            Integer feeLoanChargeId = loanTransactionHelper.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(feeCharge), feeCharge1AddedDate, "10"));

            // set loan as chargeoff
            updateBusinessDate("14 September 2022");
            String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6)
                    + Utils.randomStringGenerator("is", 5);
            Integer chargeOffReasonId = CodeHelper.createChargeOffCodeValue(requestSpec, responseSpec, randomText, 1);
            String transactionExternalId = UUID.randomUUID().toString();
            PostLoansLoanIdTransactionsResponse chargeOffTransaction = loanTransactionHelper.chargeOffLoan((long) loanId,
                    new PostLoansLoanIdTransactionsRequest().transactionDate("14 September 2022").locale("en").dateFormat("dd MMMM yyyy")
                            .externalId(transactionExternalId).chargeOffReasonId((long) chargeOffReasonId));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
            assertTrue(loanDetails.getStatus().getActive());
            assertTrue(loanDetails.getChargedOff());

            Long reversedAndReplayedTransactionId = chargeOffTransaction.getResourceId();

            // verify Journal Entries For ChargeOff Transaction
            GetJournalEntriesTransactionIdResponse journalEntriesForChargeOff = journalEntryHelper
                    .getJournalEntries("L" + chargeOffTransaction.getResourceId().toString());

            assertNotNull(journalEntriesForChargeOff);
            List<JournalEntryTransactionItem> journalEntries = journalEntriesForChargeOff.getPageItems();
            assertEquals(4, journalEntries.size());

            verifyJournalEntry(journalEntries.get(3), 1000.0, LocalDate.of(2022, 9, 14), loansReceivable, "CREDIT");
            verifyJournalEntry(journalEntries.get(2), 10.0, LocalDate.of(2022, 9, 14), interestFeeReceivable, "CREDIT");
            verifyJournalEntry(journalEntries.get(1), 1000.0, LocalDate.of(2022, 9, 14), creditLossBadDebt, "DEBIT");
            verifyJournalEntry(journalEntries.get(0), 10.0, LocalDate.of(2022, 9, 14), feeChargeOff, "DEBIT");

            // make Repayment before chargeoff date - business date is still on 14 September 2022
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

            verifyJournalEntry(journalEntries.get(2), 90.0, LocalDate.of(2022, 9, 7), loansReceivable, "CREDIT");
            verifyJournalEntry(journalEntries.get(1), 10.0, LocalDate.of(2022, 9, 7), interestFeeReceivable, "CREDIT");
            verifyJournalEntry(journalEntries.get(0), 100.0, LocalDate.of(2022, 9, 7), suspenseClearingAccount, "DEBIT");

            // verify reverse replay of Charge-Off

            GetLoansLoanIdTransactionsTransactionIdResponse getLoansTransactionResponse = loanTransactionHelper
                    .getLoanTransactionDetails((long) loanId, transactionExternalId);
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

            verifyJournalEntry(journalEntries.get(1), 100.0, LocalDate.of(2022, 9, 15), recoveries, "CREDIT");
            verifyJournalEntry(journalEntries.get(0), 100.0, LocalDate.of(2022, 9, 15), suspenseClearingAccount, "DEBIT");
        });
    }

    // Repayment before charge off on charge off date, reverse replay of charge off
    @Test
    public void transactionOnChargeOffDateReverseTest() {
        runAt("7 September 2022", () -> {
            String loanExternalIdStr = UUID.randomUUID().toString();
            final Integer loanProductID = createLoanProductWithPeriodicAccrualAccountingAndAdvancedPaymentAllocationStrategy();
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

            verifyJournalEntry(journalEntries.get(2), 90.0, LocalDate.of(2022, 9, 7), loansReceivable, "CREDIT");
            verifyJournalEntry(journalEntries.get(1), 10.0, LocalDate.of(2022, 9, 7), interestFeeReceivable, "CREDIT");
            verifyJournalEntry(journalEntries.get(0), 100.0, LocalDate.of(2022, 9, 7), suspenseClearingAccount, "DEBIT");

            // set loan as chargeoff
            String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6)
                    + Utils.randomStringGenerator("is", 5);
            Integer chargeOffReasonId = CodeHelper.createChargeOffCodeValue(requestSpec, responseSpec, randomText, 1);
            String transactionExternalId = UUID.randomUUID().toString();
            PostLoansLoanIdTransactionsResponse chargeOffTransaction = loanTransactionHelper.chargeOffLoan((long) loanId,
                    new PostLoansLoanIdTransactionsRequest().transactionDate("7 September 2022").locale("en").dateFormat("dd MMMM yyyy")
                            .externalId(transactionExternalId).chargeOffReasonId((long) chargeOffReasonId));

            loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
            assertTrue(loanDetails.getStatus().getActive());
            assertTrue(loanDetails.getChargedOff());

            Long reversedAndReplayedTransactionId = chargeOffTransaction.getResourceId();

            // verify Journal Entries For ChargeOff Transaction
            GetJournalEntriesTransactionIdResponse journalEntriesForChargeOff = journalEntryHelper
                    .getJournalEntries("L" + chargeOffTransaction.getResourceId().toString());

            assertNotNull(journalEntriesForChargeOff);
            journalEntries = journalEntriesForChargeOff.getPageItems();
            assertEquals(2, journalEntries.size());

            verifyJournalEntry(journalEntries.get(1), 910.0, LocalDate.of(2022, 9, 7), loansReceivable, "CREDIT");
            verifyJournalEntry(journalEntries.get(0), 910.0, LocalDate.of(2022, 9, 7), creditLossBadDebt, "DEBIT");

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

            verifyJournalEntry(journalEntries.get(5), 90.0, LocalDate.of(2022, 9, 7), loansReceivable, "CREDIT");
            verifyJournalEntry(journalEntries.get(4), 10.0, LocalDate.of(2022, 9, 7), interestFeeReceivable, "CREDIT");
            verifyJournalEntry(journalEntries.get(3), 100.0, LocalDate.of(2022, 9, 7), suspenseClearingAccount, "DEBIT");
            verifyJournalEntry(journalEntries.get(2), 90.0, LocalDate.of(2022, 9, 7), loansReceivable, "DEBIT");
            verifyJournalEntry(journalEntries.get(1), 10.0, LocalDate.of(2022, 9, 7), interestFeeReceivable, "DEBIT");
            verifyJournalEntry(journalEntries.get(0), 100.0, LocalDate.of(2022, 9, 7), suspenseClearingAccount, "CREDIT");

            // verify reverse replay of Charge-Off

            GetLoansLoanIdTransactionsTransactionIdResponse getLoansTransactionResponse = loanTransactionHelper
                    .getLoanTransactionDetails((long) loanId, transactionExternalId);
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

    private void verifyJournalEntry(JournalEntryTransactionItem journalEntryTransactionItem, Double amount, LocalDate entryDate,
            Account account, String type) {
        assertEquals(amount, journalEntryTransactionItem.getAmount());
        assertEquals(entryDate, journalEntryTransactionItem.getTransactionDate());
        assertEquals(account.getAccountID().longValue(), journalEntryTransactionItem.getGlAccountId().longValue());
        assertEquals(type, journalEntryTransactionItem.getEntryType().getValue());
    }

    private void verifyTransaction(final LocalDate transactionDate, final Float transactionAmount, final Float principalPortion,
            final Float interestPortion, final Float feePortion, final Float penaltyPortion, final Integer loanID,
            final String transactionOfType) {
        ArrayList<HashMap> transactions = (ArrayList<HashMap>) loanTransactionHelper.getLoanTransactions(this.requestSpec,
                this.responseSpec, loanID);
        boolean isTransactionFound = false;
        for (int i = 0; i < transactions.size(); i++) {
            HashMap transactionType = (HashMap) transactions.get(i).get("type");
            boolean isTransaction = (Boolean) transactionType.get(transactionOfType);

            if (isTransaction) {
                ArrayList<Integer> transactionDateAsArray = (ArrayList<Integer>) transactions.get(i).get("date");
                LocalDate transactionEntryDate = LocalDate.of(transactionDateAsArray.get(0), transactionDateAsArray.get(1),
                        transactionDateAsArray.get(2));

                if (transactionDate.isEqual(transactionEntryDate)) {
                    isTransactionFound = true;
                    assertEquals(transactionAmount, Float.valueOf(String.valueOf(transactions.get(i).get("amount"))),
                            "Mismatch in transaction amounts");
                    assertEquals(principalPortion, Float.valueOf(String.valueOf(transactions.get(i).get("principalPortion"))),
                            "Mismatch in transaction amounts");
                    assertEquals(interestPortion, Float.valueOf(String.valueOf(transactions.get(i).get("interestPortion"))),
                            "Mismatch in transaction amounts");
                    assertEquals(feePortion, Float.valueOf(String.valueOf(transactions.get(i).get("feeChargesPortion"))),
                            "Mismatch in transaction amounts");
                    assertEquals(penaltyPortion, Float.valueOf(String.valueOf(transactions.get(i).get("penaltyChargesPortion"))),
                            "Mismatch in transaction amounts");
                    break;
                }
            }
        }
        assertTrue(isTransactionFound, "No Transaction entries are posted");
    }

    private Integer createLoanAccount(final Integer clientID, final Integer loanProductID, final String externalId) {

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("30")
                .withLoanTermFrequencyAsDays().withNumberOfRepayments("1").withRepaymentEveryAfter("30").withRepaymentFrequencyTypeAsDays()
                .withInterestRatePerPeriod("0").withInterestTypeAsFlatBalance().withAmortizationTypeAsEqualPrincipalPayments()
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod().withExpectedDisbursementDate("03 September 2022")
                .withSubmittedOnDate("01 September 2022").withLoanType("individual").withExternalId(externalId)
                .withRepaymentStrategy("advanced-payment-allocation-strategy").build(clientID.toString(), loanProductID.toString(), null);

        final Integer loanId = loanTransactionHelper.getLoanId(loanApplicationJSON);
        loanTransactionHelper.approveLoan("02 September 2022", "1000", loanId, null);
        loanTransactionHelper.disburseLoanWithTransactionAmount("03 September 2022", loanId, "1000");
        return loanId;
    }

    private Integer createLoanProductWithPeriodicAccrualAccountingAndAdvancedPaymentAllocationStrategy() {

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

        String futureInstallmentAllocationRule = "NEXT_INSTALLMENT";
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation(futureInstallmentAllocationRule);

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
