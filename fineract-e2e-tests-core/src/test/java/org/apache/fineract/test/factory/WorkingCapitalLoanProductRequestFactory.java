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

import static org.apache.fineract.test.factory.LoanProductsRequestFactory.CURRENCY_CODE;
import static org.apache.fineract.test.factory.LoanProductsRequestFactory.DATE_FORMAT;
import static org.apache.fineract.test.factory.LoanProductsRequestFactory.DAYS_IN_MONTH_TYPE_30;
import static org.apache.fineract.test.factory.LoanProductsRequestFactory.DAYS_IN_YEAR_TYPE_360;
import static org.apache.fineract.test.factory.LoanProductsRequestFactory.DELINQUENCY_BUCKET_ID;
import static org.apache.fineract.test.factory.LoanProductsRequestFactory.FUND_ID;
import static org.apache.fineract.test.factory.LoanProductsRequestFactory.LOCALE_EN;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsRequest;
import org.apache.fineract.test.helper.Utils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkingCapitalLoanProductRequestFactory {

    private final LoanProductsRequestFactory loanProductsRequestFactory;

    public static final String WCLP_NAME_PREFIX = "WCLP-";
    public static final String WCLP_DESCRIPTION = "Working Capital Loan Product";

    public PostWorkingCapitalLoanProductsRequest defaultWorkingCapitalLoanProductRequest() {
        String name = Utils.randomStringGenerator(WCLP_NAME_PREFIX, 10);
        String shortName = loanProductsRequestFactory.generateShortNameSafely();

        return new PostWorkingCapitalLoanProductsRequest()//
                .name(name)//
                .shortName(shortName)//
                .description(WCLP_DESCRIPTION)//
                .fundId(FUND_ID)//
                .periodPaymentRate(new BigDecimal(1))//
                .repaymentFrequencyType(PostWorkingCapitalLoanProductsRequest.RepaymentFrequencyTypeEnum.valueOf("DAYS"))//
                .repaymentEvery(DAYS_IN_MONTH_TYPE_30)//
                .startDate(null)//
                .closeDate(null)//
                .currencyCode(CURRENCY_CODE)//
                .digitsAfterDecimal(2)//
                .inMultiplesOf(0)//
                .principal(new BigDecimal(100))//
                .minPrincipal(new BigDecimal(10))//
                .maxPrincipal(new BigDecimal(100000))//
                .amortizationType(PostWorkingCapitalLoanProductsRequest.AmortizationTypeEnum.valueOf("EIR"))//
                .npvDayCount(DAYS_IN_YEAR_TYPE_360)//
                .delinquencyBucketId(DELINQUENCY_BUCKET_ID.longValue())//
                .dateFormat(DATE_FORMAT)//
                .locale(LOCALE_EN);//
    }
}
