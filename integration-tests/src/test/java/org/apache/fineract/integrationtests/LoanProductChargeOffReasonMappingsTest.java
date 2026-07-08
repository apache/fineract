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

import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.AllowAttributeOverrides;
import org.apache.fineract.client.models.GetLoanPaymentChannelToFundSourceMappings;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.LoanProductChargeData;
import org.apache.fineract.client.models.LoanProductChargeToGLAccountMapper;
import org.apache.fineract.client.models.PostChargeOffReasonToExpenseAccountMappings;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignDelinquencyHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignFundHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.lang.NonNull;

public class LoanProductChargeOffReasonMappingsTest extends FeignLoanTestBase {

    @Test
    public void testCreateAndUpdateLoanProductWithValidChargeOffReason() {
        runAt("15 January 2023", () -> {
            final Account expenseAccount = accountHelper.createExpenseAccount("expense");
            final Account otherExpenseAccount = accountHelper.createExpenseAccount("expense");
            Long chargeOffReasons = createChargeOffReason();
            Long localLoanProductId = createLoanProduct(loanProductsRequest(chargeOffReasons, expenseAccount.getAccountID().longValue()));

            Assertions.assertNotNull(localLoanProductId);

            GetLoanProductsProductIdResponse loanProductDetails = retrieveLoanProduct(localLoanProductId);
            Assertions.assertEquals(expenseAccount.getAccountID().longValue(),
                    loanProductDetails.getChargeOffReasonToExpenseAccountMappings().get(0).getExpenseAccount().getId());
            Assertions.assertEquals(chargeOffReasons,
                    loanProductDetails.getChargeOffReasonToExpenseAccountMappings().get(0).getReasonCodeValue().getId());

            List<PostChargeOffReasonToExpenseAccountMappings> chargeOffReasonToExpenseAccountMappings = createPostChargeOffReasonToExpenseAccountMappings(
                    chargeOffReasons, otherExpenseAccount.getAccountID().longValue());

            updateLoanProduct(localLoanProductId, new PutLoanProductsProductIdRequest().locale("en")
                    .chargeOffReasonToExpenseAccountMappings(chargeOffReasonToExpenseAccountMappings));

            loanProductDetails = retrieveLoanProduct(localLoanProductId);
            Assertions.assertEquals(otherExpenseAccount.getAccountID().longValue(),
                    loanProductDetails.getChargeOffReasonToExpenseAccountMappings().get(0).getExpenseAccount().getId());
            Assertions.assertEquals(chargeOffReasons,
                    loanProductDetails.getChargeOffReasonToExpenseAccountMappings().get(0).getReasonCodeValue().getId());
        });
    }

    @Test
    public void testCreateLoanProductWithInvalidGLAccount() {
        runAt("15 January 2023", () -> {
            try {
                Long chargeOffReasons = createChargeOffReason();
                createLoanProduct(loanProductsRequest(chargeOffReasons, -1L));
            } catch (CallFailedRuntimeException e) {
                Assertions.assertTrue(e.getMessage().contains("validation.msg.glaccount.not.found"));
            }
        });
    }

    @Test
    public void testCreateLoanProductWithInvalidChargeOffReason() {
        runAt("15 January 2023", () -> {
            try {
                final Account expenseAccount = accountHelper.createExpenseAccount("expense");
                createLoanProduct(loanProductsRequest(-1L, expenseAccount.getAccountID().longValue()));
            } catch (CallFailedRuntimeException e) {
                Assertions.assertTrue(e.getMessage().contains("validation.msg.chargeoffreason.invalid"));
            }
        });
    }

