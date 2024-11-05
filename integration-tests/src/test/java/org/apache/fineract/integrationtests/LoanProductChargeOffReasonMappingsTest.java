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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.client.models.AllowAttributeOverrides;
import org.apache.fineract.client.models.ChargeData;
import org.apache.fineract.client.models.ChargeToGLAccountMapper;
import org.apache.fineract.client.models.GetChargeOffReasonsToExpenseMappings;
import org.apache.fineract.client.models.GetLoanFeeToIncomeAccountMappings;
import org.apache.fineract.client.models.GetLoanPaymentChannelToFundSourceMappings;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.client.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.common.BusinessStepHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.apache.fineract.integrationtests.common.system.CodeHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(LoanTestLifecycleExtension.class)
public class LoanProductChargeOffReasonMappingsTest extends BaseLoanIntegrationTest {

    private static final String CODE_VALUE_NAME = "ChargeOffReasons";

    private static ResponseSpecification responseSpec;
    private static RequestSpecification requestSpec;
    private static LoanTransactionHelper loanTransactionHelper;

    @BeforeAll
    public static void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        requestSpec.header("Fineract-Platform-TenantId", "default");
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
        BusinessStepHelper businessStepHelper = new BusinessStepHelper();
        // setup COB Business Steps to prevent test failing due other integration test configurations
        businessStepHelper.updateSteps("LOAN_CLOSE_OF_BUSINESS", "APPLY_CHARGE_TO_OVERDUE_LOANS", "LOAN_DELINQUENCY_CLASSIFICATION",
                "CHECK_LOAN_REPAYMENT_DUE", "CHECK_LOAN_REPAYMENT_OVERDUE", "UPDATE_LOAN_ARREARS_AGING", "ADD_PERIODIC_ACCRUAL_ENTRIES",
                "EXTERNAL_ASSET_OWNER_TRANSFER", "ACCRUAL_ACTIVITY_POSTING");
    }

    @Test
    public void testCreateLoanProductWithValidChargeOffReason() {
        final String creationBusinessDay = "15 January 2023";
        runAt(creationBusinessDay, () -> {
            Integer chargeOffReasons = createChargeOffReason();
            Long localLoanProductId = loanTransactionHelper.createLoanProduct(loanProductsRequest(Long.valueOf(chargeOffReasons), 15L))
                    .getResourceId();

            Assertions.assertNotNull(localLoanProductId);
        });
    }

    @Test
    public void testUpdateLoanProductWithValidChargeOffReason() {
        final String creationBusinessDay = "15 January 2023";
        runAt(creationBusinessDay, () -> {
            Integer chargeOffReasons = createChargeOffReason();
            List<GetChargeOffReasonsToExpenseMappings> chargeOffReasonsToExpenseMappings = new ArrayList<>();
            GetChargeOffReasonsToExpenseMappings getChargeOffReasonsToExpenseMappings = new GetChargeOffReasonsToExpenseMappings();
            getChargeOffReasonsToExpenseMappings.setChargeOffReasonCodeValueId(Long.valueOf(chargeOffReasons));
            getChargeOffReasonsToExpenseMappings.setExpenseGLAccountId(15L);
            chargeOffReasonsToExpenseMappings.add(getChargeOffReasonsToExpenseMappings);

            Long localLoanProductId = loanTransactionHelper.updateLoanProduct(1L,
                    new PutLoanProductsProductIdRequest().locale("en").chargeOffReasonsToExpenseMappings(chargeOffReasonsToExpenseMappings))
                    .getResourceId();

            Assertions.assertNotNull(localLoanProductId);
        });
    }

    @Test
    public void testCreateLoanProductWithInvalidGLAccount() {
        final String creationBusinessDay = "15 January 2023";
        runAt(creationBusinessDay, () -> {
            try {
                Integer chargeOffReasons = createChargeOffReason();
                loanTransactionHelper.createLoanProduct(loanProductsRequest(Long.valueOf(chargeOffReasons), 9999L));
            } catch (CallFailedRuntimeException e) {
                Assertions.assertTrue(e.getMessage().contains("validation.msg.glaccount.not.found"));
            }
        });
    }

    @Test
    public void testCreateLoanProductWithInvalidChargeOffReason() {
        final String creationBusinessDay = "15 January 2023";
        runAt(creationBusinessDay, () -> {
            try {
                loanTransactionHelper.createLoanProduct(loanProductsRequest(1L, 12L));
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

        List<GetChargeOffReasonsToExpenseMappings> chargeOffReasonsToExpenseMappings = new ArrayList<>();
        GetChargeOffReasonsToExpenseMappings getChargeOffReasonsToExpenseMappings = new GetChargeOffReasonsToExpenseMappings();
        getChargeOffReasonsToExpenseMappings.setChargeOffReasonCodeValueId(chargeOffReasonId);
        getChargeOffReasonsToExpenseMappings.setExpenseGLAccountId(glAccountId);
        chargeOffReasonsToExpenseMappings.add(getChargeOffReasonsToExpenseMappings);

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
                .chargeOffReasonsToExpenseMappings(chargeOffReasonsToExpenseMappings).feeToIncomeAccountMappings(feeToIncomeAccountMappings)//
                .isInterestRecalculationEnabled(true)//
                .preClosureInterestCalculationStrategy(1)//
                .rescheduleStrategyMethod(3)//
                .interestRecalculationCompoundingMethod(0)//
                .recalculationRestFrequencyType(2)//
                .recalculationRestFrequencyInterval(1)//
                .allowPartialPeriodInterestCalcualtion(false);//
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
