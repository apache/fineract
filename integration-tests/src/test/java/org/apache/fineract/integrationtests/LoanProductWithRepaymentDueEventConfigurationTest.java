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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.HashMap;
import java.util.UUID;
import org.apache.fineract.client.models.GetDelinquencyBucketsResponse;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoanProductWithRepaymentDueEventConfigurationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private ClientHelper clientHelper;
    private LoanTransactionHelper loanTransactionHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void loanProductCreationWithDueDaysConfigurationForRepaymentEventTest() {
        // Loan ExternalId
        String loanExternalIdStr = UUID.randomUUID().toString();

        // Delinquency Bucket
        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketId);

        // event days configuration
        Integer dueDaysForRepaymentEvent = 1;
        Integer overDueDaysForRepaymentEvent = 2;

        // Client and Loan account creation

        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
        Integer loanProductId = createLoanProductWithDueDaysForRepaymentEvent(loanTransactionHelper, delinquencyBucketId,
                dueDaysForRepaymentEvent, overDueDaysForRepaymentEvent);
        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = loanTransactionHelper.getLoanProduct(loanProductId);
        assertNotNull(getLoanProductsProductResponse);
        assertNotNull(getLoanProductsProductResponse.getDueDaysForRepaymentEvent());
        assertNotNull(getLoanProductsProductResponse.getOverDueDaysForRepaymentEvent());
        assertEquals(getLoanProductsProductResponse.getDueDaysForRepaymentEvent(), dueDaysForRepaymentEvent);
        assertEquals(getLoanProductsProductResponse.getOverDueDaysForRepaymentEvent(), overDueDaysForRepaymentEvent);
    }

    @Test
    public void loanProductUpdateWithDueDaysConfigurationForRepaymentEventTest() {
        // Loan ExternalId
        String loanExternalIdStr = UUID.randomUUID().toString();

        // Delinquency Bucket
        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketId);

        // Client and Loan account creation

        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(loanTransactionHelper,
                delinquencyBucketId);
        assertNotNull(getLoanProductsProductResponse);

        // Modify Loan Product
        PutLoanProductsProductIdResponse loanProductModifyResponse = updateLoanProduct(loanTransactionHelper,
                getLoanProductsProductResponse.getId());
        assertNotNull(loanProductModifyResponse);

    }

    private PutLoanProductsProductIdResponse updateLoanProduct(LoanTransactionHelper loanTransactionHelper, Long id) {
        // event days configuration
        Integer dueDaysForRepaymentEvent = 1;
        Integer overDueDaysForRepaymentEvent = 2;
        final PutLoanProductsProductIdRequest requestModifyLoan = new PutLoanProductsProductIdRequest()
                .dueDaysForRepaymentEvent(dueDaysForRepaymentEvent).overDueDaysForRepaymentEvent(overDueDaysForRepaymentEvent).locale("en");
        return loanTransactionHelper.updateLoanProduct(id, requestModifyLoan);
    }

    private GetLoanProductsProductIdResponse createLoanProduct(final LoanTransactionHelper loanTransactionHelper,
            final Integer delinquencyBucketId) {
        final HashMap<String, Object> loanProductMap = new LoanProductTestBuilder().build(null, delinquencyBucketId);
        final Integer loanProductId = loanTransactionHelper.getLoanProductId(Utils.convertToJson(loanProductMap));
        return loanTransactionHelper.getLoanProduct(loanProductId);
    }

    private Integer createLoanProductWithDueDaysForRepaymentEvent(final LoanTransactionHelper loanTransactionHelper,
            final Integer delinquencyBucketId, Integer dueDaysForRepaymentEvent, Integer overDueDaysForRepaymentEvent) {
        final HashMap<String, Object> loanProductMap = new LoanProductTestBuilder().withDueDaysForRepaymentEvent(dueDaysForRepaymentEvent)
                .withOverDueDaysForRepaymentEvent(overDueDaysForRepaymentEvent).build(null, delinquencyBucketId);
        final Integer loanProductId = loanTransactionHelper.getLoanProductId(Utils.convertToJson(loanProductMap));
        return loanProductId;
    }

}
