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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.apache.fineract.client.models.AllowAttributeOverrides;
import org.apache.fineract.client.models.GetCodesResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostChargeOffReasonToExpenseAccountMappings;
import org.apache.fineract.client.models.PostCodeValueDataResponse;
import org.apache.fineract.client.models.PostCodeValuesDataRequest;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.client.models.PutLoansLoanIdResponse;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.JournalEntry;
import org.apache.fineract.integrationtests.common.accounting.JournalEntryHelper;
import org.apache.fineract.integrationtests.common.accounting.PeriodicAccrualAccountingHelper;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.system.CodeHelper;
import org.apache.fineract.integrationtests.inlinecob.InlineLoanCOBHelper;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoanChargeOffAccountingTest extends BaseLoanIntegrationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private ClientHelper clientHelper;
    private LoanTransactionHelper loanTransactionHelper;
    private JournalEntryHelper journalEntryHelper;
    private AccountHelper accountHelper;
    private Account assetAccount;
    private Account incomeAccount;
    private Account expenseAccount;
    private Account overpaymentAccount;
    private DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().appendPattern("dd MMMM yyyy").toFormatter();
    private InlineLoanCOBHelper inlineLoanCOBHelper;
    private PeriodicAccrualAccountingHelper periodicAccrualAccountingHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.requestSpec.header("Fineract-Platform-TenantId", "default");
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.assetAccount = this.accountHelper.createAssetAccount();
        this.incomeAccount = this.accountHelper.createIncomeAccount();
        this.expenseAccount = this.accountHelper.createExpenseAccount();
        this.overpaymentAccount = this.accountHelper.createLiabilityAccount();
        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);
        this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
        this.inlineLoanCOBHelper = new InlineLoanCOBHelper(this.requestSpec, this.responseSpec);
        this.periodicAccrualAccountingHelper = new PeriodicAccrualAccountingHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void loanChargeOffAccountingTreatmentTestForPeriodicAccrualAccounting() {
        runAt("12 September 2022", () -> {

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
            String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6)
                    + Utils.randomStringGenerator("is", 5);
            Integer chargeOffReasonId = CodeHelper.createChargeOffCodeValue(requestSpec, responseSpec, randomText, 1);
            String transactionExternalId = UUID.randomUUID().toString();
            this.loanTransactionHelper.chargeOffLoan((long) loanId,
                    new PostLoansLoanIdTransactionsRequest().transactionDate("6 September 2022").locale("en").dateFormat("dd MMMM yyyy")
                            .externalId(transactionExternalId).chargeOffReasonId((long) chargeOffReasonId));

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

            // verify Journal entries for Charge Adjustment
            this.journalEntryHelper.checkJournalEntryForLiabilityAccount(overpaymentAccount, "12 September 2022",
                    new JournalEntry(10, JournalEntry.TransactionType.CREDIT));
            this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "12 September 2022",
                    new JournalEntry(10, JournalEntry.TransactionType.DEBIT));

        });
    }

    @Test
    public void loanChargeOffFraudAccountingTreatmentTestForCashBasedAccounting() {
        runAt("12 September 2022", () -> {

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
            String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6)
                    + Utils.randomStringGenerator("is", 5);
            Integer chargeOffReasonId = CodeHelper.createChargeOffCodeValue(requestSpec, responseSpec, randomText, 1);
            String transactionExternalId = UUID.randomUUID().toString();
            this.loanTransactionHelper.chargeOffLoan((long) loanId,
                    new PostLoansLoanIdTransactionsRequest().transactionDate("6 September 2022").locale("en").dateFormat("dd MMMM yyyy")
                            .externalId(transactionExternalId).chargeOffReasonId((long) chargeOffReasonId));

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

            // verify Journal entries for Charge Adjustment
            this.journalEntryHelper.checkJournalEntryForLiabilityAccount(overpaymentAccount, "12 September 2022",
                    new JournalEntry(10, JournalEntry.TransactionType.CREDIT));
            this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, "12 September 2022",
                    new JournalEntry(10, JournalEntry.TransactionType.DEBIT));
        });
    }

    // Tests for Goodwill Credit accounting changes
    @Test
    public void loanAccountingTreatmentTestForGoodwillCreditPeriodicAccrualAccounting_NoChargeOff() {
        runAt("12 September 2022", () -> {

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
        });

    }

    @Test
    public void loanAccountingTreatmentTestForGoodwillCreditPeriodicAccrualAccounting_ChargeOff() {
        runAt("12 September 2022", () -> {

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
            String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6)
                    + Utils.randomStringGenerator("is", 5);
            Integer chargeOffReasonId = CodeHelper.createChargeOffCodeValue(requestSpec, responseSpec, randomText, 1);
            String transactionExternalId = UUID.randomUUID().toString();
            this.loanTransactionHelper.chargeOffLoan((long) loanId,
                    new PostLoansLoanIdTransactionsRequest().transactionDate("6 September 2022").locale("en").dateFormat("dd MMMM yyyy")
                            .externalId(transactionExternalId).chargeOffReasonId((long) chargeOffReasonId));

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
        });
    }

    @Test
    public void loanAccountingTreatmentTestForCashBasedAccounting_NoChargeOff() {
        runAt("12 September 2022", () -> {

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
        });
    }

    @Test
    public void loanAccountingTreatmentTestForCashBasedAccounting_ChargeOff() {
        runAt("12 September 2022", () -> {

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
            String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6)
                    + Utils.randomStringGenerator("is", 5);
            Integer chargeOffReasonId = CodeHelper.createChargeOffCodeValue(requestSpec, responseSpec, randomText, 1);
            String transactionExternalId = UUID.randomUUID().toString();
            this.loanTransactionHelper.chargeOffLoan((long) loanId,
                    new PostLoansLoanIdTransactionsRequest().transactionDate("6 September 2022").locale("en").dateFormat("dd MMMM yyyy")
                            .externalId(transactionExternalId).chargeOffReasonId((long) chargeOffReasonId));

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
        });
    }

    @Test
    public void noIncomeRecognitionAfterChargeOff() {
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, LocalDate.of(2020, 9, 5));
            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            final Integer loanProductId = this.createLoanProductWithInterestRecalculation(assetAccount, incomeAccount, expenseAccount,
                    overpaymentAccount);
            final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

            final Integer loanId = this.createLoanEntityWithEntitiesForTestResceduleWithLatePayment(clientId, loanProductId);

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

            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, LocalDate.of(2020, 9, 6));
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId.longValue()));
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());
            assertTrue(loanDetails.getTransactions().get(0).getType().getDisbursement());
            assertTrue(loanDetails.getTransactions().get(1).getType().getAccrual());
            assertEquals(2, loanDetails.getTransactions().size());

            // set loan as chargeoff
            String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6)
                    + Utils.randomStringGenerator("is", 5);
            Integer chargeOffReasonId = CodeHelper.createChargeOffCodeValue(requestSpec, responseSpec, randomText, 1);
            String transactionExternalId = UUID.randomUUID().toString();
            this.loanTransactionHelper.chargeOffLoan((long) loanId,
                    new PostLoansLoanIdTransactionsRequest().transactionDate("6 September 2020").locale("en").dateFormat("dd MMMM yyyy")
                            .externalId(transactionExternalId).chargeOffReasonId((long) chargeOffReasonId));
            loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
            assertTrue(loanDetails.getStatus().getActive());
            assertTrue(loanDetails.getChargedOff());

            // no accrual
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, LocalDate.of(2020, 9, 7));
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId.longValue()));
            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());
            assertTrue(loanDetails.getTransactions().get(0).getType().getDisbursement());
            assertTrue(loanDetails.getTransactions().get(1).getType().getAccrual());
            assertTrue(loanDetails.getTransactions().get(2).getType().getChargeoff());
            assertEquals(3, loanDetails.getTransactions().size());

            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, LocalDate.of(2020, 9, 8));
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId.longValue()));
            this.periodicAccrualAccountingHelper.runPeriodicAccrualAccounting(dateFormatter.format(LocalDate.of(2020, 9, 8)));
            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());
            assertTrue(loanDetails.getTransactions().get(0).getType().getDisbursement());
            assertTrue(loanDetails.getTransactions().get(1).getType().getAccrual());
            assertTrue(loanDetails.getTransactions().get(2).getType().getChargeoff());
            assertEquals(3, loanDetails.getTransactions().size());

            loanTransactionHelper.undoChargeOffLoan((long) loanId, new PostLoansLoanIdTransactionsRequest());
            // generate accrual again
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, LocalDate.of(2020, 9, 9));
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId.longValue()));
            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());
            assertTrue(loanDetails.getTransactions().get(0).getType().getDisbursement());
            assertTrue(loanDetails.getTransactions().get(1).getType().getAccrual());
            assertTrue(loanDetails.getTransactions().get(2).getType().getChargeoff());
            assertTrue(loanDetails.getTransactions().get(3).getType().getAccrual());
            assertEquals(4, loanDetails.getTransactions().size());

            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, LocalDate.of(2020, 9, 10));

            this.loanTransactionHelper.chargeOffLoan((long) loanId,
                    new PostLoansLoanIdTransactionsRequest().transactionDate("10 September 2020").locale("en").dateFormat("dd MMMM yyyy")
                            .chargeOffReasonId((long) chargeOffReasonId));

            loanTransactionHelper.makeLoanRepayment(loanId.longValue(), new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy")
                    .transactionDate("10 September 2020").locale("en").transactionAmount(15825.23));
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId.longValue()));
            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());
            assertTrue(loanDetails.getTransactions().get(0).getType().getDisbursement());
            assertTrue(loanDetails.getTransactions().get(1).getType().getAccrual());
            assertTrue(loanDetails.getTransactions().get(2).getType().getChargeoff());
            assertTrue(loanDetails.getTransactions().get(3).getType().getAccrual());
            assertTrue(loanDetails.getTransactions().get(4).getType().getChargeoff());
            assertTrue(loanDetails.getTransactions().get(5).getType().getRepayment());
            assertEquals(6, loanDetails.getTransactions().size());
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void advancedAccountingForChargeOff() {
        runAt("02 January 2023", () -> {
            final Account chargeOffDelinquentExpenseAccount = accountHelper
                    .createExpenseAccount("delinquent_expense_for_charge_off_reason");
            GetCodesResponse chargeOffReasonCode = fetchChargeOffReasonCode();
            PostCodeValueDataResponse chargeOffReason = codeHelper.createCodeValue(chargeOffReasonCode.getId(),
                    new PostCodeValuesDataRequest().name(Utils.uniqueRandomStringGenerator("DELINQUENT_", 6)).isActive(true).position(10));
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            PostLoanProductsResponse productsResponse = createLoanProductWithAdvancedChargeOffAccounting(chargeOffReason,
                    chargeOffDelinquentExpenseAccount);
            // We are creating a 2nd product to test, the mapping is correct!
            PostLoanProductsResponse secondProduct = createLoanProductWithAdvancedChargeOffAccounting(chargeOffReason,
                    chargeOffDelinquentExpenseAccount);
            Assertions.assertNotNull(secondProduct.getResourceId());
            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, productsResponse.getResourceId(), "01 January 2023", 1000.0, 1);
            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 January 2023");

            PostLoansLoanIdTransactionsResponse chargeOffTransaction = this.loanTransactionHelper.chargeOffLoan(loanId,
                    new PostLoansLoanIdTransactionsRequest().transactionDate("02 January 2023").locale("en").dateFormat("dd MMMM yyyy")
                            .chargeOffReasonId(chargeOffReason.getSubResourceId()));
            // verify journal entries
            verifyTRJournalEntries(chargeOffTransaction.getResourceId(), journalEntry(1000.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(1000.0, chargeOffDelinquentExpenseAccount, "DEBIT"));
        });
    }

    @Test
    public void advancedAccountingForChargeOffFallbackToRegular() {
        runAt("02 January 2023", () -> {
            final Account chargeOffDelinquentExpenseAccount = accountHelper
                    .createExpenseAccount("delinquent_expense_for_charge_off_reason");
            GetCodesResponse chargeOffReasonCode = fetchChargeOffReasonCode();
            PostCodeValueDataResponse chargeOffReason = codeHelper.createCodeValue(chargeOffReasonCode.getId(),
                    new PostCodeValuesDataRequest().name(Utils.uniqueRandomStringGenerator("DELINQUENT_", 6)).isActive(true).position(10));
            GetCodesResponse secondChargeOffReason = fetchChargeOffReasonCode();
            PostCodeValueDataResponse secondChargeOffReasonResponse = codeHelper.createCodeValue(secondChargeOffReason.getId(),
                    new PostCodeValuesDataRequest().name(Utils.uniqueRandomStringGenerator("FRAUD_", 6)).isActive(true).position(10));
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            PostLoanProductsResponse productsResponse = createLoanProductWithAdvancedChargeOffAccounting(chargeOffReason,
                    chargeOffDelinquentExpenseAccount);
            // We are creating a 2nd product to test, the mapping is correct!
            PostLoanProductsResponse secondProduct = createLoanProductWithAdvancedChargeOffAccounting(chargeOffReason,
                    chargeOffDelinquentExpenseAccount);
            Assertions.assertNotNull(secondProduct.getResourceId());
            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, productsResponse.getResourceId(), "01 January 2023", 1000.0, 1);
            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 January 2023");

            PostLoansLoanIdTransactionsResponse chargeOffTransaction = this.loanTransactionHelper.chargeOffLoan(loanId,
                    new PostLoansLoanIdTransactionsRequest().transactionDate("02 January 2023").locale("en").dateFormat("dd MMMM yyyy")
                            .chargeOffReasonId(secondChargeOffReasonResponse.getSubResourceId()));
            // verify journal entries
            verifyTRJournalEntries(chargeOffTransaction.getResourceId(), journalEntry(1000.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(1000.0, chargeOffExpenseAccount, "DEBIT"));
        });
    }

    private PostLoanProductsResponse createLoanProductWithAdvancedChargeOffAccounting(PostCodeValueDataResponse chargeOffReason,
            Account chargeOffDelinquentExpenseAccount) {
        return this.loanTransactionHelper.createLoanProduct(new PostLoanProductsRequest()
                .name(Utils.uniqueRandomStringGenerator("LOAN_PRODUCT_", 6))//
                .shortName(Utils.uniqueRandomStringGenerator("", 4))//
                .description("Loan Product Description")//
                .includeInBorrowerCycle(false)//
                .currencyCode("USD")//
                .digitsAfterDecimal(2)//
                .inMultiplesOf(0)//
                .installmentAmountInMultiplesOf(1)//
                .useBorrowerCycle(false)//
                .minPrincipal(100.0)//
                .principal(1000.0)//
                .maxPrincipal(100000.0)//
                .minNumberOfRepayments(1)//
                .numberOfRepayments(1)//
                .maxNumberOfRepayments(30)//
                .isLinkedToFloatingInterestRates(false)//
                .minInterestRatePerPeriod((double) 0)//
                .interestRatePerPeriod(0.0)//
                .maxInterestRatePerPeriod((double) 100)//
                .interestRateFrequencyType(2)//
                .repaymentEvery(30)//
                .repaymentFrequencyType(0L)//
                .amortizationType(1)//
                .interestType(0)//
                .isEqualAmortization(false)//
                .interestCalculationPeriodType(1)//
                .transactionProcessingStrategyCode(
                        LoanProductTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY)//
                .loanScheduleType(LoanScheduleType.CUMULATIVE.toString()) //
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
                .charges(Collections.emptyList())//
                .accountingRule(3)//
                .fundSourceAccountId(fundSource.getAccountID().longValue())//
                .loanPortfolioAccountId(loansReceivableAccount.getAccountID().longValue())//
                .transfersInSuspenseAccountId(suspenseAccount.getAccountID().longValue())//
                .interestOnLoanAccountId(interestIncomeAccount.getAccountID().longValue())//
                .incomeFromFeeAccountId(feeIncomeAccount.getAccountID().longValue())//
                .incomeFromPenaltyAccountId(penaltyIncomeAccount.getAccountID().longValue())//
                .incomeFromRecoveryAccountId(recoveriesAccount.getAccountID().longValue())//
                .writeOffAccountId(writtenOffAccount.getAccountID().longValue())//
                .overpaymentLiabilityAccountId(overpaymentAccount.getAccountID().longValue())//
                .receivableInterestAccountId(interestReceivableAccount.getAccountID().longValue())//
                .receivableFeeAccountId(feeReceivableAccount.getAccountID().longValue())//
                .receivablePenaltyAccountId(penaltyReceivableAccount.getAccountID().longValue())//
                .goodwillCreditAccountId(goodwillExpenseAccount.getAccountID().longValue())//
                .incomeFromGoodwillCreditInterestAccountId(interestIncomeChargeOffAccount.getAccountID().longValue())//
                .incomeFromGoodwillCreditFeesAccountId(feeChargeOffAccount.getAccountID().longValue())//
                .incomeFromGoodwillCreditPenaltyAccountId(feeChargeOffAccount.getAccountID().longValue())//
                .incomeFromChargeOffInterestAccountId(interestIncomeChargeOffAccount.getAccountID().longValue())//
                .incomeFromChargeOffFeesAccountId(feeChargeOffAccount.getAccountID().longValue())//
                .incomeFromChargeOffPenaltyAccountId(penaltyChargeOffAccount.getAccountID().longValue())//
                .chargeOffExpenseAccountId(chargeOffExpenseAccount.getAccountID().longValue())//
                .chargeOffFraudExpenseAccountId(chargeOffFraudExpenseAccount.getAccountID().longValue())//
                .addChargeOffReasonToExpenseAccountMappingsItem(
                        new PostChargeOffReasonToExpenseAccountMappings().chargeOffReasonCodeValueId(chargeOffReason.getSubResourceId())
                                .expenseAccountId(chargeOffDelinquentExpenseAccount.getAccountID().longValue()))
                .dateFormat(DATETIME_PATTERN)//
                .locale("en_GB")//
                .disallowExpectedDisbursements(true)//
                .allowApprovedDisbursedAmountsOverApplied(true)//
                .overAppliedCalculationType("percentage")//
                .overAppliedNumber(50));
    }

    private GetCodesResponse fetchChargeOffReasonCode() {
        return codeHelper.retrieveCodes().stream().filter(c -> "ChargeOffReasons".equals(c.getName())).findFirst().orElseThrow();
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

    private Integer createLoanProductWithInterestRecalculation(final Account... accounts) {
        final String interestRecalculationCompoundingMethod = LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_NONE;
        final String rescheduleStrategyMethod = LoanProductTestBuilder.RECALCULATION_STRATEGY_REDUCE_NUMBER_OF_INSTALLMENTS;
        final String recalculationRestFrequencyType = LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_DAILY;
        final String recalculationRestFrequencyInterval = "0";
        final String preCloseInterestCalculationStrategy = LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE;
        final String recalculationCompoundingFrequencyType = null;
        final String recalculationCompoundingFrequencyInterval = null;
        final Integer recalculationCompoundingFrequencyOnDayType = null;
        final Integer recalculationCompoundingFrequencyDayOfWeekType = null;
        final Integer recalculationRestFrequencyOnDayType = null;
        final Integer recalculationRestFrequencyDayOfWeekType = null;

        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("100000.00").withNumberOfRepayments("12")
                .withinterestRatePerPeriod("18").withInterestRateFrequencyTypeAsYear().withInterestTypeAsDecliningBalance()
                .withInterestCalculationPeriodTypeAsDays()
                .withInterestRecalculationDetails(interestRecalculationCompoundingMethod, rescheduleStrategyMethod,
                        preCloseInterestCalculationStrategy)
                .withInterestRecalculationRestFrequencyDetails(recalculationRestFrequencyType, recalculationRestFrequencyInterval,
                        recalculationRestFrequencyOnDayType, recalculationRestFrequencyDayOfWeekType)
                .withInterestRecalculationCompoundingFrequencyDetails(recalculationCompoundingFrequencyType,
                        recalculationCompoundingFrequencyInterval, recalculationCompoundingFrequencyOnDayType,
                        recalculationCompoundingFrequencyDayOfWeekType)
                .withAccountingRulePeriodicAccrual(accounts).build(null);

        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer createLoanProductWithCashBasedAccounting(final Account... accounts) {

        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("1000").withRepaymentAfterEvery("1")
                .withNumberOfRepayments("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat()
                .withAccountingRuleAsCashBased(accounts).withDaysInMonth("30").withDaysInYear("365").withMoratorium("0", "0").build(null);

        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer createLoanEntityWithEntitiesForTestResceduleWithLatePayment(Integer clientId, Integer loanProductId) {
        String firstRepaymentDate = "02 September 2020";
        String submittedDate = "02 September 2020";

        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("15000").withLoanTermFrequency("12")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("12").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestCalculationPeriodTypeAsDays()
                .withInterestRatePerPeriod("12").withInterestTypeAsDecliningBalance().withSubmittedOnDate(submittedDate)
                .withExpectedDisbursementDate(submittedDate).withFirstRepaymentDate(firstRepaymentDate)
                .withRepaymentStrategy(
                        LoanApplicationTestBuilder.DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY)
                .withinterestChargedFromDate(submittedDate).build(clientId.toString(), loanProductId.toString(), null);

        Integer loanId = this.loanTransactionHelper.getLoanId(loanApplicationJSON);

        this.loanTransactionHelper.approveLoan(submittedDate, loanId);
        this.loanTransactionHelper.disburseLoanWithNetDisbursalAmount(submittedDate, loanId, "10000.00");
        return loanId;
    }

}
