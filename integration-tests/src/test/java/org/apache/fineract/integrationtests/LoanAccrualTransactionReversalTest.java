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
import java.util.List;
import java.util.UUID;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.junit.jupiter.api.Test;

public class LoanAccrualTransactionReversalTest extends FeignLoanTestBase {

    @Test
    public void testNoAccrualTransactionReversalForMultipleDisbursementWithChargeForLoanAccountWithNoInterestBearingSchedulePeriodicAccrual() {

        final Account assetAccount = accountHelper.createAssetAccount("asset");
        final Account incomeAccount = accountHelper.createIncomeAccount("income");
        final Account expenseAccount = accountHelper.createExpenseAccount("expense");
        final Account overpaymentAccount = accountHelper.createLiabilityAccount("overpayment");

        String loanExternalIdStr = UUID.randomUUID().toString();

        final Long delinquencyBucketId = DelinquencyBucketsHelper.createDefaultBucket();
        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithMultipleDisbursement(
                delinquencyBucketId, assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
        assertNotNull(getLoanProductsProductResponse);

        Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
        final Long loanId = createLoanAccount(clientId, getLoanProductsProductResponse.getId(), loanExternalIdStr);
        disburseLoanWithAmount(loanId, "03 September 2022", 100.0);
        disburseLoanWithAmount(loanId, "04 September 2022", 300.0);

        LocalDate targetDate = LocalDate.of(2022, 9, 4);
        final String penaltyCharge1AddedDate = dateTimeFormatter.format(targetDate);

        addCharge(loanId, true, 10.0, penaltyCharge1AddedDate);

        runPeriodicAccrualAccounting(penaltyCharge1AddedDate);

        checkAccrualTransaction(targetDate, 0.0f, 0.0f, 10.0f, loanId);

        disburseLoanWithAmount(loanId, "05 September 2022", 600.0);

        checkAccrualTransaction(targetDate, 0.0f, 0.0f, 10.0f, loanId);

    }

    @Test
    public void testLastAccrualTransactionReversalRecalculationForLoanAccountWithInterestBearingScheduleWithDecliningBalance() {

        try {
            LocalDate currentDate = LocalDate.of(2022, 05, 8);
            final String accrualRunTillDate = dateTimeFormatter.format(currentDate);

            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateBusinessDate(accrualRunTillDate);

            final Account assetAccount = accountHelper.createAssetAccount("asset");
            final Account incomeAccount = accountHelper.createIncomeAccount("income");
            final Account expenseAccount = accountHelper.createExpenseAccount("expense");
            final Account overpaymentAccount = accountHelper.createLiabilityAccount("overpayment");

            String loanExternalIdStr = UUID.randomUUID().toString();

            final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithInterestRecalculation(assetAccount,
                    incomeAccount, expenseAccount, overpaymentAccount);
            assertNotNull(getLoanProductsProductResponse);
            final Long loanId = createLoanAccountWithInterestRecalculation(clientId, getLoanProductsProductResponse.getId(),
                    loanExternalIdStr);
            runPeriodicAccrualAccounting(accrualRunTillDate);
            checkAccrualTransaction(currentDate, 0.82f, 0.0f, 0.0f, loanId);
            makeLoanRepayment(loanExternalIdStr, new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN)
                    .transactionDate("5 February 2022").locale(LoanTestData.LOCALE).transactionAmount(106.57));
            checkAccrualTransaction(currentDate, 0.71f, 0.0f, 0.0f, loanId);
        } finally {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }

    }

    private Long createLoanAccountWithInterestRecalculation(final Long clientId, final Long loanProductId, final String externalId) {

        PostLoansRequest request = new PostLoansRequest().clientId(clientId).productId(loanProductId).principal(BigDecimal.valueOf(1000))
                .loanTermFrequency(12).loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS).numberOfRepayments(12)
                .repaymentEvery(1).repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)
                .amortizationType(LoanTestData.AmortizationType.EQUAL_INSTALLMENTS)
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.DAILY)
                .interestRatePerPeriod(BigDecimal.valueOf(12)).interestType(LoanTestData.InterestType.DECLINING_BALANCE)
                .graceOnPrincipalPayment(2).graceOnInterestPayment(2).expectedDisbursementDate("05 January 2022")
                .submittedOnDate("05 January 2022").dateFormat(LoanTestData.DATETIME_PATTERN).locale(LoanTestData.LOCALE)
                .loanType("individual").externalId(externalId)
                .transactionProcessingStrategyCode("due-penalty-interest-principal-fee-in-advance-penalty-interest-principal-fee-strategy");

        Long loanId = applyForLoan(request);
        approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, "05 January 2022"));
        disburseLoan(loanId, "05 January 2022", 1000.0);
        return loanId;
    }

    private GetLoanProductsProductIdResponse createLoanProductWithInterestRecalculation(final Account... accounts) {
        Long productId = createLoanProduct(twelveMonthInterestRecalculationPeriodicAccrual(accounts));
        return retrieveLoanProduct(productId);
    }

    private GetLoanProductsProductIdResponse createLoanProductWithMultipleDisbursement(final Long delinquencyBucketId,
            final Account... accounts) {
        Long productId = createLoanProduct(singleRepaymentMultiDisbursePeriodicAccrual(delinquencyBucketId, accounts));
        return retrieveLoanProduct(productId);
    }

    private Long createLoanAccount(final Long clientId, final Long loanProductId, final String externalId) {

        PostLoansRequest request = applyLoanRequest(clientId, loanProductId, "01 September 2022", 1000.0, 1,
                req -> req.expectedDisbursementDate("03 September 2022").externalId(externalId).interestRatePerPeriod(BigDecimal.ZERO)
                        .interestType(LoanTestData.InterestType.DECLINING_BALANCE)
                        .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)
                        .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)
                        .repaymentEvery(1).repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS).loanTermFrequency(1)
                        .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS).dateFormat(LoanTestData.DATETIME_PATTERN)
                        .locale(LoanTestData.LOCALE));

        Long loanId = applyForLoan(request);
        approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, "02 September 2022", "03 September 2022"));
        return loanId;
    }

    private void checkAccrualTransaction(final LocalDate transactionDate, final Float interestPortion, final Float feePortion,
            final Float penaltyPortion, final Long loanId) {

        List<GetLoansLoanIdTransactions> transactions = getLoanDetails(loanId).getTransactions();
        boolean isTransactionFound = false;
        for (GetLoansLoanIdTransactions transaction : transactions) {
            if (Boolean.TRUE.equals(transaction.getType().getAccrual())) {
                if (DateUtils.isEqual(transactionDate, transaction.getDate())) {
                    isTransactionFound = true;
                    assertEquals(interestPortion, Float.valueOf(String.valueOf(Utils.getDoubleValue(transaction.getInterestPortion()))),
                            "Mismatch in transaction amounts");
                    assertEquals(feePortion, Float.valueOf(String.valueOf(Utils.getDoubleValue(transaction.getFeeChargesPortion()))),
                            "Mismatch in transaction amounts");
                    assertEquals(penaltyPortion,
                            Float.valueOf(String.valueOf(Utils.getDoubleValue(transaction.getPenaltyChargesPortion()))),
                            "Mismatch in transaction amounts");
                    break;
                }
            }
        }
        assertTrue(isTransactionFound, "No Accrual entries are posted");
    }

}
