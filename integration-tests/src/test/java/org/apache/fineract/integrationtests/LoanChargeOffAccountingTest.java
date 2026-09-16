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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Collections;
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
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.JournalEntry;
import org.apache.fineract.integrationtests.common.accounting.PeriodicAccrualAccountingHelper;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoanChargeOffAccountingTest extends FeignLoanTestBase {

    @BeforeEach
    public void setupAccounts() {
        this.assetAccount = getAccounts().getLoansReceivableAccount();
        this.incomeAccount = getAccounts().getInterestIncomeAccount();
        this.expenseAccount = getAccounts().getChargeOffExpenseAccount();
        this.overpaymentAccount = getAccounts().getOverpaymentAccount();
    }

    private Account assetAccount;
    private Account incomeAccount;
    private Account expenseAccount;
    private Account overpaymentAccount;

    private DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().appendPattern("dd MMMM yyyy").toFormatter();

    @Test
    public void loanChargeOffAccountingTreatmentTestForPeriodicAccrualAccounting() {
        runAt("6 September 2022", () -> {

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // Product to GL account mapping for test
            // ASSET
            // -fundSourceAccountId,loanPortfolioAccountId,transfersInSuspenseAccountId,receivableFeeAccountId,receivablePenaltyAccountId,receivableInterestAccountId
            // INCOME-interestOnLoanAccountId,incomeFromFeeAccountId,incomeFromPenaltyAccountId,incomeFromRecoveryAccountId,incomeFromChargeOffInterestAccountId,incomeFromChargeOffFeesAccountId,incomeFromChargeOffPenaltyAccountId,incomeFromGoodwillCreditInterestAccountId,incomeFromGoodwillCreditFeesAccountId,incomeFromGoodwillCreditPenaltyAccountId
            // EXPENSE-writeOffAccountId,goodwillCreditAccountId,chargeOffExpenseAccountId,chargeOffFraudExpenseAccountId
            // LIABILITY-overpaymentLiabilityAccountId

            final Long loanProductID = createLoanProductWithPeriodicAccrualAccounting();
            final Long clientId = createClient();
            final Long loanId = createLoanAccount(clientId, loanProductID, loanExternalIdStr);

            // apply charges
            Long feeCharge = chargesHelper.createLoanSpecifiedDueDateCharge(ChargeCalculationType.FLAT, 10.0d, false).getResourceId();

            LocalDate targetDate = LocalDate.of(2022, 9, 5);
            final String feeCharge1AddedDate = dateFormatter.format(targetDate);
            Long feeLoanChargeId = addChargesForLoan(loanId, new PostLoansLoanIdChargesRequest().locale(LoanTestData.LOCALE)
                    .dateFormat(LoanTestData.DATETIME_PATTERN).chargeId(feeCharge).amount(10.0d).dueDate(feeCharge1AddedDate))
                    .getResourceId();

            // apply penalty
            Long penalty = chargesHelper.createLoanSpecifiedDueDateCharge(ChargeCalculationType.FLAT, 10.0d, true).getResourceId();

            final String penaltyCharge1AddedDate = dateFormatter.format(targetDate);

            Long penalty1LoanChargeId = addChargesForLoan(loanId, new PostLoansLoanIdChargesRequest().locale(LoanTestData.LOCALE)
                    .dateFormat(LoanTestData.DATETIME_PATTERN).chargeId(penalty).amount(10.0d).dueDate(penaltyCharge1AddedDate))
                    .getResourceId();

            // set loan as chargeoff
            String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6)
                    + Utils.randomStringGenerator("is", 5);
            Long chargeOffReasonId = codeHelper.createChargeOffCodeValue(randomText, 1);
            String transactionExternalId = UUID.randomUUID().toString();
            chargeOffLoan(loanId, new PostLoansLoanIdTransactionsRequest().transactionDate("6 September 2022").locale("en")
                    .dateFormat("dd MMMM yyyy").externalId(transactionExternalId).chargeOffReasonId((long) chargeOffReasonId));

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getActive());
            assertTrue(loanDetails.getChargedOff());

            // verify Journal Entries For ChargeOff Transaction
            checkJournalEntryForAssetAccount(assetAccount, "6 September 2022",
                    journalEntry(1020, assetAccount, JournalEntry.TransactionType.CREDIT.name()));
            checkJournalEntryForExpenseAccount(expenseAccount, "6 September 2022",
                    journalEntry(1000, expenseAccount, JournalEntry.TransactionType.DEBIT.name()));
            checkJournalEntryForIncomeAccount(incomeAccount, "6 September 2022",
                    journalEntry(20, incomeAccount, JournalEntry.TransactionType.DEBIT.name()));

            updateBusinessDate("12 September 2022");
            // make Repayment
            final PostLoansLoanIdTransactionsResponse repaymentTransaction = makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("7 September 2022").locale("en")
                            .transactionAmount(100.0));

            loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getActive());
            assertTrue(loanDetails.getChargedOff());

            // verify Journal Entries for Repayment transaction
            checkJournalEntryForIncomeAccount(incomeAccount, "7 September 2022",
                    journalEntry(100, incomeAccount, JournalEntry.TransactionType.CREDIT.name()));
            checkJournalEntryForAssetAccount(assetAccount, "7 September 2022",
                    journalEntry(100, assetAccount, JournalEntry.TransactionType.DEBIT.name()));

            // Merchant Refund
            final PostLoansLoanIdTransactionsResponse merchantIssuedRefund_1 = makeMerchantIssuedRefund(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("8 September 2022").locale("en")
                            .transactionAmount(100.0));

            loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getActive());
            assertTrue(loanDetails.getChargedOff());

            // verify Journal Entries for Merchant Refund
            checkJournalEntryForExpenseAccount(expenseAccount, "8 September 2022",
                    journalEntry(100, expenseAccount, JournalEntry.TransactionType.CREDIT.name()));
            checkJournalEntryForAssetAccount(assetAccount, "8 September 2022",
                    journalEntry(100, assetAccount, JournalEntry.TransactionType.DEBIT.name()));

            // Payout Refund
            final PostLoansLoanIdTransactionsResponse payoutRefund_1 = makePayoutRefund(loanId, new PostLoansLoanIdTransactionsRequest()
                    .dateFormat("dd MMMM yyyy").transactionDate("9 September 2022").locale("en").transactionAmount(100.0));

            loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getActive());
            assertTrue(loanDetails.getChargedOff());

            // verify Journal Entries for Payout Refund
            checkJournalEntryForExpenseAccount(expenseAccount, "9 September 2022",
                    journalEntry(100, expenseAccount, JournalEntry.TransactionType.CREDIT.name()));
            checkJournalEntryForAssetAccount(assetAccount, "9 September 2022",
                    journalEntry(100, assetAccount, JournalEntry.TransactionType.DEBIT.name()));

            // Goodwill Credit
            final PostLoansLoanIdTransactionsResponse goodwillCredit_1 = makeGoodwillCredit(loanId, new PostLoansLoanIdTransactionsRequest()
                    .dateFormat("dd MMMM yyyy").transactionDate("10 September 2022").locale("en").transactionAmount(100.0));

            loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getActive());
            assertTrue(loanDetails.getChargedOff());

            // verify Journal Entries for Goodwill Credit
            checkJournalEntryForExpenseAccount(expenseAccount, "10 September 2022",
                    journalEntry(100, expenseAccount, JournalEntry.TransactionType.DEBIT.name()));
            checkJournalEntryForIncomeAccount(incomeAccount, "10 September 2022",
                    journalEntry(100, incomeAccount, JournalEntry.TransactionType.CREDIT.name()));

            // make overpaid repayment
            final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("11 September 2022").locale("en")
                            .transactionAmount(720.0));

            loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getOverpaid());
            assertTrue(loanDetails.getChargedOff());

            // verify Journal entries for overpaid repayment
            checkJournalEntryForLiabilityAccount(overpaymentAccount, "11 September 2022",
                    journalEntry(100, overpaymentAccount, JournalEntry.TransactionType.CREDIT.name()));
            checkJournalEntryForIncomeAccount(incomeAccount, "11 September 2022",
                    journalEntry(620, incomeAccount, JournalEntry.TransactionType.CREDIT.name()));
            checkJournalEntryForAssetAccount(assetAccount, "11 September 2022",
                    journalEntry(720, assetAccount, JournalEntry.TransactionType.DEBIT.name()));

            // CBR for making loan active again
            final PostLoansLoanIdTransactionsResponse cbr_transaction = makeCreditBalanceRefund(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("12 September 2022").locale("en")
                            .transactionAmount(100.0));

            // Charge Adjustment making loan overpaid
            final PostLoansLoanIdChargesChargeIdResponse chargeAdjustmentResult = chargeAdjustment(loanId, (long) feeLoanChargeId,
                    new PostLoansLoanIdChargesChargeIdRequest().amount(10.0).locale("en"));

            loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getOverpaid());

            // verify Journal entries for Charge Adjustment
            checkJournalEntryForLiabilityAccount(overpaymentAccount, "12 September 2022",
                    journalEntry(10, overpaymentAccount, JournalEntry.TransactionType.CREDIT.name()));
            checkJournalEntryForIncomeAccount(incomeAccount, "12 September 2022",
                    journalEntry(10, incomeAccount, JournalEntry.TransactionType.DEBIT.name()));

        });
    }

    @Test
    public void loanChargeOffFraudAccountingTreatmentTestForCashBasedAccounting() {
        runAt("6 September 2022", () -> {

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // Product to GL account mapping for test
            // ASSET
            // -fundSourceAccountId,loanPortfolioAccountId,transfersInSuspenseAccountId
            // INCOME-interestOnLoanAccountId,incomeFromFeeAccountId,incomeFromPenaltyAccountId,incomeFromRecoveryAccountId,incomeFromChargeOffInterestAccountId,incomeFromChargeOffFeesAccountId,incomeFromChargeOffPenaltyAccountId,incomeFromGoodwillCreditInterestAccountId,incomeFromGoodwillCreditFeesAccountId,incomeFromGoodwillCreditPenaltyAccountId
            // EXPENSE-writeOffAccountId,goodwillCreditAccountId,chargeOffExpenseAccountId,chargeOffFraudExpenseAccountId
            // LIABILITY-overpaymentLiabilityAccountId

            final Long loanProductID = createLoanProductWithCashBasedAccounting();
            final Long clientId = createClient();
            final Long loanId = createLoanAccount(clientId, loanProductID, loanExternalIdStr);

            // apply charges
            Long feeCharge = chargesHelper.createLoanSpecifiedDueDateCharge(ChargeCalculationType.FLAT, 10.0d, false).getResourceId();

            LocalDate targetDate = LocalDate.of(2022, 9, 5);
            final String feeCharge1AddedDate = dateFormatter.format(targetDate);
            Long feeLoanChargeId = addChargesForLoan(loanId, new PostLoansLoanIdChargesRequest().locale(LoanTestData.LOCALE)
                    .dateFormat(LoanTestData.DATETIME_PATTERN).chargeId(feeCharge).amount(10.0d).dueDate(feeCharge1AddedDate))
                    .getResourceId();

            // apply penalty
            Long penalty = chargesHelper.createLoanSpecifiedDueDateCharge(ChargeCalculationType.FLAT, 10.0d, true).getResourceId();

            final String penaltyCharge1AddedDate = dateFormatter.format(targetDate);

            Long penalty1LoanChargeId = addChargesForLoan(loanId, new PostLoansLoanIdChargesRequest().locale(LoanTestData.LOCALE)
                    .dateFormat(LoanTestData.DATETIME_PATTERN).chargeId(penalty).amount(10.0d).dueDate(penaltyCharge1AddedDate))
                    .getResourceId();

            // set loan as fraud
            changeLoanFraudState(loanId, true);

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getActive());
            assertTrue(loanDetails.getFraud());

            // set loan as chargeoff
            String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6)
                    + Utils.randomStringGenerator("is", 5);
            Long chargeOffReasonId = codeHelper.createChargeOffCodeValue(randomText, 1);
            String transactionExternalId = UUID.randomUUID().toString();
            chargeOffLoan(loanId, new PostLoansLoanIdTransactionsRequest().transactionDate("6 September 2022").locale("en")
                    .dateFormat("dd MMMM yyyy").externalId(transactionExternalId).chargeOffReasonId((long) chargeOffReasonId));

            loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getActive());
            assertTrue(loanDetails.getFraud());
            assertTrue(loanDetails.getChargedOff());

            // verify Journal Entries For ChargeOff Transaction
            checkJournalEntryForAssetAccount(assetAccount, "6 September 2022",
                    journalEntry(1000, assetAccount, JournalEntry.TransactionType.CREDIT.name()));
            checkJournalEntryForIncomeAccount(incomeAccount, "6 September 2022",
                    journalEntry(20, incomeAccount, JournalEntry.TransactionType.CREDIT.name()));
            checkJournalEntryForExpenseAccount(expenseAccount, "6 September 2022",
                    journalEntry(1000, expenseAccount, JournalEntry.TransactionType.DEBIT.name()));
            checkJournalEntryForIncomeAccount(incomeAccount, "6 September 2022",
                    journalEntry(20, incomeAccount, JournalEntry.TransactionType.DEBIT.name()));

            updateBusinessDate("12 September 2022");

            // make Repayment
            final PostLoansLoanIdTransactionsResponse repaymentTransaction = makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("7 September 2022").locale("en")
                            .transactionAmount(100.0));

            loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getActive());
            assertTrue(loanDetails.getFraud());
            assertTrue(loanDetails.getChargedOff());

            // verify Journal Entries for Repayment transaction
            checkJournalEntryForIncomeAccount(incomeAccount, "7 September 2022",
                    journalEntry(100, incomeAccount, JournalEntry.TransactionType.CREDIT.name()));
            checkJournalEntryForAssetAccount(assetAccount, "7 September 2022",
                    journalEntry(100, assetAccount, JournalEntry.TransactionType.DEBIT.name()));

            // Merchant Refund
            final PostLoansLoanIdTransactionsResponse merchantIssuedRefund_1 = makeMerchantIssuedRefund(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("8 September 2022").locale("en")
                            .transactionAmount(100.0));

            loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getActive());
            assertTrue(loanDetails.getFraud());
            assertTrue(loanDetails.getChargedOff());

            // verify Journal Entries for Merchant Refund
            checkJournalEntryForExpenseAccount(expenseAccount, "8 September 2022",
                    journalEntry(100, expenseAccount, JournalEntry.TransactionType.CREDIT.name()));
            checkJournalEntryForAssetAccount(assetAccount, "8 September 2022",
                    journalEntry(100, assetAccount, JournalEntry.TransactionType.DEBIT.name()));

            // Payout Refund
            final PostLoansLoanIdTransactionsResponse payoutRefund_1 = makePayoutRefund(loanId, new PostLoansLoanIdTransactionsRequest()
                    .dateFormat("dd MMMM yyyy").transactionDate("9 September 2022").locale("en").transactionAmount(100.0));

            loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getActive());
            assertTrue(loanDetails.getFraud());
            assertTrue(loanDetails.getChargedOff());

            // verify Journal Entries for Payout Refund
            checkJournalEntryForExpenseAccount(expenseAccount, "9 September 2022",
                    journalEntry(100, expenseAccount, JournalEntry.TransactionType.CREDIT.name()));
            checkJournalEntryForAssetAccount(assetAccount, "9 September 2022",
                    journalEntry(100, assetAccount, JournalEntry.TransactionType.DEBIT.name()));

            // Goodwill Credit
            final PostLoansLoanIdTransactionsResponse goodwillCredit_1 = makeGoodwillCredit(loanId, new PostLoansLoanIdTransactionsRequest()
                    .dateFormat("dd MMMM yyyy").transactionDate("10 September 2022").locale("en").transactionAmount(100.0));

            loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getActive());
            assertTrue(loanDetails.getFraud());
            assertTrue(loanDetails.getChargedOff());

            // verify Journal Entries for Goodwill Credit
            checkJournalEntryForExpenseAccount(expenseAccount, "10 September 2022",
                    journalEntry(100, expenseAccount, JournalEntry.TransactionType.DEBIT.name()));
            checkJournalEntryForIncomeAccount(incomeAccount, "10 September 2022",
                    journalEntry(100, incomeAccount, JournalEntry.TransactionType.CREDIT.name()));

            // make overpaid repayment
            final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("11 September 2022").locale("en")
                            .transactionAmount(720.0));

            loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getOverpaid());
            assertTrue(loanDetails.getFraud());
            assertTrue(loanDetails.getChargedOff());

            // verify Journal entries for overpaid repayment
            checkJournalEntryForLiabilityAccount(overpaymentAccount, "11 September 2022",
                    journalEntry(100, overpaymentAccount, JournalEntry.TransactionType.CREDIT.name()));
            checkJournalEntryForIncomeAccount(incomeAccount, "11 September 2022",
                    journalEntry(620, incomeAccount, JournalEntry.TransactionType.CREDIT.name()));
            checkJournalEntryForAssetAccount(assetAccount, "11 September 2022",
                    journalEntry(720, assetAccount, JournalEntry.TransactionType.DEBIT.name()));

            // CBR for making loan active again
            final PostLoansLoanIdTransactionsResponse cbr_transaction = makeCreditBalanceRefund(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("12 September 2022").locale("en")
                            .transactionAmount(100.0));

            // Charge Adjustment making loan overpaid
            final PostLoansLoanIdChargesChargeIdResponse chargeAdjustmentResult = chargeAdjustment(loanId, (long) feeLoanChargeId,
                    new PostLoansLoanIdChargesChargeIdRequest().amount(10.0).locale("en"));

            loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getOverpaid());

            // verify Journal entries for Charge Adjustment
            checkJournalEntryForLiabilityAccount(overpaymentAccount, "12 September 2022",
                    journalEntry(10, overpaymentAccount, JournalEntry.TransactionType.CREDIT.name()));
            checkJournalEntryForIncomeAccount(incomeAccount, "12 September 2022",
                    journalEntry(10, incomeAccount, JournalEntry.TransactionType.DEBIT.name()));
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

            final Long loanProductID = createLoanProductWithPeriodicAccrualAccounting();
            final Long clientId = createClient();
            final Long loanId = createLoanAccount(clientId, loanProductID, loanExternalIdStr);

            // apply charges
            Long feeCharge = chargesHelper.createLoanSpecifiedDueDateCharge(ChargeCalculationType.FLAT, 10.0d, false).getResourceId();

            LocalDate targetDate = LocalDate.of(2022, 9, 5);
            final String feeCharge1AddedDate = dateFormatter.format(targetDate);
            Long feeLoanChargeId = addChargesForLoan(loanId, new PostLoansLoanIdChargesRequest().locale(LoanTestData.LOCALE)
                    .dateFormat(LoanTestData.DATETIME_PATTERN).chargeId(feeCharge).amount(10.0d).dueDate(feeCharge1AddedDate))
                    .getResourceId();

            // apply penalty
            Long penalty = chargesHelper.createLoanSpecifiedDueDateCharge(ChargeCalculationType.FLAT, 110.0d, true).getResourceId();

            final String penaltyCharge1AddedDate = dateFormatter.format(targetDate);

            Long penalty1LoanChargeId = addChargesForLoan(loanId, new PostLoansLoanIdChargesRequest().locale(LoanTestData.LOCALE)
                    .dateFormat(LoanTestData.DATETIME_PATTERN).chargeId(penalty).amount(10.0d).dueDate(penaltyCharge1AddedDate))
                    .getResourceId();

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getActive());

            // Goodwill Credit
            final PostLoansLoanIdTransactionsResponse goodwillCredit_1 = makeGoodwillCredit(loanId, new PostLoansLoanIdTransactionsRequest()
                    .dateFormat("dd MMMM yyyy").transactionDate("06 September 2022").locale("en").transactionAmount(800.0));

            loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getActive());

            // verify Journal Entries for Goodwill Credit
            checkJournalEntryForAssetAccount(assetAccount, "6 September 2022",
                    journalEntry(800, assetAccount, JournalEntry.TransactionType.CREDIT.name()));
            checkJournalEntryForExpenseAccount(expenseAccount, "6 September 2022",
                    journalEntry(780, expenseAccount, JournalEntry.TransactionType.DEBIT.name()));
            checkJournalEntryForIncomeAccount(incomeAccount, "6 September 2022",
                    journalEntry(20, incomeAccount, JournalEntry.TransactionType.DEBIT.name()));
        });

    }

    @Test
    public void loanAccountingTreatmentTestForGoodwillCreditPeriodicAccrualAccounting_ChargeOff() {
        runAt("6 September 2022", () -> {

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // Product to GL account mapping for test
            // ASSET
            // -fundSourceAccountId,loanPortfolioAccountId,transfersInSuspenseAccountId,receivableFeeAccountId,receivablePenaltyAccountId,receivableInterestAccountId
            // INCOME-interestOnLoanAccountId,incomeFromFeeAccountId,incomeFromPenaltyAccountId,incomeFromRecoveryAccountId,incomeFromChargeOffInterestAccountId,incomeFromChargeOffFeesAccountId,incomeFromChargeOffPenaltyAccountId,incomeFromGoodwillCreditInterestAccountId,incomeFromGoodwillCreditFeesAccountId,incomeFromGoodwillCreditPenaltyAccountId
            // EXPENSE-writeOffAccountId,goodwillCreditAccountId,chargeOffExpenseAccountId,chargeOffFraudExpenseAccountId
            // LIABILITY-overpaymentLiabilityAccountId

            final Long loanProductID = createLoanProductWithPeriodicAccrualAccounting();
            final Long clientId = createClient();
            final Long loanId = createLoanAccount(clientId, loanProductID, loanExternalIdStr);

            // apply charges
            Long feeCharge = chargesHelper.createLoanSpecifiedDueDateCharge(ChargeCalculationType.FLAT, 10.0d, false).getResourceId();

            LocalDate targetDate = LocalDate.of(2022, 9, 5);
            final String feeCharge1AddedDate = dateFormatter.format(targetDate);
            Long feeLoanChargeId = addChargesForLoan(loanId, new PostLoansLoanIdChargesRequest().locale(LoanTestData.LOCALE)
                    .dateFormat(LoanTestData.DATETIME_PATTERN).chargeId(feeCharge).amount(10.0d).dueDate(feeCharge1AddedDate))
                    .getResourceId();

            // apply penalty
            Long penalty = chargesHelper.createLoanSpecifiedDueDateCharge(ChargeCalculationType.FLAT, 10.0d, true).getResourceId();

            final String penaltyCharge1AddedDate = dateFormatter.format(targetDate);

            Long penalty1LoanChargeId = addChargesForLoan(loanId, new PostLoansLoanIdChargesRequest().locale(LoanTestData.LOCALE)
                    .dateFormat(LoanTestData.DATETIME_PATTERN).chargeId(penalty).amount(10.0d).dueDate(penaltyCharge1AddedDate))
                    .getResourceId();

            // set loan as chargeoff
            String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6)
                    + Utils.randomStringGenerator("is", 5);
            Long chargeOffReasonId = codeHelper.createChargeOffCodeValue(randomText, 1);
            String transactionExternalId = UUID.randomUUID().toString();
            chargeOffLoan(loanId, new PostLoansLoanIdTransactionsRequest().transactionDate("6 September 2022").locale("en")
                    .dateFormat("dd MMMM yyyy").externalId(transactionExternalId).chargeOffReasonId((long) chargeOffReasonId));

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getActive());
            assertTrue(loanDetails.getChargedOff());

            // verify Journal Entries For ChargeOff Transaction
            checkJournalEntryForAssetAccount(assetAccount, "6 September 2022",
                    journalEntry(1020, assetAccount, JournalEntry.TransactionType.CREDIT.name()));
            checkJournalEntryForExpenseAccount(expenseAccount, "6 September 2022",
                    journalEntry(1000, expenseAccount, JournalEntry.TransactionType.DEBIT.name()));
            checkJournalEntryForIncomeAccount(incomeAccount, "6 September 2022",
                    journalEntry(20, incomeAccount, JournalEntry.TransactionType.DEBIT.name()));

            updateBusinessDate("12 September 2022");

            // Goodwill Credit
            final PostLoansLoanIdTransactionsResponse goodwillCredit_1 = makeGoodwillCredit(loanId, new PostLoansLoanIdTransactionsRequest()
                    .dateFormat("dd MMMM yyyy").transactionDate("08 September 2022").locale("en").transactionAmount(800.0));

            loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getActive());
            assertTrue(loanDetails.getChargedOff());

            // verify Journal Entries for Goodwill Credit
            checkJournalEntryForIncomeAccount(incomeAccount, "8 September 2022",
                    journalEntry(800, incomeAccount, JournalEntry.TransactionType.CREDIT.name()));
            checkJournalEntryForIncomeAccount(incomeAccount, "8 September 2022",
                    journalEntry(20, incomeAccount, JournalEntry.TransactionType.DEBIT.name()));
            checkJournalEntryForExpenseAccount(expenseAccount, "8 September 2022",
                    journalEntry(780, expenseAccount, JournalEntry.TransactionType.DEBIT.name()));
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

            final Long loanProductID = createLoanProductWithCashBasedAccounting();
            final Long clientId = createClient();
            final Long loanId = createLoanAccount(clientId, loanProductID, loanExternalIdStr);

            // apply charges
            Long feeCharge = chargesHelper.createLoanSpecifiedDueDateCharge(ChargeCalculationType.FLAT, 10.0d, false).getResourceId();

            LocalDate targetDate = LocalDate.of(2022, 9, 5);
            final String feeCharge1AddedDate = dateFormatter.format(targetDate);
            Long feeLoanChargeId = addChargesForLoan(loanId, new PostLoansLoanIdChargesRequest().locale(LoanTestData.LOCALE)
                    .dateFormat(LoanTestData.DATETIME_PATTERN).chargeId(feeCharge).amount(10.0d).dueDate(feeCharge1AddedDate))
                    .getResourceId();

            // apply penalty
            Long penalty = chargesHelper.createLoanSpecifiedDueDateCharge(ChargeCalculationType.FLAT, 10.0d, true).getResourceId();

            final String penaltyCharge1AddedDate = dateFormatter.format(targetDate);

            Long penalty1LoanChargeId = addChargesForLoan(loanId, new PostLoansLoanIdChargesRequest().locale(LoanTestData.LOCALE)
                    .dateFormat(LoanTestData.DATETIME_PATTERN).chargeId(penalty).amount(10.0d).dueDate(penaltyCharge1AddedDate))
                    .getResourceId();

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getActive());

            // Goodwill Credit
            final PostLoansLoanIdTransactionsResponse goodwillCredit_1 = makeGoodwillCredit(loanId, new PostLoansLoanIdTransactionsRequest()
                    .dateFormat("dd MMMM yyyy").transactionDate("06 September 2022").locale("en").transactionAmount(800.0));

            loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getActive());

            // verify Journal Entries for Goodwill Credit
            checkJournalEntryForAssetAccount(assetAccount, "6 September 2022",
                    journalEntry(780, assetAccount, JournalEntry.TransactionType.CREDIT.name()));
            checkJournalEntryForIncomeAccount(incomeAccount, "6 September 2022",
                    journalEntry(10, incomeAccount, JournalEntry.TransactionType.CREDIT.name()));
            checkJournalEntryForIncomeAccount(incomeAccount, "6 September 2022",
                    journalEntry(10, incomeAccount, JournalEntry.TransactionType.CREDIT.name()));
            checkJournalEntryForExpenseAccount(expenseAccount, "6 September 2022",
                    journalEntry(780, expenseAccount, JournalEntry.TransactionType.DEBIT.name()));
            checkJournalEntryForIncomeAccount(incomeAccount, "6 September 2022",
                    journalEntry(20, incomeAccount, JournalEntry.TransactionType.DEBIT.name()));
        });
    }

    @Test
    public void loanAccountingTreatmentTestForCashBasedAccounting_ChargeOff() {
        runAt("6 September 2022", () -> {

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // Product to GL account mapping for test
            // ASSET
            // -fundSourceAccountId,loanPortfolioAccountId,transfersInSuspenseAccountId
            // INCOME-interestOnLoanAccountId,incomeFromFeeAccountId,incomeFromPenaltyAccountId,incomeFromRecoveryAccountId,incomeFromChargeOffInterestAccountId,incomeFromChargeOffFeesAccountId,incomeFromChargeOffPenaltyAccountId,incomeFromGoodwillCreditInterestAccountId,incomeFromGoodwillCreditFeesAccountId,incomeFromGoodwillCreditPenaltyAccountId
            // EXPENSE-writeOffAccountId,goodwillCreditAccountId,chargeOffExpenseAccountId,chargeOffFraudExpenseAccountId
            // LIABILITY-overpaymentLiabilityAccountId

            final Long loanProductID = createLoanProductWithCashBasedAccounting();
            final Long clientId = createClient();
            final Long loanId = createLoanAccount(clientId, loanProductID, loanExternalIdStr);

            // apply charges
            Long feeCharge = chargesHelper.createLoanSpecifiedDueDateCharge(ChargeCalculationType.FLAT, 10.0d, false).getResourceId();

            LocalDate targetDate = LocalDate.of(2022, 9, 5);
            final String feeCharge1AddedDate = dateFormatter.format(targetDate);
            Long feeLoanChargeId = addChargesForLoan(loanId, new PostLoansLoanIdChargesRequest().locale(LoanTestData.LOCALE)
                    .dateFormat(LoanTestData.DATETIME_PATTERN).chargeId(feeCharge).amount(10.0d).dueDate(feeCharge1AddedDate))
                    .getResourceId();

            // apply penalty
            Long penalty = chargesHelper.createLoanSpecifiedDueDateCharge(ChargeCalculationType.FLAT, 10.0d, true).getResourceId();

            final String penaltyCharge1AddedDate = dateFormatter.format(targetDate);

            Long penalty1LoanChargeId = addChargesForLoan(loanId, new PostLoansLoanIdChargesRequest().locale(LoanTestData.LOCALE)
                    .dateFormat(LoanTestData.DATETIME_PATTERN).chargeId(penalty).amount(10.0d).dueDate(penaltyCharge1AddedDate))
                    .getResourceId();

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getActive());

            // set loan as chargeoff
            String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6)
                    + Utils.randomStringGenerator("is", 5);
            Long chargeOffReasonId = codeHelper.createChargeOffCodeValue(randomText, 1);
            String transactionExternalId = UUID.randomUUID().toString();
            chargeOffLoan(loanId, new PostLoansLoanIdTransactionsRequest().transactionDate("6 September 2022").locale("en")
                    .dateFormat("dd MMMM yyyy").externalId(transactionExternalId).chargeOffReasonId((long) chargeOffReasonId));

            loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getActive());
            assertTrue(loanDetails.getChargedOff());

            // verify Journal Entries For ChargeOff Transaction
            checkJournalEntryForAssetAccount(assetAccount, "6 September 2022",
                    journalEntry(1000, assetAccount, JournalEntry.TransactionType.CREDIT.name()));
            checkJournalEntryForIncomeAccount(incomeAccount, "6 September 2022",
                    journalEntry(20, incomeAccount, JournalEntry.TransactionType.CREDIT.name()));
            checkJournalEntryForExpenseAccount(expenseAccount, "6 September 2022",
                    journalEntry(1000, expenseAccount, JournalEntry.TransactionType.DEBIT.name()));
            checkJournalEntryForIncomeAccount(incomeAccount, "6 September 2022",
                    journalEntry(20, incomeAccount, JournalEntry.TransactionType.DEBIT.name()));

            updateBusinessDate("12 September 2022");

            // Goodwill Credit
            final PostLoansLoanIdTransactionsResponse goodwillCredit_1 = makeGoodwillCredit(loanId, new PostLoansLoanIdTransactionsRequest()
                    .dateFormat("dd MMMM yyyy").transactionDate("10 September 2022").locale("en").transactionAmount(800.0));

            loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getActive());
            assertTrue(loanDetails.getChargedOff());

            // verify Journal Entries for Goodwill Credit
            checkJournalEntryForAssetAccount(assetAccount, "6 September 2022",
                    journalEntry(1000, assetAccount, JournalEntry.TransactionType.CREDIT.name()));
            checkJournalEntryForIncomeAccount(incomeAccount, "6 September 2022",
                    journalEntry(20, incomeAccount, JournalEntry.TransactionType.CREDIT.name()));
            checkJournalEntryForExpenseAccount(expenseAccount, "6 September 2022",
                    journalEntry(1000, expenseAccount, JournalEntry.TransactionType.DEBIT.name()));
            checkJournalEntryForIncomeAccount(incomeAccount, "6 September 2022",
                    journalEntry(20, incomeAccount, JournalEntry.TransactionType.DEBIT.name()));
        });
    }

    @Test
    public void noIncomeRecognitionAfterChargeOff() {
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateBusinessDate(LocalDate.of(2020, 9, 5).toString());
            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            final Long loanProductId = this.createLoanProductWithInterestRecalculation();
            final Long clientId = createClient();

            final Long loanId = this.createLoanEntityWithEntitiesForTestResceduleWithLatePayment(clientId, loanProductId);

            // apply charges
            Long feeCharge = chargesHelper.createLoanSpecifiedDueDateCharge(ChargeCalculationType.FLAT, 10.0d, false).getResourceId();

            LocalDate targetDate = LocalDate.of(2022, 9, 5);
            final String feeCharge1AddedDate = dateFormatter.format(targetDate);
            Long feeLoanChargeId = addChargesForLoan(loanId, new PostLoansLoanIdChargesRequest().locale(LoanTestData.LOCALE)
                    .dateFormat(LoanTestData.DATETIME_PATTERN).chargeId(feeCharge).amount(10.0d).dueDate(feeCharge1AddedDate))
                    .getResourceId();

            // apply penalty
            Long penalty = chargesHelper.createLoanSpecifiedDueDateCharge(ChargeCalculationType.FLAT, 10.0d, true).getResourceId();

            final String penaltyCharge1AddedDate = dateFormatter.format(targetDate);

            Long penalty1LoanChargeId = addChargesForLoan(loanId, new PostLoansLoanIdChargesRequest().locale(LoanTestData.LOCALE)
                    .dateFormat(LoanTestData.DATETIME_PATTERN).chargeId(penalty).amount(10.0d).dueDate(penaltyCharge1AddedDate))
                    .getResourceId();

            updateBusinessDate(LocalDate.of(2020, 9, 6).toString());
            executeInlineCOB(loanId);
            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getTransactions().get(0).getType().getDisbursement());
            assertTrue(loanDetails.getTransactions().get(1).getType().getAccrual());
            assertEquals(2, loanDetails.getTransactions().size());

            // set loan as chargeoff
            String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6)
                    + Utils.randomStringGenerator("is", 5);
            Long chargeOffReasonId = codeHelper.createChargeOffCodeValue(randomText, 1);
            String transactionExternalId = UUID.randomUUID().toString();
            chargeOffLoan(loanId, new PostLoansLoanIdTransactionsRequest().transactionDate("6 September 2020").locale("en")
                    .dateFormat("dd MMMM yyyy").externalId(transactionExternalId).chargeOffReasonId((long) chargeOffReasonId));
            loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getActive());
            assertTrue(loanDetails.getChargedOff());

            // no accrual
            updateBusinessDate(LocalDate.of(2020, 9, 7).toString());
            executeInlineCOB(loanId);
            loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getTransactions().get(0).getType().getDisbursement());
            assertTrue(loanDetails.getTransactions().get(1).getType().getAccrual());
            assertTrue(loanDetails.getTransactions().get(2).getType().getAccrual());
            assertTrue(loanDetails.getTransactions().get(3).getType().getChargeoff());
            assertEquals(4, loanDetails.getTransactions().size());

            updateBusinessDate(LocalDate.of(2020, 9, 8).toString());
            executeInlineCOB(loanId);
            PeriodicAccrualAccountingHelper.runPeriodicAccrualAccounting(dateFormatter.format(LocalDate.of(2020, 9, 8)));
            loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getTransactions().get(0).getType().getDisbursement());
            assertTrue(loanDetails.getTransactions().get(1).getType().getAccrual());
            assertTrue(loanDetails.getTransactions().get(2).getType().getAccrual());
            assertTrue(loanDetails.getTransactions().get(3).getType().getChargeoff());
            assertEquals(4, loanDetails.getTransactions().size());

            undoChargeOffLoan(loanId);
            // generate accrual again
            updateBusinessDate(LocalDate.of(2020, 9, 9).toString());
            executeInlineCOB(loanId);
            loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getTransactions().get(0).getType().getDisbursement());
            assertTrue(loanDetails.getTransactions().get(1).getType().getAccrual());
            assertTrue(loanDetails.getTransactions().get(2).getType().getAccrual());
            assertTrue(loanDetails.getTransactions().get(3).getType().getChargeoff());
            assertTrue(loanDetails.getTransactions().get(4).getType().getAccrual());
            assertEquals(5, loanDetails.getTransactions().size());

            updateBusinessDate(LocalDate.of(2020, 9, 10).toString());

            chargeOffLoan(loanId, new PostLoansLoanIdTransactionsRequest().transactionDate("10 September 2020").locale("en")
                    .dateFormat("dd MMMM yyyy").chargeOffReasonId((long) chargeOffReasonId));

            makeLoanRepayment(loanId, new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy")
                    .transactionDate("10 September 2020").locale("en").transactionAmount(15825.23));
            executeInlineCOB(loanId);
            loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getTransactions().get(0).getType().getDisbursement());
            assertTrue(loanDetails.getTransactions().get(1).getType().getAccrual());
            assertTrue(loanDetails.getTransactions().get(2).getType().getAccrual());
            assertTrue(loanDetails.getTransactions().get(3).getType().getChargeoff());
            assertTrue(loanDetails.getTransactions().get(4).getType().getAccrual());
            assertTrue(loanDetails.getTransactions().get(5).getType().getAccrual());
            assertTrue(loanDetails.getTransactions().get(6).getType().getChargeoff());
            assertTrue(loanDetails.getTransactions().get(7).getType().getRepayment());
            assertEquals(8, loanDetails.getTransactions().size());
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
            Long clientId = createClient();
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

            PostLoansLoanIdTransactionsResponse chargeOffTransaction = chargeOffLoan(loanId,
                    new PostLoansLoanIdTransactionsRequest().transactionDate("02 January 2023").locale("en").dateFormat("dd MMMM yyyy")
                            .chargeOffReasonId(chargeOffReason.getSubResourceId()));
            // verify journal entries
            verifyTRJournalEntries(chargeOffTransaction.getResourceId(),
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(1000.0, chargeOffDelinquentExpenseAccount, JournalEntry.TransactionType.DEBIT.name()));
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
            Long clientId = createClient();
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

            PostLoansLoanIdTransactionsResponse chargeOffTransaction = chargeOffLoan(loanId,
                    new PostLoansLoanIdTransactionsRequest().transactionDate("02 January 2023").locale("en").dateFormat("dd MMMM yyyy")
                            .chargeOffReasonId(secondChargeOffReasonResponse.getSubResourceId()));
            // verify journal entries
            verifyTRJournalEntries(chargeOffTransaction.getResourceId(),
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(1000.0, getAccounts().getChargeOffExpenseAccount(), JournalEntry.TransactionType.DEBIT.name()));
        });
    }

    private PostLoanProductsResponse createLoanProductWithAdvancedChargeOffAccounting(PostCodeValueDataResponse chargeOffReason,
            Account chargeOffDelinquentExpenseAccount) {
        return loanHelper.createLoanProduct(new PostLoanProductsRequest().name(Utils.uniqueRandomStringGenerator("LOAN_PRODUCT_", 6))//
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
                .allowPartialPeriodInterestCalculation(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0)//
                .charges(Collections.emptyList())//
                .accountingRule(3)//
                .fundSourceAccountId(getAccounts().getFundSource().getAccountID().longValue())//
                .loanPortfolioAccountId(getAccounts().getLoansReceivableAccount().getAccountID().longValue())//
                .transfersInSuspenseAccountId(getAccounts().getSuspenseAccount().getAccountID().longValue())//
                .interestOnLoanAccountId(getAccounts().getInterestIncomeAccount().getAccountID().longValue())//
                .incomeFromFeeAccountId(getAccounts().getFeeIncomeAccount().getAccountID().longValue())//
                .incomeFromPenaltyAccountId(getAccounts().getPenaltyIncomeAccount().getAccountID().longValue())//
                .incomeFromRecoveryAccountId(getAccounts().getRecoveriesAccount().getAccountID().longValue())//
                .writeOffAccountId(getAccounts().getWrittenOffAccount().getAccountID().longValue())//
                .overpaymentLiabilityAccountId(getAccounts().getOverpaymentAccount().getAccountID().longValue())//
                .receivableInterestAccountId(getAccounts().getInterestReceivableAccount().getAccountID().longValue())//
                .receivableFeeAccountId(getAccounts().getFeeReceivableAccount().getAccountID().longValue())//
                .receivablePenaltyAccountId(getAccounts().getPenaltyReceivableAccount().getAccountID().longValue())//
                .goodwillCreditAccountId(getAccounts().getGoodwillExpenseAccount().getAccountID().longValue())//
                .incomeFromGoodwillCreditInterestAccountId(getAccounts().getInterestIncomeChargeOffAccount().getAccountID().longValue())//
                .incomeFromGoodwillCreditFeesAccountId(getAccounts().getFeeChargeOffAccount().getAccountID().longValue())//
                .incomeFromGoodwillCreditPenaltyAccountId(getAccounts().getFeeChargeOffAccount().getAccountID().longValue())//
                .incomeFromChargeOffInterestAccountId(getAccounts().getInterestIncomeChargeOffAccount().getAccountID().longValue())//
                .incomeFromChargeOffFeesAccountId(getAccounts().getFeeChargeOffAccount().getAccountID().longValue())//
                .incomeFromChargeOffPenaltyAccountId(getAccounts().getPenaltyChargeOffAccount().getAccountID().longValue())//
                .chargeOffExpenseAccountId(getAccounts().getChargeOffExpenseAccount().getAccountID().longValue())//
                .chargeOffFraudExpenseAccountId(getAccounts().getChargeOffFraudExpenseAccount().getAccountID().longValue())//
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
        return codeHelper.retrieveCodeByName("ChargeOffReasons");
    }

    private Long createLoanAccount(final Long clientId, final Long loanProductId, final String externalId) {
        final PostLoansRequest application = LoanRequestBuilders.applyLoan(clientId, loanProductId, "01 September 2022", 1000.0, 1)//
                .expectedDisbursementDate("03 September 2022")//
                .interestType(LoanTestData.InterestType.FLAT)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                .externalId(externalId);

        final Long loanId = applyForLoan(application);
        approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, "02 September 2022"));
        disburseLoan(loanId, "03 September 2022", 1000.0);
        return loanId;
    }

    private Long createLoanProductWithPeriodicAccrualAccounting() {

        final PostLoanProductsRequest loanProductRequest = new LoanProductTestBuilder().withPrincipal("1000").withRepaymentAfterEvery("1")
                .withNumberOfRepayments("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat()
                .withAccountingRulePeriodicAccrual(
                        new Account[] { getAccounts().getLoansReceivableAccount(), getAccounts().getInterestIncomeAccount(),
                                getAccounts().getChargeOffExpenseAccount(), getAccounts().getOverpaymentAccount() })
                .withDaysInMonth("30").withDaysInYear("365").withMoratorium("0", "0").buildRequest(null);

        return createLoanProduct(loanProductRequest);
    }

    private Long createLoanProductWithInterestRecalculation() {
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

        final PostLoanProductsRequest loanProductRequest = new LoanProductTestBuilder().withPrincipal("100000.00")
                .withNumberOfRepayments("12").withinterestRatePerPeriod("18").withInterestRateFrequencyTypeAsYear()
                .withInterestTypeAsDecliningBalance().withInterestCalculationPeriodTypeAsDays()
                .withInterestRecalculationDetails(interestRecalculationCompoundingMethod, rescheduleStrategyMethod,
                        preCloseInterestCalculationStrategy)
                .withInterestRecalculationRestFrequencyDetails(recalculationRestFrequencyType, recalculationRestFrequencyInterval,
                        recalculationRestFrequencyOnDayType, recalculationRestFrequencyDayOfWeekType)
                .withInterestRecalculationCompoundingFrequencyDetails(recalculationCompoundingFrequencyType,
                        recalculationCompoundingFrequencyInterval, recalculationCompoundingFrequencyOnDayType,
                        recalculationCompoundingFrequencyDayOfWeekType)
                .withAccountingRulePeriodicAccrual(
                        new Account[] { getAccounts().getLoansReceivableAccount(), getAccounts().getInterestIncomeAccount(),
                                getAccounts().getChargeOffExpenseAccount(), getAccounts().getOverpaymentAccount() })
                .buildRequest(null);

        return createLoanProduct(loanProductRequest);
    }

    private Long createLoanProductWithCashBasedAccounting() {

        final PostLoanProductsRequest loanProductRequest = new LoanProductTestBuilder().withPrincipal("1000").withRepaymentAfterEvery("1")
                .withNumberOfRepayments("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat()
                .withAccountingRuleAsCashBased(
                        new Account[] { getAccounts().getLoansReceivableAccount(), getAccounts().getInterestIncomeAccount(),
                                getAccounts().getChargeOffExpenseAccount(), getAccounts().getOverpaymentAccount() })
                .withDaysInMonth("30").withDaysInYear("365").withMoratorium("0", "0").buildRequest(null);

        return createLoanProduct(loanProductRequest);
    }

    private Long createLoanEntityWithEntitiesForTestResceduleWithLatePayment(Long clientId, Long loanProductId) {
        String firstRepaymentDate = "02 September 2020";
        String submittedDate = "02 September 2020";

        final PostLoansRequest application = LoanRequestBuilders.applyLoan(clientId, loanProductId, submittedDate, 15000.0, 12)//
                .loanTermFrequency(12)//
                .interestRatePerPeriod(BigDecimal.valueOf(12))//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.DAILY)//
                .repaymentsStartingFromDate(firstRepaymentDate)//
                .interestChargedFromDate(submittedDate)//
                .transactionProcessingStrategyCode(
                        LoanProductTestBuilder.DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY);

        Long loanId = applyForLoan(application);
        approveLoan(loanId, LoanRequestBuilders.approveLoan(15000.0, submittedDate));
        disburseLoanWithNetDisbursalAmount(loanId, submittedDate, "10000.00");
        return loanId;
    }

}
