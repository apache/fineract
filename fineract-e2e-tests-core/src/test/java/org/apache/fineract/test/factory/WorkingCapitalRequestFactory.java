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
import static org.apache.fineract.test.factory.LoanProductsRequestFactory.DELINQUENCY_BUCKET_ID;
import static org.apache.fineract.test.factory.LoanProductsRequestFactory.FUND_ID;
import static org.apache.fineract.test.factory.LoanProductsRequestFactory.LOCALE_EN;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PostAllowAttributeOverrides;
import org.apache.fineract.client.models.PostPaymentAllocation;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsRequest;
import org.apache.fineract.client.models.PutWorkingCapitalLoanProductsProductIdRequest;
import org.apache.fineract.test.helper.Utils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkingCapitalRequestFactory {

    private final LoanProductsRequestFactory loanProductsRequestFactory;

    public static final String WCLP_NAME_PREFIX = "WCLP-";
    public static final String WCLP_DESCRIPTION = "Working Capital Loan Product";
    public static final String PENALTY = "PENALTY";
    public static final String FEE = "FEE";
    public static final String PRINCIPAL = "PRINCIPAL";

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
                .delinquencyBucketId(DELINQUENCY_BUCKET_ID.longValue())//
                .dateFormat(DATE_FORMAT)//
                .locale(LOCALE_EN)//
                .paymentAllocation(List.of(//
                        createPaymentAllocation(PostPaymentAllocation.TransactionTypeEnum.DEFAULT.getValue(),
                                List.of(PENALTY, FEE, PRINCIPAL))));//
    }

    public PutWorkingCapitalLoanProductsProductIdRequest defaultWorkingCapitalLoanProductRequestUpdate() {
        String name = Utils.randomStringGenerator(WCLP_NAME_PREFIX, 10);
        String shortName = loanProductsRequestFactory.generateShortNameSafely();

        PostAllowAttributeOverrides allowAttributeOverrides = new PostAllowAttributeOverrides().delinquencyBucketClassification(true)
                .discountDefault(false).flatPercentageAmount(true).periodPaymentFrequencyType(false).periodPaymentFrequency(true);

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
                .amortizationType(PutWorkingCapitalLoanProductsProductIdRequest.AmortizationTypeEnum.EIR)//
                .npvDayCount(DAYS365.value)//
                .delinquencyBucketId(null)//
                .dateFormat(DATE_FORMAT)//
                .locale(LOCALE_EN)//
                .allowAttributeOverrides(allowAttributeOverrides).paymentAllocation(List.of(//
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

}
