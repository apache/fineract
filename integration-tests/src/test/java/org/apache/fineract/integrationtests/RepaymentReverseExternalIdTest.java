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

import static org.apache.fineract.integrationtests.common.funds.FundsResourceHandler.createFund;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.fineract.client.models.AllowAttributeOverrides;
import org.apache.fineract.client.models.ChargeData;
import org.apache.fineract.client.models.ChargeToGLAccountMapper;
import org.apache.fineract.client.models.GetLoanFeeToIncomeAccountMappings;
import org.apache.fineract.client.models.GetLoanPaymentChannelToFundSourceMappings;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RepaymentReverseExternalIdTest extends BaseLoanIntegrationTest {

    private static final String loanAmount = "1000";
    private static final String startDate = "20 December 2024";
    private static final String firstRepaymentDate = "23 December 2024";
    private static final String secondRepaymentDate = "26 December 2024";
    private static final String reverseDate = "27 December 2024";
    private static final Double firstRepaymentAmount = 1000.0;
    private static final Double secondRepaymentAmount = 10.0;

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private ClientHelper clientHelper;
    private LoanTransactionHelper loanTransactionHelper;
    private static String loanExternalId = "";

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
        loanExternalId = UUID.randomUUID().toString();
    }

    @Test
    public void testReverseRepaymentUpdatesExternalIdCorrectlyForOverpayment() {
        try {
            // Set up
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID,
                    new PutGlobalConfigurationsRequest().enabled(true));
            final LocalDate todaysDate = Utils.getLocalDateOfTenant();
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, todaysDate);

            // Create a client and a loan with externalId
            final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
            final Long loanProductId = loanTransactionHelper.createLoanProduct(loanProductsRequest()).getResourceId();

            final Integer loanId = createLoanAccount(clientId, loanProductId, loanExternalId);

            // First repayment to cover part of the loan
            final PostLoansLoanIdTransactionsResponse repaymentTransaction1 = loanTransactionHelper.makeLoanRepayment(loanExternalId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate(firstRepaymentDate).locale("en")
                            .transactionAmount(firstRepaymentAmount));

            // Second repayment to create overpayment
            final PostLoansLoanIdTransactionsResponse repaymentTransaction2 = loanTransactionHelper.makeLoanRepayment(loanExternalId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate(secondRepaymentDate).locale("en")
                            .transactionAmount(secondRepaymentAmount)); // This creates an overpayment as total is now
            // 1010

            // Reverse the second repayment to remove the overpayment
            loanTransactionHelper.reverseRepayment(loanId.intValue(), repaymentTransaction2.getResourceId().intValue(), reverseDate);

            // Verify that the loan is no longer overpaid and overpaid date is reset
            GetLoansLoanIdResponse loanDetailsNotOverpaid = loanTransactionHelper.getLoanDetails(loanId.longValue());

            List<GetLoansLoanIdTransactions> accrualActivityTransactional = loanDetailsNotOverpaid.getTransactions().stream()
                    .filter(transaction -> transaction.getType().getAccrual().equals(true)).toList();

            assertFalse(accrualActivityTransactional.isEmpty());

            boolean allAccrualTransactionsHaveExternalId = accrualActivityTransactional.stream()
                    .allMatch(transaction -> transaction.getExternalId() != null);

            assertTrue(allAccrualTransactionsHaveExternalId);
        } finally {
            // Disable business date configuration if it was enabled
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    private PostLoanProductsRequest loanProductsRequest() {
        String name = Utils.uniqueRandomStringGenerator("LOAN_PRODUCT_", 6);
        String shortName = Utils.uniqueRandomStringGenerator("", 4);

        List<Integer> principalVariationsForBorrowerCycle = new ArrayList<>();
        List<Integer> numberOfRepaymentVariationsForBorrowerCycle = new ArrayList<>();
        List<Integer> interestRateVariationsForBorrowerCycle = new ArrayList<>();
        List<ChargeData> charges = new ArrayList<>();
        List<ChargeToGLAccountMapper> penaltyToIncomeAccountMappings = new ArrayList<>();
        List<GetLoanFeeToIncomeAccountMappings> feeToIncomeAccountMappings = new ArrayList<>();

        List<GetLoanPaymentChannelToFundSourceMappings> paymentChannelToFundSourceMappings = new ArrayList<>();
        GetLoanPaymentChannelToFundSourceMappings loanPaymentChannelToFundSourceMappings = new GetLoanPaymentChannelToFundSourceMappings();
        loanPaymentChannelToFundSourceMappings.fundSourceAccountId(fundSource.getAccountID().longValue());
        loanPaymentChannelToFundSourceMappings.paymentTypeId(1L);
        paymentChannelToFundSourceMappings.add(loanPaymentChannelToFundSourceMappings);

        final Integer fundId = createFund(requestSpec, responseSpec);
        Assertions.assertNotNull(fundId);

        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
        Assertions.assertNotNull(delinquencyBucketId);

        return new PostLoanProductsRequest()//
                .name(name)//
                .externalId(loanExternalId).enableAccrualActivityPosting(true)//
                .shortName(shortName)//
                .description(
                        "LP1 with 12% DECLINING BALANCE interest, interest period: Daily, Interest recalculation-Daily, Compounding:none")//
                .fundId(fundId.longValue())//
                .startDate(null)//
                .closeDate(null)//
                .includeInBorrowerCycle(false)//
                .currencyCode("EUR")//
                .digitsAfterDecimal(2)//
                .inMultiplesOf(1)//
                .installmentAmountInMultiplesOf(1)//
                .useBorrowerCycle(false)//
                .minPrincipal(100.0)//
                .principal(1000.0)//
                .maxPrincipal(10000.0)//
                .minNumberOfRepayments(1)//
                .numberOfRepayments(1)//
                .maxNumberOfRepayments(30)//
                .isLinkedToFloatingInterestRates(false)//
                .minInterestRatePerPeriod(0.0)//
                .interestRatePerPeriod(12.0)//
                .maxInterestRatePerPeriod(30.0)//
                .interestRateFrequencyType(3)//
                .repaymentEvery(30)//
                .repaymentFrequencyType(0L)//
                .principalVariationsForBorrowerCycle(principalVariationsForBorrowerCycle)//
                .numberOfRepaymentVariationsForBorrowerCycle(numberOfRepaymentVariationsForBorrowerCycle)//
                .interestRateVariationsForBorrowerCycle(interestRateVariationsForBorrowerCycle)//
                .amortizationType(1)//
                .interestType(0)//
                .isEqualAmortization(false)//
                .interestCalculationPeriodType(0)//
                .transactionProcessingStrategyCode("mifos-standard-strategy")//
                .daysInYearType(1)//
                .daysInMonthType(1)//
                .canDefineInstallmentAmount(true)//
                .graceOnArrearsAgeing(3)//
                .overdueDaysForNPA(179)//
                .accountMovesOutOfNPAOnlyOnArrearsCompletion(false)//
                .principalThresholdForLastInstallment(50)//
                .allowVariableInstallments(false)//
                .canUseForTopup(false)//
                .holdGuaranteeFunds(false)//
                .multiDisburseLoan(false)//
                .allowAttributeOverrides(new AllowAttributeOverrides()//
                        .amortizationType(true)//
                        .interestType(true)//
                        .transactionProcessingStrategyCode(true)//
                        .interestCalculationPeriodType(true)//
                        .inArrearsTolerance(true)//
                        .repaymentEvery(true)//
                        .graceOnPrincipalAndInterestPayment(true)//
                        .graceOnArrearsAgeing(true))
                .outstandingLoanBalance(10000.0)//
                .charges(charges)//
                .accountingRule(3)//

                .fundSourceAccountId(suspenseAccount.getAccountID().longValue())//
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
                .chargeOffExpenseAccountId(chargeOffExpenseAccount.getAccountID().longValue())//
                .chargeOffFraudExpenseAccountId(chargeOffFraudExpenseAccount.getAccountID().longValue())//
                .incomeFromChargeOffPenaltyAccountId(penaltyChargeOffAccount.getAccountID().longValue())//

                .dateFormat("dd MMMM yyyy")//
                .locale("en")//
                .disallowExpectedDisbursements(false)//
                .allowApprovedDisbursedAmountsOverApplied(false)//
                .delinquencyBucketId(delinquencyBucketId.longValue())//
                .paymentChannelToFundSourceMappings(paymentChannelToFundSourceMappings)//
                .penaltyToIncomeAccountMappings(penaltyToIncomeAccountMappings)//
                .feeToIncomeAccountMappings(feeToIncomeAccountMappings)//
                .isInterestRecalculationEnabled(false)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(3)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .allowPartialPeriodInterestCalcualtion(false);//
    }

    private Integer createLoanAccount(final Integer clientID, final Long loanProductID, final String externalId) {

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal(loanAmount).withExternalId(externalId)
                .withLoanTermFrequency("1").withLoanTermFrequencyAsMonths().withNumberOfRepayments("1").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("1").withInterestTypeAsFlatBalance()
                .withAmortizationTypeAsEqualPrincipalPayments().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate(startDate).withSubmittedOnDate(startDate).withLoanType("individual")
                .withExternalId(externalId).build(clientID.toString(), loanProductID.toString(), null);

        final Integer loanId = loanTransactionHelper.getLoanId(loanApplicationJSON);
        loanTransactionHelper.approveLoan(startDate, loanAmount, loanId, null);
        loanTransactionHelper.disburseLoan(Long.valueOf(loanId), new PostLoansLoanIdRequest().actualDisbursementDate(startDate)
                .transactionAmount(new BigDecimal(loanAmount)).locale("en").dateFormat("dd MMMM yyyy"));
        return loanId;
    }
}
