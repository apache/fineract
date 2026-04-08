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

import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FixedLengthLoanProductIntegrationTest extends BaseLoanIntegrationTest {

    @Test
    public void testCreateReadUpdateReadLoanProductWithFixedLength() {
        // create with 4
        PostLoanProductsRequest loanProductsRequest = fixedLengthLoanProduct(4);
        PostLoanProductsResponse loanProduct = loanProductHelper.createLoanProduct(loanProductsRequest);
        Assertions.assertNotNull(loanProduct.getResourceId());

        // read
        GetLoanProductsProductIdResponse getLoanProductsProductIdResponse = loanProductHelper
                .retrieveLoanProductById(loanProduct.getResourceId());
        Assertions.assertEquals(4, getLoanProductsProductIdResponse.getFixedLength());

        // update to 5
        PutLoanProductsProductIdRequest updateRequest = new PutLoanProductsProductIdRequest().fixedLength(5).locale("en");
        PutLoanProductsProductIdResponse putLoanProductsProductIdResponse = loanProductHelper
                .updateLoanProductById(loanProduct.getResourceId(), updateRequest);
        Assertions.assertNotNull(putLoanProductsProductIdResponse.getResourceId());

        // read again
        getLoanProductsProductIdResponse = loanProductHelper.retrieveLoanProductById(loanProduct.getResourceId());
        Assertions.assertEquals(5, getLoanProductsProductIdResponse.getFixedLength());

        // update to null
        loanTransactionHelper.updateLoanProduct(putLoanProductsProductIdResponse.getResourceId(), """
                {
                    "fixedLength": null,
                    "locale": "en"
                }
                """);

        // read again
        getLoanProductsProductIdResponse = loanProductHelper.retrieveLoanProductById(loanProduct.getResourceId());
        Assertions.assertNull(getLoanProductsProductIdResponse.getFixedLength());
    }

    @Test
    public void testLoanApplicationWithFixedLengthInheritedFromLoanProduct() {
        runAt("01 January 2023", () -> {
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            PostLoanProductsResponse loanProduct = loanProductHelper.createLoanProduct(fixedLengthLoanProduct(4));
            Assertions.assertNotNull(loanProduct.getResourceId());

            PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProduct.getResourceId(), "01 January 2023", 1000.0, 4);
            applicationRequest = applicationRequest
                    .transactionProcessingStrategyCode(LoanProductTestBuilder.ADVANCED_PAYMENT_ALLOCATION_STRATEGY);

            PostLoansResponse loanResponse = loanTransactionHelper.applyLoan(applicationRequest);
            Long loanId = loanResponse.getLoanId();

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            Assertions.assertEquals(4, loanDetails.getFixedLength());
        });
    }

    @Test
    public void testLoanApplicationWithFixedLengthOverriddenByLoanApplication() {
        runAt("01 January 2023", () -> {
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            PostLoanProductsResponse loanProduct = loanProductHelper.createLoanProduct(fixedLengthLoanProduct(4));
            Assertions.assertNotNull(loanProduct.getResourceId());

            PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProduct.getResourceId(), "01 January 2023", 1000.0, 4);
            applicationRequest = applicationRequest.fixedLength(5).repaymentEvery(1).repaymentFrequencyType(2).loanTermFrequencyType(2)
                    .loanTermFrequency(4).transactionProcessingStrategyCode(LoanProductTestBuilder.ADVANCED_PAYMENT_ALLOCATION_STRATEGY);

            PostLoansResponse loanResponse = loanTransactionHelper.applyLoan(applicationRequest);
            Long loanId = loanResponse.getLoanId();

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            Assertions.assertEquals(5, loanDetails.getFixedLength());
        });
    }

    private PostLoanProductsRequest fixedLengthLoanProduct(Integer fixedLength) {
        return createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation() //
                .numberOfRepayments(4).repaymentEvery(1) //
                .repaymentFrequencyType(RepaymentFrequencyType.MONTHS.longValue()) //
                .loanScheduleType(LoanScheduleType.PROGRESSIVE.toString()) //
                .transactionProcessingStrategyCode(AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY) //
                .loanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.name()) //
                .interestRatePerPeriod(0.0).fixedLength(fixedLength);
    }

}