    private PostLoanProductsRequest loanProductsRequest(Long chargeOffReasonId, Long glAccountId) {
        String name = Utils.uniqueRandomStringGenerator("LOAN_PRODUCT_", 6);
        String shortName = Utils.uniqueRandomStringGenerator("", 4);

        List<Integer> principalVariationsForBorrowerCycle = new ArrayList<>();
        List<Integer> numberOfRepaymentVariationsForBorrowerCycle = new ArrayList<>();
        List<Integer> interestRateVariationsForBorrowerCycle = new ArrayList<>();
        List<LoanProductChargeData> charges = new ArrayList<>();
        List<LoanProductChargeToGLAccountMapper> penaltyToIncomeAccountMappings = new ArrayList<>();
        List<LoanProductChargeToGLAccountMapper> feeToIncomeAccountMappings = new ArrayList<>();

        List<PostChargeOffReasonToExpenseAccountMappings> chargeOffReasonToExpenseAccountMappings = createPostChargeOffReasonToExpenseAccountMappings(
                chargeOffReasonId, glAccountId);

        List<GetLoanPaymentChannelToFundSourceMappings> paymentChannelToFundSourceMappings = new ArrayList<>();
        GetLoanPaymentChannelToFundSourceMappings loanPaymentChannelToFundSourceMappings = new GetLoanPaymentChannelToFundSourceMappings();
        loanPaymentChannelToFundSourceMappings.fundSourceAccountId(getAccounts().getFundSource().getAccountID().longValue());
        loanPaymentChannelToFundSourceMappings.paymentTypeId(1L);
        paymentChannelToFundSourceMappings.add(loanPaymentChannelToFundSourceMappings);

        final Long fundId = new FeignFundHelper(FineractFeignClientHelper.getFineractFeignClient()).createFund().getResourceId();
        Assertions.assertNotNull(fundId);

        final Long delinquencyBucketId = new FeignDelinquencyHelper(FineractFeignClientHelper.getFineractFeignClient())
                .createDefaultBucket();
        Assertions.assertNotNull(delinquencyBucketId);

        return new PostLoanProductsRequest()//
                .name(name)//
                .enableAccrualActivityPosting(true)//
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

                .fundSourceAccountId(getAccounts().getSuspenseAccount().getAccountID().longValue())//
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
                .chargeOffExpenseAccountId(getAccounts().getChargeOffExpenseAccount().getAccountID().longValue())//
                .chargeOffFraudExpenseAccountId(getAccounts().getChargeOffFraudExpenseAccount().getAccountID().longValue())//
                .incomeFromChargeOffPenaltyAccountId(getAccounts().getPenaltyChargeOffAccount().getAccountID().longValue())//

                .dateFormat("dd MMMM yyyy")//
                .locale("en")//
                .disallowExpectedDisbursements(false)//
                .allowApprovedDisbursedAmountsOverApplied(false)//
                .delinquencyBucketId(delinquencyBucketId.longValue())//
                .paymentChannelToFundSourceMappings(paymentChannelToFundSourceMappings)//
                .penaltyToIncomeAccountMappings(penaltyToIncomeAccountMappings)//
                .chargeOffReasonToExpenseAccountMappings(chargeOffReasonToExpenseAccountMappings)
                .feeToIncomeAccountMappings(feeToIncomeAccountMappings)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(3)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .allowPartialPeriodInterestCalculation(false);//
    }

    @NonNull
    private static List<PostChargeOffReasonToExpenseAccountMappings> createPostChargeOffReasonToExpenseAccountMappings(
            Long chargeOffReasonId, Long glAccountId) {
        List<PostChargeOffReasonToExpenseAccountMappings> chargeOffReasonToExpenseAccountMappings = new ArrayList<>();
        PostChargeOffReasonToExpenseAccountMappings chargeOffReasonToExpenseAccountMapping = new PostChargeOffReasonToExpenseAccountMappings();
        chargeOffReasonToExpenseAccountMapping.setChargeOffReasonCodeValueId(chargeOffReasonId);
        chargeOffReasonToExpenseAccountMapping.setExpenseAccountId(glAccountId);
        chargeOffReasonToExpenseAccountMappings.add(chargeOffReasonToExpenseAccountMapping);
        return chargeOffReasonToExpenseAccountMappings;
    }

    private Long createChargeOffReason() {
        return codeHelper.createChargeOffCodeValue(Utils.uniqueRandomStringGenerator("REASON_", 6), 1);
    }
}
