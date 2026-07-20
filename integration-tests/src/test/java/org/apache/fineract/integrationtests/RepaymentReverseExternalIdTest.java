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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.fineract.client.models.AllowAttributeOverrides;
import org.apache.fineract.client.models.GetLoanPaymentChannelToFundSourceMappings;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.LoanProductChargeData;
import org.apache.fineract.client.models.LoanProductChargeToGLAccountMapper;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestAccounts;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.funds.FundsResourceHandler;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.junit.jupiter.api.Test;

public class RepaymentReverseExternalIdTest extends FeignLoanTestBase {

    private static final String loanAmount = "1000";
    private static final String startDate = "20 December 2024";
    private static final String firstRepaymentDate = "23 December 2024";
    private static final String secondRepaymentDate = "26 December 2024";
    private static final String reverseDate = "27 December 2024";
    private static final Double firstRepaymentAmount = 1000.0;
    private static final Double secondRepaymentAmount = 10.0;

    @Test
    public void testReverseRepaymentUpdatesExternalIdCorrectlyForOverpayment() {
        String loanExternalId = UUID.randomUUID().toString();
        runAt(reverseDate, () -> {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_AUTO_GENERATED_EXTERNAL_ID,
                    new PutGlobalConfigurationsRequest().enabled(true));

            final Long clientId = createClient();
            final Long loanProductId = createLoanProduct(loanProductsRequest(loanExternalId));

            final Long loanId = createLoanAccount(clientId, loanProductId, loanExternalId);

            final PostLoansLoanIdTransactionsResponse repaymentTransaction1 = makeLoanRepayment(loanExternalId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate(firstRepaymentDate).locale("en")
                            .transactionAmount(firstRepaymentAmount));

            final PostLoansLoanIdTransactionsResponse repaymentTransaction2 = makeLoanRepayment(loanExternalId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate(secondRepaymentDate).locale("en")
                            .transactionAmount(secondRepaymentAmount));

            reverseRepayment(loanId, repaymentTransaction2.getResourceId(), reverseDate);

            GetLoansLoanIdResponse loanDetailsNotOverpaid = getLoanDetails(loanId);

            List<GetLoansLoanIdTransactions> accrualActivityTransactional = loanDetailsNotOverpaid.getTransactions().stream()
                    .filter(transaction -> transaction.getType().getAccrual().equals(true)).toList();

            assertFalse(accrualActivityTransactional.isEmpty());

            boolean allAccrualTransactionsHaveExternalId = accrualActivityTransactional.stream()
                    .allMatch(transaction -> transaction.getExternalId() != null);

            assertTrue(allAccrualTransactionsHaveExternalId);
        });
    }

    private PostLoanProductsRequest loanProductsRequest(String loanExternalId) {
        LoanTestAccounts accounts = getAccounts();
        String name = Utils.uniqueRandomStringGenerator("LOAN_PRODUCT_", 6);
        String shortName = Utils.uniqueRandomStringGenerator("", 4);

        List<Integer> principalVariationsForBorrowerCycle = new ArrayList<>();
        List<Integer> numberOfRepaymentVariationsForBorrowerCycle = new ArrayList<>();
        List<Integer> interestRateVariationsForBorrowerCycle = new ArrayList<>();
        List<LoanProductChargeData> charges = new ArrayList<>();
        List<LoanProductChargeToGLAccountMapper> penaltyToIncomeAccountMappings = new ArrayList<>();
        List<LoanProductChargeToGLAccountMapper> feeToIncomeAccountMappings = new ArrayList<>();

        List<GetLoanPaymentChannelToFundSourceMappings> paymentChannelToFundSourceMappings = new ArrayList<>();
        GetLoanPaymentChannelToFundSourceMappings loanPaymentChannelToFundSourceMappings = new GetLoanPaymentChannelToFundSourceMappings();
        loanPaymentChannelToFundSourceMappings.fundSourceAccountId(accounts.getFundSource().getAccountID().longValue());
        loanPaymentChannelToFundSourceMappings.paymentTypeId(1L);
        paymentChannelToFundSourceMappings.add(loanPaymentChannelToFundSourceMappings);

        final Long fundId = FundsResourceHandler.createFund().getResourceId();
        assertNotNull(fundId);

        final Long delinquencyBucketId = DelinquencyBucketsHelper.createDefaultBucket();

        return new PostLoanProductsRequest().name(name).externalId(loanExternalId).enableAccrualActivityPosting(true).shortName(shortName)
                .description(
                        "LP1 with 12% DECLINING BALANCE interest, interest period: Daily, Interest recalculation-Daily, Compounding:none")
                .fundId(fundId).startDate(null).closeDate(null).includeInBorrowerCycle(false).currencyCode("EUR").digitsAfterDecimal(2)
                .inMultiplesOf(1).installmentAmountInMultiplesOf(1).useBorrowerCycle(false).minPrincipal(100.0).principal(1000.0)
                .maxPrincipal(10000.0).minNumberOfRepayments(1).numberOfRepayments(1).maxNumberOfRepayments(30)
                .isLinkedToFloatingInterestRates(false).minInterestRatePerPeriod(0.0).interestRatePerPeriod(12.0)
                .maxInterestRatePerPeriod(30.0).interestRateFrequencyType(3).repaymentEvery(30).repaymentFrequencyType(0L)
                .principalVariationsForBorrowerCycle(principalVariationsForBorrowerCycle)
                .numberOfRepaymentVariationsForBorrowerCycle(numberOfRepaymentVariationsForBorrowerCycle)
                .interestRateVariationsForBorrowerCycle(interestRateVariationsForBorrowerCycle).amortizationType(1).interestType(0)
                .isEqualAmortization(false).interestCalculationPeriodType(0).transactionProcessingStrategyCode("mifos-standard-strategy")
                .daysInYearType(1).daysInMonthType(1).canDefineInstallmentAmount(true).graceOnArrearsAgeing(3).overdueDaysForNPA(179)
                .accountMovesOutOfNPAOnlyOnArrearsCompletion(false).principalThresholdForLastInstallment(50)
                .allowVariableInstallments(false).canUseForTopup(false).holdGuaranteeFunds(false).multiDisburseLoan(false)
                .allowAttributeOverrides(new AllowAttributeOverrides().amortizationType(true).interestType(true)
                        .transactionProcessingStrategyCode(true).interestCalculationPeriodType(true).inArrearsTolerance(true)
                        .repaymentEvery(true).graceOnPrincipalAndInterestPayment(true).graceOnArrearsAgeing(true))
                .outstandingLoanBalance(10000.0).charges(charges).accountingRule(3)
                .fundSourceAccountId(accounts.getSuspenseAccount().getAccountID().longValue())
                .loanPortfolioAccountId(accounts.getLoansReceivableAccount().getAccountID().longValue())
                .transfersInSuspenseAccountId(accounts.getSuspenseAccount().getAccountID().longValue())
                .interestOnLoanAccountId(accounts.getInterestIncomeAccount().getAccountID().longValue())
                .incomeFromFeeAccountId(accounts.getFeeIncomeAccount().getAccountID().longValue())
                .incomeFromPenaltyAccountId(accounts.getPenaltyIncomeAccount().getAccountID().longValue())
                .incomeFromRecoveryAccountId(accounts.getRecoveriesAccount().getAccountID().longValue())
                .writeOffAccountId(accounts.getWrittenOffAccount().getAccountID().longValue())
                .overpaymentLiabilityAccountId(accounts.getOverpaymentAccount().getAccountID().longValue())
                .receivableInterestAccountId(accounts.getInterestReceivableAccount().getAccountID().longValue())
                .receivableFeeAccountId(accounts.getFeeReceivableAccount().getAccountID().longValue())
                .receivablePenaltyAccountId(accounts.getPenaltyReceivableAccount().getAccountID().longValue())
                .goodwillCreditAccountId(accounts.getGoodwillExpenseAccount().getAccountID().longValue())
                .incomeFromGoodwillCreditInterestAccountId(accounts.getInterestIncomeChargeOffAccount().getAccountID().longValue())
                .incomeFromGoodwillCreditFeesAccountId(accounts.getFeeChargeOffAccount().getAccountID().longValue())
                .incomeFromGoodwillCreditPenaltyAccountId(accounts.getFeeChargeOffAccount().getAccountID().longValue())
                .incomeFromChargeOffInterestAccountId(accounts.getInterestIncomeChargeOffAccount().getAccountID().longValue())
                .incomeFromChargeOffFeesAccountId(accounts.getFeeChargeOffAccount().getAccountID().longValue())
                .chargeOffExpenseAccountId(accounts.getChargeOffExpenseAccount().getAccountID().longValue())
                .chargeOffFraudExpenseAccountId(accounts.getChargeOffFraudExpenseAccount().getAccountID().longValue())
                .incomeFromChargeOffPenaltyAccountId(accounts.getPenaltyChargeOffAccount().getAccountID().longValue())
                .dateFormat("dd MMMM yyyy").locale("en").disallowExpectedDisbursements(false)
                .allowApprovedDisbursedAmountsOverApplied(false).delinquencyBucketId(delinquencyBucketId)
                .paymentChannelToFundSourceMappings(paymentChannelToFundSourceMappings)
                .penaltyToIncomeAccountMappings(penaltyToIncomeAccountMappings).feeToIncomeAccountMappings(feeToIncomeAccountMappings)
                .isInterestRecalculationEnabled(false).preClosureInterestCalculationStrategy(1).rescheduleStrategyMethod(3)
                .interestRecalculationCompoundingMethod(0).recalculationRestFrequencyType(2).recalculationRestFrequencyInterval(1)
                .allowPartialPeriodInterestCalculation(false);
    }

    private Long createLoanAccount(final Long clientId, final Long loanProductId, final String loanExternalId) {
        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal(loanAmount).withExternalId(loanExternalId)
                .withLoanTermFrequency("1").withLoanTermFrequencyAsMonths().withNumberOfRepayments("1").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("1").withInterestTypeAsFlatBalance()
                .withAmortizationTypeAsEqualPrincipalPayments().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate(startDate).withSubmittedOnDate(startDate).withLoanType("individual")
                .build(clientId.toString(), loanProductId.toString(), null);

        final Long loanId = applyForLoanFromJson(loanApplicationJSON);
        approveLoan(loanId, approveLoanRequest(Double.parseDouble(loanAmount), startDate));
        disburseLoanWithNetDisbursalAmount(loanId, startDate, loanAmount);
        return loanId;
    }
}
