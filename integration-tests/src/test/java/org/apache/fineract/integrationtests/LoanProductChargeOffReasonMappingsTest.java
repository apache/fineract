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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.client.models.AllowAttributeOverrides;
import org.apache.fineract.client.models.ChargeData;
import org.apache.fineract.client.models.ChargeToGLAccountMapper;
import org.apache.fineract.client.models.GetLoanFeeToIncomeAccountMappings;
import org.apache.fineract.client.models.GetLoanPaymentChannelToFundSourceMappings;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.PostChargeOffReasonToExpenseAccountMappings;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.client.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.apache.fineract.integrationtests.common.system.CodeHelper;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LoanProductChargeOffReasonMappingsTest extends BaseLoanIntegrationTest {

    private static final String CODE_VALUE_NAME = "ChargeOffReasons";
    private final Account expenseAccount = accountHelper.createExpenseAccount();
    private final Account otherExpenseAccount = accountHelper.createExpenseAccount();

    @Test
    public void testCreateAndUpdateLoanProductWithValidChargeOffReason() {
        runAt("15 January 2023", () -> {
            Integer chargeOffReasons = createChargeOffReason();
            Long localLoanProductId = loanTransactionHelper
                    .createLoanProduct(loanProductsRequest(Long.valueOf(chargeOffReasons), expenseAccount.getAccountID().longValue()))
                    .getResourceId();

            Assertions.assertNotNull(localLoanProductId);

            GetLoanProductsProductIdResponse loanProductDetails = loanTransactionHelper.getLoanProduct(localLoanProductId.intValue());
            Assertions.assertEquals(expenseAccount.getAccountID().longValue(),
                    loanProductDetails.getChargeOffReasonToExpenseAccountMappings().get(0).getExpenseAccount().getId());
            Assertions.assertEquals(Long.valueOf(chargeOffReasons),
                    loanProductDetails.getChargeOffReasonToExpenseAccountMappings().get(0).getChargeOffReasonCodeValue().getId());

            List<PostChargeOffReasonToExpenseAccountMappings> chargeOffReasonToExpenseAccountMappings = createPostChargeOffReasonToExpenseAccountMappings(
                    Long.valueOf(chargeOffReasons), otherExpenseAccount.getAccountID().longValue());

            loanTransactionHelper.updateLoanProduct(localLoanProductId, new PutLoanProductsProductIdRequest().locale("en")
                    .chargeOffReasonToExpenseAccountMappings(chargeOffReasonToExpenseAccountMappings)).getResourceId();

            loanProductDetails = loanTransactionHelper.getLoanProduct(localLoanProductId.intValue());
            Assertions.assertEquals(otherExpenseAccount.getAccountID().longValue(),
                    loanProductDetails.getChargeOffReasonToExpenseAccountMappings().get(0).getExpenseAccount().getId());
            Assertions.assertEquals(Long.valueOf(chargeOffReasons),
                    loanProductDetails.getChargeOffReasonToExpenseAccountMappings().get(0).getChargeOffReasonCodeValue().getId());
        });
    }

    @Test
    public void testCreateLoanProductWithInvalidGLAccount() {
        runAt("15 January 2023", () -> {
            try {
                Integer chargeOffReasons = createChargeOffReason();
                loanTransactionHelper.createLoanProduct(loanProductsRequest(Long.valueOf(chargeOffReasons), -1L));
            } catch (CallFailedRuntimeException e) {
                Assertions.assertTrue(e.getMessage().contains("validation.msg.glaccount.not.found"));
            }
        });
    }

    @Test
    public void testCreateLoanProductWithInvalidChargeOffReason() {
        runAt("15 January 2023", () -> {
            try {
                loanTransactionHelper.createLoanProduct(loanProductsRequest(-1L, expenseAccount.getAccountID().longValue()));
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
        List<ChargeData> charges = new ArrayList<>();
        List<ChargeToGLAccountMapper> penaltyToIncomeAccountMappings = new ArrayList<>();
        List<GetLoanFeeToIncomeAccountMappings> feeToIncomeAccountMappings = new ArrayList<>();

        List<PostChargeOffReasonToExpenseAccountMappings> chargeOffReasonToExpenseAccountMappings = createPostChargeOffReasonToExpenseAccountMappings(
                chargeOffReasonId, glAccountId);

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
                .chargeOffReasonToExpenseAccountMappings(chargeOffReasonToExpenseAccountMappings)
                .feeToIncomeAccountMappings(feeToIncomeAccountMappings)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(3)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .allowPartialPeriodInterestCalcualtion(false);//
    }

    @NotNull
    private static List<PostChargeOffReasonToExpenseAccountMappings> createPostChargeOffReasonToExpenseAccountMappings(
            Long chargeOffReasonId, Long glAccountId) {
        List<PostChargeOffReasonToExpenseAccountMappings> chargeOffReasonToExpenseAccountMappings = new ArrayList<>();
        PostChargeOffReasonToExpenseAccountMappings chargeOffReasonToExpenseAccountMapping = new PostChargeOffReasonToExpenseAccountMappings();
        chargeOffReasonToExpenseAccountMapping.setChargeOffReasonCodeValueId(chargeOffReasonId);
        chargeOffReasonToExpenseAccountMapping.setExpenseAccountId(glAccountId);
        chargeOffReasonToExpenseAccountMappings.add(chargeOffReasonToExpenseAccountMapping);
        return chargeOffReasonToExpenseAccountMappings;
    }

    private Integer createChargeOffReason() {
        Integer chargeOffReasonId;
        HashMap<String, Object> codes = CodeHelper.getCodeByName(requestSpec, responseSpec, CODE_VALUE_NAME);
        if (codes.isEmpty()) {
            CodeHelper.createCode(requestSpec, responseSpec, CODE_VALUE_NAME, "");
        }
        codes = CodeHelper.getCodeByName(requestSpec, responseSpec, CODE_VALUE_NAME);
        Integer codeId = (Integer) codes.get("id");
        HashMap<String, Object> codeValues = CodeHelper.getOrCreateCodeValueByCodeIdAndCodeName(requestSpec, responseSpec, codeId,
                CODE_VALUE_NAME, 1);
        chargeOffReasonId = (Integer) codeValues.get("id");
        return chargeOffReasonId;
    }
}
