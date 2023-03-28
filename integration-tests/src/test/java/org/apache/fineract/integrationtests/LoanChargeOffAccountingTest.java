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

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.UUID;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PutLoansLoanIdResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.JournalEntry;
import org.apache.fineract.integrationtests.common.accounting.JournalEntryHelper;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.system.CodeHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoanChargeOffAccountingTest {

    private ResponseSpecification responseSpec;
    private ResponseSpecification responseSpec403;
    private RequestSpecification requestSpec;
    private ClientHelper clientHelper;
    private LoanTransactionHelper loanTransactionHelper;
    private LoanTransactionHelper loanTransactionHelperValidationError;
    private JournalEntryHelper journalEntryHelper;
    private AccountHelper accountHelper;
    private Account assetAccount;
    private Account incomeAccount;
    private Account expenseAccount;
    private Account overpaymentAccount;
    private DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().appendPattern("dd MMMM yyyy").toFormatter();

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.responseSpec403 = new ResponseSpecBuilder().expectStatusCode(403).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.loanTransactionHelperValidationError = new LoanTransactionHelper(this.requestSpec, new ResponseSpecBuilder().build());
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.assetAccount = this.accountHelper.createAssetAccount();
        this.incomeAccount = this.accountHelper.createIncomeAccount();
        this.expenseAccount = this.accountHelper.createExpenseAccount();
        this.overpaymentAccount = this.accountHelper.createLiabilityAccount();
        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);
        this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void loanChargeOffAccountingTreatmentTestForPeriodicAccrualAccounting() {
        // Loan ExternalId
        String loanExternalIdStr = UUID.randomUUID().toString();

        // Product to GL account mapping for test
        // ASSET
        // -fundSourceAccountId,loanPortfolioAccountId,transfersInSuspenseAccountId,receivableFeeAccountId,receivablePenaltyAccountId,receivableInterestAccountId
        // INCOME-interestOnLoanAccountId,incomeFromFeeAccountId,incomeFromPenaltyAccountId,incomeFromRecoveryAccountId,incomeFromChargeOffInterestAccountId,incomeFromChargeOffFeesAccountId,incomeFromChargeOffPenaltyAccountId,incomeFromGoodwillCreditInterestAccountId,incomeFromGoodwillCreditFeesAccountId,incomeFromGoodwillCreditPenaltyAccountId
        // EXPENSE-writeOffAccountId,goodwillCreditAccountId,chargeOffExpenseAccountId,chargeOffFraudExpenseAccountId
        // LIABILITY-overpaymentLiabilityAccountId

        final Integer loanProductID = createLoanProductWithPeriodicAccrualAccounting(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);
        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
        final Integer loanId = createLoanAccount(clientId, loanProductID, loanExternalIdStr);

        // apply charges
        Integer feeCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", false));

        LocalDate targetDate = LocalDate.of(2022, 9, 5);
        final String feeCharge1AddedDate = dateFormatter.format(targetDate);
        Integer feeLoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(feeCharge), feeCharge1AddedDate, "10"));

        // apply penalty
        Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", true));

        final String penaltyCharge1AddedDate = dateFormatter.format(targetDate);

        Integer penalty1LoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), penaltyCharge1AddedDate, "10"));

        // set loan as chargeoff
        String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6) + Utils.randomStringGenerator("is", 5);
        Integer chargeOffReasonId = CodeHelper.createChargeOffCodeValue(requestSpec, responseSpec, randomText, 1);
        String transactionExternalId = UUID.randomUUID().toString();
        this.loanTransactionHelper.chargeOffLoan((long) loanId, new PostLoansLoanIdTransactionsRequest().transactionDate("6 September 2022")
                .locale("en").dateFormat("dd MMMM yyyy").externalId(transactionExternalId).chargeOffReasonId((long) chargeOffReasonId));

        GetLoansLoanIdResponse loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries For ChargeOff Transaction
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "6 September 2022",
                new JournalEntry(1020, JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, "6 September 2022",
                new JournalEntry(1000, JournalEntry.TransactionType.DEBIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "6 September 2022",
                new JournalEntry(20, JournalEntry.TransactionType.DEBIT));

        // make Repayment
        final PostLoansLoanIdTransactionsResponse repaymentTransaction = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("7 September 2022").locale("en")
                        .transactionAmount(100.0));

        loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries for Repayment transaction
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "7 September 2022",
                new JournalEntry(100, JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "7 September 2022",
                new JournalEntry(100, JournalEntry.TransactionType.DEBIT));

        // Merchant Refund
        final PostLoansLoanIdTransactionsResponse merchantIssuedRefund_1 = loanTransactionHelper.makeMerchantIssuedRefund((long) loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("8 September 2022").locale("en")
                        .transactionAmount(100.0));

        loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries for Merchant Refund
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, "8 September 2022",
                new JournalEntry(100, JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "8 September 2022",
                new JournalEntry(100, JournalEntry.TransactionType.DEBIT));

        // Payout Refund
        final PostLoansLoanIdTransactionsResponse payoutRefund_1 = loanTransactionHelper.makePayoutRefund((long) loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("9 September 2022").locale("en")
                        .transactionAmount(100.0));

        loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries for Payout Refund
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, "9 September 2022",
                new JournalEntry(100, JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "9 September 2022",
                new JournalEntry(100, JournalEntry.TransactionType.DEBIT));

        // Goodwill Credit
        final PostLoansLoanIdTransactionsResponse goodwillCredit_1 = loanTransactionHelper.makeGoodwillCredit((long) loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("10 September 2022").locale("en")
                        .transactionAmount(100.0));

        loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries for Goodwill Credit
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, "10 September 2022",
                new JournalEntry(100, JournalEntry.TransactionType.DEBIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "10 September 2022",
                new JournalEntry(100, JournalEntry.TransactionType.CREDIT));

        // make overpaid repayment
        final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("11 September 2022").locale("en")
                        .transactionAmount(720.0));

        loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getOverpaid());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal entries for overpaid repayment
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(overpaymentAccount, "11 September 2022",
                new JournalEntry(100, JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "11 September 2022",
                new JournalEntry(620, JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "11 September 2022",
                new JournalEntry(720, JournalEntry.TransactionType.DEBIT));

        // CBR for making loan active again
        final PostLoansLoanIdTransactionsResponse cbr_transaction = loanTransactionHelper.makeCreditBalanceRefund(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("12 September 2022").locale("en")
                        .transactionAmount(100.0));

        // Charge Adjustment making loan overpaid
        final PostLoansLoanIdChargesChargeIdResponse chargeAdjustmentResult = loanTransactionHelper.chargeAdjustment((long) loanId,
                (long) feeLoanChargeId, new PostLoansLoanIdChargesChargeIdRequest().amount(10.0).locale("en"));

        loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getOverpaid());

        final LocalDate todaysDate = Utils.getLocalDateOfTenant();
        String transactionDate = Utils.dateFormatter.format(todaysDate);

        // verify Journal entries for Charge Adjustment
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(overpaymentAccount, transactionDate,
                new JournalEntry(10, JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, transactionDate,
                new JournalEntry(10, JournalEntry.TransactionType.DEBIT));
    }

    @Test
    public void loanChargeOffFraudAccountingTreatmentTestForCashBasedAccounting() {
        // Loan ExternalId
        String loanExternalIdStr = UUID.randomUUID().toString();

        // Product to GL account mapping for test
        // ASSET
        // -fundSourceAccountId,loanPortfolioAccountId,transfersInSuspenseAccountId
        // INCOME-interestOnLoanAccountId,incomeFromFeeAccountId,incomeFromPenaltyAccountId,incomeFromRecoveryAccountId,incomeFromChargeOffInterestAccountId,incomeFromChargeOffFeesAccountId,incomeFromChargeOffPenaltyAccountId,incomeFromGoodwillCreditInterestAccountId,incomeFromGoodwillCreditFeesAccountId,incomeFromGoodwillCreditPenaltyAccountId
        // EXPENSE-writeOffAccountId,goodwillCreditAccountId,chargeOffExpenseAccountId,chargeOffFraudExpenseAccountId
        // LIABILITY-overpaymentLiabilityAccountId

        final Integer loanProductID = createLoanProductWithCashBasedAccounting(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);
        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
        final Integer loanId = createLoanAccount(clientId, loanProductID, loanExternalIdStr);

        // apply charges
        Integer feeCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", false));

        LocalDate targetDate = LocalDate.of(2022, 9, 5);
        final String feeCharge1AddedDate = dateFormatter.format(targetDate);
        Integer feeLoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(feeCharge), feeCharge1AddedDate, "10"));

        // apply penalty
        Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", true));

        final String penaltyCharge1AddedDate = dateFormatter.format(targetDate);

        Integer penalty1LoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), penaltyCharge1AddedDate, "10"));

        // set loan as fraud
        final String command = "markAsFraud";
        String payload = loanTransactionHelper.getLoanFraudPayloadAsJSON("fraud", "true");
        PutLoansLoanIdResponse putLoansLoanIdResponse = loanTransactionHelper.modifyLoanCommand(loanId, command, payload,
                this.responseSpec);

        GetLoansLoanIdResponse loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getFraud());

        // set loan as chargeoff
        String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6) + Utils.randomStringGenerator("is", 5);
        Integer chargeOffReasonId = CodeHelper.createChargeOffCodeValue(requestSpec, responseSpec, randomText, 1);
        String transactionExternalId = UUID.randomUUID().toString();
        this.loanTransactionHelper.chargeOffLoan((long) loanId, new PostLoansLoanIdTransactionsRequest().transactionDate("6 September 2022")
                .locale("en").dateFormat("dd MMMM yyyy").externalId(transactionExternalId).chargeOffReasonId((long) chargeOffReasonId));

        loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getFraud());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries For ChargeOff Transaction
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "6 September 2022",
                new JournalEntry(1000, JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "6 September 2022",
                new JournalEntry(20, JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, "6 September 2022",
                new JournalEntry(1000, JournalEntry.TransactionType.DEBIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "6 September 2022",
                new JournalEntry(20, JournalEntry.TransactionType.DEBIT));

        // make Repayment
        final PostLoansLoanIdTransactionsResponse repaymentTransaction = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("7 September 2022").locale("en")
                        .transactionAmount(100.0));

        loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getFraud());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries for Repayment transaction
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "7 September 2022",
                new JournalEntry(100, JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "7 September 2022",
                new JournalEntry(100, JournalEntry.TransactionType.DEBIT));

        // Merchant Refund
        final PostLoansLoanIdTransactionsResponse merchantIssuedRefund_1 = loanTransactionHelper.makeMerchantIssuedRefund((long) loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("8 September 2022").locale("en")
                        .transactionAmount(100.0));

        loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getFraud());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries for Merchant Refund
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, "8 September 2022",
                new JournalEntry(100, JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "8 September 2022",
                new JournalEntry(100, JournalEntry.TransactionType.DEBIT));

        // Payout Refund
        final PostLoansLoanIdTransactionsResponse payoutRefund_1 = loanTransactionHelper.makePayoutRefund((long) loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("9 September 2022").locale("en")
                        .transactionAmount(100.0));

        loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getFraud());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries for Payout Refund
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, "9 September 2022",
                new JournalEntry(100, JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "9 September 2022",
                new JournalEntry(100, JournalEntry.TransactionType.DEBIT));

        // Goodwill Credit
        final PostLoansLoanIdTransactionsResponse goodwillCredit_1 = loanTransactionHelper.makeGoodwillCredit((long) loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("10 September 2022").locale("en")
                        .transactionAmount(100.0));

        loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getFraud());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries for Goodwill Credit
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, "10 September 2022",
                new JournalEntry(100, JournalEntry.TransactionType.DEBIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "10 September 2022",
                new JournalEntry(100, JournalEntry.TransactionType.CREDIT));

        // make overpaid repayment
        final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("11 September 2022").locale("en")
                        .transactionAmount(720.0));

        loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getOverpaid());
        assertTrue(loanDetails.getFraud());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal entries for overpaid repayment
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(overpaymentAccount, "11 September 2022",
                new JournalEntry(100, JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "11 September 2022",
                new JournalEntry(620, JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "11 September 2022",
                new JournalEntry(720, JournalEntry.TransactionType.DEBIT));

        // CBR for making loan active again
        final PostLoansLoanIdTransactionsResponse cbr_transaction = loanTransactionHelper.makeCreditBalanceRefund(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("12 September 2022").locale("en")
                        .transactionAmount(100.0));

        // Charge Adjustment making loan overpaid
        final PostLoansLoanIdChargesChargeIdResponse chargeAdjustmentResult = loanTransactionHelper.chargeAdjustment((long) loanId,
                (long) feeLoanChargeId, new PostLoansLoanIdChargesChargeIdRequest().amount(10.0).locale("en"));

        loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getOverpaid());

        final LocalDate todaysDate = Utils.getLocalDateOfTenant();
        String transactionDate = Utils.dateFormatter.format(todaysDate);

        // verify Journal entries for Charge Adjustment
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(overpaymentAccount, transactionDate,
                new JournalEntry(10, JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, transactionDate,
                new JournalEntry(10, JournalEntry.TransactionType.DEBIT));
    }

    // Tests for Goodwill Credit accounting changes
    @Test
    public void loanAccountingTreatmentTestForGoodwillCreditPeriodicAccrualAccounting_NoChargeOff() {
        // Loan ExternalId
        String loanExternalIdStr = UUID.randomUUID().toString();

        // Product to GL account mapping for test
        // ASSET
        // -fundSourceAccountId,loanPortfolioAccountId,transfersInSuspenseAccountId,receivableFeeAccountId,receivablePenaltyAccountId,receivableInterestAccountId
        // INCOME-interestOnLoanAccountId,incomeFromFeeAccountId,incomeFromPenaltyAccountId,incomeFromRecoveryAccountId,incomeFromChargeOffInterestAccountId,incomeFromChargeOffFeesAccountId,incomeFromChargeOffPenaltyAccountId,incomeFromGoodwillCreditInterestAccountId,incomeFromGoodwillCreditFeesAccountId,incomeFromGoodwillCreditPenaltyAccountId
        // EXPENSE-writeOffAccountId,goodwillCreditAccountId,chargeOffExpenseAccountId,chargeOffFraudExpenseAccountId
        // LIABILITY-overpaymentLiabilityAccountId

        final Integer loanProductID = createLoanProductWithPeriodicAccrualAccounting(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);
        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
        final Integer loanId = createLoanAccount(clientId, loanProductID, loanExternalIdStr);

        // apply charges
        Integer feeCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", false));

        LocalDate targetDate = LocalDate.of(2022, 9, 5);
        final String feeCharge1AddedDate = dateFormatter.format(targetDate);
        Integer feeLoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(feeCharge), feeCharge1AddedDate, "10"));

        // apply penalty
        Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "110", true));

        final String penaltyCharge1AddedDate = dateFormatter.format(targetDate);

        Integer penalty1LoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), penaltyCharge1AddedDate, "10"));

        GetLoansLoanIdResponse loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());

        // Goodwill Credit
        final PostLoansLoanIdTransactionsResponse goodwillCredit_1 = loanTransactionHelper.makeGoodwillCredit((long) loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("06 September 2022").locale("en")
                        .transactionAmount(800.0));

        loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());

        // verify Journal Entries for Goodwill Credit
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "6 September 2022",
                new JournalEntry(800, JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, "6 September 2022",
                new JournalEntry(780, JournalEntry.TransactionType.DEBIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "6 September 2022",
                new JournalEntry(20, JournalEntry.TransactionType.DEBIT));

    }

    @Test
    public void loanAccountingTreatmentTestForGoodwillCreditPeriodicAccrualAccounting_ChargeOff() {
        // Loan ExternalId
        String loanExternalIdStr = UUID.randomUUID().toString();

        // Product to GL account mapping for test
        // ASSET
        // -fundSourceAccountId,loanPortfolioAccountId,transfersInSuspenseAccountId,receivableFeeAccountId,receivablePenaltyAccountId,receivableInterestAccountId
        // INCOME-interestOnLoanAccountId,incomeFromFeeAccountId,incomeFromPenaltyAccountId,incomeFromRecoveryAccountId,incomeFromChargeOffInterestAccountId,incomeFromChargeOffFeesAccountId,incomeFromChargeOffPenaltyAccountId,incomeFromGoodwillCreditInterestAccountId,incomeFromGoodwillCreditFeesAccountId,incomeFromGoodwillCreditPenaltyAccountId
        // EXPENSE-writeOffAccountId,goodwillCreditAccountId,chargeOffExpenseAccountId,chargeOffFraudExpenseAccountId
        // LIABILITY-overpaymentLiabilityAccountId

        final Integer loanProductID = createLoanProductWithPeriodicAccrualAccounting(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);
        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
        final Integer loanId = createLoanAccount(clientId, loanProductID, loanExternalIdStr);

        // apply charges
        Integer feeCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", false));

        LocalDate targetDate = LocalDate.of(2022, 9, 5);
        final String feeCharge1AddedDate = dateFormatter.format(targetDate);
        Integer feeLoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(feeCharge), feeCharge1AddedDate, "10"));

        // apply penalty
        Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", true));

        final String penaltyCharge1AddedDate = dateFormatter.format(targetDate);

        Integer penalty1LoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), penaltyCharge1AddedDate, "10"));

        // set loan as chargeoff
        String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6) + Utils.randomStringGenerator("is", 5);
        Integer chargeOffReasonId = CodeHelper.createChargeOffCodeValue(requestSpec, responseSpec, randomText, 1);
        String transactionExternalId = UUID.randomUUID().toString();
        this.loanTransactionHelper.chargeOffLoan((long) loanId, new PostLoansLoanIdTransactionsRequest().transactionDate("6 September 2022")
                .locale("en").dateFormat("dd MMMM yyyy").externalId(transactionExternalId).chargeOffReasonId((long) chargeOffReasonId));

        GetLoansLoanIdResponse loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries For ChargeOff Transaction
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "6 September 2022",
                new JournalEntry(1020, JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, "6 September 2022",
                new JournalEntry(1000, JournalEntry.TransactionType.DEBIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "6 September 2022",
                new JournalEntry(20, JournalEntry.TransactionType.DEBIT));

        // Goodwill Credit
        final PostLoansLoanIdTransactionsResponse goodwillCredit_1 = loanTransactionHelper.makeGoodwillCredit((long) loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("08 September 2022").locale("en")
                        .transactionAmount(800.0));

        loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries for Goodwill Credit
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "8 September 2022",
                new JournalEntry(800, JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "8 September 2022",
                new JournalEntry(20, JournalEntry.TransactionType.DEBIT));
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, "8 September 2022",
                new JournalEntry(780, JournalEntry.TransactionType.DEBIT));

    }

    @Test
    public void loanAccountingTreatmentTestForCashBasedAccounting_NoChargeOff() {
        // Loan ExternalId
        String loanExternalIdStr = UUID.randomUUID().toString();

        // Product to GL account mapping for test
        // ASSET
        // -fundSourceAccountId,loanPortfolioAccountId,transfersInSuspenseAccountId
        // INCOME-interestOnLoanAccountId,incomeFromFeeAccountId,incomeFromPenaltyAccountId,incomeFromRecoveryAccountId,incomeFromChargeOffInterestAccountId,incomeFromChargeOffFeesAccountId,incomeFromChargeOffPenaltyAccountId,incomeFromGoodwillCreditInterestAccountId,incomeFromGoodwillCreditFeesAccountId,incomeFromGoodwillCreditPenaltyAccountId
        // EXPENSE-writeOffAccountId,goodwillCreditAccountId,chargeOffExpenseAccountId,chargeOffFraudExpenseAccountId
        // LIABILITY-overpaymentLiabilityAccountId

        final Integer loanProductID = createLoanProductWithCashBasedAccounting(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);
        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
        final Integer loanId = createLoanAccount(clientId, loanProductID, loanExternalIdStr);

        // apply charges
        Integer feeCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", false));

        LocalDate targetDate = LocalDate.of(2022, 9, 5);
        final String feeCharge1AddedDate = dateFormatter.format(targetDate);
        Integer feeLoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(feeCharge), feeCharge1AddedDate, "10"));

        // apply penalty
        Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", true));

        final String penaltyCharge1AddedDate = dateFormatter.format(targetDate);

        Integer penalty1LoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), penaltyCharge1AddedDate, "10"));

        GetLoansLoanIdResponse loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());

        // Goodwill Credit
        final PostLoansLoanIdTransactionsResponse goodwillCredit_1 = loanTransactionHelper.makeGoodwillCredit((long) loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("06 September 2022").locale("en")
                        .transactionAmount(800.0));

        loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());

        // verify Journal Entries for Goodwill Credit
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "6 September 2022",
                new JournalEntry(780, JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "6 September 2022",
                new JournalEntry(10, JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "6 September 2022",
                new JournalEntry(10, JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, "6 September 2022",
                new JournalEntry(780, JournalEntry.TransactionType.DEBIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "6 September 2022",
                new JournalEntry(20, JournalEntry.TransactionType.DEBIT));

    }

    @Test
    public void loanAccountingTreatmentTestForCashBasedAccounting_ChargeOff() {
        // Loan ExternalId
        String loanExternalIdStr = UUID.randomUUID().toString();

        // Product to GL account mapping for test
        // ASSET
        // -fundSourceAccountId,loanPortfolioAccountId,transfersInSuspenseAccountId
        // INCOME-interestOnLoanAccountId,incomeFromFeeAccountId,incomeFromPenaltyAccountId,incomeFromRecoveryAccountId,incomeFromChargeOffInterestAccountId,incomeFromChargeOffFeesAccountId,incomeFromChargeOffPenaltyAccountId,incomeFromGoodwillCreditInterestAccountId,incomeFromGoodwillCreditFeesAccountId,incomeFromGoodwillCreditPenaltyAccountId
        // EXPENSE-writeOffAccountId,goodwillCreditAccountId,chargeOffExpenseAccountId,chargeOffFraudExpenseAccountId
        // LIABILITY-overpaymentLiabilityAccountId

        final Integer loanProductID = createLoanProductWithCashBasedAccounting(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);
        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
        final Integer loanId = createLoanAccount(clientId, loanProductID, loanExternalIdStr);

        // apply charges
        Integer feeCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", false));

        LocalDate targetDate = LocalDate.of(2022, 9, 5);
        final String feeCharge1AddedDate = dateFormatter.format(targetDate);
        Integer feeLoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(feeCharge), feeCharge1AddedDate, "10"));

        // apply penalty
        Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", true));

        final String penaltyCharge1AddedDate = dateFormatter.format(targetDate);

        Integer penalty1LoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), penaltyCharge1AddedDate, "10"));

        GetLoansLoanIdResponse loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());

        // set loan as chargeoff
        String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6) + Utils.randomStringGenerator("is", 5);
        Integer chargeOffReasonId = CodeHelper.createChargeOffCodeValue(requestSpec, responseSpec, randomText, 1);
        String transactionExternalId = UUID.randomUUID().toString();
        this.loanTransactionHelper.chargeOffLoan((long) loanId, new PostLoansLoanIdTransactionsRequest().transactionDate("6 September 2022")
                .locale("en").dateFormat("dd MMMM yyyy").externalId(transactionExternalId).chargeOffReasonId((long) chargeOffReasonId));

        loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries For ChargeOff Transaction
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "6 September 2022",
                new JournalEntry(1000, JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "6 September 2022",
                new JournalEntry(20, JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, "6 September 2022",
                new JournalEntry(1000, JournalEntry.TransactionType.DEBIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "6 September 2022",
                new JournalEntry(20, JournalEntry.TransactionType.DEBIT));

        // Goodwill Credit
        final PostLoansLoanIdTransactionsResponse goodwillCredit_1 = loanTransactionHelper.makeGoodwillCredit((long) loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("10 September 2022").locale("en")
                        .transactionAmount(800.0));

        loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // verify Journal Entries for Goodwill Credit
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "6 September 2022",
                new JournalEntry(1000, JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "6 September 2022",
                new JournalEntry(20, JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, "6 September 2022",
                new JournalEntry(1000, JournalEntry.TransactionType.DEBIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "6 September 2022",
                new JournalEntry(20, JournalEntry.TransactionType.DEBIT));
    }

    private Integer createLoanAccount(final Integer clientID, final Integer loanProductID, final String externalId) {

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("1")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("1").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("0").withInterestTypeAsFlatBalance()
                .withAmortizationTypeAsEqualPrincipalPayments().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate("03 September 2022").withSubmittedOnDate("01 September 2022").withLoanType("individual")
                .withExternalId(externalId).build(clientID.toString(), loanProductID.toString(), null);

        final Integer loanId = loanTransactionHelper.getLoanId(loanApplicationJSON);
        loanTransactionHelper.approveLoan("02 September 2022", "1000", loanId, null);
        loanTransactionHelper.disburseLoanWithNetDisbursalAmount("03 September 2022", loanId, "1000");
        return loanId;
    }

    private Integer createLoanProductWithPeriodicAccrualAccounting(final Account... accounts) {

        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("1000").withRepaymentAfterEvery("1")
                .withNumberOfRepayments("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat()
                .withAccountingRulePeriodicAccrual(accounts).withDaysInMonth("30").withDaysInYear("365").withMoratorium("0", "0")
                .build(null);

        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer createLoanProductWithCashBasedAccounting(final Account... accounts) {

        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("1000").withRepaymentAfterEvery("1")
                .withNumberOfRepayments("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat()
                .withAccountingRuleAsCashBased(accounts).withDaysInMonth("30").withDaysInYear("365").withMoratorium("0", "0").build(null);

        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

}
