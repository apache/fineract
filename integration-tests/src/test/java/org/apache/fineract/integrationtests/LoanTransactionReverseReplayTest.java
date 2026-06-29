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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.apache.fineract.client.models.GetJournalEntriesTransactionIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.junit.jupiter.api.Test;

public class LoanTransactionReverseReplayTest extends FeignLoanTestBase {

    @Test
    public void loanTransactionReverseReplayWithAdditionalInstallmentAndChargesTest() {
        try {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateBusinessDate("04 October 2022");

            String loanExternalIdStr = UUID.randomUUID().toString();

            final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            final Long loanProductId = createDefaultLoanProductId();

            final Long loanId = createLoanAccount(clientId, loanProductId, loanExternalIdStr);

            final PostLoansLoanIdTransactionsResponse repaymentTransaction = makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate("03 October 2022")
                            .locale(LoanTestData.LOCALE).transactionAmount(1000.0));

            makeMerchantIssuedRefund(loanExternalIdStr, new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN)
                    .transactionDate("04 October 2022").locale(LoanTestData.LOCALE).transactionAmount(500.0));

            executeInlineCOB(loanId);

            updateBusinessDate("05 October 2022");

            makeCreditBalanceRefund(loanExternalIdStr, new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN)
                    .transactionDate("05 October 2022").locale(LoanTestData.LOCALE).transactionAmount(500.0));

            updateBusinessDate("06 October 2022");

            reverseLoanTransaction(loanExternalIdStr, repaymentTransaction.getResourceId(),
                    new PostLoansLoanIdTransactionsTransactionIdRequest().transactionDate("06 October 2022").locale(LoanTestData.LOCALE)
                            .dateFormat(LoanTestData.DATETIME_PATTERN).transactionAmount(0.0));

            LocalDate targetDate = LocalDate.of(2022, 10, 6);
            final String penaltyCharge1AddedDate = dateTimeFormatter.format(targetDate);
            addCharge(loanId, true, 10.0, penaltyCharge1AddedDate);
            executeInlineCOB(loanId);
        } finally {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void loanTransactionReverseReplayWithAdditionalInstallmentAndChargesScheduleDueDateTest() {
        try {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateBusinessDate("04 October 2022");

            String loanExternalIdStr = UUID.randomUUID().toString();

            final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            final Long loanProductId = createDefaultLoanProductId();

            final Long loanId = createLoanAccount(clientId, loanProductId, loanExternalIdStr);

            String loanTransactionExternalIdStr = UUID.randomUUID().toString();
            makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate("03 October 2022")
                            .locale(LoanTestData.LOCALE).transactionAmount(1000.0).externalId(loanTransactionExternalIdStr));

            LocalDate targetDate = LocalDate.of(2022, 10, 10);
            final String penaltyCharge1AddedDate = dateTimeFormatter.format(targetDate);
            addCharge(loanId, true, 10.0, penaltyCharge1AddedDate);
            executeInlineCOB(loanId);

            GetLoansLoanIdResponse loansLoanIdResponse = getLoanDetails(loanExternalIdStr);
            int lastPeriodIndex = loansLoanIdResponse.getRepaymentSchedule().getPeriods().size() - 1;
            assertEquals(LocalDate.of(2022, 10, 10),
                    loansLoanIdResponse.getRepaymentSchedule().getPeriods().get(lastPeriodIndex).getDueDate());

            updateBusinessDate("06 October 2022");

            final PostLoansLoanIdTransactionsResponse repaymentTransaction = makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate("06 October 2022")
                            .locale(LoanTestData.LOCALE).transactionAmount(500.0));

            makeCreditBalanceRefund(loanExternalIdStr, new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN)
                    .transactionDate("06 October 2022").locale(LoanTestData.LOCALE).transactionAmount(490.0));

            reverseLoanTransaction(loanExternalIdStr, repaymentTransaction.getResourceId(),
                    new PostLoansLoanIdTransactionsTransactionIdRequest().transactionDate("06 October 2022").locale(LoanTestData.LOCALE)
                            .dateFormat(LoanTestData.DATETIME_PATTERN).transactionAmount(0.0));

            loansLoanIdResponse = getLoanDetails(loanExternalIdStr);
            lastPeriodIndex = loansLoanIdResponse.getRepaymentSchedule().getPeriods().size() - 1;
            assertEquals(LocalDate.of(2022, 10, 10),
                    loansLoanIdResponse.getRepaymentSchedule().getPeriods().get(lastPeriodIndex).getDueDate());

            updateBusinessDate("11 October 2022");
            chargebackLoanTransaction(loanExternalIdStr, loanTransactionExternalIdStr, new PostLoansLoanIdTransactionsTransactionIdRequest()
                    .locale(LoanTestData.LOCALE).transactionAmount(100.0).paymentTypeId(1L));

            loansLoanIdResponse = getLoanDetails(loanExternalIdStr);
            lastPeriodIndex = loansLoanIdResponse.getRepaymentSchedule().getPeriods().size() - 1;
            assertEquals(LocalDate.of(2022, 10, 11),
                    loansLoanIdResponse.getRepaymentSchedule().getPeriods().get(lastPeriodIndex).getDueDate());
        } finally {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void loanTransactionReverseReplayWithChargeOffAndCBR() {
        try {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateBusinessDate("04 October 2022");

            final Account assetAccount = accountHelper.createAssetAccount("asset");
            final Account assetFeeAndPenaltyAccount = accountHelper.createAssetAccount("feePenaltyAsset");
            final Account incomeAccount = accountHelper.createIncomeAccount("income");
            final Account expenseAccount = accountHelper.createExpenseAccount("expense");
            final Account overpaymentAccount = accountHelper.createLiabilityAccount("overpayment");

            String loanExternalIdStr = UUID.randomUUID().toString();

            final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            final Long loanProductId = createLoanProductWithPeriodicAccrualAccounting(assetAccount, assetFeeAndPenaltyAccount,
                    incomeAccount, expenseAccount, overpaymentAccount);

            final Long loanId = createLoanAccount(clientId, loanProductId, loanExternalIdStr);

            Long chargeOffReasonId = codeHelper.createChargeOffCodeValue(
                    Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6) + Utils.randomStringGenerator("is", 5), 1);
            String transactionExternalId = UUID.randomUUID().toString();
            PostLoansLoanIdTransactionsResponse chargeOffResponse = chargeOffLoan(loanId,
                    new PostLoansLoanIdTransactionsRequest().transactionDate("03 October 2022").locale(LoanTestData.LOCALE)
                            .dateFormat(LoanTestData.DATETIME_PATTERN).externalId(transactionExternalId)
                            .chargeOffReasonId(chargeOffReasonId));

            final PostLoansLoanIdTransactionsResponse repaymentTransaction = makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate("03 October 2022")
                            .locale(LoanTestData.LOCALE).transactionAmount(1500.0));

            executeInlineCOB(loanId);

            updateBusinessDate("05 October 2022");

            PostLoansLoanIdTransactionsResponse cbrTransactionResponse = makeCreditBalanceRefund(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate("05 October 2022")
                            .locale(LoanTestData.LOCALE).transactionAmount(500.0));

            GetLoansLoanIdResponse loansLoanIdResponse = getLoanDetails(loanExternalIdStr);
            int lastTransactionIndex = loansLoanIdResponse.getTransactions().size() - 1;
            assertEquals(500.0, Utils.getDoubleValue(loansLoanIdResponse.getTransactions().get(lastTransactionIndex).getAmount()));

            GetJournalEntriesTransactionIdResponse journalEntriesForCBR = getJournalEntries(
                    "L" + cbrTransactionResponse.getResourceId().toString());
            assertNotNull(journalEntriesForCBR);
            List<JournalEntryTransactionItem> cbrExpenseJournalEntries = journalEntriesForCBR.getPageItems().stream()
                    .filter(journalEntry -> assetAccount.getAccountID().longValue() == journalEntry.getGlAccountId()).toList();

            List<JournalEntryTransactionItem> cbrAssetJournalEntries = journalEntriesForCBR.getPageItems().stream()
                    .filter(journalEntry -> overpaymentAccount.getAccountID().longValue() == journalEntry.getGlAccountId()).toList();

            assertEquals(1, cbrExpenseJournalEntries.size());
            assertEquals(1, cbrAssetJournalEntries.size());

            updateBusinessDate("06 October 2022");

            reverseLoanTransaction(loanExternalIdStr, repaymentTransaction.getResourceId(),
                    new PostLoansLoanIdTransactionsTransactionIdRequest().transactionDate("06 October 2022").locale(LoanTestData.LOCALE)
                            .dateFormat(LoanTestData.DATETIME_PATTERN).transactionAmount(0.0));

            journalEntriesForCBR = getJournalEntries("L" + cbrTransactionResponse.getResourceId().toString());
            assertNotNull(journalEntriesForCBR);
            cbrExpenseJournalEntries = journalEntriesForCBR.getPageItems().stream()
                    .filter(journalEntry -> assetAccount.getAccountID().longValue() == journalEntry.getGlAccountId()).toList();

            cbrAssetJournalEntries = journalEntriesForCBR.getPageItems().stream()
                    .filter(journalEntry -> overpaymentAccount.getAccountID().longValue() == journalEntry.getGlAccountId()).toList();

            assertEquals(2, cbrExpenseJournalEntries.size());
            assertEquals(2, cbrAssetJournalEntries.size());

            executeInlineCOB(loanId);
            loansLoanIdResponse = getLoanDetails(loanExternalIdStr);
            lastTransactionIndex = loansLoanIdResponse.getTransactions().size() - 1;
            assertEquals(500.0, Utils.getDoubleValue(loansLoanIdResponse.getTransactions().get(lastTransactionIndex).getAmount()));

            GetLoansLoanIdTransactions newCBRTransaction = loansLoanIdResponse.getTransactions().stream()
                    .filter(transaction -> transaction.getType().getCreditBalanceRefund()).findFirst().orElse(null);

            assertNotNull(newCBRTransaction);

            Long newCBRTransactionId = newCBRTransaction.getId();

            journalEntriesForCBR = getJournalEntries("L" + newCBRTransactionId);
            GetJournalEntriesTransactionIdResponse journalEntriesForChargeOff = getJournalEntries(
                    "L" + chargeOffResponse.getResourceId().toString());
            assertNotNull(journalEntriesForCBR);
            assertNotNull(journalEntriesForChargeOff);

            String expenseGlAccountCodeForChargeOff = journalEntriesForChargeOff.getPageItems().get(0).getGlAccountCode();
            String assetGlAccountCodeForChargeOff = journalEntriesForChargeOff.getPageItems().get(1).getGlAccountCode();

            cbrExpenseJournalEntries = journalEntriesForCBR.getPageItems().stream()
                    .filter(journalEntry -> expenseGlAccountCodeForChargeOff.equals(journalEntry.getGlAccountCode())
                            && expenseAccount.getAccountID().longValue() == journalEntry.getGlAccountId())
                    .toList();

            cbrAssetJournalEntries = journalEntriesForCBR.getPageItems().stream()
                    .filter(journalEntry -> assetGlAccountCodeForChargeOff.equals(journalEntry.getGlAccountCode())
                            && assetAccount.getAccountID().longValue() == journalEntry.getGlAccountId())
                    .toList();

            assertEquals(1, cbrExpenseJournalEntries.size());
            assertEquals(1, cbrAssetJournalEntries.size());
        } finally {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    private Long createDefaultLoanProductId() {
        return createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct().principal(1000.0).numberOfRepayments(1)
                .repaymentEvery(1).repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS_L));
    }

    private Long createLoanProductWithPeriodicAccrualAccounting(final Account assetAccount, final Account assetFeeAndPenaltyAccount,
            final Account incomeAccount, final Account expenseAccount, final Account overpaymentAccount) {
        final Long incomeAccountId = incomeAccount.getAccountID().longValue();
        final Long expenseAccountId = expenseAccount.getAccountID().longValue();
        PostLoanProductsRequest loanProductsRequest = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct().principal(1000.0)
                .numberOfRepayments(1).repaymentEvery(1).repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS_L)
                .interestRateFrequencyType(LoanTestData.InterestRateFrequencyType.MONTHS)
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL).interestType(LoanTestData.InterestType.FLAT)
                .daysInMonthType(LoanTestData.DaysInMonthType.DAYS_30).daysInYearType(LoanTestData.DaysInYearType.DAYS_365)
                .fundSourceAccountId(assetAccount.getAccountID().longValue())
                .loanPortfolioAccountId(assetAccount.getAccountID().longValue())
                .transfersInSuspenseAccountId(assetAccount.getAccountID().longValue())
                .receivableFeeAccountId(assetFeeAndPenaltyAccount.getAccountID().longValue())
                .receivablePenaltyAccountId(assetFeeAndPenaltyAccount.getAccountID().longValue())
                .receivableInterestAccountId(assetAccount.getAccountID().longValue()).interestOnLoanAccountId(incomeAccountId)
                .incomeFromFeeAccountId(incomeAccountId).incomeFromPenaltyAccountId(incomeAccountId)
                .incomeFromRecoveryAccountId(incomeAccountId).incomeFromChargeOffInterestAccountId(incomeAccountId)
                .incomeFromChargeOffFeesAccountId(incomeAccountId).incomeFromChargeOffPenaltyAccountId(incomeAccountId)
                .incomeFromGoodwillCreditInterestAccountId(incomeAccountId).incomeFromGoodwillCreditFeesAccountId(incomeAccountId)
                .incomeFromGoodwillCreditPenaltyAccountId(incomeAccountId).writeOffAccountId(expenseAccountId)
                .goodwillCreditAccountId(expenseAccountId).chargeOffExpenseAccountId(expenseAccountId)
                .chargeOffFraudExpenseAccountId(expenseAccountId)
                .overpaymentLiabilityAccountId(overpaymentAccount.getAccountID().longValue());
        return createLoanProduct(loanProductsRequest);
    }

    private Long createLoanAccount(final Long clientId, final Long loanProductId, final String externalId) {
        Long loanId = applyForLoan(applyLoanRequest(clientId, loanProductId, "01 September 2022", 1000.0, 1,
                r -> r.externalId(externalId).expectedDisbursementDate("03 September 2022").repaymentEvery(1)
                        .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS).loanTermFrequency(1)
                        .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)
                        .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL).interestType(LoanTestData.InterestType.FLAT)));
        approveLoan(loanId, approveLoanRequest(1000.0, "02 September 2022", "03 September 2022"));
        disburseLoan(loanId, "03 September 2022", 1000.0);
        return loanId;
    }

}
