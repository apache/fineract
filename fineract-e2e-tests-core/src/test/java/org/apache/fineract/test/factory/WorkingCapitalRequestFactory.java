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
package org.apache.fineract.test.factory;

import static org.apache.fineract.test.data.DaysInYearType.DAYS365;
import static org.apache.fineract.test.factory.LoanProductsRequestFactory.CURRENCY_CODE;
import static org.apache.fineract.test.factory.LoanProductsRequestFactory.CURRENCY_CODE_USD;
import static org.apache.fineract.test.factory.LoanProductsRequestFactory.DATE_FORMAT;
import static org.apache.fineract.test.factory.LoanProductsRequestFactory.DAYS_IN_MONTH_TYPE_30;
import static org.apache.fineract.test.factory.LoanProductsRequestFactory.DAYS_IN_YEAR_TYPE_360;
import static org.apache.fineract.test.factory.LoanProductsRequestFactory.FUND_ID;
import static org.apache.fineract.test.factory.LoanProductsRequestFactory.LOCALE_EN;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.DelinquencyBucketRequest;
import org.apache.fineract.client.models.DelinquencyBucketResponse;
import org.apache.fineract.client.models.MinimumPaymentPeriodAndRule;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PostAllowAttributeOverrides;
import org.apache.fineract.client.models.PostPaymentAllocation;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsRequest.AccountingRuleEnum;
import org.apache.fineract.client.models.PostWorkingCapitalLoanTransactionsRequest;
import org.apache.fineract.client.models.PutWorkingCapitalLoanProductsProductIdRequest;
import org.apache.fineract.client.models.WorkingCapitalBreachData;
import org.apache.fineract.client.models.WorkingCapitalBreachRequest;
import org.apache.fineract.client.models.WorkingCapitalNearBreachData;
import org.apache.fineract.client.models.WorkingCapitalNearBreachRequest;
import org.apache.fineract.test.data.accounttype.AccountTypeResolver;
import org.apache.fineract.test.data.accounttype.DefaultAccountType;
import org.apache.fineract.test.data.delinquency.DelinquencyBucketType;
import org.apache.fineract.test.data.delinquency.DelinquencyFrequencyType;
import org.apache.fineract.test.data.delinquency.DelinquencyMinimumPayment;
import org.apache.fineract.test.data.workingcapitalproduct.WorkingCapitalBreachCalculationType;
import org.apache.fineract.test.data.workingcapitalproduct.WorkingCapitalBreachFrequencyType;
import org.apache.fineract.test.helper.Utils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkingCapitalRequestFactory {

    private final LoanProductsRequestFactory loanProductsRequestFactory;
    private final FineractFeignClient fineractClient;
    private final AccountTypeResolver accountTypeResolver;

    public static final String WCLP_NAME_PREFIX = "WCLP-";
    public static final String WCLP_DESCRIPTION = "Working Capital Loan Product";
    public static final String DEFAULT_WC_DELINQUENCY_BUCKET_NAME = "Default Working Capital delinquency bucket";
    public static final String DEFAULT_WC_BREACH_NAME = "Default Working Capital breach";
    public static final String DEFAULT_WC_NEAR_BREACH_NAME = "Default Working Capital near breach";
    public static final String PENALTY = "PENALTY";
    public static final String FEE = "FEE";
    public static final String PRINCIPAL = "PRINCIPAL";
    public static final Integer DEFAULT_WC_BREACH_FREQUENCY = 2;
    public static final String DEFAULT_WC_BREACH_FREQUENCY_TYPE = WorkingCapitalBreachFrequencyType.MONTHS.getCode();
    public static final String DEFAULT_WC_BREACH_AMOUNT_CALCULATION_TYPE = WorkingCapitalBreachCalculationType.PERCENTAGE.getCode();
    public static final BigDecimal DEFAULT_WC_BREACH_AMOUNT = new BigDecimal("1.23");
    public static final String DEFAULT_WC_BREACH_NAME_PREFIX = "WCB-";
    public static final Integer DEFAULT_WC_NEAR_BREACH_FREQUENCY = 12;
    public static final String DEFAULT_WC_NEAR_BREACH_FREQUENCY_TYPE = WorkingCapitalBreachFrequencyType.DAYS.getCode();
    public static final BigDecimal DEFAULT_WC_NEAR_BREACH_THRESHOLD = new BigDecimal("70.23");

    public PostWorkingCapitalLoanProductsRequest defaultWorkingCapitalLoanProductRequestWithCashAccounting() {
        return defaultWorkingCapitalLoanProductRequest()//
                .accountingRule(AccountingRuleEnum.CASH_BASED)//
                .fundSourceAccountId(accountTypeResolver.resolve(DefaultAccountType.SUSPENSE_CLEARING_ACCOUNT))//
                .loanPortfolioAccountId(accountTypeResolver.resolve(DefaultAccountType.LOANS_RECEIVABLE))//
                .transfersInSuspenseAccountId(accountTypeResolver.resolve(DefaultAccountType.TRANSFER_IN_SUSPENSE_ACCOUNT))//
                .deferredIncomeLiabilityAccountId(accountTypeResolver.resolve(DefaultAccountType.DEFERRED_CAPITALIZED_INCOME))//
                .incomeFromDiscountFeeAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_INCOME))//
                .incomeFromFeeAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_INCOME))//
                .incomeFromPenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_INCOME))//
                .incomeFromRecoveryAccountId(accountTypeResolver.resolve(DefaultAccountType.RECOVERIES))//
                .writeOffAccountId(accountTypeResolver.resolve(DefaultAccountType.WRITTEN_OFF))//
                .overpaymentLiabilityAccountId(accountTypeResolver.resolve(DefaultAccountType.OVERPAYMENT_ACCOUNT))//
                .goodwillCreditAccountId(accountTypeResolver.resolve(DefaultAccountType.GOODWILL_EXPENSE_ACCOUNT))//
                .incomeFromGoodwillCreditFeesAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF))//
                .incomeFromGoodwillCreditPenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF))//
                .incomeFromChargeOffFeesAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF))//
                .incomeFromChargeOffPenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_CHARGE_OFF))//
                .chargeOffExpenseAccountId(accountTypeResolver.resolve(DefaultAccountType.CREDIT_LOSS_BAD_DEBT))//
                .chargeOffFraudExpenseAccountId(accountTypeResolver.resolve(DefaultAccountType.CREDIT_LOSS_BAD_DEBT_FRAUD));//
    }

    /**
     * Creates a Cash based accounting request where optional Income-type GL accounts are overridden with distinct (but
     * still type-correct) accounts to verify each mapping is stored and returned independently.
     */
    public PostWorkingCapitalLoanProductsRequest defaultWorkingCapitalLoanProductRequestWithDistinctCashAccountingMappings() {
        return defaultWorkingCapitalLoanProductRequestWithCashAccounting()//
                .incomeFromPenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.RECOVERIES))//
                .incomeFromGoodwillCreditFeesAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_INCOME_CHARGE_OFF))//
                .incomeFromGoodwillCreditPenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.FEE_INCOME))//
                .incomeFromChargeOffFeesAccountId(accountTypeResolver.resolve(DefaultAccountType.RECOVERIES))//
                .incomeFromChargeOffPenaltyAccountId(accountTypeResolver.resolve(DefaultAccountType.INTEREST_INCOME_CHARGE_OFF));
    }

    public PostWorkingCapitalLoanProductsRequest defaultWorkingCapitalLoanProductRequest() {
        String name = Utils.randomStringGenerator(WCLP_NAME_PREFIX, 10);
        String shortName = loanProductsRequestFactory.generateShortNameSafely();

        return new PostWorkingCapitalLoanProductsRequest()//
                .name(name)//
                .shortName(shortName)//
                .description(WCLP_DESCRIPTION)//
                .fundId(FUND_ID)//
                .periodPaymentRate(new BigDecimal(1))//
                .repaymentFrequencyType(PostWorkingCapitalLoanProductsRequest.RepaymentFrequencyTypeEnum.DAYS)//
                .repaymentEvery(DAYS_IN_MONTH_TYPE_30)//
                .startDate(null)//
                .closeDate(null)//
                .currencyCode(CURRENCY_CODE)//
                .digitsAfterDecimal(2)//
                .inMultiplesOf(1)//
                .principal(new BigDecimal(100))//
                .minPrincipal(new BigDecimal(10))//
                .maxPrincipal(new BigDecimal(100000))//
                .amortizationType(PostWorkingCapitalLoanProductsRequest.AmortizationTypeEnum.EIR)//
                .npvDayCount(DAYS_IN_YEAR_TYPE_360)//
                .delinquencyBucketId(getWCDelinquencyBucketIdByName(DEFAULT_WC_DELINQUENCY_BUCKET_NAME))//
                .dateFormat(DATE_FORMAT)//
                .locale(LOCALE_EN)//
                .accountingRule(AccountingRuleEnum.NONE)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation(PostPaymentAllocation.TransactionTypeEnum.DEFAULT.getValue(),
                                List.of(PENALTY, FEE, PRINCIPAL))));//
    }

    public PostWorkingCapitalLoanProductsRequest defaultWorkingCapitalLoanProductAllowAttributesOverrideRequest() {
        String name = Utils.randomStringGenerator(WCLP_NAME_PREFIX, 10);
        String shortName = loanProductsRequestFactory.generateShortNameSafely();

        PostAllowAttributeOverrides allowAttributeOverrides = new PostAllowAttributeOverrides().delinquencyBucketClassification(true)
                .breach(true).discountDefault(true).periodPaymentFrequencyType(true).periodPaymentFrequency(true);

        return defaultWorkingCapitalLoanProductRequest().name(name)//
                .shortName(shortName)//
                .allowAttributeOverrides(allowAttributeOverrides);
    }

    public PostWorkingCapitalLoanProductsRequest defaultWorkingCapitalLoanProductBreachRequest() {
        String name = Utils.randomStringGenerator(WCLP_NAME_PREFIX, 10);
        String shortName = loanProductsRequestFactory.generateShortNameSafely();

        Long breachId = getWCBreachIdByName(DEFAULT_WC_BREACH_NAME);
        return defaultWorkingCapitalLoanProductAllowAttributesOverrideRequest().name(name)//
                .shortName(shortName)//
                .breachId(breachId);
    }

    public PostWorkingCapitalLoanProductsRequest defaultWorkingCapitalLoanProductBreachNearBreachRequest() {
        String name = Utils.randomStringGenerator(WCLP_NAME_PREFIX, 10);
        String shortName = loanProductsRequestFactory.generateShortNameSafely();

        Long breachId = getWCBreachIdByName(DEFAULT_WC_BREACH_NAME);
        Long nearBreachId = getWCNearBreachIdByName(DEFAULT_WC_NEAR_BREACH_NAME);
        return defaultWorkingCapitalLoanProductAllowAttributesOverrideRequest().name(name)//
                .shortName(shortName)//
                .breachId(breachId) //
                .nearBreachId(nearBreachId); //
    }

    public PutWorkingCapitalLoanProductsProductIdRequest defaultWorkingCapitalLoanProductRequestUpdate() {
        String name = Utils.randomStringGenerator(WCLP_NAME_PREFIX, 10);
        String shortName = loanProductsRequestFactory.generateShortNameSafely();

        PostAllowAttributeOverrides allowAttributeOverrides = new PostAllowAttributeOverrides().delinquencyBucketClassification(true)
                .breach(true).discountDefault(false).periodPaymentFrequencyType(false).periodPaymentFrequency(true);

        return new PutWorkingCapitalLoanProductsProductIdRequest()//
                .name(name)//
                .shortName(shortName)//
                .description(WCLP_DESCRIPTION)//
                .fundId(FUND_ID)//
                .periodPaymentRate(new BigDecimal(1))//
                .repaymentFrequencyType(PutWorkingCapitalLoanProductsProductIdRequest.RepaymentFrequencyTypeEnum.MONTHS)//
                .repaymentEvery(1)//
                .startDate(null)//
                .closeDate(null)//
                .currencyCode(CURRENCY_CODE_USD)//
                .digitsAfterDecimal(2)//
                .inMultiplesOf(1)//
                .principal(new BigDecimal(200))//
                .minPrincipal(new BigDecimal(15))//
                .maxPrincipal(new BigDecimal(300000))//
                .discount(new BigDecimal(50)) //
                .amortizationType(PutWorkingCapitalLoanProductsProductIdRequest.AmortizationTypeEnum.EIR)//
                .npvDayCount(DAYS365.value)//
                .delinquencyBucketId(getWCDelinquencyBucketIdByName(DEFAULT_WC_DELINQUENCY_BUCKET_NAME))//
                .dateFormat(DATE_FORMAT)//
                .locale(LOCALE_EN)//
                .allowAttributeOverrides(allowAttributeOverrides)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation(PostPaymentAllocation.TransactionTypeEnum.DEFAULT.getValue(), //
                                List.of(FEE, PRINCIPAL, PENALTY))));//
    }

    public List<PostPaymentAllocation> invalidNumberOfPaymentAllocationRulesForWorkingCapitalLoanProductCreateRequest() {
        return List.of(//
                createPaymentAllocation(PostPaymentAllocation.TransactionTypeEnum.DEFAULT.getValue(), //
                        List.of(FEE, PRINCIPAL, PENALTY, "INTEREST")));//
    }

    public List<PostPaymentAllocation> invalidPaymentAllocationRulesForWorkingCapitalLoanProductCreateRequest() {
        return List.of(//
                createPaymentAllocation(PostPaymentAllocation.TransactionTypeEnum.DEFAULT.getValue(), //
                        List.of(FEE, PRINCIPAL, "INTEREST")));//
    }

    public List<PostPaymentAllocation> invalidNumberOfPaymentAllocationRulesForWorkingCapitalLoanProductUpdateRequest() {
        return List.of(//
                createPaymentAllocation(PostPaymentAllocation.TransactionTypeEnum.DEFAULT.getValue(), //
                        List.of(FEE, PRINCIPAL, PENALTY, "INTEREST")));//
    }

    public List<PostPaymentAllocation> invalidPaymentAllocationRulesForWorkingCapitalLoanProductUpdateRequest() {
        return List.of(//
                createPaymentAllocation(PostPaymentAllocation.TransactionTypeEnum.DEFAULT.getValue(), //
                        List.of(FEE, PRINCIPAL, "INTEREST")));//
    }

    public static PostPaymentAllocation createPaymentAllocation(String transactionType, List<String> paymentAllocationRules) {
        PostPaymentAllocation.TransactionTypeEnum transactionTypeName = PostPaymentAllocation.TransactionTypeEnum.valueOf(transactionType);
        PostPaymentAllocation paymentAllocationData = new PostPaymentAllocation();
        paymentAllocationData.setTransactionType(transactionTypeName);

        List<PaymentAllocationOrder> paymentAllocationOrders = new ArrayList<>();
        for (int i = 0; i < paymentAllocationRules.size(); i++) {
            PaymentAllocationOrder e = new PaymentAllocationOrder();
            e.setOrder(i + 1);
            e.setPaymentAllocationRule(paymentAllocationRules.get(i));
            paymentAllocationOrders.add(e);
        }

        paymentAllocationData.setPaymentAllocationOrder(paymentAllocationOrders);
        return paymentAllocationData;
    }

    public DelinquencyBucketRequest defaultWorkingCapitalDelinquencyBucketRequest() {
        return new DelinquencyBucketRequest() //
                .name("DB-WCL-" + Utils.randomStringGenerator(8)) //
                .bucketType(DelinquencyBucketType.WORKING_CAPITAL.name())//
                .ranges(List.of(1L)) //
                .minimumPaymentPeriodAndRule(new MinimumPaymentPeriodAndRule() //
                        .frequency(1) //
                        .minimumPaymentType(DelinquencyMinimumPayment.PERCENTAGE.name()) //
                        .frequencyType(DelinquencyFrequencyType.WEEKS.name()) //
                        .minimumPayment(new BigDecimal("1.23")));
    }

    public WorkingCapitalBreachRequest defaultWorkingCapitalBreachRequest() {
        return new WorkingCapitalBreachRequest() //
                .name(Utils.randomStringGenerator(DEFAULT_WC_BREACH_NAME_PREFIX, 12)) //
                .breachFrequency(DEFAULT_WC_BREACH_FREQUENCY) //
                .breachFrequencyType(DEFAULT_WC_BREACH_FREQUENCY_TYPE) //
                .breachAmountCalculationType(DEFAULT_WC_BREACH_AMOUNT_CALCULATION_TYPE) //
                .breachAmount(DEFAULT_WC_BREACH_AMOUNT);
    }

    public WorkingCapitalNearBreachRequest defaultWorkingCapitalNearBreachRequest() {
        return new WorkingCapitalNearBreachRequest() //
                .nearBreachName("NearBreach-WCL-" + Utils.randomStringGenerator(8)) //
                .nearBreachFrequency(DEFAULT_WC_NEAR_BREACH_FREQUENCY) //
                .nearBreachFrequencyType(DEFAULT_WC_NEAR_BREACH_FREQUENCY_TYPE) //
                .nearBreachThreshold(DEFAULT_WC_NEAR_BREACH_THRESHOLD); //
    }

    public PostWorkingCapitalLoanTransactionsRequest defaultWorkingCapitalLoanRepaymentRequest() {
        return new PostWorkingCapitalLoanTransactionsRequest() //
                .dateFormat(DATE_FORMAT) //
                .locale(LOCALE_EN);
    }

    private Long getWCDelinquencyBucketIdByName(String bucketName) {
        try {
            List<DelinquencyBucketResponse> buckets = fineractClient.delinquencyRangeAndBucketsManagement().getBuckets(Map.of());
            return buckets.stream().filter(b -> bucketName.equals(b.getName())).findFirst().map(DelinquencyBucketResponse::getId)
                    .orElseThrow(() -> new RuntimeException("Working Capital delinquency bucket not found with name: " + bucketName));
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch Working Capital delinquency bucket by name: " + bucketName, e);
        }
    }

    private Long getWCBreachIdByName(String breachName) {
        try {
            List<WorkingCapitalBreachData> breaches = fineractClient.workingCapitalBreaches().retrieveAllWorkingCapitalBreaches(Map.of());
            return breaches.stream().filter(b -> breachName.equals(b.getName())).findFirst().map(WorkingCapitalBreachData::getId)
                    .orElseThrow(() -> new RuntimeException("Working Capital Breach not found with name: " + breachName));
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch Working Capital Breacht by name: " + breachName, e);
        }
    }

    private Long getWCNearBreachIdByName(String breachName) {
        try {
            List<WorkingCapitalNearBreachData> breaches = fineractClient.workingCapitalNearBreaches()
                    .retrieveAllWorkingCapitalNearBreaches(Map.of());
            return breaches.stream().filter(b -> breachName.equals(b.getName())).findFirst().map(WorkingCapitalNearBreachData::getId)
                    .orElseThrow(() -> new RuntimeException("Working Capital Breach not found with name: " + breachName));
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch Working Capital Breacht by name: " + breachName, e);
        }
    }

}
