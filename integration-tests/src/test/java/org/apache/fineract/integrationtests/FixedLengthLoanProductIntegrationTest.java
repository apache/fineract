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
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(LoanTestLifecycleExtension.class)
public class FixedLengthLoanProductIntegrationTest extends BaseLoanIntegrationTest {

    @Test
    public void testCreateReadUpdateReadLoanProductWithFixedLength() {
        // create with 5
        PostLoanProductsRequest loanProductsRequest = fixedLengthLoanProduct(5);
        PostLoanProductsResponse loanProduct = loanProductHelper.createLoanProduct(loanProductsRequest);
        Assertions.assertNotNull(loanProduct.getResourceId());

        // read
        GetLoanProductsProductIdResponse getLoanProductsProductIdResponse = loanProductHelper
                .retrieveLoanProductById(loanProduct.getResourceId());
        Assertions.assertEquals(5, getLoanProductsProductIdResponse.getFixedLength());

        // update to 6
        PutLoanProductsProductIdRequest updateRequest = new PutLoanProductsProductIdRequest().fixedLength(6).locale("en");
        PutLoanProductsProductIdResponse putLoanProductsProductIdResponse = loanProductHelper
                .updateLoanProductById(loanProduct.getResourceId(), updateRequest);
        Assertions.assertNotNull(putLoanProductsProductIdResponse.getResourceId());

        // read again
        getLoanProductsProductIdResponse = loanProductHelper.retrieveLoanProductById(loanProduct.getResourceId());
        Assertions.assertEquals(6, getLoanProductsProductIdResponse.getFixedLength());

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

            PostLoanProductsResponse loanProduct = loanProductHelper.createLoanProduct(fixedLengthLoanProduct(6));
            Assertions.assertNotNull(loanProduct.getResourceId());

            Long loanId = applyAndApproveLoan(clientId, loanProduct.getResourceId(), "01 January 2023", 1000.0);
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            Assertions.assertEquals(6, loanDetails.getFixedLength());
        });
    }

    @Test
    public void testLoanApplicationWithFixedLengthOverriddenByLoanApplication() {
        runAt("01 January 2023", () -> {
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            PostLoanProductsResponse loanProduct = loanProductHelper.createLoanProduct(fixedLengthLoanProduct(6));
            Assertions.assertNotNull(loanProduct.getResourceId());

            Long loanId = applyAndApproveLoan(clientId, loanProduct.getResourceId(), "01 January 2023", 1000.0, 1, //
                    loanApplication -> loanApplication.fixedLength(5) //
            );
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            Assertions.assertEquals(5, loanDetails.getFixedLength());
        });
    }

    private PostLoanProductsRequest fixedLengthLoanProduct(Integer fixedLength) {
        return createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct().numberOfRepayments(4)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(RepaymentFrequencyType.MONTHS.longValue())//
                .transactionProcessingStrategyCode("mifos-standard-strategy").fixedLength(fixedLength);
    }

}
